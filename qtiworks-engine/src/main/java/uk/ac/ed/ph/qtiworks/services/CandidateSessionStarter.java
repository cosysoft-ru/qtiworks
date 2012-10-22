/* Copyright (c) 2012, University of Edinburgh.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *
 * * Neither the name of the University of Edinburgh nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 * This software is derived from (and contains code from) QTItools and MathAssessEngine.
 * QTItools is (c) 2008, University of Southampton.
 * MathAssessEngine is (c) 2010, University of Edinburgh.
 */
package uk.ac.ed.ph.qtiworks.services;

import uk.ac.ed.ph.qtiworks.QtiWorksLogicException;
import uk.ac.ed.ph.qtiworks.QtiWorksRuntimeException;
import uk.ac.ed.ph.qtiworks.base.services.Auditor;
import uk.ac.ed.ph.qtiworks.domain.DomainConstants;
import uk.ac.ed.ph.qtiworks.domain.DomainEntityNotFoundException;
import uk.ac.ed.ph.qtiworks.domain.IdentityContext;
import uk.ac.ed.ph.qtiworks.domain.Privilege;
import uk.ac.ed.ph.qtiworks.domain.PrivilegeException;
import uk.ac.ed.ph.qtiworks.domain.dao.AssessmentDao;
import uk.ac.ed.ph.qtiworks.domain.dao.CandidateSessionDao;
import uk.ac.ed.ph.qtiworks.domain.dao.DeliveryDao;
import uk.ac.ed.ph.qtiworks.domain.entities.Assessment;
import uk.ac.ed.ph.qtiworks.domain.entities.AssessmentPackage;
import uk.ac.ed.ph.qtiworks.domain.entities.CandidateItemEvent;
import uk.ac.ed.ph.qtiworks.domain.entities.CandidateItemEventType;
import uk.ac.ed.ph.qtiworks.domain.entities.CandidateSession;
import uk.ac.ed.ph.qtiworks.domain.entities.CandidateSessionStatus;
import uk.ac.ed.ph.qtiworks.domain.entities.Delivery;
import uk.ac.ed.ph.qtiworks.domain.entities.DeliveryType;
import uk.ac.ed.ph.qtiworks.domain.entities.ItemDeliverySettings;
import uk.ac.ed.ph.qtiworks.domain.entities.User;
import uk.ac.ed.ph.qtiworks.domain.entities.UserType;

import uk.ac.ed.ph.jqtiplus.internal.util.Assert;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Helper service for launching new candidate sessions on an
 * {@link AssessmentItem} or {@link AssessmentTest}
 *
 * @author David McKain
 */
@Service
@Transactional(propagation=Propagation.REQUIRED)
public class CandidateSessionStarter {

    @Resource
    private Auditor auditor;

    @Resource
    private IdentityContext identityContext;

    @Resource
    private CandidateAuditLogger candidateAuditLogger;

    @Resource
    private CandidateDataServices candidateDataServices;

    @Resource
    private EntityGraphService entityGraphService;

    @Resource
    private AssessmentDao assessmentDao;

    @Resource
    private DeliveryDao deliveryDao;

    @Resource
    private CandidateSessionDao candidateSessionDao;

    //-------------------------------------------------
    // System samples

    public Delivery lookupSystemSampleDelivery(final long aid)
            throws DomainEntityNotFoundException, PrivilegeException {
        final Assessment assessment = lookupSampleAssessment(aid);
        final List<Delivery> systemDemoDeliveries = deliveryDao.getForAssessmentAndType(assessment, DeliveryType.SYSTEM_DEMO);
        if (systemDemoDeliveries.size()!=1) {
            throw new QtiWorksLogicException("Expected system sample Assessment with ID " + aid
                    + " to have exactly 1 system demo deliverable associated with it");
        }
        return systemDemoDeliveries.get(0);
    }

    private Assessment lookupSampleAssessment(final long aid)
            throws DomainEntityNotFoundException, PrivilegeException {
        final Assessment assessment = assessmentDao.requireFindById(aid);
        final User caller = identityContext.getCurrentThreadEffectiveIdentity();
        if (!assessment.isPublic() || assessment.getSampleCategory()==null) {
            throw new PrivilegeException(caller, Privilege.LAUNCH_ASSESSMENT_AS_SAMPLE, assessment);
        }
        return assessment;
    }

    //----------------------------------------------------
    // Candidate delivery access

    public Delivery lookupItemDelivery(final long did)
            throws DomainEntityNotFoundException, PrivilegeException {
        final Delivery itemDelivery = deliveryDao.requireFindById(did);
        ensureCandidateMayAccess(itemDelivery);
        return itemDelivery;
    }

    /**
     * FIXME: Currently we're only allowing access to public or owned deliveries! This will need
     * to be relaxed in order to allow "real" deliveries to be done.
     */
    private User ensureCandidateMayAccess(final Delivery itemDelivery)
            throws PrivilegeException {
        final User caller = identityContext.getCurrentThreadEffectiveIdentity();
        if (!itemDelivery.isOpen()) {
            throw new PrivilegeException(caller, Privilege.LAUNCH_CLOSED_DELIVERY, itemDelivery);
        }
        final Assessment assessment = itemDelivery.getAssessment();
        if (!(assessment.isPublic()
                || (itemDelivery.isLtiEnabled() && caller.getUserType()==UserType.LTI)
                || caller.equals(assessment.getOwner()))) {
            throw new PrivilegeException(caller, Privilege.LAUNCH_DELIVERY, itemDelivery);
        }
        return caller;
    }

    //----------------------------------------------------
    // Session creation and initialisation

    public CandidateSession createSystemSampleSession(final long aid, final String exitUrl)
            throws PrivilegeException, DomainEntityNotFoundException {
        final Delivery sampleItemDelivery = lookupSystemSampleDelivery(aid);
        return createCandidateSession(sampleItemDelivery, exitUrl);
    }

    /**
     * Starts a new {@link CandidateSession} for the {@link Delivery}
     * having the given ID (did).
     */
    public CandidateSession createCandidateSession(final long did, final String exitUrl)
            throws PrivilegeException, DomainEntityNotFoundException {
        final Delivery itemDelivery = lookupItemDelivery(did);
        return createCandidateSession(itemDelivery, exitUrl);
    }

    /**
     * Starts new {@link CandidateSession} for the given {@link Delivery}
     *
     * @param delivery
     *
     * @return
     * @throws PrivilegeException
     */
    public CandidateSession createCandidateSession(final Delivery delivery, final String exitUrl)
            throws PrivilegeException {
        Assert.notNull(delivery, "delivery");
        final User candidate = identityContext.getCurrentThreadEffectiveIdentity();

        /* Make sure delivery is open */
        /* FIXME: This prevents instructors from trying out their own sessions! */
        if (!delivery.isOpen()) {
            throw new PrivilegeException(candidate, Privilege.LAUNCH_CLOSED_DELIVERY, delivery);
        }

        /* Make sure underlying Assessment is valid */
        final Assessment assessment = delivery.getAssessment();
        final AssessmentPackage assessmentPackage = entityGraphService.getCurrentAssessmentPackage(assessment);
        if (!assessmentPackage.isValid()) {
            throw new PrivilegeException(candidate, Privilege.LAUNCH_INVALID_ASSESSMENT, delivery);
        }

        /* Now branch depending on whether this is an item or test */
        switch (assessment.getAssessmentType()) {
            case ASSESSMENT_ITEM:
                return createCandidateItemSession(delivery, exitUrl);

            case ASSESSMENT_TEST:
                throw new QtiWorksRuntimeException("Launching a CandidateSession on a test has not been implemented yet");

            default:
                throw new QtiWorksLogicException("Unexpected switch case " + assessment.getAssessmentType());
        }

    }

    private CandidateSession createCandidateItemSession(final Delivery delivery, final String exitUrl) {
        final User candidate = identityContext.getCurrentThreadEffectiveIdentity();
        final ItemDeliverySettings itemDeliverySettings = (ItemDeliverySettings) delivery.getDeliverySettings();

        /* Create fresh JQTI+ state Object */
        final ItemSessionState itemSessionState = new ItemSessionState();

        /* Set up listener to record any notifications */
        final NotificationRecorder notificationRecorder = new NotificationRecorder(NotificationLevel.INFO);

        /* Initialise state */
        final ItemSessionController itemSessionController = candidateDataServices.createItemSessionController(delivery, itemSessionState, notificationRecorder);
        itemSessionController.performTemplateProcessing();

        /* Check whether an attempt is allowed. This is a bit pathological here,
         * but it makes sense to be consistent.
         */
        final boolean attemptAllowed = itemSessionController.isAttemptAllowed(itemDeliverySettings.getMaxAttempts());

        /* Create new session and put into appropriate initial state */
        final CandidateSession candidateSession = new CandidateSession();
        candidateSession.setSessionToken(ServiceUtilities.createRandomAlphanumericToken(DomainConstants.CANDIDATE_SESSION_TOKEN_LENGTH));
        candidateSession.setExitUrl(exitUrl);
        candidateSession.setCandidate(candidate);
        candidateSession.setDelivery(delivery);
        candidateSession.setCandidateSessionStatus(attemptAllowed ? CandidateSessionStatus.INTERACTING : CandidateSessionStatus.CLOSED);
        candidateSessionDao.persist(candidateSession);

        /* Record and log event */
        final CandidateItemEvent candidateItemEvent = candidateDataServices.recordCandidateItemEvent(candidateSession, CandidateItemEventType.INIT, itemSessionState, notificationRecorder);
        candidateAuditLogger.logCandidateItemEvent(candidateSession, candidateItemEvent);

        auditor.recordEvent("Created and initialised new CandidateItemSession #" + candidateSession.getId()
                + " on ItemDelivery #" + delivery.getId());
        return candidateSession;
    }
}