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
package com.espertech.esper.regressionlib.suite.expr.enummethod;

import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.common.internal.util.CollectionUtil;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.lrreport.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ExprEnumDocSamples {

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> execs = new ArrayList<>();
        execs.add(new ExprEnumExpressions());
        execs.add(new ExprEnumHowToUse());
        execs.add(new ExprEnumSubquery());
        execs.add(new ExprEnumNamedWindow());
        execs.add(new ExprEnumAccessAggWindow());
        execs.add(new ExprEnumPrevWindow());
        execs.add(new ExprEnumProperties());
        execs.add(new ExprEnumUDFSingleRow());
        execs.add(new ExprEnumScalarArray());
        execs.add(new ExprEnumDeclared());
        return execs;
    }

    private static class ExprEnumHowToUse implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String eplFragment = "@name('s0') select items.where(i => i.location.x = 0 and i.location.y = 0) as zeroloc from LocationReport";
            env.compileDeploy(eplFragment).addListener("s0");

            env.sendEventBean(LocationReportFactory.makeSmall());

            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("zeroloc"));
                assertEquals(1, items.length);
                assertEquals("P00020", items[0].getAssetId());
            });

            env.undeployAll();
            eplFragment = "@name('s0') select items.where(i => i.location.x = 0).where(i => i.location.y = 0) as zeroloc from LocationReport";
            env.compileDeploy(eplFragment).addListener("s0");

            env.sendEventBean(LocationReportFactory.makeSmall());

            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("zeroloc"));
                assertEquals(1, items.length);
                assertEquals("P00020", items[0].getAssetId());
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumSubquery implements RegressionExecution {
        public void run(RegressionEnvironment env) {

            String eplFragment = "@name('s0') select assetId," +
                "  (select * from Zone#keepall).where(z => inrect(z.rectangle, location)) as zones " +
                "from Item";
            env.compileDeploy(eplFragment).addListener("s0");

            env.sendEventBean(new Zone("Z1", new Rectangle(0, 0, 20, 20)));
            env.sendEventBean(new Zone("Z2", new Rectangle(21, 21, 40, 40)));
            env.sendEventBean(new Item("A1", new Location(10, 10)));

            env.assertEventNew("s0", event -> {
                Zone[] zones = toArrayZones((Collection<Zone>) event.get("zones"));
                assertEquals(1, zones.length);
                assertEquals("Z1", zones[0].getName());
            });

            // subquery with event as input
            String epl = "create schema SettlementEvent (symbol string, price double);" +
                "create schema PriceEvent (symbol string, price double);\n" +
                "create schema OrderEvent (orderId string, pricedata PriceEvent);\n" +
                "select (select pricedata from OrderEvent#unique(orderId))\n" +
                ".anyOf(v => v.symbol = 'GE') as has_ge from SettlementEvent(symbol = 'GE')";
            env.compileDeploy(epl);

            // subquery with aggregation
            env.compileDeploy("select (select name, count(*) as cnt from Zone#keepall group by name).where(v => cnt > 1) from LocationReport");

            env.undeployAll();
        }
    }

    private static class ExprEnumNamedWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl;

            RegressionPath path = new RegressionPath();
            env.compileDeploy("@public create window ZoneWindow#keepall as Zone", path);
            env.compileDeploy("insert into ZoneWindow select * from Zone", path);

            epl = "@name('s0') select ZoneWindow.where(z => inrect(z.rectangle, location)) as zones from Item";
            env.compileDeploy(epl, path).addListener("s0");

            env.sendEventBean(new Zone("Z1", new Rectangle(0, 0, 20, 20)));
            env.sendEventBean(new Zone("Z2", new Rectangle(21, 21, 40, 40)));
            env.sendEventBean(new Item("A1", new Location(10, 10)));

            env.assertEventNew("s0", event -> {
                Zone[] zones = toArrayZones((Collection<Zone>) event.get("zones"));
                assertEquals(1, zones.length);
                assertEquals("Z1", zones[0].getName());
            });

            env.undeployModuleContaining("s0");

            epl = "@name('s0') select ZoneWindow(name in ('Z4', 'Z5', 'Z3')).where(z => inrect(z.rectangle, location)) as zones from Item";
            env.compileDeploy(epl, path).addListener("s0");

            env.sendEventBean(new Zone("Z3", new Rectangle(0, 0, 20, 20)));
            env.sendEventBean(new Item("A1", new Location(10, 10)));

            env.assertEventNew("s0", event -> {
                Zone[] zones = toArrayZones((Collection<Zone>) event.get("zones"));
                assertEquals(1, zones.length);
                assertEquals("Z3", zones[0].getName());
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumAccessAggWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select window(*).where(p => distance(0, 0, p.location.x, p.location.y) < 20) as centeritems " +
                "from Item(type='P')#time(10) group by assetId";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new Item("P0001", new Location(10, 10), "P", null));
            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("centeritems"));
                assertEquals(1, items.length);
                assertEquals("P0001", items[0].getAssetId());
            });

            env.sendEventBean(new Item("P0002", new Location(10, 1000), "P", null));
            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("centeritems"));
                assertEquals(0, items.length);
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumPrevWindow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select prevwindow(items).where(p => distance(0, 0, p.location.x, p.location.y) < 20) as centeritems " +
                "from Item(type='P')#time(10) as items";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new Item("P0001", new Location(10, 10), "P", null));
            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("centeritems"));
                assertEquals(1, items.length);
                assertEquals("P0001", items[0].getAssetId());
            });

            env.sendEventBean(new Item("P0002", new Location(10, 1000), "P", null));
            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("centeritems"));
                assertEquals(1, items.length);
                assertEquals("P0001", items[0].getAssetId());
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumProperties implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select items.where(p => distance(0, 0, p.location.x, p.location.y) < 20) as centeritems " +
                "from LocationReport";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(LocationReportFactory.makeSmall());
            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("centeritems"));
                assertEquals(1, items.length);
                assertEquals("P00020", items[0].getAssetId());
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumUDFSingleRow implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select ZoneFactory.getZones().where(z => inrect(z.rectangle, item.location)) as zones\n" +
                "from Item as item";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(new Item("A1", new Location(5, 5)));
            env.assertEventNew("s0", event -> {
                Zone[] zones = toArrayZones((Collection<Zone>) event.get("zones"));
                assertEquals(1, zones.length);
                assertEquals("Z1", zones[0].getName());
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumDeclared implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') expression passengers {\n" +
                "  lr => lr.items.where(l => l.type='P')\n" +
                "}\n" +
                "select passengers(lr) as p," +
                "passengers(lr).where(x => assetId = 'P01') as p2 from LocationReport lr";
            env.compileDeploy(epl).addListener("s0");

            env.sendEventBean(LocationReportFactory.makeSmall());
            env.assertEventNew("s0", event -> {
                Item[] items = toArrayItems((Collection<Item>) event.get("p"));
                assertEquals(2, items.length);
                assertEquals("P00002", items[0].getAssetId());
                assertEquals("P00020", items[1].getAssetId());
            });

            env.undeployAll();
        }
    }

    private static class ExprEnumExpressions implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            RegressionPath path = new RegressionPath();
            assertStmt(env, path, "select items.firstof().assetId as firstcenter from LocationReport");
            assertStmt(env, path, "select items.where(p => p.type=\"P\") from LocationReport");
            assertStmt(env, path, "select items.where((p,ind) => p.type=\"P\" and ind>2) from LocationReport");
            assertStmt(env, path, "select items.aggregate(\"\",(result,item) => result||(case when result=\"\" then \"\" else \",\" end)||item.assetId) as assets from LocationReport");
            assertStmt(env, path, "select items.allof(i => distance(i.location.x,i.location.y,0,0)<1000) as assets from LocationReport");
            assertStmt(env, path, "select items.average(i => distance(i.location.x,i.location.y,0,0)) as avgdistance from LocationReport");
            assertStmt(env, path, "select items.countof(i => distance(i.location.x,i.location.y,0,0)<20) as cntcenter from LocationReport");
            assertStmt(env, path, "select items.firstof(i => distance(i.location.x,i.location.y,0,0)<20) as firstcenter from LocationReport");
            assertStmt(env, path, "select items.lastof().assetId as firstcenter from LocationReport");
            assertStmt(env, path, "select items.lastof(i => distance(i.location.x,i.location.y,0,0)<20) as lastcenter from LocationReport");
            assertStmt(env, path, "select items.where(i => i.type=\"L\").groupby(i => assetIdPassenger) as luggagePerPerson from LocationReport");
            assertStmt(env, path, "select items.where((p,ind) => p.type=\"P\" and ind>2) from LocationReport");
            assertStmt(env, path, "select items.groupby(k => assetId,v => distance(v.location.x,v.location.y,0,0)) as distancePerItem from LocationReport");
            assertStmt(env, path, "select items.min(i => distance(i.location.x,i.location.y,0,0)) as mincenter from LocationReport");
            assertStmt(env, path, "select items.max(i => distance(i.location.x,i.location.y,0,0)) as maxcenter from LocationReport");
            assertStmt(env, path, "select items.minBy(i => distance(i.location.x,i.location.y,0,0)) as minItemCenter from LocationReport");
            assertStmt(env, path, "select items.minBy(i => distance(i.location.x,i.location.y,0,0)).assetId as minItemCenter from LocationReport");
            assertStmt(env, path, "select items.orderBy(i => distance(i.location.x,i.location.y,0,0)) as itemsOrderedByDist from LocationReport");
            assertStmt(env, path, "select items.selectFrom(i => assetId) as itemAssetIds from LocationReport");
            assertStmt(env, path, "select items.take(5) as first5Items, items.takeLast(5) as last5Items from LocationReport");
            assertStmt(env, path, "select items.toMap(k => k.assetId,v => distance(v.location.x,v.location.y,0,0)) as assetDistance from LocationReport");
            assertStmt(env, path, "select items.where(i => i.assetId=\"L001\").union(items.where(i => i.type=\"P\")) as itemsUnion from LocationReport");
            assertStmt(env, path, "select (select name from Zone#unique(name)).orderBy() as orderedZones from pattern [every timer:interval(30)]");

            env.compileDeploy("@buseventtype @public create schema MyEvent as (seqone String[], seqtwo String[])", path);

            assertStmt(env, path, "select seqone.sequenceEqual(seqtwo) from MyEvent");
            assertStmt(env, path, "select window(assetId).orderBy() as orderedAssetIds from Item#time(10) group by assetId");
            assertStmt(env, path, "select prevwindow(assetId).orderBy() as orderedAssetIds from Item#time(10) as items");
            assertStmt(env, path, "select getZoneNames().where(z => z!=\"Z1\") from pattern [every timer:interval(30)]");
            assertStmt(env, path, "select items.selectFrom(i => new{assetId,distanceCenter=distance(i.location.x,i.location.y,0,0)}) as itemInfo from LocationReport");
            assertStmt(env, path, "select items.leastFrequent(i => type) as leastFreqType from LocationReport");

            String epl = "expression myquery {itm => " +
                "(select * from Zone#keepall).where(z => inrect(z.rectangle,itm.location))" +
                "} " +
                "select assetId, myquery(item) as subq, myquery(item).where(z => z.name=\"Z01\") as assetItem " +
                "from Item as item";
            assertStmt(env, path, epl);

            assertStmt(env, path, "select za.items.except(zb.items) as itemsCompared from LocationReport as za unidirectional, LocationReport#length(10) as zb");

            env.undeployAll();
        }
    }

    private static class ExprEnumScalarArray implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            validate(env, "{1, 2, 3}.aggregate(0, (result, value) => result + value)", 6);
            validate(env, "{1, 2, 3}.aggregate(0, (result, value, index) => result + value + index*10)", 36);
            validate(env, "{1, 2, 3}.aggregate(0, (result, value, index, size) => result + value + index*10 + size*100)", 936);
            validate(env, "{1, 2, 3}.allOf(v => v > 0)", true);
            validate(env, "{1, 2, 3}.allOf(v => v > 1)", false);
            validate(env, "{1, 2, 3}.allOf((v, index) => case when index < 2 then true else v > 1 end)", true);
            validate(env, "{1, 2, 3}.allOf((v, index, size) => v > 1 or size >= 3)", true);
            validate(env, "{1, 2, 3}.anyOf(v => v > 1)", true);
            validate(env, "{1, 2, 3}.anyOf(v => v > 3)", false);
            validate(env, "{1, 2, 3}.anyOf( (v, index) => case when index < 2 then false else v = 3 end)", true);
            validate(env, "{1, 2, 3}.anyOf( (v, index, size) => v > 100 or size >= 3)", true);
            validate(env, "{1, 2, 3}.average()", 2.0);
            validate(env, "{1, 2, 3}.average(v => v+1)", 3d);
            validate(env, "{1, 2, 3}.average((v, index) => v+10*index)", 12d);
            validate(env, "{1, 2, 3}.average((v, index, size) => v+10*index + 100*size)", 312d);
            validate(env, "{1, 2, 3}.countOf()", 3);
            validate(env, "{1, 2, 3}.countOf(v => v < 2)", 1);
            validate(env, "{1, 2, 3}.countOf( (v, index) => v > index)", 3);
            validate(env, "{1, 2, 3}.countOf( (v, index, size) => v >= size)", 1);
            validate(env, "{1, 2, 3}.except({1})", new Object[]{2, 3});
            validate(env, "{1, 2, 3}.intersect({2,3})", new Object[]{2, 3});
            validate(env, "{1, 2, 3}.firstOf()", 1);
            validate(env, "{1, 2, 3}.firstOf(v => v / 2 = 1)", 2);
            validate(env, "{1, 2, 3}.firstOf((v, index) => index = 1)", 2);
            validate(env, "{1, 2, 3}.firstOf((v, index, size) => v = size-1)", 2);
            validate(env, "{1, 2, 3}.intersect({2, 3})", new Object[]{2, 3});
            validate(env, "{1, 2, 3}.lastOf()", 3);
            validate(env, "{1, 2, 3}.lastOf(v => v < 3)", 2);
            validate(env, "{1, 2, 3}.lastOf((v, index) => index < 2 )", 2);
            validate(env, "{1, 2, 3}.lastOf((v, index, size) => index < size - 2 )", 1);
            validate(env, "{1, 2, 3, 2, 1}.leastFrequent()", 3);
            validate(env, "{1, 2, 3, 2, 1}.leastFrequent(v => case when v = 3 then 4 else v end)", 4);
            validate(env, "{1, 2, 3, 2, 1}.leastFrequent((v, index) => case when index = 2 then 4 else v end)", 4);
            validate(env, "{1, 2, 3, 2, 1}.leastFrequent((v, index, size) => case when index = size - 2 then 4 else v end)", 2);
            validate(env, "{1, 2, 3, 2, 1}.max()", 3);
            validate(env, "{1, 2, 3, 2, 1}.max(v => case when v >= 3 then 0 else v end)", 2);
            validate(env, "{1, 2, 3, 2, 1}.max((v, index) => case when index = 2 then 0 else v end)", 2);
            validate(env, "{1, 2, 3, 2, 1}.max((v, index, size) => case when index > size - 4 then 0 else v end)", 2);
            validate(env, "{1, 2, 3, 2, 1}.min()", 1);
            validate(env, "{1, 2, 3, 2, 1}.min(v => v + 1)", 2);
            validate(env, "{1, 2, 3, 2, 1}.min((v, index) => v - index)", -3);
            validate(env, "{1, 2, 3, 2, 1}.min((v, index, size) => v - size)", -4);
            validate(env, "{1, 2, 3, 2, 1, 2}.mostFrequent()", 2);
            validate(env, "{1, 2, 3, 2, 1, 2}.mostFrequent(v => case when v = 2 then 10 else v end)", 10);
            validate(env, "{1, 2, 3, 2, 1, 2}.mostFrequent((v, index) => case when index > 2 then 4 else v end)", 4);
            validate(env, "{1, 2, 3, 2, 1, 2}.mostFrequent((v, index, size) => case when size > 3 then 0 else v end)", 0);
            validate(env, "{2, 3, 2, 1}.orderBy()", new Object[]{1, 2, 2, 3});
            validate(env, "{2, 3, 2, 1}.orderBy(v => -v)", new Object[]{3, 2, 2, 1});
            validate(env, "{2, 3, 2, 1}.orderBy((v, index) => index)", new Object[]{2, 3, 2, 1});
            validate(env, "{2, 3, 2, 1}.orderBy((v, index, size) => case when index < size - 2 then v else -v end)", new Object[]{2, 1, 2, 3});
            validate(env, "{2, 3, 2, 1}.distinctOf()", new Object[]{2, 3, 1});
            validate(env, "{2, 3, 2, 1}.distinctOf(v => case when v > 1 then 0 else -1 end)", new Object[]{2, 1});
            validate(env, "{2, 3, 2, 1}.distinctOf((v, index) => case when index = 0 then 1 else 2 end)", new Object[]{2, 3});
            validate(env, "{2, 3, 2, 1}.distinctOf((v, index, size) => case when index+1=size then 1 else 2 end)", new Object[]{2, 1});
            validate(env, "{2, 3, 2, 1}.reverse()", new Object[]{1, 2, 3, 2});
            validate(env, "{1, 2, 3}.sequenceEqual({1})", false);
            validate(env, "{1, 2, 3}.sequenceEqual({1, 2, 3})", true);
            validate(env, "{1, 2, 3}.sumOf()", 6);
            validate(env, "{1, 2, 3}.sumOf(v => v+1)", 9);
            validate(env, "{1, 2, 3}.sumOf((v, index) => v + index)", 1 + 3 + 5);
            validate(env, "{1, 2, 3}.sumOf((v, index, size) => v+index+size)", 18);
            validate(env, "{1, 2, 3}.take(2)", new Object[]{1, 2});
            validate(env, "{1, 2, 3}.takeLast(2)", new Object[]{2, 3});
            validate(env, "{1, 2, 3}.takeWhile(v => v < 3)", new Object[]{1, 2});
            validate(env, "{1, 2, 3}.takeWhile((v,ind) => ind < 2)", new Object[]{1, 2});
            validate(env, "{1, 2, -1, 4, 5, 6}.takeWhile((v,ind) => ind < 5 and v > 0)", new Object[]{1, 2});
            validate(env, "{1, 2, -1, 4, 5, 6}.takeWhile((v,ind,sz) => ind < sz - 5 and v > 0)", new Object[]{1});
            validate(env, "{1, 2, 3}.takeWhileLast(v => v > 1)", new Object[]{2, 3});
            validate(env, "{1, 2, 3}.takeWhileLast((v,ind) => ind < 2)", new Object[]{2, 3});
            validate(env, "{1, 2, -1, 4, 5, 6}.takeWhileLast((v,ind) => ind < 5 and v > 0)", new Object[]{4, 5, 6});
            validate(env, "{1, 2, -1, 4, 5, 6}.takeWhileLast((v,ind,sz) => ind < sz-4 and v > 0)", new Object[]{5, 6});
            validate(env, "{1, 2, 3}.union({4, 5})", new Object[]{1, 2, 3, 4, 5});
            validate(env, "{1, 2, 3}.where(v => v != 2)", new Object[]{1, 3});
            validate(env, "{1, 2, 3}.where((v, index) => v != 2 and index < 2)", new Object[]{1});
            validate(env, "{1, 2, 3}.where((v, index, size) => v != 2 and index < size - 2)", new Object[]{1});
            validate(env, "{1, 2, 3}.groupby(k => 'K' || Integer.toString(k))", CollectionUtil.buildMap("K1", singletonList(1), "K2", singletonList(2), "K3", singletonList(3)));
            validate(env, "{1, 2, 3}.groupby(k => 'K' || Integer.toString(k), v => 'V' || Integer.toString(v))", CollectionUtil.buildMap("K1", singletonList("V1"), "K2", singletonList("V2"), "K3", singletonList("V3")));
            validate(env, "{1, 2, 3}.groupby((k, i) => 'K' || Integer.toString(k) || \"_\" || Integer.toString(i), (v, i) => 'V' || Integer.toString(v) || \"_\" || Integer.toString(i))", CollectionUtil.buildMap("K1_0", singletonList("V1_0"), "K2_1", singletonList("V2_1"), "K3_2", singletonList("V3_2")));
            validate(env, "{1, 2, 3}.groupby((k, i, s) => 'K' || Integer.toString(k) || \"_\" || Integer.toString(s), (v, i, s) => 'V' || Integer.toString(v) || \"_\" || Integer.toString(s))", CollectionUtil.buildMap("K1_3", singletonList("V1_3"), "K2_3", singletonList("V2_3"), "K3_3", singletonList("V3_3")));
            validate(env, "{1, 2, 3, 2, 1}.maxby(v => v)", 3);
            validate(env, "{1, 2, 3, 2, 1}.maxby((v, index) => case when index < 3 then -1 else 0 end)", 2);
            validate(env, "{1, 2, 3, 2, 1}.maxby((v, index, size) => case when index < size - 2 then -1 else 0 end)", 2);
            validate(env, "{1, 2, 3, 2, 1}.minby(v => v)", 1);
            validate(env, "{1, 2, 3, 2, 1}.minby((v, index) => case when index < 3 then -1 else 0 end)", 1);
            validate(env, "{1, 2, 3, 2, 1}.minby((v, index, size) => case when index < size - 2 then -1 else 0 end)", 1);
            validate(env, "{'A','B','C'}.selectFrom(v => '<' || v || '>')", Arrays.asList("<A>", "<B>", "<C>"));
            validate(env, "{'A','B','C'}.selectFrom((v, index) => v || '_' || Integer.toString(index))", Arrays.asList("A_0", "B_1", "C_2"));
            validate(env, "{'A','B','C'}.selectFrom((v, index, size) => v || '_' || Integer.toString(size))", Arrays.asList("A_3", "B_3", "C_3"));
            validateWithVerifier(env, "{1, 2, 3}.arrayOf()", result -> EPAssertionUtil.assertEqualsExactOrder((Object[]) result, new Integer[]{1, 2, 3}));
            validateWithVerifier(env, "{1, 2, 3}.arrayOf(v => v+1)", result -> EPAssertionUtil.assertEqualsExactOrder((Object[]) result, new Integer[]{2, 3, 4}));
            validateWithVerifier(env, "{1, 2, 3}.arrayOf((v, index) => v+index)", result -> EPAssertionUtil.assertEqualsExactOrder((Object[]) result, new Integer[]{1, 3, 5}));
            validateWithVerifier(env, "{1, 2, 3}.arrayOf((v, index, size) => v+index+size)", result -> EPAssertionUtil.assertEqualsExactOrder((Object[]) result, new Integer[]{4, 6, 8}));
            validate(env, "{1, 2, 3}.toMap(k => 'K' || Integer.toString(k), v => 'V' || Integer.toString(v))", CollectionUtil.buildMap("K1", "V1", "K2", "V2", "K3", "V3"));
            validate(env, "{1, 2, 3}.toMap((k, i) => 'K' || Integer.toString(k) || \"_\" || Integer.toString(i), (v, i) => 'V' || Integer.toString(v) || \"_\" || Integer.toString(i))", CollectionUtil.buildMap("K1_0", "V1_0", "K2_1", "V2_1", "K3_2", "V3_2"));
            validate(env, "{1, 2, 3}.toMap((k, i, s) => 'K' || Integer.toString(k) || \"_\" || Integer.toString(s), (v, i, s) => 'V' || Integer.toString(v) || \"_\" || Integer.toString(s))", CollectionUtil.buildMap("K1_3", "V1_3", "K2_3", "V2_3", "K3_3", "V3_3"));
        }
    }

    private static void validate(RegressionEnvironment env, String select, Object expected) {
        if (expected instanceof Object[]) {
            validateWithVerifier(env, select, result -> {
                Object[] returned = ((Collection) result).toArray();
                EPAssertionUtil.assertEqualsExactOrder((Object[]) expected, returned);
            });
        } else if (expected instanceof Collection) {
            validateWithVerifier(env, select, result -> {
                Object[] returned = ((Collection) result).toArray();
                EPAssertionUtil.assertEqualsExactOrder(((Collection) expected).toArray(), returned);
            });
        } else {
            validateWithVerifier(env, select, result -> {
                assertEquals(expected, result);
            });
        }
    }

    private static void validateWithVerifier(RegressionEnvironment env, String select, Consumer<Object> verifier) {
        String epl = "@name('s0') select " + select + " as result from SupportBean";
        env.compileDeploy(epl).addListener("s0");

        env.sendEventBean(new SupportBean("E1", 0));
        env.assertEventNew("s0", event -> {
            Object result = event.get("result");
            verifier.accept(result);
        });

        env.undeployAll();
    }

    private static void assertStmt(RegressionEnvironment env, RegressionPath path, String epl) {
        env.compileDeploy("@name('s0')" + epl, path).undeployModuleContaining("s0");
        env.eplToModelCompileDeploy("@name('s0') " + epl, path).undeployModuleContaining("s0");
    }

    private static Zone[] toArrayZones(Collection<Zone> it) {
        return it.toArray(new Zone[it.size()]);
    }

    private static Item[] toArrayItems(Collection<Item> it) {
        return it.toArray(new Item[it.size()]);
    }
}
