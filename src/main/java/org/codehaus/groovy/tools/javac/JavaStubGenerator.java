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
package org.codehaus.groovy.tools.javac;

import groovy.transform.PackageScope;
import groovy.transform.PackageScopeTarget;
import org.apache.groovy.ast.tools.ExpressionUtils;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.FinalVariableAnalyzer;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.classgen.VerifierCodeVisitor;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.tools.Utilities;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.Opcodes;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.apache.groovy.ast.tools.ConstructorNodeUtils.getFirstIfSpecialConstructorCall;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isStaticConstantInitializerType;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.WideningCategories.isFloatingCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isLongCategory;

public class JavaStubGenerator {
    private final boolean java5;
    private final String encoding;
    private final boolean requireSuperResolved;
    private final File outputPath;
    private final List<ConstructorNode> constructors = new ArrayList<>();
    private final Map<String, MethodNode> propertyMethods = new LinkedHashMap<>();
    private final static ClassNode PACKAGE_SCOPE_TYPE = ClassHelper.make(PackageScope.class);

    private ModuleNode currentModule;

    public JavaStubGenerator(final File outputPath, final boolean requireSuperResolved, final boolean java5, String encoding) {
        this.outputPath = outputPath;
        this.requireSuperResolved = requireSuperResolved;
        this.java5 = java5;
        this.encoding = encoding;
        if (null != outputPath) outputPath.mkdirs(); // when outputPath is null, we generate stubs in memory
    }

    public JavaStubGenerator(final File outputPath) {
        this(outputPath, false, false, Charset.defaultCharset().name());
    }

    private static void mkdirs(File parent, String relativeFile) {
        int index = relativeFile.lastIndexOf('/');
        if (index == -1) return;
        File dir = new File(parent, relativeFile.substring(0, index));
        dir.mkdirs();
    }

    public void generateClass(ClassNode classNode) throws FileNotFoundException {
        // Only attempt to render our self if our super-class is resolved, else wait for it
        if (requireSuperResolved && !classNode.getSuperClass().isResolved()) {
            return;
        }

        // owner should take care for us
        if (classNode instanceof InnerClassNode)
            return;

        // don't generate stubs for private classes, as they are only visible in the same file
        if ((classNode.getModifiers() & Opcodes.ACC_PRIVATE) != 0) return;


        if (null == outputPath) {
            generateMemStub(classNode);
        } else {
            generateFileStub(classNode);
        }
    }

    private void generateMemStub(ClassNode classNode) {
        javaStubCompilationUnitSet.add(new MemJavaFileObject(classNode, generateStubContent(classNode)));
    }

    private void generateFileStub(ClassNode classNode) throws FileNotFoundException {
        String fileName = classNode.getName().replace('.', '/');
        mkdirs(outputPath, fileName);

        File file = createJavaStubFile(fileName);

        Writer writer = new OutputStreamWriter(
                new FileOutputStream(file),
                Charset.forName(encoding)
        );

        try (PrintWriter out = new PrintWriter(writer)) {
            out.print(generateStubContent(classNode));
        }

        javaStubCompilationUnitSet.add(new RawJavaFileObject(createJavaStubFile(fileName).toPath().toUri()));
    }

    private String generateStubContent(ClassNode classNode) {
        Writer writer = new StringBuilderWriter(8192);
        try (PrintWriter out = new PrintWriter(writer)) {
            boolean packageInfo = "package-info".equals(classNode.getNameWithoutPackage());
            String packageName = classNode.getPackageName();
            if (packageName != null) {
                if (packageInfo) {
                    printAnnotations(out, classNode.getPackage());
                }
                out.println("package " + packageName + ";");
            }

            // should just output the package statement for `package-info` class node
            if (!packageInfo) {
                printImports(out, classNode);
                printClassContents(out, classNode);
            }
        }

        return writer.toString();
    }

    private static Iterable<ClassNode> findTraits(ClassNode node) {
        Set<ClassNode> traits = new LinkedHashSet<>();

        LinkedList<ClassNode> todo = new LinkedList<>();
        Collections.addAll(todo, node.getInterfaces());
        while (!todo.isEmpty()) {
            ClassNode next = todo.removeLast();
            if (Traits.isTrait(next)) traits.add(next);
            Collections.addAll(todo, next.getInterfaces());
        }

        return traits;
    }

    private void printClassContents(PrintWriter out, ClassNode classNode) {
        if (classNode instanceof InnerClassNode && ((InnerClassNode) classNode).isAnonymous()) {
            // if it is an anonymous inner class, don't generate the stub code for it.
            return;
        }
        try {
            Verifier verifier = new Verifier() {
                @Override
                public void visitClass(ClassNode node) {
                    List<Statement> savedStatements = new ArrayList<>(node.getObjectInitializerStatements());
                    super.visitClass(node);
                    node.getObjectInitializerStatements().addAll(savedStatements);

                    for (ClassNode trait : findTraits(node)) {
                        // GROOVY-9031: replace property type placeholder with resolved type from trait generics
                        Map<String, ClassNode> generics = trait.isUsingGenerics() ? createGenericsSpec(trait) : null;
                        for (PropertyNode traitProperty : trait.getProperties()) {
                            ClassNode traitPropertyType = traitProperty.getType();
                            traitProperty.setType(correctToGenericsSpecRecurse(generics, traitPropertyType));
                            super.visitProperty(traitProperty);
                            traitProperty.setType(traitPropertyType);
                        }
                    }
                }

                @Override
                public void visitConstructor(ConstructorNode node) {
                    Statement stmt = node.getCode();
                    if (stmt != null) {
                        stmt.visit(new VerifierCodeVisitor(getClassNode()));
                    }
                }

                @Override
                public void visitProperty(PropertyNode pn) {
                    // GROOVY-8233 skip static properties for traits since they don't make the interface
                    if (!pn.isStatic() || !Traits.isTrait(pn.getDeclaringClass())) {
                        super.visitProperty(pn);
                    }
                }

                public void addCovariantMethods(ClassNode cn) {}
                protected void addInitialization(ClassNode node) {}
                protected void addPropertyMethod(MethodNode method) {
                    doAddMethod(method);
                }
                protected void addReturnIfNeeded(MethodNode node) {}
                protected MethodNode addMethod(ClassNode node, boolean shouldBeSynthetic, String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
                    return doAddMethod(new MethodNode(name, modifiers, returnType, parameters, exceptions, code));
                }

                protected void addConstructor(Parameter[] newParams, ConstructorNode ctor, Statement code, ClassNode node) {
                    if (code instanceof ExpressionStatement) {//GROOVY-4508
                        Statement temp = code;
                        code = new BlockStatement();
                        ((BlockStatement) code).addStatement(temp);
                    }
                    ConstructorNode ctrNode = new ConstructorNode(ctor.getModifiers(), newParams, ctor.getExceptions(), code);
                    ctrNode.setDeclaringClass(node);
                    constructors.add(ctrNode);
                }

                protected void addDefaultParameters(DefaultArgsAction action, MethodNode method) {
                    final Parameter[] parameters = method.getParameters();
                    final Expression[] saved = new Expression[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        if (parameters[i].hasInitialExpression())
                            saved[i] = parameters[i].getInitialExpression();
                    }
                    super.addDefaultParameters(action, method);
                    for (int i = 0; i < parameters.length; i++) {
                        if (saved[i] != null)
                            parameters[i].setInitialExpression(saved[i]);
                    }
                }

                private MethodNode doAddMethod(MethodNode method) {
                    propertyMethods.putIfAbsent(method.getTypeDescriptor(), method);
                    return method;
                }

                @Override
                protected void addDefaultConstructor(ClassNode node) {
                    // not required for stub generation
                }

                @Override
                protected FinalVariableAnalyzer.VariableNotFinalCallback getFinalVariablesCallback() {
                    return null;
                }
            };
            int origNumConstructors = classNode.getDeclaredConstructors().size();
            verifier.visitClass(classNode);
            // undo unwanted side-effect of verifier
            if (origNumConstructors == 0 && classNode.getDeclaredConstructors().size() == 1) {
                classNode.getDeclaredConstructors().clear();
            }
            currentModule = classNode.getModule();

            boolean isEnum = classNode.isEnum();
            boolean isInterface = !isEnum && isInterfaceOrTrait(classNode);
            boolean isAnnotationDefinition = classNode.isAnnotationDefinition();
            printAnnotations(out, classNode);

            int flags = classNode.getModifiers();
            if (isEnum) flags &= ~Opcodes.ACC_FINAL;
            if (isEnum || isInterface) flags &= ~Opcodes.ACC_ABSTRACT;
            if (classNode.isSyntheticPublic() && hasPackageScopeXform(classNode,
                        PackageScopeTarget.CLASS)) flags &= ~Opcodes.ACC_PUBLIC;
            printModifiers(out, flags);

            if (isInterface) {
                if (isAnnotationDefinition) {
                    out.print("@");
                }
                out.print("interface ");
            } else if (isEnum) {
                out.print("enum ");
            } else {
                out.print("class ");
            }

            String className = classNode.getNameWithoutPackage();
            if (classNode instanceof InnerClassNode)
                className = className.substring(className.lastIndexOf('$') + 1);
            out.println(className);
            printGenericsBounds(out, classNode, true);

            ClassNode superClass = classNode.getUnresolvedSuperClass(false);

            if (!isInterface && !isEnum) {
                out.print("  extends ");
                printType(out, superClass);
            }

            ClassNode[] interfaces = classNode.getInterfaces();
            if (interfaces != null && interfaces.length > 0 && !isAnnotationDefinition) {
                if (isInterface) {
                    out.println("  extends");
                } else {
                    out.println("  implements");
                }
                for (int i = 0; i < interfaces.length - 1; ++i) {
                    out.print("    ");
                    printType(out, interfaces[i]);
                    out.print(",");
                }
                out.print("    ");
                printType(out, interfaces[interfaces.length - 1]);
            }
            out.println(" {");

            printFields(out, classNode, isInterface);
            printMethods(out, classNode, isEnum);

            for (Iterator<InnerClassNode> inner = classNode.getInnerClasses(); inner.hasNext(); ) {
                // GROOVY-4004: Clear the methods from the outer class so that they don't get duplicated in inner ones
                constructors.clear();
                propertyMethods.clear();
                printClassContents(out, inner.next());
            }

            out.println("}");
        } finally {
            currentModule = null;
            constructors.clear();
            propertyMethods.clear();
        }
    }

    private void printFields(PrintWriter out, ClassNode classNode, boolean ifaceOrTrait) {
        List<FieldNode> fields = classNode.getFields();
        if (!fields.isEmpty()) {
            List<FieldNode> enumFields = new ArrayList<>();
            List<FieldNode> normalFields = new ArrayList<>();
            for (FieldNode field : fields) {
                int flags = field.getModifiers();
                if (hasPackageScopeXform(field, PackageScopeTarget.FIELDS)){
                    flags &= ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC);
                    List<AnnotationNode> annotations = field.getAnnotations();
                    field = new FieldNode(field.getName(), flags, field.getType(), field.getOwner(), field.getInitialExpression());
                    field.setDeclaringClass(classNode);
                    field.addAnnotations(annotations);
                }

                if (field.isEnum()) {
                    enumFields.add(field);
                } else if ((flags & (Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC)) == 0) {
                    normalFields.add(field);
                }
            }

            printEnumFields(out, enumFields);
            for (FieldNode normalField : normalFields) {
                printField(out, normalField, ifaceOrTrait);
            }
        }
    }

    private static void printEnumFields(final PrintWriter out, final List<FieldNode> fields) {
        if (!fields.isEmpty()) {
            int i = 0;
            for (FieldNode field : fields) {
                if (i++ != 0) {
                    out.print(", ");
                }
                out.print(field.getName());
            }
        }
        out.println(';');
    }

    private void printField(final PrintWriter out, final FieldNode field, final boolean ifaceOrTrait) {
        printAnnotations(out, field);
        if (!ifaceOrTrait) {
            printModifiers(out, field.getModifiers());
        }
        ClassNode type = field.getType();
        printType(out, type);
        out.print(' ');
        out.print(field.getName());

        if (ifaceOrTrait || field.isFinal()) {
            out.print(" = ");
            if (field.isStatic()) {
                Expression value = ExpressionUtils.transformInlineConstants(field.getInitialValueExpression(), type);
                if (value instanceof ConstantExpression) {
                    if (isPrimitiveType(type)) { // do not pass string of length 1 for String field:
                        value = Verifier.transformToPrimitiveConstantIfPossible((ConstantExpression) value);
                    }
                    if ((type.equals(value.getType()) // GROOVY-10611: integer/decimal value
                                || (isLongCategory(type) && value.getType().equals(ClassHelper.int_TYPE))
                                || (isFloatingCategory(type) && ClassHelper.BigDecimal_TYPE.equals(value.getType())))
                            && (type.equals(ClassHelper.boolean_TYPE) || isStaticConstantInitializerType(type))) {
                        printValue(out, type, value);
                        out.println(';');
                        return;
                    }
                }

                // GROOVY-5150, GROOVY-10902, GROOVY-10928, GROOVY-11019: dummy value that prevents inlining
                if (isPrimitiveType(type) || type.equals(ClassHelper.STRING_TYPE)) {
                    out.print("new " + ClassHelper.getWrapper(type) + "(");
                    printDefaultValue(out, type);
                    out.print(')');
                } else {
                    out.print("null");
                }
            } else if (isPrimitiveType(type)) {
                printDefaultValue(out, type);
            } else {
                out.print("null");
            }
        }
        out.println(';');
    }

    private void printMethods(PrintWriter out, ClassNode classNode, boolean isEnum) {
        if (!isEnum) printConstructors(out, classNode);

        List<MethodNode> methods = new ArrayList<>(propertyMethods.values());
        methods.addAll(classNode.getMethods());
        for (MethodNode method : methods) {
            if (isEnum && method.isSynthetic()) {
                // skip values() method and valueOf(String)
                String name = method.getName();
                Parameter[] params = method.getParameters();
                if (params.length == 0 && name.equals("values")) continue;
                if (params.length == 1 && name.equals("valueOf")
                        && params[0].getType().equals(ClassHelper.STRING_TYPE)) {
                    continue;
                }
            }
            printMethod(out, classNode, method);
        }

        // print the methods from traits
        for (ClassNode trait : findTraits(classNode)) {
            Map<String, ClassNode> generics = trait.isUsingGenerics() ? createGenericsSpec(trait) : null;
            List<MethodNode> traitMethods = trait.getMethods();
            for (MethodNode traitOrigMethod : traitMethods) {
                // GROOVY-9606: replace method return type and parameter type placeholder with resolved type from trait generics
                MethodNode traitMethod = correctToGenericsSpec(generics, traitOrigMethod);
                MethodNode existingMethod = classNode.getMethod(traitMethod.getName(), traitMethod.getParameters());
                if (existingMethod != null) continue;
                for (MethodNode propertyMethod : propertyMethods.values()) {
                    if (propertyMethod.getName().equals(traitMethod.getName())) {
                        boolean sameParams = sameParameterTypes(propertyMethod, traitMethod);
                        if (sameParams) {
                            existingMethod = propertyMethod;
                            break;
                        }
                    }
                }
                if (existingMethod == null && isCandidateTraitMethod(trait, traitMethod)) {
                    printMethod(out, classNode, traitMethod);
                }
            }
        }
    }

    private boolean isCandidateTraitMethod(final ClassNode trait, final MethodNode traitMethod) {
        boolean precompiled = trait.redirect() instanceof DecompiledClassNode;
        if (!precompiled) return !traitMethod.isAbstract();
        List<MethodNode> helperMethods = Traits.findHelper(trait).getMethods();
        for (MethodNode helperMethod : helperMethods) {
            boolean isSynthetic = (traitMethod.getModifiers() & Opcodes.ACC_SYNTHETIC) != 0;
            if (helperMethod.getName().equals(traitMethod.getName()) && !isSynthetic && !traitMethod.getName().contains("$")) {
                Parameter[] origParams = helperMethod.getParameters();
                Parameter[] newParams = Arrays.copyOfRange(origParams, 1, origParams.length);
                if (sameParameterTypes(newParams, traitMethod.getParameters())) return true;
            }
        }
        return false;
    }

    private static boolean sameParameterTypes(final MethodNode firstMethod, final MethodNode secondMethod) {
        return sameParameterTypes(firstMethod.getParameters(), secondMethod.getParameters());
    }

    private static boolean sameParameterTypes(final Parameter[] firstParams, final Parameter[] secondParams) {
        return org.codehaus.groovy.ast.tools.ParameterUtils.parametersEqual(firstParams, secondParams);
    }

    private void printConstructors(final PrintWriter out, final ClassNode classNode) {
        List<ConstructorNode> constructors = new ArrayList<>(this.constructors);
        constructors.addAll(classNode.getDeclaredConstructors());
        for (ConstructorNode constructor : constructors) {
            printConstructor(out, classNode, constructor);
        }
    }

    private void printConstructor(PrintWriter out, ClassNode clazz, ConstructorNode constructorNode) {
        printAnnotations(out, constructorNode);
        // printModifiers(out, constructorNode.getModifiers());

        out.print("public "); // temporary hack
        String className = clazz.getNameWithoutPackage();
        if (clazz instanceof InnerClassNode)
            className = className.substring(className.lastIndexOf('$') + 1);
        out.println(className);

        printParams(out, constructorNode);

        ClassNode[] exceptions = constructorNode.getExceptions();
        printExceptions(out, exceptions);

        ConstructorCallExpression constrCall = getFirstIfSpecialConstructorCall(constructorNode.getCode());
        if (constrCall == null) {
            out.println(" {}");
        } else {
            out.println(" {");
            printSpecialConstructorArgs(out, constructorNode, constrCall);
            out.println("}");
        }
    }

    private static Parameter[] selectAccessibleConstructorFromSuper(final ConstructorNode source) {
        ClassNode superType = source.getDeclaringClass().getUnresolvedSuperClass();
        Map<String, ClassNode> superTypeGenerics = createGenericsSpec(superType);

        Parameter[] bestMatch = null;
        for (ConstructorNode target : superType.getDeclaredConstructors()) {
            // only look at things we can actually call
            // TODO: package-private and types are peers
            if (!target.isPublic() && !target.isProtected()) continue;

            Parameter[] parameters = target.getParameters();
            Parameter[] normalized = Arrays.stream(parameters).map(parameter -> {
                ClassNode normalizedType = parameter.getOriginType();
                if (superType.getGenericsTypes() == null // GROOVY-10407
                        && superType.redirect().getGenericsTypes() != null) {
                    // GROOVY-5859: remove generic type info for raw type
                    normalizedType = normalizedType.getPlainNodeReference();
                } else {
                    // GROOVY-7306: apply type arguments from declaring type to parameter type
                    normalizedType = correctToGenericsSpecRecurse(superTypeGenerics, normalizedType);
                }
                return new Parameter(normalizedType, parameter.getName());
            }).toArray(Parameter[]::new);

            if (noExceptionToAvoid(source, target)) return normalized;
            if (bestMatch == null) bestMatch = normalized;
        }
        if (bestMatch != null) return bestMatch;

        // fall back for parameterless constructor
        if (superType.isPrimaryClassNode()) {
            return Parameter.EMPTY_ARRAY;
        }

        return null;
    }

    private static boolean noExceptionToAvoid(ConstructorNode fromStub, ConstructorNode fromSuper) {
        ClassNode[] superExceptions = fromSuper.getExceptions();
        if (superExceptions==null || superExceptions.length==0) return true;

        ClassNode[] stubExceptions = fromStub.getExceptions();
        if (stubExceptions==null || stubExceptions.length==0) return false;


        // if all remaining exceptions are used in the stub we are good
        outer:
        for (ClassNode superExc : superExceptions) {
            for (ClassNode stub : stubExceptions) {
                if (stub.isDerivedFrom(superExc)) continue outer;
            }
            // not found
            return false;
        }

        return true;
    }

    private void printSpecialConstructorArgs(final PrintWriter out, final ConstructorNode ctor, final ConstructorCallExpression ctorCall) {
        // Select a constructor from our class, or super-class which is legal to call,
        // then write out an invoke w/nulls using casts to avoid ambiguous calls
        Parameter[] params = selectAccessibleConstructorFromSuper(ctor);
        if (params != null) {
            out.print("super (");
            for (int i = 0, n = params.length; i < n; i += 1) {
                printDefaultValue(out, params[i].getType());
                if (i + 1 < n) {
                    out.print(", ");
                }
            }
            out.println(");");
            return;
        }

        // Otherwise try the older method based on the constructor's call expression
        Expression arguments = ctorCall.getArguments();
        if (ctorCall.isSuperCall()) {
            out.print("super(");
        } else {
            out.print("this(");
        }
        if (arguments instanceof ArgumentListExpression) {
            List<Expression> args = ((ArgumentListExpression) arguments).getExpressions();
            int i = 0, n = args.size();
            for (Expression arg : args) {
                if (arg instanceof ConstantExpression) {
                    Object value = ((ConstantExpression) arg).getValue();
                    if (value instanceof String) {
                        out.print("(String)null");
                    } else {
                        out.print(arg.getText());
                    }
                } else {
                    printDefaultValue(out, getConstructorArgumentType(arg, ctor));
                }
                if (++i < n) {
                    out.print(", ");
                }
            }
        }
        out.println(");");
    }

    private static ClassNode getConstructorArgumentType(final Expression arg, final ConstructorNode ctor) {
        if (arg instanceof VariableExpression) {
            Variable variable = ((VariableExpression) arg).getAccessedVariable();
            if (variable instanceof DynamicVariable) { // GROOVY-10464
                return ClassHelper.CLASS_Type.getPlainNodeReference();
            }
            return variable.getType(); // field, property, parameter
        }
        if (arg instanceof PropertyExpression) {
            if ("class".equals(((PropertyExpression) arg).getPropertyAsString())) {
                return ClassHelper.CLASS_Type.getPlainNodeReference();
            }
            return null;
        }
        if (arg instanceof MethodCallExpression) { // GROOVY-10122
            MethodCallExpression mce = (MethodCallExpression) arg;
            if (ExpressionUtils.isThisExpression(mce.getObjectExpression())) {
                MethodNode mn = ctor.getDeclaringClass().tryFindPossibleMethod(mce.getMethodAsString(), mce.getArguments());
                if (mn != null) return mn.getReturnType();
            }
            return null;
        }
        return arg.getType();
    }

    private void printMethod(final PrintWriter out, final ClassNode classNode, final MethodNode methodNode) {
        if (methodNode.isStaticConstructor()) return;
        if (methodNode.isPrivate() || !Utilities.isJavaIdentifier(methodNode.getName())) return;
        if (methodNode.isSynthetic() && methodNode.getName().equals("$getStaticMetaClass")) return;

        printAnnotations(out, methodNode);
        if (!isInterfaceOrTrait(classNode)) {
            int modifiers = methodNode.getModifiers();
            if (isDefaultTraitImpl(methodNode)) {
                modifiers ^= Opcodes.ACC_ABSTRACT;
            }
            printModifiers(out, modifiers & ~(classNode.isEnum() ? Opcodes.ACC_ABSTRACT : 0));
        }

        printGenericsBounds(out, methodNode.getGenericsTypes());
        out.print(" ");
        printType(out, methodNode.getReturnType());
        out.print(" ");
        out.print(methodNode.getName());

        printParams(out, methodNode);

        ClassNode[] exceptions = methodNode.getExceptions();
        printExceptions(out, exceptions);

        if (Traits.isTrait(classNode)) {
            out.println(";");
        } else if (isAbstract(methodNode) && !classNode.isEnum()) {
            if (classNode.isAnnotationDefinition() && methodNode.hasAnnotationDefault()) {
                Statement fs = methodNode.getFirstStatement();
                if (fs instanceof ExpressionStatement) {
                    ExpressionStatement es = (ExpressionStatement) fs;
                    Expression re = es.getExpression();
                    ClassNode rt = methodNode.getReturnType();
                    Consumer<Expression> valuePrinter = (value) -> {
                        if (rt.equals(ClassHelper.CLASS_Type) || (rt.isArray()
                            && rt.getComponentType().equals(ClassHelper.CLASS_Type))) {
                            if (value.getType().getName().equals("groovy.lang.Closure")) {
                                out.print("groovy.lang.Closure.class");
                                return;
                            }
                            String valueText = value.getText();
                            out.print(valueText);
                            if (!valueText.endsWith(".class")) {
                                out.print(".class");
                            }
                        } else if (value instanceof ConstantExpression) {
                            printValue(out, rt, value);
                        } else {
                            out.print(value.getText());
                        }
                    };
                    out.print(" default ");
                    if (re instanceof ListExpression) {
                        out.print("{ ");
                        ListExpression le = (ListExpression) re;
                        boolean first = true;
                        for (Expression expression : le.getExpressions()) {
                            if (first) first = false; else out.print(", ");
                            valuePrinter.accept(expression);
                        }
                        out.print(" }");
                    } else {
                        valuePrinter.accept(re);
                    }
                }
            }
            out.println(";");
        } else {
            out.print(" { ");
            if (!methodNode.isVoidMethod()) {
                out.print("return ");
                printDefaultValue(out, methodNode.getReturnType());
                out.print(";");
            }
            out.println("}");
        }
    }

    private void printExceptions(PrintWriter out, ClassNode[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            ClassNode exception = exceptions[i];
            if (i == 0) {
                out.print("throws ");
            } else {
                out.print(", ");
            }
            printType(out, exception);
        }
    }

    private static boolean isAbstract(final MethodNode methodNode) {
        if (isDefaultTraitImpl(methodNode)) {
            return false;
        }
        return (methodNode.getModifiers() & Opcodes.ACC_ABSTRACT) != 0;
    }

    private static boolean isDefaultTraitImpl(final MethodNode methodNode) {
        return Traits.isTrait(methodNode.getDeclaringClass()) && Traits.hasDefaultImplementation(methodNode);
    }

    private void printValue(final PrintWriter out, final ClassNode type, final Expression value) {
        ClassNode valueType = ClassHelper.getUnwrapper(value.getType());
        if (valueType == ClassHelper.char_TYPE) {
            out.print("'");
            out.print(escapeSpecialChars(value.getText().substring(0, 1)));
            out.print("'");
        } else if (valueType.equals(ClassHelper.STRING_TYPE)) {
            out.print('"');
            out.print(escapeSpecialChars(value.getText()));
            out.print('"');
        } else if (valueType == ClassHelper.double_TYPE) {
            out.print(value.getText());
            out.print('d');
        } else if (valueType == ClassHelper.float_TYPE) {
            out.print(value.getText());
            out.print('f');
        } else if (valueType == ClassHelper.long_TYPE) {
            out.print(value.getText());
            out.print('L');
        } else {
            if (valueType != ClassHelper.int_TYPE && valueType != ClassHelper.boolean_TYPE && !valueType.equals(ClassHelper.BigDecimal_TYPE)) {
                out.print('(');
                printType(out, type);
                out.print(')');
            }
            out.print(value.getText());
        }
    }

    private void printDefaultValue(final PrintWriter out, final ClassNode type) {
        if (type != null && !type.equals(ClassHelper.boolean_TYPE)) {
            out.print('(');
            printType(out, type);
            out.print(')');
        }
        if (type != null && isPrimitiveType(type)) {
            if (type.equals(ClassHelper.boolean_TYPE)) {
                out.print("false");
            } else {
                out.print("0");
            }
        } else {
            out.print("null");
        }
    }

    private void printType(PrintWriter out, ClassNode type) {
        if (type.isArray()) {
            printType(out, type.getComponentType());
            out.print("[]");
        } else if (java5 && type.isGenericsPlaceHolder()) {
            out.print(type.getUnresolvedName());
        } else {
            printGenericsBounds(out, type, false);
        }
    }

    private void printTypeName(PrintWriter out, ClassNode type) {
        if (isPrimitiveType(type)) {
            if (type.equals(ClassHelper.boolean_TYPE)) {
                out.print("boolean");
            } else if (type.equals(ClassHelper.byte_TYPE)) {
                out.print("byte");
            } else if (type.equals(ClassHelper.char_TYPE)) {
                out.print("char");
            } else if (type.equals(ClassHelper.int_TYPE)) {
                out.print("int");
            } else if (type.equals(ClassHelper.long_TYPE)) {
                out.print("long");
            } else if (type.equals(ClassHelper.short_TYPE)) {
                out.print("short");
            } else if (type.equals(ClassHelper.float_TYPE)) {
                out.print("float");
            } else if (type.equals(ClassHelper.double_TYPE)) {
                out.print("double");
            } else {
                out.print("void");
            }
        } else {
            String name = type.getName();
            // check for an alias
            ClassNode alias = currentModule.getImportType(name);
            if (alias != null) name = alias.getName();
            out.print(name.replace('$', '.'));
        }
    }

    private void printGenericsBounds(PrintWriter out, ClassNode type, boolean skipName) {
        if (!skipName) printTypeName(out, type);
        if (java5 && !ClassHelper.isCachedType(type)) {
            printGenericsBounds(out, type.getGenericsTypes());
        }
    }

    private static void printGenericsBounds(PrintWriter out, GenericsType[] genericsTypes) {
        if (genericsTypes == null || genericsTypes.length == 0) return;
        out.print('<');
        for (int i = 0; i < genericsTypes.length; i++) {
            if (i != 0) out.print(", ");
            out.print(genericsTypes[i].toString().replace("$","."));
        }
        out.print('>');
    }

    private void printParams(PrintWriter out, MethodNode methodNode) {
        out.print("(");
        Parameter[] parameters = methodNode.getParameters();
        if (parameters != null && parameters.length != 0) {
            int lastIndex = parameters.length - 1;
            boolean vararg = parameters[lastIndex].getType().isArray();
            for (int i = 0; i != parameters.length; ++i) {
                printAnnotations(out, parameters[i]);
                if (i == lastIndex && vararg) {
                    printType(out, parameters[i].getType().getComponentType());
                    out.print("...");
                } else {
                    printType(out, parameters[i].getType());
                }
                out.print(" ");
                out.print(parameters[i].getName());
                if (i + 1 < parameters.length) {
                    out.print(", ");
                }
            }
        }
        out.print(")");
    }

    private void printAnnotations(PrintWriter out, AnnotatedNode annotated) {
        if (!java5) return;
        for (AnnotationNode annotation : annotated.getAnnotations()) {
            if (!annotation.getClassNode().equals(PACKAGE_SCOPE_TYPE))
                printAnnotation(out, annotation);
        }
    }

    private void printAnnotation(PrintWriter out, AnnotationNode annotation) {
        out.print("@" + annotation.getClassNode().getName().replace('$', '.') + "(");
        boolean first = true;
        Map<String, Expression> members = annotation.getMembers();
        for (Map.Entry<String, Expression> entry : members.entrySet()) {
            String key = entry.getKey();
            if (first) first = false;
            else out.print(", ");
            out.print(key + "=" + getAnnotationValue(entry.getValue()));
        }
        out.print(") ");
    }

    private String getAnnotationValue(Object memberValue) {
        String val = "null";
        boolean replaceDollars = true;
        if (memberValue instanceof ListExpression) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            ListExpression le = (ListExpression) memberValue;
            for (Expression e : le.getExpressions()) {
                if (first) first = false;
                else sb.append(",");
                sb.append(getAnnotationValue(e));
            }
            sb.append("}");
            val = sb.toString();
        } else if (memberValue instanceof ConstantExpression) {
            ConstantExpression ce = (ConstantExpression) memberValue;
            Object constValue = ce.getValue();
            if (constValue instanceof AnnotationNode) {
                Writer writer = new StringBuilderWriter();
                PrintWriter out = new PrintWriter(writer);
                printAnnotation(out, (AnnotationNode) constValue);
                val = writer.toString();
            } else if (constValue instanceof Number || constValue instanceof Boolean) {
                val = constValue.toString();
            } else {
                val = "\"" + escapeSpecialChars(constValue.toString()) + "\"";
                replaceDollars = false;
            }
        } else if (memberValue instanceof PropertyExpression) {
            // assume must be static class field or enum value or class that Java can resolve
            val = ((Expression) memberValue).getText();
        } else if (memberValue instanceof VariableExpression) {
            val = ((Expression) memberValue).getText();
            // check for an alias
            ImportNode alias = currentModule.getStaticImports().get(val);
            if (alias != null)
                val = alias.getClassName() + "." + alias.getFieldName();
        } else if (memberValue instanceof ClosureExpression) {
            // annotation closure; replaced with this specific class literal to cover the
            // case where annotation type uses Class<? extends Closure> for the closure's type
            val = "groovy.lang.Closure.class";
        } else if (memberValue instanceof ClassExpression) {
            val = ((Expression) memberValue).getText() + ".class";
        }
        return replaceDollars ? val.replace('$', '.') : val;
    }

    private static void printModifiers(PrintWriter out, int modifiers) {
        if ((modifiers & Opcodes.ACC_PUBLIC) != 0)
            out.print("public ");

        if ((modifiers & Opcodes.ACC_PROTECTED) != 0)
            out.print("protected ");

        if ((modifiers & Opcodes.ACC_PRIVATE) != 0)
            out.print("private ");

        if ((modifiers & Opcodes.ACC_STATIC) != 0)
            out.print("static ");

        if ((modifiers & Opcodes.ACC_SYNCHRONIZED) != 0)
            out.print("synchronized ");

        if ((modifiers & Opcodes.ACC_FINAL) != 0)
            out.print("final ");

        if ((modifiers & Opcodes.ACC_ABSTRACT) != 0)
            out.print("abstract ");
    }

    private static void printImports(PrintWriter out, ClassNode classNode) {
        List<String> imports = new ArrayList<String>();

        ModuleNode moduleNode = classNode.getModule();
        for (ImportNode importNode : moduleNode.getStarImports()) {
            imports.add(importNode.getPackageName());
        }

        for (ImportNode imp : moduleNode.getImports()) {
            if (imp.getAlias() == null)
                imports.add(imp.getType().getName());
        }

        imports.addAll(Arrays.asList(ResolveVisitor.DEFAULT_IMPORTS));

        for (Map.Entry<String, ImportNode> entry : moduleNode.getStaticImports().entrySet()) {
            if (entry.getKey().equals(entry.getValue().getFieldName()))
                imports.add("static "+entry.getValue().getType().getName()+"."+entry.getKey());
        }

        for (Map.Entry<String, ImportNode> entry : moduleNode.getStaticStarImports().entrySet()) {
            imports.add("static "+entry.getValue().getType().getName()+".");
        }

        for (String imp : imports) {
            String s = ("import " +
                    imp +
                    ((imp.charAt(imp.length() - 1) == '.') ? "*;" : ";"))
                    .replace('$', '.');
            out.println(s);
        }
        out.println();
    }

    private File createJavaStubFile(String path) {
        return new File(outputPath, path + ".java");
    }

    private static String escapeSpecialChars(String value) {
        return InvokerHelper.escapeBackslashes(value).replace("\"", "\\\"");
    }

    private static boolean isInterfaceOrTrait(final ClassNode cn) {
        return cn.isInterface() || Traits.isTrait(cn);
    }

    private boolean hasPackageScopeXform(final AnnotatedNode node, final PackageScopeTarget type) {
        boolean member = (!(node instanceof ClassNode) && type != PackageScopeTarget.CLASS);
        for (AnnotationNode anno : node.getAnnotations()) {
            if (anno.getClassNode().equals(PACKAGE_SCOPE_TYPE)) {
                Expression expr = anno.getMember("value");
                if (expr == null) {
                    // if empty @PackageScope, node type and target type must be in alignment
                    return member || (node instanceof ClassNode && type == PackageScopeTarget.CLASS);
                }

                final boolean[] val = new boolean[1];
                expr.visit(new CodeVisitorSupport() {
                    @Override
                    public void visitPropertyExpression(final PropertyExpression property) {
                        if (property.getObjectExpression().getText().equals("groovy.transform.PackageScopeTarget")
                                && property.getPropertyAsString().equals(type.name())) {
                            val[0] = true;
                        }
                    }
                    @Override
                    public void visitVariableExpression(final VariableExpression variable) {
                        if (variable.getName().equals(type.name())) {
                            ImportNode imp = currentModule.getStaticImports().get(type.name());
                            if (imp != null && imp.getType().getName().equals("groovy.transform.PackageScopeTarget")) {
                                val[0] = true;
                            } else if (imp == null && currentModule.getStaticStarImports().get("groovy.transform.PackageScopeTarget") != null) {
                                val[0] = true;
                            }
                        }
                    }
                });
                return val[0];
            }
        }
        if (member) { // check for @PackageScope(XXX) on class
            return hasPackageScopeXform(node.getDeclaringClass(), type);
        }
        return false;
    }

    //--------------------------------------------------------------------------

    private final Set<JavaFileObject> javaStubCompilationUnitSet = new HashSet<>();

    public Set<JavaFileObject> getJavaStubCompilationUnitSet() {
        return javaStubCompilationUnitSet;
    }

    public void clean() {
        Stream<JavaFileObject> javaFileObjectStream =
                javaStubCompilationUnitSet.size() < 2
                        ? javaStubCompilationUnitSet.stream()
                        : javaStubCompilationUnitSet.parallelStream();

        javaFileObjectStream.forEach(FileObject::delete);
        javaStubCompilationUnitSet.clear();
    }
}
