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
package groovy.util

/**
 * Groovy test case, which recreates internal GroovyShell in each setUp()
 */
class GroovyShellTestCase extends GroovyTestCase {

    @Delegate protected GroovyShell shell

    protected void setUp() {
        super.setUp();
        shell = createNewShell();
    }

    protected void tearDown() {
        shell = null;
        super.tearDown();
    }

    /**
     * Create new shell instance.
     * Overwrite it to customize
     */
    protected GroovyShell createNewShell() {
        return new GroovyShell()
    }

    /**
     * Executes closure with given binding
     */
    protected def withBinding (Map map, Closure closure) {
        Binding binding = shell.context
        Map bmap = binding.variables
        try {
            Map vars = new HashMap(bmap)
            bmap.putAll map

            return closure.call()
        }
        finally {
            bmap.clear()
            bmap.putAll vars
        }
    }

    /**
     * Evaluates script with given binding
     */
    protected def withBinding (Map map, String script) {
        Binding binding = shell.context
        Map bmap = binding.variables
        try {
            Map vars = new HashMap(bmap)
            bmap.putAll map

            return evaluate(script)
        }
        finally {
            bmap.clear()
            bmap.putAll vars
        }
    }
}