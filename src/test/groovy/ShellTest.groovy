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

class ShellTest extends GroovyTestCase {

    void testReadAndWriteVariable() {
        def shell = new GroovyShell()
        
        shell.foo = 1
        
        def value = shell.evaluate("""
println('foo is currently ' + foo)
foo = 2 
println('foo is now ' + foo)                
return foo
""", "Dummy1.groovy")

        assert value == 2
        assert shell.foo == 2 , "Value is now ${shell.foo}"
    }
    
    void testDefineNewVariable() {
        def shell = new GroovyShell()
        
        def value = shell.evaluate( """
bar = 3 
println('bar is now ' + bar)                
return bar
""", "Dummy2.groovy")

        assert value == 3
        assert shell.bar == 3 , "Value is now ${shell.bar}"
    }

    void testArgs() {
        def seventyfive = new GroovyShell().run("args[0] + args[1]", "StringSummerScript", ['7', '5'])
        assert seventyfive == '75'
        def twelve = new GroovyShell().run("args*.toInteger().sum()", "NumberSummerScript", ['7', '5'])
        assert twelve == 12
    }
}