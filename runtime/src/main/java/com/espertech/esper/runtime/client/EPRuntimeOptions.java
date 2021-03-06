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
package com.espertech.esper.runtime.client;

import com.espertech.esper.runtime.client.option.UpgradeOption;

public class EPRuntimeOptions {
    private UpgradeOption upgradeOption;

    public UpgradeOption getUpgradeOption() {
        return upgradeOption;
    }

    public void setUpgradeOption(UpgradeOption upgradeOption) {
        this.upgradeOption = upgradeOption;
    }
}
