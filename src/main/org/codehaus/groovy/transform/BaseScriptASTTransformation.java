/*
 * Copyright 2008-2013 the original author or authors.
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

import groovy.transform.BaseScript;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Handles transformation for the @BaseScript annotation.
 *
 * @author Paul King
 * @author Cedric Champeau
 * @author Vladimir Orany
 * @author Jim White
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class BaseScriptASTTransformation extends AbstractASTTransformation {

    private static final Class<BaseScript> MY_CLASS = BaseScript.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private SourceUnit sourceUnit;

    public void visit(ASTNode[] nodes, SourceUnit source) {
        sourceUnit = source;
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) parent;
            ClassNode cNode = de.getDeclaringClass();
            if (!cNode.isScript()) {
                addError("Annotation " + MY_TYPE_NAME + " can only be used within a Script.", parent);
                return;
            }
            
            if (de.isMultipleAssignmentDeclaration()) {
                addError("Annotation " + MY_TYPE_NAME + " not supported with multiple assignment notation.", parent);
                return;
            }
            
            if (!(de.getRightExpression() instanceof EmptyExpression)) {
                addError("Annotation " + MY_TYPE_NAME + " not supported with variable assignment.", parent);
                return;
            }
            
            ClassNode baseScriptType = de.getVariableExpression().getType().getPlainNodeReference();
            
            if(!baseScriptType.isScript()){
                addError("Declared type " + baseScriptType + " does not extend groovy.lang.Script class!", parent);
                return;
            }

            cNode.setSuperClass(baseScriptType);
            de.setRightExpression(new VariableExpression("this"));

            // Method in base script that will contain the script body code.
            MethodNode runScriptMethod = ClassHelper.findSAM(baseScriptType);

            // If they want to use a name other than than "run", then make the change.
            if (isSuitableAbstractMethod(runScriptMethod)) {
                MethodNode defaultMethod = cNode.getDeclaredMethod("run", Parameter.EMPTY_ARRAY);
                cNode.removeMethod(defaultMethod);
                MethodNode methodNode = new MethodNode(runScriptMethod.getName(), runScriptMethod.getModifiers() & ~ACC_ABSTRACT
                    , runScriptMethod.getReturnType(), runScriptMethod.getParameters(), runScriptMethod.getExceptions()
                    , defaultMethod.getCode());
                // The AST node metadata has the flag that indicates that this method is a script body.
                // It may also be carrying data for other AST transforms.
                methodNode.copyNodeMetaData(defaultMethod);
                cNode.addMethod(methodNode);
            }
        }
    }

    private boolean isSuitableAbstractMethod(MethodNode node) {
        return node != null
                && !(node.getDeclaringClass().equals(ClassHelper.SCRIPT_TYPE)
                && "run".equals(node.getName())
                && node.getParameters().length == 0);
    }

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }
    
    protected void addError(String msg, ASTNode expr) {
        // for some reason the source unit is null sometimes, e.g. in testNotAllowedInScriptInnerClassMethods
        sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                        expr.getLastLineNumber(), expr.getLastColumnNumber()),
                sourceUnit)
        );
    }
}
