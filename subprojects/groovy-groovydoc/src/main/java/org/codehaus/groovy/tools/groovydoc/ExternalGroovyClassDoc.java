/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class not in the codebase being processed.
 */
public class ExternalGroovyClassDoc implements GroovyClassDoc {
    private Class externalClass;
    private final List<GroovyAnnotationRef> annotationRefs;

    public ExternalGroovyClassDoc(Class externalClass) {
        this.externalClass = externalClass;
        annotationRefs = new ArrayList<GroovyAnnotationRef>();
    }

    public boolean isPrimitive() {
        return externalClass.isPrimitive();
    }

    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(new GroovyAnnotationRef[annotationRefs.size()]);
    }

    public String qualifiedTypeName() {
        return externalClass.getName();
    }

    public GroovyClassDoc superclass() {
        Class aClass = externalClass.getSuperclass();
        if (aClass != null) return new ExternalGroovyClassDoc(aClass);
        return new ExternalGroovyClassDoc(Object.class);
    }

    public Class externalClass() {
        return externalClass;
    }

    public String getTypeSourceDescription() {
        return externalClass.isInterface() ? "interface" : "class";
    }

    public String simpleTypeName() {
        return qualifiedTypeName(); // TODO fix
    }

    public String typeName() {
        return qualifiedTypeName(); // TODO fix
    }

    @Override
    public int hashCode() {
        return qualifiedTypeName().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof ExternalGroovyClassDoc)) return false;
        return qualifiedTypeName().equals(((ExternalGroovyClassDoc)other).qualifiedTypeName());
    }

    // TODO implement below if/when needed

    public GroovyType superclassType() {
        return null;
    }

    public GroovyConstructorDoc[] constructors() {
        return new GroovyConstructorDoc[0];
    }

    public GroovyConstructorDoc[] constructors(boolean filter) {
        return new GroovyConstructorDoc[0];
    }

    public boolean definesSerializableFields() {
        return false;
    }

    public GroovyFieldDoc[] enumConstants() {
        return new GroovyFieldDoc[0];
    }

    public GroovyFieldDoc[] fields() {
        return new GroovyFieldDoc[0];
    }

    public GroovyFieldDoc[] properties() {
        return new GroovyFieldDoc[0];
    }

    public GroovyFieldDoc[] fields(boolean filter) {
        return new GroovyFieldDoc[0];
    }

    public GroovyClassDoc findClass(String className) {
        return null;
    }

    public GroovyClassDoc[] importedClasses() {
        return new GroovyClassDoc[0];
    }

    public GroovyPackageDoc[] importedPackages() {
        return new GroovyPackageDoc[0];
    }

    public GroovyClassDoc[] innerClasses() {
        return new GroovyClassDoc[0];
    }

    public GroovyClassDoc[] innerClasses(boolean filter) {
        return new GroovyClassDoc[0];
    }

    public GroovyClassDoc[] interfaces() {
        return new GroovyClassDoc[0];
    }

    public GroovyType[] interfaceTypes() {
        return new GroovyType[0];
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isExternalizable() {
        return false;
    }

    public boolean isSerializable() {
        return false;
    }

    public GroovyMethodDoc[] methods() {
        return new GroovyMethodDoc[0];
    }

    public GroovyMethodDoc[] methods(boolean filter) {
        return new GroovyMethodDoc[0];
    }

    public GroovyFieldDoc[] serializableFields() {
        return new GroovyFieldDoc[0];
    }

    public GroovyMethodDoc[] serializationMethods() {
        return new GroovyMethodDoc[0];
    }

    public boolean subclassOf(GroovyClassDoc gcd) {
        return false;
    }

    public String getFullPathName() {
        return null;
    }

    public String getRelativeRootPath() {
        return null;
    }

    public GroovyClassDoc containingClass() {
        return null;
    }

    public GroovyPackageDoc containingPackage() {
        return null;
    }

    public boolean isFinal() {
        return false;
    }

    public boolean isPackagePrivate() {
        return false;
    }

    public boolean isPrivate() {
        return false;
    }

    public boolean isProtected() {
        return false;
    }

    public boolean isPublic() {
        return false;
    }

    public boolean isStatic() {
        return false;
    }

    public String modifiers() {
        return null;
    }

    public int modifierSpecifier() {
        return 0;
    }

    public String qualifiedName() {
        return null;
    }

    public String commentText() {
        return null;
    }

    public String getRawCommentText() {
        return null;
    }

    public boolean isAnnotationType() {
        return false;
    }

    public boolean isAnnotationTypeElement() {
        return false;
    }

    public boolean isClass() {
        return false;
    }

    public boolean isConstructor() {
        return false;
    }

    public boolean isDeprecated() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isEnumConstant() {
        return false;
    }

    public boolean isError() {
        return false;
    }

    public boolean isException() {
        return false;
    }

    public boolean isField() {
        return false;
    }

    public boolean isIncluded() {
        return false;
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isMethod() {
        return false;
    }

    public boolean isOrdinaryClass() {
        return false;
    }

    public String name() {
        return externalClass.getSimpleName();
    }

    public void setRawCommentText(String arg0) {

    }

    public String firstSentenceCommentText() {
        return null;
    }

    public int compareTo(Object o) {
        return 0;
    }
}
