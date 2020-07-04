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

    public SourceAwareCustomizer(CompilationCustomizer delegate) {
        super(delegate);
    }

    @Override
    public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
        String fileName = source.getName();
        ReaderSource reader = source.getSource();
        if (reader instanceof FileReaderSource) {
            FileReaderSource file = (FileReaderSource) reader;
            fileName = file.getFile().getName();
        }
        if (acceptSource(source) && acceptClass(classNode) && accept(fileName)) {
            delegate.call(source, context, classNode);
        }
    }

    public void setBaseNameValidator(final Closure<Boolean> baseNameValidator) {
        this.baseNameValidator = baseNameValidator;
    }

    public void setExtensionValidator(final Closure<Boolean> extensionValidator) {
        this.extensionValidator = extensionValidator;
    }

    public void setSourceUnitValidator(final Closure<Boolean> sourceUnitValidator) {
        this.sourceUnitValidator = sourceUnitValidator;
    }

    public void setClassValidator(final Closure<Boolean> classValidator) {
        this.classValidator = classValidator;
    }

    public boolean accept(String fileName) {
        int ext = fileName.lastIndexOf('.');
        String baseName = ext<0?fileName:fileName.substring(0, ext);
        String extension = ext<0?"":fileName.substring(ext+1);
        return acceptExtension(extension) && acceptBaseName(baseName);
    }

    public boolean acceptClass(ClassNode cnode) {
        return classValidator == null || classValidator.call(cnode);
    }

    public boolean acceptSource(SourceUnit unit) {
        return sourceUnitValidator==null || sourceUnitValidator.call(unit);
    }

    public boolean acceptExtension(String extension) {
        return extensionValidator==null || extensionValidator.call(extension);
    }

    public boolean acceptBaseName(String baseName) {
        return baseNameValidator==null || baseNameValidator.call(baseName);
    }
}
