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
package org.apache.groovy.ast.pattern;

import org.apache.groovy.ast.pattern.MatchPattern.BindingPattern;
import org.apache.groovy.ast.pattern.MatchPattern.ConstantPattern;
import org.apache.groovy.ast.pattern.MatchPattern.ListPattern;
import org.apache.groovy.ast.pattern.MatchPattern.MapPattern;
import org.apache.groovy.ast.pattern.MatchPattern.RecordPattern;
import org.apache.groovy.ast.pattern.MatchPattern.RestPattern;
import org.apache.groovy.ast.pattern.MatchPattern.TypePattern;
import org.apache.groovy.ast.pattern.MatchPattern.WildcardPattern;
import org.apache.groovy.lang.annotation.Incubating;
import org.apache.groovy.runtime.ListPatternSupport;
import org.apache.groovy.runtime.MapPatternSupport;
import org.apache.groovy.runtime.RecordPatternSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PatternExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.geX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.minusX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Derives the matching code of every {@link PatternExpression} in a module
 * (GEP-19). It is the first operation of instruction selection, so that:
 * <ul>
 * <li>resolution, scoping, AST transforms and compilation customizers of the
 *     earlier phases work on the pattern alone, as do tools that compile to
 *     one of those phases, such as the AST browser of the console and the
 *     language server;</li>
 * <li>the type checker and code generation, which follow, see ordinary code
 *     with the same meaning in dynamic and in static compilation, while the
 *     pattern itself stays on the {@code PatternExpression} for the decisions
 *     that need it, such as exhaustiveness and the types of bindings.</li>
 * </ul>
 * The pattern cannot change once written, and the lowered code is a function
 * of it, so the two never disagree. A transform that runs during or after
 * instruction selection and creates a pattern, or a switch with pattern arms,
 * lowers the class it changed with {@link #lower(ClassNode, SourceUnit)}
 * before class generation; lowering a class twice is harmless.
 * <p>
 * A pattern arm of a switch becomes a boolean test on a synthetic subject
 * variable, which the bytecode writers bind to the evaluated selector, and
 * matching steps at the start of the arm, each of the form
 * {@code if (!(check)) break <arm>}, where the arm label is the case
 * statement's statement label and the break continues with the next case test:
 * <pre>
 * switch (x) {
 *   case Integer i when i &gt; 0 -&gt; 'p'
 *   default                     -&gt; 'z'
 * }
 * </pre>
 * becomes (with {@code __$$pv0} bound to {@code x})
 * <pre>
 * switch (x) {
 *   case __$$pv0 instanceof Integer -&gt; { __$$arm1: if (!(__$$pv0 instanceof Integer i)) break __$$arm1
 *                                              if (!(i &gt; 0)) break __$$arm1
 *                                              'p' }
 *   default                         -&gt; 'z'
 * }
 * </pre>
 * The variables the pattern binds are declared by the pattern's own
 * {@link VariableExpression}s, so references that scoping resolved before
 * lowering stay valid. A list pattern arm materialises an array or
 * {@code Iterable} selector as a list into a second synthetic variable, shared
 * by every list pattern arm of the switch, so that the selector is traversed
 * at most once per evaluation of the switch.
 * <p>
 * A record pattern whose type is a record with components known at compile
 * time (a native Groovy or Java record) and the pattern's number of them reads
 * each component through its accessor, {@code __$$rp1.x()}, boxed to
 * {@code Object} where it is the operand of an {@code instanceof} test; a type
 * pattern on a primitive component of its own type binds without a test, as
 * that component cannot be {@code null}. Any other value, such as an emulated
 * record or an object deconstructed through its {@code toList()} method, is
 * deconstructed at run time by {@link RecordPatternSupport#components} into a
 * list that is sized and indexed.
 * <p>
 * {@code source instanceof T name} becomes the {@code instanceof} binding of
 * JEP 394, and {@code source instanceof R(...)} a short-circuit conjunction of
 * ordinary expressions and such bindings, so flow scoping makes every binding
 * visible where the conjunction is true, e.g.
 * {@code p instanceof Point(int x, var y)} becomes, for a record {@code Point}:
 * <pre>
 * p instanceof Point __$$rp1
 *   &amp;&amp; ((Object) __$$rp1.x()) instanceof Integer x
 *   &amp;&amp; RecordPatternSupport.bindable() instanceof Object y
 *   &amp;&amp; RecordPatternSupport.bound(y = __$$rp1.y())
 * </pre>
 * A {@code var}/{@code def} component is unconditional: a {@code null}
 * component keeps the match alive and leaves the binding {@code null}, so its
 * variable is introduced by a test against a non-null token and then assigned.
 * A value deconstructed at run time is bound by
 * {@link RecordPatternSupport#componentsOrEmpty} and indexed by
 * {@link RecordPatternSupport#component}, which tolerate the {@code null}
 * default of a pattern variable whose type test failed.
 *
 * @since 7.0.0
 */
@Incubating
public final class PatternLowering extends ClassCodeVisitorSupport {

    private static final ClassNode RECORD_PATTERN_SUPPORT_TYPE = ClassHelper.makeCached(RecordPatternSupport.class);
    private static final ClassNode LIST_PATTERN_SUPPORT_TYPE = ClassHelper.makeCached(ListPatternSupport.class);
    private static final ClassNode MAP_PATTERN_SUPPORT_TYPE = ClassHelper.makeCached(MapPatternSupport.class);

    private final SourceUnit sourceUnit;
    private boolean lowered;
    private int seq;

    private PatternLowering(final SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit;
    }

    /**
     * Lowers every pattern in the module of {@code sourceUnit}.
     */
    public static void lower(final SourceUnit sourceUnit) {
        ModuleNode module = sourceUnit.getAST();
        if (module == null) return;
        PatternLowering lowering = new PatternLowering(sourceUnit);
        for (ClassNode classNode : module.getClasses()) {
            lowering.lowerClass(classNode);
        }
    }

    /**
     * Lowers every pattern in {@code classNode} that is not lowered yet, and
     * scopes the code this produced. For a transform that creates patterns
     * during or after instruction selection.
     */
    public static void lower(final ClassNode classNode, final SourceUnit sourceUnit) {
        new PatternLowering(sourceUnit).lowerClass(classNode);
    }

    private void lowerClass(final ClassNode classNode) {
        lowered = false;
        visitClass(classNode);
        if (lowered) { // the lowered code declares and references variables
            new VariableScopeVisitor(sourceUnit).visitClass(classNode);
        }
    }

    /**
     * The value matched by a lowered {@code instanceof} pattern: the operand of
     * the head type test that starts the conjunction.
     */
    public static Expression sourceOf(final Expression lowered) {
        Expression expression = lowered;
        while (expression instanceof BinaryExpression binary && binary.getOperation().getType() == Types.LOGICAL_AND) {
            expression = binary.getLeftExpression();
        }
        return ((BinaryExpression) expression).getLeftExpression();
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        lowerArms(statement.getCaseStatements());
        super.visitSwitch(statement);
    }

    @Override
    public void visitSwitchExpression(final SwitchExpression expression) {
        lowerArms(expression.getCaseStatements());
        super.visitSwitchExpression(expression);
    }

    @Override
    public void visitPatternExpression(final PatternExpression expression) {
        if (expression.getLowered() == null && expression.isInstanceOf()) {
            expression.getSource().visit(this);
            lowerInstanceOf(expression);
        }
        super.visitPatternExpression(expression);
    }

    //--------------------------------------------------------------------------
    // switch arms

    private void lowerArms(final List<CaseStatement> caseStatements) {
        Parameter subject = null, elements = null;
        List<PatternExpression> labels = new ArrayList<>();
        boolean listArm = false;
        for (CaseStatement caseStatement : caseStatements) {
            if (caseStatement.getExpression() instanceof PatternExpression label) {
                labels.add(label);
                if (label.getLowered() != null) { // lowered earlier; its arms share the variables
                    subject = label.getSubject();
                    elements = label.getElements();
                } else if (label.getPattern() instanceof ListPattern) {
                    listArm = true;
                }
            }
        }
        if (labels.isEmpty()) return;
        if (subject == null) {
            subject = new Parameter(ClassHelper.dynamicType(), "__$$pv" + seq++);
        }
        if (elements == null && listArm) {
            elements = new Parameter(ClassHelper.LIST_TYPE.getPlainNodeReference(), "__$$pl" + seq++);
        }
        for (PatternExpression label : labels) {
            label.setSubject(subject);
            label.setElements(elements);
        }
        for (CaseStatement caseStatement : caseStatements) {
            if (caseStatement.getExpression() instanceof PatternExpression label && label.getLowered() == null) {
                lowerArm(caseStatement, label);
            }
        }
        lowered = true;
    }

    private void lowerArm(final CaseStatement caseStatement, final PatternExpression label) {
        Token instanceOf = instanceOfToken(label);
        Parameter subject = label.getSubject();
        List<Object> items = new ArrayList<>();
        Expression test;
        MatchPattern pattern = label.getPattern();
        if (pattern instanceof TypePattern typePattern) {
            // the instanceof check tests the wrapper and a primitive binding gets the
            // primitive (JEP 507 semantics for a reference-typed subject)
            appendElementItems(typePattern, varX(subject), instanceOf, items);
            test = binX(varX(subject), instanceOf, atType(new ClassExpression(ClassHelper.getWrapper(typePattern.type())), typePattern.type()));
        } else if (pattern instanceof RecordPattern recordPattern) {
            appendRecordItems(recordPattern, varX(subject), instanceOf, items);
            test = binX(varX(subject), instanceOf, atType(new ClassExpression(recordPattern.type()), recordPattern.type()));
        } else {
            // a list or map pattern has no head type to test in the label, and the label
            // cannot declare the variables the pattern binds, so the label is `true` and
            // the whole match is made by the steps of the arm, which `break <arm>` to the
            // next case test when it fails; the writers evaluate the label as a boolean
            if (pattern instanceof ListPattern listPattern) {
                appendListItems(listPattern, elementsOfSubject(label), instanceOf, items);
            } else {
                appendMapItems((MapPattern) pattern, varX(subject), instanceOf, items);
            }
            test = constX(Boolean.TRUE, true);
        }
        test.setSourcePosition(label);
        label.setLowered(test);

        String armLabel = "__$$arm" + seq++;
        List<Statement> statements = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Expression check) {
                statements.add(checkS(check, armLabel, label));
            } else {
                statements.add((Statement) item);
            }
        }
        if (label.hasGuard()) {
            statements.add(checkS(label.getGuard(), armLabel, label.getGuard()));
        }
        Statement armCode = caseStatement.getCode();
        if (armCode instanceof BlockStatement block && block.getStatementLabels() == null) {
            // one flat block, so that the arm still ends with its last statement
            // (or `expr; break` in a statement switch, which ReturnAdder relies on)
            statements.addAll(block.getStatements());
        } else {
            statements.add(armCode);
        }
        BlockStatement code = new BlockStatement(statements, new VariableScope());
        code.setSourcePosition(armCode);
        caseStatement.setCode(code);
        caseStatement.addStatementLabel(armLabel);
    }

    /**
     * The selector as a list, for a list pattern arm: the switch's elements
     * variable, materialised from the subject by the first list pattern arm
     * that is tried and reused by the others.
     */
    private static Expression elementsOfSubject(final PatternExpression label) {
        Parameter elements = label.getElements();
        Expression materialise = callX(classX(LIST_PATTERN_SUPPORT_TYPE), "elementsOrNull", args(varX(label.getSubject())));
        return ternaryX(notNullX(varX(elements)), varX(elements), assignX(varX(elements), materialise));
    }

    private static Statement checkS(final Expression check, final String armLabel, final ASTNode position) {
        Statement breakArm = new BreakStatement(armLabel);
        breakArm.setSourcePosition(position);
        Statement ifS = ifS(notX(check), breakArm);
        ifS.setSourcePosition(position);
        return ifS;
    }

    /** The matching steps of a record pattern rooted at {@code source}. */
    private void appendRecordItems(final RecordPattern pattern, final Expression source, final Token instanceOf, final List<Object> items) {
        String recordName = "__$$rp" + seq++;
        items.add(binX(source, instanceOf, declX(atType(varX(recordName, pattern.type()), pattern.type()), EmptyExpression.INSTANCE)));
        List<RecordComponentNode> accessible = accessibleComponents(pattern);
        if (accessible != null) {
            for (int i = 0, n = pattern.components().size(); i < n; i += 1) {
                MatchPattern component = pattern.components().get(i);
                if (component instanceof WildcardPattern) continue;
                Expression access = accessorX(recordName, accessible.get(i));
                if (component instanceof TypePattern typed && isTotalFor(typed, accessible.get(i).getType())) {
                    // a primitive component of the pattern's type cannot fail the test, so bind it directly
                    if (typed.variable() != null) items.add(declS(typed.variable(), access));
                } else {
                    appendElementItems(component, tested(component, access), instanceOf, items);
                }
            }
            return;
        }
        String componentsName = "__$$rc" + seq++;
        items.add(declS(localVarX(componentsName, ClassHelper.LIST_TYPE.getPlainNodeReference()),
                callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "components", args(varX(recordName)))));
        items.add(eqX(callX(varX(componentsName), "size"), constX(pattern.components().size(), true)));
        for (int i = 0, n = pattern.components().size(); i < n; i += 1) {
            MatchPattern component = pattern.components().get(i);
            if (component instanceof WildcardPattern) continue;
            appendElementItems(component, callX(varX(componentsName), "get", args(constX(i, true))), instanceOf, items);
        }
    }

    /** The matching steps of a list pattern whose elements are given by {@code elements}, a list or {@code null}. */
    private void appendListItems(final ListPattern pattern, final Expression elements, final Token instanceOf, final List<Object> items) {
        String elementsName = "__$$lv" + seq++;
        items.add(binX(elements, instanceOf, declX(varX(elementsName, ClassHelper.LIST_TYPE.getPlainNodeReference()), EmptyExpression.INSTANCE)));
        List<MatchPattern> elementPatterns = pattern.elements();
        int n = elementPatterns.size();
        int restIndex = pattern.restIndex();
        int fixed = n - (restIndex < 0 ? 0 : 1);
        if (restIndex < 0) {
            items.add(eqX(callX(varX(elementsName), "size"), constX(fixed, true)));
        } else if (fixed > 0) {
            items.add(geX(callX(varX(elementsName), "size"), constX(fixed, true)));
        }
        for (int i = 0; i < n; i += 1) {
            MatchPattern element = elementPatterns.get(i);
            if (element instanceof RestPattern rest) {
                if (rest.type() == null && rest.variable() == null) continue; // a bare rest just relaxes the size check
                VariableExpression restVar = rest.variable() != null ? rest.variable() : localVarX("__$$lr" + seq++, ClassHelper.LIST_TYPE.getPlainNodeReference());
                items.add(declS(restVar,
                        callX(classX(LIST_PATTERN_SUPPORT_TYPE), "rest", args(varX(elementsName), constX(i, true), constX(n - i - 1, true)))));
                if (rest.type() != null) {
                    items.add(callX(classX(LIST_PATTERN_SUPPORT_TYPE), "allInstanceOf",
                            args(varX(restVar), classX(ClassHelper.getWrapper(rest.type())))));
                }
                continue;
            }
            if (element instanceof WildcardPattern) continue;
            Expression index = (restIndex >= 0 && i > restIndex)
                    ? minusX(callX(varX(elementsName), "size"), constX(n - i, true))
                    : constX(i, true);
            appendElementItems(element, callX(varX(elementsName), "get", args(index)), instanceOf, items);
        }
    }

    /** The matching steps of a map pattern rooted at {@code source}. */
    private void appendMapItems(final MapPattern pattern, final Expression source, final Token instanceOf, final List<Object> items) {
        String entriesName = "__$$mv" + seq++;
        items.add(binX(callX(classX(MAP_PATTERN_SUPPORT_TYPE), "entriesOrNull", args(source)), instanceOf,
                declX(varX(entriesName, ClassHelper.MAP_TYPE.getPlainNodeReference()), EmptyExpression.INSTANCE)));
        if (pattern.entries().isEmpty() && pattern.rest() == null) { // `[:]` matches only an empty map
            items.add(callX(varX(entriesName), "isEmpty"));
            return;
        }
        for (MapPattern.Entry entry : pattern.entries()) {
            items.add(callX(varX(entriesName), "containsKey", args(constX(entry.key()))));
            if (entry.value() instanceof WildcardPattern) continue; // presence check only
            appendElementItems(entry.value(), callX(varX(entriesName), "get", args(constX(entry.key()))), instanceOf, items);
        }
        if (pattern.rest() != null && pattern.rest().variable() != null) {
            List<Expression> namedKeys = new ArrayList<>();
            for (MapPattern.Entry entry : pattern.entries()) {
                namedKeys.add(constX(entry.key()));
            }
            items.add(declS(pattern.rest().variable(),
                    callX(classX(MAP_PATTERN_SUPPORT_TYPE), "rest", args(varX(entriesName), listX(namedKeys)))));
        }
    }

    /** The matching steps for one record component, list element or map entry value. */
    private void appendElementItems(final MatchPattern element, final Expression access, final Token instanceOf, final List<Object> items) {
        if (element instanceof ConstantPattern constant) {
            items.add(eqX(access, constant.value()));
        } else if (element instanceof RecordPattern record) {
            appendRecordItems(record, access, instanceOf, items);
        } else if (element instanceof ListPattern list) {
            appendListItems(list, callX(classX(LIST_PATTERN_SUPPORT_TYPE), "elementsOrNull", args(access)), instanceOf, items);
        } else if (element instanceof MapPattern map) {
            appendMapItems(map, access, instanceOf, items);
        } else if (element instanceof TypePattern typed) {
            ClassNode bindType = ClassHelper.getWrapper(typed.type());
            if (typed.variable() == null) {
                items.add(binX(access, instanceOf, atType(new ClassExpression(bindType), typed.type())));
            } else if (ClassHelper.isPrimitiveType(typed.type())) {
                // the instanceof check needs the wrapper, but the pattern variable is bound
                // with its declared primitive type, as Java record patterns do (JEP 440)
                String wrapperName = "__$$pw" + seq++;
                items.add(binX(access, instanceOf, declX(atType(varX(wrapperName, bindType), typed.type()), EmptyExpression.INSTANCE)));
                items.add(declS(typed.variable(), castX(typed.type(), varX(wrapperName))));
            } else {
                items.add(binX(access, instanceOf, declaring(typed.variable(), typed.type())));
            }
        } else if (element instanceof BindingPattern binding) { // unconditional, also matches a null value
            items.add(declS(binding.variable(), access));
        }
    }

    //--------------------------------------------------------------------------
    // instanceof

    private void lowerInstanceOf(final PatternExpression expression) {
        Token instanceOf = instanceOfToken(expression);
        List<Expression> conjuncts = new ArrayList<>();
        if (expression.getPattern() instanceof TypePattern typed) { // `source instanceof T name` (JEP 394)
            Expression test = typed.variable() != null
                    ? declaring(typed.variable(), typed.type())
                    : atType(new ClassExpression(ClassHelper.getWrapper(typed.type())), typed.type());
            conjuncts.add(binX(expression.getSource(), instanceOf, test));
        } else {
            appendRecordConjuncts((RecordPattern) expression.getPattern(), expression.getSource(), instanceOf, conjuncts);
        }
        Expression lowered = conjuncts.get(0);
        for (int i = 1, n = conjuncts.size(); i < n; i += 1) {
            lowered = binX(lowered, Token.newSymbol(Types.LOGICAL_AND, instanceOf.getStartLine(), instanceOf.getStartColumn()), conjuncts.get(i));
        }
        lowered.setSourcePosition(expression);
        expression.setLowered(lowered);
        this.lowered = true;
    }

    private void appendRecordConjuncts(final RecordPattern pattern, final Expression source, final Token instanceOf, final List<Expression> conjuncts) {
        String recordName = "__$$rp" + seq++;
        conjuncts.add(binX(source, instanceOf, declX(atType(varX(recordName, pattern.type()), pattern.type()), EmptyExpression.INSTANCE)));
        List<RecordComponentNode> accessible = accessibleComponents(pattern);
        if (accessible != null) {
            // the accessor calls follow the type test in a short-circuit conjunction
            for (int i = 0, n = pattern.components().size(); i < n; i += 1) {
                MatchPattern component = pattern.components().get(i);
                if (component instanceof WildcardPattern) continue;
                appendComponentConjuncts(component, tested(component, accessorX(recordName, accessible.get(i))), instanceOf, conjuncts);
            }
            return;
        }
        // componentsOrEmpty(...) always yields a List; this conjunct just binds it
        String componentsName = "__$$rc" + seq++;
        conjuncts.add(binX(callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "componentsOrEmpty", args(varX(recordName))), instanceOf,
                declX(varX(componentsName, ClassHelper.LIST_TYPE.getPlainNodeReference()), EmptyExpression.INSTANCE)));
        conjuncts.add(eqX(callX(varX(componentsName), "size"), constX(pattern.components().size(), true)));
        for (int i = 0, n = pattern.components().size(); i < n; i += 1) {
            MatchPattern component = pattern.components().get(i);
            if (component instanceof WildcardPattern) continue;
            Expression access = callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "component", args(varX(componentsName), constX(i, true)));
            appendComponentConjuncts(component, access, instanceOf, conjuncts);
        }
    }

    /** The conjuncts for one record component of an {@code instanceof} record pattern, whose value is {@code access}. */
    private void appendComponentConjuncts(final MatchPattern component, final Expression access, final Token instanceOf, final List<Expression> conjuncts) {
        if (component instanceof RecordPattern nested) {
            appendRecordConjuncts(nested, access, instanceOf, conjuncts);
        } else if (component instanceof TypePattern typed) {
            // the variable of a primitive type pattern is declared with the wrapper type
            // here, as instanceof binds the tested object (see PatternExpression.instanceOf)
            Expression test = typed.variable() != null
                    ? declaring(typed.variable(), typed.type())
                    : atType(new ClassExpression(ClassHelper.getWrapper(typed.type())), typed.type());
            conjuncts.add(binX(access, instanceOf, test));
        } else if (component instanceof BindingPattern binding) {
            // instanceof never matches null, so the pattern variable is introduced by a
            // test against a non-null token and then assigned the component value
            conjuncts.add(binX(callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "bindable"), instanceOf,
                    declX(binding.variable(), EmptyExpression.INSTANCE)));
            conjuncts.add(callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "bound", args(assignX(varX(binding.variable()), access))));
        }
    }

    //--------------------------------------------------------------------------
    // record components

    /**
     * The record components a pattern reads through their accessors: those of
     * its type when the type is a record whose components are known at compile
     * time (a native Groovy or Java record; an emulated record declares none)
     * with as many components as the pattern. Otherwise {@code null}: the value
     * is deconstructed at run time through {@link RecordPatternSupport}, which
     * also leaves a pattern of the wrong arity, an error under static type
     * checking, as a pattern that never matches in dynamic code.
     */
    private static List<RecordComponentNode> accessibleComponents(final RecordPattern pattern) {
        if (!pattern.isRecord()) return null;
        List<RecordComponentNode> declared = pattern.type().getRecordComponents();
        return declared.size() == pattern.components().size() ? declared : null;
    }

    /** The accessor call that reads {@code component} of the record held by the variable {@code recordName}. */
    private static Expression accessorX(final String recordName, final RecordComponentNode component) {
        MethodCallExpression accessor = callX(varX(recordName), component.getName());
        accessor.setImplicitThis(false);
        return accessor;
    }

    /**
     * The component value as the operand of the {@code instanceof} test a type,
     * record, list or map component pattern makes: cast to {@code Object}, so
     * that a primitive component is boxed for the test and a component pattern
     * that cannot match the declared component type (not a diagnostic of the
     * type checker, which reports an impossible pattern at the case label) is
     * a test that fails rather than an incompatible {@code instanceof}. A binding
     * or constant component pattern reads the value as it is.
     */
    private static Expression tested(final MatchPattern component, final Expression access) {
        return component instanceof BindingPattern || component instanceof ConstantPattern
                ? access : castX(ClassHelper.OBJECT_TYPE, access);
    }

    /**
     * Whether a type pattern matches every value of a record component of the
     * given type: when the component is a primitive of the pattern's type,
     * which cannot be {@code null}, as {@link RecordPattern#isTotal} counts it.
     */
    private static boolean isTotalFor(final TypePattern typed, final ClassNode componentType) {
        return ClassHelper.isPrimitiveType(componentType)
                && ClassHelper.getWrapper(componentType).equals(ClassHelper.getWrapper(typed.type()));
    }

    //--------------------------------------------------------------------------

    private static Token instanceOfToken(final ASTNode position) {
        return Token.newSymbol(Types.KEYWORD_INSTANCEOF, position.getLineNumber(), position.getColumnNumber());
    }

    /**
     * The {@code instanceof} operand that declares a pattern variable
     * (JEP 394), spanning from the type to the name when both are positioned.
     */
    private static DeclarationExpression declaring(final VariableExpression variable, final ClassNode type) {
        DeclarationExpression declaration = declX(variable, EmptyExpression.INSTANCE);
        if (type.getLineNumber() > 0 && variable.getLineNumber() > 0) {
            declaration.setLineNumber(type.getLineNumber());
            declaration.setColumnNumber(type.getColumnNumber());
            declaration.setLastLineNumber(variable.getLastLineNumber());
            declaration.setLastColumnNumber(variable.getLastColumnNumber());
        } else {
            declaration.setSourcePosition(variable);
        }
        return declaration;
    }

    /** Gives a node synthesized for a pattern the source position of the pattern's type, so that diagnostics such as an unresolved class point at it. */
    private static <T extends ASTNode> T atType(final T node, final ClassNode type) {
        if (type.getLineNumber() > 0) node.setSourcePosition(type);
        return node;
    }
}
