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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.groovy.runtime.DefaultGroovyStaticMethods;

import java.io.File;
import java.io.IOException;

/**
 * Compiles Java and Groovy source files.
 *
 * This works by invoking the {@link GenerateStubsTask} task, then the
 * {@link Javac} task and then the {@link GroovycTask}.  Each task can be
 * configured by creating a nested element.  Common configuration such as
 * the source dir and classpath is picked up from this tasks configuration.
 */
public class UberCompileTask extends Task {
    private Path src;

    private File destdir;

    private Path classpath;

    private GenStubsAdapter genStubsTask;

    private GroovycAdapter groovycTask;

    private JavacAdapter javacTask;

    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    public void setSrcdir(final Path dir) {
        assert dir != null;

        if (src == null) {
            src = dir;
        }
        else {
            src.append(dir);
        }
    }

    public Path getSrcdir() {
        return src;
    }

    public void setDestdir(final File dir) {
        assert dir != null;

        this.destdir = dir;
    }

    public void setClasspath(final Path path) {
        assert path != null;
        
        if (classpath == null) {
            classpath = path;
        }
        else {
            classpath.append(path);
        }
    }

    public Path getClasspath() {
        return classpath;
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }

        return classpath.createPath();
    }

    public void setClasspathRef(final Reference r) {
        assert r != null;

        createClasspath().setRefid(r);
    }

    public GenStubsAdapter createGeneratestubs() {
        if (genStubsTask == null) {
            genStubsTask = new GenStubsAdapter();
            genStubsTask.setProject(getProject());
        }
        return genStubsTask;
    }

    public GroovycAdapter createGroovyc() {
        if (groovycTask == null) {
            groovycTask = new GroovycAdapter();
            groovycTask.setProject(getProject());
        }
        return groovycTask;
    }

    public JavacAdapter createJavac() {
        if (javacTask == null) {
            javacTask = new JavacAdapter();
            javacTask.setProject(getProject());
        }
        return javacTask;
    }

    protected void validate() throws BuildException {
        if (src == null) {
            throw new BuildException("Missing attribute: srcdir (or one or more nested <src> elements).", getLocation());
        }

        if (destdir == null) {
            throw new BuildException("Missing attribute: destdir", getLocation());
        }

        if (!destdir.exists()) {
            throw new BuildException("Destination directory does not exist: " + destdir, getLocation());
        }
    }
    
    public void execute() throws BuildException {
        validate();

        FileSet fileset;
        
        GenStubsAdapter genstubs = createGeneratestubs();
        genstubs.classpath = classpath;
        genstubs.src = src;
        if (genstubs.destdir == null) {
            genstubs.destdir = createTempDir();
        }

        fileset = genstubs.getFileSet();
        if (!fileset.hasPatterns()) {
            genstubs.createInclude().setName("**/*.java");
            genstubs.createInclude().setName("**/*.groovy");
        }
                
        JavacAdapter javac = createJavac();
        javac.setSrcdir(src);
        javac.setDestdir(destdir);
        javac.setClasspath(classpath);

        fileset = javac.getFileSet();
        if (!fileset.hasPatterns()) {
            javac.createInclude().setName("**/*.java");
        }

        // Include the stubs in the Javac compilation
        javac.createSrc().createPathElement().setLocation(genstubs.destdir);

        GroovycAdapter groovyc = createGroovyc();
        groovyc.classpath = classpath;
        groovyc.src = src;
        groovyc.destdir = destdir;

        //
        // HACK: For now force all classes to compile, so we pick up stub changes
        //
        groovyc.force = true;
        
        fileset = groovyc.getFileSet();
        if (!fileset.hasPatterns()) {
            groovyc.createInclude().setName("**/*.groovy");
        }

        // Invoke each task in the right order
        genstubs.execute();
        javac.execute();
        groovyc.execute();
    }

    private File createTempDir()  {
        try {
            return DefaultGroovyStaticMethods.createTempDir(null, "groovy-", "stubs");
        }
        catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }

    //
    // Nested task adapters
    //
    
    private class GenStubsAdapter extends GenerateStubsTask {
        public FileSet getFileSet() {
            return super.getImplicitFileSet();
        }

        public String getTaskName() {
            return UberCompileTask.this.getTaskName() + ":genstubs";
        }
    }

    private class JavacAdapter extends Javac {
        public FileSet getFileSet() {
            return super.getImplicitFileSet();
        }

        public String getTaskName() {
            return UberCompileTask.this.getTaskName() + ":javac";
        }
    }

    private class GroovycAdapter extends GroovycTask {
        public FileSet getFileSet() {
            return super.getImplicitFileSet();
        }

        public String getTaskName() {
            return UberCompileTask.this.getTaskName() + ":groovyc";
        }
    }
}