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
package com.espertech.esper.regressionlib.suite.context;

import com.espertech.esper.common.client.util.DateTime;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.bean.SupportProductIdEvent;

import java.util.ArrayList;
import java.util.Collection;

public class ContextInitTermPrioritized {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ContextInitTermPrioNonOverlappingSubqueryAndInvalid());
        execs.add(new ContextInitTermPrioAtNowWithSelectedEventEnding());
        return execs;
    }

    public static class ContextInitTermPrioNonOverlappingSubqueryAndInvalid implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimeEvent(env, "2002-05-1T10:00:00.000");

            RegressionPath path = new RegressionPath();
            String epl =
                "\n @Name('ctx') @public create context RuleActivityTime as start (0, 9, *, *, *) end (0, 17, *, *, *);" +
                    "\n @Name('window') @public context RuleActivityTime create window EventsWindow#firstunique(productID) as SupportProductIdEvent;" +
                    "\n @Name('variable') create variable boolean IsOutputTriggered_2 = false;" +
                    "\n @Name('A') context RuleActivityTime insert into EventsWindow select * from SupportProductIdEvent(not exists (select * from EventsWindow));" +
                    "\n @Name('B') context RuleActivityTime insert into EventsWindow select * from SupportProductIdEvent(not exists (select * from EventsWindow));" +
                    "\n @Name('C') context RuleActivityTime insert into EventsWindow select * from SupportProductIdEvent(not exists (select * from EventsWindow));" +
                    "\n @Name('D') context RuleActivityTime insert into EventsWindow select * from SupportProductIdEvent(not exists (select * from EventsWindow));" +
                    "\n @Name('out') context RuleActivityTime select * from EventsWindow";
            env.compileDeploy(epl, path).addListener("out");

            env.sendEventBean(new SupportProductIdEvent("A1"));

            // invalid - subquery not the same context
            env.tryInvalidCompile(path, "insert into EventsWindow select * from SupportProductIdEvent(not exists (select * from EventsWindow))",
                "Failed to validate subquery number 1 querying EventsWindow: Named window by name 'EventsWindow' has been declared for context 'RuleActivityTime' and can only be used within the same context");

            env.undeployAll();
        }
    }

    public static class ContextInitTermPrioAtNowWithSelectedEventEnding implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "theString".split(",");
            String epl = "@Priority(1) create context C1 start @now end SupportBean;\n" +
                "@name('s0') @Priority(0) context C1 select * from SupportBean;\n";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.assertPropsNew("s0", fields, new Object[]{"E1"});

            env.sendEventBean(new SupportBean("E2", 1));
            env.assertPropsNew("s0", fields, new Object[]{"E2"});

            env.undeployAll();
        }
    }

    private static void sendTimeEvent(RegressionEnvironment env, String time) {
        env.advanceTime(DateTime.parseDefaultMSec(time));
    }
}
