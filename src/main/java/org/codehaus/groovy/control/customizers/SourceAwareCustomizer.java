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
package org.codehaus.groovy.control.customizers;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.FileReaderSource;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * A base class for customizers which only have to be applied on specific source units.
 * This is for example useful if you want a customizer to be applied only for files
 * matching some extensions.
 * <p>
 * For convenience, this class implements several methods that you may extend to customize
 * the behaviour of this utility. For example, if you want to apply a customizer only
 * for classes matching the '.foo' file extension, then you only have to override the
 * {@link #acceptExtension(String)} method:
 * <pre><code>return "foo".equals(extension)</code></pre>
 *
 * @since 2.1.0
 */
public class SourceAwareCustomizer extends DelegatingCustomizer {

    private Closure<Boolean> extensionValidator;
    private Closure<Boolean> baseNameValidator;
    private Closure<Boolean> sourceUnitValidator;
    private Closure<Boolean> classValidator;

    /**
     * Creates a source-aware wrapper around another compilation customizer.
     *
     * @param delegate the customizer to invoke when the source matches
     */
    public SourceAwareCustomizer(CompilationCustomizer delegate) {
        super(delegate);
    }

    /**
     * Invokes the delegate only when the source and class validators accept the current input.
     *
     * @param source the source unit being customized
     * @param context the current generator context
     * @param classNode the class node being customized
     * @throws CompilationFailedException if the delegate fails
     */
    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
        String fileName = source.getName();
        ReaderSource reader = source.getSource();
        if (reader instanceof FileReaderSource file) {
            fileName = file.getFile().getName();
        }
        if (acceptSource(source) && acceptClass(classNode) && accept(fileName)) {
            delegate.call(source, context, classNode);
        }
    }

    /**
     * Sets the predicate used to validate source base names.
     *
     * @param baseNameValidator the validator to use
     */
    public void setBaseNameValidator(final Closure<Boolean> baseNameValidator) {
        this.baseNameValidator = baseNameValidator;
    }

    /**
     * Sets the predicate used to validate source file extensions.
     *
     * @param extensionValidator the validator to use
     */
    public void setExtensionValidator(final Closure<Boolean> extensionValidator) {
        this.extensionValidator = extensionValidator;
    }

    /**
     * Sets the predicate used to validate whole source units.
     *
     * @param sourceUnitValidator the validator to use
     */
    public void setSourceUnitValidator(final Closure<Boolean> sourceUnitValidator) {
        this.sourceUnitValidator = sourceUnitValidator;
    }

    /**
     * Sets the predicate used to validate class nodes.
     *
     * @param classValidator the validator to use
     */
    public void setClassValidator(final Closure<Boolean> classValidator) {
        this.classValidator = classValidator;
    }

    /**
     * Checks whether a source file name is accepted by the base-name and extension validators.
     *
     * @param fileName the file name to inspect
     * @return {@code true} if the file name is accepted
     */
    public boolean accept(String fileName) {
        int ext = fileName.lastIndexOf('.');
        String baseName = ext<0?fileName:fileName.substring(0, ext);
        String extension = ext<0?"":fileName.substring(ext+1);
        return acceptExtension(extension) && acceptBaseName(baseName);
    }

    /**
     * Checks whether a class node is accepted.
     *
     * @param cnode the class node to inspect
     * @return {@code true} if the class is accepted
     */
    public boolean acceptClass(ClassNode cnode) {
        return classValidator == null || classValidator.call(cnode);
    }

    /**
     * Checks whether a source unit is accepted.
     *
     * @param unit the source unit to inspect
     * @return {@code true} if the source is accepted
     */
    public boolean acceptSource(SourceUnit unit) {
        return sourceUnitValidator==null || sourceUnitValidator.call(unit);
    }

    /**
     * Checks whether a file extension is accepted.
     *
     * @param extension the extension to inspect
     * @return {@code true} if the extension is accepted
     */
    public boolean acceptExtension(String extension) {
        return extensionValidator==null || extensionValidator.call(extension);
    }

    /**
     * Checks whether a base file name is accepted.
     *
     * @param baseName the base file name to inspect
     * @return {@code true} if the base name is accepted
     */
    public boolean acceptBaseName(String baseName) {
        return baseNameValidator==null || baseNameValidator.call(baseName);
    }
}
