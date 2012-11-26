/*
 * Copyright 2008-2012 the original author or authors.
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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractASTTransformation implements Opcodes, ASTTransformation {
    private SourceUnit sourceUnit;

    protected void init(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }
        this.sourceUnit = sourceUnit;
    }

    protected boolean memberHasValue(AnnotationNode node, String name, Object value) {
        final Expression member = node.getMember(name);
        return member != null && member instanceof ConstantExpression && ((ConstantExpression) member).getValue().equals(value);
    }

    protected Object getMemberValue(AnnotationNode node, String name) {
        final Expression member = node.getMember(name);
        if (member != null && member instanceof ConstantExpression) return ((ConstantExpression) member).getValue();
        return null;
    }

    protected String getMemberStringValue(AnnotationNode node, String name) {
        final Expression member = node.getMember(name);
        if (member != null && member instanceof ConstantExpression) {
            Object result = ((ConstantExpression) member).getValue();
            if (result != null) return result.toString();
        }
        return null;
    }

    protected List<String> getMemberList(AnnotationNode anno, String name) {
        List<String> list;
        Expression expr = anno.getMember(name);
        if (expr != null && expr instanceof ListExpression) {
            list = new ArrayList<String>();
            final ListExpression listExpression = (ListExpression) expr;
            for (Expression itemExpr : listExpression.getExpressions()) {
                if (itemExpr != null && itemExpr instanceof ConstantExpression) {
                    Object value = ((ConstantExpression) itemExpr).getValue();
                    if (value != null) list.add(value.toString());
                }
            }
        } else {
            list = tokenize(getMemberStringValue(anno, name));
        }
        return list;
    }

    protected void addError(String msg, ASTNode expr) {
        sourceUnit.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(
                new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                        expr.getLastLineNumber(), expr.getLastColumnNumber()),
                sourceUnit)
        );
    }

    protected void checkNotInterface(ClassNode cNode, String annotationName) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() + "'. " +
                    annotationName + " not allowed for interfaces.", cNode);
        }
    }

    protected boolean hasAnnotation(ClassNode cNode, ClassNode annotation) {
        List annots = cNode.getAnnotations(annotation);
        return (annots != null && annots.size() > 0);
    }

    protected List<String> tokenize(String rawExcludes) {
        return rawExcludes == null ? new ArrayList<String>() : StringGroovyMethods.tokenize(rawExcludes, ", ");
    }
}
