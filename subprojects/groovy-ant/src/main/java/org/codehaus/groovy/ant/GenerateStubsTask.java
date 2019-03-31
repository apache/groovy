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

import groovy.lang.GroovyClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.codehaus.groovy.tools.javac.JavaStubCompilationUnit;

import java.io.File;

/**
 * Generates Java stubs from Groovy sources.
 */
public class GenerateStubsTask extends CompileTaskSupport {
    @Override
    protected void compile() {
        GroovyClassLoader gcl = createClassLoader();
        JavaStubCompilationUnit cu = new JavaStubCompilationUnit(config, gcl, destdir);

        int count = 0;

        for (String srcPath : src.list()) {
            File srcDir = getProject().resolveFile(srcPath);
            if (!srcDir.exists()) {
                throw new BuildException("Source directory does not exist: " + srcDir, getLocation());
            }

            DirectoryScanner scanner = getDirectoryScanner(srcDir);

            log.debug("Including files from: " + srcDir);

            for (String includeName : scanner.getIncludedFiles()) {
                log.debug("    " + includeName);

                File file = new File(srcDir, includeName);
                cu.addSource(file);

                // Increment the count for each non/java src we found
                if (!includeName.endsWith(".java")) {
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Generating " + count + " Java stub" + (count > 1 ? "s" : "") + " to " + destdir);
            cu.compile();
            log.info("Generated " + cu.getStubCount() + " Java stub(s)");
        } else {
            log.info("No sources found for stub generation");
        }
    }
}
