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

class Groovy4241Bug extends GroovyTestCase {
    void testAsTypeWithinvokeMethodOverridden() {
        Foo4241.metaClass.invokeMethod = { String name, args ->
            println name
            for (arg in args) {
                println arg.getClass()
            }
        }
        def f = new Foo4241()
        
        def bar = [key: 'foo'] as Bar4241
        f.echo(bar) // this used to work
        
        f.echo([key: 'foo'] as Bar4241) // this used to fail with NPE
    }
}

class Foo4241 {}

class Bar4241 {}
