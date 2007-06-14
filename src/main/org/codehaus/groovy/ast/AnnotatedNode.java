/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for any AST node which is capable of being annotated
 *
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @version $Revision$
 */
public class AnnotatedNode extends ASTNode {
    private Map annotations = new HashMap();
    private Map annotationClasses = new HashMap();
    private boolean synthetic;
    ClassNode declaringClass;

    public AnnotatedNode() {
    }

    public Map getAnnotations() {
        return annotations;
    }

    public AnnotationNode getAnnotations(String name) {
        return (AnnotationNode) annotations.get(name);
    }
    
    public ClassNode getAnnotationClass(String name) {
        return (ClassNode) annotationClasses.get(name);
    }

    public void addAnnotation(String name, AnnotationNode value) {
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
