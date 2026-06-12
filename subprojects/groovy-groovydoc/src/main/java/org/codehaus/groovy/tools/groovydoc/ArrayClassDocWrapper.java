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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyConstructorDoc;
import org.codehaus.groovy.groovydoc.GroovyDoc;
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyType;

/**
 * A {@link GroovyClassDoc} decorator that wraps a component-type class doc for array-type
 * representations. All {@link GroovyClassDoc} method calls are delegated to the wrapped instance.
 */
public class ArrayClassDocWrapper implements GroovyClassDoc {

    private final GroovyClassDoc delegate;

    /**
     * Creates a wrapper for the given component-type class doc.
     *
     * @param delegate the non-array class doc to wrap
     */
    public ArrayClassDocWrapper(GroovyClassDoc delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the wrapped component-type class doc.
     */
    public GroovyClassDoc getDelegate() {
        return delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyConstructorDoc[] constructors() {
        return delegate.constructors();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyConstructorDoc[] constructors(boolean filter) {
        return delegate.constructors(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean definesSerializableFields() {
        return delegate.definesSerializableFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] enumConstants() {
        return delegate.enumConstants();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] fields() {
        return delegate.fields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] properties() {
        return delegate.properties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] fields(boolean filter) {
        return delegate.fields(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc findClass(String className) {
        return delegate.findClass(className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] importedClasses() {
        return delegate.importedClasses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyPackageDoc[] importedPackages() {
        return delegate.importedPackages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] innerClasses() {
        return delegate.innerClasses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] innerClasses(boolean filter) {
        return delegate.innerClasses(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] interfaces() {
        return delegate.interfaces();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyType[] interfaceTypes() {
        return delegate.interfaceTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbstract() {
        return delegate.isAbstract();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExternalizable() {
        return delegate.isExternalizable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSerializable() {
        return delegate.isSerializable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyMethodDoc[] methods() {
        return delegate.methods();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyMethodDoc[] methods(boolean filter) {
        return delegate.methods(filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] serializableFields() {
        return delegate.serializableFields();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyMethodDoc[] serializationMethods() {
        return delegate.serializationMethods();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean subclassOf(GroovyClassDoc gcd) {
        return delegate.subclassOf(gcd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc superclass() {
        return delegate.superclass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyType superclassType() {
        return delegate.superclassType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullPathName() {
        return delegate.getFullPathName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRelativeRootPath() {
        return delegate.getRelativeRootPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrimitive() {
        return delegate.isPrimitive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String qualifiedTypeName() {
        return delegate.qualifiedTypeName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String simpleTypeName() {
        return delegate.simpleTypeName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String typeName() {
        return delegate.typeName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyAnnotationRef[] annotations() {
        return delegate.annotations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc containingClass() {
        return delegate.containingClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyPackageDoc containingPackage() {
        return delegate.containingPackage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinal() {
        return delegate.isFinal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPackagePrivate() {
        return delegate.isPackagePrivate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivate() {
        return delegate.isPrivate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProtected() {
        return delegate.isProtected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublic() {
        return delegate.isPublic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatic() {
        return delegate.isStatic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String modifiers() {
        return delegate.modifiers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int modifierSpecifier() {
        return delegate.modifierSpecifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String qualifiedName() {
        return delegate.qualifiedName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String commentText() {
        return delegate.commentText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRawCommentText() {
        return delegate.getRawCommentText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnnotationType() {
        return delegate.isAnnotationType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnnotationTypeElement() {
        return delegate.isAnnotationTypeElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClass() {
        return delegate.isClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConstructor() {
        return delegate.isConstructor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeprecated() {
        return delegate.isDeprecated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnum() {
        return delegate.isEnum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnumConstant() {
        return delegate.isEnumConstant();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isError() {
        return delegate.isError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecord() {
        return delegate.isRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isException() {
        return delegate.isException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isField() {
        return delegate.isField();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIncluded() {
        return delegate.isIncluded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInterface() {
        return delegate.isInterface();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMethod() {
        return delegate.isMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOrdinaryClass() {
        return delegate.isOrdinaryClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return delegate.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRawCommentText(String arg0) {
        delegate.setRawCommentText(arg0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String firstSentenceCommentText() {
        return delegate.firstSentenceCommentText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(GroovyDoc o) {
        return delegate.compareTo(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayClassDocWrapper ? delegate.equals(((ArrayClassDocWrapper) obj).delegate) : delegate.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}
