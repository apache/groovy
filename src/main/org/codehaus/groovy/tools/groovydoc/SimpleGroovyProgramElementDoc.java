/*
 * Copyright 2003-2007 the original author or authors.
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

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyProgramElementDoc;

import java.util.ArrayList;
import java.util.List;

public class SimpleGroovyProgramElementDoc extends SimpleGroovyDoc implements GroovyProgramElementDoc {
	private GroovyPackageDoc packageDoc;
    private boolean publicElement;
    private boolean staticElement;
    private boolean finalElement;
    private boolean privateElement;
    private boolean protectedElement;
    private final List<GroovyAnnotationRef> annotationRefs;

    public SimpleGroovyProgramElementDoc(String name) {
        super(name);
        annotationRefs = new ArrayList<GroovyAnnotationRef>();
    }

    public GroovyPackageDoc containingPackage() {
        return packageDoc;
    }

    public void setContainingPackage(GroovyPackageDoc packageDoc) {
        this.packageDoc = packageDoc;
    }

    public void setPublic(boolean b) {
        publicElement = b;
    }

    public boolean isPublic() {
        return publicElement;
    }

    public void setStatic(boolean b) {
        staticElement = b;
    }

    public boolean isStatic() {
        return staticElement;
    }

    public boolean isFinal() {
        return finalElement;
    }

    public void setFinal(boolean b) {
        this.finalElement = b;
    }

    public void setPrivate(boolean b) {
        privateElement = b;
    }

    public void setProtected(boolean b) {
        protectedElement = b;
    }

    public boolean isPrivate() {
        return privateElement;
    }

    public boolean isProtected() {
        return protectedElement;
    }

    public GroovyAnnotationRef[] annotations() {
        return annotationRefs.toArray(new GroovyAnnotationRef[annotationRefs.size()]);
    }

    public void addAnnotationRef(GroovyAnnotationRef ref) {
        annotationRefs.add(ref);
    }

	public GroovyClassDoc containingClass() {/*todo*/return null;}

    public boolean isPackagePrivate() {/*todo*/return false;}

    public String modifiers() {/*todo*/return null;}

    public int modifierSpecifier() {/*todo*/return 0;}

    public String qualifiedName() {/*todo*/return null;}
}
