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
package com.espertech.esper.regressionlib.suite.resultset.aggregate;

import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import java.util.concurrent.atomic.AtomicInteger;



public class ResultSetAggregateLeaving implements RegressionExecution {

    public void run(RegressionEnvironment env) {
        AtomicInteger milestone = new AtomicInteger();

        String epl = "@name('s0') select leaving() as val from SupportBean#length(3)";
        env.compileDeploy(epl).addListener("s0");
        runAssertion(env, milestone);

        env.undeployAll();

        env.eplToModelCompileDeploy(epl).addListener("s0");

        runAssertion(env, milestone);

        env.undeployAll();

        env.tryInvalidCompile("select leaving(1) from SupportBean",
            "Failed to validate select-clause expression 'leaving(1)': The 'leaving' function expects no parameters");
    }

    private static void runAssertion(RegressionEnvironment env, AtomicInteger milestone) {
        String[] fields = "val".split(",");

        env.sendEventBean(new SupportBean("E1", 1));
        env.assertPropsNew("s0", fields, new Object[]{false});

        env.milestoneInc(milestone);

        env.sendEventBean(new SupportBean("E2", 2));
        env.assertPropsNew("s0", fields, new Object[]{false});

        env.sendEventBean(new SupportBean("E3", 3));
        env.assertPropsNew("s0", fields, new Object[]{false});

        env.milestoneInc(milestone);

        env.sendEventBean(new SupportBean("E4", 4));
        env.assertPropsNew("s0", fields, new Object[]{true});

        env.milestoneInc(milestone);

        env.sendEventBean(new SupportBean("E5", 5));
        env.assertPropsNew("s0", fields, new Object[]{true});
    }
}
