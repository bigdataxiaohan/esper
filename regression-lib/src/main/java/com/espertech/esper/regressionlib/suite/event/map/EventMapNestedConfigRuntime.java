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
package com.espertech.esper.regressionlib.suite.event.map;

import com.espertech.esper.regressionlib.framework.RegressionEnvironment;
import com.espertech.esper.regressionlib.framework.RegressionExecution;
import com.espertech.esper.regressionlib.framework.RegressionPath;

import static com.espertech.esper.regressionlib.suite.event.map.EventMapNestedConfigStatic.runAssertion;

public class EventMapNestedConfigRuntime implements RegressionExecution {
    public void run(RegressionEnvironment env) {
        RegressionPath path = new RegressionPath();
        String epl =
                "@buseventtype @public create schema N1N1 (n1n1 String);\n" +
                "@buseventtype @public create schema N1 (n1 string, n2 N1N1);\n" +
                "@buseventtype @public create schema NestedMapWithSimpleProps (nested N1);\n";
        env.compileDeploy(epl, path);

        runAssertion(env, path);
    }
}
