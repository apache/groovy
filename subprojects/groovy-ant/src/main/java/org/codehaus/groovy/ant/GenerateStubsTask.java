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
public class GenerateStubsTask
    extends CompileTaskSupport
{
    @Override
    protected void compile() {
        GroovyClassLoader gcl = createClassLoader();
        JavaStubCompilationUnit cu = new JavaStubCompilationUnit(config, gcl, destdir);

        int count = 0;

        String[] list = src.list();
        for (int i = 0; i < list.length; i++) {
            File basedir = getProject().resolveFile(list[i]);
            if (!basedir.exists()) {
                throw new BuildException("Source directory does not exist: " + basedir, getLocation());
            }

            DirectoryScanner scanner = getDirectoryScanner(basedir);
            String[] includes = scanner.getIncludedFiles();

            log.debug("Including files from: " + basedir);

            for (int j=0; j < includes.length; j++) {
                log.debug("    "  + includes[j]);
                
                File file = new File(basedir, includes[j]);
                cu.addSource(file);

                // Increment the count for each non/java src we found
                if (!includes[j].endsWith(".java")) {
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Generating " + count + " Java stub" + (count > 1 ? "s" : "") + " to " + destdir);

            cu.compile();

            log.info("Generated " + cu.getStubCount() + " Java stub(s)");
        }
        else {
            log.info("No sources found for stub generation");
        }
    }
}
