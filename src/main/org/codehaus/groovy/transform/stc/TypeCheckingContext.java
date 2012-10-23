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
    protected ClassNode classNode;
    protected MethodNode methodNode;
    protected Set<MethodNode> methodsToBeVisited = Collections.emptySet();
    protected ErrorCollector errorCollector;
    protected boolean isInStaticContext = false;// used for closure return type inference
    protected ClosureExpression closureExpression;
    protected List<ClassNode> closureReturnTypes;// whenever a method using a closure as argument (typically, "with") is detected, this list is updated
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
    protected Map<Parameter, ClassNode> controlStructureVariables = new HashMap<Parameter, ClassNode>();// this map is used to ensure that two errors are not reported on the same line/column
    protected final Set<Long> reportedErrors = new TreeSet<Long>();// stores the current binary expresssion. This is used when assignments are made with a null object, for type
    // inference
    protected BinaryExpression currentBinaryExpression;

    public TypeCheckingContext(final StaticTypeCheckingVisitor staticTypeCheckingVisitor) {
    }
}