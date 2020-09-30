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
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.codehaus.groovy.control.CompilationUnit;

import java.io.File;

/**
 * Compiles Groovy source files.
 */
public class GroovycTask
    extends CompileTaskSupport
{
    protected boolean force;

    public void setForce(final boolean flag) {
        this.force = flag;
    }

    @Override
    protected void compile() {
        Path path = getClasspath();
        if (path != null) {
            config.setClasspath(path.toString());
        }

        config.setTargetDirectory(destdir);

        GroovyClassLoader gcl = createClassLoader();
        CompilationUnit compilation = new CompilationUnit(config, null, gcl);

        GlobPatternMapper mapper = new GlobPatternMapper();
        mapper.setFrom("*.groovy");
        mapper.setTo("*.class");
        
        int count = 0;
        String[] list = src.list();

        for (String s : list) {
            File basedir = getProject().resolveFile(s);

            if (!basedir.exists()) {
                throw new BuildException("Source directory does not exist: " + basedir, getLocation());
            }

            DirectoryScanner scanner = getDirectoryScanner(basedir);
            String[] includes = scanner.getIncludedFiles();

            if (force) {
                log.debug("Forcefully including all files from: " + basedir);

                for (String include : includes) {
                    File file = new File(basedir, include);
                    log.debug("    " + file);

                    compilation.addSource(file);
                    count++;
                }
            } else {
                log.debug("Including changed files from: " + basedir);

                SourceFileScanner sourceScanner = new SourceFileScanner(this);
                File[] files = sourceScanner.restrictAsFiles(includes, basedir, destdir, mapper);

                for (File file : files) {
                    log.debug("    " + file);

                    compilation.addSource(file);
                    count++;
                }
            }
        }

        if (count > 0) {
            log.info("Compiling " + count + " source file" + (count > 1 ? "s" : "") + " to " + destdir);

            compilation.compile();
        }
        else {
            log.info("No sources found to compile");
        }
    }
}