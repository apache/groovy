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
package groovy.text.markup;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This AST transformer is responsible for modifying a source template into something which can be compiled as a
 * {@link groovy.text.markup.BaseTemplate} subclass.</p>
 * <p/>
 * <p>It performs the following operations:</p>
 * <p/>
 * <ul> <li>replace dynamic variables with <i>getModel().get(dynamicVariable)</i> calls</li> <li>optionally wrap
 * <i>getModel().get(...)</i> calls into <i>tryEscape</i> calls for automatic escaping</li> <li>replace <i>include
 * XXX:'...'</i> calls with the appropriate <i>includeXXXX</i> method calls</li> <li>replace <i>':tagName'()</i> calls
 * into <i>methodMissing('tagName', ...)</i> calls</li> </ul>
 */
class MarkupBuilderCodeTransformer extends ClassCodeExpressionTransformer {

    static final String TARGET_VARIABLE = "target.variable";

    private final SourceUnit unit;
    private final boolean autoEscape;
    private final ClassNode classNode;

    public MarkupBuilderCodeTransformer(final SourceUnit unit, final ClassNode classNode, final boolean autoEscape) {
        this.unit = unit;
        this.autoEscape = autoEscape;
        this.classNode = classNode;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    @Override
    public Expression transform(final Expression exp) {
        if (exp instanceof BinaryExpression) {
            return transformBinaryExpression((BinaryExpression) exp);
        }
        if (exp instanceof MethodCallExpression) {
            return transformMethodCall((MethodCallExpression) exp);
        }
        if (exp instanceof ClosureExpression) {
            ClosureExpression cl = (ClosureExpression) exp;
            cl.getCode().visit(this);
            return cl;
        }
        if (exp instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) exp;
            if (var.getAccessedVariable() instanceof DynamicVariable) {
                MethodCallExpression callGetModel = new MethodCallExpression(
                        new VariableExpression("this"),
                        "getModel",
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                callGetModel.setImplicitThis(true);
                callGetModel.setSourcePosition(exp);
                String varName = var.getName();
                if ("model".equals(varName) || "unescaped".equals(varName)) {
                    return callGetModel;
                }
                MethodCallExpression mce = new MethodCallExpression(
                        callGetModel,
                        "get",
                        new ArgumentListExpression(new ConstantExpression(varName))
                );
                mce.setSourcePosition(exp);
                mce.setImplicitThis(false);
                MethodCallExpression yield = new MethodCallExpression(
                        new VariableExpression("this"),
                        "tryEscape",
                        new ArgumentListExpression(mce)
                );
                yield.setImplicitThis(true);
                yield.setSourcePosition(exp);
                yield.putNodeMetaData(TARGET_VARIABLE, varName);
                return autoEscape?yield:mce;
            }
        }
        return super.transform(exp);
    }

    private Expression transformBinaryExpression(final BinaryExpression bin) {
        Expression left = bin.getLeftExpression();
        Expression right = bin.getRightExpression();
        boolean assignment = bin.getOperation().getType() == Types.ASSIGN;
        if (assignment && left instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) left;
            if (var.getAccessedVariable() instanceof DynamicVariable) {
                String varName = var.getName();
                if (!"modelTypes".equals(varName)) {
                    MethodCallExpression callGetModel = new MethodCallExpression(
                            new VariableExpression("this"),
                            "getModel",
                            ArgumentListExpression.EMPTY_ARGUMENTS
                    );
                    callGetModel.setImplicitThis(true);
                    callGetModel.setSourcePosition(left);
                    MethodCallExpression mce = new MethodCallExpression(
                            callGetModel,
                            "put",
                            new ArgumentListExpression(new ConstantExpression(varName), right)
                    );
                    mce.setSourcePosition(left);
                    mce.setImplicitThis(false);
                    return transform(mce);
                }
            }
        }
        if (assignment && left instanceof VariableExpression && right instanceof ClosureExpression) {
            VariableExpression var = (VariableExpression) left;
            if ("modelTypes".equals(var.getName())) {
                // template declaring its expected types from model directly
                // modelTypes = {
                //  List<String> items
                //  ...
                // }
                Map<String,ClassNode> modelTypes = extractModelTypesFromClosureExpression((ClosureExpression)right);
                Expression result = EmptyExpression.INSTANCE;
                classNode.putNodeMetaData(MarkupTemplateEngine.MODELTYPES_ASTKEY, modelTypes);
                return result;
            }
        }
        return super.transform(bin);
    }

    private Map<String, ClassNode> extractModelTypesFromClosureExpression(final ClosureExpression expression) {
        Map<String, ClassNode> model = new HashMap<String, ClassNode>();
        extractModelTypesFromStatement(expression.getCode(), model);
        return model;
    }

    private void extractModelTypesFromStatement(final Statement code, final Map<String, ClassNode> model) {
        if (code instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) code;
            for (Statement statement : block.getStatements()) {
                extractModelTypesFromStatement(statement, model);
            }
        } else if (code instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) code).getExpression();
            if (expression instanceof DeclarationExpression) {
                VariableExpression var = ((DeclarationExpression) expression).getVariableExpression();
                model.put(var.getName(), var.getOriginType());
            }
        }
    }

    private Expression transformMethodCall(final MethodCallExpression exp) {
        String name = exp.getMethodAsString();
        if (exp.isImplicitThis() && "include".equals(name)) {
            return tryTransformInclude(exp);
        } else if (exp.isImplicitThis() && name.startsWith(":")) {
            List<Expression> args;
            if (exp.getArguments() instanceof ArgumentListExpression) {
                args = ((ArgumentListExpression) exp.getArguments()).getExpressions();
            } else {
                args = Collections.singletonList(exp.getArguments());
            }
            Expression newArguments = transform(new ArgumentListExpression(new ConstantExpression(name.substring(1)), new ArrayExpression(ClassHelper.OBJECT_TYPE, args)));
            MethodCallExpression call = new MethodCallExpression(
                    new VariableExpression("this"),
                    "methodMissing",
                    newArguments
            );
            call.setImplicitThis(true);
            call.setSafe(exp.isSafe());
            call.setSpreadSafe(exp.isSpreadSafe());
            call.setSourcePosition(exp);
            return call;
        } else if (name!=null && name.startsWith("$")) {
            MethodCallExpression reformatted = new MethodCallExpression(
                    exp.getObjectExpression(),
                    name.substring(1),
                    exp.getArguments()
            );
            reformatted.setImplicitThis(exp.isImplicitThis());
            reformatted.setSafe(exp.isSafe());
            reformatted.setSpreadSafe(exp.isSpreadSafe());
            reformatted.setSourcePosition(exp);
            // wrap in a stringOf { ... } closure call
            ClosureExpression clos = new ClosureExpression(Parameter.EMPTY_ARRAY, new ExpressionStatement(reformatted));
            clos.setVariableScope(new VariableScope());
            MethodCallExpression stringOf = new MethodCallExpression(new VariableExpression("this"),
                    "stringOf",
                    clos);
            stringOf.setImplicitThis(true);
            stringOf.setSourcePosition(reformatted);
            return stringOf;
        }
        return super.transform(exp);
    }

    private Expression tryTransformInclude(final MethodCallExpression exp) {
        Expression arguments = exp.getArguments();
        if (arguments instanceof TupleExpression) {
            List<Expression> expressions = ((TupleExpression) arguments).getExpressions();
            if (expressions.size() == 1 && expressions.get(0) instanceof MapExpression) {
                MapExpression map = (MapExpression) expressions.get(0);
                List<MapEntryExpression> entries = map.getMapEntryExpressions();
                if (entries.size() == 1) {
                    MapEntryExpression mapEntry = entries.get(0);
                    Expression keyExpression = mapEntry.getKeyExpression();
                    try {
                        IncludeType includeType = IncludeType.valueOf(keyExpression.getText().toLowerCase());
                        MethodCallExpression call = new MethodCallExpression(
                                exp.getObjectExpression(),
                                includeType.getMethodName(),
                                new ArgumentListExpression(
                                        mapEntry.getValueExpression()
                                )
                        );
                        call.setImplicitThis(true);
                        call.setSafe(exp.isSafe());
                        call.setSpreadSafe(exp.isSpreadSafe());
                        call.setSourcePosition(exp);
                        return call;
                    } catch (IllegalArgumentException e) {
                        // not a valid import type, do not modify the code
                    }
                }

            }
        }
        return super.transform(exp);
    }
}
