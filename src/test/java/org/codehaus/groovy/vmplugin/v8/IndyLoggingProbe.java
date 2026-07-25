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
package org.codehaus.groovy.vmplugin.v8;

import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistryChangeEvent;
import org.codehaus.groovy.reflection.ClassInfo;

/**
 * Child-process probe for {@code -Dgroovy.indy.logging=true} branches
 * (GROOVY-12191). Invoked only from {@link IndyScopedSwitchPointTest}.
 */
public final class IndyLoggingProbe {

    private IndyLoggingProbe() {
    }

    public static void main(final String[] args) {
        if (!IndyInterface.LOG_ENABLED) {
            System.err.println("LOG_ENABLED expected true");
            System.exit(2);
        }
        // type != null listener log path
        ClassInfo.getClassInfo(ProbeHost.class).getIndySwitchPoint();
        GroovySystem.getMetaClassRegistry().setMetaClass(
                ProbeHost.class,
                GroovySystem.getMetaClassRegistry().getMetaClass(ProbeHost.class));
        // category / invalidateCallSites log path
        IndyInterface.invalidateSwitchPoints();
        // unscoped log path via synthetic null-class event. Other registry listeners
        // may NPE on a null Class; the IndyInterface listener must still run.
        MetaClassRegistryChangeEvent event = new MetaClassRegistryChangeEvent(
                GroovySystem.getMetaClassRegistry(), null, null, null, null);
        for (var listener : GroovySystem.getMetaClassRegistry().getMetaClassRegistryChangeEventListeners()) {
            try {
                listener.updateConstantMetaClass(event);
            } catch (Throwable ignore) {
                // non-indy listeners may reject null class
            }
        }
        System.out.println("OK");
    }

    public static final class ProbeHost {
    }
}
