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
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;

import java.util.*;

public class TypeCheckingContext {
    protected SourceUnit source;
    protected ClassNode enclosingClassNode;
    protected Set<MethodNode> methodsToBeVisited = Collections.emptySet();
    protected ErrorCollector errorCollector;
    protected boolean isInStaticContext = false;

    protected final LinkedList<MethodNode> enclosingMethods = new LinkedList<MethodNode>();

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
    protected Stack<Map<Object, List<ClassNode>>> temporaryIfBranchTypeInformation;
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
        enclosingBinaryExpressions.push(binaryExpression);
    }

    /**
     * Pops a binary expression from the binary expression stack.
     * @return the popped binary expression
     */
    public BinaryExpression popEnclosingBinaryExpression() {
        return enclosingBinaryExpressions.pop();
    }

    /**
     * Returns the binary expression which is on the top of the stack, or null
     * if there's no such element.
     * @return the binary expression on top of the stack, or null if no such element.
     */
    public BinaryExpression getEnclosingBinaryExpression() {
        return enclosingBinaryExpressions.peekFirst();
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
        enclosingClosures.push(new EnclosingClosure(closureExpression));
    }

    /**
     * Pops a closure expression from the closure expression stack.
     * @return the popped closure expression
     */
    public EnclosingClosure popEnclosingClosure() {
        return enclosingClosures.pop();
    }

    /**
     * Returns the closure expression which is on the top of the stack, or null
     * if there's no such element.
     * @return the closure expression on top of the stack, or null if no such element.
     */
    public EnclosingClosure getEnclosingClosure() {
        return enclosingClosures.peekFirst();
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
        enclosingMethods.push(methodNode);
    }

    /**
     * Pops a method from the enclosing methods stack.
     * @return the popped method
     */
    public MethodNode popEnclosingMethod() {
        return enclosingMethods.pop();
    }

    /**
     * Returns the method node which is on the top of the stack, or null
     * if there's no such element.
     * @return the enclosing method on top of the stack, or null if no such element.
     */
    public MethodNode getEnclosingMethod() {
        return enclosingMethods.peekFirst();
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
    }
}