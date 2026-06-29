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

import groovy.transform.stc.ClassTag;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.extractGenericsConnections;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsForClassNode;

/**
 * Support for {@link groovy.transform.stc.ClassTag} (GROOVY-12115): matching a call that omits
 * compiler-supplied {@code Class<X>} token argument(s) to the overload that can absorb them, so
 * the static type checker can synthesise the token(s) from the receiver's type argument(s).
 * Matching is a pure computation - the call's AST is only rewritten by an explicit
 * {@link ClassTagMatch#rewriteArguments} once the caller's retried method selection has succeeded,
 * so a failed match never alters how the call as written binds or is reported.
 */
final class ClassTagSupport {

    private static final ClassNode CLASSTAG_CLASSNODE = ClassHelper.make(ClassTag.class);

    private ClassTagSupport() {
    }

    /**
     * <em>Additive</em> matching, consulted when the call as written matched no method (e.g.
     * {@code asChecked()} with its otherwise-mandatory token omitted). Looks across the
     * {@code receivers}, in their usual (delegate/owner) priority order, for the first receiver
     * with an overload of {@code name} that can absorb compiler-supplied {@code Class<X>} tokens:
     * an overload whose non-{@code @ClassTag} parameters number exactly the supplied argument
     * count and whose every {@code @ClassTag} parameter reifies to a concrete class from that
     * receiver's type argument(s). Among such overloads on the matched receiver, the one that
     * reifies the most tokens (strongest checking) is preferred; equally-specific candidates that
     * disagree on the erased classes are too ambiguous, and no match is returned. Static-only by
     * construction (the visitor runs under {@code @TypeChecked}/{@code @CompileStatic}); when a
     * type argument is not statically known no token is synthesised and the call binds as before.
     * <p>
     * Matching is by arity (and reifiability) only - whether the overload's remaining parameters
     * accept the supplied argument types is decided by the caller's retried selection against
     * {@link ClassTagMatch#expandArgumentTypes}, constrained to {@link ClassTagMatch#receiver the
     * matched receiver} so the tokens are always reified from the receiver the call binds on.
     *
     * @return the match to retry selection with, or {@code null} if no injection should occur
     */
    static ClassTagMatch matchAdditive(final StaticTypeCheckingVisitor visitor, final List<Receiver<String>> receivers, final String name, final ClassNode[] args) {
        for (Receiver<String> receiver : receivers) {
            ClassNode receiverType = receiver.getType();
            ClassNode plain = receiverType.getPlainNodeReference();
            List<MethodNode> candidates = new ArrayList<>(visitor.findMethodsWithGenerated(plain, name));
            candidates.addAll(findDGMMethodsForClassNode(visitor.getSourceUnit().getClassLoader(), plain, name));
            ClassTagMatch best = bestMatch(candidates, receiver, receiverType, args.length);
            if (best != null) return best;
        }
        return null;
    }

    /**
     * <em>Preemptive</em> matching, consulted when a token-less overload already matched (the
     * {@code incumbents}) and a {@code @ClassTag} overload might supersede it - e.g. the lenient
     * {@code withDefault(Map, Closure)} superseded by the key-checked
     * {@code withDefault(Map, Class, Closure)}. Preemption changes the meaning of existing source,
     * so three conditions gate it (GROOVY-12115):
     * <ul>
     * <li><em>Intent</em>: the candidate must declare {@code @ClassTag(preempt=true)}; without it
     * an overload is only ever selected additively.</li>
     * <li><em>Containment</em>: candidates are drawn only from the incumbents' owners - for an
     * extension-method incumbent, tagged overloads declared by the same extension class; for an
     * instance-method incumbent, methods of the receiver's own hierarchy. A library can upgrade
     * callers of <em>its own</em> lenient API, but a jar on the compile classpath can never
     * capture existing calls owned by another library.</li>
     * <li><em>Consent</em>: the consuming build may veto all preemption globally via
     * {@link org.codehaus.groovy.control.CompilerConfiguration#isClassTagPreemptionDisabled()}
     * (checked up front via {@link #isPreemptionPossible}).</li>
     * </ul>
     * Tokens are reified from {@code receiver} - the receiver the incumbents bound on - so the
     * upgraded call can never change binding target, only overload.
     *
     * @return the match to retry selection with, or {@code null} if no preemption should occur
     */
    static ClassTagMatch matchPreemption(final StaticTypeCheckingVisitor visitor, final Receiver<String> receiver, final List<MethodNode> incumbents, final String name, final ClassNode[] args) {
        ClassNode receiverType = receiver.getType();
        ClassNode plain = receiverType.getPlainNodeReference();

        // containment: candidates come only from the incumbents' owners
        Set<String> extensionOwners = new HashSet<>();
        boolean instanceIncumbent = false;
        for (MethodNode incumbent : incumbents) {
            if (incumbent instanceof ExtensionMethodNode) {
                extensionOwners.add(((ExtensionMethodNode) incumbent).getExtensionMethodNode().getDeclaringClass().getName());
            } else {
                instanceIncumbent = true;
            }
        }
        List<MethodNode> candidates = new ArrayList<>();
        // gate: skip the extension candidate scan unless some extension overload of this name
        // actually declares preempt intent (out of the box only withDefault does), so ordinary
        // DGM-heavy code - each/collect/... - pays a single set lookup, not a full scan+filter
        ClassLoader loader = visitor.getSourceUnit().getClassLoader();
        if (!extensionOwners.isEmpty() && ExtensionMethodCache.INSTANCE.getPreemptiveNames(loader).contains(name)) {
            for (MethodNode candidate : findDGMMethodsForClassNode(loader, plain, name)) {
                if (candidate instanceof ExtensionMethodNode
                        && extensionOwners.contains(((ExtensionMethodNode) candidate).getExtensionMethodNode().getDeclaringClass().getName())) {
                    candidates.add(candidate);
                }
            }
        }
        if (instanceIncumbent) {
            candidates.addAll(visitor.findMethodsWithGenerated(plain, name));
        }
        candidates.removeIf(candidate -> !declaresPreemptIntent(candidate));

        return bestMatch(candidates, receiver, receiverType, args.length);
    }

    /**
     * Whether preemption is possible at all under the current configuration:
     * {@link org.codehaus.groovy.control.CompilerConfiguration#isClassTagPreemptionDisabled()}
     * disables every preemptive upgrade (additive injection is unaffected). Cheap, so it guards
     * the per-call preemption attempt.
     */
    static boolean isPreemptionPossible(final StaticTypeCheckingVisitor visitor) {
        SourceUnit source = visitor.getSourceUnit();
        return source != null && !source.getConfiguration().isClassTagPreemptionDisabled();
    }

    /**
     * Validates the {@code @ClassTag} parameters of a method compiled from source: the annotation
     * must sit on a {@code Class} parameter; a non-empty {@code @ClassTag("name")} override must
     * resolve to a type variable declared by the method or its enclosing class; and on an instance
     * method the named variable must not be declared by the method itself (a method-declared
     * variable shadows the class's and can never be reified from the receiver, so honouring it
     * would silently disable - or worse, mis-target - injection). Each is reported as a
     * compile-time error rather than left to silently disable injection.
     * <p>
     * This only covers methods compiled from source in the current unit; an extension method from an
     * already-compiled library is never visited here, so a mistake in its declaration is not caught
     * (it just fails to inject - see the matching-side guard in {@link #resolveTagType}). In
     * practice library {@code @ClassTag} parameters use the no-override form, where the name is
     * taken from {@code Class<X>} and cannot be mistyped. Static methods skip the shadowing check:
     * in the extension-method authoring pattern the type variable is necessarily method-declared
     * and connects to the receiver through the self parameter.
     */
    static void validateParameters(final StaticTypeCheckingVisitor visitor, final MethodNode node) {
        for (Parameter parameter : node.getParameters()) {
            if (!hasClassTag(parameter)) continue;
            if (!"java.lang.Class".equals(parameter.getType().getName())) {
                visitor.addStaticTypeError("@ClassTag only applies to a Class parameter", parameter);
                continue;
            }
            String varName = tagOverride(parameter);
            if (!varName.isEmpty() && !namesTypeVariable(node, varName)) {
                visitor.addStaticTypeError("@ClassTag(\"" + varName + "\") does not name a type parameter in scope", parameter);
                continue;
            }
            if (varName.isEmpty()) {
                GenericsType[] gts = parameter.getType().getGenericsTypes();
                if (gts == null || gts.length != 1) continue; // raw or wildcard token: nothing nameable to check
                varName = gts[0].getName();
            }
            if (!node.isStatic() && containsTypeVariable(node.getGenericsTypes(), varName)) {
                visitor.addStaticTypeError("@ClassTag cannot reify type parameter '" + varName + "' declared by the method itself; only the receiver's type arguments are reifiable", parameter);
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * The outcome of matching one overload against a {@code @ClassTag} call: the receiver the
     * tokens were reified from, the overload's parameter count, the (ascending) positions of its
     * {@code @ClassTag} parameters, and the concrete class token resolved for each, in the same
     * order.
     */
    static final class ClassTagMatch {
        final Receiver<String> receiver;
        final int parameterCount;
        final List<Integer> tagPositions;
        final List<ClassNode> tokens;

        private ClassTagMatch(final Receiver<String> receiver, final int parameterCount, final List<Integer> tagPositions, final List<ClassNode> tokens) {
            this.receiver = receiver;
            this.parameterCount = parameterCount;
            this.tagPositions = tagPositions;
            this.tokens = tokens;
        }

        /**
         * Returns the argument types of the call as if the synthesised {@code X.class} token(s) had
         * been written explicitly: each token contributes a {@code Class<X>} at its declared
         * position, and the supplied argument types fill the remaining slots left-to-right. Used to
         * retry method selection without touching the call's AST.
         */
        ClassNode[] expandArgumentTypes(final ClassNode[] suppliedArgs) {
            ClassNode[] expanded = new ClassNode[parameterCount];
            for (int p = 0, s = 0, t = 0; p < parameterCount; p += 1) {
                if (t < tagPositions.size() && tagPositions.get(t) == p) {
                    expanded[p] = GenericsUtils.makeClassSafe0(CLASS_Type, new GenericsType(tokens.get(t).getPlainNodeReference()));
                    t += 1;
                } else {
                    expanded[p] = suppliedArgs[s++];
                }
            }
            return expanded;
        }

        /**
         * Commits the match: inserts the synthesised class-literal argument(s) at their declared
         * positions in {@code argumentList} (the supplied arguments filling the remaining slots
         * left-to-right), so code generation treats the call as if the tokens had been written
         * explicitly. Only call after the retried selection has bound a method.
         */
        void rewriteArguments(final ArgumentListExpression argumentList) {
            List<Expression> supplied = new ArrayList<>(argumentList.getExpressions());
            List<Expression> rebuilt = new ArrayList<>(parameterCount);
            for (int p = 0, s = 0, t = 0; p < parameterCount; p += 1) {
                if (t < tagPositions.size() && tagPositions.get(t) == p) {
                    rebuilt.add(classX(tokens.get(t).getPlainNodeReference()));
                    t += 1;
                } else {
                    rebuilt.add(supplied.get(s++));
                }
            }
            argumentList.getExpressions().clear();
            argumentList.getExpressions().addAll(rebuilt);
        }
    }

    /**
     * Selects the best {@link ClassTagMatch} among {@code candidates}: the overload that reifies
     * the most tokens (strongest checking) wins; equally-specific candidates that disagree on the
     * erased classes are too ambiguous, and {@code null} is returned. Also applies the
     * nothing-to-gain guard: when every token erases to {@code Object} - e.g. an untyped/{@code def}
     * map - no injection occurs, keeping (say) the lenient {@code withDefault} rather than a
     * checked view that could never reject anything.
     */
    private static ClassTagMatch bestMatch(final List<MethodNode> candidates, final Receiver<String> receiver, final ClassNode receiverType, final int suppliedArgCount) {
        ClassTagMatch best = null; // prefer the overload that reifies the most tokens (strongest checking)
        for (MethodNode candidate : candidates) {
            ClassTagMatch match = matchOverload(receiver, candidate, receiverType, suppliedArgCount);
            if (match == null) continue;
            if (best == null || match.tokens.size() > best.tokens.size()) {
                best = match;
            } else if (match.tokens.size() == best.tokens.size() && !sameErasedClasses(best.tokens, match.tokens)) {
                return null; // equally-specific candidates disagree on the tokens; too ambiguous to proceed
            }
        }
        if (best != null && best.tokens.stream().allMatch(token -> isObjectType(token))) return null;
        return best;
    }

    /**
     * Whether the candidate declares preemption intent: any of its {@code @ClassTag} parameters
     * carries {@code preempt=true}. (Convention is to set it on all of them; any one suffices.)
     */
    static boolean declaresPreemptIntent(final MethodNode candidate) {
        for (Parameter parameter : candidate.getParameters()) {
            if (hasClassTag(parameter) && tagPreempt(parameter)) return true;
        }
        return false;
    }

    /**
     * If {@code candidate} has one or more {@code @ClassTag} parameters such that the remaining
     * parameters number exactly {@code suppliedArgCount}, and every tag reifies to a concrete class
     * from the receiver's type argument(s), returns the match; otherwise {@code null} (not a match,
     * or a type variable that is not statically reifiable - the fail-soft case in which no
     * injection happens and the call binds as before).
     */
    private static ClassTagMatch matchOverload(final Receiver<String> receiver, final MethodNode candidate, final ClassNode receiverType, final int suppliedArgCount) {
        Parameter[] params = candidate.getParameters();
        List<Integer> tagPositions = new ArrayList<>();
        for (int i = 0; i < params.length; i += 1) {
            if (hasClassTag(params[i])) tagPositions.add(i);
        }
        if (tagPositions.isEmpty() || params.length - tagPositions.size() != suppliedArgCount) return null;

        // map the receiver's actual type argument(s) onto the declaring/self type variables
        Map<GenericsTypeName, GenericsType> spec = new HashMap<>();
        GenericsType[] methodTypeVariables;
        if (candidate instanceof ExtensionMethodNode) {
            Parameter[] declared = ((ExtensionMethodNode) candidate).getExtensionMethodNode().getParameters();
            if (declared.length == 0) return null;
            extractGenericsConnections(spec, receiverType, declared[0].getType()); // self param carries <T> / <K,V>
            methodTypeVariables = null; // the self parameter is the connection, so method-declared variables are the mechanism
        } else {
            extractGenericsConnections(spec, receiverType, candidate.getDeclaringClass());
            methodTypeVariables = candidate.getGenericsTypes(); // a method-declared variable shadows the class's; it cannot be reified from the receiver
        }

        List<ClassNode> tokens = new ArrayList<>(tagPositions.size());
        for (int pos : tagPositions) {
            ClassNode token = resolveTagType(params[pos], spec, methodTypeVariables);
            if (token == null) return null; // a tag could not be reified; degrade (no injection)
            tokens.add(token);
        }
        return new ClassTagMatch(receiver, params.length, tagPositions, tokens);
    }

    /**
     * Resolves the concrete class that a single {@code @ClassTag Class<X>} parameter reifies, using
     * the supplied receiver placeholder map. Honours an explicit {@code @ClassTag("name")} override.
     * Returns {@code null} when the type variable cannot be statically determined, or when it is
     * declared by the method itself ({@code methodTypeVariables}) - per Java scoping such a variable
     * shadows any like-named class variable, so reifying the receiver's would inject the wrong class.
     */
    private static ClassNode resolveTagType(final Parameter tagParam, final Map<GenericsTypeName, GenericsType> spec, final GenericsType[] methodTypeVariables) {
        ClassNode paramType = tagParam.getType();
        if (!"java.lang.Class".equals(paramType.getName())) return null; // @ClassTag only applies to Class tokens
        String varName = tagOverride(tagParam);
        if (varName.isEmpty()) {
            GenericsType[] gts = paramType.getGenericsTypes();
            if (gts == null || gts.length != 1) return null; // raw Class with no override: nothing to reify
            varName = gts[0].getName();
        }
        if (containsTypeVariable(methodTypeVariables, varName)) return null; // shadowed by the method's own declaration
        GenericsType resolved = spec.get(new GenericsTypeName(varName));
        if (resolved == null) return null;
        ClassNode type = resolved.getType();
        if (type == null || type.isGenericsPlaceHolder() || GenericsUtils.hasUnresolvedGenerics(type)) return null;
        return type;
    }

    private static boolean namesTypeVariable(final MethodNode node, final String name) {
        if (containsTypeVariable(node.getGenericsTypes(), name)) return true;
        ClassNode declaringClass = node.getDeclaringClass();
        return declaringClass != null && containsTypeVariable(declaringClass.getGenericsTypes(), name);
    }

    private static boolean containsTypeVariable(final GenericsType[] typeVariables, final String name) {
        if (typeVariables != null) {
            for (GenericsType typeVariable : typeVariables) {
                if (typeVariable.getName().equals(name)) return true;
            }
        }
        return false;
    }

    private static boolean hasClassTag(final Parameter parameter) {
        return !parameter.getAnnotations(CLASSTAG_CLASSNODE).isEmpty();
    }

    private static String tagOverride(final Parameter parameter) {
        AnnotationNode tag = parameter.getAnnotations(CLASSTAG_CLASSNODE).get(0);
        Expression member = tag.getMember("value");
        return (member instanceof ConstantExpression) ? member.getText() : "";
    }

    private static boolean tagPreempt(final Parameter parameter) {
        AnnotationNode tag = parameter.getAnnotations(CLASSTAG_CLASSNODE).get(0);
        Expression member = tag.getMember("preempt");
        return member instanceof ConstantExpression && Boolean.TRUE.equals(((ConstantExpression) member).getValue());
    }

    private static boolean sameErasedClasses(final List<ClassNode> a, final List<ClassNode> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i += 1) {
            if (!a.get(i).getName().equals(b.get(i).getName())) return false;
        }
        return true;
    }
}
