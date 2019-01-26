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

    public SimpleGroovyProgramElementDoc(String name) {
        super(name);
        annotationRefs = new ArrayList<>();
    }

    public GroovyPackageDoc containingPackage() {
        return packageDoc;
    }

    public void setContainingPackage(GroovyPackageDoc packageDoc) {
        this.packageDoc = packageDoc;
    }

    public void setStatic(boolean b) {
        staticElement = b;
    }

    public boolean isStatic() {
        return staticElement;
    }

    public void setFinal(boolean b) {
        this.finalElement = b;
    }

    public boolean isFinal() {
        return finalElement;
    }

    public void setPublic(boolean b) {
        publicScope = b;
    }

    public boolean isPublic() {
        return publicScope;
    }

    public void setProtected(boolean b) {
        protectedScope = b;
    }

    public boolean isProtected() {
        return protectedScope;
    }

    public void setPackagePrivate(boolean b) {
        packagePrivateScope = b;
    }

    public boolean isPackagePrivate() {
        return packagePrivateScope;
    }

    public void setPrivate(boolean b) {
        privateScope = b;
    }

    public boolean isPrivate() {
        return privateScope;
    }

    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(EMPTY_GROOVYANNOTATIONREF_ARRAY);
    }

    public void addAnnotationRef(GroovyAnnotationRef ref) {
        annotationRefs.add(ref);
    }

    public GroovyClassDoc containingClass() {/*todo*/return null;}

    public String modifiers() {/*todo*/return null;}

    public int modifierSpecifier() {/*todo*/return 0;}

    public String qualifiedName() {/*todo*/return null;}
}
