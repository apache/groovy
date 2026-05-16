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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Holds mutable state shared across a static type-checking visit.
 */
public class TypeCheckingContext {

    /**
     * Creates a context for the supplied type-checking visitor.
     */
    public TypeCheckingContext(final StaticTypeCheckingVisitor visitor) {
        this.visitor = visitor;
    }

    /*public TypeCheckingContext(final SourceUnit source) {
        this.source = source;
        pushErrorCollector(source.getErrorCollector());
    }*/

    /** Visitor that owns this context. */
    protected final StaticTypeCheckingVisitor visitor;
    /** Source unit currently being type checked. */
    protected       SourceUnit source;

    /**
     * Returns the source unit currently being type checked.
     */
    public SourceUnit getSource() {
        return source;
    }

    //--------------------------------------------------------------------------

    /** Compilation unit associated with the current type-checking run. */
    protected CompilationUnit compilationUnit;

    /**
     * Returns the compilation unit associated with this context.
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    /**
     * Sets the compilation unit associated with this context.
     */
    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    //--------------------------------------------------------------------------

    /** Error collectors stacked for nested type-checking operations. */
    protected final LinkedList<ErrorCollector> errorCollectors = new LinkedList<>();

    /**
     * Pushes a fresh error collector derived from the current compiler configuration.
     */
    public ErrorCollector pushErrorCollector() {
        CompilerConfiguration config = Optional.ofNullable(getErrorCollector())
                .map(ErrorCollector::getConfiguration).orElseGet(() -> getSource().getConfiguration());

        ErrorCollector collector = new ErrorCollector(config);
        pushErrorCollector(collector);
        return collector;
    }

    /**
     * Pushes the supplied error collector.
     */
    public void pushErrorCollector(final ErrorCollector collector) {
        errorCollectors.addFirst(collector);
    }

    /**
     * Pops the current error collector.
     */
    public ErrorCollector popErrorCollector() {
        return errorCollectors.removeFirst();
    }

    /**
     * Returns the current error collector, or {@code null} if none is active.
     */
    public ErrorCollector getErrorCollector() {
        if (errorCollectors.isEmpty()) {
            return null;
        }
        return errorCollectors.getFirst();
    }

    /**
     * Returns the active error collector stack.
     */
    public List<ErrorCollector> getErrorCollectors() {
        return Collections.unmodifiableList(errorCollectors);
    }

    //--------------------------------------------------------------------------

    /**
     * Stores information which is valid in the "then" branch of an if-then-else
     * statement.  This is used when the if condition expression makes use of an
     * {@code instanceof} check.
     */
    protected Stack<Map<Object, List<ClassNode>>> temporaryIfBranchTypeInformation = new Stack<>();

    List<ClassNode> peekTemporaryTypeInfo(final Object o) {
        return temporaryIfBranchTypeInformation.peek().computeIfAbsent(o, x -> new LinkedList<>());
    }

    /**
     * Pushes a temporary type-information frame.
     */
    public void pushTemporaryTypeInfo() {
        temporaryIfBranchTypeInformation.push(new HashMap<>());
    }

    /**
     * Pops the current temporary type-information frame.
     */
    public void popTemporaryTypeInfo() {
        temporaryIfBranchTypeInformation.pop();
    }

    //--------------------------------------------------------------------------
    // TODO: Should these be fields of StaticTypeCheckingVisitor?

    /**
     * Whenever a method using a closure as argument (typically, "with") is
     * detected, this list is updated with the receiver type of the with method.
     */
    protected DelegationMetadata delegationMetadata;

    /** Indicates whether the current visit happens in a static context. */
    protected boolean isInStaticContext;

    /**
     * The type of the last encountered "it" implicit parameter.
     */
    protected ClassNode lastImplicitItType;

    /** Optional filter restricting which methods should be visited. */
    protected Set<MethodNode> methodsToBeVisited = Collections.emptySet();

    /**
     * This field is used to track assignments in if/else branches, for loops and
     * while loops. For example, in the following code: <code>if (cond) { x = 1 } else { x = '123' }</code>
     * the inferred type of x after the if/else statement should be the LUB of int and String.
     */
    protected Map<VariableExpression, List<ClassNode>> ifElseForWhileAssignmentTracker;

    /** Methods already visited during the current checking pass. */
    protected Set<MethodNode> alreadyVisitedMethods = new HashSet<>();

    /**
     * Some expressions need to be visited twice, because type information may be insufficient at some point. For
     * example, for closure shared variables, we need a first pass to collect every type which is assigned to a closure
     * shared variable, then a second pass to ensure that every method call on such a variable is made on a LUB.
     */
    protected final LinkedHashSet<SecondPassExpression> secondPassExpressions = new LinkedHashSet<>();

    /**
     * A map used to store every type used in closure shared variable assignments. In a second pass, we will compute the
     * LUB of each type and check that method calls on those variables are valid.
     */
    protected final Map<VariableExpression, List<ClassNode>> closureSharedVariablesAssignmentTypes = new HashMap<>();

    /** Types tracked for variables introduced by control structures. */
    protected       Map<Parameter, ClassNode> controlStructureVariables = new HashMap<>();

    // this map is used to ensure that two errors are not reported on the same line/column
    /** Source positions that already produced a type-checking error. */
    protected final Set<Long> reportedErrors = new TreeSet<>();

    //--------------------------------------------------------------------------

    // TODO: replace with general node stack and filters
    /** Stack of enclosing class nodes. */
    protected final LinkedList<ClassNode> enclosingClassNodes = new LinkedList<>();
    /** Stack of enclosing methods. */
    protected final LinkedList<MethodNode> enclosingMethods = new LinkedList<>();
    /** Stack of enclosing method-call expressions. */
    protected final LinkedList<Expression> enclosingMethodCalls = new LinkedList<>();
    /** Stack of enclosing switch statements. */
    protected final LinkedList<SwitchStatement> switchStatements = new LinkedList<>();
    /** Stack of enclosing closures. */
    protected final LinkedList<EnclosingClosure> enclosingClosures = new LinkedList<>();
    // stores the current binary expression. This is used when assignments are made with a null object, for type inference
    /** Stack of enclosing binary expressions. */
    protected final LinkedList<BinaryExpression> enclosingBinaryExpressions = new LinkedList<>();

    /**
     * Pushes a binary expression into the binary expression stack.
     */
    public void pushEnclosingBinaryExpression(final BinaryExpression binaryExpression) {
        enclosingBinaryExpressions.addFirst(binaryExpression);
    }

    /**
     * Pops a binary expression from the binary expression stack.
     */
    public BinaryExpression popEnclosingBinaryExpression() {
        return enclosingBinaryExpressions.removeFirst();
    }

    /**
     * Returns the binary expression which is on the top of the stack, or null
     * if there's no such element.
     */
    public BinaryExpression getEnclosingBinaryExpression() {
        if (enclosingBinaryExpressions.isEmpty()) return null;
        return enclosingBinaryExpressions.getFirst();
    }

    /**
     * Returns the current stack of enclosing binary expressions. The first
     * element is the top of the stack.
     */
    public List<BinaryExpression> getEnclosingBinaryExpressionStack() {
        return Collections.unmodifiableList(enclosingBinaryExpressions);
    }

    /**
     * Indicates whether the expression is the left-hand side of the current assignment.
     */
    public boolean isTargetOfEnclosingAssignment(final Expression expression) {
        return Optional.ofNullable(getEnclosingBinaryExpression()).filter(be ->
            be.getLeftExpression() == expression && StaticTypeCheckingSupport.isAssignment(be.getOperation().getType())
        ).isPresent();
    }

    /**
     * Pushes a closure expression into the closure expression stack.
     */
    public void pushEnclosingClosureExpression(final ClosureExpression closureExpression) {
        enclosingClosures.addFirst(new EnclosingClosure(closureExpression));
    }

    /**
     * Pops a closure expression from the closure expression stack.
     */
    public EnclosingClosure popEnclosingClosure() {
        return enclosingClosures.removeFirst();
    }

    /**
     * Returns the closure expression which is on the top of the stack, or null
     * if there's no such element.
     */
    public EnclosingClosure getEnclosingClosure() {
        if (enclosingClosures.isEmpty()) return null;
        return enclosingClosures.getFirst();
    }

    /**
     * Returns the current stack of enclosing closure expressions. The first
     * element is the top of the stack.
     */
    public List<EnclosingClosure> getEnclosingClosureStack() {
        return Collections.unmodifiableList(enclosingClosures);
    }

    /**
     * Pushes a class into the classes stack.
     */
    public void pushEnclosingClassNode(final ClassNode classNode) {
        enclosingClassNodes.addFirst(classNode);
    }

    /**
     * Pops a class from the enclosing classes stack.
     */
    public ClassNode popEnclosingClassNode() {
        return enclosingClassNodes.removeFirst();
    }

    /**
     * Returns the class node which is on the top of the stack, or null
     * if there's no such element.
     */
    public ClassNode getEnclosingClassNode() {
        if (enclosingClassNodes.isEmpty()) return null;
        return enclosingClassNodes.getFirst();
    }

    /**
     * Returns the current stack of enclosing classes. The first
     * element is the top of the stack, that is to say the currently visited class.
     */
    public List<ClassNode> getEnclosingClassNodes() {
        return Collections.unmodifiableList(enclosingClassNodes);
    }

    /**
     * Pushes a method into the method stack.
     */
    public void pushEnclosingMethod(final MethodNode methodNode) {
        enclosingMethods.addFirst(methodNode);
    }

    /**
     * Pops a method from the enclosing methods stack.
     */
    public MethodNode popEnclosingMethod() {
        return enclosingMethods.removeFirst();
    }

    /**
     * Returns the method node which is on the top of the stack, or null
     * if there's no such element.
     */
    public MethodNode getEnclosingMethod() {
        if (enclosingMethods.isEmpty()) return null;
        return enclosingMethods.getFirst();
    }

    /**
     * Returns the current stack of enclosing methods. The first
     * element is the top of the stack, that is to say the last visited method.
     */
    public List<MethodNode> getEnclosingMethods() {
        return Collections.unmodifiableList(enclosingMethods);
    }

    /**
     * Pushes a method call into the method call stack.
     *
     * @param call the call expression to be pushed, either a {@link MethodCallExpression} or a {@link StaticMethodCallExpression}
     */
    public void pushEnclosingMethodCall(final Expression call) {
        if (call instanceof MethodCallExpression || call instanceof StaticMethodCallExpression) {
            enclosingMethodCalls.addFirst(call);
        } else {
            throw new IllegalArgumentException("Expression must be a method call or a static method call");
        }
    }

    /**
     * Pops a method call from the enclosing method call stack.
     */
    public Expression popEnclosingMethodCall() {
        return enclosingMethodCalls.removeFirst();
    }

    /**
     * Returns the method call which is on the top of the stack, or null
     * if there's no such element.
     */
    public Expression getEnclosingMethodCall() {
        if (enclosingMethodCalls.isEmpty()) return null;
        return enclosingMethodCalls.getFirst();
    }

    /**
     * Returns the current stack of enclosing method calls. The first
     * element is the top of the stack, that is to say the currently visited method call.
     */
    public List<Expression> getEnclosingMethodCalls() {
        return Collections.unmodifiableList(enclosingMethodCalls);
    }

    /**
     * Pushes a switch statement into the switch statement stack.
     */
    public void pushEnclosingSwitchStatement(final SwitchStatement switchStatement) {
        switchStatements.addFirst(switchStatement);
    }

    /**
     * Pops a switch statement from the enclosing switch statements stack.
     */
    public SwitchStatement popEnclosingSwitchStatement() {
        return switchStatements.removeFirst();
    }

    /**
     * Returns the switch statement which is on the top of the stack, or null
     * if there's no such element.
     */
    public SwitchStatement getEnclosingSwitchStatement() {
        if (switchStatements.isEmpty()) return null;
        return switchStatements.getFirst();
    }

    /**
     * Returns the current stack of enclosing switch statements. The first
     * element is the top of the stack, that is to say the last visited switch statement.
     */
    public List<SwitchStatement> getEnclosingSwitchStatements() {
        return Collections.unmodifiableList(switchStatements);
    }

    /**
     * Represents the context of an enclosing closure. An enclosing closure wraps
     * a closure expression and the list of return types found in the closure body.
     */
    public static class EnclosingClosure {
        private final ClosureExpression closureExpression;
        private final List<ClassNode> returnTypes = new LinkedList<>();

        /**
         * Creates metadata for an enclosing closure.
         */
        public EnclosingClosure(final ClosureExpression closureExpression) {
            this.closureExpression = closureExpression;
        }

        /**
         * Returns the wrapped closure expression.
         */
        public ClosureExpression getClosureExpression() {
            return closureExpression;
        }

        /**
         * Returns the return types collected for the closure body.
         */
        public List<ClassNode> getReturnTypes() {
            return returnTypes;
        }

        /**
         * Records another inferred closure return type.
         */
        public void addReturnType(final ClassNode type) {
            returnTypes.add(type);
        }

        /**
         * Returns a diagnostic representation of this closure context.
         */
        @Override
        public String toString() {
            return "EnclosingClosure{closureExpression=" + closureExpression.getText() + ", returnTypes=" + returnTypes + "}";
        }
    }
}
