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
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyProgramElementDoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation for documented program elements with modifiers, package ownership, and annotations.
 */
public class SimpleGroovyProgramElementDoc extends SimpleGroovyDoc implements GroovyProgramElementDoc {
    private static final GroovyAnnotationRef[] EMPTY_GROOVYANNOTATIONREF_ARRAY = new GroovyAnnotationRef[0];
    private GroovyPackageDoc packageDoc;
    private boolean staticElement;
    private boolean finalElement;
    private boolean publicScope;
    private boolean protectedScope;
    private boolean packagePrivateScope;
    private boolean privateScope;
    private final List<GroovyAnnotationRef> annotationRefs;

    /**
     * Creates a documented program element with the supplied name.
     *
     * @param name the element name
     */
    public SimpleGroovyProgramElementDoc(String name) {
        super(name);
        annotationRefs = new ArrayList<GroovyAnnotationRef>();
    }

    /** {@inheritDoc} */
    @Override
    public GroovyPackageDoc containingPackage() {
        return packageDoc;
    }

    /**
     * Associates this element with its containing package.
     *
     * @param packageDoc the containing package
     */
    public void setContainingPackage(GroovyPackageDoc packageDoc) {
        this.packageDoc = packageDoc;
    }

    /**
     * Sets whether this element is static.
     *
     * @param b {@code true} if the element is static
     */
    public void setStatic(boolean b) {
        staticElement = b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStatic() {
        return staticElement;
    }

    /**
     * Sets whether this element is final.
     *
     * @param b {@code true} if the element is final
     */
    public void setFinal(boolean b) {
        this.finalElement = b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinal() {
        return finalElement;
    }

    /**
     * Sets whether this element is public.
     *
     * @param b {@code true} if the element is public
     */
    public void setPublic(boolean b) {
        publicScope = b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPublic() {
        return publicScope;
    }

    /**
     * Sets whether this element is protected.
     *
     * @param b {@code true} if the element is protected
     */
    public void setProtected(boolean b) {
        protectedScope = b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isProtected() {
        return protectedScope;
    }

    /**
     * Sets whether this element has package-private visibility.
     *
     * @param b {@code true} if the element is package-private
     */
    public void setPackagePrivate(boolean b) {
        packagePrivateScope = b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPackagePrivate() {
        return packagePrivateScope;
    }

    /**
     * Sets whether this element is private.
     *
     * @param b {@code true} if the element is private
     */
    public void setPrivate(boolean b) {
        privateScope = b;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPrivate() {
        return privateScope;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(EMPTY_GROOVYANNOTATIONREF_ARRAY);
    }

    /**
     * Adds an annotation declared on this element.
     *
     * @param ref the annotation reference to add
     */
    public void addAnnotationRef(GroovyAnnotationRef ref) {
        annotationRefs.add(ref);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc containingClass() {/*todo*/return null;}

    /** {@inheritDoc} */
    @Override
    public String modifiers() {/*todo*/return null;}

    /** {@inheritDoc} */
    @Override
    public int modifierSpecifier() {/*todo*/return 0;}

    /** {@inheritDoc} */
    @Override
    public String qualifiedName() {/*todo*/return null;}
}
