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
package com.espertech.esper.regressionlib.support.bean;

import java.io.Serializable;

/**
 * Test event; only serializable because it *may* go over the wire  when running remote tests and serialization is just convenient. Serialization generally not used for HA and HA testing.
 */
public class SupportBeanTimestamp implements Serializable {
    private static final long serialVersionUID = 8962783408264231220L;
    private String id;
    private long timestamp;
    private String groupId;

    public SupportBeanTimestamp(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public SupportBeanTimestamp(String id, String groupId, long timestamp) {
        this.id = id;
        this.groupId = groupId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getGroupId() {
        return groupId;
    }
}
