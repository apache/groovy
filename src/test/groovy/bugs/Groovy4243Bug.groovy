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

import org.codehaus.groovy.control.*

class Groovy4243Bug extends GroovyTestCase {
    void testScriptBaseClassWithAnonymousInnerClass() {
        def configuration = new CompilerConfiguration()
        configuration.scriptBaseClass = TestScript4243.name
        def classLoader = new GroovyClassLoader(getClass().classLoader, configuration)

        // This works
        def scriptClass = classLoader.parseClass('''
            def r = new TestRunnable()
            class TestRunnable implements Runnable {
                public void run() {}
            }
        ''')
        assert TestScript4243.isAssignableFrom(scriptClass)

        // This does not work
        scriptClass = classLoader.parseClass('''
            def r = new Runnable() {
                public void run() { }
            }
        ''')
        assert Script.isAssignableFrom(scriptClass)
        assert TestScript4243.isAssignableFrom(scriptClass) // <-- fails here    
    }
}

abstract class TestScript4243 extends Script { }