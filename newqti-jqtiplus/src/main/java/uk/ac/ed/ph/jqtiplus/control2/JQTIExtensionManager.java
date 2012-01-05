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
package uk.ac.ed.ph.jqtiplus.control2;

import uk.ac.ed.ph.jqtiplus.control.JQTIExtensionPackage;
import uk.ac.ed.ph.jqtiplus.node.XmlNode;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.CustomOperator;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.UnsupportedCustomOperator;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.CustomInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UnsupportedCustomInteraction;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Once created, all properties of this manager are unmodifiable and safe to use by multiple threads.
 * 
 * @author David McKain
 */
public final class JQTIExtensionManager {

    private static final Logger logger = LoggerFactory.getLogger(JQTIExtensionManager.class);

    private final List<JQTIExtensionPackage> extensionPackages;

    private final Map<String, String> extensionSchemaMap;

    public JQTIExtensionManager(JQTIExtensionPackage... jqtiExtensionPackages) {
        this(Arrays.asList(jqtiExtensionPackages));
    }

    public JQTIExtensionManager(List<JQTIExtensionPackage> jqtiExtensionPackages) {
        this.extensionPackages = Collections.unmodifiableList(jqtiExtensionPackages);
        this.extensionSchemaMap = Collections.unmodifiableMap(buildExtensionSchemaMap());
    }

    public List<JQTIExtensionPackage> getExtensionPackages() {
        return extensionPackages;
    }

    public Map<String, String> getExtensionSchemaMap() {
        return extensionSchemaMap;
    }

    private Map<String, String> buildExtensionSchemaMap() {
        final Map<String, String> result = new HashMap<String, String>();
        for (final JQTIExtensionPackage extensionPackage : extensionPackages) {
            result.putAll(extensionPackage.getSchemaInformation());
        }
        return result;
    }

    //---------------------------------------------------------------------

    public CustomInteraction createCustomInteraction(XmlNode parentObject, String interactionClass) {
        CustomInteraction result = null;
        for (final JQTIExtensionPackage extensionPackage : extensionPackages) {
            result = extensionPackage.createCustomInteraction(parentObject, interactionClass);
            if (result != null) {
                logger.debug("Created customInteraction of class {} using package {}", interactionClass, extensionPackage);
                return result;
            }
        }
        logger.warn("customInteraction of class {} not supported by any registered package. Using placeholder", interactionClass);
        return new UnsupportedCustomInteraction(parentObject);
    }

    public CustomOperator createCustomOperator(ExpressionParent expressionParent, String operatorClass) {
        CustomOperator result;
        for (final JQTIExtensionPackage extensionPackage : extensionPackages) {
            result = extensionPackage.createCustomOperator(expressionParent, operatorClass);
            if (result != null) {
                logger.debug("Created customOperator of class {} using package {}", operatorClass, extensionPackage);
                return result;
            }
        }
        logger.warn("customOperator of class {} not supported by any registered package. Using placeholder", operatorClass);
        return new UnsupportedCustomOperator(expressionParent);
    }

    //---------------------------------------------------------------------

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode()
                + "(extensionPackages=" + extensionPackages
                + ")";
    }
}