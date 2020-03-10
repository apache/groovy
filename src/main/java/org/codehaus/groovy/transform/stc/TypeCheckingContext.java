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
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class TypeCheckingContext {

    private final SourceUnit source;

    public TypeCheckingContext(final SourceUnit source) {
        this.source = source;
        pushErrorCollector(source.getErrorCollector());
    }

    @Deprecated
    public TypeCheckingContext(final StaticTypeCheckingVisitor visitor) {
        this.source = null;
    }

    public SourceUnit getSource() {
        return source;
    }

    //--------------------------------------------------------------------------

    private CompilationUnit compilationUnit;

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    //--------------------------------------------------------------------------

    private final LinkedList<ErrorCollector> errorCollectors = new LinkedList<>();

    public ErrorCollector pushErrorCollector() {
        CompilerConfiguration config = Optional.ofNullable(getErrorCollector())
                .map(ErrorCollector::getConfiguration).orElseGet(() -> getSource().getConfiguration());

        ErrorCollector collector = new ErrorCollector(config);
        pushErrorCollector(collector);
        return collector;
    }

    public void pushErrorCollector(final ErrorCollector collector) {
        errorCollectors.addFirst(collector);
    }

    public ErrorCollector popErrorCollector() {
        return errorCollectors.removeFirst();
    }

    public ErrorCollector getErrorCollector() {
        if (errorCollectors.isEmpty()) {
            return null;
        }
        return errorCollectors.getFirst();
    }

    public List<ErrorCollector> getErrorCollectors() {
        return Collections.unmodifiableList(errorCollectors);
    }

    //--------------------------------------------------------------------------

    /**
     * Stores information which is only valid in the "if" branch of an if-then-else statement. This is used when the if
     * condition expression makes use of an instanceof check
     */
    protected final Stack<Map<Object, List<ClassNode>>> temporaryIfBranchTypeInformation = new Stack<>();

    public void pushTemporaryTypeInfo() {
        Map<Object, List<ClassNode>> potentialTypes = new HashMap<>();
        temporaryIfBranchTypeInformation.push(potentialTypes);
    }

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

    protected boolean isInStaticContext;

    /**
     * The type of the last encountered "it" implicit parameter.
     */
    protected ClassNode lastImplicitItType;

    protected Set<MethodNode> methodsToBeVisited = Collections.emptySet();

    /**
     * This field is used to track assignments in if/else branches, for loops and
     * while loops. For example, in the following code: <code>if (cond) { x = 1 } else { x = '123' }</code>
     * the inferred type of x after the if/else statement should be the LUB of int and String.
     */
    protected Map<VariableExpression, List<ClassNode>> ifElseForWhileAssignmentTracker;

    /**
     * This field used for type derivation
     * Check IfStatement matched pattern:
     * Object var1;
     * if (!(var1 instanceOf Runnable)){
     * return
     * }
     * // Here var1 instance of Runnable
     */
    protected final Map<BlockStatement, Map<VariableExpression, List<ClassNode>>> blockStatements2Types = new IdentityHashMap<>();

    protected Set<MethodNode> alreadyVisitedMethods = new HashSet<>();

    /**
     * Some expressions need to be visited twice, because type information may be insufficient at some point. For
     * example, for closure shared variables, we need a first pass to collect every type which is assigned to a closure
     * shared variable, then a second pass to ensure that every method call on such a variable is made on a LUB.
     */
    protected final Set<SecondPassExpression> secondPassExpressions = new LinkedHashSet<>();

    /**
     * A map used to store every type used in closure shared variable assignments. In a second pass, we will compute the
     * LUB of each type and check that method calls on those variables are valid.
     */
    protected final Map<VariableExpression, List<ClassNode>> closureSharedVariablesAssignmentTypes = new HashMap<>();

    protected final Map<Parameter, ClassNode> controlStructureVariables = new HashMap<>();

    // this map is used to ensure that two errors are not reported on the same line/column
    protected final Set<Long> reportedErrors = new TreeSet<>();

    //--------------------------------------------------------------------------

    // TODO: replace with general node stack and filters
    private final LinkedList<ClassNode> enclosingClassNodes = new LinkedList<>();
    private final LinkedList<MethodNode> enclosingMethods = new LinkedList<>();
    private final LinkedList<Expression> enclosingMethodCalls = new LinkedList<>();
    protected final LinkedList<BlockStatement> enclosingBlocks = new LinkedList<>();
    private final LinkedList<SwitchStatement> switchStatements = new LinkedList<>();
    private final LinkedList<EnclosingClosure> enclosingClosures = new LinkedList<>();
    // stores the current binary expression. This is used when assignments are made with a null object, for type inference
    private final LinkedList<BinaryExpression> enclosingBinaryExpressions = new LinkedList<>();

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

        public EnclosingClosure(final ClosureExpression closureExpression) {
            this.closureExpression = closureExpression;
        }

        public ClosureExpression getClosureExpression() {
            return closureExpression;
        }

        public List<ClassNode> getReturnTypes() {
            return returnTypes;
        }

        public void addReturnType(final ClassNode type) {
            returnTypes.add(type);
        }

        @Override
        public String toString() {
            return "EnclosingClosure{closureExpression=" + closureExpression.getText() + ", returnTypes=" + returnTypes + "}";
        }
    }
}
