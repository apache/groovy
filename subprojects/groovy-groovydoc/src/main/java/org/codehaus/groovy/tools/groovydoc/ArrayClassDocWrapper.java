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
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyType;

public class ArrayClassDocWrapper implements GroovyClassDoc {

    private final GroovyClassDoc delegate;

    public ArrayClassDocWrapper(GroovyClassDoc delegate) {
        this.delegate = delegate;
    }

    public GroovyClassDoc getDelegate() {
        return delegate;
    }

    public GroovyConstructorDoc[] constructors() {
        return delegate.constructors();
    }

    public GroovyConstructorDoc[] constructors(boolean filter) {
        return delegate.constructors(filter);
    }

    public boolean definesSerializableFields() {
        return delegate.definesSerializableFields();
    }

    public GroovyFieldDoc[] enumConstants() {
        return delegate.enumConstants();
    }

    public GroovyFieldDoc[] fields() {
        return delegate.fields();
    }

    public GroovyFieldDoc[] properties() {
        return delegate.properties();
    }

    public GroovyFieldDoc[] fields(boolean filter) {
        return delegate.fields(filter);
    }

    public GroovyClassDoc findClass(String className) {
        return delegate.findClass(className);
    }

    public GroovyClassDoc[] importedClasses() {
        return delegate.importedClasses();
    }

    public GroovyPackageDoc[] importedPackages() {
        return delegate.importedPackages();
    }

    public GroovyClassDoc[] innerClasses() {
        return delegate.innerClasses();
    }

    public GroovyClassDoc[] innerClasses(boolean filter) {
        return delegate.innerClasses(filter);
    }

    public GroovyClassDoc[] interfaces() {
        return delegate.interfaces();
    }

    public GroovyType[] interfaceTypes() {
        return delegate.interfaceTypes();
    }

    public boolean isAbstract() {
        return delegate.isAbstract();
    }

    public boolean isExternalizable() {
        return delegate.isExternalizable();
    }

    public boolean isSerializable() {
        return delegate.isSerializable();
    }

    public GroovyMethodDoc[] methods() {
        return delegate.methods();
    }

    public GroovyMethodDoc[] methods(boolean filter) {
        return delegate.methods(filter);
    }

    public GroovyFieldDoc[] serializableFields() {
        return delegate.serializableFields();
    }

    public GroovyMethodDoc[] serializationMethods() {
        return delegate.serializationMethods();
    }

    public boolean subclassOf(GroovyClassDoc gcd) {
        return delegate.subclassOf(gcd);
    }

    public GroovyClassDoc superclass() {
        return delegate.superclass();
    }

    public GroovyType superclassType() {
        return delegate.superclassType();
    }

    public String getFullPathName() {
        return delegate.getFullPathName();
    }

    public String getRelativeRootPath() {
        return delegate.getRelativeRootPath();
    }

    public boolean isPrimitive() {
        return delegate.isPrimitive();
    }

    public String qualifiedTypeName() {
        return delegate.qualifiedTypeName();
    }

    public String simpleTypeName() {
        return delegate.simpleTypeName();
    }

    public String typeName() {
        return delegate.typeName();
    }

    public String toString() {
        return delegate.toString();
    }

    public GroovyAnnotationRef[] annotations() {
        return delegate.annotations();
    }

    public GroovyClassDoc containingClass() {
        return delegate.containingClass();
    }

    public GroovyPackageDoc containingPackage() {
        return delegate.containingPackage();
    }

    public boolean isFinal() {
        return delegate.isFinal();
    }

    public boolean isPackagePrivate() {
        return delegate.isPackagePrivate();
    }

    public boolean isPrivate() {
        return delegate.isPrivate();
    }

    public boolean isProtected() {
        return delegate.isProtected();
    }

    public boolean isPublic() {
        return delegate.isPublic();
    }

    public boolean isStatic() {
        return delegate.isStatic();
    }

    public String modifiers() {
        return delegate.modifiers();
    }

    public int modifierSpecifier() {
        return delegate.modifierSpecifier();
    }

    public String qualifiedName() {
        return delegate.qualifiedName();
    }

    public String commentText() {
        return delegate.commentText();
    }

    public String getRawCommentText() {
        return delegate.getRawCommentText();
    }

    public boolean isAnnotationType() {
        return delegate.isAnnotationType();
    }

    public boolean isAnnotationTypeElement() {
        return delegate.isAnnotationTypeElement();
    }

    public boolean isClass() {
        return delegate.isClass();
    }

    public boolean isConstructor() {
        return delegate.isConstructor();
    }

    public boolean isDeprecated() {
        return delegate.isDeprecated();
    }

    public boolean isEnum() {
        return delegate.isEnum();
    }

    public boolean isEnumConstant() {
        return delegate.isEnumConstant();
    }

    public boolean isError() {
        return delegate.isError();
    }

    public boolean isException() {
        return delegate.isException();
    }

    public boolean isField() {
        return delegate.isField();
    }

    public boolean isIncluded() {
        return delegate.isIncluded();
    }

    public boolean isInterface() {
        return delegate.isInterface();
    }

    public boolean isMethod() {
        return delegate.isMethod();
    }

    public boolean isOrdinaryClass() {
        return delegate.isOrdinaryClass();
    }

    public String name() {
        return delegate.name();
    }

    public void setRawCommentText(String arg0) {
        delegate.setRawCommentText(arg0);
    }

    public String firstSentenceCommentText() {
        return delegate.firstSentenceCommentText();
    }

    public int compareTo(Object o) {
        return delegate.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayClassDocWrapper ? delegate.equals(((ArrayClassDocWrapper) obj).delegate) : delegate.equals(obj);
    }
}
