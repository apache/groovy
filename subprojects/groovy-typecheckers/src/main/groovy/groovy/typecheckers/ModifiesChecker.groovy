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
package groovy.typecheckers

import org.apache.groovy.ast.tools.ImmutablePropertyUtils
import org.apache.groovy.lang.annotation.Incubating
import org.apache.groovy.typecheckers.CheckingVisitor
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.codehaus.groovy.transform.stc.StaticTypesMarker

import static org.codehaus.groovy.syntax.Types.isAssignment

/**
 * A compile-time checker that verifies method bodies comply with their
 * {@code @Modifies} frame condition declarations. Checks that:
 * <ul>
 *     <li>Direct field assignments only target fields listed in {@code @Modifies}</li>
 *     <li>Calls to methods on {@code this} are compatible with the declared frame</li>
 *     <li>Calls on parameters/variables not in {@code @Modifies} use only non-mutating methods</li>
 * </ul>
 * <p>
 * Non-mutating calls are determined by:
 * <ol>
 *     <li>Receiver type is immutable (String, Integer, etc.)</li>
 *     <li>Method is annotated {@code @Pure}</li>
 *     <li>Method name is in a known-safe whitelist (toString, size, get, etc.)</li>
 * </ol>
 * <p>
 * This checker is opt-in:
 * <pre>
 * {@code @TypeChecked(extensions = 'groovy.typecheckers.ModifiesChecker')}
 * </pre>
 *
 * @since 6.0.0
 * @see groovy.contracts.Modifies
 */
@Incubating
class ModifiesChecker extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {

    // Methods known to be non-mutating on common mutable types
    private static final Set<String> SAFE_METHOD_NAMES = Set.of(
            // Object fundamentals
            'toString', 'hashCode', 'equals', 'compareTo', 'getClass',
            // Collection/Map queries
            'size', 'length', 'isEmpty', 'contains', 'containsKey', 'containsValue',
            'get', 'getAt', 'getOrDefault', 'indexOf', 'lastIndexOf',
            'iterator', 'listIterator', 'spliterator',
            'stream', 'parallelStream',
            'toArray', 'toList', 'toSet', 'toSorted', 'toUnique',
            'subList', 'headSet', 'tailSet', 'subSet',
            'keySet', 'values', 'entrySet',
            'first', 'last', 'head', 'tail', 'init',
            'find', 'findAll', 'collect', 'collectEntries',
            'any', 'every', 'count', 'sum', 'min', 'max',
            'join', 'inject', 'groupBy', 'countBy',
            'each', 'eachWithIndex', 'reverseEach',
            // String queries
            'charAt', 'substring', 'trim', 'strip', 'toLowerCase', 'toUpperCase',
            'startsWith', 'endsWith', 'matches', 'split',
            // Array queries
            'clone'
    )

    private static final Set<String> PURE_ANNOS = Set.of('Pure')
    private static final Set<String> SIDE_EFFECT_FREE_ANNOS = Set.of('SideEffectFree')
    private static final Set<String> CONTRACT_ANNOS = Set.of('Contract')

    /**
     * Registers frame-condition checks for methods visited by the type checker.
     */
    @Override
    Object run() {
        afterVisitMethod { MethodNode mn ->
            // Key matches ModifiesASTTransformation.MODIFIES_FIELDS_KEY — no hard dependency on groovy-contracts
            Set<String> modifiesSet = mn.getNodeMetaData('groovy.contracts.modifiesFields') as Set<String>
            if (modifiesSet == null && isPureEquivalent(mn)) {
                modifiesSet = Collections.emptySet() // @Pure/@SideEffectFree/@Contract(pure=true) implies @Modifies({})
            }
            if (modifiesSet == null) return // no @Modifies or purity annotation — nothing to check

            mn.code?.visit(makeVisitor(modifiesSet, mn))
        }
    }

    /** Checks for @Pure, @SideEffectFree, or @Contract(pure=true). */
    private static boolean isPureEquivalent(MethodNode method) {
        hasPureAnno(method) || hasSideEffectFreeAnno(method) || hasContractPureAnno(method)
    }

    private static boolean hasPureAnno(MethodNode method) {
        method.annotations?.any { it.classNode?.nameWithoutPackage in PURE_ANNOS } ?: false
    }

    private static boolean hasSideEffectFreeAnno(MethodNode method) {
        method.annotations?.any { it.classNode?.nameWithoutPackage in SIDE_EFFECT_FREE_ANNOS } ?: false
    }

    /**
     * Checks for {@code @Contract(pure = true)} (JetBrains annotations).
     */
    private static boolean hasContractPureAnno(MethodNode method) {
        method.annotations?.any { anno ->
            anno.classNode?.nameWithoutPackage in CONTRACT_ANNOS &&
                    anno.getMember('pure')?.text == 'true'
        } ?: false
    }

    /**
     * Parses {@code @Contract(mutates = "this,param1")} into a set of mutation targets.
     * Returns null if no mutates attribute is present.
     * Maps "this" to field names (all fields) and "param1","param2" etc. to parameter names.
     */
    private static Set<String> parseContractMutates(MethodNode callee) {
        for (anno in callee.annotations) {
            if (anno.classNode?.nameWithoutPackage in CONTRACT_ANNOS) {
                def mutatesExpr = anno.getMember('mutates')
                if (mutatesExpr != null) {
                    String mutatesStr = mutatesExpr.text
                    if (mutatesStr == null || mutatesStr.isEmpty()) return Collections.emptySet()
                    Set<String> result = new LinkedHashSet<>()
                    def params = callee.parameters
                    for (String token : mutatesStr.split(',')) {
                        token = token.trim()
                        if (token == 'this') {
                            // "this" means the callee mutates its own receiver
                            result.add('this')
                        } else if (token.startsWith('param')) {
                            // "param1" = first parameter (1-based)
                            try {
                                int idx = Integer.parseInt(token.substring(5)) - 1
                                if (idx >= 0 && idx < params.length) {
                                    result.add(params[idx].name)
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    return result
                }
            }
        }
        null
    }

    private CheckingVisitor makeVisitor(Set<String> modifiesSet, MethodNode methodNode) {
        Set<String> paramNames = methodNode.parameters*.name as Set<String>

        new CheckingVisitor() {

            @Override
            void visitDeclarationExpression(DeclarationExpression decl) {
                super.visitDeclarationExpression(decl)
                // Local variable declarations are always fine
            }

            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                super.visitBinaryExpression(expression)
                if (isAssignment(expression.operation.type)) {
                    checkWriteTarget(expression.leftExpression, expression)
                }
            }

            @Override
            void visitPostfixExpression(PostfixExpression expression) {
                super.visitPostfixExpression(expression)
                checkWriteTarget(expression.expression, expression)
            }

            @Override
            void visitPrefixExpression(PrefixExpression expression) {
                super.visitPrefixExpression(expression)
                checkWriteTarget(expression.expression, expression)
            }

            @Override
            void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call)
                checkMethodCall(call.objectExpression, call)
            }

            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                super.visitStaticMethodCallExpression(call)
                checkStaticCall(call)
            }

            // ---- helpers ----

            private void checkWriteTarget(Expression target, Expression context) {
                if (target instanceof PropertyExpression) {
                    def objExpr = target.objectExpression
                    if (objExpr instanceof VariableExpression && objExpr.isThisExpression()) {
                        String fieldName = target.propertyAsString
                        if (fieldName && !modifiesSet.contains(fieldName)) {
                            addStaticTypeError("@Modifies violation: assignment to 'this.${fieldName}' but '${fieldName}' is not declared in @Modifies", context)
                        }
                    }
                } else if (target instanceof VariableExpression) {
                    // Check if this is actually a field (implicit this)
                    Variable accessedVar = findTargetVariable(target)
                    if (accessedVar instanceof FieldNode) {
                        String fieldName = accessedVar.name
                        if (!modifiesSet.contains(fieldName)) {
                            addStaticTypeError("@Modifies violation: assignment to '${fieldName}' but '${fieldName}' is not declared in @Modifies", context)
                        }
                    }
                    // Local variables and parameters being reassigned are fine
                }
            }

            private void checkMethodCall(Expression receiver, MethodCallExpression call) {
                if (receiver instanceof VariableExpression && receiver.isThisExpression()) {
                    checkCallOnThis(call)
                } else if (receiver instanceof VariableExpression || receiver instanceof PropertyExpression) {
                    checkCallOnVariable(receiver, call)
                }
            }

            private void checkCallOnThis(MethodCallExpression call) {
                // Check if the callee method has @Modifies
                def targetMethod = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (!(targetMethod instanceof MethodNode)) return

                Set<String> calleeModifies = targetMethod.getNodeMetaData('groovy.contracts.modifiesFields') as Set<String>

                if (calleeModifies != null) {
                    // Callee has @Modifies — check it's a subset of our frame
                    for (String field : calleeModifies) {
                        if (!modifiesSet.contains(field)) {
                            addStaticTypeError("@Modifies violation: call to '${targetMethod.name}()' modifies '${field}' which is not in this method's @Modifies", call)
                        }
                    }
                } else if (isPureEquivalent(targetMethod)) {
                    // @Pure, @SideEffectFree, @Contract(pure=true) methods are safe
                } else {
                    // Check @Contract(mutates=...) on callee
                    Set<String> contractMutates = parseContractMutates(targetMethod)
                    if (contractMutates != null) {
                        if (contractMutates.isEmpty()) return // mutates nothing — pure
                        // Check parameter mutations (arguments that map to mutated params)
                        checkContractParamMutations(contractMutates, targetMethod, call)
                        // "this" in mutates means the callee mutates its receiver
                        if (contractMutates.contains('this')) {
                            addStaticTypeError("@Modifies warning: call to 'this.${call.methodAsString}()' declares @Contract(mutates=\"this\") — may modify fields not in @Modifies", call)
                        }
                        return
                    } else if (SAFE_METHOD_NAMES.contains(call.methodAsString)) {
                        // Known-safe method name
                    } else {
                        // Unknown effects — warn
                        addStaticTypeError("@Modifies warning: call to 'this.${call.methodAsString}()' has unknown effects (consider adding @Modifies or @Pure)", call)
                    }
                }
            }

            private void checkCallOnVariable(Expression receiver, MethodCallExpression call) {
                String receiverName = resolveReceiverName(receiver)

                // If receiver is in the modifies set, any call is allowed
                if (receiverName && modifiesSet.contains(receiverName)) return

                // Check if the method is known to be safe
                def targetMethod = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)

                // Immutable receiver type — all calls safe
                ClassNode receiverType = getType(receiver)
                if (receiverType && ImmutablePropertyUtils.isBuiltinImmutable(receiverType.name)) return

                // @Pure, @SideEffectFree, @Contract(pure=true) method
                if (targetMethod instanceof MethodNode && isPureEquivalent(targetMethod)) return

                // @Contract(mutates=...) — check if this call mutates the receiver
                if (targetMethod instanceof MethodNode) {
                    Set<String> contractMutates = parseContractMutates(targetMethod)
                    if (contractMutates != null) {
                        if (contractMutates.isEmpty()) return // mutates nothing
                        // Check if the callee mutates "this" (the receiver variable)
                        // and map paramN to the actual argument expressions
                        if (!contractMutates.contains('this')) {
                            // Callee doesn't mutate its receiver — safe for our variable
                            // But check if any mutated params map to our non-modifiable variables
                            checkContractParamMutations(contractMutates, targetMethod, call)
                            return
                        }
                        // Callee mutates "this" = our receiver variable
                        if (receiverName) {
                            addStaticTypeError("@Modifies warning: call to '${receiverName}.${call.methodAsString}()' may modify '${receiverName}' which is not in @Modifies", call)
                        }
                        return
                    }
                }

                // Known-safe method name
                if (SAFE_METHOD_NAMES.contains(call.methodAsString)) return

                // Unknown — warn
                if (receiverName) {
                    addStaticTypeError("@Modifies warning: call to '${receiverName}.${call.methodAsString}()' may modify '${receiverName}' which is not in @Modifies", call)
                }
            }

            private void checkStaticCall(StaticMethodCallExpression call) {
                def targetMethod = call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET)
                if (!(targetMethod instanceof MethodNode)) return

                // Check @Contract(mutates=...) on static callee
                Set<String> contractMutates = parseContractMutates(targetMethod)
                if (contractMutates != null && !contractMutates.isEmpty()) {
                    // Static methods can't mutate "this", but can mutate params
                    def args = call.arguments
                    if (args instanceof org.codehaus.groovy.ast.expr.TupleExpression) {
                        def argList = args.expressions
                        def params = targetMethod.parameters
                        for (int i = 0; i < params.length && i < argList.size(); i++) {
                            if (contractMutates.contains(params[i].name)) {
                                String argName = resolveReceiverName(argList[i])
                                if (argName && !modifiesSet.contains(argName)) {
                                    addStaticTypeError("@Modifies warning: argument '${argName}' passed to '${call.methodAsString}()' parameter '${params[i].name}' which is declared as mutated", call)
                                }
                            }
                        }
                    }
                }
            }

            /**
             * For @Contract(mutates="param1,param2"), check if the actual arguments
             * at the call site are variables not in our modifies set.
             */
            private void checkContractParamMutations(Set<String> contractMutates, MethodNode callee, MethodCallExpression call) {
                def args = call.arguments
                if (!(args instanceof org.codehaus.groovy.ast.expr.TupleExpression)) return
                def argList = args.expressions
                def params = callee.parameters

                for (int i = 0; i < params.length && i < argList.size(); i++) {
                    if (contractMutates.contains(params[i].name)) {
                        // This parameter is mutated — check what argument was passed
                        String argName = resolveReceiverName(argList[i])
                        if (argName && !modifiesSet.contains(argName)) {
                            addStaticTypeError("@Modifies warning: argument '${argName}' passed to '${call.methodAsString}()' parameter '${params[i].name}' which is declared as mutated", call)
                        }
                    }
                }
            }

            private String resolveReceiverName(Expression receiver) {
                if (receiver instanceof VariableExpression) {
                    return receiver.name
                }
                if (receiver instanceof PropertyExpression) {
                    def obj = receiver.objectExpression
                    if (obj instanceof VariableExpression && obj.isThisExpression()) {
                        return receiver.propertyAsString
                    }
                }
                null
            }

        }
    }
}
