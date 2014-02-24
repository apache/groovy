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

import groovy.transform.ScriptURI;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/**
 * Handles transformation for the @ScriptURI annotation.
 *
 * @author Paul King
 * @author Cedric Champeau
 * @author Vladimir Orany
 * @author Jim White
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ScriptURIASTTransformation extends AbstractASTTransformation {

    private static final Class<ScriptURI> MY_CLASS = ScriptURI.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode URI_TYPE = ClassHelper.make(java.net.URI.class);

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
            setScriptURIOnDeclaration((DeclarationExpression) parent, node);
        } else {
            addError("Expected to find the annotation " + MY_TYPE_NAME + " on an declaration statement.", parent);
        }
    }

    private void setScriptURIOnDeclaration(final DeclarationExpression de, final AnnotationNode node) {
        if (de.isMultipleAssignmentDeclaration()) {
            addError("Annotation " + MY_TYPE_NAME + " not supported with multiple assignment notation.", de);
            return;
        }

        if (!(de.getRightExpression() instanceof EmptyExpression)) {
            addError("Annotation " + MY_TYPE_NAME + " not supported with variable assignment.", de);
            return;
        }

        URI uri = sourceUnit.getSource().getURI();

        if (uri == null) {
            addError("Unable to get the URI for the source of this script!", de);
        } else {
            // Set the RHS to '= new URI("string for this URI")'.
            // That may throw a URISyntaxException, but we don't need to deal with that here.
            de.setRightExpression(new ConstructorCallExpression(URI_TYPE
                    , new ArgumentListExpression(new ConstantExpression(uri.toString()))));
        }
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
