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

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventPropertyDescriptor;
import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExprCorePrevious {
    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ExprCorePreviousLengthWindowWhere());
        execs.add(new ExprCorePreviousPrevStream());
        execs.add(new ExprCorePreviousLengthWindow());
        execs.add(new ExprCorePreviousTimeBatch());
        execs.add(new ExprCorePreviousPrevCountStar());
        execs.add(new ExprCorePreviousPerGroupTwoCriteria());
        execs.add(new ExprCorePreviousExprNameAndTypeAndSODA());
        execs.add(new ExprCorePreviousSortWindowPerGroup());
        execs.add(new ExprCorePreviousTimeBatchPerGroup());
        execs.add(new ExprCorePreviousLengthBatchPerGroup());
        execs.add(new ExprCorePreviousTimeWindowPerGroup());
        execs.add(new ExprCorePreviousExtTimeWindowPerGroup());
        execs.add(new ExprCorePreviousLengthWindowPerGroup());
        execs.add(new ExprCorePreviousTimeWindow());
        execs.add(new ExprCorePreviousExtTimedWindow());
        execs.add(new ExprCorePreviousTimeBatchWindow());
        execs.add(new ExprCorePreviousLengthBatch());
        execs.add(new ExprCorePreviousLengthWindowDynamic());
        execs.add(new ExprCorePreviousSortWindow());
        execs.add(new ExprCorePreviousExtTimedBatch());
        execs.add(new ExprCorePreviousPrevCountStarWithStaticMethod());
        execs.add(new ExprCorePreviousTimeBatchWindowJoin());
        execs.add(new ExprCorePreviousInvalid());
        return execs;
    }

    private static class ExprCorePreviousTimeBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.advanceTime(0);

            String text = "@name('s0') select irstream symbol, " +
                "prev(1, symbol) as prev1, " +
                "prevtail(symbol) as prevtail, " +
                "prevcount(symbol) as prevCountSym, " +
                "prevwindow(symbol) as prevWindowSym " +
                "from SupportMarketDataBean#time_batch(1 sec)";
            env.compileDeploy(text).addListener("s0");
            String[] fields = new String[]{"symbol", "prev1", "prevtail", "prevCountSym", "prevWindowSym"};

            env.advanceTime(1500);
            env.sendEventBean(makeMarketDataEvent("E1"));
            env.advanceTime(1700);
            env.sendEventBean(makeMarketDataEvent("E2"));
            env.advanceTime(2499);

            env.milestone(1);

            env.advanceTime(2500);
            env.assertPropsPerRowNewOnly("s0", fields, new Object[][]{{"E1", null, "E1", 2L, new Object[]{"E2", "E1"}}, {"E2", "E1", "E1", 2L, new Object[]{"E2", "E1"}}});

            env.milestone(2);

            env.advanceTime(2500);
            env.sendEventBean(makeMarketDataEvent("E3"));
            env.sendEventBean(makeMarketDataEvent("E4"));

            env.milestone(3);

            env.advanceTime(2600);
            env.sendEventBean(makeMarketDataEvent("E5"));

            env.milestone(4);

            env.advanceTime(3500);
            Object[] win = new Object[]{"E5", "E4", "E3"};
            env.assertListener("s0", listener -> {
                EPAssertionUtil.assertPropsPerRow(listener.getNewDataListFlattened(), fields,
                    new Object[][]{{"E3", null, "E3", 3L, win}, {"E4", "E3", "E3", 3L, win}, {"E5", "E4", "E3", 3L, win}});
                EPAssertionUtil.assertPropsPerRow(listener.getOldDataListFlattened(), fields,
                    new Object[][]{{"E1", null, null, null, null}, {"E2", null, null, null, null}});
                listener.reset();
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousExprNameAndTypeAndSODA implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select " +
                "prev(1,intPrimitive), " +
                "prev(1,sb), " +
                "prevtail(1,intPrimitive), " +
                "prevtail(1,sb), " +
                "prevwindow(intPrimitive), " +
                "prevwindow(sb), " +
                "prevcount(intPrimitive), " +
                "prevcount(sb) " +
                "from SupportBean#time(1 minutes) as sb";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("E1", 1));
            env.sendEventBean(new SupportBean("E2", 2));
            env.assertListener("s0", listener -> {
                EventBean resultBean = listener.getNewDataListFlattened()[1];
                Object[][] rows = new Object[][]{
                    {"prev(1,intPrimitive)", Integer.class},
                    {"prev(1,sb)", SupportBean.class},
                    {"prevtail(1,intPrimitive)", Integer.class},
                    {"prevtail(1,sb)", SupportBean.class},
                    {"prevwindow(intPrimitive)", Integer[].class},
                    {"prevwindow(sb)", SupportBean[].class},
                    {"prevcount(intPrimitive)", Long.class},
                    {"prevcount(sb)", Long.class}
                };
                env.assertStatement("s0", statement -> {
                    for (int i = 0; i < rows.length; i++) {
                        String message = "For prop '" + rows[i][0] + "'";
                        EventPropertyDescriptor prop = statement.getEventType().getPropertyDescriptors()[i];
                        assertEquals(message, rows[i][0], prop.getPropertyName());
                        assertEquals(message, rows[i][1], prop.getPropertyType());
                        Object result = resultBean.get(prop.getPropertyName());
                        assertEquals(message, prop.getPropertyType(), result.getClass());
                    }
                });
            });

            env.undeployAll();

            env.eplToModelCompileDeploy(epl).addListener("s0").milestone(1);
            env.undeployAll();
        }
    }

    private static class ExprCorePreviousPrevStream implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select prev(1, s0) as result, " +
                "prevtail(0, s0) as tailresult," +
                "prevwindow(s0) as windowresult," +
                "prevcount(s0) as countresult " +
                "from SupportBean_S0#length(2) as s0";
            env.compileDeploy(epl).addListener("s0");

            String[] fields = "result,tailresult,windowresult,countresult".split(",");

            SupportBean_S0 e1 = new SupportBean_S0(1);
            env.sendEventBean(e1);

            env.assertPropsNew("s0", fields,
                new Object[]{null, e1, new Object[]{e1}, 1L});

            SupportBean_S0 e2 = new SupportBean_S0(2);
            env.sendEventBean(e2);
            env.assertPropsNew("s0", fields,
                new Object[]{e1, e1, new Object[]{e2, e1}, 2L});
            env.assertStmtType("s0", "result", EPTypePremade.getOrCreate(SupportBean_S0.class));

            SupportBean_S0 e3 = new SupportBean_S0(3);
            env.sendEventBean(e3);
            env.assertPropsNew("s0", fields,
                new Object[]{e2, e2, new Object[]{e3, e2}, 2L});

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousPrevCountStarWithStaticMethod implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream count(*) as total, " +
                "prev(" + ExprCorePrevious.class.getName() + ".intToLong(count(*)) - 1, price) as firstPrice from SupportMarketDataBean#time(60)";
            env.compileDeploy(epl).addListener("s0");

            assertPrevCount(env);

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousPrevCountStar implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream count(*) as total, " +
                "prev(count(*) - 1, price) as firstPrice from SupportMarketDataBean#time(60)";
            env.compileDeploy(epl).addListener("s0");

            assertPrevCount(env);

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousPerGroupTwoCriteria implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select symbol, feed, " +
                "prev(1, price) as prevPrice, " +
                "prevtail(price) as tailPrice, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol, feed)#length(2)";
            env.compileDeploy(epl).addListener("s0");

            String[] fields = "symbol,feed,prevPrice,tailPrice,countPrice,windowPrice".split(",");

            env.sendEventBean(new SupportMarketDataBean("IBM", 10, 0L, "F1"));
            env.assertPropsNew("s0", fields, new Object[]{"IBM", "F1", null, 10d, 1L, splitDoubles("10d")});

            env.sendEventBean(new SupportMarketDataBean("IBM", 11, 0L, "F1"));
            env.assertPropsNew("s0", fields, new Object[]{"IBM", "F1", 10d, 10d, 2L, splitDoubles("11d,10d")});

            env.sendEventBean(new SupportMarketDataBean("MSFT", 100, 0L, "F2"));
            env.assertPropsNew("s0", fields, new Object[]{"MSFT", "F2", null, 100d, 1L, splitDoubles("100d")});

            env.sendEventBean(new SupportMarketDataBean("IBM", 12, 0L, "F2"));
            env.assertPropsNew("s0", fields, new Object[]{"IBM", "F2", null, 12d, 1L, splitDoubles("12d")});

            env.sendEventBean(new SupportMarketDataBean("IBM", 13, 0L, "F1"));
            env.assertPropsNew("s0", fields, new Object[]{"IBM", "F1", 11d, 11d, 2L, splitDoubles("13d,11d")});

            env.sendEventBean(new SupportMarketDataBean("MSFT", 101, 0L, "F2"));
            env.assertPropsNew("s0", fields, new Object[]{"MSFT", "F2", 100d, 100d, 2L, splitDoubles("101d,100d")});

            env.sendEventBean(new SupportMarketDataBean("IBM", 17, 0L, "F2"));
            env.assertPropsNew("s0", fields, new Object[]{"IBM", "F2", 12d, 12d, 2L, splitDoubles("17d,12d")});

            env.undeployAll();

            // test length window overflow
            env.compileDeployAddListenerMile("@name('s0') select prev(5,intPrimitive) as val0 from SupportBean#groupwin(theString)#length(5)", "s0", 1);

            env.sendEventBean(new SupportBean("A", 11));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("A", 12));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("A", 13));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("A", 14));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("A", 15));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("C", 20));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("C", 21));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("C", 22));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("C", 23));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("C", 24));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("B", 31));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("C", 25));
            env.assertEqualsNew("s0", "val0", null);

            env.sendEventBean(new SupportBean("A", 16));
            env.assertEqualsNew("s0", "val0", null);

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousSortWindowPerGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            // descending sort
            String epl = "@name('s0')select " +
                "symbol, " +
                "prev(1, price) as prevPrice, " +
                "prev(2, price) as prevPrevPrice, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol)#sort(10, price asc)";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrevPrice"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail0Price"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail1Price"));
                assertEquals(Long.class, statement.getEventType().getPropertyType("countPrice"));
                assertEquals(Double[].class, statement.getEventType().getPropertyType("windowPrice"));
            });

            sendMarketEvent(env, "IBM", 75);
            assertReceived(env, "IBM", null, null, 75d, null, 1L, splitDoubles("75d"));
            sendMarketEvent(env, "IBM", 80);
            assertReceived(env, "IBM", 80d, null, 80d, 75d, 2L, splitDoubles("75d,80d"));
            sendMarketEvent(env, "IBM", 79);
            assertReceived(env, "IBM", 79d, 80d, 80d, 79d, 3L, splitDoubles("75d,79d,80d"));
            sendMarketEvent(env, "IBM", 81);
            assertReceived(env, "IBM", 79d, 80d, 81d, 80d, 4L, splitDoubles("75d,79d,80d,81d"));
            sendMarketEvent(env, "IBM", 79.5);
            assertReceived(env, "IBM", 79d, 79.5d, 81d, 80d, 5L, splitDoubles("75d,79d,79.5,80d,81d"));    // 75, 79, 79.5, 80, 81

            sendMarketEvent(env, "MSFT", 10);
            assertReceived(env, "MSFT", null, null, 10d, null, 1L, splitDoubles("10d"));
            sendMarketEvent(env, "MSFT", 20);
            assertReceived(env, "MSFT", 20d, null, 20d, 10d, 2L, splitDoubles("10d,20d"));
            sendMarketEvent(env, "MSFT", 21);
            assertReceived(env, "MSFT", 20d, 21d, 21d, 20d, 3L, splitDoubles("10d,20d,21d")); // 10, 20, 21

            sendMarketEvent(env, "IBM", 74d);
            assertReceived(env, "IBM", 75d, 79d, 81d, 80d, 6L, splitDoubles("74d,75d,79d,79.5,80d,81d"));  // 74, 75, 79, 79.5, 80, 81

            sendMarketEvent(env, "MSFT", 19);
            assertReceived(env, "MSFT", 19d, 20d, 21d, 20d, 4L, splitDoubles("10d,19d,20d,21d")); // 10, 19, 20, 21

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousTimeBatchPerGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);

            String epl = "@name('s0')select " +
                "symbol, " +
                "prev(1, price) as prevPrice, " +
                "prev(2, price) as prevPrevPrice, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol)#time_batch(1 sec)";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrevPrice"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail0Price"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail1Price"));
            });

            sendMarketEvent(env, "IBM", 75);
            sendMarketEvent(env, "MSFT", 40);
            sendMarketEvent(env, "IBM", 76);
            sendMarketEvent(env, "CIC", 1);
            sendTimer(env, 1000);

            env.assertListener("s0", listener -> {
                EventBean[] events = listener.getLastNewData();
                // order not guaranteed as timed batch, however for testing the order is reliable as schedule buckets are created
                // in a predictable order
                // Previous is looking at the same batch, doesn't consider outside of window
                assertReceived(events[0], "IBM", null, null, 75d, 76d, 2L, splitDoubles("76d,75d"));
                assertReceived(events[1], "IBM", 75d, null, 75d, 76d, 2L, splitDoubles("76d,75d"));
                assertReceived(events[2], "MSFT", null, null, 40d, null, 1L, splitDoubles("40d"));
                assertReceived(events[3], "CIC", null, null, 1d, null, 1L, splitDoubles("1d"));
            });

            // Next batch, previous is looking only within the same batch
            sendMarketEvent(env, "MSFT", 41);
            sendMarketEvent(env, "IBM", 77);
            sendMarketEvent(env, "IBM", 78);
            sendMarketEvent(env, "CIC", 2);
            sendMarketEvent(env, "MSFT", 42);
            sendMarketEvent(env, "CIC", 3);
            sendMarketEvent(env, "CIC", 4);
            sendTimer(env, 2000);

            env.assertListener("s0", listener -> {
                EventBean[] events = listener.getLastNewData();
                assertReceived(events[0], "IBM", null, null, 77d, 78d, 2L, splitDoubles("78d,77d"));
                assertReceived(events[1], "IBM", 77d, null, 77d, 78d, 2L, splitDoubles("78d,77d"));
                assertReceived(events[2], "MSFT", null, null, 41d, 42d, 2L, splitDoubles("42d,41d"));
                assertReceived(events[3], "MSFT", 41d, null, 41d, 42d, 2L, splitDoubles("42d,41d"));
                assertReceived(events[4], "CIC", null, null, 2d, 3d, 3L, splitDoubles("4d,3d,2d"));
                assertReceived(events[5], "CIC", 2d, null, 2d, 3d, 3L, splitDoubles("4d,3d,2d"));
                assertReceived(events[6], "CIC", 3d, 2d, 2d, 3d, 3L, splitDoubles("4d,3d,2d"));
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousLengthBatchPerGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            // Also testing the alternative syntax here of "prev(property)" and "prev(property, index)" versus "prev(index, property)"
            String epl = "@name('s0')select irstream " +
                "symbol, " +
                "prev(price) as prevPrice, " +
                "prev(price, 2) as prevPrevPrice, " +
                "prevtail(price, 0) as prevTail0Price, " +
                "prevtail(price, 1) as prevTail1Price, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol)#length_batch(3)";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrevPrice"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail0Price"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail1Price"));
            });

            sendMarketEvent(env, "IBM", 75);
            sendMarketEvent(env, "MSFT", 50);
            sendMarketEvent(env, "IBM", 76);
            sendMarketEvent(env, "CIC", 1);
            env.assertListenerNotInvoked("s0");
            sendMarketEvent(env, "IBM", 77);

            env.assertListener("s0", listener -> {
                EventBean[] events = listener.getLastNewData();
                assertEquals(3, events.length);
                assertReceived(events[0], "IBM", null, null, 75d, 76d, 3L, splitDoubles("77d,76d,75d"));
                assertReceived(events[1], "IBM", 75d, null, 75d, 76d, 3L, splitDoubles("77d,76d,75d"));
                assertReceived(events[2], "IBM", 76d, 75d, 75d, 76d, 3L, splitDoubles("77d,76d,75d"));
                listener.reset();
            });

            // Next batch, previous is looking only within the same batch
            sendMarketEvent(env, "MSFT", 51);
            sendMarketEvent(env, "IBM", 78);
            sendMarketEvent(env, "IBM", 79);
            sendMarketEvent(env, "CIC", 2);
            sendMarketEvent(env, "CIC", 3);

            env.assertListener("s0", listener -> {
                EventBean[] events = listener.getLastNewData();
                assertEquals(3, events.length);
                assertReceived(events[0], "CIC", null, null, 1d, 2d, 3L, splitDoubles("3d,2d,1d"));
                assertReceived(events[1], "CIC", 1d, null, 1d, 2d, 3L, splitDoubles("3d,2d,1d"));
                assertReceived(events[2], "CIC", 2d, 1d, 1d, 2d, 3L, splitDoubles("3d,2d,1d"));
                listener.reset();
            });

            sendMarketEvent(env, "MSFT", 52);

            env.assertListener("s0", listener -> {
                EventBean[] events = listener.getLastNewData();
                assertEquals(3, events.length);
                assertReceived(events[0], "MSFT", null, null, 50d, 51d, 3L, splitDoubles("52d,51d,50d"));
                assertReceived(events[1], "MSFT", 50d, null, 50d, 51d, 3L, splitDoubles("52d,51d,50d"));
                assertReceived(events[2], "MSFT", 51d, 50d, 50d, 51d, 3L, splitDoubles("52d,51d,50d"));
                listener.reset();
            });

            sendMarketEvent(env, "IBM", 80);

            env.assertListener("s0", listener -> {
                EventBean[] eventsNew = listener.getLastNewData();
                EventBean[] eventsOld = listener.getLastOldData();
                assertEquals(3, eventsNew.length);
                assertEquals(3, eventsOld.length);
                assertReceived(eventsNew[0], "IBM", null, null, 78d, 79d, 3L, splitDoubles("80d,79d,78d"));
                assertReceived(eventsNew[1], "IBM", 78d, null, 78d, 79d, 3L, splitDoubles("80d,79d,78d"));
                assertReceived(eventsNew[2], "IBM", 79d, 78d, 78d, 79d, 3L, splitDoubles("80d,79d,78d"));
                assertReceived(eventsOld[0], "IBM", null, null, null, null, null, null);
                assertReceived(eventsOld[1], "IBM", null, null, null, null, null, null);
                assertReceived(eventsOld[2], "IBM", null, null, null, null, null, null);
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousTimeWindowPerGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select " +
                "symbol, " +
                "prev(1, price) as prevPrice, " +
                "prev(2, price) as prevPrevPrice, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol)#time(20 sec) ";
            assertPerGroup(epl, env);
        }
    }

    private static class ExprCorePreviousExtTimeWindowPerGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select " +
                "symbol, " +
                "prev(1, price) as prevPrice, " +
                "prev(2, price) as prevPrevPrice, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol)#ext_timed(volume, 20 sec) ";
            assertPerGroup(epl, env);
        }
    }

    private static class ExprCorePreviousLengthWindowPerGroup implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select symbol, " +
                "prev(1, price) as prevPrice, " +
                "prev(2, price) as prevPrevPrice, " +
                "prevtail(price, 0) as prevTail0Price, " +
                "prevtail(price, 1) as prevTail1Price, " +
                "prevcount(price) as countPrice, " +
                "prevwindow(price) as windowPrice " +
                "from SupportMarketDataBean#groupwin(symbol)#length(10) ";
            assertPerGroup(epl, env);
        }
    }

    private static class ExprCorePreviousTimeWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream symbol as currSymbol, " +
                " prev(2, symbol) as prevSymbol, " +
                " prev(2, price) as prevPrice, " +
                " prevtail(0, symbol) as prevTailSymbol, " +
                " prevtail(0, price) as prevTailPrice, " +
                " prevtail(1, symbol) as prevTail1Symbol, " +
                " prevtail(1, price) as prevTail1Price, " +
                " prevcount(price) as prevCountPrice, " +
                " prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#time(1 min) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prevSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
            });

            sendTimer(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D1", 1);
            assertNewEventWTail(env, "D1", null, null, "D1", 1d, null, null, 1L, splitDoubles("1d"));

            sendTimer(env, 1000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D2", 2);
            assertNewEventWTail(env, "D2", null, null, "D1", 1d, "D2", 2d, 2L, splitDoubles("2d,1d"));

            sendTimer(env, 2000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D3", 3);
            assertNewEventWTail(env, "D3", "D1", 1d, "D1", 1d, "D2", 2d, 3L, splitDoubles("3d,2d,1d"));

            sendTimer(env, 3000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D4", 4);
            assertNewEventWTail(env, "D4", "D2", 2d, "D1", 1d, "D2", 2d, 4L, splitDoubles("4d,3d,2d,1d"));

            sendTimer(env, 4000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D5", 5);
            assertNewEventWTail(env, "D5", "D3", 3d, "D1", 1d, "D2", 2d, 5L, splitDoubles("5d,4d,3d,2d,1d"));

            sendTimer(env, 30000);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "D6", 6);
            assertNewEventWTail(env, "D6", "D4", 4d, "D1", 1d, "D2", 2d, 6L, splitDoubles("6d,5d,4d,3d,2d,1d"));

            // Test remove stream, always returns null as previous function
            // returns null for remove stream for time windows
            sendTimer(env, 60000);
            assertOldEventWTail(env, "D1", null, null, null, null, null, null, null, null);
            sendTimer(env, 61000);
            assertOldEventWTail(env, "D2", null, null, null, null, null, null, null, null);
            sendTimer(env, 62000);
            assertOldEventWTail(env, "D3", null, null, null, null, null, null, null, null);
            sendTimer(env, 63000);
            assertOldEventWTail(env, "D4", null, null, null, null, null, null, null, null);
            sendTimer(env, 64000);
            assertOldEventWTail(env, "D5", null, null, null, null, null, null, null, null);
            sendTimer(env, 90000);
            assertOldEventWTail(env, "D6", null, null, null, null, null, null, null, null);

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousExtTimedWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream symbol as currSymbol, " +
                " prev(2, symbol) as prevSymbol, " +
                " prev(2, price) as prevPrice, " +
                " prevtail(0, symbol) as prevTailSymbol, " +
                " prevtail(0, price) as prevTailPrice, " +
                " prevtail(1, symbol) as prevTail1Symbol, " +
                " prevtail(1, price) as prevTail1Price, " +
                " prevcount(price) as prevCountPrice, " +
                " prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#ext_timed(volume, 1 min) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prevSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
                assertEquals(String.class, statement.getEventType().getPropertyType("prevTailSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevTailPrice"));
            });

            sendMarketEvent(env, "D1", 1, 0);
            assertNewEventWTail(env, "D1", null, null, "D1", 1d, null, null, 1L, splitDoubles("1d"));

            sendMarketEvent(env, "D2", 2, 1000);
            assertNewEventWTail(env, "D2", null, null, "D1", 1d, "D2", 2d, 2L, splitDoubles("2d,1d"));

            sendMarketEvent(env, "D3", 3, 3000);
            assertNewEventWTail(env, "D3", "D1", 1d, "D1", 1d, "D2", 2d, 3L, splitDoubles("3d,2d,1d"));

            sendMarketEvent(env, "D4", 4, 4000);
            assertNewEventWTail(env, "D4", "D2", 2d, "D1", 1d, "D2", 2d, 4L, splitDoubles("4d,3d,2d,1d"));

            sendMarketEvent(env, "D5", 5, 5000);
            assertNewEventWTail(env, "D5", "D3", 3d, "D1", 1d, "D2", 2d, 5L, splitDoubles("5d,4d,3d,2d,1d"));

            sendMarketEvent(env, "D6", 6, 30000);
            assertNewEventWTail(env, "D6", "D4", 4d, "D1", 1d, "D2", 2d, 6L, splitDoubles("6d,5d,4d,3d,2d,1d"));

            sendMarketEvent(env, "D7", 7, 60000);
            env.assertListener("s0", listener -> {
                assertEventWTail(listener.getLastNewData()[0], "D7", "D5", 5d, "D2", 2d, "D3", 3d, 6L, splitDoubles("7d,6d,5d,4d,3d,2d"));
                assertEventWTail(listener.getLastOldData()[0], "D1", null, null, null, null, null, null, null, null);
                listener.reset();
            });

            sendMarketEvent(env, "D8", 8, 61000);
            env.assertListener("s0", listener -> {
                assertEventWTail(listener.getLastNewData()[0], "D8", "D6", 6d, "D3", 3d, "D4", 4d, 6L, splitDoubles("8d,7d,6d,5d,4d,3d"));
                assertEventWTail(listener.getLastOldData()[0], "D2", null, null, null, null, null, null, null, null);
                listener.reset();
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousTimeBatchWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream symbol as currSymbol, " +
                " prev(2, symbol) as prevSymbol, " +
                " prev(2, price) as prevPrice, " +
                " prevtail(0, symbol) as prevTailSymbol, " +
                " prevtail(0, price) as prevTailPrice, " +
                " prevtail(1, symbol) as prevTail1Symbol, " +
                " prevtail(1, price) as prevTail1Price, " +
                " prevcount(price) as prevCountPrice, " +
                " prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#time_batch(1 min) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prevSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
            });

            sendTimer(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 2);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 60000);
            env.assertListener("s0", listener -> {
                assertEquals(2, listener.getLastNewData().length);
                assertEventWTail(listener.getLastNewData()[0], "A", null, null, "A", 1d, "B", 2d, 2L, splitDoubles("2d,1d"));
                assertEventWTail(listener.getLastNewData()[1], "B", null, null, "A", 1d, "B", 2d, 2L, splitDoubles("2d,1d"));
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            sendTimer(env, 80000);
            sendMarketEvent(env, "C", 3);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 120000);
            env.assertListener("s0", listener -> {
                assertEquals(1, listener.getLastNewData().length);
                assertEventWTail(listener.getLastNewData()[0], "C", null, null, "C", 3d, null, null, 1L, splitDoubles("3d"));
                assertEquals(2, listener.getLastOldData().length);
                assertEventWTail(listener.getLastOldData()[0], "A", null, null, null, null, null, null, null, null);
                listener.reset();
            });

            sendTimer(env, 300000);
            sendMarketEvent(env, "D", 4);
            sendMarketEvent(env, "E", 5);
            sendMarketEvent(env, "F", 6);
            sendMarketEvent(env, "G", 7);
            sendTimer(env, 360000);
            env.assertListener("s0", listener -> {
                assertEquals(4, listener.getLastNewData().length);
                assertEventWTail(listener.getLastNewData()[0], "D", null, null, "D", 4d, "E", 5d, 4L, splitDoubles("7d,6d,5d,4d"));
                assertEventWTail(listener.getLastNewData()[1], "E", null, null, "D", 4d, "E", 5d, 4L, splitDoubles("7d,6d,5d,4d"));
                assertEventWTail(listener.getLastNewData()[2], "F", "D", 4d, "D", 4d, "E", 5d, 4L, splitDoubles("7d,6d,5d,4d"));
                assertEventWTail(listener.getLastNewData()[3], "G", "E", 5d, "D", 4d, "E", 5d, 4L, splitDoubles("7d,6d,5d,4d"));
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousTimeBatchWindowJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select theString as currSymbol, " +
                " prev(2, symbol) as prevSymbol, " +
                " prev(1, price) as prevPrice, " +
                " prevtail(0, symbol) as prevTailSymbol, " +
                " prevtail(0, price) as prevTailPrice, " +
                " prevtail(1, symbol) as prevTail1Symbol, " +
                " prevtail(1, price) as prevTail1Price, " +
                " prevcount(price) as prevCountPrice, " +
                " prevwindow(price) as prevWindowPrice " +
                "from SupportBean#keepall, SupportMarketDataBean#time_batch(1 min)";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prevSymbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
            });

            sendTimer(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 2);
            sendBeanEvent(env, "X1");
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 60000);
            env.assertListener("s0", listener -> {
                assertEquals(2, listener.getLastNewData().length);
                assertEventWTail(listener.getLastNewData()[0], "X1", null, null, "A", 1d, "B", 2d, 2L, splitDoubles("2d,1d"));
                assertEventWTail(listener.getLastNewData()[1], "X1", null, 1d, "A", 1d, "B", 2d, 2L, splitDoubles("2d,1d"));
                assertNull(listener.getLastOldData());
                listener.reset();
            });

            sendMarketEvent(env, "C1", 11);
            sendMarketEvent(env, "C2", 12);
            sendMarketEvent(env, "C3", 13);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 120000);
            env.assertListener("s0", listener -> {
                assertEquals(3, listener.getLastNewData().length);
                assertEventWTail(listener.getLastNewData()[0], "X1", null, null, "C1", 11d, "C2", 12d, 3L, splitDoubles("13d,12d,11d"));
                assertEventWTail(listener.getLastNewData()[1], "X1", null, 11d, "C1", 11d, "C2", 12d, 3L, splitDoubles("13d,12d,11d"));
                assertEventWTail(listener.getLastNewData()[2], "X1", "C1", 12d, "C1", 11d, "C2", 12d, 3L, splitDoubles("13d,12d,11d"));
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousLengthWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream symbol as currSymbol, " +
                "prev(0, symbol) as prev0Symbol, " +
                "prev(1, symbol) as prev1Symbol, " +
                "prev(2, symbol) as prev2Symbol, " +
                "prev(0, price) as prev0Price, " +
                "prev(1, price) as prev1Price, " +
                "prev(2, price) as prev2Price," +
                "prevtail(0, symbol) as prevTail0Symbol, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, symbol) as prevTail1Symbol, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as prevCountPrice, " +
                "prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#length(3) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prev0Symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prev0Price"));
            });

            sendMarketEvent(env, "A", 1);
            assertNewEvents(env, "A", "A", 1d, null, null, null, null, "A", 1d, null, null, 1L, splitDoubles("1d"));

            env.milestone(1);

            sendMarketEvent(env, "B", 2);
            assertNewEvents(env, "B", "B", 2d, "A", 1d, null, null, "A", 1d, "B", 2d, 2L, splitDoubles("2d,1d"));

            env.milestone(2);

            sendMarketEvent(env, "C", 3);
            assertNewEvents(env, "C", "C", 3d, "B", 2d, "A", 1d, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d"));

            env.milestone(3);

            sendMarketEvent(env, "D", 4);
            env.assertListener("s0", listener -> {
                EventBean newEvent = listener.getLastNewData()[0];
                EventBean oldEvent = listener.getLastOldData()[0];
                assertEventProps(env, newEvent, "D", "D", 4d, "C", 3d, "B", 2d, "B", 2d, "C", 3d, 3L, splitDoubles("4d,3d,2d"));
                assertEventProps(env, oldEvent, "A", null, null, null, null, null, null, null, null, null, null, null, null);
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousLengthBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select irstream symbol as currSymbol, " +
                "prev(0, symbol) as prev0Symbol, " +
                "prev(1, symbol) as prev1Symbol, " +
                "prev(2, symbol) as prev2Symbol, " +
                "prev(0, price) as prev0Price, " +
                "prev(1, price) as prev1Price, " +
                "prev(2, price) as prev2Price, " +
                "prevtail(0, symbol) as prevTail0Symbol, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, symbol) as prevTail1Symbol, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as prevCountPrice, " +
                "prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#length_batch(3) ";
            env.compileDeploy(epl).addListener("s0");

            // assert select result type
            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prev0Symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prev0Price"));
            });

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 2);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "C", 3);
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getLastNewData();
                assertEquals(3, newEvents.length);
                assertEventProps(env, newEvents[0], "A", "A", 1d, null, null, null, null, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d"));
                assertEventProps(env, newEvents[1], "B", "B", 2d, "A", 1d, null, null, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d"));
                assertEventProps(env, newEvents[2], "C", "C", 3d, "B", 2d, "A", 1d, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d"));
                listener.reset();
            });

            sendMarketEvent(env, "D", 4);
            sendMarketEvent(env, "E", 5);
            env.assertListenerNotInvoked("s0");

            sendMarketEvent(env, "F", 6);
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getLastNewData();
                EventBean[] oldEvents = listener.getLastOldData();
                assertEquals(3, newEvents.length);
                assertEquals(3, oldEvents.length);
                assertEventProps(env, newEvents[0], "D", "D", 4d, null, null, null, null, "D", 4d, "E", 5d, 3L, splitDoubles("6d,5d,4d"));
                assertEventProps(env, newEvents[1], "E", "E", 5d, "D", 4d, null, null, "D", 4d, "E", 5d, 3L, splitDoubles("6d,5d,4d"));
                assertEventProps(env, newEvents[2], "F", "F", 6d, "E", 5d, "D", 4d, "D", 4d, "E", 5d, 3L, splitDoubles("6d,5d,4d"));
                assertEventProps(env, oldEvents[0], "A", null, null, null, null, null, null, null, null, null, null, null, null);
                assertEventProps(env, oldEvents[1], "B", null, null, null, null, null, null, null, null, null, null, null, null);
                assertEventProps(env, oldEvents[2], "C", null, null, null, null, null, null, null, null, null, null, null, null);
            });

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousLengthWindowWhere implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select prev(2, symbol) as currSymbol " +
                "from SupportMarketDataBean#length(100) " +
                "where prev(2, price) > 100";
            env.compileDeploy(epl).addListener("s0");

            sendMarketEvent(env, "A", 1);
            sendMarketEvent(env, "B", 130);
            sendMarketEvent(env, "C", 10);
            env.assertListenerNotInvoked("s0");
            sendMarketEvent(env, "D", 5);
            env.assertEqualsNew("s0", "currSymbol", "B");

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousLengthWindowDynamic implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select prev(intPrimitive, theString) as sPrev " +
                "from SupportBean#length(100)";
            env.compileDeploy(epl).addListener("s0");

            sendBeanEvent(env, "A", 1);
            env.assertEqualsNew("s0", "sPrev", null);

            sendBeanEvent(env, "B", 0);
            env.assertEqualsNew("s0", "sPrev", "B");

            sendBeanEvent(env, "C", 2);
            env.assertEqualsNew("s0", "sPrev", "A");

            sendBeanEvent(env, "D", 1);
            env.assertEqualsNew("s0", "sPrev", "C");

            sendBeanEvent(env, "E", 4);
            env.assertEqualsNew("s0", "sPrev", "A");

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousSortWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0')select symbol as currSymbol, " +
                " prev(0, symbol) as prev0Symbol, " +
                " prev(1, symbol) as prev1Symbol, " +
                " prev(2, symbol) as prev2Symbol, " +
                " prev(0, price) as prev0Price, " +
                " prev(1, price) as prev1Price, " +
                " prev(2, price) as prev2Price, " +
                " prevtail(0, symbol) as prevTail0Symbol, " +
                " prevtail(0, price) as prevTail0Price, " +
                " prevtail(1, symbol) as prevTail1Symbol, " +
                " prevtail(1, price) as prevTail1Price, " +
                " prevcount(price) as prevCountPrice, " +
                " prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#sort(100, symbol asc)";
            env.compileDeploy(epl).addListener("s0");

            env.assertStatement("s0", statement -> {
                assertEquals(String.class, statement.getEventType().getPropertyType("prev0Symbol"));
                assertEquals(Double.class, statement.getEventType().getPropertyType("prev0Price"));
            });

            sendMarketEvent(env, "COX", 30);
            assertNewEvents(env, "COX", "COX", 30d, null, null, null, null, "COX", 30d, null, null, 1L, splitDoubles("30d"));

            sendMarketEvent(env, "IBM", 45);
            assertNewEvents(env, "IBM", "COX", 30d, "IBM", 45d, null, null, "IBM", 45d, "COX", 30d, 2L, splitDoubles("30d,45d"));

            sendMarketEvent(env, "MSFT", 33);
            assertNewEvents(env, "MSFT", "COX", 30d, "IBM", 45d, "MSFT", 33d, "MSFT", 33d, "IBM", 45d, 3L, splitDoubles("30d,45d,33d"));

            sendMarketEvent(env, "XXX", 55);
            assertNewEvents(env, "XXX", "COX", 30d, "IBM", 45d, "MSFT", 33d, "XXX", 55d, "MSFT", 33d, 4L, splitDoubles("30d,45d,33d,55d"));

            sendMarketEvent(env, "CXX", 56);
            assertNewEvents(env, "CXX", "COX", 30d, "CXX", 56d, "IBM", 45d, "XXX", 55d, "MSFT", 33d, 5L, splitDoubles("30d,56d,45d,33d,55d"));

            sendMarketEvent(env, "GE", 1);
            assertNewEvents(env, "GE", "COX", 30d, "CXX", 56d, "GE", 1d, "XXX", 55d, "MSFT", 33d, 6L, splitDoubles("30d,56d,1d,45d,33d,55d"));

            sendMarketEvent(env, "AAA", 1);
            assertNewEvents(env, "AAA", "AAA", 1d, "COX", 30d, "CXX", 56d, "XXX", 55d, "MSFT", 33d, 7L, splitDoubles("1d,30d,56d,1d,45d,33d,55d"));

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousExtTimedBatch implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "currSymbol,prev0Symbol,prev0Price,prev1Symbol,prev1Price,prev2Symbol,prev2Price,prevTail0Symbol,prevTail0Price,prevTail1Symbol,prevTail1Price,prevCountPrice,prevWindowPrice".split(",");
            String epl = "@name('s0')select irstream symbol as currSymbol, " +
                "prev(0, symbol) as prev0Symbol, " +
                "prev(0, price) as prev0Price, " +
                "prev(1, symbol) as prev1Symbol, " +
                "prev(1, price) as prev1Price, " +
                "prev(2, symbol) as prev2Symbol, " +
                "prev(2, price) as prev2Price," +
                "prevtail(0, symbol) as prevTail0Symbol, " +
                "prevtail(0, price) as prevTail0Price, " +
                "prevtail(1, symbol) as prevTail1Symbol, " +
                "prevtail(1, price) as prevTail1Price, " +
                "prevcount(price) as prevCountPrice, " +
                "prevwindow(price) as prevWindowPrice " +
                "from SupportMarketDataBean#ext_timed_batch(volume, 10, 0L) ";
            env.compileDeploy(epl).addListener("s0");

            sendMarketEvent(env, "A", 1, 1000);
            sendMarketEvent(env, "B", 2, 1001);
            sendMarketEvent(env, "C", 3, 1002);
            sendMarketEvent(env, "D", 4, 10000);

            env.assertPropsPerRowIRPair("s0", fields,
                new Object[][]{
                    {"A", "A", 1d, null, null, null, null, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d")},
                    {"B", "B", 2d, "A", 1d, null, null, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d")},
                    {"C", "C", 3d, "B", 2d, "A", 1d, "A", 1d, "B", 2d, 3L, splitDoubles("3d,2d,1d")}
                },
                null);

            sendMarketEvent(env, "E", 5, 20000);

            env.assertPropsPerRowIRPair("s0", fields,
                new Object[][]{
                    {"D", "D", 4d, null, null, null, null, "D", 4d, null, null, 1L, splitDoubles("4d")},
                },
                new Object[][]{
                    {"A", null, null, null, null, null, null, null, null, null, null, null, null},
                    {"B", null, null, null, null, null, null, null, null, null, null, null, null},
                    {"C", null, null, null, null, null, null, null, null, null, null, null, null},
                }
            );

            env.undeployAll();
        }
    }

    private static class ExprCorePreviousInvalid implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            env.tryInvalidCompile("select prev(0, average) " +
                    "from SupportMarketDataBean#length(100)#uni(price)",
                "Previous function requires a single data window view onto the stream [");

            env.tryInvalidCompile("select count(*) from SupportBean#keepall where prev(0, intPrimitive) = 5",
                "The 'prev' function may not occur in the where-clause or having-clause of a statement with aggregations as 'previous' does not provide remove stream data; Use the 'first','last','window' or 'count' aggregation functions instead [select count(*) from SupportBean#keepall where prev(0, intPrimitive) = 5]");

            env.tryInvalidCompile("select count(*) from SupportBean#keepall having prev(0, intPrimitive) = 5",
                "The 'prev' function may not occur in the where-clause or having-clause of a statement with aggregations as 'previous' does not provide remove stream data; Use the 'first','last','window' or 'count' aggregation functions instead [select count(*) from SupportBean#keepall having prev(0, intPrimitive) = 5]");
        }
    }

    private static void assertEventWTail(EventBean eventBean,
                                         String currSymbol,
                                         String prevSymbol,
                                         Double prevPrice,
                                         String prevTailSymbol,
                                         Double prevTailPrice,
                                         String prevTail1Symbol,
                                         Double prevTail1Price,
                                         Long prevcount,
                                         Object[] prevwindow) {
        assertEquals(currSymbol, eventBean.get("currSymbol"));
        assertEquals(prevSymbol, eventBean.get("prevSymbol"));
        assertEquals(prevPrice, eventBean.get("prevPrice"));
        assertEquals(prevTailSymbol, eventBean.get("prevTailSymbol"));
        assertEquals(prevTailPrice, eventBean.get("prevTailPrice"));
        assertEquals(prevTail1Symbol, eventBean.get("prevTail1Symbol"));
        assertEquals(prevTail1Price, eventBean.get("prevTail1Price"));
        assertEquals(prevcount, eventBean.get("prevCountPrice"));
        EPAssertionUtil.assertEqualsExactOrder((Object[]) eventBean.get("prevWindowPrice"), prevwindow);
    }

    private static void assertNewEvents(RegressionEnvironment env, String currSymbol,
                                        String prev0Symbol,
                                        Double prev0Price,
                                        String prev1Symbol,
                                        Double prev1Price,
                                        String prev2Symbol,
                                        Double prev2Price,
                                        String prevTail0Symbol,
                                        Double prevTail0Price,
                                        String prevTail1Symbol,
                                        Double prevTail1Price,
                                        Long prevCount,
                                        Object[] prevWindow) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(oldData);
            assertEquals(1, newData.length);
            assertEventProps(env, newData[0], currSymbol, prev0Symbol, prev0Price, prev1Symbol, prev1Price, prev2Symbol, prev2Price,
                prevTail0Symbol, prevTail0Price, prevTail1Symbol, prevTail1Price, prevCount, prevWindow);

            listener.reset();
        });
    }

    private static void assertEventProps(RegressionEnvironment env,
                                         EventBean eventBean,
                                         String currSymbol,
                                         String prev0Symbol,
                                         Double prev0Price,
                                         String prev1Symbol,
                                         Double prev1Price,
                                         String prev2Symbol,
                                         Double prev2Price,
                                         String prevTail0Symbol,
                                         Double prevTail0Price,
                                         String prevTail1Symbol,
                                         Double prevTail1Price,
                                         Long prevCount,
                                         Object[] prevWindow) {
        assertEquals(currSymbol, eventBean.get("currSymbol"));
        assertEquals(prev0Symbol, eventBean.get("prev0Symbol"));
        assertEquals(prev0Price, eventBean.get("prev0Price"));
        assertEquals(prev1Symbol, eventBean.get("prev1Symbol"));
        assertEquals(prev1Price, eventBean.get("prev1Price"));
        assertEquals(prev2Symbol, eventBean.get("prev2Symbol"));
        assertEquals(prev2Price, eventBean.get("prev2Price"));
        assertEquals(prevTail0Symbol, eventBean.get("prevTail0Symbol"));
        assertEquals(prevTail0Price, eventBean.get("prevTail0Price"));
        assertEquals(prevTail1Symbol, eventBean.get("prevTail1Symbol"));
        assertEquals(prevTail1Price, eventBean.get("prevTail1Price"));
        assertEquals(prevCount, eventBean.get("prevCountPrice"));
        EPAssertionUtil.assertEqualsExactOrder((Object[]) eventBean.get("prevWindowPrice"), prevWindow);

        env.listenerReset("s0");
    }

    private static void sendTimer(RegressionEnvironment env, long timeInMSec) {
        env.advanceTime(timeInMSec);
    }

    private static void sendMarketEvent(RegressionEnvironment env, String symbol, double price) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, 0L, null);
        env.sendEventBean(bean);
    }

    private static void sendMarketEvent(RegressionEnvironment env, String symbol, double price, long volume) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, volume, null);
        env.sendEventBean(bean);
    }

    private static void sendBeanEvent(RegressionEnvironment env, String theString) {
        SupportBean bean = new SupportBean();
        bean.setTheString(theString);
        env.sendEventBean(bean);
    }

    private static void sendBeanEvent(RegressionEnvironment env, String theString, int intPrimitive) {
        SupportBean bean = new SupportBean();
        bean.setTheString(theString);
        bean.setIntPrimitive(intPrimitive);
        env.sendEventBean(bean);
    }

    private static void assertNewEventWTail(RegressionEnvironment env, String currSymbol,
                                            String prevSymbol,
                                            Double prevPrice,
                                            String prevTailSymbol,
                                            Double prevTailPrice,
                                            String prevTail1Symbol,
                                            Double prevTail1Price,
                                            Long prevcount,
                                            Object[] prevwindow) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(oldData);
            assertEquals(1, newData.length);

            assertEventWTail(newData[0], currSymbol, prevSymbol, prevPrice, prevTailSymbol, prevTailPrice, prevTail1Symbol, prevTail1Price, prevcount, prevwindow);

            listener.reset();
        });
    }

    private static void assertOldEventWTail(RegressionEnvironment env,
                                            String currSymbol,
                                            String prevSymbol,
                                            Double prevPrice,
                                            String prevTailSymbol,
                                            Double prevTailPrice,
                                            String prevTail1Symbol,
                                            Double prevTail1Price,
                                            Long prevcount,
                                            Object[] prevwindow) {
        env.assertListener("s0", listener -> {
            EventBean[] oldData = listener.getLastOldData();
            EventBean[] newData = listener.getLastNewData();

            assertNull(newData);
            assertEquals(1, oldData.length);

            assertEventWTail(oldData[0], currSymbol, prevSymbol, prevPrice, prevTailSymbol, prevTailPrice, prevTail1Symbol, prevTail1Price, prevcount, prevwindow);

            listener.reset();
        });
    }

    private static void assertPerGroup(String epl, RegressionEnvironment env) {
        env.compileDeploy(epl).addListener("s0");

        // assert select result type.
        env.assertStatement("s0", statement -> {
            assertEquals(String.class, statement.getEventType().getPropertyType("symbol"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrice"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("prevPrevPrice"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail0Price"));
            assertEquals(Double.class, statement.getEventType().getPropertyType("prevTail1Price"));
            assertEquals(Long.class, statement.getEventType().getPropertyType("countPrice"));
            assertEquals(Double[].class, statement.getEventType().getPropertyType("windowPrice"));
        });

        sendMarketEvent(env, "IBM", 75);
        assertReceived(env, "IBM", null, null, 75d, null, 1L, splitDoubles("75d"));

        sendMarketEvent(env, "MSFT", 40);
        assertReceived(env, "MSFT", null, null, 40d, null, 1L, splitDoubles("40d"));

        sendMarketEvent(env, "IBM", 76);
        assertReceived(env, "IBM", 75d, null, 75d, 76d, 2L, splitDoubles("76d,75d"));

        sendMarketEvent(env, "CIC", 1);
        assertReceived(env, "CIC", null, null, 1d, null, 1L, splitDoubles("1d"));

        sendMarketEvent(env, "MSFT", 41);
        assertReceived(env, "MSFT", 40d, null, 40d, 41d, 2L, splitDoubles("41d,40d"));

        sendMarketEvent(env, "IBM", 77);
        assertReceived(env, "IBM", 76d, 75d, 75d, 76d, 3L, splitDoubles("77d,76d,75d"));

        sendMarketEvent(env, "IBM", 78);
        assertReceived(env, "IBM", 77d, 76d, 75d, 76d, 4L, splitDoubles("78d,77d,76d,75d"));

        sendMarketEvent(env, "CIC", 2);
        assertReceived(env, "CIC", 1d, null, 1d, 2d, 2L, splitDoubles("2d,1d"));

        sendMarketEvent(env, "MSFT", 42);
        assertReceived(env, "MSFT", 41d, 40d, 40d, 41d, 3L, splitDoubles("42d,41d,40d"));

        sendMarketEvent(env, "CIC", 3);
        assertReceived(env, "CIC", 2d, 1d, 1d, 2d, 3L, splitDoubles("3d,2d,1d"));

        env.undeployAll();
    }

    private static void assertReceived(RegressionEnvironment env, String symbol, Double prevPrice, Double prevPrevPrice,
                                       Double prevTail1Price, Double prevTail2Price,
                                       Long countPrice, Object[] windowPrice) {
        env.assertEventNew("s0", event -> assertReceived(event, symbol, prevPrice, prevPrevPrice, prevTail1Price, prevTail2Price, countPrice, windowPrice));
    }

    private static void assertReceived(EventBean theEvent, String symbol, Double prevPrice, Double prevPrevPrice,
                                       Double prevTail0Price, Double prevTail1Price,
                                       Long countPrice, Object[] windowPrice) {
        assertEquals(symbol, theEvent.get("symbol"));
        assertEquals(prevPrice, theEvent.get("prevPrice"));
        assertEquals(prevPrevPrice, theEvent.get("prevPrevPrice"));
        assertEquals(prevTail0Price, theEvent.get("prevTail0Price"));
        assertEquals(prevTail1Price, theEvent.get("prevTail1Price"));
        assertEquals(countPrice, theEvent.get("countPrice"));
        EPAssertionUtil.assertEqualsExactOrder(windowPrice, (Object[]) theEvent.get("windowPrice"));
    }

    private static void assertCountAndPrice(RegressionEnvironment env, Long total, Double price) {
        env.assertEventNew("s0", event -> assertCountAndPrice(event, total, price));
    }

    private static void assertCountAndPrice(EventBean event, Long total, Double price) {
        assertEquals(total, event.get("total"));
        assertEquals(price, event.get("firstPrice"));
    }

    private static void assertPrevCount(RegressionEnvironment env) {
        sendTimer(env, 0);
        sendMarketEvent(env, "IBM", 75);
        assertCountAndPrice(env, 1L, 75D);

        sendMarketEvent(env, "IBM", 76);
        assertCountAndPrice(env, 2L, 75D);

        sendTimer(env, 10000);
        sendMarketEvent(env, "IBM", 77);
        assertCountAndPrice(env, 3L, 75D);

        sendTimer(env, 20000);
        sendMarketEvent(env, "IBM", 78);
        assertCountAndPrice(env, 4L, 75D);

        sendTimer(env, 50000);
        sendMarketEvent(env, "IBM", 79);
        assertCountAndPrice(env, 5L, 75D);

        sendTimer(env, 60000);
        env.assertListener("s0", listener -> {
            assertEquals(1, listener.getOldDataList().size());
            EventBean[] oldData = listener.getLastOldData();
            assertEquals(2, oldData.length);
            assertCountAndPrice(oldData[0], 3L, null);
            listener.reset();
        });

        sendMarketEvent(env, "IBM", 80);
        assertCountAndPrice(env, 4L, 77D);

        sendTimer(env, 65000);
        env.assertListenerNotInvoked("s0");

        sendTimer(env, 70000);
        env.assertListener("s0", listener -> {
            assertEquals(1, listener.getOldDataList().size());
            EventBean[] oldData = listener.getLastOldData();
            assertEquals(1, oldData.length);
            assertCountAndPrice(oldData[0], 3L, null);
            listener.reset();
        });

        sendTimer(env, 80000);
        env.listenerReset("s0");

        sendMarketEvent(env, "IBM", 81);
        assertCountAndPrice(env, 3L, 79D);

        sendTimer(env, 120000);
        env.listenerReset("s0");

        sendMarketEvent(env, "IBM", 82);
        assertCountAndPrice(env, 2L, 81D);

        sendTimer(env, 300000);
        env.listenerReset("s0");

        sendMarketEvent(env, "IBM", 83);
        assertCountAndPrice(env, 1L, 83D);
    }

    private static SupportMarketDataBean makeMarketDataEvent(String symbol) {
        return new SupportMarketDataBean(symbol, 0, 0L, null);
    }

    // Don't remove me, I'm dynamically referenced by EPL
    public static Integer intToLong(Long longValue) {
        if (longValue == null) {
            return null;
        } else {
            return longValue.intValue();
        }
    }

    private static Object[] splitDoubles(String doubleList) {
        String[] doubles = doubleList.split(",");
        Object[] result = new Object[doubles.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Double.parseDouble(doubles[i]);
        }
        return result;
    }
}
