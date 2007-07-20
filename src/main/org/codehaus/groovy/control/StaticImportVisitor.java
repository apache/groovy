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

import java.util.Iterator;
import java.util.Map;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.*;

/**
 * Visitor to resolve constants and method calls from static Imports
 *
 * @author Jochen Theodorou
 * @auther Paul King
 */
public class StaticImportVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private CompilationUnit compilationUnit;

    public StaticImportVisitor(CompilationUnit cu) {
        compilationUnit = cu;
    }

    public void visitClass(ClassNode node) {
        this.currentClass = node;
        super.visitClass(node);
    }

    private Expression findStaticFieldImportFromModule(String name) {
        ModuleNode module = currentClass.getModule();
        if (module == null) return null;
        Map aliases = module.getStaticImportAliases();
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

    public Expression transform(Expression exp) {
        if (exp==null) return null;
        if (exp instanceof VariableExpression) {
            return transformVariableExpression((VariableExpression) exp);
        } else if (exp instanceof MethodCallExpression) {
            return transformMethodCallExpression((MethodCallExpression)exp);
        } else {
            return exp.transformExpression(this);
        }
    }

    protected Expression transformVariableExpression(VariableExpression ve) {
        Variable v = ve.getAccessedVariable();
        if (v instanceof DynamicVariable) {
            Expression result = findStaticFieldImportFromModule(ve.getName());
            if (result != null) return result;
        }
        return ve;
    }
    
    protected Expression transformMethodCallExpression(MethodCallExpression mce) {
        Expression args = transform(mce.getArguments());
        Expression method = transform(mce.getMethod());

        if (mce.isImplicitThis()) {
            Expression ret = findStaticMethodImportFromModule(method, args);
            if (ret != null) return ret;
        }
        return mce;
    }

    protected SourceUnit getSourceUnit() {
        return null;
    }
}
