/*
 $Id: AnnotationNode.java 10909 2006-12-02 19:31:27Z blackdrag $

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
package org.codehaus.groovy.classgen;

import java.util.Collection;
import java.util.Iterator;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;


/**
 * A specialized Groovy AST visitor meant to perform additional verifications upon the
 * current AST. Currently it does checks on annotated nodes and annotations itself.
 * 
 * Current limitations:
 * - annotations on local variables are not supported
 * 
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 */
public class ExtendedVerifier implements GroovyClassVisitor {
    public static final String JVM_ERROR_MESSAGE = "Please make sure you are running on a JVM >= 1.5";

    private SourceUnit source;
    private ClassNode currentClass;
    
    public ExtendedVerifier(SourceUnit sourceUnit) {
        this.source = sourceUnit;
    }

    public void visitClass(ClassNode node) {
        this.currentClass = node;
        visitAnnotations(node, AnnotationNode.TYPE_TARGET);
        node.visitContents(this);
    }

    public void visitConstructor(ConstructorNode node) {
        visitAnnotations(node, AnnotationNode.CONSTRUCTOR_TARGET);
    }

    public void visitField(FieldNode node) {
        visitAnnotations(node, AnnotationNode.FIELD_TARGET);
    }

    public void visitMethod(MethodNode node) {
        visitAnnotations(node, AnnotationNode.METHOD_TARGET);
        for (int i = 0; i < node.getParameters().length; i++) {
            Parameter parameter = node.getParameters()[i];
            visitAnnotations(parameter, AnnotationNode.PARAMETER_TARGET);
        }
    }

    public void visitProperty(PropertyNode node) {
    }

    protected void visitAnnotations(AnnotatedNode node, int target) {
        if(node.getAnnotations().isEmpty()) {
            return;
        }
        
        this.currentClass.setAnnotated(true);
        
        if(!isAnnotationCompatible()) {
            addError("Annotations are not supported in the current runtime." + JVM_ERROR_MESSAGE,
                    node);
            return;
        }
        
        Collection annotations = node.getAnnotations().values();
        for(Iterator it = annotations.iterator(); it.hasNext(); ) {
            AnnotationNode an = (AnnotationNode) it.next();

            AnnotationNode annotation = visitAnnotation(an);
            if(!annotation.isValid()) {
                return;
            }
            if(!annotation.isTargetAllowed(target)) {
                addError("Annotation @" + annotation.getClassNode().getName()
                        + " is not allowed on element " + AnnotationNode.targetToName(target),
                        annotation);
            }
        }
    }
    
    /**
     * Resolve metadata and details of the annotation.
     */
    private AnnotationNode visitAnnotation(AnnotationNode node) {
        ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
        AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
        AnnotationNode solvedAnnotation = visitor.visit(node);
        this.source.getErrorCollector().addCollectorContents(errorCollector);
        return solvedAnnotation;
    }

    /**
     * Check if the current runtime allows Annotation usage.
     */
    protected boolean isAnnotationCompatible() {
        return CompilerConfiguration.POST_JDK5.equals(this.source.getConfiguration().getTargetBytecode()); 
    }
    
    protected void addError(String msg, ASTNode expr) {
        this.source.getErrorCollector().addErrorAndContinue(
            new SyntaxErrorMessage(
                    new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber()), this.source)
        );
    }

    public void visitGenericType(GenericsType genericsType) {

    }
}
