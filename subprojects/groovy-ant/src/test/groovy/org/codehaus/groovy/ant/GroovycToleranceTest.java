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
package org.codehaus.groovy.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * GROOVY-12306: the {@code tolerance} attribute of the {@code <groovyc>} task must reach the
 * compiler. Both the forked and in-process paths run the arguments assembled here through the
 * {@link FileSystemCompiler} parser, so covering the assembled command line covers both.
 */
final class GroovycToleranceTest {

    @Test
    void toleranceIsOmittedWhenTheAttributeIsUnset() throws Exception {
        assertFalse(commandLineFor(null).contains("-t"), "no tolerance attribute means no -t argument");
        assertEquals(CompilerConfiguration.DEFAULT_TOLERANCE, toleranceFor(null));
    }

    @Test
    void toleranceAttributeReachesTheCompilerConfiguration() throws Exception {
        assertEquals(5, toleranceFor(5));
        assertEquals(1, toleranceFor(1));
    }

    // zero is meaningful rather than absent: it asks for unlimited error reporting
    @Test
    void zeroToleranceIsPassedThroughRatherThanTreatedAsUnset() throws Exception {
        List<String> commandLine = commandLineFor(0);
        assertEquals("0", commandLine.get(commandLine.indexOf("-t") + 1));
        assertEquals(0, toleranceFor(0));
    }

    private static int toleranceFor(Integer tolerance) throws Exception {
        List<String> commandLine = commandLineFor(tolerance);
        FileSystemCompiler.CompilationOptions options = new FileSystemCompiler.CompilationOptions();
        FileSystemCompiler.configureParser(options).parseArgs(commandLine.toArray(new String[0]));
        return options.toCompilerConfiguration().getTolerance();
    }

    private static List<String> commandLineFor(Integer tolerance) throws Exception {
        Project project = new Project();
        project.init();
        Groovyc task = new Groovyc();
        task.setProject(project);
        if (tolerance != null) {
            task.setTolerance(tolerance);
        }
        List<String> commandLineList = new ArrayList<>();
        Method method = Groovyc.class.getDeclaredMethod(
                "doNormalCommandLineList", List.class, List.class, Path.class);
        method.setAccessible(true);
        method.invoke(task, commandLineList, new ArrayList<String>(), new Path(project));
        return commandLineList;
    }
}
