/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.runtime.indy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Child-process probe for {@code -Dgroovy.indy.invalidation.stats=true} log branches.
 * Invoked from {@link IndyInvalidationTest}.
 */
public final class IndyInvalidationStatsProbe {

    private IndyInvalidationStatsProbe() {
    }

    public static void main(final String[] args) {
        Logger log = Logger.getLogger(IndyInvalidation.class.getName());
        log.setLevel(Level.FINE);
        // Ensure a parent handler does not filter FINE.
        Logger root = Logger.getLogger("");
        root.setLevel(Level.FINE);
        for (var h : root.getHandlers()) {
            h.setLevel(Level.FINE);
        }

        IndyInvalidation.invalidateClass(ProbeHost.class);
        IndyInvalidation.invalidateBulk();
        IndyInvalidation.invalidateCategory();
        IndyInvalidation.invalidateUnscoped();
        System.out.println("OK");
    }

    public static final class ProbeHost {
    }
}
