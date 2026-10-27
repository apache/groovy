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
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PatternExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
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
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Derives the matching code of every {@link PatternExpression} in a module
 * (GEP-19). It runs once, at the start of semantic analysis, so that
 * resolution, scoping, type checking and code generation all see the same
 * ordinary code, while the pattern itself stays on the
 * {@code PatternExpression} for the phases that need it.
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
 * {@code source instanceof R(...)} becomes a short-circuit conjunction of
 * ordinary expressions and {@code instanceof} bindings (JEP 394), so flow
 * scoping makes every binding visible where the conjunction is true, e.g.
 * {@code p instanceof Point(int x, var y)} becomes:
 * <pre>
 * p instanceof Point __$$rp1
 *   &amp;&amp; RecordPatternSupport.componentsOrEmpty(__$$rp1) instanceof List __$$rc2
 *   &amp;&amp; __$$rc2.size() == 2
 *   &amp;&amp; RecordPatternSupport.component(__$$rc2, 0) instanceof Integer x
 *   &amp;&amp; RecordPatternSupport.bindable() instanceof Object y
 *   &amp;&amp; RecordPatternSupport.bound(y = RecordPatternSupport.component(__$$rc2, 1))
 * </pre>
 * A {@code var}/{@code def} component is unconditional: a {@code null}
 * component keeps the match alive and leaves the binding {@code null}, so its
 * variable is introduced by a test against a non-null token and then assigned.
 * <p>
 * Lowering is idempotent. An AST transform that creates a pattern after this
 * pass has run calls {@link #lower(PatternExpression, SourceUnit)} or lowers
 * the enclosing switch with {@link #lower(List, SourceUnit)}.
 *
 * @since 7.0.0
 */
@Incubating
public final class PatternLowering extends ClassCodeVisitorSupport {

    private static final ClassNode RECORD_PATTERN_SUPPORT_TYPE = ClassHelper.makeCached(RecordPatternSupport.class);
    private static final ClassNode LIST_PATTERN_SUPPORT_TYPE = ClassHelper.makeCached(ListPatternSupport.class);
    private static final ClassNode MAP_PATTERN_SUPPORT_TYPE = ClassHelper.makeCached(MapPatternSupport.class);

    private final SourceUnit sourceUnit;
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
            lowering.visitClass(classNode);
        }
    }

    /**
     * Lowers the pattern arms among {@code caseStatements}, which belong to one switch.
     */
    public static void lower(final List<CaseStatement> caseStatements, final SourceUnit sourceUnit) {
        new PatternLowering(sourceUnit).lowerArms(caseStatements);
    }

    /**
     * Lowers {@code source instanceof pattern}.
     */
    public static void lower(final PatternExpression expression, final SourceUnit sourceUnit) {
        expression.visit(new PatternLowering(sourceUnit));
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
        Parameter subject = null;
        for (CaseStatement caseStatement : caseStatements) {
            if (caseStatement.getExpression() instanceof PatternExpression label && label.getSubject() != null) {
                subject = label.getSubject(); // already lowered
                break;
            }
        }
        for (CaseStatement caseStatement : caseStatements) {
            if (caseStatement.getExpression() instanceof PatternExpression label && label.getLowered() == null) {
                if (subject == null) {
                    subject = new Parameter(ClassHelper.dynamicType(), "__$$pv" + seq++);
                }
                lowerArm(caseStatement, label, subject);
            }
        }
    }

    private void lowerArm(final CaseStatement caseStatement, final PatternExpression label, final Parameter subject) {
        Token instanceOf = instanceOfToken(label);
        Expression subjectVar = varX(subject.getName());
        List<Object> items = new ArrayList<>();
        Expression test;
        MatchPattern pattern = label.getPattern();
        if (pattern instanceof TypePattern typePattern) {
            // the instanceof check tests the wrapper and a primitive binding gets the
            // primitive (JEP 507 semantics for a reference-typed subject)
            appendElementItems(typePattern, subjectVar, instanceOf, items);
            test = binX(varX(subject.getName()), instanceOf, atType(new ClassExpression(ClassHelper.getWrapper(typePattern.type())), typePattern.type()));
        } else if (pattern instanceof RecordPattern recordPattern) {
            appendRecordItems(recordPattern, subjectVar, instanceOf, items);
            test = binX(varX(subject.getName()), instanceOf, atType(new ClassExpression(recordPattern.type()), recordPattern.type()));
        } else if (pattern instanceof ListPattern listPattern) {
            appendListItems(listPattern, subjectVar, instanceOf, items);
            test = constX(Boolean.TRUE, true);
        } else {
            appendMapItems((MapPattern) pattern, subjectVar, instanceOf, items);
            test = constX(Boolean.TRUE, true);
        }
        test.setSourcePosition(label);
        label.setSubject(subject);
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

    /** The matching steps of a list pattern rooted at {@code source}. */
    private void appendListItems(final ListPattern pattern, final Expression source, final Token instanceOf, final List<Object> items) {
        String elementsName = "__$$lv" + seq++;
        items.add(binX(callX(classX(LIST_PATTERN_SUPPORT_TYPE), "elementsOrNull", args(source)), instanceOf,
                declX(varX(elementsName, ClassHelper.LIST_TYPE.getPlainNodeReference()), EmptyExpression.INSTANCE)));
        List<MatchPattern> elements = pattern.elements();
        int n = elements.size();
        int restIndex = pattern.restIndex();
        int fixed = n - (restIndex < 0 ? 0 : 1);
        if (restIndex < 0) {
            items.add(eqX(callX(varX(elementsName), "size"), constX(fixed, true)));
        } else if (fixed > 0) {
            items.add(geX(callX(varX(elementsName), "size"), constX(fixed, true)));
        }
        for (int i = 0; i < n; i += 1) {
            MatchPattern element = elements.get(i);
            if (element instanceof RestPattern rest) {
                if (rest.type() == null && rest.name() == null) continue; // a bare rest just relaxes the size check
                String restName = rest.name() != null ? rest.name() : "__$$lr" + seq++;
                items.add(declS(localVarX(restName, ClassHelper.LIST_TYPE.getPlainNodeReference()),
                        callX(classX(LIST_PATTERN_SUPPORT_TYPE), "rest", args(varX(elementsName), constX(i, true), constX(n - i - 1, true)))));
                if (rest.type() != null) {
                    items.add(callX(classX(LIST_PATTERN_SUPPORT_TYPE), "allInstanceOf",
                            args(varX(restName), classX(ClassHelper.getWrapper(rest.type())))));
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
        if (pattern.rest() != null && pattern.rest().name() != null) {
            List<Expression> namedKeys = new ArrayList<>();
            for (MapPattern.Entry entry : pattern.entries()) {
                namedKeys.add(constX(entry.key()));
            }
            items.add(declS(localVarX(pattern.rest().name(), ClassHelper.MAP_TYPE.getPlainNodeReference()),
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
            appendListItems(list, access, instanceOf, items);
        } else if (element instanceof MapPattern map) {
            appendMapItems(map, access, instanceOf, items);
        } else if (element instanceof TypePattern typed) {
            ClassNode bindType = ClassHelper.getWrapper(typed.type());
            if (typed.name() == null) {
                items.add(binX(access, instanceOf, atType(new ClassExpression(bindType), typed.type())));
            } else if (ClassHelper.isPrimitiveType(typed.type())) {
                // the instanceof check needs the wrapper, but the pattern variable is bound
                // with its declared primitive type, as Java record patterns do (JEP 440)
                String wrapperName = "__$$pw" + seq++;
                items.add(binX(access, instanceOf, declX(atType(varX(wrapperName, bindType), typed.type()), EmptyExpression.INSTANCE)));
                items.add(declS(atType(localVarX(typed.name(), typed.type()), typed.type()), castX(typed.type(), varX(wrapperName))));
            } else {
                items.add(binX(access, instanceOf, declX(atType(varX(typed.name(), bindType), typed.type()), EmptyExpression.INSTANCE)));
            }
        } else if (element instanceof BindingPattern binding) { // unconditional, also matches a null value
            items.add(declS(localVarX(binding.name(), ClassHelper.dynamicType()), access));
        }
    }

    //--------------------------------------------------------------------------
    // instanceof

    private void lowerInstanceOf(final PatternExpression expression) {
        Token instanceOf = instanceOfToken(expression);
        List<Expression> conjuncts = new ArrayList<>();
        appendRecordConjuncts((RecordPattern) expression.getPattern(), expression.getSource(), instanceOf, conjuncts);
        Expression lowered = conjuncts.get(0);
        for (int i = 1, n = conjuncts.size(); i < n; i += 1) {
            lowered = binX(lowered, Token.newSymbol(Types.LOGICAL_AND, instanceOf.getStartLine(), instanceOf.getStartColumn()), conjuncts.get(i));
        }
        lowered.setSourcePosition(expression);
        expression.setLowered(lowered);
    }

    private void appendRecordConjuncts(final RecordPattern pattern, final Expression source, final Token instanceOf, final List<Expression> conjuncts) {
        String recordName = "__$$rp" + seq++;
        conjuncts.add(binX(source, instanceOf, declX(atType(varX(recordName, pattern.type()), pattern.type()), EmptyExpression.INSTANCE)));
        // componentsOrEmpty(...) always yields a List; this conjunct just binds it
        String componentsName = "__$$rc" + seq++;
        conjuncts.add(binX(callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "componentsOrEmpty", args(varX(recordName))), instanceOf,
                declX(varX(componentsName, ClassHelper.LIST_TYPE.getPlainNodeReference()), EmptyExpression.INSTANCE)));
        conjuncts.add(eqX(callX(varX(componentsName), "size"), constX(pattern.components().size(), true)));
        for (int i = 0, n = pattern.components().size(); i < n; i += 1) {
            MatchPattern component = pattern.components().get(i);
            if (component instanceof WildcardPattern) continue;
            Expression access = callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "component", args(varX(componentsName), constX(i, true)));
            if (component instanceof RecordPattern nested) {
                appendRecordConjuncts(nested, access, instanceOf, conjuncts);
            } else if (component instanceof TypePattern typed) {
                ClassNode bindType = ClassHelper.getWrapper(typed.type());
                Expression rhs = typed.name() != null
                        ? declX(atType(varX(typed.name(), bindType), typed.type()), EmptyExpression.INSTANCE)
                        : atType(new ClassExpression(bindType), typed.type());
                conjuncts.add(binX(access, instanceOf, rhs));
            } else if (component instanceof BindingPattern binding) {
                // instanceof never matches null, so the pattern variable is introduced by a
                // test against a non-null token and then assigned the component value
                conjuncts.add(binX(callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "bindable"), instanceOf,
                        declX(varX(binding.name(), ClassHelper.OBJECT_TYPE.getPlainNodeReference()), EmptyExpression.INSTANCE)));
                conjuncts.add(callX(classX(RECORD_PATTERN_SUPPORT_TYPE), "bound", args(assignX(varX(binding.name()), access))));
            }
        }
    }

    //--------------------------------------------------------------------------

    private static Token instanceOfToken(final ASTNode position) {
        return Token.newSymbol(Types.KEYWORD_INSTANCEOF, position.getLineNumber(), position.getColumnNumber());
    }

    /** Gives a node synthesized for a pattern the source position of the pattern's type, so that diagnostics such as an unresolved class point at it. */
    private static <T extends ASTNode> T atType(final T node, final ClassNode type) {
        if (type.getLineNumber() > 0) node.setSourcePosition(type);
        return node;
    }
}
