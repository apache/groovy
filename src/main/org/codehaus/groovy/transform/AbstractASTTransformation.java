/*
 * Copyright 2008-2010 the original author or authors.
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
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractASTTransformation implements Opcodes, ASTTransformation {
    protected static final ClassNode SELF_TYPE = new ClassNode(ImmutableASTTransformation.class);
    protected static final ClassNode HASHMAP_TYPE = new ClassNode(HashMap.class);
    protected static final ClassNode MAP_TYPE = new ClassNode(Map.class);
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

    protected void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        sourceUnit.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), sourceUnit)
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
        return rawExcludes == null ? new ArrayList<String>() : DefaultGroovyMethods.tokenize(rawExcludes, ", ");
    }
}
