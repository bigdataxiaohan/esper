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
package com.espertech.esper.regressionlib.suite.expr.exprcore;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ExprCoreBigNumberSupportMathContext {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> executions = new ArrayList<>();
        executions.add(new ExprCoreMathContextBigDecConvDivide());
        executions.add(new ExprCoreMathContextDivide());
        return executions;
    }

    private static class ExprCoreMathContextBigDecConvDivide implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select 10/BigDecimal.valueOf(5,0) as c0 from SupportBean";
            env.compileDeploy(epl).addListener("s0");

            String[] fields = "c0".split(",");
            env.assertStatement("s0", statement -> assertEquals(BigDecimal.class, statement.getEventType().getPropertyType("c0")));

            env.sendEventBean(new SupportBean());
            env.assertPropsNew("s0", fields, new Object[]{BigDecimal.valueOf(2, 0)});

            env.undeployAll();
        }
    }

    private static class ExprCoreMathContextDivide implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            // cast and divide
            env.compileDeploy("@name('s0')  Select cast(1.6, BigDecimal) / cast(9.2, BigDecimal) from SupportBean").addListener("s0");
            env.assertStatement("s0", statement -> {
                statement.setSubscriber(new Object() {
                    public void update(BigDecimal value) {
                        assertEquals(0.1739130d, value.doubleValue(), 0);
                    }
                });
            });
            env.sendEventBean(new SupportBean());

            env.undeployAll();
        }
    }
}
