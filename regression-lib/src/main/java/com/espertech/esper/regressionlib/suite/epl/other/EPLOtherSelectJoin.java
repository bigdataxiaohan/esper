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
package com.espertech.esper.regressionlib.suite.epl.other;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBean_A;
import com.espertech.esper.regressionlib.support.bean.SupportBean_B;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EPLOtherSelectJoin {
    public static List<RegressionExecution> executions() {
        List<RegressionExecution> execs = new ArrayList<>();
        execs.add(new EPLOtherJoinUniquePerId());
        execs.add(new EPLOtherJoinNonUniquePerId());
        return execs;
    }

    private static class EPLOtherJoinUniquePerId implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            SelectJoinHolder holder = setupStmt(env);

            sendEvent(env, holder.eventsA[0]);
            sendEvent(env, holder.eventsB[1]);
            env.assertListenerNotInvoked("s0");

            // Test join new B with id 0
            sendEvent(env, holder.eventsB[0]);
            env.assertListener("s0", listener -> {
                assertSame(holder.eventsA[0], listener.getLastNewData()[0].get("streamA"));
                assertSame(holder.eventsB[0], listener.getLastNewData()[0].get("streamB"));
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            // Test join new A with id 1
            sendEvent(env, holder.eventsA[1]);
            env.assertListener("s0", listener -> {
                assertSame(holder.eventsA[1], listener.getLastNewData()[0].get("streamA"));
                assertSame(holder.eventsB[1], listener.getLastNewData()[0].get("streamB"));
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            sendEvent(env, holder.eventsA[2]);
            env.assertListener("s0", listener -> assertNull(listener.getLastOldData()));

            // Test join old A id 0 leaves length window of 3 events
            sendEvent(env, holder.eventsA[3]);
            env.assertListener("s0", listener -> {
                assertSame(holder.eventsA[0], listener.getLastOldData()[0].get("streamA"));
                assertSame(holder.eventsB[0], listener.getLastOldData()[0].get("streamB"));
                assertNull(listener.getLastNewData());
                listener.reset();
            });

            // Test join old B id 1 leaves window
            sendEvent(env, holder.eventsB[4]);
            env.assertListener("s0", listener -> assertNull(listener.getLastOldData()));

            sendEvent(env, holder.eventsB[5]);
            env.assertListener("s0", listener -> {
                assertSame(holder.eventsA[1], listener.getLastOldData()[0].get("streamA"));
                assertSame(holder.eventsB[1], listener.getLastOldData()[0].get("streamB"));
                assertNull(listener.getLastNewData());
            });

            env.undeployAll();
        }
    }

    private static class EPLOtherJoinNonUniquePerId implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            SelectJoinHolder holder = setupStmt(env);

            sendEvent(env, holder.eventsA[0]);
            sendEvent(env, holder.eventsA[1]);
            sendEvent(env, holder.eventsASetTwo[0]);
            env.assertListener("s0", listener -> assertTrue(listener.getLastOldData() == null && listener.getLastNewData() == null));

            sendEvent(env, holder.eventsB[0]); // Event B id 0 joins to A id 0 twice
            env.assertListener("s0", listener -> {
                EventBean[] data = listener.getLastNewData();
                assertTrue(holder.eventsASetTwo[0] == data[0].get("streamA") || holder.eventsASetTwo[0] == data[1].get("streamA"));    // Order arbitrary
                assertSame(holder.eventsB[0], data[0].get("streamB"));
                assertTrue(holder.eventsA[0] == data[0].get("streamA") || holder.eventsA[0] == data[1].get("streamA"));
                assertSame(holder.eventsB[0], data[1].get("streamB"));
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            sendEvent(env, holder.eventsB[2]);
            sendEvent(env, holder.eventsBSetTwo[0]);  // Ignore events generated
            env.listenerReset("s0");

            sendEvent(env, holder.eventsA[3]);  // Pushes A id 0 out of window, which joins to B id 0 twice
            env.assertListener("s0", listener -> {
                EventBean[] data = listener.getLastOldData();
                assertSame(holder.eventsA[0], listener.getLastOldData()[0].get("streamA"));
                assertTrue(holder.eventsB[0] == data[0].get("streamB") || holder.eventsB[0] == data[1].get("streamB"));    // B order arbitrary
                assertSame(holder.eventsA[0], listener.getLastOldData()[1].get("streamA"));
                assertTrue(holder.eventsBSetTwo[0] == data[0].get("streamB") || holder.eventsBSetTwo[0] == data[1].get("streamB"));
                assertNull(listener.getLastNewData());
                listener.reset();
            });

            sendEvent(env, holder.eventsBSetTwo[2]);  // Pushes B id 0 out of window, which joins to A set two id 0
            env.assertListener("s0", listener -> {
                assertSame(holder.eventsASetTwo[0], listener.getLastOldData()[0].get("streamA"));
                assertSame(holder.eventsB[0], listener.getLastOldData()[0].get("streamB"));
                Assert.assertEquals(1, listener.getLastOldData().length);
            });

            env.undeployAll();
        }
    }

    private static SelectJoinHolder setupStmt(RegressionEnvironment env) {
        SelectJoinHolder holder = new SelectJoinHolder();

        String epl = "@name('s0') select irstream * from SupportBean_A#length(3) as streamA, SupportBean_B#length(3) as streamB where streamA.id = streamB.id";
        env.compileDeploy(epl).addListener("s0");

        env.assertStatement("s0", statement -> {
            Assert.assertEquals(SupportBean_A.class, statement.getEventType().getPropertyType("streamA"));
            Assert.assertEquals(SupportBean_B.class, statement.getEventType().getPropertyType("streamB"));
            Assert.assertEquals(2, statement.getEventType().getPropertyNames().length);
        });

        holder.eventsA = new SupportBean_A[10];
        holder.eventsASetTwo = new SupportBean_A[10];
        holder.eventsB = new SupportBean_B[10];
        holder.eventsBSetTwo = new SupportBean_B[10];
        for (int i = 0; i < holder.eventsA.length; i++) {
            holder.eventsA[i] = new SupportBean_A(Integer.toString(i));
            holder.eventsASetTwo[i] = new SupportBean_A(Integer.toString(i));
            holder.eventsB[i] = new SupportBean_B(Integer.toString(i));
            holder.eventsBSetTwo[i] = new SupportBean_B(Integer.toString(i));
        }
        return holder;
    }

    private static void sendEvent(RegressionEnvironment env, Object theEvent) {
        env.sendEventBean(theEvent);
    }

    private static class SelectJoinHolder {
        SupportBean_A[] eventsA;
        SupportBean_A[] eventsASetTwo;
        SupportBean_B[] eventsB;
        SupportBean_B[] eventsBSetTwo;
    }
}
