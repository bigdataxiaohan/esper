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
public class SupportBean_N implements Serializable {
    private static final long serialVersionUID = 4298178039762862893L;
    private int intPrimitive;
    private Integer intBoxed;
    private double doublePrimitive;
    private Double doubleBoxed;
    private boolean boolPrimitive;
    private Boolean boolBoxed;

    public SupportBean_N(int intPrimitive, Integer intBoxed, double doublePrimitive, Double doubleBoxed, boolean boolPrimitive, Boolean boolBoxed) {
        this.intPrimitive = intPrimitive;
        this.intBoxed = intBoxed;
        this.doublePrimitive = doublePrimitive;
        this.doubleBoxed = doubleBoxed;
        this.boolPrimitive = boolPrimitive;
        this.boolBoxed = boolBoxed;
    }

    public SupportBean_N(int intPrimitive, Integer intBoxed) {
        this.intPrimitive = intPrimitive;
        this.intBoxed = intBoxed;
    }

    public int getIntPrimitive() {
        return intPrimitive;
    }

    public Integer getIntBoxed() {
        return intBoxed;
    }

    public double getDoublePrimitive() {
        return doublePrimitive;
    }

    public Double getDoubleBoxed() {
        return doubleBoxed;
    }

    public boolean isBoolPrimitive() {
        return boolPrimitive;
    }

    public Boolean getBoolBoxed() {
        return boolBoxed;
    }

    public String toString() {
        return "intPrim=" + intPrimitive +
            " intBoxed=" + intBoxed +
            " doublePrim=" + doublePrimitive +
            " doubleBoxed=" + doubleBoxed +
            " boolPrim=" + boolPrimitive +
            " boolBoxed=" + boolBoxed;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SupportBean_N that = (SupportBean_N) o;

        if (intPrimitive != that.intPrimitive) return false;
        if (Double.compare(that.doublePrimitive, doublePrimitive) != 0) return false;
        if (boolPrimitive != that.boolPrimitive) return false;
        if (intBoxed != null ? !intBoxed.equals(that.intBoxed) : that.intBoxed != null) return false;
        if (doubleBoxed != null ? !doubleBoxed.equals(that.doubleBoxed) : that.doubleBoxed != null) return false;
        return boolBoxed != null ? boolBoxed.equals(that.boolBoxed) : that.boolBoxed == null;
    }

    public int hashCode() {
        int result;
        long temp;
        result = intPrimitive;
        result = 31 * result + (intBoxed != null ? intBoxed.hashCode() : 0);
        temp = Double.doubleToLongBits(doublePrimitive);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (doubleBoxed != null ? doubleBoxed.hashCode() : 0);
        result = 31 * result + (boolPrimitive ? 1 : 0);
        result = 31 * result + (boolBoxed != null ? boolBoxed.hashCode() : 0);
        return result;
    }
}
