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
package org.codehaus.groovy.ast;

import java.util.*;

/**
 * Base class for any AST node which is capable of being annotated
 *
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
public class AnnotatedNode extends ASTNode {
    private Map annotations = Collections.EMPTY_MAP;
    private Map annotationClasses = Collections.EMPTY_MAP;
    private boolean synthetic;
    ClassNode declaringClass;

    public AnnotatedNode() {
    }

    public Map getAnnotations() {
        return annotations;
    }

    public AnnotationNode getAnnotations(String name) {
        return annotations == Collections.EMPTY_MAP ? null : (AnnotationNode) annotations.get(name);
    }
    
    public ClassNode getAnnotationClass(String name) {
        return annotationClasses == Collections.EMPTY_MAP ? null : (ClassNode) annotationClasses.get(name);
    }

    public void addAnnotation(String name, AnnotationNode value) {
        checkInit();

        annotationClasses.put(name,value.getClassNode());
        AnnotationNode oldValue = (AnnotationNode) annotations.get(name);

        // TODO can we support many annotations of the same name?
        if (oldValue == null) {
            annotations.put(name, value);
        }
        else {
            List list = null;
            if (oldValue instanceof List) {
                list = (List) oldValue;
            }
            else {
                list = new ArrayList();
                list.add(oldValue);
                annotations.put(name, list);
            }
            list.add(value);
        }
    }

    private void checkInit() {
        if (annotations == Collections.EMPTY_MAP)
          annotations = new HashMap();
        if (annotationClasses == Collections.EMPTY_MAP)
          annotationClasses = new HashMap();
    }

    public void addAnnotations(List annotations) {
        for (Iterator iter = annotations.iterator(); iter.hasNext();) {
            AnnotationNode node = (AnnotationNode) iter.next();
            addAnnotation(node.getClassNode().getName(), node);
        }
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public ClassNode getDeclaringClass() {
        return declaringClass;
    }

    /**
     * @param declaringClass The declaringClass to set.
     */
    public void setDeclaringClass(ClassNode declaringClass) {
        this.declaringClass = declaringClass;
    }
}
