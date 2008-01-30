/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.vmplugin.v5;

import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.GroovyASTMacro;
import org.codehaus.groovy.ast.ASTAnnotationMacro;
import org.codehaus.groovy.ast.ClassNode;

import java.util.Iterator;
import java.util.Map;
import java.util.Collection;

/**
 * @author Danno Ferrin (shemnon)
 */
public class ASTAnnotationMacroCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private Map<Integer, ASTAnnotationMacroCodeVisitor> stageVisitors;

    public ASTAnnotationMacroCollectorCodeVisitor(Map<Integer, ASTAnnotationMacroCodeVisitor> stageVisitors) {
        this.stageVisitors = stageVisitors;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations().values()) {
            Class annotationType = annotation.getClassNode().getTypeClass();
            GroovyASTMacro macroAnnotation = (GroovyASTMacro) annotationType.getAnnotation(GroovyASTMacro.class);
            try {
                stageVisitors.get(macroAnnotation.phase())
                    .addAnnotation(
                        annotationType.getName(),
                        (ASTAnnotationMacro) macroAnnotation.macroClass().newInstance());
            } catch (InstantiationException e) {
                source.getErrorCollector().addError(
                    new SimpleMessage(
                        "Could not instantiate Macro Processor " + macroAnnotation.macroClass().getName(),
                        source));
            } catch (IllegalAccessException e) {
                source.getErrorCollector().addError(
                    new SimpleMessage(
                        "Could not instantiate Macro Processor " + macroAnnotation.macroClass().getName(),
                        source));
            }
        }
    }

    public CompilationUnit.PrimaryClassNodeOperation getOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTAnnotationMacroCollectorCodeVisitor.this.source = source;
                visitClass(classNode);
            }
        };
    }
}
