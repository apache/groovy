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

import groovy.transform.SourceURI;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.io.File;
import java.net.URI;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;

/**
 * Handles transformation for the @SourceURI annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class SourceURIASTTransformation extends AbstractASTTransformation {

    private static final Class<SourceURI> MY_CLASS = SourceURI.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode URI_TYPE = ClassHelper.make(java.net.URI.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof DeclarationExpression) {
            setScriptURIOnDeclaration((DeclarationExpression) parent, node);
        } else if (parent instanceof FieldNode) {
            setScriptURIOnField((FieldNode) parent, node);
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

        URI uri = getSourceURI(node);

        if (uri == null) {
            addError("Unable to get the URI for the source of this script!", de);
        } else {
            // Set the RHS to '= URI.create("string for this URI")'.
            // That may throw an IllegalArgumentExpression wrapping the URISyntaxException.
            de.setRightExpression(getExpression(uri));
        }
    }

    private void setScriptURIOnField(final FieldNode fieldNode, final AnnotationNode node) {
        if (fieldNode.hasInitialExpression()) {
            addError("Annotation " + MY_TYPE_NAME + " not supported with variable assignment.", fieldNode);
            return;
        }

        URI uri = getSourceURI(node);

        if (uri == null) {
            addError("Unable to get the URI for the source of this class!", fieldNode);
        } else {
            // Set the RHS to '= URI.create("string for this URI")'.
            // That may throw an IllegalArgumentExpression wrapping the URISyntaxException.
            fieldNode.setInitialValueExpression(getExpression(uri));
        }
    }

    private static Expression getExpression(URI uri) {
        return callX(URI_TYPE, "create", args(constX(uri.toString())));
    }

    protected URI getSourceURI(AnnotationNode node) {
        URI uri = sourceUnit.getSource().getURI();

        if (uri != null) {
            if (!(uri.isAbsolute() || memberHasValue(node, "allowRelative", true))) {
                // FIXME:  What should we use as the base URI?
                // It is unlikely we get to this point with a relative URI since making a URL
                // from will make it absolute I think.  But lets handle the simple case of
                // using file paths and turning that into an absolute file URI.
                // So we will use the current working directory as the base.
                URI baseURI = new File(".").toURI();
                uri = uri.resolve(baseURI);
            }
        }

        return uri;
    }
}
