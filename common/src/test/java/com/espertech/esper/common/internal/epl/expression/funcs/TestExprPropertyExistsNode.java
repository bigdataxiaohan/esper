/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.expression.funcs;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.epl.expression.core.ExprNodeUtilityPrint;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;
import com.espertech.esper.common.internal.epl.expression.ops.ExprEqualsNodeImpl;
import com.espertech.esper.common.internal.support.SupportExprValidationContextFactory;
import com.espertech.esper.common.internal.supportunit.event.SupportEventBeanFactory;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNode;
import com.espertech.esper.common.internal.supportunit.util.SupportExprNodeFactory;
import junit.framework.TestCase;

public class TestExprPropertyExistsNode extends TestCase {
    private ExprPropertyExistsNode[] existsNodes;

    public void setUp() throws Exception {
        existsNodes = new ExprPropertyExistsNode[2];

        existsNodes[0] = new ExprPropertyExistsNode();
        existsNodes[0].addChildNode(SupportExprNodeFactory.makeIdentNode("dummy?", "s0"));

        existsNodes[1] = new ExprPropertyExistsNode();
        existsNodes[1].addChildNode(SupportExprNodeFactory.makeIdentNode("boolPrimitive?", "s0"));
    }

    public void testGetType() throws Exception {
        for (int i = 0; i < existsNodes.length; i++) {
            existsNodes[i].validate(SupportExprValidationContextFactory.makeEmpty());
            assertEquals(EPTypePremade.BOOLEANBOXED.getEPType(), existsNodes[i].getEvaluationType());
        }
    }

    public void testValidate() throws Exception {
        ExprPropertyExistsNode castNode = new ExprPropertyExistsNode();

        // Test too few nodes under this node
        try {
            castNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }

        castNode.addChildNode(new SupportExprNode(1));
        try {
            castNode.validate(SupportExprValidationContextFactory.makeEmpty());
            fail();
        } catch (ExprValidationException ex) {
            // Expected
        }
    }

    public void testEvaluate() throws Exception {
        for (int i = 0; i < existsNodes.length; i++) {
            existsNodes[i].validate(SupportExprValidationContextFactory.makeEmpty());
        }

        assertEquals(false, existsNodes[0].evaluate(new EventBean[3], false, null));
        assertEquals(false, existsNodes[1].evaluate(new EventBean[3], false, null));

        EventBean[] events = new EventBean[]{SupportEventBeanFactory.makeEvents(new String[1])[0]};
        assertEquals(false, existsNodes[0].evaluate(events, false, null));
        assertEquals(true, existsNodes[1].evaluate(events, false, null));
    }

    public void testEquals() throws Exception {
        assertFalse(existsNodes[0].equalsNode(new ExprEqualsNodeImpl(true, false), false));
        assertTrue(existsNodes[0].equalsNode(existsNodes[1], false));
    }

    public void testToExpressionString() throws Exception {
        existsNodes[0].validate(SupportExprValidationContextFactory.makeEmpty());
        assertEquals("exists(s0.dummy?)", ExprNodeUtilityPrint.toExpressionStringMinPrecedenceSafe(existsNodes[0]));
    }
}
