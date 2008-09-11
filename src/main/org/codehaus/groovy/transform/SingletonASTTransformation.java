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

package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.objectweb.asm.Opcodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

import groovy.beans.VetoableASTTransformation;
import groovy.lang.Singleton;

/**
 * Handles generation of code for the @Singleton annotation
 *
 * @author Alex Tkachman
 */
@GroovyASTTransformation(phase= CompilePhase.CANONICALIZATION)
public class SingletonASTTransformation implements ASTTransformation, Opcodes {

    /**
     * Handles the bulk of the processing, mostly delegating to other methods.
     *
     * @param nodes   the ast nodes
     * @param source  the source unit for the nodes
     */
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: $node.class / $parent.class");
        }
        AnnotatedNode parent = (AnnotatedNode) nodes[1];

        if (parent instanceof ClassNode) {
            ClassNode classNode = (ClassNode) parent;
            classNode.addField("instance", ACC_PUBLIC|ACC_FINAL|ACC_STATIC, classNode, new ConstructorCallExpression(classNode, new ArgumentListExpression()));
            final BlockStatement body = new BlockStatement();
            body.addStatement(new IfStatement(
                    new BooleanExpression(new PropertyExpression(new ClassExpression(classNode), "instance")),
                    new ThrowStatement(
                            new ConstructorCallExpression(ClassHelper.make(RuntimeException.class),
                                    new ArgumentListExpression(
                                            new ConstantExpression("Can't instantiate singleton " + classNode.getName() + ". Use " + classNode.getName() + ".instance" )))),
                    new EmptyStatement()));
            classNode.addConstructor(new ConstructorNode(ACC_PRIVATE, body));
        }
    }
}