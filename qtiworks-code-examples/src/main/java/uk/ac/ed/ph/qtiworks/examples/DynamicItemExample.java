/* Copyright (c) 2012-2013, University of Edinburgh.
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
 * This software is derived from (and contains code from) QTITools and MathAssessEngine.
 * QTITools is (c) 2008, University of Southampton.
 * MathAssessEngine is (c) 2010, University of Edinburgh.
 */
package uk.ac.ed.ph.qtiworks.examples;

import com.google.common.collect.Lists;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.internal.util.ObjectDumper;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.presentation.B;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Table;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tbody;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Td;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Blockquote;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Div;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapText;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Gap;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.reading.QtiObjectReader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.AssessmentObjectResolver;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.validation.AssessmentObjectValidator;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.NullResourceLocator;

import java.util.List;

/**
 * This example builds a simple JQTI+ {@link AssessmentItem} programmatically,
 * checks its validity, then prints out the resulting XML.
 *
 * <h3>How to run</h3>
 *
 * You can run this via Maven as follows:
 * <pre>
 * mvn exec:java -Dexec.mainClass=uk.ac.ed.ph.qtiworks.examples.DynamicItemExample
 * </pre>
 * You should also be able to run this inside your favourite IDE if you have loaded the QTIWorks
 * source code into it.
 *
 * @author David McKain
 */
public final class DynamicItemExample {

    public static void main(final String[] args) throws Exception {
        /* Create empty AssessmentItem and add necessary properties to make it valid */
        final AssessmentItem assessmentItem = new AssessmentItem();
        assessmentItem.setIdentifier("MyItem");
        assessmentItem.setTitle("Title");
        assessmentItem.setAdaptive(Boolean.FALSE);
        assessmentItem.setTimeDependent(Boolean.FALSE);

        final ItemBody itemBody = new ItemBody(assessmentItem);
        final List<Block> blocks = itemBody.getBlocks();
        final Div divQS = new Div(itemBody);
        divQS.setClassAttr(Lists.newArrayList("question-section"));
        final StringAttribute dataTypeAttribute = new StringAttribute(divQS, "data-type", "gap-match-table", true);
        dataTypeAttribute.setValue("gap-match-table");
        divQS.getAttributes().add(dataTypeAttribute);
        blocks.add(divQS);
        assessmentItem.setItemBody(itemBody);
        final Div divQT = new Div(divQS);
        divQT.setClassAttr(Lists.newArrayList("question-text"));
        divQS.getFlows().add(divQT);
        final P divQt_p = new P(divQT);
        final B boldText = new B(divQt_p);
        boldText.getInlines().add(new TextRun(divQt_p, "bold"));
        divQt_p.getInlines().add(boldText);
        divQt_p.getInlines().add(new TextRun(divQt_p, "question text"));
        divQT.getFlows().add(divQt_p);
        final GapMatchInteraction gapInter = new GapMatchInteraction(divQS);
        divQS.getFlows().add(gapInter);
        final GapText gapText = new GapText(gapInter);
        gapInter.getGapChoices().add(gapText);
        gapInter.getBlockStatics().add(new Blockquote(gapInter));
        final Td td = new Td(divQS);
        final Table table = new Table(gapInter);
        gapInter.getBlockStatics().add(table);
        final Tbody tbody = new Tbody(table);
        table.getTbodys().add(tbody);
        final Tr tr = new Tr(tbody);
        tbody.getTrs().add(tr);
        tr.getTableCells().add(td);
        final Gap gap = new Gap(td);
        td.getChildren().add(gap);


        /* Declare a SCORE outcome variable */
        final OutcomeDeclaration score = new OutcomeDeclaration(assessmentItem);
        score.setIdentifier(Identifier.assumedLegal("SCORE"));
        score.setCardinality(Cardinality.SINGLE);
        score.setBaseType(BaseType.FLOAT);
        final DefaultValue defaultValue = new DefaultValue(score);
        defaultValue.getFieldValues().add(new FieldValue(defaultValue, new FloatValue(0.0)));
        score.setDefaultValue(defaultValue);
        assessmentItem.getOutcomeDeclarations().add(score);

//        /* Validate */
        final JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
//        final QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager);
//        final QtiObjectReader qtiObjectReader = qtiXmlReader.createQtiObjectReader(NullResourceLocator.getInstance(), false);
//        final AssessmentObjectResolver resolver = new AssessmentObjectResolver(qtiObjectReader);
//        final ResolvedAssessmentItem resolvedAssessmentItem = resolver.resolveAssessmentItem(assessmentItem);
//        final AssessmentObjectValidator validator = new AssessmentObjectValidator(jqtiExtensionManager);
//        final ItemValidationResult validationResult = validator.validateItem(resolvedAssessmentItem);

//        /* Print out validation result */
//        System.out.println("Validation result:");
//        ObjectDumper.dumpObjectToStdout(validationResult);

        /* Finally serialize the assessmentItem to XML and print it out */
        final QtiSerializer qtiSerializer = new QtiSerializer(jqtiExtensionManager);
        System.out.println("Serialized XML:");
        System.out.println(qtiSerializer.serializeJqtiObject(assessmentItem));
    }

}
