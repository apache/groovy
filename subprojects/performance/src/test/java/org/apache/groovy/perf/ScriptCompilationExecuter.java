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
package org.apache.groovy.perf;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ScriptCompilationExecuter {
    private final File[] sources;
    private final URL[] classpath;

    public ScriptCompilationExecuter(File[] sourceDirectories, final List<URL> classpath) throws IOException {
        this.classpath = classpath.toArray(new URL[classpath.size()]);
        Set<File> sources = new HashSet<>();
        for (File sourceDirectory : sourceDirectories) {
            Files.walk(sourceDirectory.toPath())
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile()
                                && file.getName().endsWith(".groovy");
                    })
                    .forEach(path -> sources.add(path.toFile()));
        }
        this.sources = sources.toArray(new File[sources.size()]);
        System.out.println("sources = " + sources.size());
    }

    public long execute() throws Exception {
        ClassLoader cl = new URLClassLoader(classpath, ClassLoader.getSystemClassLoader().getParent());
        GroovyClassLoader gcl = new GroovyClassLoader(cl);
        CompilationUnit cu = new CompilationUnit(new CompilerConfiguration(), null, gcl, new GroovyClassLoader(this.getClass().getClassLoader()));
        for (File source : sources) {
            cu.addSource(source);
        }
        long sd = System.nanoTime();
        cu.compile(CompilePhase.CLASS_GENERATION.getPhaseNumber());
        long dur = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - sd);
        return dur;
    }
}
