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
package org.apache.groovy.json.internal

import groovy.test.GroovyTestCase

class FastStringUtilsTest extends GroovyTestCase {

    static class NoServiceLoaderGroovyClassLoader extends GroovyClassLoader {

        @Override
        Enumeration<URL> getResources(String name) throws IOException {
            if (name == "META-INF/services/org.apache.groovy.json.FastStringServiceFactory") {
                return Collections.emptyEnumeration();
            }
            return super.getResources(name);
        }
    }

    void testToCharArray() {
        char[] expected = ["t", "e", "s", "t"]
        assertEquals(expected, FastStringUtils.toCharArray("test"))
    }

    void testToCharArrayWithNoServiceLoaderUsedTheDefaultStringService() {
        ClassLoader previous = Thread.currentThread().getContextClassLoader()
        try {
            // Making sure that our class loader is used in the ServiceLoader class
            def loader = new NoServiceLoaderGroovyClassLoader();
            Thread.currentThread().setContextClassLoader(loader)
            def shell = new GroovyShell(loader)
            def result = shell.evaluate('''
            import org.apache.groovy.json.internal.FastStringUtils
            FastStringUtils.toCharArray("json")
        ''')
            assert result == ['j', 's', 'o', 'n'] as char[]

        } finally {
            Thread.currentThread().setContextClassLoader(previous)
        }
    }
}
