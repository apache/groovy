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
import org.codehaus.groovy.control.ErrorFormat;
import org.codehaus.groovy.tools.FileSystemCompiler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * GROOVY-12312: the {@code errorFormat} attribute of the {@code <groovyc>} task must reach the
 * compiler. Both the forked and in-process paths run the arguments assembled here through the
 * {@link FileSystemCompiler} parser, so covering the assembled command line covers both.
 */
final class GroovycErrorFormatTest {

    @Test
    void errorFormatIsOmittedWhenTheAttributeIsUnset() throws Exception {
        assertFalse(commandLineFor(null).contains("--error-format"), "no attribute means no option");
        assertEquals(ErrorFormat.FULL, errorFormatFor(null));
    }

    @Test
    void errorFormatAttributeReachesTheCompilerConfiguration() throws Exception {
        assertEquals(ErrorFormat.SHORT, errorFormatFor("short"));
        assertEquals(ErrorFormat.FULL, errorFormatFor("full"));
    }

    // Ant build files are written by hand, so the attribute accepts any casing
    @Test
    void errorFormatAttributeIsCaseInsensitive() throws Exception {
        assertEquals(ErrorFormat.SHORT, errorFormatFor("SHORT"));
        assertEquals(ErrorFormat.SHORT, errorFormatFor("Short"));
    }

    private static ErrorFormat errorFormatFor(String errorFormat) throws Exception {
        List<String> commandLine = commandLineFor(errorFormat);
        FileSystemCompiler.CompilationOptions options = new FileSystemCompiler.CompilationOptions();
        FileSystemCompiler.configureParser(options).parseArgs(commandLine.toArray(new String[0]));
        return options.toCompilerConfiguration().getErrorFormat();
    }

    private static List<String> commandLineFor(String errorFormat) throws Exception {
        Project project = new Project();
        project.init();
        Groovyc task = new Groovyc();
        task.setProject(project);
        if (errorFormat != null) {
            task.setErrorFormat(errorFormat);
        }
        List<String> commandLineList = new ArrayList<>();
        Method method = Groovyc.class.getDeclaredMethod(
                "doNormalCommandLineList", List.class, List.class, Path.class);
        method.setAccessible(true);
        method.invoke(task, commandLineList, new ArrayList<String>(), new Path(project));
        return commandLineList;
    }
}
