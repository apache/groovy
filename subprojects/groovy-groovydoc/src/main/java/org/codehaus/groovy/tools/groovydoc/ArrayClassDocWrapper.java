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

    @Override
    public GroovyConstructorDoc[] constructors() {
        return delegate.constructors();
    }

    @Override
    public GroovyConstructorDoc[] constructors(boolean filter) {
        return delegate.constructors(filter);
    }

    @Override
    public boolean definesSerializableFields() {
        return delegate.definesSerializableFields();
    }

    @Override
    public GroovyFieldDoc[] enumConstants() {
        return delegate.enumConstants();
    }

    @Override
    public GroovyFieldDoc[] fields() {
        return delegate.fields();
    }

    @Override
    public GroovyFieldDoc[] properties() {
        return delegate.properties();
    }

    @Override
    public GroovyFieldDoc[] fields(boolean filter) {
        return delegate.fields(filter);
    }

    @Override
    public GroovyClassDoc findClass(String className) {
        return delegate.findClass(className);
    }

    @Override
    public GroovyClassDoc[] importedClasses() {
        return delegate.importedClasses();
    }

    @Override
    public GroovyPackageDoc[] importedPackages() {
        return delegate.importedPackages();
    }

    @Override
    public GroovyClassDoc[] innerClasses() {
        return delegate.innerClasses();
    }

    @Override
    public GroovyClassDoc[] innerClasses(boolean filter) {
        return delegate.innerClasses(filter);
    }

    @Override
    public GroovyClassDoc[] interfaces() {
        return delegate.interfaces();
    }

    @Override
    public GroovyType[] interfaceTypes() {
        return delegate.interfaceTypes();
    }

    @Override
    public boolean isAbstract() {
        return delegate.isAbstract();
    }

    @Override
    public boolean isExternalizable() {
        return delegate.isExternalizable();
    }

    @Override
    public boolean isSerializable() {
        return delegate.isSerializable();
    }

    @Override
    public GroovyMethodDoc[] methods() {
        return delegate.methods();
    }

    @Override
    public GroovyMethodDoc[] methods(boolean filter) {
        return delegate.methods(filter);
    }

    @Override
    public GroovyFieldDoc[] serializableFields() {
        return delegate.serializableFields();
    }

    @Override
    public GroovyMethodDoc[] serializationMethods() {
        return delegate.serializationMethods();
    }

    @Override
    public boolean subclassOf(GroovyClassDoc gcd) {
        return delegate.subclassOf(gcd);
    }

    @Override
    public GroovyClassDoc superclass() {
        return delegate.superclass();
    }

    @Override
    public GroovyType superclassType() {
        return delegate.superclassType();
    }

    @Override
    public String getFullPathName() {
        return delegate.getFullPathName();
    }

    @Override
    public String getRelativeRootPath() {
        return delegate.getRelativeRootPath();
    }

    @Override
    public boolean isPrimitive() {
        return delegate.isPrimitive();
    }

    @Override
    public String qualifiedTypeName() {
        return delegate.qualifiedTypeName();
    }

    @Override
    public String simpleTypeName() {
        return delegate.simpleTypeName();
    }

    @Override
    public String typeName() {
        return delegate.typeName();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public GroovyAnnotationRef[] annotations() {
        return delegate.annotations();
    }

    @Override
    public GroovyClassDoc containingClass() {
        return delegate.containingClass();
    }

    @Override
    public GroovyPackageDoc containingPackage() {
        return delegate.containingPackage();
    }

    @Override
    public boolean isFinal() {
        return delegate.isFinal();
    }

    @Override
    public boolean isPackagePrivate() {
        return delegate.isPackagePrivate();
    }

    @Override
    public boolean isPrivate() {
        return delegate.isPrivate();
    }

    @Override
    public boolean isProtected() {
        return delegate.isProtected();
    }

    @Override
    public boolean isPublic() {
        return delegate.isPublic();
    }

    @Override
    public boolean isStatic() {
        return delegate.isStatic();
    }

    @Override
    public String modifiers() {
        return delegate.modifiers();
    }

    @Override
    public int modifierSpecifier() {
        return delegate.modifierSpecifier();
    }

    @Override
    public String qualifiedName() {
        return delegate.qualifiedName();
    }

    @Override
    public String commentText() {
        return delegate.commentText();
    }

    @Override
    public String getRawCommentText() {
        return delegate.getRawCommentText();
    }

    @Override
    public boolean isAnnotationType() {
        return delegate.isAnnotationType();
    }

    @Override
    public boolean isAnnotationTypeElement() {
        return delegate.isAnnotationTypeElement();
    }

    @Override
    public boolean isClass() {
        return delegate.isClass();
    }

    @Override
    public boolean isConstructor() {
        return delegate.isConstructor();
    }

    @Override
    public boolean isDeprecated() {
        return delegate.isDeprecated();
    }

    @Override
    public boolean isEnum() {
        return delegate.isEnum();
    }

    @Override
    public boolean isEnumConstant() {
        return delegate.isEnumConstant();
    }

    @Override
    public boolean isError() {
        return delegate.isError();
    }

    @Override
    public boolean isException() {
        return delegate.isException();
    }

    @Override
    public boolean isField() {
        return delegate.isField();
    }

    @Override
    public boolean isIncluded() {
        return delegate.isIncluded();
    }

    @Override
    public boolean isInterface() {
        return delegate.isInterface();
    }

    @Override
    public boolean isMethod() {
        return delegate.isMethod();
    }

    @Override
    public boolean isOrdinaryClass() {
        return delegate.isOrdinaryClass();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public void setRawCommentText(String arg0) {
        delegate.setRawCommentText(arg0);
    }

    @Override
    public String firstSentenceCommentText() {
        return delegate.firstSentenceCommentText();
    }

    @Override
    public int compareTo(Object o) {
        return delegate.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ArrayClassDocWrapper ? delegate.equals(((ArrayClassDocWrapper) obj).delegate) : delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }
}
