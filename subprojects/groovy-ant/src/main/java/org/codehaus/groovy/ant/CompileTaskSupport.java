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
import org.apache.groovy.io.StringBuilderWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.ErrorReporter;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Support for compilation related tasks.
 */
public abstract class CompileTaskSupport
    extends MatchingTask
{
    /**
     * Logger used by subclasses to report task progress.
     */
    protected final LoggingHelper log = new LoggingHelper(this);

    /**
     * Source roots to scan for compilation inputs.
     */
    protected Path src;

    /**
     * Destination directory for generated output.
     */
    protected File destdir;

    /**
     * Compilation classpath.
     */
    protected Path classpath;

    /**
     * Compiler configuration shared with the concrete task implementation.
     */
    protected CompilerConfiguration config = new CompilerConfiguration();

    /**
     * Whether task failures should stop the Ant build.
     */
    protected boolean failOnError = true;

    /**
     * Controls whether compilation errors should fail the Ant build.
     *
     * @param fail {@code true} to fail the build on compilation errors
     */
    public void setFailonerror(final boolean fail) {
        failOnError = fail;
    }

    /**
     * Indicates whether compilation errors fail the Ant build.
     *
     * @return {@code true} if failures abort the build
     */
    public boolean getFailonerror() {
        return failOnError;
    }

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
     * Sets the output directory for generated classes or stubs.
     *
     * @param dir the destination directory
     */
    public void setDestdir(final File dir) {
        assert dir != null;

        this.destdir = dir;
    }

    /**
     * Adds entries to the compilation classpath.
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
     * Returns the configured compilation classpath.
     *
     * @return the compilation classpath
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
     * Returns the mutable compiler configuration used by this task.
     *
     * @return the compiler configuration
     */
    public CompilerConfiguration createConfiguration() {
        return config;
    }

    /**
     * Validates the task configuration before compilation starts.
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
     * Creates a class loader for the current compiler configuration and classpath.
     *
     * @return the class loader to use during compilation
     */
    protected GroovyClassLoader createClassLoader() {
        GroovyClassLoader gcl = new GroovyClassLoader(ClassLoader.getSystemClassLoader(), config);

        Path path = getClasspath();
        if (path != null) {
            final String[] filePaths = path.list();
            for (String filePath : filePaths) {
                gcl.addClasspath(filePath);
            }
        }

        return gcl;
    }

    /**
     * Converts a compilation failure into the Ant task's configured error handling.
     *
     * @param e the exception raised by compilation
     * @throws BuildException if failures should abort the build
     */
    protected void handleException(final Exception e) throws BuildException {
        assert e != null;

        Writer writer = new StringBuilderWriter();
        new ErrorReporter(e, false).write(new PrintWriter(writer));
        String message = writer.toString();

        if (failOnError) {
            throw new BuildException(message, e, getLocation());
        }
        else {
            log.error(message);
        }
    }

    /**
     * Validates the task and delegates to {@link #compile()}.
     *
     * @throws BuildException if validation fails or compilation fails fatally
     */
    @Override
    public void execute() throws BuildException {
        validate();

        try {
            compile();
        }
        catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Performs the concrete compilation work for the task.
     *
     * @throws Exception if compilation fails
     */
    protected abstract void compile() throws Exception;
}
