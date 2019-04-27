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
package groovy.bugs

import java.lang.reflect.*

class Groovy3726Bug extends GroovyTestCase {
    void testVolatilePropertiesResultingInBridgeMethods() {
        def scriptStr, clazz, fooGetter, fooSetter
        GroovyClassLoader cl = new GroovyClassLoader();

        scriptStr = """
            public class GroovyBean3726B {
                volatile String foo = "anything"
            }
        """
        clazz = cl.parseClass(scriptStr, 'GroovyBean3726B.groovy')

        fooGetter = clazz.getMethod('getFoo')
        assertFalse fooGetter.isBridge()
        assertFalse Modifier.isVolatile(fooGetter.modifiers)

        fooSetter = clazz.getMethod('setFoo', [String] as Class[])
        assertFalse fooSetter.isBridge()
        assertFalse Modifier.isVolatile(fooSetter.modifiers)
    }

    void testTransientPropertiesResultingInVarArgsMethods() {
        def scriptStr, clazz, barGetter, barSetter
        GroovyClassLoader cl = new GroovyClassLoader();

        scriptStr = """
            public class GroovyBean3726D {
                transient String bar = "anything"
            }
        """
        clazz = cl.parseClass(scriptStr, 'GroovyBean3726D.groovy')

        barGetter = clazz.getMethod('getBar')
        assertFalse barGetter.isVarArgs()
        assertFalse Modifier.isTransient(barGetter.modifiers)

        barSetter = clazz.getMethod('setBar', [String] as Class[])
        assertFalse barSetter.isVarArgs()
        assertFalse Modifier.isTransient(barSetter.modifiers)
    }
}
