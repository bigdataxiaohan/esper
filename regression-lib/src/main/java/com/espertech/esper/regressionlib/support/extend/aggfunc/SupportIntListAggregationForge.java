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
package com.espertech.esper.regressionlib.support.extend.aggfunc;

import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionForge;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionMode;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionModeMultiParam;
import com.espertech.esper.common.client.hook.aggfunc.AggregationFunctionValidationContext;
import com.espertech.esper.common.client.hook.forgeinject.InjectionStrategyClassNewInstance;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeClassParameterized;
import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;

import java.util.List;

public class SupportIntListAggregationForge implements AggregationFunctionForge {

    public void validate(AggregationFunctionValidationContext validationContext) throws ExprValidationException {
    }

    public AggregationFunctionMode getAggregationFunctionMode() {
        return new AggregationFunctionModeMultiParam().setInjectionStrategyAggregationFunctionFactory(new InjectionStrategyClassNewInstance(SupportIntListAggregationFactory.EPTYPE));
    }

    public EPTypeClass getValueType() {
        return new EPTypeClassParameterized(List.class, new EPTypeClass[] {new EPTypeClass(Integer.class)});
    }

    public void setFunctionName(String functionName) {
    }
}
