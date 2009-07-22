/*
 * Copyright 2003-2009 the original author or authors.
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
import org.objectweb.asm.Opcodes;

import java.util.*;

/**
 * Visitor to resolve constants and method calls from static Imports
 *
 * @author Jochen Theodorou
 * @author Paul King
 */
public class StaticImportVisitor extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private MethodNode currentMethod;
    private SourceUnit source;
    private CompilationUnit compilationUnit; // TODO use it or lose it
    private boolean stillResolving;
    private boolean inSpecialConstructorCall;
    private boolean inClosure;
    private boolean inPropertyExpression;
    private Expression foundConstant;
    private Expression foundArgs;
    private boolean inAnnotation;

    public StaticImportVisitor(CompilationUnit cu) {
        compilationUnit = cu;
    }

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        super.visitClass(node);
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        this.currentMethod = node;
        super.visitConstructorOrMethod(node, isConstructor);
        this.currentMethod = null;
    }

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        boolean oldInAnnotation = inAnnotation;
        inAnnotation = true;
        super.visitAnnotations(node);
        inAnnotation = oldInAnnotation;
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
        if (exp.getClass() == ArgumentListExpression.class) {
            Expression result = exp.transformExpression(this);
            if (inPropertyExpression) {
                foundArgs = result;
            }
            return result;
        }
        if (exp instanceof ConstantExpression) {
            Expression result = exp.transformExpression(this);
            if (inPropertyExpression) {
                foundConstant = result;
            }
            if (inAnnotation && exp instanceof AnnotationConstantExpression) {
                ConstantExpression ce = (ConstantExpression) result;
                if (ce.getValue() instanceof AnnotationNode) {
                    // replicate a little bit of AnnotationVisitor here
                    // because we can't wait until later to do this
                    AnnotationNode an = (AnnotationNode) ce.getValue();
                    Map<String, Expression> attributes = an.getMembers();
                    for (Map.Entry entry : attributes.entrySet()) {
                        Expression attrExpr = transform((Expression) entry.getValue());
                        entry.setValue(attrExpr);
                    }

                }
            }
            return result;
        }
        return exp.transformExpression(this);
    }

    protected Expression transformVariableExpression(VariableExpression ve) {
        Variable v = ve.getAccessedVariable();
        if (v != null && v instanceof DynamicVariable) {
            Expression result = findStaticFieldImportFromModule(v.getName());
            if (result != null) {
            	result.setSourcePosition(ve);
                if (inAnnotation) {
                    result = transformInlineConstants(result);
                }
            	return result;
            }
            if (!inPropertyExpression || inSpecialConstructorCall) addStaticVariableError(ve);
        }
        return ve;
    }

    // resolve constant-looking expressions statically (do here as gets transformed away later)
    private Expression transformInlineConstants(Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            if (pe.getObjectExpression() instanceof ClassExpression) {
                ClassExpression ce = (ClassExpression) pe.getObjectExpression();
                ClassNode type = ce.getType();
                if (type.isEnum()) return exp;
                FieldNode fn = type.getField(pe.getPropertyAsString());
                if (fn != null && !fn.isEnum() && fn.isStatic() && fn.isFinal()) {
                    if (fn.getInitialValueExpression() instanceof ConstantExpression) {
                        return fn.getInitialValueExpression();
                    }
                }
            }
        } else if (exp instanceof ListExpression) {
            ListExpression le = (ListExpression) exp;
            ListExpression result = new ListExpression();
            for (Expression e : le.getExpressions()) {
                result.addExpression(transformInlineConstants(e));
            }
            return result;
        }

        return exp;
    }

    protected Expression transformMethodCallExpression(MethodCallExpression mce) {
        Expression args = transform(mce.getArguments());
        Expression method = transform(mce.getMethod());
        Expression object = transform(mce.getObjectExpression());
        boolean isExplicitThisOrSuper = false;
        if (object instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) object;
            isExplicitThisOrSuper = !mce.isImplicitThis() && (ve.getName().equals("this") || ve.getName().equals("super"));
        }

        if (mce.isImplicitThis() || isExplicitThisOrSuper) {
            if (mce.isImplicitThis()) {
                Expression ret = findStaticMethodImportFromModule(method, args);
                if (ret != null) {
                    ret.setSourcePosition(mce);
                    return ret;
                }
            }

            if (method instanceof ConstantExpression) {
                ConstantExpression ce = (ConstantExpression) method;
                Object value = ce.getValue();
                if (value instanceof String) {
                    String methodName = (String) value;
                    boolean lookForPossibleStaticMethod = true;
                    if(this.currentMethod != null && !this.currentMethod.isStatic()) {
                        if(currentClass.hasPossibleMethod(methodName, args)) {
                            lookForPossibleStaticMethod = false;
                        }
                    }
                    if (inSpecialConstructorCall ||
                            (lookForPossibleStaticMethod && currentClass.hasPossibleStaticMethod(methodName, args))) {
                    	StaticMethodCallExpression smce = new StaticMethodCallExpression(currentClass, methodName, args);
                    	smce.setSourcePosition(mce);
                    	return smce;
                    }
                }
            }
        }

        MethodCallExpression result = new MethodCallExpression(object, method, args);
        result.setSafe(mce.isSafe());
        result.setImplicitThis(mce.isImplicitThis());
        result.setSpreadSafe(mce.isSpreadSafe());
        result.setSourcePosition(mce);
        return result;
    }

    protected Expression transformConstructorCallExpression(ConstructorCallExpression cce) {
        inSpecialConstructorCall = cce.isSpecialCall();
        Expression ret = cce.transformExpression(this);
        inSpecialConstructorCall = false;
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
        boolean oldInPropertyExpression = inPropertyExpression;
        Expression oldFoundArgs = foundArgs;
        Expression oldFoundMethod = foundConstant;
        inPropertyExpression = true;
        foundArgs = null;
        foundConstant = null;
        Expression objectExpression = transform(pe.getObjectExpression());

        // check for static field access in a static method in the class containing the field
        if (objectExpression instanceof ClassExpression && currentMethod != null && currentMethod.isStatic()) {
            ClassExpression ce = (ClassExpression) objectExpression;
            if (ce.getType().getName().equals(currentClass.getName())) {
                FieldNode field = currentClass.getDeclaredField(pe.getPropertyAsString());
                if (field != null && field.isStatic()) {
                    Expression expression = new FieldExpression(field);
                    expression.setSourcePosition(pe);
                    return expression;
                }
            }
        }

        // some this/super validation
        boolean isExplicitThisOrSuper = false;
        if (objectExpression instanceof VariableExpression) {
            VariableExpression ve = (VariableExpression) objectExpression;
            isExplicitThisOrSuper = !pe.isImplicitThis() && (ve.getName().equals("this") || ve.getName().equals("super"));
            if (isExplicitThisOrSuper && currentMethod != null && currentMethod.isStatic()) {
                addError("Non-static variable '" + ve.getName() + "' cannot be referenced from the static method " + currentMethod.getName() + ".", pe);
                return null;
            }
        }

        if (foundArgs != null && foundConstant != null) {
            Expression result = findStaticMethodImportFromModule(foundConstant, foundArgs);
            if (result != null) {
                objectExpression = result;
            }
        }
        inPropertyExpression = oldInPropertyExpression;
        foundArgs = oldFoundArgs;
        foundConstant = oldFoundMethod;
        pe.setObjectExpression(objectExpression);
        if (!inSpecialConstructorCall) checkStaticScope(pe);
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
        // propertyExpressions will handle the error a bit differently
        if (!inSpecialConstructorCall && (inClosure || !ve.isInStaticContext())) return;
        if (stillResolving) return;
        if (ve.isThisExpression() || ve.isSuperExpression()) return;
        Variable v = ve.getAccessedVariable();
        if (v != null && !(v instanceof DynamicVariable) && v.isInStaticContext()) return;
        addError("Apparent variable '" + ve.getName() + "' was found in a static scope but doesn't refer" +
                " to a local variable, static field or class. Possible causes:\n" +
                "You attempted to reference a variable in the binding or an instance variable from a static context.\n" +
                "You misspelled a classname or statically imported field. Please check the spelling.\n" +
                "You attempted to use a method '" + ve.getName() +
                "' but left out brackets in a place not allowed by the grammar.", ve);
    }

    private Expression findStaticFieldImportFromModule(String name) {
        ModuleNode module = currentClass.getModule();
        if (module == null) return null;
        Map<String, ImportNode> importNodes = module.getStaticImports();
        stillResolving = false;
        if (importNodes.containsKey(name)) {
            ImportNode importNode = importNodes.get(name);
            Expression expression = findStaticField(importNode.getType(), importNode.getFieldName());
            if (expression != null) return expression;
        }
        for (ImportNode importNode : module.getStaticStarImports().values()) {
            ClassNode node = importNode.getType();
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
        } else {
            stillResolving = true;
        }
        return null;
    }

    private Expression findStaticMethodImportFromModule(Expression method, Expression args) {
        ModuleNode module = currentClass.getModule();
        if (module == null || !(method instanceof ConstantExpression)) return null;
        Map<String, ImportNode> importNodes = module.getStaticImports();
        ConstantExpression ce = (ConstantExpression) method;
        Object value = ce.getValue();
        // skip non-Strings, e.g. Integer
        if (!(value instanceof String)) return null;
        final String name = (String) value;
        if (importNodes.containsKey(name)) {
            ImportNode importNode = importNodes.get(name);
            Expression expression = findStaticMethod(importNode.getType(), importNode.getFieldName(), args);
            if (expression != null) return expression;
        }
        Map<String, ImportNode> importPackages = module.getStaticStarImports();
        ClassNode starImportType = null;
        if(isEnum(currentClass) && importPackages.containsKey(currentClass.getName())) {
            ImportNode importNode = importPackages.get(currentClass.getName());
            starImportType = importNode == null ? null : importNode.getType();
            Expression expression = findStaticMethod(starImportType, name, args);
            if (expression != null) return expression;
        } else {
            for (Map.Entry<String, ImportNode> entry : importPackages.entrySet()) {
                String className = entry.getKey();
                ImportNode importNode = importPackages.get(className);
                starImportType = importNode == null ? null : importNode.getType();
                Expression expression = findStaticMethod(starImportType, name, args);
                if (expression != null) return expression;
            }
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

    private boolean isEnum(ClassNode node) {
        return (node.getModifiers() & Opcodes.ACC_ENUM) != 0;
    }
}
