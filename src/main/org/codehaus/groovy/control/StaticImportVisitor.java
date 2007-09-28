/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.*;

/**
 * Visitor to resolve constants and method calls from static Imports
 *
 * @author Jochen Theodorou
 * @author Paul King
 */
public class StaticImportVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private SourceUnit source;
    private CompilationUnit compilationUnit;
    private boolean stillResolving;
    private boolean inSpecialContructorCall;
    private boolean inClosure;
    private boolean inPropertyExpression;

    public StaticImportVisitor(CompilationUnit cu) {
        compilationUnit = cu;
    }

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        super.visitClass(node);
    }

    public Expression transform(Expression exp) {
        if (exp == null) return null;
        if (exp.getClass() == VariableExpression.class) {
            return transformVariableExpression((VariableExpression) exp);
        }
        if (exp.getClass() == PropertyExpression.class) {
            return transformPropertyExpression((PropertyExpression) exp);
        }
        if (exp.getClass() == MethodCallExpression.class) {
            return transformMethodCallExpression((MethodCallExpression) exp);
        }
        if (exp.getClass() == ClosureExpression.class) {
            return transformClosureExpression((ClosureExpression) exp);
        }
        if (exp.getClass() == ConstructorCallExpression.class) {
            return transformConstructorCallExpression((ConstructorCallExpression) exp);
        }
        return exp.transformExpression(this);
    }

    protected Expression transformVariableExpression(VariableExpression ve) {
        Variable v = ve.getAccessedVariable();
        if (v == null || !(v instanceof DynamicVariable)) return ve;
        Expression result = findStaticFieldImportFromModule(v.getName());
        if (result != null) return result;
        if (!inPropertyExpression || inSpecialContructorCall) addStaticVariableError(ve);
        return ve;
    }

    protected Expression transformMethodCallExpression(MethodCallExpression mce) {
        Expression args = transform(mce.getArguments());
        Expression method = transform(mce.getMethod());
        if (mce.isImplicitThis()) {
            Expression ret = findStaticMethodImportFromModule(method, args);
            if (ret != null) {
                return ret;
            }
        }
        mce.setArguments(args);
        mce.setMethod(method);
        return mce;
    }

    protected Expression transformConstructorCallExpression(ConstructorCallExpression cce) {
        inSpecialContructorCall = cce.isSpecialCall();
        Expression ret = cce.transformExpression(this);
        inSpecialContructorCall = false;
        return ret;
    }

    protected Expression transformClosureExpression(ClosureExpression ce) {
        boolean oldInClosure = inClosure;
        inClosure = true;
        Statement code = ce.getCode();
        if (code != null) code.visit(this);
        inClosure = oldInClosure;
        return ce;
    }

    protected Expression transformPropertyExpression(PropertyExpression pe) {
        boolean ipe = inPropertyExpression;
        Expression objectExpression = pe.getObjectExpression();
        inPropertyExpression = true;
        objectExpression = objectExpression.transformExpression(this);
        inPropertyExpression = false;
        Expression property = pe.getProperty().transformExpression(this);
        inPropertyExpression = ipe;

        boolean spreadSafe = pe.isSpreadSafe();
        pe = new PropertyExpression(objectExpression, property, pe.isSafe());
        pe.setSpreadSafe(spreadSafe);
        if (!inSpecialContructorCall) checkStaticScope(pe);
        return pe;
    }

    private void checkStaticScope(PropertyExpression pe) {
        if (inClosure) return;
        for (Expression it = pe; it != null; it = ((PropertyExpression) it).getObjectExpression()) {
            if (it instanceof PropertyExpression) continue;
            if (it instanceof VariableExpression) {
                addStaticVariableError((VariableExpression) it);
            }
            return;
        }
    }

    private void addStaticVariableError(VariableExpression ve) {
        // closures are always dynamic
        // propertiesExpressions will handle the error a bit different
        if (!inSpecialContructorCall && (inClosure || !ve.isInStaticContext())) return;
        if (stillResolving) return;
        if (ve == VariableExpression.THIS_EXPRESSION || ve == VariableExpression.SUPER_EXPRESSION) return;
        Variable v = ve.getAccessedVariable();
        if (v != null && !(v instanceof DynamicVariable) && v.isInStaticContext()) return;
        addError("the name " + ve.getName() + " doesn't refer to a declared variable or class. The static" +
                " scope requires that you declare variables before using them. If the variable should have" +
                " been a class check the spelling.", ve);
    }

    private Expression findStaticFieldImportFromModule(String name) {
        ModuleNode module = currentClass.getModule();
        if (module == null) return null;
        Map aliases = module.getStaticImportAliases();
        stillResolving = false;
        if (aliases.containsKey(name)) {
            ClassNode node = (ClassNode) aliases.get(name);
            Map fields = module.getStaticImportFields();
            String fieldName = (String) fields.get(name);
            Expression expression = findStaticField(node, fieldName);
            if (expression != null) return expression;
        }
        Map importedClasses = module.getStaticImportClasses();
        Iterator it = importedClasses.keySet().iterator();
        while (it.hasNext()) {
            String className = (String) it.next();
            ClassNode node = (ClassNode) importedClasses.get(className);
            Expression expression = findStaticField(node, name);
            if (expression != null) return expression;
        }
        return null;
    }

    private Expression findStaticField(ClassNode staticImportType, String fieldName) {
        if (!staticImportType.isResolved() && !staticImportType.isPrimaryClassNode()) {
            stillResolving = true;
        }
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            staticImportType.getFields(); // force init
            FieldNode field = staticImportType.getField(fieldName);
            if (field != null && field.isStatic()) {
                return new PropertyExpression(new ClassExpression(staticImportType), fieldName);
            }
        }
        return null;
    }

    private Expression findStaticMethodImportFromModule(Expression method, Expression args) {
        ModuleNode module = currentClass.getModule();
        if (module == null || !(method instanceof ConstantExpression)) return null;
        Map aliases = module.getStaticImportAliases();
        ConstantExpression ce = (ConstantExpression) method;
        final String name = (String) ce.getValue();
        if (aliases.containsKey(name)) {
            ClassNode node = (ClassNode) aliases.get(name);
            Map fields = module.getStaticImportFields();
            String fieldName = (String) fields.get(name);
            Expression expression = findStaticMethod(node, fieldName, args);
            if (expression != null) return expression;
        }
        Map importPackages = module.getStaticImportClasses();
        Iterator it = importPackages.keySet().iterator();
        while (it.hasNext()) {
            String className = (String) it.next();
            ClassNode starImportType = (ClassNode) importPackages.get(className);
            Expression expression = findStaticMethod(starImportType, name, args);
            if (expression != null) return expression;
        }
        return null;
    }

    private Expression findStaticMethod(ClassNode staticImportType, String methodName, Expression args) {
        if (staticImportType.isPrimaryClassNode() || staticImportType.isResolved()) {
            if (staticImportType.hasPossibleStaticMethod(methodName, args)) {
                return new StaticMethodCallExpression(staticImportType, methodName, args);
            }
        }
        return null;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }
}
