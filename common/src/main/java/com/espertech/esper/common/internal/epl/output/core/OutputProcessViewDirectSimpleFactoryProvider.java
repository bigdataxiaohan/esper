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
package com.espertech.esper.common.internal.epl.output.core;

import com.espertech.esper.common.client.type.EPTypeClass;

public class OutputProcessViewDirectSimpleFactoryProvider implements OutputProcessViewFactoryProvider {
    public final static EPTypeClass EPTYPE = new EPTypeClass(OutputProcessViewDirectSimpleFactoryProvider.class);

    public final static OutputProcessViewDirectSimpleFactoryProvider INSTANCE = new OutputProcessViewDirectSimpleFactoryProvider();

    private OutputProcessViewDirectSimpleFactoryProvider() {
    }

    public OutputProcessViewFactory getOutputProcessViewFactory() {
        return OutputProcessViewDirectSimpleFactory.INSTANCE;
    }
}
