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
package org.apache.groovy.parser.antlr4;

import groovy.lang.Groovydoc;
import groovy.lang.groovydoc.GroovydocHolder;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.ConstantExpression;

import java.util.List;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

/**
 * A utilities for managing groovydoc, e.g.
 * 1) extracting groovydoc from groovy AST;
 * 2) TODO extracting tags from groovydoc;
 * 3) attach groovydoc to AST node as metadata
 */
public class GroovydocManager {
    public static final String DOC_COMMENT = GroovydocHolder.DOC_COMMENT; // keys for meta data
    private static final String DOC_COMMENT_PREFIX = "/**";
    private static final String TRUE_STR = "true";

    @Deprecated
    private static final String EXTRACT_DOC_COMMENT = "groovy.extract.doc.comment"; // it will be removed in future releases and replaced with ATTACH_GROOVYDOC
    private static final String ATTACH_GROOVYDOC = "groovy.attach.groovydoc";
    private static final boolean ATTACHING_GROOVYDOC_ENABLED;
    private static final String ATTACH_RUNTIME_GROOVYDOC = "groovy.attach.runtime.groovydoc";
    private static final boolean ATTACHING_RUNTIME_GROOVYDOC_ENABLED;
    private static final String VALUE = "value";
    private static final String RUNTIME_GROOVYDOC_PATTERN = "(?s)/[*][*]@\\s+.*?[*]/";

    private static final GroovydocManager INSTANCE = new GroovydocManager();

    static {
        ATTACHING_GROOVYDOC_ENABLED = isFeatureEnabled(ATTACH_GROOVYDOC) || isFeatureEnabled(EXTRACT_DOC_COMMENT);
        ATTACHING_RUNTIME_GROOVYDOC_ENABLED = isFeatureEnabled(ATTACH_RUNTIME_GROOVYDOC);
    }

    private static boolean isFeatureEnabled(String featureOpt) {
        boolean result;

        try {
            result = TRUE_STR.equals(System.getProperty(featureOpt));
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    private GroovydocManager() {}

    public static GroovydocManager getInstance() {
        return INSTANCE;
    }

    /**
     * Attach doc comment to member node as meta data
     *
     */
    public void handle(ASTNode node, GroovyParser.GroovyParserRuleContext ctx) {
        if (!asBoolean(node) || !asBoolean(ctx)) {
            return;
        }

        String docCommentNodeText = this.findDocCommentByNode(ctx);
        if (null == docCommentNodeText) {
            return;
        }

        attachDocCommentAsMetaData(node, docCommentNodeText);
        attachGroovydocAnnotation(node, docCommentNodeText);
    }

    /*
     * Attach doc comment to member node as meta data
     */
    private void attachDocCommentAsMetaData(ASTNode node, String docCommentNodeText) {
        if (!ATTACHING_GROOVYDOC_ENABLED) {
            return;
        }

        if (!(node instanceof GroovydocHolder)) {
            return;
        }

        node.putNodeMetaData(DOC_COMMENT, new groovy.lang.groovydoc.Groovydoc(docCommentNodeText, (GroovydocHolder) node));
    }

    /*
     * Attach Groovydoc annotation to the target element
     */
    private void attachGroovydocAnnotation(ASTNode node, String docCommentNodeText) {
        if (!(node instanceof AnnotatedNode)) {
            return;
        }

        if (!(ATTACHING_RUNTIME_GROOVYDOC_ENABLED || docCommentNodeText.matches(RUNTIME_GROOVYDOC_PATTERN))) {
            return;
        }

        AnnotatedNode annotatedNode = (AnnotatedNode) node;
        AnnotationNode annotationNode = new AnnotationNode(ClassHelper.make(Groovydoc.class));
        annotationNode.addMember(VALUE, new ConstantExpression(docCommentNodeText));
        annotatedNode.addAnnotation(annotationNode);
    }

    private String findDocCommentByNode(ParserRuleContext node) {
        if (!asBoolean(node)) {
            return null;
        }

        if (node instanceof GroovyParser.ClassBodyContext) {
            return null;
        }

        ParserRuleContext parentContext = node.getParent();

        if (!asBoolean(parentContext)) {
            return null;
        }

        String docCommentNodeText = null;
        boolean sameTypeNodeBefore = false;
        for (ParseTree child : parentContext.children) {

            if (node == child) {
                // if no doc comment node found and no siblings of same type before the node,
                // try to find doc comment node of its parent
                if (!asBoolean((Object) docCommentNodeText) && !sameTypeNodeBefore) {
                    return findDocCommentByNode(parentContext);
                }

                return docCommentNodeText;
            }

            if (node.getClass() == child.getClass()) { // e.g. ClassBodyDeclarationContext == ClassBodyDeclarationContext
                docCommentNodeText = null;
                sameTypeNodeBefore = true;
                continue;
            }

            if (!(child instanceof GroovyParser.NlsContext || child instanceof GroovyParser.SepContext)) {
                continue;
            }

            // doc comments are treated as NL
            List<? extends TerminalNode> nlList =
                    child instanceof GroovyParser.NlsContext
                            ? ((GroovyParser.NlsContext) child).NL()
                            : ((GroovyParser.SepContext) child).NL();

            int nlListSize = nlList.size();
            if (0 == nlListSize) {
                continue;
            }

            for (int i = nlListSize - 1; i >= 0; i--) {
                String text = nlList.get(i).getText();

                if (text.matches("\\s+")) {
                    continue;
                }

                if (text.startsWith(DOC_COMMENT_PREFIX)) {
                    docCommentNodeText = text;
                } else {
                    docCommentNodeText = null;
                }

                break;
            }
        }

        throw new GroovyBugError("node can not be found: " + node.getText()); // The exception should never be thrown!
    }
}
