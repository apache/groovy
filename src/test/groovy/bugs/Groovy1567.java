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
package groovy.bugs;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.junit.Test;

import java.io.File;

public final class Groovy1567 {

    @Test
    public void testGroovyScriptEngineVsGroovyShell() throws Exception {
        // @todo refactor this path
        File currentDir = new File("src/test/groovy/bugs");
        String file = "scriptForGroovy1567.groovy";

        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding);
        String[] test = null;

        //
        shell.run(new File(currentDir, file), test);

        String[] roots = new String[]{currentDir.getAbsolutePath()};
        GroovyScriptEngine gse = new GroovyScriptEngine(roots);
        binding = new Binding();
        // a MME was ensued here stating no 't.start()' was available in the script
        gse.run(file, binding);
    }
}
