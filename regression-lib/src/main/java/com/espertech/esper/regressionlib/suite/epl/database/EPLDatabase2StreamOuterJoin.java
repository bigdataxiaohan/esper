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
package com.espertech.esper.regressionlib.suite.epl.database;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EPLDatabase2StreamOuterJoin {
    private final static String ALL_FIELDS = "mybigint, myint, myvarchar, mychar, mybool, mynumeric, mydecimal, mydouble, myreal";

    public static List<RegressionExecution> executions() {
        List<RegressionExecution> execs = new ArrayList<>();
        execs.add(new EPLDatabaseOuterJoinLeftS0());
        execs.add(new EPLDatabaseOuterJoinRightS1());
        execs.add(new EPLDatabaseOuterJoinFullS0());
        execs.add(new EPLDatabaseOuterJoinFullS1());
        execs.add(new EPLDatabaseOuterJoinRightS0());
        execs.add(new EPLDatabaseOuterJoinLeftS1());
        execs.add(new EPLDatabaseLeftOuterJoinOnFilter());
        execs.add(new EPLDatabaseRightOuterJoinOnFilter());
        execs.add(new EPLDatabaseOuterJoinReversedOnFilter());
        return execs;
    }

    private static class EPLDatabaseOuterJoinLeftS0 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                "SupportBean as s0 left outer join " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 on intPrimitive = mybigint";
            tryOuterJoinResult(env, stmtText);
        }
    }

    private static class EPLDatabaseOuterJoinRightS1 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 right outer join " +
                "SupportBean as s0 on intPrimitive = mybigint";
            tryOuterJoinResult(env, stmtText);
        }
    }

    private static class EPLDatabaseOuterJoinFullS0 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 full outer join " +
                "SupportBean as s0 on intPrimitive = mybigint";
            tryOuterJoinResult(env, stmtText);
        }
    }

    private static class EPLDatabaseOuterJoinFullS1 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                "SupportBean as s0 full outer join " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 on intPrimitive = mybigint";
            tryOuterJoinResult(env, stmtText);
        }
    }

    private static class EPLDatabaseOuterJoinRightS0 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                "SupportBean as s0 right outer join " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 on intPrimitive = mybigint";
            tryOuterJoinNoResult(env, stmtText);
        }
    }

    private static class EPLDatabaseOuterJoinLeftS1 implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 left outer join " +
                "SupportBean as s0 on intPrimitive = mybigint";
            tryOuterJoinNoResult(env, stmtText);
        }
    }

    private static class EPLDatabaseLeftOuterJoinOnFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "MyInt,myint".split(",");
            String stmtText = "@name('s0') @IterableUnbound select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                "SupportBean as s0 " +
                " left outer join " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 " +
                "on theString = myvarchar";
            env.compileDeploy(stmtText).addListener("s0");

            env.assertPropsPerRowIteratorAnyOrder("s0", fields, null);

            // Result as the SQL query returns 1 row and therefore the on-clause filters it out, but because of left out still getting a row
            sendEvent(env, 1, "xxx");
            env.assertEventNew("s0", received -> {
                assertEquals(1, received.get("MyInt"));
                assertReceived(received, null, null, null, null, null, null, null, null, null);
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{1, null}});

            // Result as the SQL query returns 0 rows
            sendEvent(env, -1, "xxx");
            env.assertEventNew("s0", received -> {
                assertEquals(-1, received.get("MyInt"));
                assertReceived(received, null, null, null, null, null, null, null, null, null);
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{-1, null}});

            sendEvent(env, 2, "B");
            env.assertEventNew("s0", received -> {
                assertEquals(2, received.get("MyInt"));
                assertReceived(received, 2L, 20, "B", "Y", false, new BigDecimal(100), new BigDecimal(200), 2.2d, 2.3d);
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{2, 20}});

            env.undeployAll();
        }
    }

    private static class EPLDatabaseRightOuterJoinOnFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "MyInt,myint".split(",");
            String stmtText = "@name('s0') @IterableUnbound select s0.intPrimitive as MyInt, " + ALL_FIELDS + " from " +
                " sql:MyDBWithRetain ['select " + ALL_FIELDS + " from mytesttable where ${s0.intPrimitive} = mytesttable.mybigint'] as s1 right outer join " +
                "SupportBean as s0 on theString = myvarchar";
            env.compileDeploy(stmtText).addListener("s0");

            env.assertPropsPerRowIteratorAnyOrder("s0", fields, null);

            // No result as the SQL query returns 1 row and therefore the on-clause filters it out
            sendEvent(env, 1, "xxx");
            env.assertEventNew("s0", received -> {
                assertEquals(1, received.get("MyInt"));
                assertReceived(received, null, null, null, null, null, null, null, null, null);
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{1, null}});

            // Result as the SQL query returns 0 rows
            sendEvent(env, -1, "xxx");
            env.assertEventNew("s0", received -> {
                assertEquals(-1, received.get("MyInt"));
                assertReceived(received, null, null, null, null, null, null, null, null, null);
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{-1, null}});

            sendEvent(env, 2, "B");
            env.assertEventNew("s0", received -> {
                assertEquals(2, received.get("MyInt"));
                assertReceived(received, 2L, 20, "B", "Y", false, new BigDecimal(100), new BigDecimal(200), 2.2d, 2.3d);
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{2, 20}});

            env.undeployAll();
        }
    }

    private static class EPLDatabaseOuterJoinReversedOnFilter implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "MyInt,MyVarChar".split(",");
            String stmtText = "@name('s0') select s0.intPrimitive as MyInt, MyVarChar from " +
                "SupportBean#keepall as s0 " +
                " right outer join " +
                " sql:MyDBWithRetain ['select myvarchar MyVarChar from mytesttable'] as s1 " +
                "on theString = MyVarChar";
            env.compileDeploy(stmtText).addListener("s0");

            env.assertPropsPerRowIteratorAnyOrder("s0", fields, null);

            // No result as the SQL query returns 1 row and therefore the on-clause filters it out
            sendEvent(env, 1, "xxx");
            env.assertListenerNotInvoked("s0");
            env.assertPropsPerRowIteratorAnyOrder("s0", fields, null);

            sendEvent(env, -1, "A");
            env.assertEventNew("s0", received -> {
                assertEquals(-1, received.get("MyInt"));
                assertEquals("A", received.get("MyVarChar"));
            });
            env.assertPropsPerRowIterator("s0", fields, new Object[][]{{-1, "A"}});

            env.undeployAll();
        }
    }

    private static void tryOuterJoinNoResult(RegressionEnvironment env, String statementText) {
        env.compileDeploy(statementText).addListener("s0");

        sendEvent(env, 2);
        env.assertEventNew("s0", received -> {
            assertEquals(2, received.get("MyInt"));
            assertReceived(received, 2L, 20, "B", "Y", false, new BigDecimal(100), new BigDecimal(200), 2.2d, 2.3d);
        });

        sendEvent(env, 11);
        env.assertListenerNotInvoked("s0");

        env.undeployAll();
    }

    private static void tryOuterJoinResult(RegressionEnvironment env, String statementText) {
        env.compileDeploy(statementText).addListener("s0");

        sendEvent(env, 1);
        env.assertEventNew("s0", received -> {
            assertEquals(1, received.get("MyInt"));
            assertReceived(received, 1L, 10, "A", "Z", true, new BigDecimal(5000), new BigDecimal(100), 1.2d, 1.3d);
        });

        sendEvent(env, 11);
        env.assertEventNew("s0", received -> {
            assertEquals(11, received.get("MyInt"));
            assertReceived(received, null, null, null, null, null, null, null, null, null);
        });

        env.undeployAll();
    }

    private static void assertReceived(EventBean theEvent, Long mybigint, Integer myint, String myvarchar, String mychar, Boolean mybool, BigDecimal mynumeric, BigDecimal mydecimal, Double mydouble, Double myreal) {
        assertEquals(mybigint, theEvent.get("mybigint"));
        assertEquals(myint, theEvent.get("myint"));
        assertEquals(myvarchar, theEvent.get("myvarchar"));
        assertEquals(mychar, theEvent.get("mychar"));
        assertEquals(mybool, theEvent.get("mybool"));
        assertEquals(mynumeric, theEvent.get("mynumeric"));
        assertEquals(mydecimal, theEvent.get("mydecimal"));
        assertEquals(mydouble, theEvent.get("mydouble"));
        assertEquals(myreal, theEvent.get("myreal"));
    }

    private static void sendEvent(RegressionEnvironment env, int intPrimitive) {
        SupportBean bean = new SupportBean();
        bean.setIntPrimitive(intPrimitive);
        env.sendEventBean(bean);
    }

    private static void sendEvent(RegressionEnvironment env, int intPrimitive, String theString) {
        SupportBean bean = new SupportBean();
        bean.setIntPrimitive(intPrimitive);
        bean.setTheString(theString);
        env.sendEventBean(bean);
    }
}
