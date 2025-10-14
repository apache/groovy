// GPars - Groovy Parallel Systems
//
// Copyright © 2008--2011  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.util;

import groovyx.gpars.AsyncFun;
import groovyx.gpars.GParsPoolUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.Arrays;
import java.util.Collection;

import static groovyx.gpars.util.ASTUtils.addError;

/**
 * This transformation turns field initialExpressions into method calls to {@link groovyx.gpars.GParsPoolUtil#asyncFun(groovy.lang.Closure, boolean)}.
 *
 * @author Vladimir Orany
 * @author Hamlet D'Arcy
 * @author Dinko Srkoč
 * @author Paul King
 * @see groovyx.gpars.GParsPoolUtil
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AsyncFunASTTransformation implements ASTTransformation {
    private static final ClassNode MY_TYPE = ClassHelper.make(AsyncFun.class);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes);

        final AnnotatedNode fieldNode = (AnnotatedNode) nodes[1];
        final AnnotationNode annotation = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(annotation.getClassNode()) || !(fieldNode instanceof FieldNode)) return;

        final Expression classExpression;
        final Collection<Expression> values = annotation.getMembers().values();
        final Expression value = values.isEmpty() ? null : values.iterator().next();
        if (value != null && value instanceof ClassExpression) {
            classExpression = value;
        } else {
            classExpression = new ClassExpression(ClassHelper.make(GParsPoolUtil.class));
        }

        validatePoolClass(classExpression, fieldNode, source);
        final Expression initExpression = ((FieldNode) fieldNode).getInitialValueExpression();
        final ConstantExpression blocking = new ConstantExpression(memberHasValue(annotation, "blocking", true));

        final Expression newInitExpression = new StaticMethodCallExpression(
                classExpression.getType(),
                "asyncFun",
                new ArgumentListExpression(initExpression, blocking));
        ((FieldNode) fieldNode).setInitialValueExpression(newInitExpression);
    }

    private static void validatePoolClass(final Expression classExpression, final AnnotatedNode fieldNode, final SourceUnit source) {
        final Parameter[] parameters = {new Parameter(ClassHelper.CLOSURE_TYPE, "a1"),
                new Parameter(ClassHelper.boolean_TYPE, "a2")};
        final MethodNode asyncFunMethod = classExpression.getType().getMethod("asyncFun", parameters);
        if (asyncFunMethod == null || !asyncFunMethod.isStatic())
            addError("Supplied pool class has no static asyncFun(Closure, boolean) method", fieldNode, source);
    }

    private static void init(final ASTNode[] nodes) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }
    }

    private static boolean memberHasValue(final AnnotationNode node, final String name, final Object value) {
        final Expression member = node.getMember(name);
        return member != null && member instanceof ConstantExpression && ((ConstantExpression) member).getValue() == value;
    }
}
