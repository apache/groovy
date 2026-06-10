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
package org.apache.groovy.contracts.generation;

import org.apache.groovy.contracts.annotations.meta.Postcondition;
import org.apache.groovy.contracts.ast.visitor.AnnotationClosureVisitor;
import org.apache.groovy.contracts.ast.visitor.BaseVisitor;
import org.apache.groovy.contracts.util.AnnotationUtils;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.ReaderSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.mapX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveVoid;

/**
 * <p>
 * Code generator for postconditions.
 * </p>
 */
public class PostconditionGenerator extends BaseGenerator {
    private static final String METHOD_PROCESSED = "org.apache.groovy.contracts.POSTCONDITION_PROCESSED";

    /**
     * Creates a generator for postcondition support code.
     *
     * @param source the reader source backing the current source unit
     */
    public PostconditionGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Adds a synthetic method to the given <tt>classNode</tt> which can be used
     * to create a map of most instance variables found in this class. Used for the <tt>old</tt> variable
     * mechanism.
     *
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} to add the synthetic method to
     */
    public void addOldVariablesMethod(final ClassNode classNode) {
        OldVariableGenerationUtility.addOldVariableMethodNode(classNode);
    }

    /**
     * Injects a postcondition assertion statement in the given <tt>method</tt>, based on the <tt>booleanExpression</tt>.
     *
     * @param method        the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param postcondition the {@link org.apache.groovy.contracts.domain.Postcondition} the assertion statement should be generated from
     */
    public void generatePostconditionAssertionStatement(MethodNode method, org.apache.groovy.contracts.domain.Postcondition postcondition) {

        final BooleanExpression postconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Postcondition.class, postcondition.booleanExpression(), true);


        BlockStatement blockStatement;
        final BlockStatement originalBlockStatement = postcondition.originalBlockStatement();
        // if useExecutionTracker flag is found in the meta-data the annotation closure visitor discovered
        // method calls which might be subject to cycling boolean expressions -> no inline mode possible
        final boolean useExecutionTracker = originalBlockStatement == null || Boolean.TRUE.equals(originalBlockStatement.getNodeMetaData(AnnotationClosureVisitor.META_DATA_USE_EXECUTION_TRACKER));

        if (!useExecutionTracker && Boolean.TRUE.equals(method.getNodeMetaData(META_DATA_USE_INLINE_MODE))) {
            blockStatement = getInlineModeBlockStatement(originalBlockStatement);
        } else {
            blockStatement = wrapAssertionBooleanExpression(method.getDeclaringClass(), method, postconditionBooleanExpression, "postcondition");
        }

        addPostcondition(method, blockStatement);
    }

    /**
     * Adds a default postcondition if a postcondition has already been defined for this {@link org.codehaus.groovy.ast.MethodNode}
     * in a super-class.
     *
     * @param type   the current {@link org.codehaus.groovy.ast.ClassNode} of the given <tt>methodNode</tt>
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to create the default postcondition for
     */
    public void generateDefaultPostconditionStatement(final ClassNode type, final MethodNode method) {
        boolean noPostconditionInHierarchy = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), method, ClassHelper.makeWithoutCaching(Postcondition.class)).isEmpty();
        if (noPostconditionInHierarchy) return;

        // if another post-condition is available we need to add a default expression of TRUE
        // since post-conditions are usually connected with a logical AND
        final BooleanExpression postconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Postcondition.class, boolX(ConstantExpression.TRUE), true);
        if (postconditionBooleanExpression.getExpression() == ConstantExpression.TRUE) return;

        final BlockStatement blockStatement = wrapAssertionBooleanExpression(type, method, postconditionBooleanExpression, "postcondition");
        addPostcondition(method, blockStatement);
    }

    private void addPostcondition(MethodNode method, BlockStatement postconditionBlockStatement) {
        if (Boolean.TRUE.equals(method.getNodeMetaData(METHOD_PROCESSED))) return;
        final BlockStatement block = (BlockStatement) method.getCode();

        final List<Statement> statements = block.getStatements();
        if (!statements.isEmpty()) {
            Expression contractsEnabled = localVarX(BaseVisitor.GCONTRACTS_ENABLED_VAR, ClassHelper.boolean_TYPE);

            if (!isPrimitiveVoid(method.getReturnType())) {
                // if return type is not void, then a "result" variable is provided in the postcondition expression
                List<ReturnStatement> returnStatements = AssertStatementCreationUtility.getReturnStatements(method);

                for (ReturnStatement returnStatement : returnStatements) {
                    BlockStatement localPostconditionBlockStatement = block(new VariableScope(), postconditionBlockStatement.getStatements());

                    Expression result = localVarX("result", method.getReturnType());
                    localPostconditionBlockStatement.getStatements().add(0, declS(result, returnStatement.getExpression()));
                    AssertStatementCreationUtility.injectResultVariableReturnStatementAndAssertionCallStatement(block, method.getReturnType().redirect(), returnStatement, localPostconditionBlockStatement);
                }
                setOldVariablesIfEnabled(block, contractsEnabled, method);

            } else if (method instanceof ConstructorNode) {
                block.addStatements(postconditionBlockStatement.getStatements());
            } else {
                setOldVariablesIfEnabled(block, contractsEnabled, method);
                block.addStatements(postconditionBlockStatement.getStatements());
            }
            method.putNodeMetaData(METHOD_PROCESSED, true);
        }
    }

    private void setOldVariablesIfEnabled(BlockStatement block, Expression contractsEnabled, MethodNode method) {
        final Expression oldVariableExpression = localVarX("old", new ClassNode(Map.class));
        if (method.isStatic()) {
            // static methods have no instance state to snapshot; the synthetic old-variables method is an
            // instance method, so 'old' starts as an empty map. GROOVY-12078: it is still populated with
            // the entry values of any parameters referenced via old.<name> (field references stay rejected)
            block.getStatements().add(0, declS(oldVariableExpression, mapX()));
            List<Statement> parameterSnapshots = parameterSnapshotStatements(method);
            if (!parameterSnapshots.isEmpty()) {
                block.getStatements().add(1, ifS(boolX(contractsEnabled), block(new VariableScope(), parameterSnapshots)));
            }
            return;
        }
        // Snapshot instance field state into a local variable: Map old = $_gc_computeOldVariables()
        final List<Statement> oldSetup = new ArrayList<>();
        oldSetup.add(assignS(oldVariableExpression, callThisX(OldVariableGenerationUtility.OLD_VARIABLES_METHOD)));
        // GROOVY-12078: also snapshot the entry values of parameters referenced via old.<name>, so a
        // postcondition can compare against an argument that the method body reassigns
        oldSetup.addAll(parameterSnapshotStatements(method));

        block.getStatements().add(0, declS(oldVariableExpression, ConstantExpression.NULL));
        block.getStatements().add(1, ifS(boolX(contractsEnabled), block(new VariableScope(), oldSetup)));
    }

    /**
     * Builds {@code old.put('name', name)} statements for each method parameter referenced via
     * {@code old.<name>} in the postcondition. Parameters are snapshotted after the field state so a
     * parameter shadowing a field name wins, mirroring lexical scoping. {@code final} (and {@code val})
     * parameters are snapshotted too: {@code final} prevents reassignment but not in-place mutation, so
     * {@code old.<name>} must still capture the entry state.
     *
     * @param method the method whose postcondition is being generated
     * @return the list of snapshot statements (possibly empty)
     */
    private static List<Statement> parameterSnapshotStatements(final MethodNode method) {
        final Set<String> oldReferences = method.getNodeMetaData(AnnotationClosureVisitor.OLD_REFERENCES_KEY);
        if (oldReferences == null || oldReferences.isEmpty()) return List.of();

        final List<Statement> statements = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (!oldReferences.contains(parameter.getName())) continue;
            // old.put('name', name) - snapshotExpression defensively copies a mutable cloneable value,
            // but stores an immutable (or otherwise uncopyable) value by reference
            Expression snapshot = OldVariableGenerationUtility.snapshotExpression(parameter.getType(), varX(parameter));
            statements.add(stmt(callX(varX("old"), "put", args(constX(parameter.getName()), snapshot))));
        }
        return statements;
    }
}
