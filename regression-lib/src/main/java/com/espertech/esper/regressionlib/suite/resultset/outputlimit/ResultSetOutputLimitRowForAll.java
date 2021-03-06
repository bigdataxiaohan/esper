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
package com.espertech.esper.regressionlib.suite.resultset.outputlimit;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.internal.collection.UniformPair;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.support.SupportBean_S0;
import com.espertech.esper.common.internal.util.SerializableObjectCopier;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportMarketDataBean;
import com.espertech.esper.regressionlib.support.epl.SupportOutputLimitOpt;
import com.espertech.esper.regressionlib.support.extend.aggfunc.SupportInvocationCountFunction;
import com.espertech.esper.regressionlib.support.patternassert.ResultAssertExecution;
import com.espertech.esper.regressionlib.support.patternassert.ResultAssertTestResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ResultSetOutputLimitRowForAll {
    private final static String CATEGORY = "Fully-Aggregated and Un-grouped";

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ResultSet1NoneNoHavingNoJoin());
        execs.add(new ResultSet2NoneNoHavingJoin());
        execs.add(new ResultSet3NoneHavingNoJoin());
        execs.add(new ResultSet4NoneHavingJoin());
        execs.add(new ResultSet5DefaultNoHavingNoJoin());
        execs.add(new ResultSet6DefaultNoHavingJoin());
        execs.add(new ResultSet7DefaultHavingNoJoin());
        execs.add(new ResultSet8DefaultHavingJoin());
        execs.add(new ResultSet9AllNoHavingNoJoin());
        execs.add(new ResultSet10AllNoHavingJoin());
        execs.add(new ResultSet11AllHavingNoJoin());
        execs.add(new ResultSet12AllHavingJoin());
        execs.add(new ResultSet13LastNoHavingNoJoin());
        execs.add(new ResultSet14LastNoHavingJoin());
        execs.add(new ResultSet15LastHavingNoJoin());
        execs.add(new ResultSet16LastHavingJoin());
        execs.add(new ResultSet17FirstNoHavingNoJoin());
        execs.add(new ResultSet18SnapshotNoHavingNoJoin());
        execs.add(new ResultSetOutputLastWithInsertInto());
        execs.add(new ResultSetAggAllHaving());
        execs.add(new ResultSetAggAllHavingJoin());
        execs.add(new ResultSetJoinSortWindow());
        execs.add(new ResultSetMaxTimeWindow());
        execs.add(new ResultSetTimeWindowOutputCountLast());
        execs.add(new ResultSetTimeBatchOutputCount());
        execs.add(new ResultSetLimitSnapshot());
        execs.add(new ResultSetLimitSnapshotJoin());
        execs.add(new ResultSetOutputSnapshotGetValue());
        return execs;
    }

    private static class ResultSet1NoneNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec)";
            tryAssertion12(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet2NoneNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol";
            tryAssertion12(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet3NoneHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                " having sum(price) > 100";
            tryAssertion34(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet4NoneHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                " having sum(price) > 100";
            tryAssertion34(env, stmtText, "none", new AtomicInteger());
        }
    }

    private static class ResultSet5DefaultNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "output every 1 seconds";
            tryAssertion56(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet6DefaultNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "output every 1 seconds";
            tryAssertion56(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet7DefaultHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) \n" +
                "having sum(price) > 100" +
                "output every 1 seconds";
            tryAssertion78(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet8DefaultHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec), " +
                "SupportBean#keepall where theString=symbol " +
                "having sum(price) > 100" +
                "output every 1 seconds";
            tryAssertion78(env, stmtText, "default", new AtomicInteger());
        }
    }

    private static class ResultSet9AllNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion9AllNoHavingNoJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion9AllNoHavingNoJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec) " +
            "output all every 1 seconds";
        tryAssertion56(env, stmtText, "all", milestone);
    }

    private static class ResultSet10AllNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion10AllNoHavingJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion10AllNoHavingJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec), " +
            "SupportBean#keepall where theString=symbol " +
            "output all every 1 seconds";
        tryAssertion56(env, stmtText, "all", milestone);
    }

    private static class ResultSet11AllHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion11AllHavingNoJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion11AllHavingNoJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec) " +
            "having sum(price) > 100" +
            "output all every 1 seconds";
        tryAssertion78(env, stmtText, "all", milestone);
    }

    private static class ResultSet12AllHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion12AllHavingJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion12AllHavingJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec), " +
            "SupportBean#keepall where theString=symbol " +
            "having sum(price) > 100" +
            "output all every 1 seconds";
        tryAssertion78(env, stmtText, "all", milestone);
    }

    private static class ResultSet13LastNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion13LastNoHavingNoJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion13LastNoHavingNoJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec)" +
            "output last every 1 seconds";
        tryAssertion13_14(env, stmtText, "last", milestone);
    }

    private static class ResultSet14LastNoHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion14LastNoHavingJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion14LastNoHavingJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec), " +
            "SupportBean#keepall where theString=symbol " +
            "output last every 1 seconds";
        tryAssertion13_14(env, stmtText, "last", milestone);
    }

    private static class ResultSet15LastHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion15LastHavingNoJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion15LastHavingNoJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec)" +
            "having sum(price) > 100 " +
            "output last every 1 seconds";
        tryAssertion15_16(env, stmtText, "last", milestone);
    }

    private static class ResultSet16LastHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            AtomicInteger milestone = new AtomicInteger();
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                runAssertion16LastHavingJoin(env, outputLimitOpt, milestone);
            }
        }
    }

    private static void runAssertion16LastHavingJoin(RegressionEnvironment env, SupportOutputLimitOpt opt, AtomicInteger milestone) {
        String stmtText = opt.getHint() + "@name('s0') select sum(price) " +
            "from SupportMarketDataBean#time(5.5 sec), " +
            "SupportBean#keepall where theString=symbol " +
            "having sum(price) > 100 " +
            "output last every 1 seconds";
        tryAssertion15_16(env, stmtText, "last", milestone);
    }

    private static class ResultSet17FirstNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "output first every 1 seconds";
            tryAssertion17(env, stmtText, "first", new AtomicInteger());
        }
    }

    private static class ResultSet18SnapshotNoHavingNoJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(price) " +
                "from SupportMarketDataBean#time(5.5 sec) " +
                "output snapshot every 1 seconds";
            tryAssertion18(env, stmtText, "first", new AtomicInteger());
        }
    }

    private static class ResultSetOutputLastWithInsertInto implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            for (SupportOutputLimitOpt outputLimitOpt : SupportOutputLimitOpt.values()) {
                tryAssertionOuputLastWithInsertInto(env, outputLimitOpt);
            }
        }
    }

    private static class ResultSetAggAllHaving implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(volume) as result " +
                "from SupportMarketDataBean#length(10) as two " +
                "having sum(volume) > 0 " +
                "output every 5 events";
            env.compileDeploy(stmtText).addListener("s0");

            String[] fields = new String[]{"result"};

            sendMDEvent(env, 20);
            sendMDEvent(env, -100);
            sendMDEvent(env, 0);
            sendMDEvent(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMDEvent(env, 0);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{20L}});

            env.undeployAll();
        }
    }

    private static class ResultSetAggAllHavingJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select sum(volume) as result " +
                "from SupportMarketDataBean#length(10) as one," +
                "SupportBean#length(10) as two " +
                "where one.symbol=two.theString " +
                "having sum(volume) > 0 " +
                "output every 5 events";
            env.compileDeploy(stmtText).addListener("s0");

            String[] fields = new String[]{"result"};
            env.sendEventBean(new SupportBean("S0", 0));

            sendMDEvent(env, 20);
            sendMDEvent(env, -100);
            sendMDEvent(env, 0);
            sendMDEvent(env, 0);
            env.assertListenerNotInvoked("s0");

            sendMDEvent(env, 0);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{20L}});

            env.undeployAll();
        }
    }

    private static class ResultSetJoinSortWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);

            String epl = "@name('s0') select irstream max(price) as maxVol" +
                " from SupportMarketDataBean#sort(1,volume desc) as s0, " +
                "SupportBean#keepall as s1 where s1.theString=s0.symbol " +
                "output every 1.0d seconds";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new SupportBean("JOIN_KEY", -1));

            sendEvent(env, "JOIN_KEY", 1d);
            sendEvent(env, "JOIN_KEY", 2d);
            env.listenerReset("s0");

            // moves all events out of the window,
            sendTimer(env, 1000);        // newdata is 2 eventa, old data is the same 2 events, therefore the sum is null
            env.assertListener("s0", listener -> {
                UniformPair<EventBean[]> result = listener.getDataListsFlattened();
                assertEquals(2, result.getFirst().length);
                assertEquals(1.0, result.getFirst()[0].get("maxVol"));
                assertEquals(2.0, result.getFirst()[1].get("maxVol"));
                assertEquals(2, result.getSecond().length);
                assertEquals(null, result.getSecond()[0].get("maxVol"));
                assertEquals(1.0, result.getSecond()[1].get("maxVol"));
            });

            // statement object model test
            EPStatementObjectModel model = env.eplToModel(epl);
            SerializableObjectCopier.copyMayFail(model);
            assertEquals(epl, model.toEPL());

            env.undeployAll();
        }
    }

    private static class ResultSetMaxTimeWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            sendTimer(env, 0);

            String epl = "@name('s0') select irstream max(price) as maxVol" +
                " from SupportMarketDataBean#time(1.1 sec) " +
                "output every 1 seconds";
            env.compileDeploy(epl).addListener("s0");

            sendEvent(env, "SYM1", 1d);
            sendEvent(env, "SYM1", 2d);
            env.listenerReset("s0");

            // moves all events out of the window,
            sendTimer(env, 1000);        // newdata is 2 eventa, old data is the same 2 events, therefore the sum is null
            env.assertListener("s0", listener -> {
                UniformPair<EventBean[]> result = listener.getDataListsFlattened();
                assertEquals(2, result.getFirst().length);
                assertEquals(1.0, result.getFirst()[0].get("maxVol"));
                assertEquals(2.0, result.getFirst()[1].get("maxVol"));
                assertEquals(2, result.getSecond().length);
                assertEquals(null, result.getSecond()[0].get("maxVol"));
                assertEquals(1.0, result.getSecond()[1].get("maxVol"));
            });

            env.undeployAll();
        }
    }

    private static class ResultSetTimeWindowOutputCountLast implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select count(*) as cnt from SupportBean#time(10 seconds) output every 10 seconds";
            env.compileDeploy(stmtText).addListener("s0");

            sendTimer(env, 0);
            sendTimer(env, 10000);
            env.assertListenerNotInvoked("s0");
            sendTimer(env, 20000);
            env.assertListenerNotInvoked("s0");

            sendEvent(env, "e1");
            sendTimer(env, 30000);
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getAndResetLastNewData();
                assertEquals(2, newEvents.length);
                assertEquals(1L, newEvents[0].get("cnt"));
                assertEquals(0L, newEvents[1].get("cnt"));
            });

            sendTimer(env, 31000);

            sendEvent(env, "e2");
            sendEvent(env, "e3");
            sendTimer(env, 40000);
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getAndResetLastNewData();
                assertEquals(2, newEvents.length);
                assertEquals(1L, newEvents[0].get("cnt"));
                assertEquals(2L, newEvents[1].get("cnt"));
            });

            env.undeployAll();
        }
    }

    private static class ResultSetTimeBatchOutputCount implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String stmtText = "@name('s0') select count(*) as cnt from SupportBean#time_batch(10 seconds) output every 10 seconds";
            env.compileDeploy(stmtText).addListener("s0");

            sendTimer(env, 0);
            sendTimer(env, 10000);
            env.assertListenerNotInvoked("s0");
            sendTimer(env, 20000);
            env.assertListenerNotInvoked("s0");

            sendEvent(env, "e1");
            sendTimer(env, 30000);
            env.assertListenerNotInvoked("s0");
            sendTimer(env, 40000);
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getAndResetLastNewData();
                assertEquals(2, newEvents.length);
                // output limiting starts 10 seconds after, therefore the old batch was posted already and the cnt is zero
                assertEquals(1L, newEvents[0].get("cnt"));
                assertEquals(0L, newEvents[1].get("cnt"));
            });

            sendTimer(env, 50000);
            env.assertListener("s0", listener -> {
                EventBean[] newData = listener.getLastNewData();
                assertEquals(0L, newData[0].get("cnt"));
                listener.reset();
            });

            sendEvent(env, "e2");
            sendEvent(env, "e3");
            sendTimer(env, 60000);
            env.assertListener("s0", listener -> {
                EventBean[] newEvents = listener.getAndResetLastNewData();
                assertEquals(1, newEvents.length);
                assertEquals(2L, newEvents[0].get("cnt"));
            });

            env.undeployAll();
        }
    }

    private static class ResultSetLimitSnapshot implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"cnt"};
            sendTimer(env, 0);
            String selectStmt = "@name('s0') select count(*) as cnt from SupportBean#time(10 seconds) where intPrimitive > 0 output snapshot every 1 seconds";
            env.compileDeploy(selectStmt).addListener("s0");

            sendEvent(env, "s0", 1);

            sendTimer(env, 500);
            sendEvent(env, "s1", 1);
            sendEvent(env, "s2", -1);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 1000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{2L}});

            sendTimer(env, 1500);
            sendEvent(env, "s4", 2);
            sendEvent(env, "s5", 3);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 2000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{4L}});

            sendEvent(env, "s5", 4);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 9000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{5L}});

            sendTimer(env, 10000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{4L}});

            sendTimer(env, 10999);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 11000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{3L}});

            env.undeployAll();
        }
    }

    private static class ResultSetLimitSnapshotJoin implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = new String[]{"cnt"};
            sendTimer(env, 0);
            String selectStmt = "@name('s0') select count(*) as cnt from " +
                "SupportBean#time(10 seconds) as s, " +
                "SupportMarketDataBean#keepall as m where m.symbol = s.theString and intPrimitive > 0 output snapshot every 1 seconds";
            env.compileDeploy(selectStmt).addListener("s0");

            env.sendEventBean(new SupportMarketDataBean("s0", 0, 0L, ""));
            env.sendEventBean(new SupportMarketDataBean("s1", 0, 0L, ""));
            env.sendEventBean(new SupportMarketDataBean("s2", 0, 0L, ""));
            env.sendEventBean(new SupportMarketDataBean("s4", 0, 0L, ""));
            env.sendEventBean(new SupportMarketDataBean("s5", 0, 0L, ""));

            sendEvent(env, "s0", 1);

            sendTimer(env, 500);
            sendEvent(env, "s1", 1);
            sendEvent(env, "s2", -1);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 1000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{2L}});

            sendTimer(env, 1500);
            sendEvent(env, "s4", 2);
            sendEvent(env, "s5", 3);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 2000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{4L}});

            sendEvent(env, "s5", 4);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 9000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{5L}});

            // The execution of the join is after the snapshot, as joins are internal dispatch
            sendTimer(env, 10000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{5L}});

            sendTimer(env, 10999);
            env.assertListenerNotInvoked("s0");

            sendTimer(env, 11000);
            env.assertPropsPerRowLastNew("s0", fields, new Object[][]{{3L}});

            env.undeployAll();
        }
    }

    private static class ResultSetOutputSnapshotGetValue implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            tryAssertionOutputSnapshotGetValue(env, true);
            tryAssertionOutputSnapshotGetValue(env, false);
        }
    }

    private static void tryAssertionOutputSnapshotGetValue(RegressionEnvironment env, boolean join) {
        String epl = "@name('s0') select customagg(intPrimitive) as c0 from SupportBean" +
            (join ? "#keepall, SupportBean_S0#lastevent" : "") +
            " output snapshot every 3 events";
        env.compileDeploy(epl).addListener("s0");

        env.sendEventBean(new SupportBean_S0(1));

        SupportInvocationCountFunction.resetGetValueInvocationCount();

        env.sendEventBean(new SupportBean("E1", 10));
        env.sendEventBean(new SupportBean("E2", 20));
        env.assertThat(() -> assertEquals(0, SupportInvocationCountFunction.getGetValueInvocationCount()));

        env.sendEventBean(new SupportBean("E3", 30));
        env.assertEqualsNew("s0", "c0", 60);
        env.assertThat(() -> assertEquals(1, SupportInvocationCountFunction.getGetValueInvocationCount()));

        env.sendEventBean(new SupportBean("E3", 40));
        env.sendEventBean(new SupportBean("E4", 50));
        env.sendEventBean(new SupportBean("E5", 60));
        env.assertEqualsNew("s0", "c0", 210);
        env.assertThat(() -> assertEquals(2, SupportInvocationCountFunction.getGetValueInvocationCount()));

        env.undeployAll();
    }

    private static void tryAssertionOuputLastWithInsertInto(RegressionEnvironment env, SupportOutputLimitOpt opt) {
        String epl = opt.getHint() + "insert into MyStream select sum(intPrimitive) as thesum from SupportBean#keepall " +
            "output last every 2 events;\n" +
            "@name('s0') select * from MyStream;\n";
        env.compileDeploy(epl).addListener("s0");

        env.sendEventBean(new SupportBean("E1", 10));
        env.sendEventBean(new SupportBean("E2", 20));
        env.assertPropsNew("s0", "thesum".split(","), new Object[]{30});

        env.undeployAll();
    }

    private static void tryAssertion12(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(200, 1, new Object[][]{{25d}}, new Object[][]{{null}});
        expected.addResultInsRem(800, 1, new Object[][]{{34d}}, new Object[][]{{25d}});
        expected.addResultInsRem(1500, 1, new Object[][]{{58d}}, new Object[][]{{34d}});
        expected.addResultInsRem(1500, 2, new Object[][]{{59d}}, new Object[][]{{58d}});
        expected.addResultInsRem(2100, 1, new Object[][]{{85d}}, new Object[][]{{59d}});
        expected.addResultInsRem(3500, 1, new Object[][]{{87d}}, new Object[][]{{85d}});
        expected.addResultInsRem(4300, 1, new Object[][]{{109d}}, new Object[][]{{87d}});
        expected.addResultInsRem(4900, 1, new Object[][]{{112d}}, new Object[][]{{109d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{87d}}, new Object[][]{{112d}});
        expected.addResultInsRem(5900, 1, new Object[][]{{88d}}, new Object[][]{{87d}});
        expected.addResultInsRem(6300, 0, new Object[][]{{79d}}, new Object[][]{{88d}});
        expected.addResultInsRem(7000, 0, new Object[][]{{54d}}, new Object[][]{{79d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion34(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(4300, 1, new Object[][]{{109d}}, null);
        expected.addResultInsRem(4900, 1, new Object[][]{{112d}}, new Object[][]{{109d}});
        expected.addResultInsRem(5700, 0, null, new Object[][]{{112d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion13_14(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, new Object[][]{{34d}}, new Object[][]{{null}});
        expected.addResultInsRem(2200, 0, new Object[][]{{85d}}, new Object[][]{{34d}});
        expected.addResultInsRem(3200, 0, new Object[][]{{85d}}, new Object[][]{{85d}});
        expected.addResultInsRem(4200, 0, new Object[][]{{87d}}, new Object[][]{{85d}});
        expected.addResultInsRem(5200, 0, new Object[][]{{112d}}, new Object[][]{{87d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{88d}}, new Object[][]{{112d}});
        expected.addResultInsRem(7200, 0, new Object[][]{{54d}}, new Object[][]{{88d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion15_16(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, null, null);
        expected.addResultInsRem(2200, 0, null, null);
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0, null, null);
        expected.addResultInsRem(5200, 0, new Object[][]{{112d}}, new Object[][]{{109d}});
        expected.addResultInsRem(6200, 0, null, new Object[][]{{112d}});
        expected.addResultInsRem(7200, 0, null, null);

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion78(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, null, null);
        expected.addResultInsRem(2200, 0, null, null);
        expected.addResultInsRem(3200, 0, null, null);
        expected.addResultInsRem(4200, 0, null, null);
        expected.addResultInsRem(5200, 0, new Object[][]{{109d}, {112d}}, new Object[][]{{109d}});
        expected.addResultInsRem(6200, 0, null, new Object[][]{{112d}});
        expected.addResultInsRem(7200, 0, null, null);

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion56(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, new Object[][]{{25d}, {34d}}, new Object[][]{{null}, {25d}});
        expected.addResultInsRem(2200, 0, new Object[][]{{58d}, {59d}, {85d}}, new Object[][]{{34d}, {58d}, {59d}});
        expected.addResultInsRem(3200, 0, new Object[][]{{85d}}, new Object[][]{{85d}});
        expected.addResultInsRem(4200, 0, new Object[][]{{87d}}, new Object[][]{{85d}});
        expected.addResultInsRem(5200, 0, new Object[][]{{109d}, {112d}}, new Object[][]{{87d}, {109d}});
        expected.addResultInsRem(6200, 0, new Object[][]{{87d}, {88d}}, new Object[][]{{112d}, {87d}});
        expected.addResultInsRem(7200, 0, new Object[][]{{79d}, {54d}}, new Object[][]{{88d}, {79d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion17(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(200, 1, new Object[][]{{25d}}, new Object[][]{{null}});
        expected.addResultInsRem(1500, 1, new Object[][]{{58d}}, new Object[][]{{34d}});
        expected.addResultInsRem(3500, 1, new Object[][]{{87d}}, new Object[][]{{85d}});
        expected.addResultInsRem(4300, 1, new Object[][]{{109d}}, new Object[][]{{87d}});
        expected.addResultInsRem(5700, 0, new Object[][]{{87d}}, new Object[][]{{112d}});
        expected.addResultInsRem(6300, 0, new Object[][]{{79d}}, new Object[][]{{88d}});

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void tryAssertion18(RegressionEnvironment env, String stmtText, String outputLimit, AtomicInteger milestone) {
        sendTimer(env, 0);
        env.compileDeploy(stmtText).addListener("s0");

        String[] fields = new String[]{"sum(price)"};
        ResultAssertTestResult expected = new ResultAssertTestResult(CATEGORY, outputLimit, fields);
        expected.addResultInsRem(1200, 0, new Object[][]{{34d}}, null);
        expected.addResultInsRem(2200, 0, new Object[][]{{85d}}, null);
        expected.addResultInsRem(3200, 0, new Object[][]{{85d}}, null);
        expected.addResultInsRem(4200, 0, new Object[][]{{87d}}, null);
        expected.addResultInsRem(5200, 0, new Object[][]{{112d}}, null);
        expected.addResultInsRem(6200, 0, new Object[][]{{88d}}, null);
        expected.addResultInsRem(7200, 0, new Object[][]{{54d}}, null);

        ResultAssertExecution execution = new ResultAssertExecution(stmtText, env, expected);
        execution.execute(false, milestone);
    }

    private static void sendEvent(RegressionEnvironment env, String s) {
        SupportBean bean = new SupportBean();
        bean.setTheString(s);
        bean.setDoubleBoxed(0.0);
        bean.setIntPrimitive(0);
        bean.setIntBoxed(0);
        env.sendEventBean(bean);
    }

    private static void sendEvent(RegressionEnvironment env, String s, int intPrimitive) {
        SupportBean bean = new SupportBean();
        bean.setTheString(s);
        bean.setIntPrimitive(intPrimitive);
        env.sendEventBean(bean);
    }

    private static void sendTimer(RegressionEnvironment env, long time) {
        env.advanceTime(time);
    }

    private static void sendEvent(RegressionEnvironment env, String symbol, double price) {
        SupportMarketDataBean bean = new SupportMarketDataBean(symbol, price, 0L, null);
        env.sendEventBean(bean);
    }

    private static void sendMDEvent(RegressionEnvironment env, long volume) {
        SupportMarketDataBean bean = new SupportMarketDataBean("S0", 0, volume, null);
        env.sendEventBean(bean);
    }
}
