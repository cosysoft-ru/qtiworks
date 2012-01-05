/* $Id:SAXErrorHandler.java 2824 2008-08-01 15:46:17Z davemckain $
 *
 * Copyright (c) 2011, The University of Edinburgh.
 * All Rights Reserved
 */
package dave;

import uk.ac.ed.ph.jqtiplus.control.AssessmentItemController;
import uk.ac.ed.ph.jqtiplus.control2.JQTIExtensionManager;
import uk.ac.ed.ph.jqtiplus.exception2.RuntimeValidationException;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.AssessmentItemState;
import uk.ac.ed.ph.jqtiplus.validation.ValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.ClassPathResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.legacy.AssessmentItemManager;
import uk.ac.ed.ph.jqtiplus.xmlutils.legacy.QTIObjectManager;
import uk.ac.ed.ph.jqtiplus.xmlutils.legacy.QTIReadResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.legacy.SimpleQTIObjectCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.legacy.SupportedXMLReader;
import uk.ac.ed.ph.jqtiplus.xmlutils.legacy.XMLParseResult;

import java.net.URI;

public class TemplateConstraintTest {
    
    public static void main(String[] args) throws RuntimeValidationException {
        JQTIExtensionManager jqtiExtensionManager = new JQTIExtensionManager();
        SupportedXMLReader xmlReader = new SupportedXMLReader(true);
        QTIObjectManager objectManager = new QTIObjectManager(jqtiExtensionManager, xmlReader, new ClassPathResourceLocator(), new SimpleQTIObjectCache());

        URI inputUri = URI.create("classpath:/templateConstraint-1.xml");
        
        System.out.println("Reading and validating");
        QTIReadResult<AssessmentItem> qtiReadResult = objectManager.getQTIObject(inputUri, AssessmentItem.class);
        XMLParseResult xmlParseResult = qtiReadResult.getXMLParseResult();
        if (!xmlParseResult.isSchemaValid()) {
            System.out.println("Schema validation failed: " + xmlParseResult);
            return;
        }
        
        AssessmentItem item = qtiReadResult.getJQTIObject();
        
        AssessmentItemManager itemManager = new AssessmentItemManager(objectManager, item);
        ValidationResult validationResult = itemManager.validateItem();
        if (!validationResult.getAllItems().isEmpty()) {
            System.out.println("JQTI validation failed: " + validationResult);
            return;          
        }
        
        AssessmentItemState itemState = new AssessmentItemState();
        AssessmentItemController itemController = new AssessmentItemController(itemManager, itemState);
        
        System.out.println("\nInitialising");
        itemController.initialize(null);
        System.out.println("Template Values: " + itemState.getTemplateValues());
    }

}