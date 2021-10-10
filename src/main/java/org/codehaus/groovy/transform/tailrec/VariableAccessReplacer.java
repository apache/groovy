package org.codehaus.groovy.transform.tailrec;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.util.Map;

/**
 * Replace all access to variables and args by new variables.
 * The variable names to replace as well as their replacement name and type have to be configured
 * in nameAndTypeMapping before calling replaceIn().
 * <p>
 * The VariableReplacedListener can be set if clients want to react to variable replacement.
 */
class VariableAccessReplacer {
    public VariableAccessReplacer(Map<String, Map> nameAndTypeMapping) {
        this.nameAndTypeMapping = nameAndTypeMapping;
    }

    public VariableAccessReplacer(Map<String, Map> nameAndTypeMapping, VariableReplacedListener listener) {
        this.nameAndTypeMapping = nameAndTypeMapping;
        this.listener = listener;
    }

    public void replaceIn(ASTNode root) {
        Closure<Boolean> whenParam = new Closure<Boolean>(this, this) {
            public Boolean doCall(VariableExpression expr) {
                return nameAndTypeMapping.containsKey(expr.getName());
            }

        };
        Closure<VariableExpression> replaceWithLocalVariable = new Closure<VariableExpression>(this, this) {
            public VariableExpression doCall(VariableExpression expr) {
                VariableExpression newVar = AstHelper.createVariableReference(nameAndTypeMapping.get(expr.getName()));
                getListener().variableReplaced(expr, newVar);
                return newVar;
            }

        };
        new VariableExpressionReplacer(whenParam, replaceWithLocalVariable).replaceIn(root);
    }

    public void setNameAndTypeMapping(Map<String, Map> nameAndTypeMapping) {
        this.nameAndTypeMapping = nameAndTypeMapping;
    }

    public VariableReplacedListener getListener() {
        return listener;
    }

    public void setListener(VariableReplacedListener listener) {
        this.listener = listener;
    }

    /**
     * Nested map of variable accesses to replace
     * e.g.: [
     * 'varToReplace': [name: 'newVar', type: TypeOfVar],
     * 'varToReplace2': [name: 'newVar2', type: TypeOfVar2],
     * ]
     */
    private Map<String, Map> nameAndTypeMapping;
    private VariableReplacedListener listener = VariableReplacedListener.NULL;
}
