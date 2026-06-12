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
 * the source dir and classpath is picked up from this task's configuration.
 */
public class UberCompileTask extends Task {
    private Path src;

    private File destdir;

    private Path classpath;

    private GenStubsAdapter genStubsTask;

    private GroovycAdapter groovycTask;

    private JavacAdapter javacTask;

    /**
     * Creates a nested {@code <src>} path element.
     *
     * @return the path element to configure
     */
    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    /**
     * Adds source roots to the task.
     *
     * @param dir the source path to append
     */
    public void setSrcdir(final Path dir) {
        assert dir != null;

        if (src == null) {
            src = dir;
        }
        else {
            src.append(dir);
        }
    }

    /**
     * Returns the configured source roots.
     *
     * @return the source path
     */
    public Path getSrcdir() {
        return src;
    }

    /**
     * Sets the destination directory for compiled output.
     *
     * @param dir the destination directory
     */
    public void setDestdir(final File dir) {
        assert dir != null;

        this.destdir = dir;
    }

    /**
     * Adds entries to the shared compilation classpath.
     *
     * @param path the classpath entries to append
     */
    public void setClasspath(final Path path) {
        assert path != null;

        if (classpath == null) {
            classpath = path;
        }
        else {
            classpath.append(path);
        }
    }

    /**
     * Returns the configured shared compilation classpath.
     *
     * @return the classpath
     */
    public Path getClasspath() {
        return classpath;
    }

    /**
     * Creates a nested {@code <classpath>} path element.
     *
     * @return the path element to configure
     */
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }

        return classpath.createPath();
    }

    /**
     * Adds a reference to a classpath defined elsewhere.
     *
     * @param r the classpath reference
     */
    public void setClasspathRef(final Reference r) {
        assert r != null;

        createClasspath().setRefid(r);
    }

    /**
     * Lazily creates the nested stub-generation adapter.
     *
     * @return the adapter used to configure stub generation
     */
    public GenStubsAdapter createGeneratestubs() {
        if (genStubsTask == null) {
            genStubsTask = new GenStubsAdapter();
            genStubsTask.setProject(getProject());
        }
        return genStubsTask;
    }

    /**
     * Lazily creates the nested Groovy compilation adapter.
     *
     * @return the adapter used to configure Groovy compilation
     */
    public GroovycAdapter createGroovyc() {
        if (groovycTask == null) {
            groovycTask = new GroovycAdapter();
            groovycTask.setProject(getProject());
        }
        return groovycTask;
    }

    /**
     * Lazily creates the nested Java compilation adapter.
     *
     * @return the adapter used to configure Java compilation
     */
    public JavacAdapter createJavac() {
        if (javacTask == null) {
            javacTask = new JavacAdapter();
            javacTask.setProject(getProject());
        }
        return javacTask;
    }

    /**
     * Validates the task configuration before running the nested compilers.
     *
     * @throws BuildException if required attributes are missing or invalid
     */
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

    /**
     * Runs stub generation, Java compilation, and Groovy compilation in sequence.
     *
     * @throws BuildException if configuration is invalid or any nested task fails
     */
    @Override
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
        /**
         * Returns the implicit file set configured on the nested task.
         *
         * @return the nested file set
         */
        public FileSet getFileSet() {
            return super.getImplicitFileSet();
        }

        /**
         * Returns a task name scoped to the parent uber-compile task.
         *
         * @return the effective task name
         */
        @Override
        public String getTaskName() {
            return UberCompileTask.this.getTaskName() + ":genstubs";
        }
    }

    private class JavacAdapter extends Javac {
        /**
         * Returns the implicit file set configured on the nested task.
         *
         * @return the nested file set
         */
        public FileSet getFileSet() {
            return super.getImplicitFileSet();
        }

        /**
         * Returns a task name scoped to the parent uber-compile task.
         *
         * @return the effective task name
         */
        @Override
        public String getTaskName() {
            return UberCompileTask.this.getTaskName() + ":javac";
        }
    }

    private class GroovycAdapter extends GroovycTask {
        /**
         * Returns the implicit file set configured on the nested task.
         *
         * @return the nested file set
         */
        public FileSet getFileSet() {
            return super.getImplicitFileSet();
        }

        /**
         * Returns a task name scoped to the parent uber-compile task.
         *
         * @return the effective task name
         */
        @Override
        public String getTaskName() {
            return UberCompileTask.this.getTaskName() + ":groovyc";
        }
    }
}
