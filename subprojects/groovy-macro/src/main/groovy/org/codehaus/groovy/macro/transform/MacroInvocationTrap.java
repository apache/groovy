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
package org.codehaus.groovy.macro.transform;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodInvocationTrap;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.macro.runtime.MacroBuilder;
import org.codehaus.groovy.macro.runtime.MacroSubstitutionKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION;

/**
 * @author Sergei Egorov <bsideup@gmail.com>
 */
public class MacroInvocationTrap extends MethodInvocationTrap {

    private final static ClassNode MACROCLASS_TYPE = ClassHelper.make(MacroClass.class);
    private final ReaderSource readerSource;
    private final SourceUnit sourceUnit;

    public MacroInvocationTrap(ReaderSource source, SourceUnit sourceUnit) {
        super(source, sourceUnit);
        this.readerSource = source;
        this.sourceUnit = sourceUnit;
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        ClassNode type = call.getType();
        if (type instanceof InnerClassNode) {
            if (((InnerClassNode) type).isAnonymous() &&
                    MACROCLASS_TYPE.getNameWithoutPackage().equals(type.getSuperClass().getNameWithoutPackage())) {
                //System.out.println("call = " + call.getText());
                try {
                    String source = convertInnerClassToSource(type);
                    List<Expression> macroArgumentsExpressions = new LinkedList<Expression>();
                    macroArgumentsExpressions.add(new ConstantExpression(source));
                    macroArgumentsExpressions.add(buildSubstitutionMap(type));
                    macroArgumentsExpressions.add(new ClassExpression(ClassHelper.make(ClassNode.class)));

                    MethodCallExpression macroCall = new MethodCallExpression(
                            new PropertyExpression(new ClassExpression(ClassHelper.makeWithoutCaching(MacroBuilder.class, false)), "INSTANCE"),
                            MacroTransformation.MACRO_METHOD,
                            new ArgumentListExpression(macroArgumentsExpressions)
                    );

                    macroCall.setSpreadSafe(false);
                    macroCall.setSafe(false);
                    macroCall.setImplicitThis(false);
                    call.putNodeMetaData(MacroTransformation.class, macroCall);
                    List<ClassNode> classes = sourceUnit.getAST().getClasses();
                    for (Iterator<ClassNode> iterator = classes.iterator(); iterator.hasNext(); ) {
                        final ClassNode aClass = iterator.next();
                        if (aClass==type || type==aClass.getOuterClass()) {
                            iterator.remove();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        super.visitConstructorCallExpression(call);

    }

    private String convertInnerClassToSource(final ClassNode type) throws Exception {
        String source = GeneralUtils.convertASTToSource(readerSource, type);
        // we need to remove the leading "{" and trailing "}"
        source = source.substring(source.indexOf('{')+1, source.lastIndexOf('}')-1);
        return source;
    }

    @Override
    protected boolean handleTargetMethodCallExpression(MethodCallExpression macroCall) {
        final ClosureExpression closureExpression = getClosureArgument(macroCall);

        if (closureExpression == null) {
            return true;
        }

        if (closureExpression.getParameters() != null && closureExpression.getParameters().length > 0) {
            addError("Macro closure arguments are not allowed", closureExpression);
            return true;
        }

        final MapExpression mapExpression = buildSubstitutionMap(closureExpression);

        String source = convertClosureToSource(closureExpression);

        BlockStatement closureBlock = (BlockStatement) closureExpression.getCode();

        Boolean asIs = false;

        TupleExpression macroArguments = getMacroArguments(macroCall);

        if (macroArguments == null) {
            return true;
        }

        List<Expression> macroArgumentsExpressions = macroArguments.getExpressions();

        if (macroArgumentsExpressions.size() == 2 || macroArgumentsExpressions.size() == 3) {
            Expression asIsArgumentExpression = macroArgumentsExpressions.get(macroArgumentsExpressions.size() - 2);
            if ((asIsArgumentExpression instanceof ConstantExpression)) {
                ConstantExpression asIsConstantExpression = (ConstantExpression) asIsArgumentExpression;

                if (!(asIsConstantExpression.getValue() instanceof Boolean)) {
                    addError("AsIs argument value should be boolean", asIsConstantExpression);
                    return true;
                }

                asIs = (Boolean) asIsConstantExpression.getValue();
            }
        }

        macroArgumentsExpressions.remove(macroArgumentsExpressions.size() - 1);

        macroArgumentsExpressions.add(new ConstantExpression(source));
        macroArgumentsExpressions.add(mapExpression);
        macroArgumentsExpressions.add(new ClassExpression(ClassHelper.makeWithoutCaching(MacroBuilder.getMacroValue(closureBlock, asIs).getClass(), false)));

        macroCall.setObjectExpression(new PropertyExpression(new ClassExpression(ClassHelper.makeWithoutCaching(MacroBuilder.class, false)), "INSTANCE"));
        macroCall.setSpreadSafe(false);
        macroCall.setSafe(false);
        macroCall.setImplicitThis(false);

        return true;
    }

    private MapExpression buildSubstitutionMap(final ASTNode expr) {
        final Map<MacroSubstitutionKey, ClosureExpression> map = new HashMap<MacroSubstitutionKey, ClosureExpression>();
        final MapExpression mapExpression = new MapExpression();

        ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            @Override
            public void visitClass(final ClassNode node) {
                super.visitClass(node);
                Iterator<InnerClassNode> it = node.getInnerClasses();
                while (it.hasNext()) {
                    InnerClassNode next = it.next();
                    visitClass(next);
                }
            }

            @Override
            public void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call);

                if (isBuildInvocation(call, MacroTransformation.DOLLAR_VALUE)) {
                    ClosureExpression substitutionClosureExpression = getClosureArgument(call);

                    if (substitutionClosureExpression == null) {
                        return;
                    }

                    Statement code = substitutionClosureExpression.getCode();
                    if (code instanceof BlockStatement) {
                        ((BlockStatement) code).setVariableScope(null);
                    }

                    MacroSubstitutionKey key = new MacroSubstitutionKey(call, expr.getLineNumber(), expr.getColumnNumber());

                    map.put(key, substitutionClosureExpression);
                }
            }
        };
        if (expr instanceof ClassNode) {
            visitor.visitClass((ClassNode) expr);
        } else {
            expr.visit(visitor);
        }
        for (Map.Entry<MacroSubstitutionKey, ClosureExpression> entry : map.entrySet()) {
            mapExpression.addMapEntryExpression(entry.getKey().toConstructorCallExpression(), entry.getValue());
        }
        return mapExpression;
    }

    @Override
    protected boolean isBuildInvocation(MethodCallExpression call) {
        return isBuildInvocation(call, MacroTransformation.MACRO_METHOD);
    }

    public static boolean isBuildInvocation(MethodCallExpression call, String methodName) {
        if (call == null) throw new IllegalArgumentException("Null: call");
        if (methodName == null) throw new IllegalArgumentException("Null: methodName");

        if (!(call.getMethod() instanceof ConstantExpression)) {
            return false;
        }

        if (!(methodName.equals(call.getMethodAsString()))) {
            return false;
        }

        // is method object correct type?
        return call.getObjectExpression() == THIS_EXPRESSION;
    }

    protected TupleExpression getMacroArguments(MethodCallExpression call) {
        Expression macroCallArguments = call.getArguments();
        if (macroCallArguments == null) {
            addError("Call should have arguments", call);
            return null;
        }

        if (!(macroCallArguments instanceof TupleExpression)) {
            addError("Call should have TupleExpression as arguments", macroCallArguments);
            return null;
        }

        TupleExpression tupleArguments = (TupleExpression) macroCallArguments;

        if (tupleArguments.getExpressions() == null) {
            addError("Call arguments should have expressions", tupleArguments);
            return null;
        }

        return tupleArguments;
    }

    protected ClosureExpression getClosureArgument(MethodCallExpression call) {
        TupleExpression tupleArguments = getMacroArguments(call);

        if (tupleArguments.getExpressions().size() < 1) {
            addError("Call arguments should have at least one argument", tupleArguments);
            return null;
        }

        Expression result = tupleArguments.getExpression(tupleArguments.getExpressions().size() - 1);
        if (!(result instanceof ClosureExpression)) {
            addError("Last call argument should be a closure", result);
            return null;
        }

        return (ClosureExpression) result;
    }
}
