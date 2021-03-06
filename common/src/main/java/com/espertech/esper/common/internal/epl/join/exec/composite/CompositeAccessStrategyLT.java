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
package com.espertech.esper.common.internal.epl.join.exec.composite;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;

import java.util.*;

public class CompositeAccessStrategyLT extends CompositeAccessStrategyRelOpBase implements CompositeAccessStrategy {

    public CompositeAccessStrategyLT(boolean isNWOnTrigger, int lookupStream, int numStreams, ExprEvaluator key) {
        super(isNWOnTrigger, lookupStream, numStreams, key);
    }

    public Set<EventBean> lookup(EventBean theEvent, Map parent, Set<EventBean> result, CompositeIndexQuery next, ExprEvaluatorContext context, ArrayList<Object> optionalKeyCollector, CompositeIndexQueryResultPostProcessor postProcessor) {
        TreeMap index = (TreeMap) parent;
        Object comparable = super.evaluateLookup(theEvent, context);
        if (optionalKeyCollector != null) {
            optionalKeyCollector.add(comparable);
        }
        if (comparable == null) {
            return null;
        }
        return CompositeIndexQueryRange.handle(theEvent, index.headMap(comparable), null, result, next, postProcessor);
    }

    public Collection<EventBean> lookup(EventBean[] eventsPerStream, Map parent, Collection<EventBean> result, CompositeIndexQuery next, ExprEvaluatorContext context, ArrayList<Object> optionalKeyCollector, CompositeIndexQueryResultPostProcessor postProcessor) {
        TreeMap index = (TreeMap) parent;
        Object comparable = super.evaluatePerStream(eventsPerStream, context);
        if (optionalKeyCollector != null) {
            optionalKeyCollector.add(comparable);
        }
        if (comparable == null) {
            return null;
        }
        return CompositeIndexQueryRange.handle(eventsPerStream, index.headMap(comparable), null, result, next, postProcessor);
    }
}
