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

import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;



public class EventXMLCreateSchemaInvalid implements RegressionExecution {

    public void run(RegressionEnvironment env) {
        env.tryInvalidCompile("create xml schema ABC()",
            "Required annotation @XMLSchema could not be found");

        env.tryInvalidCompile("@XMLSchema(rootElementName='a') create xml schema ABC(prop string)",
            "Create-XML-Schema does not allow specifying columns, use @XMLSchemaField instead");

        env.tryInvalidCompile("@XMLSchema(rootElementName='') create xml schema ABC()",
            "Required annotation field 'rootElementName' for annotation @XMLSchema could not be found");

        env.tryInvalidCompile("@XMLSchema(rootElementName='abc') create xml schema Base();\n" +
                "@XMLSchema(rootElementName='abc') create xml schema ABC() copyfrom Base;\n",
                "Create-XML-Schema does not allow copy-from");

        env.tryInvalidCompile("@XMLSchema(rootElementName='abc') create xml schema Base();\n" +
                "@XMLSchema(rootElementName='abc') create xml schema ABC() inherits Base;\n",
                "Create-XML-Schema does not allow inherits");

        env.tryInvalidCompile("@XMLSchema(rootElementName='abc') @XMLSchema(rootElementName='def') create xml schema Base()",
                "Found multiple @XMLSchema annotations but expected a single annotation");
    }
}
