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
package uk.ac.ed.ph.jqtiplus.reading;

import uk.ac.ed.ph.jqtiplus.internal.util.DumpMode;
import uk.ac.ed.ph.jqtiplus.internal.util.ObjectDumperOptions;
import uk.ac.ed.ph.jqtiplus.node.RootObject;
import uk.ac.ed.ph.jqtiplus.provision.RootObjectHolder;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlParseResult;

/**
 * Encapsulates the result of instantiating a QTI {@link RootObject} from XML, as returned
 * by {@link QtiXmlObjectReader}.
 *
 * @see QtiXmlObjectReader
 *
 * @author David McKain
 */
public final class QtiXmlObjectReadResult<E extends RootObject> implements RootObjectHolder<E> {

    private static final long serialVersionUID = -6470500039269477402L;

    private final Class<E> requestedRootObjectClass;
    private final E rootObject;
    private final XmlParseResult xmlParseResult;

    /** This will be either the QTI 2.1 or QTI 2.0 namespace */
    private final String qtiNamespaceUri;

    QtiXmlObjectReadResult(final Class<E> requestedRootObjectClass, final XmlParseResult xmlParseResult,
            final String qtiNamespace, final E rootObject) {
        this.requestedRootObjectClass = requestedRootObjectClass;
        this.rootObject = rootObject;
        this.xmlParseResult = xmlParseResult;
        this.qtiNamespaceUri = qtiNamespace;
    }

    @Override
    public Class<E> getRequestedRootObjectClass() {
        return requestedRootObjectClass;
    }

    @Override
    public E getRootObject() {
        return rootObject;
    }

    @ObjectDumperOptions(DumpMode.DEEP)
    public XmlParseResult getXmlParseResult() {
        return xmlParseResult;
    }

    public String getQtiNamespaceUri() {
        return qtiNamespaceUri;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(this))
                + "(rootObjectClass=" + requestedRootObjectClass
                + ",xmlParseResult=" + xmlParseResult
                + ",qtiNamespaceUri=" + qtiNamespaceUri
                + ",rootObject=" + rootObject
                + ")";
    }
}
