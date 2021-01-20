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
package com.espertech.esper.regressionlib.support.wordexample;

import java.io.Serializable;

/**
 * Test event; only serializable because it *may* go over the wire  when running remote tests and serialization is just convenient. Serialization generally not used for HA and HA testing.
 */
public class SentenceEvent implements Serializable {
    private static final long serialVersionUID = -2550674757742832011L;
    private final String sentence;

    public SentenceEvent(String sentence) {
        this.sentence = sentence;
    }

    public WordEvent[] getWords() {
        String[] split = sentence.split(" ");
        WordEvent[] words = new WordEvent[split.length];
        for (int i = 0; i < split.length; i++) {
            words[i] = new WordEvent(split[i]);
        }
        return words;
    }
}

