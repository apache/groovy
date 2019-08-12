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
package groovy.script

import groovy.test.GroovyTestCase

final class ScriptTes extends GroovyTestCase {

    void testScripts() {
        new File('src/test/groovy/script').eachFile {
            def name = it.name
            if (name.startsWith('script') && name.endsWith('.groovy')) {
                runScript(it)
            }
        }
    }

    protected def runScript(file) {
        println("Running script: " + file)

        def shell = new GroovyShell()
        def args = ['a', 'b', 'c']

        try {
            shell.run(file, args)
        }
        catch (Exception e) {
            println("Caught: " + e)
        }
    }
}
