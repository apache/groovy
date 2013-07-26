/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;

import java.util.*;

public class TypeCheckingContext {
    protected SourceUnit source;
    protected Set<MethodNode> methodsToBeVisited = Collections.emptySet();
    protected boolean isInStaticContext = false;

    protected final LinkedList<ErrorCollector> errorCollectors = new LinkedList<ErrorCollector>();
    protected final LinkedList<ClassNode> enclosingClassNodes = new LinkedList<ClassNode>();
    protected final LinkedList<MethodNode> enclosingMethods = new LinkedList<MethodNode>();
    protected final LinkedList<Expression> enclosingMethodCalls = new LinkedList<Expression>();

    // used for closure return type inference
    protected final LinkedList<EnclosingClosure> enclosingClosures = new LinkedList<EnclosingClosure>();

    // whenever a method using a closure as argument (typically, "with") is detected, this list is updated
    // with the receiver type of the with method
    protected DelegationMetadata delegationMetadata;
    /**
     * The type of the last encountered "it" implicit parameter
     */
    protected ClassNode lastImplicitItType;
    /**
     * This field is used to track assignments in if/else branches, for loops and while loops. For example, in the
     * following code: if (cond) { x = 1 } else { x = '123' } the inferred type of x after the if/else statement should
     * be the LUB of (int, String)
     */
    protected Map<VariableExpression, List<ClassNode>> ifElseForWhileAssignmentTracker = null;
    /**
     * Stores information which is only valid in the "if" branch of an if-then-else statement. This is used when the if
     * condition expression makes use of an instanceof check
     */
    protected Stack<Map<Object, List<ClassNode>>> temporaryIfBranchTypeInformation = new Stack<Map<Object, List<ClassNode>>>();
    protected Set<MethodNode> alreadyVisitedMethods = new HashSet<MethodNode>();
    /**
     * Some expressions need to be visited twice, because type information may be insufficient at some point. For
     * example, for closure shared variables, we need a first pass to collect every type which is assigned to a closure
     * shared variable, then a second pass to ensure that every method call on such a variable is made on a LUB.
     */
    protected final LinkedHashSet<SecondPassExpression> secondPassExpressions = new LinkedHashSet<SecondPassExpression>();
    /**
     * A map used to store every type used in closure shared variable assignments. In a second pass, we will compute the
     * LUB of each type and check that method calls on those variables are valid.
     */
    protected final Map<VariableExpression, List<ClassNode>> closureSharedVariablesAssignmentTypes = new HashMap<VariableExpression, List<ClassNode>>();
    protected Map<Parameter, ClassNode> controlStructureVariables = new HashMap<Parameter, ClassNode>();

    // this map is used to ensure that two errors are not reported on the same line/column
    protected final Set<Long> reportedErrors = new TreeSet<Long>();

    // stores the current binary expresssion. This is used when assignments are made with a null object, for type
    // inference
    protected final LinkedList<BinaryExpression> enclosingBinaryExpressions = new LinkedList<BinaryExpression>();

    protected final StaticTypeCheckingVisitor visitor;

    public TypeCheckingContext(final StaticTypeCheckingVisitor staticTypeCheckingVisitor) {
        this.visitor = staticTypeCheckingVisitor;
    }

    /**
     * Pushes a binary expression into the binary expression stack.
     * @param binaryExpression the binary expression to be pushed
     */
    public void pushEnclosingBinaryExpression(BinaryExpression binaryExpression) {
        enclosingBinaryExpressions.addFirst(binaryExpression);
    }

    /**
     * Pops a binary expression from the binary expression stack.
     * @return the popped binary expression
     */
    public BinaryExpression popEnclosingBinaryExpression() {
        return enclosingBinaryExpressions.removeFirst();
    }

    /**
     * Returns the binary expression which is on the top of the stack, or null
     * if there's no such element.
     * @return the binary expression on top of the stack, or null if no such element.
     */
    public BinaryExpression getEnclosingBinaryExpression() {
        if (enclosingBinaryExpressions.isEmpty()) return null;
        return enclosingBinaryExpressions.getFirst();
    }

    /**
     * Returns the current stack of enclosing binary expressions. The first
     * element is the top of the stack.
     * @return an immutable list of binary expressions.
     */
    public List<BinaryExpression> getEnclosingBinaryExpressionStack() {
        return Collections.unmodifiableList(enclosingBinaryExpressions);
    }

    /**
     * Pushes a closure expression into the closure expression stack.
     * @param closureExpression the binary expression to be pushed
     */
    public void pushEnclosingClosureExpression(ClosureExpression closureExpression) {
        enclosingClosures.addFirst(new EnclosingClosure(closureExpression));
    }

    /**
     * Pops a closure expression from the closure expression stack.
     * @return the popped closure expression
     */
    public EnclosingClosure popEnclosingClosure() {
        return enclosingClosures.removeFirst();
    }

    /**
     * Returns the closure expression which is on the top of the stack, or null
     * if there's no such element.
     * @return the closure expression on top of the stack, or null if no such element.
     */
    public EnclosingClosure getEnclosingClosure() {
        if (enclosingClosures.isEmpty()) return null;
        return enclosingClosures.getFirst();
    }

    /**
     * Returns the current stack of enclosing closure expressions. The first
     * element is the top of the stack.
     * @return an immutable list of closure expressions.
     */
    public List<EnclosingClosure> getEnclosingClosureStack() {
        return Collections.unmodifiableList(enclosingClosures);
    }

    /**
     * Pushes a method into the method stack.
     * @param methodNode the binary expression to be pushed
     */
    public void pushEnclosingMethod(MethodNode methodNode) {
        enclosingMethods.addFirst(methodNode);
    }

    /**
     * Pops a method from the enclosing methods stack.
     * @return the popped method
     */
    public MethodNode popEnclosingMethod() {
        return enclosingMethods.removeFirst();
    }

    /**
     * Returns the method node which is on the top of the stack, or null
     * if there's no such element.
     * @return the enclosing method on top of the stack, or null if no such element.
     */
    public MethodNode getEnclosingMethod() {
        if (enclosingMethods.isEmpty()) return null;
        return enclosingMethods.getFirst();
    }

    /**
     * Returns the current stack of enclosing methods. The first
     * element is the top of the stack, that is to say the last visited method.
     * @return an immutable list of method nodes.
     */
    public List<MethodNode> getEnclosingMethods() {
        return Collections.unmodifiableList(enclosingMethods);
    }

    /**
     * Pushes a method into the method stack.
     * @param classNode the binary expression to be pushed
     */
    public void pushEnclosingClassNode(ClassNode classNode) {
        enclosingClassNodes.addFirst(classNode);
    }

    /**
     * Pops a class from the enclosing classes stack.
     * @return the popped class
     */
    public ClassNode popEnclosingClassNode() {
        return enclosingClassNodes.removeFirst();
    }

    /**
     * Returns the method node which is on the top of the stack, or null
     * if there's no such element.
     * @return the enclosing method on top of the stack, or null if no such element.
     */
    public ClassNode getEnclosingClassNode() {
        if (enclosingClassNodes.isEmpty()) return null;
        return enclosingClassNodes.getFirst();
    }

    /**
     * Returns the current stack of enclosing classes. The first
     * element is the top of the stack, that is to say the currently visited class.
     * @return an immutable list of class nodes.
     */
    public List<ClassNode> getEnclosingClassNodes() {
        return Collections.unmodifiableList(enclosingClassNodes);
    }

    public void pushTemporaryTypeInfo() {
        Map<Object, List<ClassNode>> potentialTypes = new HashMap<Object, List<ClassNode>>();
        temporaryIfBranchTypeInformation.push(potentialTypes);
    }

    public void popTemporaryTypeInfo() {
        temporaryIfBranchTypeInformation.pop();
    }

    /**
     * Pushes a method call into the method call stack.
     * @param call the call expression to be pushed, either a {@link MethodCallExpression} or a {@link StaticMethodCallExpression}
     */
    public void pushEnclosingMethodCall(Expression call) {
        if (call instanceof MethodCallExpression || call instanceof StaticMethodCallExpression) {
            enclosingMethodCalls.addFirst(call);
        } else {
            throw new IllegalArgumentException("Expression must be a method call or a static method call");
        }
    }

    /**
     * Pops a method call from the enclosing method call stack.
     * @return the popped call
     */
    public Expression popEnclosingMethodCall() {
        return enclosingMethodCalls.removeFirst();
    }

    /**
     * Returns the method call which is on the top of the stack, or null
     * if there's no such element.
     * @return the enclosing method call on top of the stack, or null if no such element.
     */
    public Expression getEnclosingMethodCall() {
        if (enclosingMethodCalls.isEmpty()) return null;
        return enclosingMethodCalls.getFirst();
    }

    /**
     * Returns the current stack of enclosing classes. The first
     * element is the top of the stack, that is to say the currently visited class.
     * @return an immutable list of class nodes.
     */
    public List<Expression> getEnclosingMethodCalls() {
        return Collections.unmodifiableList(enclosingMethodCalls);
    }

    public List<ErrorCollector> getErrorCollectors() {
        return Collections.unmodifiableList(errorCollectors);
    }

    public ErrorCollector getErrorCollector() {
        if (errorCollectors.isEmpty()) return null;
        return errorCollectors.getFirst();
    }


    public void pushErrorCollector(ErrorCollector collector) {
        errorCollectors.add(0, collector);
    }

    public ErrorCollector pushErrorCollector() {
        ErrorCollector current = getErrorCollector();
        ErrorCollector collector = new ErrorCollector(current.getConfiguration());
        errorCollectors.add(0, collector);
        return collector;
    }

    public ErrorCollector popErrorCollector() {
        return errorCollectors.removeFirst();
    }

    /**
     * Represents the context of an enclosing closure. An enclosing closure wraps
     * a closure expression and the list of return types found in the closure body.
     */
    public static class EnclosingClosure {
        private final ClosureExpression closureExpression;
        private final List<ClassNode> returnTypes;

        public EnclosingClosure(final ClosureExpression closureExpression) {
            this.closureExpression = closureExpression;
            this.returnTypes = new LinkedList<ClassNode>();
        }

        public ClosureExpression getClosureExpression() {
            return closureExpression;
        }

        public List<ClassNode> getReturnTypes() {
            return returnTypes;
        }

        public void addReturnType(ClassNode type) {
            returnTypes.add(type);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("EnclosingClosure");
            sb.append("{closureExpression=").append(closureExpression.getText());
            sb.append(", returnTypes=").append(returnTypes);
            sb.append('}');
            return sb.toString();
        }
    }
}