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
package com.espertech.esper.regressionlib.suite.event.xml;

import com.espertech.esper.common.client.scopetest.EPAssertionUtil;
import com.espertech.esper.common.internal.support.SupportEventTypeAssertionUtil;
import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;
import com.espertech.esper.regressionlib.support.util.SupportXML;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;



public class EventXMLSchemaEventObservationDOM {
    protected final static String OBSERVATION_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<Sensor xmlns=\"SensorSchema\" >\n" +
        "\t<ID>urn:epc:1:4.16.36</ID>\n" +
        "\t<Observation Command=\"READ_PALLET_TAGS_ONLY\">\n" +
        "\t\t<ID>00000001</ID>\n" +
        "\t\t<Tag>\n" +
        "\t\t\t<ID>urn:epc:1:2.24.400</ID>\n" +
        "\t\t</Tag>\n" +
        "\t\t<Tag>\n" +
        "\t\t\t<ID>urn:epc:1:2.24.401</ID>\n" +
        "\t\t</Tag>\n" +
        "\t</Observation>\n" +
        "</Sensor>";

    public static List<RegressionExecution> executions() {
        List<RegressionExecution> execs = new ArrayList<>();
        execs.add(new EventXMLSchemaEventObservationDOMPreconfig());
        execs.add(new EventXMLSchemaEventObservationDOMCreateSchema());
        return execs;
    }

    public static class EventXMLSchemaEventObservationDOMPreconfig implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            runAssertion(env, "SensorEvent", new RegressionPath());
        }
    }

    public static class EventXMLSchemaEventObservationDOMCreateSchema implements RegressionExecution {
        public void run(RegressionEnvironment env) {
            String schemaUriSensorEvent = this.getClass().getClassLoader().getResource("regression/sensorSchema.xsd").toString();
            String epl = "@public @buseventtype " +
                "@XMLSchema(rootElementName='Sensor', schemaResource='" + schemaUriSensorEvent + "')" +
                "create xml schema MyEventCreateSchema()";
            RegressionPath path = new RegressionPath();
            env.compileDeploy(epl, path);
            runAssertion(env, "MyEventCreateSchema", path);
        }
    }

    private static void runAssertion(RegressionEnvironment env, String eventTypeName, RegressionPath path) {
        String stmtExampleOneText = "@name('s0') select ID, Observation.Command, Observation.ID,\n" +
            "Observation.Tag[0].ID, Observation.Tag[1].ID\n" +
            "from " + eventTypeName;
        env.compileDeploy(stmtExampleOneText, path).addListener("s0");

        env.compileDeploy("@name('e2_0') @public insert into ObservationStream\n" +
            "select ID, Observation from " + eventTypeName, path);
        env.compileDeploy("@name('e2_1') select Observation.Command, Observation.Tag[0].ID from ObservationStream", path);

        env.compileDeploy("@name('e3_0') @public insert into TagListStream\n" +
            "select ID as sensorId, Observation.* from " + eventTypeName, path);
        env.compileDeploy("@name('e3_1') select sensorId, Command, Tag[0].ID from TagListStream", path);

        Document doc = SupportXML.getDocument(OBSERVATION_XML);
        env.sendEventXMLDOM(doc, eventTypeName);

        env.assertIterator("s0", iterator -> SupportEventTypeAssertionUtil.assertConsistency(iterator.next()));
        env.assertIterator("e2_0", iterator -> SupportEventTypeAssertionUtil.assertConsistency(iterator.next()));
        env.assertIterator("e2_1", iterator -> SupportEventTypeAssertionUtil.assertConsistency(iterator.next()));
        env.assertIterator("e3_0", iterator -> SupportEventTypeAssertionUtil.assertConsistency(iterator.next()));
        env.assertIterator("e3_1", iterator -> SupportEventTypeAssertionUtil.assertConsistency(iterator.next()));

        env.assertIterator("e2_0", iterator -> EPAssertionUtil.assertProps(iterator.next(), "Observation.Command,Observation.Tag[0].ID".split(","), new Object[]{"READ_PALLET_TAGS_ONLY", "urn:epc:1:2.24.400"}));
        env.assertIterator("e3_0", iterator -> EPAssertionUtil.assertProps(iterator.next(), "sensorId,Command,Tag[0].ID".split(","), new Object[]{"urn:epc:1:4.16.36", "READ_PALLET_TAGS_ONLY", "urn:epc:1:2.24.400"}));

        env.tryInvalidCompile(path, "select Observation.Tag.ID from " + eventTypeName,
            "Failed to validate select-clause expression 'Observation.Tag.ID': Failed to resolve property 'Observation.Tag.ID' to a stream or nested property in a stream");

        env.undeployAll();
    }
}
