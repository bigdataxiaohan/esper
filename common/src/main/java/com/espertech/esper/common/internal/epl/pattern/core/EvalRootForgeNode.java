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
package com.espertech.esper.common.internal.epl.pattern.core;

import com.espertech.esper.common.client.annotation.AppliesTo;
import com.espertech.esper.common.client.annotation.AuditEnum;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.compile.util.CallbackAttribution;
import com.espertech.esper.common.internal.compile.stage2.FilterSpecTracked;
import com.espertech.esper.common.internal.context.aifactory.core.SAIFFInitializeSymbol;
import com.espertech.esper.common.internal.schedule.ScheduleHandleTracked;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.localMethod;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.ref;

/**
 * This class represents an observer expression in the evaluation tree representing an pattern expression.
 */
public class EvalRootForgeNode extends EvalForgeNodeBase {

    public EvalRootForgeNode(boolean attachPatternText, EvalForgeNode childNode, Annotation[] annotations) {
        super(attachPatternText);
        addChildNode(childNode);
        boolean audit = AuditEnum.PATTERN.getAudit(annotations) != null || AuditEnum.PATTERNINSTANCES.getAudit(annotations) != null;
        assignFactoryNodeIds(audit);
    }

    protected EPTypeClass typeOfFactory() {
        return EvalRootFactoryNode.EPTYPE;
    }

    protected String nameOfFactory() {
        return "root";
    }

    public AppliesTo appliesTo() {
        return AppliesTo.PATTERN_ROOT;
    }

    protected void inlineCodegen(CodegenMethod method, SAIFFInitializeSymbol symbols, CodegenClassScope classScope) {
        CodegenMethod childMake = getChildNodes().get(0).makeCodegen(method, symbols, classScope);
        method.getBlock()
            .exprDotMethod(ref("node"), "setChildNode", localMethod(childMake));
    }

    public void toPrecedenceFreeEPL(StringWriter writer) {
        if (!getChildNodes().isEmpty()) {
            getChildNodes().get(0).toEPL(writer, getPrecedence());
        }
    }

    public PatternExpressionPrecedenceEnum getPrecedence() {
        return PatternExpressionPrecedenceEnum.MINIMUM;
    }

    public void collectSelfFilterAndSchedule(Function<Short, CallbackAttribution> callbackAttribution, List<FilterSpecTracked> filters, List<ScheduleHandleTracked> schedules) {
        // none here
    }

    public List<EvalForgeNode> collectFactories() {
        List<EvalForgeNode> factories = new ArrayList<>(8);
        for (EvalForgeNode factoryNode : getChildNodes()) {
            collectFactoriesRecursive(factoryNode, factories);
        }
        return factories;
    }

    private static void collectFactoriesRecursive(EvalForgeNode factoryNode, List<EvalForgeNode> factories) {
        factories.add(factoryNode);
        for (EvalForgeNode childNode : factoryNode.getChildNodes()) {
            collectFactoriesRecursive(childNode, factories);
        }
    }

    // assign factory ids, a short-type number assigned once-per-statement to each pattern node
    // return the count of all ids
    private void assignFactoryNodeIds(boolean audit) {
        short count = 0;
        setFactoryNodeId(count);
        setAudit(audit);
        List<EvalForgeNode> factories = collectFactories();
        for (EvalForgeNode factoryNode : factories) {
            count++;
            factoryNode.setFactoryNodeId(count);
            factoryNode.setAudit(audit);
        }
    }
}
