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
import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.client.soda.*;
import com.espertech.esper.common.internal.avro.support.SupportAvroUtil;
import com.espertech.esper.common.internal.support.EventRepresentationChoice;
import com.espertech.esper.common.internal.support.SupportBean;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.support.bean.SupportBeanComplexProps;
import com.espertech.esper.regressionlib.support.expreval.SupportEvalBuilder;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ExprCoreArray {

    // for use in testing a static method accepting array parameters
    private static Integer[] callbackInts;
    private static String[] callbackStrings;
    private static Object[] callbackObjects;

    public static Collection<RegressionExecution> executions() {
        ArrayList<RegressionExecution> executions = new ArrayList<>();
        executions.add(new ExprCoreArraySimple());
        executions.add(new ExprCoreArrayMapResult());
        executions.add(new ExprCoreArrayCompile());
        executions.add(new ExprCoreArrayExpressionsOM());
        executions.add(new ExprCoreArrayComplexTypes());
        executions.add(new ExprCoreArrayAvroArray());
        return executions;
    }

    private static class ExprCoreArraySimple implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0".split(",");
            SupportEvalBuilder builder = new SupportEvalBuilder("SupportBean")
                .expressions(fields, "{1, 2}");
            builder.assertion(new SupportBean()).verify("c0", value -> {
                assertEquals(Integer[].class, value.getClass());
                EPAssertionUtil.assertEqualsExactOrder(new Object[]{1, 2}, (Integer[]) value);
            });

            builder.run(env);
            env.undeployAll();
        }
    }

    private static class ExprCoreArrayMapResult implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select {'a', 'b'} as stringArray," +
                "{} as emptyArray," +
                "{1} as oneEleArray," +
                "{1,2,3} as intArray," +
                "{1,null} as intNullArray," +
                "{1L,10L} as longArray," +
                "{'a',1, 1e20} as mixedArray," +
                "{1, 1.1d, 1e20} as doubleArray," +
                "{5, 6L} as intLongArray," +
                "{null} as nullArray," +
                ExprCoreArray.class.getName() + ".doIt({'a'}, {1}, {1, 'd', null, true}) as func," +
                "{true, false} as boolArray," +
                "{intPrimitive} as dynIntArr," +
                "{intPrimitive, longPrimitive} as dynLongArr," +
                "{intPrimitive, theString} as dynMixedArr," +
                "{intPrimitive, intPrimitive * 2, intPrimitive * 3} as dynCalcArr," +
                "{longBoxed, doubleBoxed * 2, theString || 'a'} as dynCalcArrNulls" +
                " from " + SupportBean.class.getSimpleName();
            env.compileDeploy(epl).addListener("s0");

            SupportBean bean = new SupportBean("a", 10);
            bean.setLongPrimitive(999);
            env.sendEventBean(bean);

            env.assertEventNew("s0", event -> {
                EPAssertionUtil.assertEqualsExactOrder((String[]) event.get("stringArray"), new String[]{"a", "b"});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("emptyArray"), new Object[0]);
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("oneEleArray"), new Integer[]{1});
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("intArray"), new Integer[]{1, 2, 3});
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("intNullArray"), new Integer[]{1, null});
                EPAssertionUtil.assertEqualsExactOrder((Long[]) event.get("longArray"), new Long[]{1L, 10L});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("mixedArray"), new Object[]{"a", 1, 1e20});
                EPAssertionUtil.assertEqualsExactOrder((Double[]) event.get("doubleArray"), new Double[]{1d, 1.1, 1e20});
                EPAssertionUtil.assertEqualsExactOrder((Long[]) event.get("intLongArray"), new Long[]{5L, 6L});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("nullArray"), new Object[]{null});
                EPAssertionUtil.assertEqualsExactOrder((String[]) event.get("func"), new String[]{"a", "b"});
                EPAssertionUtil.assertEqualsExactOrder((Boolean[]) event.get("boolArray"), new Boolean[]{true, false});
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("dynIntArr"), new Integer[]{10});
                EPAssertionUtil.assertEqualsExactOrder((Long[]) event.get("dynLongArr"), new Long[]{10L, 999L});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("dynMixedArr"), new Object[]{10, "a"});
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("dynCalcArr"), new Integer[]{10, 20, 30});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("dynCalcArrNulls"), new Object[]{null, null, "aa"});
            });

            // assert function parameters
            env.assertThat(() -> {
                EPAssertionUtil.assertEqualsExactOrder(callbackInts, new Integer[]{1});
                EPAssertionUtil.assertEqualsExactOrder(callbackStrings, new String[]{"a"});
                EPAssertionUtil.assertEqualsExactOrder(callbackObjects, new Object[]{1, "d", null, true});
            });

            env.undeployAll();
        }
    }

    private static class ExprCoreArrayCompile implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select {\"a\",\"b\"} as stringArray, " +
                "{} as emptyArray, " +
                "{1} as oneEleArray, " +
                "{1,2,3} as intArray " +
                "from SupportBean";
            env.eplToModelCompileDeploy(epl).addListener("s0").milestone(0);

            SupportBean bean = new SupportBean("a", 10);
            env.sendEventBean(bean);

            env.assertEventNew("s0", event -> {
                EPAssertionUtil.assertEqualsExactOrder((String[]) event.get("stringArray"), new String[]{"a", "b"});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("emptyArray"), new Object[0]);
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("oneEleArray"), new Integer[]{1});
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("intArray"), new Integer[]{1, 2, 3});
            });

            env.undeployAll();
        }
    }

    private static class ExprCoreArrayComplexTypes implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String[] fields = "c0".split(",");
            SupportEvalBuilder builder = new SupportEvalBuilder("SupportBeanComplexProps")
                .expressions(fields, "{arrayProperty, nested}");

            SupportBeanComplexProps bean = SupportBeanComplexProps.makeDefaultBean();
            builder.assertion(bean).verify("c0", result -> {
                Object[] arr = (Object[]) result;
                assertSame(bean.getArrayProperty(), arr[0]);
                assertSame(bean.getNested(), arr[1]);
            });

            builder.run(env);
            env.undeployAll();
        }
    }

    private static class ExprCoreArrayExpressionsOM implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String epl = "@name('s0') select {\"a\",\"b\"} as stringArray, " +
                "{} as emptyArray, " +
                "{1} as oneEleArray, " +
                "{1,2,3} as intArray " +
                "from " + SupportBean.class.getSimpleName();
            EPStatementObjectModel model = new EPStatementObjectModel();
            model.setAnnotations(Collections.singletonList(AnnotationPart.nameAnnotation("s0")));
            model.setSelectClause(SelectClause.create()
                .add(Expressions.array().add(Expressions.constant("a")).add(Expressions.constant("b")), "stringArray")
                .add(Expressions.array(), "emptyArray")
                .add(Expressions.array().add(Expressions.constant(1)), "oneEleArray")
                .add(Expressions.array().add(Expressions.constant(1)).add(2).add(3), "intArray")
            );
            model.setFromClause(FromClause.create(FilterStream.create(SupportBean.class.getSimpleName())));
            assertEquals(epl, model.toEPL());
            env.compileDeploy(model).addListener("s0");

            SupportBean bean = new SupportBean("a", 10);
            env.sendEventBean(bean);

            env.assertEventNew("s0", event -> {
                EPAssertionUtil.assertEqualsExactOrder((String[]) event.get("stringArray"), new String[]{"a", "b"});
                EPAssertionUtil.assertEqualsExactOrder((Object[]) event.get("emptyArray"), new Object[0]);
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("oneEleArray"), new Integer[]{1});
                EPAssertionUtil.assertEqualsExactOrder((Integer[]) event.get("intArray"), new Integer[]{1, 2, 3});
            });

            env.undeployAll();
        }
    }

    private static class ExprCoreArrayAvroArray implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            Schema intArraySchema = SchemaBuilder.array().items(SchemaBuilder.builder().intType());
            Schema mixedArraySchema = SchemaBuilder.array().items(SchemaBuilder.unionOf().intType().and().stringType().and().doubleType().endUnion());
            Schema nullArraySchema = SchemaBuilder.array().items(SchemaBuilder.builder().nullType());

            String stmtText =
                "@name('s0') @AvroSchemaField(name='emptyArray', schema='" + intArraySchema.toString() + "')" +
                    "@AvroSchemaField(name='mixedArray', schema='" + mixedArraySchema.toString() + "')" +
                    "@AvroSchemaField(name='nullArray', schema='" + nullArraySchema.toString() + "')" +
                    EventRepresentationChoice.AVRO.getAnnotationText() +
                    "select {'a', 'b'} as stringArray," +
                    "{} as emptyArray," +
                    "{1} as oneEleArray," +
                    "{1,2,3} as intArray," +
                    "{1,null} as intNullArray," +
                    "{1L,10L} as longArray," +
                    "{'a',1, 1e20} as mixedArray," +
                    "{1, 1.1d, 1e20} as doubleArray," +
                    "{5, 6L} as intLongArray," +
                    "{null} as nullArray," +
                    "{true, false} as boolArray" +
                    " from SupportBean";
            env.compileDeploy(stmtText).addListener("s0");

            env.sendEventBean(new SupportBean());

            env.assertEventNew("s0", event -> {
                SupportAvroUtil.avroToJson(event);

                compareColl(event, "stringArray", new String[]{"a", "b"});
                compareColl(event, "emptyArray", new Object[0]);
                compareColl(event, "oneEleArray", new Integer[]{1});
                compareColl(event, "intArray", new Integer[]{1, 2, 3});
                compareColl(event, "intNullArray", new Integer[]{1, null});
                compareColl(event, "longArray", new Long[]{1L, 10L});
                compareColl(event, "mixedArray", new Object[]{"a", 1, 1e20});
                compareColl(event, "doubleArray", new Double[]{1d, 1.1, 1e20});
                compareColl(event, "intLongArray", new Long[]{5L, 6L});
                compareColl(event, "nullArray", new Object[]{null});
                compareColl(event, "boolArray", new Boolean[]{true, false});
            });

            env.undeployAll();
        }
    }

    // for testing EPL static method call
    private static void compareColl(EventBean event, String property, Object[] expected) {
        Collection col = (Collection) event.get(property);
        EPAssertionUtil.assertEqualsExactOrder(col.toArray(), expected);
    }

    public static String[] doIt(String[] strings, Integer[] ints, Object[] objects) {
        callbackInts = ints;
        callbackStrings = strings;
        callbackObjects = objects;
        return new String[]{"a", "b"};
    }
}
