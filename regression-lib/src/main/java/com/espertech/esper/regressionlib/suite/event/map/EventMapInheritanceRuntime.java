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
import com.espertech.esper.regressionlib.framework.RegressionFlag;
import com.espertech.esper.regressionlib.framework.RegressionPath;

import java.util.EnumSet;

public class EventMapInheritanceRuntime implements RegressionExecution {
    public void run(RegressionEnvironment env) {
        RegressionPath path = new RegressionPath();
        String epl =
                "@buseventtype @public create schema RootEvent(base string);\n" +
                "@buseventtype @public create schema Sub1Event(sub1 string) inherits RootEvent;\n" +
                "@buseventtype @public create schema Sub2Event(sub2 string) inherits RootEvent;\n" +
                "@buseventtype @public create schema SubAEvent(suba string) inherits Sub1Event;\n" +
                "@buseventtype @public create schema SubBEvent(subb string) inherits Sub1Event, Sub2Event;\n";
        env.compileDeploy(epl, path);

        EventMapInheritanceInitTime.runAssertionMapInheritance(env, path);
    }

    public EnumSet<RegressionFlag> flags() {
        return EnumSet.of(RegressionFlag.OBSERVEROPS);
    }
}
