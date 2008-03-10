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
package org.codehaus.groovy.classgen;

import java.util.Collection;
import java.util.Iterator;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
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

        if (this.currentClass.isAnnotationDefinition()) {

            ErrorCollector errorCollector = new ErrorCollector(this.source.getConfiguration());
            AnnotationVisitor visitor = new AnnotationVisitor(this.source, errorCollector);
            visitor.setReportClass(currentClass);

            visitor.checkReturnType(node.getReturnType(),node);
            
            if (node.getParameters().length>0) {
                addError ("Annotation members may not have parameters.",node.getParameters()[0]);
            }

            if (node.getExceptions().length>0) {
                addError ("Annotation members may not have a throws clause.",node.getExceptions()[0]);
            }
            
            ReturnStatement code = (ReturnStatement) node.getCode();
            if (code!=null) {
                visitor.visitExpression(node.getName(),code.getExpression(),node.getReturnType());
                visitor.checkcircularReference(currentClass,node.getReturnType(),code.getExpression());
            }

            this.source.getErrorCollector().addCollectorContents(errorCollector);
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
        
        Collection annotations = node.getAnnotations();
        for(Iterator it = annotations.iterator(); it.hasNext(); ) {
            AnnotationNode an = (AnnotationNode) it.next();

            AnnotationNode annotation = visitAnnotation(an);
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
