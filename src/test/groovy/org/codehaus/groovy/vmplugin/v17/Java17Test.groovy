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
package org.codehaus.groovy.vmplugin.v17

import org.codehaus.groovy.vmplugin.VMPlugin
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier
import java.net.http.HttpClient

import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * Membership lookups used by {@code checkAccessible}: JDK 8 packages that are
 * now concealed or exported-but-not-open, and modules absent from those maps.
 */
final class Java17Test {

    private static final VMPlugin PLUGIN = VMPluginFactory.plugin

    @Test
    void publicExportedJdkTypeIsAccessible() {
        [false, true].each { allow ->
            assert PLUGIN.checkAccessible(Java17Test, String, Modifier.PUBLIC, allow) : allow
        }
    }

    @Test
    void publicTypeInPostJava8ModuleIsAccessible() {
        // java.net.http is not a JDK 8 package, so the module is absent from
        // the concealed/exported-to-open maps; membership must still succeed.
        [false, true].each { allow ->
            assert PLUGIN.checkAccessible(Java17Test, HttpClient, Modifier.PUBLIC, allow) : allow
        }
    }

    @Test
    void concealedJdkPackageIsNotAccessible() {
        Class type = concealedJdkType()
        assumeTrue(type != null, 'no non-exported JDK class visible on this runtime')
        [false, true].each { allow ->
            assert !PLUGIN.checkAccessible(Java17Test, type, Modifier.PUBLIC, allow) : "$type $allow"
            assert !PLUGIN.checkAccessible(Java17Test, type, Modifier.PRIVATE, allow) : "$type $allow"
        }
    }

    @Test
    void privateMemberOfDescriptorOpenedJdkPackageIsAccessible() {
        // jdk.unsupported opens sun.misc / sun.reflect to all modules; those
        // packages are omitted from the Java 8 exported-to-open list, so the
        // unnamed module may read private members even without allowIllegalAccess.
        Class type = descriptorOpenedJdkType()
        assumeTrue(type != null, 'no descriptor-open JDK type visible on this runtime')
        [false, true].each { allow ->
            assert PLUGIN.checkAccessible(Java17Test, type, Modifier.PUBLIC, allow) : "$type $allow"
            assert PLUGIN.checkAccessible(Java17Test, type, Modifier.PRIVATE, allow) : "$type $allow"
        }
    }

    /** First JDK class whose package is not exported to this (unnamed) module. */
    private static Class concealedJdkType() {
        for (String name : ['jdk.internal.misc.Unsafe', 'jdk.internal.access.SharedSecrets', 'sun.misc.Unsafe']) {
            try {
                Class c = Class.forName(name)
                if (!c.module.isExported(c.packageName, Java17Test.module)) return c
            } catch (ClassNotFoundException ignored) {
            }
        }
        null
    }

    /** First public JDK class whose package is open to this module by the module descriptor. */
    private static Class descriptorOpenedJdkType() {
        for (String name : ['sun.misc.Unsafe', 'sun.reflect.ReflectionFactory']) {
            try {
                Class c = Class.forName(name)
                if (c.module.isNamed()
                        && c.module.isExported(c.packageName, Java17Test.module)
                        && c.module.isOpen(c.packageName, Java17Test.module)) {
                    return c
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        null
    }
}
