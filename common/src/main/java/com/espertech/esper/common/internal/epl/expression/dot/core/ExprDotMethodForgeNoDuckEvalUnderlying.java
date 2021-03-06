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
package com.espertech.esper.common.internal.epl.expression.dot.core;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.util.ClassHelperGenericType;
import com.espertech.esper.common.internal.util.JavaClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class ExprDotMethodForgeNoDuckEvalUnderlying extends ExprDotMethodForgeNoDuckEvalPlain {
    private static final Logger log = LoggerFactory.getLogger(ExprDotMethodForgeNoDuckEvalUnderlying.class);

    public ExprDotMethodForgeNoDuckEvalUnderlying(ExprDotMethodForgeNoDuck forge, ExprEvaluator[] parameters) {
        super(forge, parameters);
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }
        if (!(target instanceof EventBean)) {
            log.warn("Expected EventBean return value but received '" + target.getClass().getName() + "' for statement " + forge.getOptionalStatementName());
            return null;
        }
        EventBean bean = (EventBean) target;
        return super.evaluate(bean.getUnderlying(), eventsPerStream, isNewData, exprEvaluatorContext);
    }

    public static CodegenExpression codegenUnderlying(ExprDotMethodForgeNoDuck forge, CodegenExpression inner, EPTypeClass innerType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        EPTypeClass underlyingType = ClassHelperGenericType.getClassEPType(forge.getMethod().getDeclaringClass());
        EPTypeClass returnType = ClassHelperGenericType.getMethodReturnEPType(forge.getMethod());
        CodegenMethod methodNode = codegenMethodScope.makeChild(JavaClassHelper.getBoxedType(returnType), ExprDotMethodForgeNoDuckEvalUnderlying.class, codegenClassScope).addParam(EventBean.EPTYPE, "target");

        CodegenExpression eval = ExprDotMethodForgeNoDuckEvalPlain.codegenPlain(forge, ref("underlying"), innerType, methodNode, exprSymbol, codegenClassScope);
        if (!JavaClassHelper.isTypeVoid(returnType)) {
            methodNode.getBlock()
                .ifRefNullReturnNull("target")
                .declareVar(underlyingType, "underlying", cast(underlyingType, exprDotMethod(ref("target"), "getUnderlying")))
                .methodReturn(eval);
        } else {
            methodNode.getBlock()
                .ifRefNotNull("target")
                .declareVar(underlyingType, "underlying", cast(underlyingType, exprDotMethod(ref("target"), "getUnderlying")))
                .expression(eval);
        }
        return localMethod(methodNode, inner);
    }
}
