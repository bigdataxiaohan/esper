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
import com.espertech.esper.common.client.hook.exception.ExceptionHandlerExceptionType;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.statement.CodegenStatementTryCatch;
import com.espertech.esper.common.internal.epl.expression.codegen.CodegenLegoCast;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.rettype.EPChainableType;
import com.espertech.esper.common.internal.rettype.EPChainableTypeHelper;
import com.espertech.esper.common.internal.util.ClassHelperGenericType;
import com.espertech.esper.common.internal.util.JavaClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class ExprDotMethodForgeNoDuckEvalPlain implements ExprDotEval {
    private static final Logger log = LoggerFactory.getLogger(ExprDotMethodForgeNoDuckEvalPlain.class);

    private final static String METHOD_HANDLETARGETEXCEPTION = "handleTargetException";

    protected final ExprDotMethodForgeNoDuck forge;
    private final ExprEvaluator[] parameters;

    ExprDotMethodForgeNoDuckEvalPlain(ExprDotMethodForgeNoDuck forge, ExprEvaluator[] parameters) {
        this.forge = forge;
        this.parameters = parameters;
    }

    public Object evaluate(Object target, EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        if (target == null) {
            return null;
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = parameters[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
        }

        try {
            return forge.getMethod().invoke(target, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            handleTargetException(forge.getOptionalStatementName(), forge.getMethod().getName(), forge.getMethod().getParameterTypes(), target.getClass().getName(), args, e, exprEvaluatorContext);
        }
        return null;
    }

    public EPChainableType getTypeInfo() {
        return forge.getTypeInfo();
    }

    public ExprDotForge getDotForge() {
        return forge;
    }

    public static CodegenExpression codegenPlain(ExprDotMethodForgeNoDuck forge, CodegenExpression inner, EPTypeClass innerType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        EPTypeClass returnType;
        if (forge.getWrapType() == ExprDotMethodForgeNoDuck.WrapType.WRAPARRAY) {
            returnType = ClassHelperGenericType.getMethodReturnEPType(forge.getMethod());
        } else {
            EPType result = EPChainableTypeHelper.getNormalizedEPType(forge.getTypeInfo());
            if (result == EPTypeNull.INSTANCE) {
                return constantNull();
            }
            returnType = JavaClassHelper.getBoxedType((EPTypeClass) result);
        }

        EPTypeClass declaringType = ClassHelperGenericType.getClassEPType(forge.getMethod().getDeclaringClass());
        CodegenMethod methodNode = codegenMethodScope.makeChild(returnType, ExprDotMethodForgeNoDuckEvalPlain.class, codegenClassScope).addParam(declaringType, "target");

        CodegenBlock block = methodNode.getBlock();

        if (!innerType.getType().isPrimitive() && !JavaClassHelper.isTypeVoid(returnType)) {
            block.ifRefNullReturnNull("target");
        }
        CodegenExpression[] args = new CodegenExpression[forge.getParameters().length];
        for (int i = 0; i < forge.getParameters().length; i++) {
            String name = "p" + i;
            EPType type = forge.getParameters()[i].getEvaluationType();
            if (type == null || type == EPTypeNull.INSTANCE) {
                block.declareVar(EPTypePremade.OBJECT.getEPType(), name, constantNull());
            } else {
                EPTypeClass typeClass = (EPTypeClass) type;
                block.declareVar(typeClass, name, forge.getParameters()[i].evaluateCodegen(typeClass, methodNode, exprSymbol, codegenClassScope));
            }
            args[i] = ref(name);
        }
        CodegenBlock tryBlock = block.tryCatch();
        CodegenExpression invocation = exprDotMethod(ref("target"), forge.getMethod().getName(), args);
        CodegenStatementTryCatch tryCatch;
        if (JavaClassHelper.isTypeVoid(returnType)) {
            tryCatch = tryBlock.expression(invocation).tryEnd();
        } else {
            tryCatch = tryBlock.tryReturn(CodegenLegoCast.castSafeFromObjectType(returnType, invocation));
        }
        CodegenBlock catchBlock = tryCatch.addCatch(EPTypePremade.THROWABLE.getEPType(), "t");
        catchBlock.declareVar(EPTypePremade.OBJECTARRAY.getEPType(), "args", newArrayByLength(EPTypePremade.OBJECT.getEPType(), constant(forge.getParameters().length)));
        for (int i = 0; i < forge.getParameters().length; i++) {
            catchBlock.assignArrayElement("args", constant(i), args[i]);
        }
        catchBlock.staticMethod(ExprDotMethodForgeNoDuckEvalPlain.class, METHOD_HANDLETARGETEXCEPTION, constant(forge.getOptionalStatementName()), constant(forge.getMethod().getName()), constant(forge.getMethod().getParameterTypes()),
            exprDotMethodChain(ref("target")).add("getClass").add("getName"), ref("args"), ref("t"), exprSymbol.getAddExprEvalCtx(methodNode));
        if (JavaClassHelper.isTypeVoid(returnType)) {
            block.methodEnd();
        } else {
            block.methodReturn(constantNull());
        }
        return localMethod(methodNode, inner);
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param optionalStatementName name
     * @param methodName            method name
     * @param methodParams          params
     * @param targetClassName       target class name
     * @param args                  args
     * @param t                     throwable
     * @param exprEvaluatorContext  expr context
     */
    public static void handleTargetException(String optionalStatementName, String methodName, Class[] methodParams, String targetClassName, Object[] args, Throwable t, ExprEvaluatorContext exprEvaluatorContext) {
        if (t instanceof InvocationTargetException) {
            t = ((InvocationTargetException) t).getTargetException();
        }
        String message = JavaClassHelper.getMessageInvocationTarget(optionalStatementName, methodName, methodParams, targetClassName, args, t);
        log.error(message, t);
        exprEvaluatorContext.getExceptionHandlingService().handleException(t, exprEvaluatorContext.getDeploymentId(), exprEvaluatorContext.getStatementName(), null, ExceptionHandlerExceptionType.PROCESS, null);
    }
}
