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
package org.codehaus.groovy.reflection;

import groovy.transform.Internal;

/**
 * The {@code groovy.dgm.factory} switch: whether {@link GeneratedMetaMethod.Proxy}
 * creates DGM adapters through the generated {@code DgmProxyFactory} (the
 * default) or resolves them by class name with reflection.
 * <p>
 * This class holds nothing but that flag, read once from the system property,
 * so that a GraalVM native image can initialise it at build time (the shipped
 * {@code native-image.properties} does so). The flag is then a constant to the
 * image builder: with the default, every adapter is reachable through the
 * factory and needs no reflection metadata; with
 * {@code -Dgroovy.dgm.factory=false} passed to {@code native-image}, the
 * factory and every adapter the native-image agent did not record are left
 * out of the image, which is smaller but fails at run time on any DGM method
 * the agent run did not exercise.
 * <p>
 * On a JVM the property is read once at start-up and only chooses between the
 * two ways of creating an adapter the first time it is used; it has no effect
 * on calls after that.
 *
 * @since 6.0.0
 */
@Internal
public final class DgmProxyFactoryConfig {

    /** The system property; absent or anything but {@code false} enables the factory. */
    public static final String PROPERTY = "groovy.dgm.factory";

    /** Whether adapters are created through the generated factory. */
    public static final boolean ENABLED = read();

    private DgmProxyFactoryConfig() {
    }

    private static boolean read() {
        try {
            // only JDK calls here: this class may be initialised at image build time
            return !"false".equalsIgnoreCase(System.getProperty(PROPERTY));
        } catch (SecurityException ignore) {
            return true;
        }
    }
}
