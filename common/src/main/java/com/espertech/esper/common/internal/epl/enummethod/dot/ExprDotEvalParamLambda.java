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
package com.espertech.esper.common.internal.epl.enummethod.dot;

import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeLambdaDesc;
import com.espertech.esper.common.internal.epl.expression.core.ExprForge;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;

import java.util.List;

public class ExprDotEvalParamLambda extends ExprDotEvalParam {

    private final int streamCountIncoming;    // count of incoming streams
    private final List<String> goesToNames;    // (x, y) => doSomething   .... parameter names are x and y
    private final EnumForgeLambdaDesc lambdaDesc;

    public ExprDotEvalParamLambda(int parameterNum, ExprNode body, ExprForge bodyEvaluator, int streamCountIncoming, List<String> goesToNames, EnumForgeLambdaDesc lambdaDesc) {
        super(parameterNum, body, bodyEvaluator);
        this.streamCountIncoming = streamCountIncoming;
        this.goesToNames = goesToNames;
        this.lambdaDesc = lambdaDesc;
    }

    public int getStreamCountIncoming() {
        return streamCountIncoming;
    }

    public List<String> getGoesToNames() {
        return goesToNames;
    }

    public EnumForgeLambdaDesc getLambdaDesc() {
        return lambdaDesc;
    }
}
