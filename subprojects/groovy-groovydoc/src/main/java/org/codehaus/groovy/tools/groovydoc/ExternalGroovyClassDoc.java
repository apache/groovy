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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a class not in the codebase being processed.
 */
public class ExternalGroovyClassDoc implements GroovyClassDoc {
    private static final GroovyAnnotationRef[] EMPTY_GROOVYANNOTATIONREF_ARRAY = new GroovyAnnotationRef[0];
    private static final GroovyConstructorDoc[] EMPTY_GROOVYCONSTRUCTORDOC_ARRAY = new GroovyConstructorDoc[0];
    private static final GroovyFieldDoc[] EMPTY_GROOVYFIELDDOC_ARRAY = new GroovyFieldDoc[0];
    private static final GroovyClassDoc[] EMPTY_GROOVYCLASSDOC_ARRAY = new GroovyClassDoc[0];
    private static final GroovyPackageDoc[] EMPTY_GROOVYPACKAGEDOC_ARRAY = new GroovyPackageDoc[0];
    private static final GroovyMethodDoc[] EMPTY_GROOVYMETHODDOC_ARRAY = new GroovyMethodDoc[0];
    private static final GroovyType[] EMPTY_GROOVYTYPE_ARRAY = new GroovyType[0];
    private final Class<?> externalClass;
    private final List<GroovyAnnotationRef> annotationRefs;

    /**
     * Creates a doc entry for the given external (non-source) class.
     *
     * @param externalClass the reflected class to represent
     */
    public ExternalGroovyClassDoc(Class<?> externalClass) {
        this.externalClass = externalClass;
        annotationRefs = new ArrayList<GroovyAnnotationRef>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrimitive() {
        return externalClass.isPrimitive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(EMPTY_GROOVYANNOTATIONREF_ARRAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String qualifiedTypeName() {
        String canonicalName = externalClass.getCanonicalName();
        return canonicalName != null ? canonicalName : externalClass.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc superclass() {
        Class<?> aClass = externalClass.getSuperclass();
        return aClass != null ? new ExternalGroovyClassDoc(aClass) : null;
    }

    /**
     * Returns the underlying reflected class.
     */
    public Class<?> externalClass() {
        return externalClass;
    }

    /**
     * Returns {@code "interface"} if the external class is an interface; otherwise {@code "class"}.
     */
    public String getTypeSourceDescription() {
        return externalClass.isInterface() ? "interface" : "class";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String simpleTypeName() {
        String simpleName = externalClass.getSimpleName();
        if (!simpleName.isEmpty()) return simpleName;
        String qualifiedName = qualifiedTypeName();
        int lastDot = qualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? qualifiedName.substring(lastDot + 1) : qualifiedName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String typeName() {
        return qualifiedTypeName(); // TODO fix
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return qualifiedTypeName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (!(other instanceof ExternalGroovyClassDoc)) return false;
        return qualifiedTypeName().equals(((ExternalGroovyClassDoc)other).qualifiedTypeName());
    }

    // TODO implement below if/when needed

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyType superclassType() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyConstructorDoc[] constructors() {
        return EMPTY_GROOVYCONSTRUCTORDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyConstructorDoc[] constructors(boolean filter) {
        return EMPTY_GROOVYCONSTRUCTORDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean definesSerializableFields() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] enumConstants() {
        return EMPTY_GROOVYFIELDDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] fields() {
        return EMPTY_GROOVYFIELDDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] properties() {
        return EMPTY_GROOVYFIELDDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] fields(boolean filter) {
        return EMPTY_GROOVYFIELDDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc findClass(String className) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] importedClasses() {
        return EMPTY_GROOVYCLASSDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyPackageDoc[] importedPackages() {
        return EMPTY_GROOVYPACKAGEDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] innerClasses() {
        return EMPTY_GROOVYCLASSDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] innerClasses(boolean filter) {
        return EMPTY_GROOVYCLASSDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc[] interfaces() {
        Class<?>[] interfaces = externalClass.getInterfaces();
        if (interfaces.length == 0) return EMPTY_GROOVYCLASSDOC_ARRAY;

        GroovyClassDoc[] result = new GroovyClassDoc[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            result[i] = new ExternalGroovyClassDoc(interfaces[i]);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyType[] interfaceTypes() {
        return EMPTY_GROOVYTYPE_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(externalClass.getModifiers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExternalizable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSerializable() {
        return java.io.Serializable.class.isAssignableFrom(externalClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyMethodDoc[] methods() {
        return ExternalJavadocSupport.methodsFor(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyMethodDoc[] methods(boolean filter) {
        return methods();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyFieldDoc[] serializableFields() {
        return EMPTY_GROOVYFIELDDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyMethodDoc[] serializationMethods() {
        return EMPTY_GROOVYMETHODDOC_ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean subclassOf(GroovyClassDoc gcd) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullPathName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRelativeRootPath() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyClassDoc containingClass() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroovyPackageDoc containingPackage() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFinal() {
        return Modifier.isFinal(externalClass.getModifiers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPackagePrivate() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(externalClass.getModifiers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(externalClass.getModifiers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPublic() {
        return Modifier.isPublic(externalClass.getModifiers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStatic() {
        return Modifier.isStatic(externalClass.getModifiers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String modifiers() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int modifierSpecifier() {
        return externalClass.getModifiers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String qualifiedName() {
        return externalClass.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String commentText() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRawCommentText() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnnotationType() {
        return externalClass.isAnnotation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnnotationTypeElement() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClass() {
        return !externalClass.isInterface() && !externalClass.isAnnotation() && !externalClass.isEnum() && !externalClass.isRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConstructor() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeprecated() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnum() {
        return externalClass.isEnum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecord() {
        return externalClass.isRecord();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnumConstant() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isError() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isException() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isField() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIncluded() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInterface() {
        return externalClass.isInterface();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMethod() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOrdinaryClass() {
        return isClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return externalClass.getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRawCommentText(String arg0) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String firstSentenceCommentText() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(GroovyDoc o) {
        return 0;
    }
}
