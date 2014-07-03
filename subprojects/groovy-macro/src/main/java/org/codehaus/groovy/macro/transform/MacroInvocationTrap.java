/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodInvocationTrap;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.macro.runtime.MacroBuilder;
import org.codehaus.groovy.macro.runtime.MacroSubstitutionKey;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.expr.VariableExpression.THIS_EXPRESSION;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
public class MacroInvocationTrap extends MethodInvocationTrap {

    public MacroInvocationTrap(ReaderSource source, SourceUnit sourceUnit) {
        super(source, sourceUnit);
    }

    @Override
    protected boolean handleTargetMethodCallExpression(MethodCallExpression macroCall) {
        final ClosureExpression closureExpression = getClosureArgument(macroCall);

        if(closureExpression == null) {
            return true;
        }

        if(closureExpression.getParameters() != null && closureExpression.getParameters().length > 0) {
            addError("Macro closure arguments are not allowed", closureExpression);
            return true;
        }

        final MapExpression mapExpression = new MapExpression();

        (new CodeVisitorSupport() {
            @Override
            public void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call);

                if(isBuildInvocation(call, MacroTransformation.DOLLAR_VALUE)) {
                    ClosureExpression substitutionClosureExpression = getClosureArgument(call);

                    if(substitutionClosureExpression == null) {
                        return;
                    }

                    MacroSubstitutionKey key = new MacroSubstitutionKey(call, closureExpression.getLineNumber(), closureExpression.getColumnNumber());

                    mapExpression.addMapEntryExpression(key.toConstructorCallExpression(), substitutionClosureExpression);
                }
            }
        }).visitClosureExpression(closureExpression);

        String source = convertClosureToSource(closureExpression);

        BlockStatement closureBlock = (BlockStatement) closureExpression.getCode();

        Boolean asIs = false;

        TupleExpression macroArguments = getMacroArguments(macroCall);

        if(macroArguments == null) {
            return true;
        }

        List<Expression> macroArgumentsExpressions = macroArguments.getExpressions();

        if(macroArgumentsExpressions.size() > 1) {
            Expression firstArgument = macroArgumentsExpressions.get(0);

            if(!(firstArgument instanceof ConstantExpression)) {
                addError("AsIs argument value should be constant(true or false)", firstArgument);
                return true;
            }

            ConstantExpression asIsConstantExpression = (ConstantExpression) firstArgument;

            if(!(asIsConstantExpression.getValue() instanceof Boolean)) {
                addError("AsIs argument value should be boolean", asIsConstantExpression);
                return true;
            }

            asIs = (Boolean) asIsConstantExpression.getValue();
        }

        List<Expression> otherArgs = new ArrayList<Expression>();
        otherArgs.add(new ConstantExpression(asIs));
        otherArgs.add(new ConstantExpression(source));
        otherArgs.add(mapExpression);
        otherArgs.add(new ClassExpression(ClassHelper.makeWithoutCaching(MacroBuilder.getMacroValue(closureBlock, asIs).getClass(), false)));

        macroCall.setArguments(new ArgumentListExpression(otherArgs));
        macroCall.setObjectExpression(new PropertyExpression(new ClassExpression(ClassHelper.makeWithoutCaching(MacroBuilder.class, false)), "INSTANCE"));
        macroCall.setSpreadSafe(false);
        macroCall.setSafe(false);
        macroCall.setImplicitThis(false);
        
        return true;
    }

    @Override
    protected boolean isBuildInvocation(MethodCallExpression call) {
        return isBuildInvocation(call, MacroTransformation.MACRO_METHOD);
    }

    public static boolean isBuildInvocation(MethodCallExpression call, String methodName) {
        if (call == null) throw new IllegalArgumentException("Null: call");
        if(methodName == null) throw new IllegalArgumentException("Null: methodName");

        if(!(call.getMethod() instanceof ConstantExpression)) {
            return false;
        }

        if(!(methodName.equals(call.getMethodAsString()))) {
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

        if(!(macroCallArguments instanceof TupleExpression)) {
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

        if(tupleArguments.getExpressions().size() < 1) {
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
