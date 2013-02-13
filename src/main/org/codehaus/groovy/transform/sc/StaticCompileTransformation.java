/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.transform.sc;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesWriterControllerFactoryImpl;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.sc.transformers.StaticCompilationTransformer;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

import java.util.Collections;
import java.util.Map;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;

/**
 * Handles the implementation of the {@link groovy.transform.CompileStatic} transformation.
 *
 * @author Cedric Champeau
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class StaticCompileTransformation extends StaticTypesTransformation {

    private final StaticTypesWriterControllerFactoryImpl factory = new StaticTypesWriterControllerFactoryImpl();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        StaticCompilationTransformer transformer = new StaticCompilationTransformer(source);
        AnnotationNode annotationInformation = (AnnotationNode) nodes[0];
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        StaticTypeCheckingVisitor visitor = null;
        Map<String,Expression> members = annotationInformation.getMembers();
        Expression extensions = members.get("extensions");
        if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            visitor = newVisitor(source, classNode);
            addTypeCheckingExtensions(visitor, extensions);
            classNode.putNodeMetaData(WriterControllerFactory.class, factory);
            node.putNodeMetaData(STATIC_COMPILE_NODE, !visitor.isSkipMode(node));
            visitor.initialize();
            visitor.visitClass(classNode);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            ClassNode declaringClass = methodNode.getDeclaringClass();
            visitor = newVisitor(source, declaringClass);
            addTypeCheckingExtensions(visitor, extensions);
            methodNode.putNodeMetaData(STATIC_COMPILE_NODE, !visitor.isSkipMode(node));
            if (declaringClass.getNodeMetaData(WriterControllerFactory.class) == null) {
                declaringClass.putNodeMetaData(WriterControllerFactory.class, factory);
            }
            visitor.setMethodsToBeVisited(Collections.singleton(methodNode));
            visitor.initialize();
            visitor.visitMethod(methodNode);
        } else {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type",
                    node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber()));
        }
        if (visitor != null) {
            visitor.performSecondPass();
        }
        if (node instanceof ClassNode) {
            transformer.visitClass((ClassNode) node);
        } else if (node instanceof MethodNode) {
            transformer.visitMethod((MethodNode) node);
        }
    }

    @Override
    protected StaticTypeCheckingVisitor newVisitor(final SourceUnit unit, final ClassNode node) {
        return new StaticCompilationVisitor(unit, node);
    }

}
