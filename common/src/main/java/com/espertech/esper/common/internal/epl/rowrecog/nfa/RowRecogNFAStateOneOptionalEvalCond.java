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
package com.espertech.esper.common.internal.epl.rowrecog.nfa;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.context.util.AgentInstanceContext;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;

/**
 * The '?' state in the regex NFA states.
 */
public class RowRecogNFAStateOneOptionalEvalCond extends RowRecogNFAStateBase implements RowRecogNFAState {
    public final static EPTypeClass EPTYPE = new EPTypeClass(RowRecogNFAStateOneOptionalEvalCond.class);

    private ExprEvaluator expression;

    public boolean matches(EventBean[] eventsPerStream, AgentInstanceContext agentInstanceContext) {
        Boolean result = (Boolean) expression.evaluate(eventsPerStream, true, agentInstanceContext);
        if (result != null) {
            return result;
        }
        return false;
    }

    public String toString() {
        return "OptionalFilterEvent-Filtered";
    }

    public void setExpression(ExprEvaluator expression) {
        this.expression = expression;
    }
}
