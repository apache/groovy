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
package org.codehaus.groovy.transform;

import groovy.transform.BaseScript;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;

/**
 * Handles transformation for the @BaseScript annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class BaseScriptASTTransformation extends AbstractASTTransformation {

    private static final Class<BaseScript> MY_CLASS = BaseScript.class;
    public static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final Parameter[] CONTEXT_CTOR_PARAMETERS = {new Parameter(ClassHelper.BINDING_TYPE, "context")};

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof DeclarationExpression) {
            changeBaseScriptTypeFromDeclaration((DeclarationExpression) parent, node);
        } else if (parent instanceof ImportNode || parent instanceof PackageNode) {
            changeBaseScriptTypeFromPackageOrImport(source, parent, node);
        } else if (parent instanceof ClassNode) {
            changeBaseScriptTypeFromClass((ClassNode) parent, node);
        }
    }

    private void changeBaseScriptTypeFromPackageOrImport(final SourceUnit source, final AnnotatedNode parent, final AnnotationNode node) {
        Expression value = node.getMember("value");
        if (!(value instanceof ClassExpression)) {
            addError("Annotation " + MY_TYPE_NAME + " member 'value' should be a class literal.", value);
            return;
        }
        List<ClassNode> classes = source.getAST().getClasses();
        for (ClassNode classNode : classes) {
            if (classNode.isScriptBody()) {
                changeBaseScriptType(parent, classNode, value.getType());
            }
        }
    }

    private void changeBaseScriptTypeFromClass(final ClassNode parent, final AnnotationNode node) {
//        Expression value = node.getMember("value");
//        if (!(value instanceof ClassExpression)) {
//            addError("Annotation " + MY_TYPE_NAME + " member 'value' should be a class literal.", value);
//            return;
//        }
        changeBaseScriptType(parent, parent, parent.getSuperClass());
    }

    private void changeBaseScriptTypeFromDeclaration(final DeclarationExpression de, final AnnotationNode node) {
        if (de.isMultipleAssignmentDeclaration()) {
            addError("Annotation " + MY_TYPE_NAME + " not supported with multiple assignment notation.", de);
            return;
        }

        if (!(de.getRightExpression() instanceof EmptyExpression)) {
            addError("Annotation " + MY_TYPE_NAME + " not supported with variable assignment.", de);
            return;
        }
        Expression value = node.getMember("value");
        if (value != null) {
            addError("Annotation " + MY_TYPE_NAME + " cannot have member 'value' if used on a declaration.", value);
            return;
        }

        ClassNode cNode = de.getDeclaringClass();
        ClassNode baseScriptType = de.getVariableExpression().getType().getPlainNodeReference();
        de.setRightExpression(new VariableExpression("this"));

        changeBaseScriptType(de, cNode, baseScriptType);
    }

    private void changeBaseScriptType(final AnnotatedNode parent, final ClassNode cNode, final ClassNode baseScriptType) {
        if (!cNode.isScriptBody()) {
            addError("Annotation " + MY_TYPE_NAME + " can only be used within a Script.", parent);
            return;
        }

        if (!baseScriptType.isScript()) {
            addError("Declared type " + baseScriptType + " does not extend groovy.lang.Script class!", parent);
            return;
        }

        cNode.setSuperClass(baseScriptType);

        // Method in base script that will contain the script body code.
        MethodNode runScriptMethod = ClassHelper.findSAM(baseScriptType);

        // If they want to use a name other than than "run", then make the change.
        if (isCustomScriptBodyMethod(runScriptMethod)) {
            MethodNode defaultMethod = cNode.getDeclaredMethod("run", Parameter.EMPTY_ARRAY);
            // GROOVY-6706: Sometimes an NPE is thrown here.
            // The reason is that our transform is getting called more than once sometimes.  
            if (defaultMethod != null) {
                cNode.removeMethod(defaultMethod);
                MethodNode methodNode = new MethodNode(runScriptMethod.getName(), runScriptMethod.getModifiers() & ~ACC_ABSTRACT
                        , runScriptMethod.getReturnType(), runScriptMethod.getParameters(), runScriptMethod.getExceptions()
                        , defaultMethod.getCode());
                // The AST node metadata has the flag that indicates that this method is a script body.
                // It may also be carrying data for other AST transforms.
                methodNode.copyNodeMetaData(defaultMethod);
                addGeneratedMethod(cNode, methodNode);
            }
        }

        // If the new script base class does not have a contextual constructor (g.l.Binding), then we won't either.
        // We have to do things this way (and rely on just default constructors) because the logic that generates
        // the constructors for our script class have already run.
        if (cNode.getSuperClass().getDeclaredConstructor(CONTEXT_CTOR_PARAMETERS) == null) {
            ConstructorNode orphanedConstructor = cNode.getDeclaredConstructor(CONTEXT_CTOR_PARAMETERS);
            cNode.removeConstructor(orphanedConstructor);
        }
    }

    private static boolean isCustomScriptBodyMethod(MethodNode node) {
        return node != null
            && !(node.getDeclaringClass().equals(ClassHelper.SCRIPT_TYPE)
                && "run".equals(node.getName())
                && node.getParameters().length == 0);
    }
}
