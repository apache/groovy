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
package org.codehaus.groovy.control;

import groovy.lang.GroovyClassLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

import static java.util.Objects.requireNonNull;

/**
 * A base class for data structures that can collect messages and errors
 * during processing.
 */
public abstract class ProcessingUnit {

    /**
     * The current phase
     */
    protected int phase = Phases.INITIALIZATION;

    /**
     * True if phase is finished
     */
    protected boolean phaseComplete;

    /**
     * Configuration and other settings that control processing
     */
    protected CompilerConfiguration configuration;

    /**
     * The ClassLoader to use during processing
     */
    protected GroovyClassLoader classLoader;

    /**
     * a helper to share errors and report them
     */
    protected ErrorCollector errorCollector;

    /**
     * Initializes the ProcessingUnit to the empty state.
     */
    public ProcessingUnit(final CompilerConfiguration configuration, final GroovyClassLoader classLoader, final ErrorCollector errorCollector) {
        setConfiguration(configuration != null ? configuration : CompilerConfiguration.DEFAULT);
        setClassLoader(classLoader);
        this.errorCollector = errorCollector != null ? errorCollector : new ErrorCollector(getConfiguration());
        configure(getConfiguration());
    }

    /**
     * Reconfigures the ProcessingUnit.
     */
    public void configure(CompilerConfiguration configuration) {
        setConfiguration(configuration);
    }

    /**
     * Gets the CompilerConfiguration for this ProcessingUnit.
     */
    public CompilerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the CompilerConfiguration for this ProcessingUnit.
     */
    public final void setConfiguration(CompilerConfiguration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    /**
     * Returns the class loader in use by this ProcessingUnit.
     */
    public GroovyClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the class loader for use by this ProcessingUnit.
     */
    public void setClassLoader(final GroovyClassLoader loader) {
        // ClassLoaders should only be created inside a doPrivileged block in case
        // this method is invoked by code that does not have security permissions.
        this.classLoader = loader != null ? loader : AccessController.doPrivileged((PrivilegedAction<GroovyClassLoader>) () -> {
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            if (parent == null) {
                parent = this.getClass().getClassLoader();
            }
            return new GroovyClassLoader(parent, getConfiguration());
        });
    }

    /**
     * Errors found during the compilation should be reported through the ErrorCollector.
     */
    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }

    /**
     * Returns the current phase.
     */
    public int getPhase() {
        return phase;
    }

    /**
     * Returns the description for the current phase.
     */
    public String getPhaseDescription() {
        return Phases.getDescription(phase);
    }

    public boolean isPhaseComplete() {
        return phaseComplete;
    }

    /**
     * Marks the current phase complete and processes any errors.
     */
    public void completePhase() throws CompilationFailedException {
        errorCollector.failIfErrors();
        phaseComplete = true;
    }

    /**
     * A synonym for <code>gotoPhase(getPhase() + 1)</code>.
     */
    public void nextPhase() throws CompilationFailedException {
        gotoPhase(phase + 1);
    }

    /**
     * Wraps up any pending operations for the current phase and switches to the given phase.
     */
    public void gotoPhase(int phase) throws CompilationFailedException {
        if (!phaseComplete) {
            completePhase();
        }
        this.phase = phase;
        phaseComplete = false;
    }
}
