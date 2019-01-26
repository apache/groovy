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
package org.codehaus.groovy.transform;

import groovy.lang.Newify;
import groovy.lang.Reference;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;

/**
 * Handles generation of code for the {@code @Newify} AST transform.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class NewifyASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation {
    private static final ClassNode MY_TYPE = make(Newify.class);
    private static final String MY_NAME = MY_TYPE.getNameWithoutPackage();
    private static final String BASE_BAD_PARAM_ERROR = "Error during @" + MY_NAME +
            " processing. Annotation parameter must be a class or list of classes but found ";
    private SourceUnit source;
    private ListExpression classesToNewify;
    private DeclarationExpression candidate;
    private boolean auto;
    private Pattern classNamePattern;

    private static Map<String, ClassNode> nameToGlobalClassesNodesMap;
    private Map<String, NewifyClassData> nameToInnerClassesNodesMap;

    // ClassHelper.classes minus interfaces, abstract classes, and classes with private ctors
    private static final Class[] globalClasses = new Class[]{
            Object.class,
            Boolean.TYPE,
            Character.TYPE,
            Byte.TYPE,
            Short.TYPE,
            Integer.TYPE,
            Long.TYPE,
            Double.TYPE,
            Float.TYPE,
            // Void.TYPE,
            // Closure.class,
            // GString.class,
            // List.class,
            // Map.class,
            // Range.class,
            //Pattern.class,
            // Script.class,
            String.class,
            Boolean.class,  // Shall we allow this ? Using Boolean ctors is usually not what user wants...
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Double.class,
            Float.class,
            BigDecimal.class,
            BigInteger.class,
            //Number.class,
            //Void.class,
            Reference.class,
            //Class.class,
            //MetaClass.class,
            //Iterator.class,
            //GeneratedClosure.class,
            //GeneratedLambda.class,
            //GroovyObjectSupport.class
    };

    static {
        nameToGlobalClassesNodesMap = new ConcurrentHashMap<>(16, 0.9f, 1);
        for (Class globalClass : globalClasses) {
            nameToGlobalClassesNodesMap.put(globalClass.getSimpleName(), ClassHelper.makeCached(globalClass));
        }
    }


    private static final Pattern extractNamePattern = Pattern.compile("^(?:.*\\$|)(.*)$");

    public static String extractName(final String s) {
        return extractNamePattern.matcher(s).replaceFirst("$1");
    }

    public void visit(ASTNode[] nodes, SourceUnit source) {
        this.source = source;
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            internalError("Expecting [AnnotationNode, AnnotatedClass] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) {
            internalError("Transformation called from wrong annotation: " + node.getClassNode().getName());
        }

        final boolean autoFlag = determineAutoFlag(node.getMember("auto"));
        final Expression classNames = node.getMember("value");
        final Pattern cnPattern = determineClassNamePattern(node.getMember("pattern"));

        if (parent instanceof ClassNode) {
            newifyClass((ClassNode) parent, autoFlag, determineClasses(classNames, false), cnPattern);
        } else if (parent instanceof MethodNode || parent instanceof FieldNode) {
            newifyMethodOrField(parent, autoFlag, determineClasses(classNames, false), cnPattern);
        } else if (parent instanceof DeclarationExpression) {
            newifyDeclaration((DeclarationExpression) parent, autoFlag, determineClasses(classNames, true), cnPattern);
        }
    }


    private void newifyClass(ClassNode cNode, boolean autoFlag, ListExpression list, final Pattern cnPattern) {
        String cName = cNode.getName();
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cName + "'. @"
                    + MY_NAME + " not allowed for interfaces.", cNode);
        }

        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        final Pattern oldCnPattern = classNamePattern;

        classesToNewify = list;
        auto = autoFlag;
        classNamePattern = cnPattern;

        super.visitClass(cNode);

        classesToNewify = oldClassesToNewify;
        auto = oldAuto;
        classNamePattern = oldCnPattern;
    }

    private void newifyMethodOrField(AnnotatedNode parent, boolean autoFlag, ListExpression list, final Pattern cnPattern) {

        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        final Pattern oldCnPattern = classNamePattern;

        checkClassLevelClashes(list);
        checkAutoClash(autoFlag, parent);

        classesToNewify = list;
        auto = autoFlag;
        classNamePattern = cnPattern;

        if (parent instanceof FieldNode) {
            super.visitField((FieldNode) parent);
        } else {
            super.visitMethod((MethodNode) parent);
        }

        classesToNewify = oldClassesToNewify;
        auto = oldAuto;
        classNamePattern = oldCnPattern;
    }


    private void newifyDeclaration(DeclarationExpression de, boolean autoFlag, ListExpression list, final Pattern cnPattern) {
        ClassNode cNode = de.getDeclaringClass();
        candidate = de;
        final ListExpression oldClassesToNewify = classesToNewify;
        final boolean oldAuto = auto;
        final Pattern oldCnPattern = classNamePattern;

        classesToNewify = list;
        auto = autoFlag;
        classNamePattern = cnPattern;

        super.visitClass(cNode);

        classesToNewify = oldClassesToNewify;
        auto = oldAuto;
        classNamePattern = oldCnPattern;
    }

    private static boolean determineAutoFlag(Expression autoExpr) {
        return !(autoExpr instanceof ConstantExpression && ((ConstantExpression) autoExpr).getValue().equals(false));
    }

    private Pattern determineClassNamePattern(Expression expr) {
        if (!(expr instanceof ConstantExpression)) { return null; }
        final ConstantExpression constExpr = (ConstantExpression) expr;
        final String text = constExpr.getText();
        if (constExpr.getValue() == null || text.equals("")) { return null; }
        try {
            final Pattern pattern = Pattern.compile(text);
            return pattern;
        } catch (PatternSyntaxException e) {
            addError("Invalid class name pattern: " + e.getMessage(), expr);
            return null;
        }
    }

    /**
     * allow non-strict mode in scripts because parsing not complete at that point
     */
    private ListExpression determineClasses(Expression expr, boolean searchSourceUnit) {
        ListExpression list = new ListExpression();
        if (expr instanceof ClassExpression) {
            list.addExpression(expr);
        } else if (expr instanceof VariableExpression && searchSourceUnit) {
            VariableExpression ve = (VariableExpression) expr;
            ClassNode fromSourceUnit = getSourceUnitClass(ve);
            if (fromSourceUnit != null) {
                ClassExpression found = classX(fromSourceUnit);
                found.setSourcePosition(ve);
                list.addExpression(found);
            } else {
                addError(BASE_BAD_PARAM_ERROR + "an unresolvable reference to '" + ve.getName() + "'.", expr);
            }
        } else if (expr instanceof ListExpression) {
            list = (ListExpression) expr;
            final List<Expression> expressions = list.getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                Expression next = expressions.get(i);
                if (next instanceof VariableExpression && searchSourceUnit) {
                    VariableExpression ve = (VariableExpression) next;
                    ClassNode fromSourceUnit = getSourceUnitClass(ve);
                    if (fromSourceUnit != null) {
                        ClassExpression found = classX(fromSourceUnit);
                        found.setSourcePosition(ve);
                        expressions.set(i, found);
                    } else {
                        addError(BASE_BAD_PARAM_ERROR + "a list containing an unresolvable reference to '" + ve.getName() + "'.", next);
                    }
                } else if (!(next instanceof ClassExpression)) {
                    addError(BASE_BAD_PARAM_ERROR + "a list containing type: " + next.getType().getName() + ".", next);
                }
            }
            checkDuplicateNameClashes(list);
        } else if (expr != null) {
            addError(BASE_BAD_PARAM_ERROR + "a type: " + expr.getType().getName() + ".", expr);
        }
        return list;
    }

    private ClassNode getSourceUnitClass(VariableExpression ve) {
        List<ClassNode> classes = source.getAST().getClasses();
        for (ClassNode classNode : classes) {
            if (classNode.getNameWithoutPackage().equals(ve.getName())) return classNode;
        }
        return null;
    }

    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof MethodCallExpression && candidate == null) {
            MethodCallExpression mce = (MethodCallExpression) expr;
            Expression args = transform(mce.getArguments());
            if (isNewifyCandidate(mce)) {
                Expression transformed = transformMethodCall(mce, args);
                transformed.setSourcePosition(mce);
                return transformed;
            }
            Expression method = transform(mce.getMethod());
            Expression object = transform(mce.getObjectExpression());
            MethodCallExpression transformed = callX(object, method, args);
            transformed.setImplicitThis(mce.isImplicitThis());
            transformed.setSafe(mce.isSafe());
            transformed.setSourcePosition(mce);
            return transformed;
        } else if (expr instanceof ClosureExpression) {
            ClosureExpression ce = (ClosureExpression) expr;
            ce.getCode().visit(this);
        } else if (expr instanceof ConstructorCallExpression) {
            ConstructorCallExpression cce = (ConstructorCallExpression) expr;
            if (cce.isUsingAnonymousInnerClass()) {
                cce.getType().visitContents(this);
            }
        } else if (expr instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) expr;
            if (shouldTransform(de)) {
                candidate = null;
                Expression left = de.getLeftExpression();
                Expression right = transform(de.getRightExpression());
                DeclarationExpression newDecl = new DeclarationExpression(left, de.getOperation(), right);
                newDecl.addAnnotations(de.getAnnotations());
                return newDecl;
            }
            return de;
        }
        return expr.transformExpression(this);
    }

    private boolean shouldTransform(DeclarationExpression exp) {
        return exp == candidate || auto || hasClassesToNewify();
    }

    private boolean hasClassesToNewify() {
        return (classesToNewify != null && !classesToNewify.getExpressions().isEmpty()) || (classNamePattern != null);
    }


    private void checkDuplicateNameClashes(ListExpression list) {
        final Set<String> seen = new HashSet<>();
        @SuppressWarnings("unchecked") final List<ClassExpression> classes = (List) list.getExpressions();
        for (ClassExpression ce : classes) {
            final String name = ce.getType().getNameWithoutPackage();
            if (seen.contains(name)) {
                addError("Duplicate name '" + name + "' found during @" + MY_NAME + " processing.", ce);
            }
            seen.add(name);
        }
    }

    private void checkAutoClash(boolean autoFlag, AnnotatedNode parent) {
        if (auto && !autoFlag) {
            addError("Error during @" + MY_NAME + " processing. The 'auto' flag can't be false at " +
                    "method/constructor/field level if it is true at the class level.", parent);
        }
    }

    private void checkClassLevelClashes(ListExpression list) {
        @SuppressWarnings("unchecked") final List<ClassExpression> classes = (List) list.getExpressions();
        for (ClassExpression ce : classes) {
            final String name = ce.getType().getNameWithoutPackage();
            if (findClassWithMatchingBasename(name)) {
                addError("Error during @" + MY_NAME + " processing. Class '" + name + "' can't appear at " +
                        "method/constructor/field level if it already appears at the class level.", ce);
            }
        }
    }


    private boolean isNewifyCandidate(MethodCallExpression mce) {
        return mce.getObjectExpression() == VariableExpression.THIS_EXPRESSION
                || (auto && isNewMethodStyle(mce));
    }

    private static boolean isNewMethodStyle(MethodCallExpression mce) {
        final Expression obj = mce.getObjectExpression();
        final Expression meth = mce.getMethod();
        return (obj instanceof ClassExpression && meth instanceof ConstantExpression
                && ((ConstantExpression) meth).getValue().equals("new"));
    }

    private Expression transformMethodCall(MethodCallExpression mce, Expression argsExp) {
        ClassNode classType;

        if (isNewMethodStyle(mce)) {
            classType = mce.getObjectExpression().getType();
        } else {
            classType = findMatchingCandidateClass(mce);
        }

        if (classType != null) {
            Expression argsToUse = argsExp;
            if (classType.getOuterClass() != null && ((classType.getModifiers() & org.objectweb.asm.Opcodes.ACC_STATIC) == 0)) {
                if (!(argsExp instanceof ArgumentListExpression)) {
                    addError("Non-static inner constructor arguments must be an argument list expression; pass 'this' pointer explicitely as first constructor argument otherwise.", mce);
                    return mce;
                }
                final ArgumentListExpression argsListExp = (ArgumentListExpression) argsExp;
                final List<Expression> argExpList = argsListExp.getExpressions();
                final VariableExpression thisVarExp = new VariableExpression("this");

                final List<Expression> expressionsWithThis = new ArrayList<>(argExpList.size() + 1);
                expressionsWithThis.add(thisVarExp);
                expressionsWithThis.addAll(argExpList);

                argsToUse = new ArgumentListExpression(expressionsWithThis);
            }
            return new ConstructorCallExpression(classType, argsToUse);
        }

        // set the args as they might have gotten Newify transformed GROOVY-3491
        mce.setArguments(argsExp);
        return mce;
    }


    private boolean findClassWithMatchingBasename(String nameWithoutPackage) {
        // For performance reasons test against classNamePattern first
        if (classNamePattern != null && classNamePattern.matcher(nameWithoutPackage).matches()) {
            return true;
        }

        if (classesToNewify != null) {
            @SuppressWarnings("unchecked") final List<ClassExpression> classes = (List) classesToNewify.getExpressions();
            for (ClassExpression ce : classes) {
                if (ce.getType().getNameWithoutPackage().equals(nameWithoutPackage)) {
                    return true;
                }
            }
        }

        return false;
    }

    private ClassNode findMatchingCandidateClass(MethodCallExpression mce) {
        final String methodName = mce.getMethodAsString();

        if (classesToNewify != null) {
            @SuppressWarnings("unchecked")
            List<ClassExpression> classes = (List) classesToNewify.getExpressions();
            for (ClassExpression ce : classes) {
                final ClassNode type = ce.getType();
                if (type.getNameWithoutPackage().equals(methodName)) {
                    return type;
                }
            }
        }

        if (classNamePattern != null && classNamePattern.matcher(methodName).matches()) {

            // One-time-fill inner classes lookup map
            if (nameToInnerClassesNodesMap == null) {
                final List<ClassNode> innerClassNodes = source.getAST().getClasses();
                nameToInnerClassesNodesMap = new HashMap<>(innerClassNodes.size());
                for (ClassNode type : innerClassNodes) {
                    final String pureClassName = extractName(type.getNameWithoutPackage());
                    final NewifyClassData classData = nameToInnerClassesNodesMap.get(pureClassName);
                    if (classData == null) {
                        nameToInnerClassesNodesMap.put(pureClassName, new NewifyClassData(pureClassName, type));
                    } else {
                        // If class name is looked up below, additional types will be used in error message
                        classData.addAdditionalType(type);
                    }
                }
            }

            // Inner classes
            final NewifyClassData innerTypeClassData = nameToInnerClassesNodesMap.get(methodName);
            if (innerTypeClassData != null) {
                if (innerTypeClassData.types != null) {
                    addError("Inner class name lookup is ambiguous between the following classes: " + DefaultGroovyMethods.join(innerTypeClassData.types, ", ") + ". Use new keyword and qualify name to break ambiguity.", mce);
                    return null;
                }
                return innerTypeClassData.type;
            }

            // Imported classes
            final ClassNode importedType = source.getAST().getImportType(methodName);
            if (importedType != null) {
                return importedType;
            }

            // Global classes
            final ClassNode globalType = nameToGlobalClassesNodesMap.get(methodName);
            if (globalType != null) {
                return globalType;
            }
        }

        return null;
    }

    private static void internalError(String message) {
        throw new GroovyBugError("Internal error: " + message);
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }


    private static class NewifyClassData {
        final String name;
        final ClassNode type;
        List<ClassNode> types = null;

        public NewifyClassData(final String name, final ClassNode type) {
            this.name = name;
            this.type = type;
        }

        public void addAdditionalType(final ClassNode additionalType) {
            if (types == null) {
                types = new LinkedList<>();
                types.add(type);
            }
            types.add(additionalType);
        }
    }


}
