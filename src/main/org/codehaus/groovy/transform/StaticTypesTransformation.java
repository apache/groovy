/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.transform;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.TypeCheckerPluginFactory;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

/**
 * Handles the implementation of the {@link groovy.transform.TypeChecked} transformation.
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @author Cedric Champeau
 * @author Guillaume Laforge
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class StaticTypesTransformation implements ASTTransformation {

    public static final String STATIC_ERROR_PREFIX = "[Static type checking] - ";

//    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        AnnotationNode annotationInformation = (AnnotationNode) nodes[0];
        Map<String,Expression> members = annotationInformation.getMembers();
        TypeCheckerPluginFactory pluginFactory = null;
        if (members!=null) {
            Expression exp = members.get("pluginFactory");
            if (exp instanceof ClassExpression) {
                ClassNode type = exp.getType();
                Class clazz = type.getTypeClass();
                if (TypeCheckerPluginFactory.class.isAssignableFrom(TypeCheckerPluginFactory.class)) {
                    try {
                        pluginFactory = (TypeCheckerPluginFactory) clazz.newInstance();
                    } catch (InstantiationException e) {
                        throw new GroovyBugError(e);
                    } catch (IllegalAccessException e) {
                        throw new GroovyBugError(e);
                    }
                }
            }
        }
        AnnotatedNode node = (AnnotatedNode) nodes[1];
        StaticTypeCheckingVisitor visitor = null;
        if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            visitor = newVisitor(source, classNode, pluginFactory);
            visitor.visitClass(classNode);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode)node;
            visitor = newVisitor(source, methodNode.getDeclaringClass(), pluginFactory);
            visitor.setMethodsToBeVisited(Collections.singleton(methodNode));
            visitor.visitMethod(methodNode);
        } else {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type", node.getLineNumber(), node.getColumnNumber()));
        }
        if (visitor!=null) {
            visitor.performSecondPass();
        }
    }

    /**
     * Allows subclasses to provide their own visitor. This is useful for example for transformations relying
     * on the static type checker.
     * @param unit the source unit
     * @param node the current classnode
     * @return a static type checking visitor
     */
    protected StaticTypeCheckingVisitor newVisitor(SourceUnit unit, ClassNode node, TypeCheckerPluginFactory pluginFactory) {
        return new StaticTypeCheckingVisitor(unit, node, pluginFactory);
    }

}
