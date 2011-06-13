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


import groovy.transform.ConditionalInterrupt
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*

/**
 * Allows "interrupt-safe" executions of scripts by adding a custom conditional
 * check on loops (for, while, do) and first statement of closures. By default, also adds an interrupt check
 * statement on the beginning of method calls.
 *
 * @see groovy.transform.ConditionalInterrupt
 *
 * @author Cedric Champeau
 * @author Hamlet D'Arcy
 *
 * @since 1.8.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ConditionalInterruptibleASTTransformation extends AbstractInterruptibleASTTransformation {

    private static final ClassNode MY_TYPE = ClassHelper.make(ConditionalInterrupt)
    private static final String CONDITION_METHOD = 'conditionalTransform$condition';

    private ClosureExpression conditionNode
    private MethodCallExpression conditionCallExpression
    private ClassNode currentClass

    protected ClassNode type() {
        return MY_TYPE;
    }

    protected void setupTransform(AnnotationNode node) {
        super.setupTransform(node)
        def member = node.getMember("value")
        if (!member || !(member instanceof ClosureExpression)) internalError("Expected closure value for annotation parameter 'value'. Found $member")
        conditionNode = member;
        conditionCallExpression = new MethodCallExpression(new VariableExpression('this'), CONDITION_METHOD, new ArgumentListExpression())
    }

    protected String getErrorMessage() {
        'Execution interrupted. The following condition failed: ' + convertClosureToSource(conditionNode)
    }

    public void visitClass(ClassNode type) {
        currentClass = type;
        def method = type.addMethod(CONDITION_METHOD, ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, conditionNode.code);
        method.synthetic = true
        super.visitClass(type);
    }

    protected Expression createCondition() {
        conditionCallExpression
    }

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        // this transformation does not apply on annotation nodes
        // visiting could lead to stack overflows
    }

    @Override
    void visitField(FieldNode node) {
      if (!node.isStatic() && !node.isSynthetic()) {
        super.visitField node
      }
    }

    @Override
    void visitProperty(PropertyNode node) {
      if (!node.isStatic() && !node.isSynthetic()) {
        super.visitProperty node
      }
    }

    @Override
    public void visitClosureExpression(ClosureExpression closureExpr) {
        if (closureExpr == conditionNode) return // no not visit the closure from the annotation itself
        def code = closureExpr.code
        closureExpr.code = wrapBlock(code)
        super.visitClosureExpression closureExpr
    }

    @Override
    public void visitMethod(MethodNode node) {
        if (node.name == CONDITION_METHOD && !node.isSynthetic()) return // do not visit the generated method
        if (node.name == 'run' && currentClass.isScript() && node.parameters.length == 0) {
            // the run() method should not have the statement added, otherwise the script binding won't be set before
            // the condition is actually tested
            super.visitMethod(node)
        } else {
            if (checkOnMethodStart && !node.isSynthetic() && !node.isStatic() && !node.isAbstract()) {
                def code = node.code
                node.code = wrapBlock(code);
            }
            if (!node.isSynthetic() && !node.isStatic()) super.visitMethod(node)
        }
    }

    /**
     * Converts a ClosureExpression into the String source.
     * @param expression    a closure
     * @return              the source the closure was created from
     */
    // TODO should this be moved to ClosureExpression? It also appers in AstBuilderTransformation
    private String convertClosureToSource(ClosureExpression expression) {
        if (expression == null) throw new IllegalArgumentException('Null: expression')

        def lineRange = (expression.lineNumber..expression.lastLineNumber)

        def source = lineRange.collect {
            def line = source.source.getLine(it, null)
            if (line == null) {
                return "Error calculating source code for expression. Trying to read line $it from ${source.source.class}"
            } else {
                if (it == expression.lastLineNumber) {
                    line = line.substring(0, expression.lastColumnNumber - 1)
                }
                if (it == expression.lineNumber) {
                    line = line.substring(expression.columnNumber - 1)
                }
            }
            return line
        }?.join('\n')?.trim()   //restoring line breaks is important b/c of lack of semicolons

        if (!source.startsWith('{')) {
            return 'Error converting ClosureExpression into source code. ' +
                    "Closures must start with {. Found: $source"
        }

        return source
    }
}
