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
package groovy

import groovy.test.GroovyTestCase

class NestedClassTest extends GroovyTestCase {

    void testStaticInnerStaticMethod () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.StaticInner.it
        """).newInstance()
        assertEquals 30, script.run()
    }

    void testStaticInnerInstanceMethod () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        new JavaClass.StaticInner ().result
        """).newInstance()
        assertEquals 239, script.run()
    }

    void testParam () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        def method (JavaClass.StaticInner obj) { obj.result }

        method new JavaClass.StaticInner ()
        """).newInstance()

        assertEquals 239, script.run()
    }

    void testTypeDecl () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.StaticInner method () { 239 }

        method ()
        """).newInstance()

        shouldFail (org.codehaus.groovy.runtime.typehandling.GroovyCastException) {
          assertEquals 239, script.run()
        }
    }

    void testFieldDecl () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.StaticInner field = 239

        field
        """).newInstance()

        shouldFail (org.codehaus.groovy.runtime.typehandling.GroovyCastException) {
          assertEquals 239, script.run()
        }
    }

    void testInstanceof () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        JavaClass.CONST instanceof JavaClass.StaticInner
        """).newInstance()

        assertTrue script.run ()
    }

    void testExtends () {
        def script = new GroovyClassLoader(getClass().getClassLoader()).parseClass ("""
        package groovy

        class U extends JavaClass.StaticInner.Inner2 {}

        new U ()

        """).newInstance()

        assert script.run () instanceof JavaClass.StaticInner.Inner2
    }
}