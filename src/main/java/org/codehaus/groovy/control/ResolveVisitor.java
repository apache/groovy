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
package org.codehaus.groovy.control;

import groovy.lang.Tuple2;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.runtime.memoize.UnlimitedConcurrentCache;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.trait.Traits;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static groovy.lang.Tuple.tuple;
import static org.apache.groovy.ast.tools.ExpressionUtils.transformInlineConstants;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;

/**
 * Visitor to resolve types and convert VariableExpression to
 * ClassExpressions if needed. The ResolveVisitor will try to
 * find the Class for a ClassExpression and prints an error if
 * it fails to do so. Constructions like C[], foo as C, (C) foo
 * will force creation of a ClassExpression for C
 * <p>
 * Note: the method to start the resolving is {@link #startResolving(ClassNode,SourceUnit)}.
 */
public class ResolveVisitor extends ClassCodeExpressionTransformer {
    // note: BigInteger and BigDecimal are also imported by default
    // `java.util` is used much frequently than other two java packages(`java.io` and `java.net`), so place java.util before the two packages
    public static final String[] DEFAULT_IMPORTS = {"java.lang.", "java.util.", "java.io.", "java.net.", "groovy.lang.", "groovy.util."};
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final String QUESTION_MARK = "?";

    private final CompilationUnit compilationUnit;
    private ClassNodeResolver classNodeResolver;
    private SourceUnit source;
    private ClassNode  currentClass;
    private ImportNode currentImport;
    private MethodNode currentMethod;
    private VariableScope currentScope;

    private Map<GenericsTypeName, GenericsType> genericParameterNames = new HashMap<>();
    private final Set<FieldNode> fieldTypesChecked = new HashSet<>();
    private boolean checkingVariableTypeInDeclaration;
    private boolean inClosure, inPropertyExpression;
    private boolean isTopLevelProperty = true;
    /*package*/ int phase; // sub-divide visit

    /**
     * A ConstructedNestedClass consists of an outer class and a name part, denoting a
     * nested class with an unknown number of levels down. This allows resolve tests to
     * skip this node for further inner class searches and combinations with imports, since
     * the outer class we know is already resolved.
     */
    private static class ConstructedNestedClass extends ClassNode {
        final ClassNode knownEnclosingType;

        public ConstructedNestedClass(final ClassNode outer, final String inner) {
            super(outer.getName() + "$" + inner.replace('.', '$'), Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
            this.knownEnclosingType = outer;
            this.isPrimaryNode = false;
        }

        @Override
        public String getUnresolvedName() {
            // outer class (aka knownEnclosingType) may have aliased name that should be reflected here too
            return super.getUnresolvedName().replace(knownEnclosingType.getName(), knownEnclosingType.getUnresolvedName());
        }

        @Override
        public boolean hasPackageName() {
            if (redirect() != this) return super.hasPackageName();
            return knownEnclosingType.hasPackageName();
        }

        @Override
        public String setName(final String name) {
            if (redirect() != this) {
                return super.setName(name);
            } else {
                throw new GroovyBugError("ConstructedNestedClass#setName should not be called");
            }
        }
    }

    /**
     * we use ConstructedClassWithPackage to limit the resolving the compiler
     * does when combining package names and class names. The idea
     * that if we use a package, then we do not want to replace the
     * '.' with a '$' for the package part, only for the class name
     * part. There is also the case of an imported class, so this logic
     * can't be done in these cases...
     */
    private static class ConstructedClassWithPackage extends ClassNode {
        final String prefix;
        String className;

        public ConstructedClassWithPackage(final String pkg, final String name) {
            super(pkg + name, Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
            isPrimaryNode = false;
            this.prefix = pkg;
            this.className = name;
        }

        @Override
        public String getName() {
            if (redirect() != this)
                return super.getName();
            return prefix + className;
        }

        @Override
        public boolean hasPackageName() {
            if (redirect() != this)
                return super.hasPackageName();
            return className.indexOf('.') != -1;
        }

        @Override
        public String setName(final String name) {
            if (redirect() != this) {
                return super.setName(name);
            } else {
                throw new GroovyBugError("ConstructedClassWithPackage#setName should not be called");
            }
        }
    }

    /**
     * we use LowerCaseClass to limit the resolving the compiler
     * does for vanilla names starting with a lower case letter. The idea
     * that if we use a vanilla name with a lower case letter, that this
     * is in most cases no class. If it is a class the class needs to be
     * imported explicitly. The effect is that in an expression like
     * "def foo = bar" we do not have to use a loadClass call to check the
     * name foo and bar for being classes. Instead we will ask the module
     * for an alias for this name which is much faster.
     */
    private static class LowerCaseClass extends ClassNode {
        final String className;

        public LowerCaseClass(final String name) {
            super(name, Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
            isPrimaryNode = false;
            this.className = name;
        }

        @Override
        public String getName() {
            if (redirect() != this)
                return super.getName();
            return className;
        }

        @Override
        public boolean hasPackageName() {
            if (redirect() != this)
                return super.hasPackageName();
            return false;
        }

        @Override
        public String setName(final String name) {
            if (redirect() != this) {
                return super.setName(name);
            } else {
                throw new GroovyBugError("LowerCaseClass#setName should not be called");
            }
        }
    }

    @SuppressWarnings("serial")
    static class Interrupt extends CompilationFailedException {
        private Interrupt(final ProcessingUnit unit) {
            super(Phases.SEMANTIC_ANALYSIS, unit);
        }
    }

    //--------------------------------------------------------------------------

    public ResolveVisitor(final CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public void setClassNodeResolver(final ClassNodeResolver classNodeResolver) {
        this.classNodeResolver = classNodeResolver;
    }

    public void startResolving(final ClassNode node, final SourceUnit source) {
        this.source = source;
        visitClass(node);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        VariableScope oldScope = currentScope;
        currentScope = node.getVariableScope();
        Map<GenericsTypeName, GenericsType> oldNames = genericParameterNames;
        genericParameterNames = node.isStatic() && !Traits.isTrait(node.getDeclaringClass())
                ? new HashMap<>() : new HashMap<>(genericParameterNames);

        resolveGenericsHeader(node.getGenericsTypes());

        for (Parameter p : node.getParameters()) {
            p.setInitialExpression(transform(p.getInitialExpression()));
            resolveOrFail(p.getType(), p.getType());
            visitAnnotations(p);
        }
        resolveOrFail(node.getReturnType(), node);
        if (node.getExceptions() != null) {
            for (ClassNode t : node.getExceptions()) {
                resolveOrFail(t, node);
            }
        }

        MethodNode oldCurrentMethod = currentMethod;
        currentMethod = node;
        super.visitConstructorOrMethod(node, isConstructor);

        currentMethod = oldCurrentMethod;
        genericParameterNames = oldNames;
        currentScope = oldScope;
    }

    @Override
    public void visitField(final FieldNode node) {
        ClassNode t = node.getType();
        if (!fieldTypesChecked.contains(node)) {
            resolveOrFail(t, node);
        }
        super.visitField(node);
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        Map<GenericsTypeName, GenericsType> oldPNames = genericParameterNames;
        if (node.isStatic() && !Traits.isTrait(node.getDeclaringClass())) {
            genericParameterNames = new HashMap<>();
        }

        ClassNode t = node.getType();
        resolveOrFail(t, node);
        super.visitProperty(node);
        fieldTypesChecked.add(node.getField());

        genericParameterNames = oldPNames;
    }

    private void resolveOrFail(final ClassNode type, final ASTNode node) {
        resolveOrFail(type, "", node);
    }

    private void resolveOrFail(final ClassNode type, final String msg, final ASTNode node) {
        resolveOrFail(type, msg, node, false);
    }

    private void resolveOrFail(final ClassNode type, final String msg, final ASTNode node, final boolean preferImports) {
        if (type.isRedirectNode() || !type.isPrimaryClassNode()) {
            visitTypeAnnotations(type); // JSR 308 support
        }
        if (preferImports && !type.isResolved() && !type.isPrimaryClassNode()) {
            resolveGenericsTypes(type.getGenericsTypes());
            if (resolveAliasFromModule(type)) return;
        }
        if (resolve(type)) return;
        if (resolveToInner(type)) return;

        addError("unable to resolve class " + type.toString(false) + msg, node);
    }

    protected boolean resolveToInner(final ClassNode type) {
        // we do not do our name mangling to find an inner class
        // if the type is a ConstructedClassWithPackage, because in this case we
        // are resolving the name at a different place already
        if (type instanceof ConstructedClassWithPackage) return false;
        if (type instanceof ConstructedNestedClass) return false;

        // GROOVY-8715
        ClassNode t = type;
        while (t.isArray()) {
            t = t.getComponentType();
        }

        String name = t.getName(), temp = name;
        while (temp.lastIndexOf('.') != -1) {
            temp = replaceLastPointWithDollar(temp);
            t.setName(temp);
            if (resolve(t, true, false, false)) {
                return true;
            }
        }
        t.setName(name);
        return false;
    }

    protected boolean resolve(final ClassNode type) {
        return resolve(type, true, true, true);
    }

    protected boolean resolve(final ClassNode type, final boolean testModuleImports, final boolean testDefaultImports, final boolean testStaticInnerClasses) {
        GenericsType[] genericsTypes = type.getGenericsTypes();
        resolveGenericsTypes(genericsTypes);

        if (type.isPrimaryClassNode()) return true;
        if (type.isArray()) {
            ClassNode element = type.getComponentType();
            boolean resolved = resolve(element, testModuleImports, testDefaultImports, testStaticInnerClasses);
            if (resolved) {
                ClassNode cn = element.makeArray();
                type.setRedirect(cn);
            }
            return resolved;
        }
        if (type.isResolved()) return true;

        String typeName = type.getName();

        GenericsType typeParameter = genericParameterNames.get(new GenericsTypeName(typeName));
        if (typeParameter != null) {
            type.setDeclaringClass(typeParameter.getType().getDeclaringClass());
            type.setGenericsTypes(new GenericsType[]{typeParameter});
            type.setRedirect(typeParameter.getType());
            type.setGenericsPlaceHolder(true);
            return true;
        }

        boolean resolved;
        if (currentClass.getNameWithoutPackage().equals(typeName)) {
            type.setRedirect(currentClass);
            resolved = true;
        } else {
            resolved = (!type.hasPackageName() && resolveNestedClass(type))
                    || resolveFromModule(type, testModuleImports)
                    || resolveFromCompileUnit(type)
                    || (testDefaultImports && !type.hasPackageName() && resolveFromDefaultImports(type))
                    || resolveToOuter(type)
                    || (testStaticInnerClasses && type.hasPackageName() && resolveFromStaticInnerClasses(type));
        }
        // GROOVY-10153: handle "C<? super T>"
        if (resolved && genericsTypes != null) {
            resolveWildcardBounding(genericsTypes, type);
        }
        return resolved;
    }

    protected boolean resolveNestedClass(final ClassNode type) {
        if (type instanceof ConstructedNestedClass || type instanceof ConstructedClassWithPackage) return false;

        ClassNode cn = currentClass; Set<ClassNode> cycleCheck = new HashSet<>();
        // GROOVY-4043: for type "X", try "A$X" with each type in the class hierarchy (except for Object)
        for (; cn != null && cycleCheck.add(cn) && !ClassHelper.isObjectType(cn); cn = cn.getSuperClass()) {
            if (setRedirect(type, cn)) return true;
        }

        // Another case we want to check here is if we are in a
        // nested class A$B$C and want to access B without
        // qualifying it by A.B. A alone will work, since that
        // is the qualified (minus package) name of that class
        // anyway.

        List<ClassNode> outerClasses = currentClass.getOuterClasses();
        if (!outerClasses.isEmpty()) {
            // Since we have B and want to get A we start with the most
            // outer class, put them together and then see if that does
            // already exist. In case of B from within A$B we are done
            // after the first step already. In case of for example
            // A.B.C.D.E.F and accessing E from F we test A$E=failed,
            // A$B$E=failed, A$B$C$E=fail, A$B$C$D$E=success.

            for (ListIterator<ClassNode> it = outerClasses.listIterator(outerClasses.size()); it.hasPrevious(); ) {
                cn = it.previous();
                if (setRedirect(type, cn)) return true;
            }
        }

        return false;
    }

    private boolean setRedirect(final ClassNode type, final ClassNode classToCheck) {
        String typeName = type.getName();

        Predicate<ClassNode> resolver = (ClassNode maybeOuter) -> {
            if (!typeName.equals(maybeOuter.getName())) {
                ClassNode maybeNested = new ConstructedNestedClass(maybeOuter, typeName);
                if (resolveFromCompileUnit(maybeNested) || resolveToOuter(maybeNested)) {
                    type.setRedirect(maybeNested);
                    return true;
                }
            }
            return false;
        };

        if (resolver.test(classToCheck)) {
            if (currentClass != classToCheck && !currentClass.getOuterClasses().contains(classToCheck) && !isVisibleNestedClass(type.redirect(), currentClass)) {
                type.setRedirect(null);
            } else {
                return true;
            }
        }
        if (classToCheck.getInterfaces().length > 0) {
            for (ClassNode face : classToCheck.getAllInterfaces()) {
                if (resolver.test(face)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String replaceLastPointWithDollar(final String name) {
        int lastPointIndex = name.lastIndexOf('.');

        return name.substring(0, lastPointIndex) + "$" + name.substring(lastPointIndex + 1);
    }

    protected boolean resolveFromStaticInnerClasses(final ClassNode type) {
        // a class consisting of a vanilla name can never be
        // a static inner class, because at least one dot is
        // required for this. Example: foo.bar -> foo$bar
        if (!(type instanceof LowerCaseClass || type instanceof ConstructedNestedClass)) {
            if (type instanceof ConstructedClassWithPackage) {
                // we replace '.' only in the className part
                // with '$' to find an inner class. The case that
                // the package is really a class is handled elsewhere
                ConstructedClassWithPackage tmp = (ConstructedClassWithPackage) type;
                String savedName = tmp.className;
                tmp.className = replaceLastPointWithDollar(savedName);
                if (resolve(tmp, false, true, true)) {
                    type.setRedirect(tmp.redirect());
                    return true;
                }
                tmp.className = savedName;
            } else {
                String savedName = type.getName();
                type.setName(replaceLastPointWithDollar(savedName));
                if (resolve(type, false, true, true)) return true;
                type.setName(savedName);
            }
        }
        return false;
    }

    protected boolean resolveFromDefaultImports(final ClassNode type) {
        // we do not resolve a vanilla name starting with a lower case letter
        // try to resolve against a default import, because we know that the
        // default packages do not contain classes like these
        if (!(type instanceof LowerCaseClass)) {
            String typeName = type.getName();

            Set<String> packagePrefixSet = DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.get(typeName);
            if (packagePrefixSet != null) {
                // if the type name was resolved before, we can try the successfully resolved packages first, which are much less and very likely successful to resolve.
                // As a result, we can avoid trying other default import packages and further resolving, which can improve the resolving performance to some extent.
                if (resolveFromDefaultImports(type, packagePrefixSet.toArray(EMPTY_STRING_ARRAY))) {
                    return true;
                }
            }

            if (resolveFromDefaultImports(type, DEFAULT_IMPORTS)) {
                return true;
            }
            if ("BigInteger".equals(typeName)) {
                type.setRedirect(ClassHelper.BigInteger_TYPE);
                return true;
            }
            if ("BigDecimal".equals(typeName)) {
                type.setRedirect(ClassHelper.BigDecimal_TYPE);
                return true;
            }
        }
        return false;
    }

    private static final Map<String, Set<String>> DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE = new UnlimitedConcurrentCache<>();
    static {
        DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.putAll(VMPluginFactory.getPlugin().getDefaultImportClasses(DEFAULT_IMPORTS));
    }

    protected boolean resolveFromDefaultImports(final ClassNode type, final String[] packagePrefixes) {
        String typeName = type.getName();

        for (String packagePrefix : packagePrefixes) {
            // We limit the inner class lookups here by using ConstructedClassWithPackage.
            // This way only the name will change, the packagePrefix will
            // not be included in the lookup. The case where the
            // packagePrefix is really a class is handled elsewhere.
            // WARNING: This code does not expect a class that has a static
            //          inner class in DEFAULT_IMPORTS
            ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(packagePrefix, typeName);
            if (resolve(tmp, false, false, false)) {
                type.setRedirect(tmp.redirect());

                if (DEFAULT_IMPORTS == packagePrefixes) { // Only the non-cached type and packages should be cached
                    Set<String> packagePrefixSet = DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.computeIfAbsent(typeName, key -> new HashSet<>(2));
                    packagePrefixSet.add(packagePrefix);
                }

                return true;
            }
        }

        return false;
    }

    protected boolean resolveFromCompileUnit(final ClassNode type) {
        // look into the compile unit if there is a class with that name
        CompileUnit compileUnit = currentClass.getCompileUnit();
        if (compileUnit == null) return false;
        ClassNode cuClass = compileUnit.getClass(type.getName());
        if (cuClass != null) {
            if (type != cuClass) type.setRedirect(cuClass);
            return true;
        }
        return false;
    }

    private void ambiguousClass(final ClassNode type, final ClassNode iType, final String name) {
        if (type.getName().equals(iType.getName())) {
            addError("reference to " + name + " is ambiguous, both class " + type.getName() + " and " + iType.getName() + " match", type);
        } else {
            type.setRedirect(iType);
        }
    }

    private boolean resolveAliasFromModule(final ClassNode type) {
        // In case of getting a ConstructedClassWithPackage here we do not check
        // for partial matches with imported classes. ConstructedClassWithPackage
        // is already a constructed node and subclass resolving takes place elsewhere.
        if (type instanceof ConstructedClassWithPackage) return false;

        ModuleNode module = currentClass.getModule();
        if (module == null) return false;
        String name = type.getName();

        // check module node imports aliases
        // the while loop enables a check for inner classes which are not fully imported,
        // but visible as the surrounding class is imported and the inner class is public/protected static
        String pname = name;
        int index = name.length();
        /*
         * we have a name foo.bar and an import foo.foo. This means foo.bar is possibly
         * foo.foo.bar rather than foo.bar. This means to cut at the dot in foo.bar and
         * foo for import
         */
        do {
            pname = name.substring(0, index);
            ClassNode aliasedNode = null;
            ImportNode importNode = module.getImport(pname);
            if (importNode != null && importNode != currentImport) {
                aliasedNode = importNode.getType();
            }
            if (aliasedNode == null) {
                importNode = module.getStaticImports().get(pname);
                if (importNode != null && importNode != currentImport) {
                    // static alias only for inner classes and must be at end of chain
                    ClassNode tmp = new ConstructedNestedClass(importNode.getType(), importNode.getFieldName());
                    if (resolve(tmp, false, false, true) && Modifier.isStatic(tmp.getModifiers())) {
                        type.setRedirect(tmp.redirect());
                        return true;
                    }
                }
            }

            if (aliasedNode != null) {
                if (pname.length() == name.length()) {
                    // full match

                    // We can compare here by length, because pname is always
                    // a substring of name, so same length means they are equal.
                    type.setRedirect(aliasedNode);
                    return true;
                } else {
                    //partial match

                    // At this point we know that we have a match for pname. This may
                    // mean, that name[pname.length()..<-1] is a static inner class.
                    // For this the rest of the name does not need any dots in its name.
                    // It is either completely an inner static class or it is not.
                    // Since we do not want to have useless lookups we create the name
                    // completely and use a ConstructedClassWithPackage to prevent lookups against the package.
                    String className = aliasedNode.getNameWithoutPackage() + "$" + name.substring(pname.length() + 1).replace('.', '$');
                    ConstructedClassWithPackage tmp = new ConstructedClassWithPackage(aliasedNode.getPackageName() + ".", className);
                    if (resolve(tmp, true, true, false)) {
                        type.setRedirect(tmp.redirect());
                        return true;
                    }
                }
            }
            index = pname.lastIndexOf('.');
        } while (index != -1);
        return false;
    }

    protected boolean resolveFromModule(final ClassNode type, final boolean testModuleImports) {
        ModuleNode module = currentClass.getModule();
        if (module == null) return false;

        if (type instanceof ConstructedNestedClass) return false;
        // We decided if we have a vanilla name starting with a lower case
        // letter that we will not try to resolve this name against .*
        // imports. Instead a full import is needed for these.
        // resolveAliasFromModule will do this check for us. This method
        // does also check the module contains a class in the same package
        // of this name. This check is not done for vanilla names starting
        // with a lower case letter anymore.
        if (type instanceof LowerCaseClass) return resolveAliasFromModule(type);

        String name = type.getName();
        boolean type_setName = false;
        // We add a package if there is none yet and the module has one. But we
        // do not add that if the type is a ConstructedClassWithPackage. The code in ConstructedClassWithPackage
        // hasPackageName() will return true if ConstructedClassWithPackage#className has no dots.
        // but since the prefix may have them and the code there does ignore that fact.
        if (!type.hasPackageName() && module.hasPackageName() && !(type instanceof ConstructedClassWithPackage)) {
            type.setName(module.getPackageName() + name);
            type_setName = true;
        }
        // check the module node for a class with the name
        for (ClassNode localClass : module.getClasses()) {
            if (localClass.getName().equals(type.getName())) {
                if (localClass != type) type.setRedirect(localClass);
                return true;
            }
        }
        if (type_setName) type.setName(name);

        if (testModuleImports) {
            // check regular imports
            if (resolveAliasFromModule(type)) {
                return true;
            }
            // check enclosing package
            if (module.hasPackageName()) {
                // The usage of ConstructedClassWithPackage indicates the module
                // package will not be involved when the compiler tries to find an inner class.
                ClassNode tmp = new ConstructedClassWithPackage(module.getPackageName(), name);
                if (resolve(tmp, false, false, false)) {
                    ambiguousClass(type, tmp, name);
                    return true;
                }
            }
            // check static imports for static inner types
            for (ImportNode importNode : module.getStaticImports().values()) { // this may be fully redundant with resolveAliasFromModule
                if (importNode.getFieldName().equals(name)) {
                    ClassNode tmp = new ConstructedNestedClass(importNode.getType(), name);
                    if (resolve(tmp, false, false, true) && Modifier.isStatic(tmp.getModifiers())) {
                        type.setRedirect(tmp.redirect());
                        return true;
                    }
                }
            }
            for (ImportNode importNode : module.getStaticStarImports().values()) {
                ClassNode tmp = new ConstructedNestedClass(importNode.getType(), name);
                if (resolve(tmp, false, false, true) && Modifier.isStatic(tmp.getModifiers())) {
                    ambiguousClass(type, tmp, name);
                    return true;
                }
            }
            // check star imports ("import foo.*" or "import foo.Bar.*")
            for (ImportNode importNode : module.getStarImports()) {
                if (importNode.getType() != null) {
                    ClassNode tmp = new ConstructedNestedClass(importNode.getType(), name);
                    if (resolve(tmp, false, false, true) && Modifier.isStatic(tmp.getModifiers())) {
                        ambiguousClass(type, tmp, name);
                        return true;
                    }
                } else {
                    ClassNode tmp = new ConstructedClassWithPackage(importNode.getPackageName(), name);
                    if (resolve(tmp, false, false, true)) {
                        ambiguousClass(type, tmp, name);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected boolean resolveToOuter(final ClassNode type) {
        String name = type.getName();
        if (classNodeResolver == null)
            classNodeResolver = new ClassNodeResolver();
        // We do not need to check instances of LowerCaseClass
        // to be a Class, because unless there was an import for
        // this we do not look up these cases. This was a decision
        // made on the mailing list. To ensure we will not visit this
        // method again we set a NO_CLASS for this name
        if (type instanceof LowerCaseClass) {
            classNodeResolver.cacheClass(name, ClassNodeResolver.NO_CLASS);
        } else if (!currentClass.getModule().hasPackageName() || name.indexOf('.') != -1) {
            ClassNodeResolver.LookupResult lr = classNodeResolver.resolveName(name, compilationUnit);
            if (lr != null) {
                if (lr.isSourceUnit()) {
                    currentClass.getCompileUnit().addClassNodeToCompile(type, lr.getSourceUnit());
                    throw new Interrupt(compilationUnit); // GROOVY-10300, et al.: restart resolve
                } else {
                    ClassNode cn = lr.getClassNode();
                    if (cn != ClassNodeResolver.NO_CLASS) type.setRedirect(cn);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Expression transform(final Expression exp) {
        if (exp == null) return null;
        Expression ret;
        if (exp instanceof VariableExpression) {
            ret = transformVariableExpression((VariableExpression) exp);
        } else if (exp.getClass() == PropertyExpression.class) {
            ret = transformPropertyExpression((PropertyExpression) exp);
        } else if (exp instanceof DeclarationExpression) {
            ret = transformDeclarationExpression((DeclarationExpression) exp);
        } else if (exp instanceof BinaryExpression) {
            ret = transformBinaryExpression((BinaryExpression) exp);
        } else if (exp instanceof MethodCallExpression) {
            ret = transformMethodCallExpression((MethodCallExpression) exp);
        } else if (exp instanceof ClosureExpression) {
            ret = transformClosureExpression((ClosureExpression) exp);
        } else if (exp instanceof ConstructorCallExpression) {
            ret = transformConstructorCallExpression((ConstructorCallExpression) exp);
        } else if (exp instanceof AnnotationConstantExpression) {
            ret = transformAnnotationConstantExpression((AnnotationConstantExpression) exp);
        } else {
            resolveOrFail(exp.getType(), exp);
            ret = exp.transformExpression(this);
        }
        if (ret != null && ret != exp) {
            ret.setSourcePosition(exp);
        }
        return ret;
    }

    private static String lookupClassName(final PropertyExpression pe) {
        boolean doInitialClassTest = true;
        StringBuilder name = new StringBuilder(32);
        // this loop builds a name from right to left each name part separated by "."
        for (Expression expr = pe; expr != null && name != null; expr = ((PropertyExpression) expr).getObjectExpression()) {
            if (expr instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) expr;
                // stop at super and this
                if (ve.isSuperExpression() || ve.isThisExpression()) {
                    return null;
                }
                String varName = ve.getName();
                Tuple2<StringBuilder, Boolean> classNameInfo = makeClassName(doInitialClassTest, name, varName);
                name = classNameInfo.getV1();
                doInitialClassTest = classNameInfo.getV2();

                break;
            }

            // anything other than PropertyExpressions or
            // VariableExpressions will stop resolving
            if (expr.getClass() != PropertyExpression.class) {
                return null;
            }

            String property = ((PropertyExpression) expr).getPropertyAsString();
            // the class property stops resolving, dynamic property names too
            if (property == null || property.equals("class")) {
                return null;
            }
            Tuple2<StringBuilder, Boolean> classNameInfo = makeClassName(doInitialClassTest, name, property);
            name = classNameInfo.getV1();
            doInitialClassTest = classNameInfo.getV2();
        }

        if (name == null || name.length() == 0) return null;

        return name.toString();
    }

    private static Tuple2<StringBuilder, Boolean> makeClassName(final boolean doInitialClassTest, final StringBuilder name, final String varName) {
        if (doInitialClassTest) {
            // we are at the first name part. This is the right most part.
            // If this part is in lower case, then we do not need a class
            // check. other parts of the property expression will be tested
            // by a different method call to this method, so foo.Bar.bar
            // can still be resolved to the class foo.Bar and the static
            // field bar.
            if (!testVanillaNameForClass(varName)) {
                return tuple(null, Boolean.TRUE);
            } else {
                return tuple(new StringBuilder(varName), Boolean.FALSE);
            }
        }
        name.insert(0, varName + ".");
        return tuple(name, Boolean.FALSE);
    }

    /**
     * Returns "Type" class expression for "Type.class" or "pack.Type.class".
     */
    private static Expression correctClassClassChain(final PropertyExpression pe) {
        ClassExpression ce = null; LinkedList<PropertyExpression> stack = new LinkedList<>();
        for (Expression e = pe; e != null; e = ((PropertyExpression) e).getObjectExpression()) {
            if (e.getClass() == PropertyExpression.class) {
                stack.push((PropertyExpression) e);
            } else if (e instanceof ClassExpression) {
                ce = (ClassExpression) e; break;
            } else {
                return pe;
            }
        }
        if (ce == null) return pe;
        PropertyExpression classProperty = stack.pop();
        String propertyName = classProperty.getPropertyAsString();

        // if it's "Type.foo.bar" or something else return PropertyExpression
        if (propertyName == null || !propertyName.equals("class")) return pe;
        // if it's "Type.class" or "pack.Type.class" return ClassExpression
        ce.setSourcePosition(classProperty);
        if (stack.isEmpty()) return ce;

        PropertyExpression classPropertyExpressionContainer = stack.pop();
        classPropertyExpressionContainer.setObjectExpression(ce);
        return pe;
    }

    protected Expression transformPropertyExpression(final PropertyExpression pe) {
        Expression objectExpression = pe.getObjectExpression(), property;
        boolean ipe = inPropertyExpression, itlp = isTopLevelProperty;
        try {
            inPropertyExpression = true;
            isTopLevelProperty = (objectExpression.getClass() != PropertyExpression.class);
            objectExpression = transform(objectExpression);
            // handle the property part as if it were not part of the property
            inPropertyExpression = false;
            property = transform(pe.getProperty());
        } finally {
            inPropertyExpression = ipe;
            isTopLevelProperty = itlp;
        }
        PropertyExpression xe = new PropertyExpression(objectExpression, property, pe.isSafe());
        xe.setSpreadSafe(pe.isSpreadSafe());
        xe.setSourcePosition(pe);
      //xe.copyNodeMetaData(pe);

        String className = lookupClassName(xe);
        if (className != null) {
            ClassNode type = ClassHelper.make(className);
            if (resolve(type)) {
                return new ClassExpression(type);
            }
        }

        if (objectExpression instanceof ClassExpression && property instanceof ConstantExpression) {
            // possibly an inner class (or inherited inner class)
            for (ClassNode propertyOwner = objectExpression.getType(); propertyOwner != null; propertyOwner = propertyOwner.getSuperClass()) {
                ClassNode type = new ConstructedNestedClass(propertyOwner, xe.getPropertyAsString());
                if (resolve(type, false, false, false)) {
                    if (propertyOwner == objectExpression.getType() || isVisibleNestedClass(type, objectExpression.getType())) {
                        return new ClassExpression(type);
                    }
                }
            }
        }

        checkThisAndSuperAsPropertyAccess(xe);
        return isTopLevelProperty ? correctClassClassChain(xe) : xe;
    }

    private static boolean isVisibleNestedClass(final ClassNode innerType, final ClassNode outerType) {
        int modifiers = innerType.getModifiers();
        return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)
                || (!Modifier.isPrivate(modifiers) && Objects.equals(innerType.getPackageName(), outerType.getPackageName()));
    }

    private boolean directlyImplementsTrait(final ClassNode trait) {
        ClassNode[] interfaces = currentClass.getInterfaces();
        if (interfaces != null) {
            for (ClassNode face : interfaces) {
                if (face.equals(trait)) {
                    return true;
                }
            }
        }
        return currentClass.getUnresolvedSuperClass().equals(trait);
    }

    private void checkThisAndSuperAsPropertyAccess(final PropertyExpression expression) {
        if (expression.isImplicitThis()) return;
        String prop = expression.getPropertyAsString();
        if (prop == null) return;
        if (!prop.equals("this") && !prop.equals("super")) return;

        ClassNode type = expression.getObjectExpression().getType();
        if (expression.getObjectExpression() instanceof ClassExpression && !isSuperCallToDefaultMethod(expression) && !isThisCallToPrivateInterfaceMethod(expression)) {
            if (!(currentClass instanceof InnerClassNode) && !Traits.isTrait(type)) {
                addError("The usage of 'Class.this' and 'Class.super' is only allowed in nested/inner classes.", expression);
                return;
            }
            if (currentScope != null && !currentScope.isInStaticContext() && Traits.isTrait(type) && "super".equals(prop) && directlyImplementsTrait(type)) {
                return;
            }
            ClassNode iterType = currentClass;
            while (iterType != null) {
                if (iterType.equals(type)) break;
                iterType = iterType.getOuterClass();
            }
            if (iterType == null) {
                addError("The class '" + type.getName() + "' needs to be an outer class of '" +
                        currentClass.getName() + "' when using '.this' or '.super'.", expression);
            }
            if (!Modifier.isStatic(currentClass.getModifiers())) return;
            if (currentScope != null && !currentScope.isInStaticContext()) return;
            addError("The usage of 'Class.this' and 'Class.super' within static nested class '" +
                    currentClass.getName() + "' is not allowed in a static context.", expression);
        }
    }

    private boolean isSuperCallToDefaultMethod(PropertyExpression expression) {
        // a more sophisticated check might be required in the future
        ClassExpression clazzExpression = (ClassExpression) expression.getObjectExpression();
        return clazzExpression.getType().isInterface() && expression.getPropertyAsString().equals("super");
    }

    private boolean isThisCallToPrivateInterfaceMethod(PropertyExpression expression) {
        // a more sophisticated check might be required in the future
        ClassExpression clazzExpression = (ClassExpression) expression.getObjectExpression();
        return clazzExpression.getType().isInterface() && expression.getPropertyAsString().equals("this");
    }

    protected Expression transformVariableExpression(final VariableExpression ve) {
        visitAnnotations(ve);
        Variable v = ve.getAccessedVariable();

        if (v instanceof DynamicVariable) {
            String name = ve.getName();
            ClassNode t = ClassHelper.make(name);
            // asking isResolved here allows to check if a primitive
            // type name like "int" was used to make t. In such a case
            // we have nothing left to do.
            boolean isClass = t.isResolved();
            if (!isClass) {
                // It was no primitive type, so next we see if the name,
                // which is a vanilla name, starts with a lower case letter.
                // In that case we change it to a LowerCaseClass to let the
                // compiler skip the resolving at several places in this class.
                if (Character.isLowerCase(name.charAt(0))) {
                    t = new LowerCaseClass(name);
                }
                isClass = resolve(t);
            }
            if (isClass) {
                // the name is a type so remove it from the scoping
                // as it is only a classvariable, it is only in
                // referencedClassVariables, but must be removed
                // for each parentscope too
                for (VariableScope scope = currentScope; scope != null && !scope.isRoot(); scope = scope.getParent()) {
                    if (scope.removeReferencedClassVariable(ve.getName()) == null) break;
                }
                ClassExpression ce = new ClassExpression(t);
                ce.setSourcePosition(ve);
                return ce;
            }
        } else if (!checkingVariableTypeInDeclaration) {
            // GROOVY-4009: When a normal variable is simply being used, there is no need to try to
            // resolve its type. Variable type resolve should proceed only if the variable is being declared.
            return ve;
        }
        resolveOrFail(ve.getType(), ve);
        ClassNode origin = ve.getOriginType();
        if (origin != ve.getType()) resolveOrFail(origin, ve);
        return ve;
    }

    private static boolean testVanillaNameForClass(final String name) {
        if (name == null || name.length() == 0) return false;
        return !Character.isLowerCase(name.charAt(0));
    }

    protected Expression transformBinaryExpression(final BinaryExpression be) {
        Expression left = transform(be.getLeftExpression());
        if (be.getOperation().isA(Types.ASSIGNMENT_OPERATOR) && left instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression) left;
            String error = "you tried to assign a value to the class '" + ce.getType().getName() + "'";
            if (ce.getType().isScript()) {
                error += ". Do you have a script with this name?";
            }
            addError(error, be.getLeftExpression());
            return be;
        }
        if (left instanceof ClassExpression && be.getOperation().isOneOf(
                new int[]{Types.ARRAY_EXPRESSION, Types.SYNTH_LIST, Types.SYNTH_MAP})) {
            if (be.getRightExpression() instanceof ListExpression) {
                ListExpression list = (ListExpression) be.getRightExpression();
                if (list.getExpressions().isEmpty()) {
                    return new ClassExpression(left.getType().makeArray());
                } else {
                    // maybe we have C[k1:v1, k2:v2] -> should become (C)([k1:v1, k2:v2])
                    boolean map = true;
                    for (Expression expression : list.getExpressions()) {
                        if (!(expression instanceof MapEntryExpression)) {
                            map = false;
                            break;
                        }
                    }

                    if (map) {
                        MapExpression me = new MapExpression();
                        for (Expression expression : list.getExpressions()) {
                            me.addMapEntryExpression((MapEntryExpression) transform(expression));
                        }
                        me.setSourcePosition(list);
                        return CastExpression.asExpression(left.getType(), me);
                    }
                }
            } else if (be.getRightExpression() instanceof SpreadMapExpression) {
                // we have C[*:map] -> should become (C) map
                SpreadMapExpression mapExpression = (SpreadMapExpression) be.getRightExpression();
                Expression right = transform(mapExpression.getExpression());
                return CastExpression.asExpression(left.getType(), right);
            }

            if (be.getRightExpression() instanceof MapEntryExpression) {
                // may be we have C[k1:v1] -> should become (C)([k1:v1])
                MapExpression me = new MapExpression();
                me.addMapEntryExpression((MapEntryExpression) transform(be.getRightExpression()));
                me.setSourcePosition(be.getRightExpression());
                return new CastExpression(left.getType(), me);
            }
        }
        Expression right = transform(be.getRightExpression());
        be.setLeftExpression(left);
        be.setRightExpression(right);
        return be;
    }

    protected Expression transformClosureExpression(final ClosureExpression ce) {
        boolean oldInClosure = inClosure;
        inClosure = true;
        for (Parameter para : getParametersSafe(ce)) {
            ClassNode t = para.getType();
            resolveOrFail(t, ce);
            visitAnnotations(para);
            if (para.hasInitialExpression()) {
                para.setInitialExpression(transform(para.getInitialExpression()));
            }
            visitAnnotations(para);
        }

        Statement code = ce.getCode();
        if (code != null) code.visit(this);
        inClosure = oldInClosure;
        return ce;
    }

    protected Expression transformConstructorCallExpression(final ConstructorCallExpression cce) {
        if (!cce.isUsingAnonymousInnerClass()) { // GROOVY-9642
            ClassNode cceType = cce.getType();
            resolveOrFail(cceType, cce);
            if (cceType.isAbstract()) {
                addError("You cannot create an instance from the abstract " + getDescription(cceType) + ".", cce);
            }
        }

        return cce.transformExpression(this);
    }

    private static String getDescription(final ClassNode node) {
        return (node.isInterface() ? "interface" : "class") + " '" + node.getName() + "'";
    }

    protected Expression transformMethodCallExpression(final MethodCallExpression mce) {
        Expression args = transform(mce.getArguments());
        Expression method = transform(mce.getMethod());
        Expression object = transform(mce.getObjectExpression());

        resolveGenericsTypes(mce.getGenericsTypes());

        MethodCallExpression ret = new MethodCallExpression(object, method, args);
        ret.setGenericsTypes(mce.getGenericsTypes());
        ret.setMethodTarget(mce.getMethodTarget());
        ret.setImplicitThis(mce.isImplicitThis());
        ret.setSpreadSafe(mce.isSpreadSafe());
        ret.setSafe(mce.isSafe());
        return ret;
    }

    protected Expression transformDeclarationExpression(final DeclarationExpression de) {
        visitAnnotations(de);
        Expression oldLeft = de.getLeftExpression();
        checkingVariableTypeInDeclaration = true;
        Expression left = transform(oldLeft);
        checkingVariableTypeInDeclaration = false;
        if (left instanceof ClassExpression) {
            ClassExpression ce = (ClassExpression) left;
            addError("you tried to assign a value to the class " + ce.getType().getName(), oldLeft);
            return de;
        }
        Expression right = transform(de.getRightExpression());
        if (right == de.getRightExpression()) {
            fixDeclaringClass(de);
            return de;
        }
        DeclarationExpression newDeclExpr = new DeclarationExpression(left, de.getOperation(), right);
        newDeclExpr.setDeclaringClass(de.getDeclaringClass());
        newDeclExpr.addAnnotations(de.getAnnotations());
        newDeclExpr.copyNodeMetaData(de);
        fixDeclaringClass(newDeclExpr);
        return newDeclExpr;
    }

    // TODO: get normal resolving to set declaring class
    private void fixDeclaringClass(final DeclarationExpression newDeclExpr) {
        if (newDeclExpr.getDeclaringClass() == null && currentMethod != null) {
            newDeclExpr.setDeclaringClass(currentMethod.getDeclaringClass());
        }
    }

    protected Expression transformAnnotationConstantExpression(final AnnotationConstantExpression ace) {
        visitAnnotation((AnnotationNode) ace.getValue());
        return ace;
    }

    private void visitTypeAnnotations(final ClassNode node) {
        visitAnnotations(node.getTypeAnnotations());
    }

    @Override
    protected void visitAnnotation(final AnnotationNode node) {
        Collection<AnnotationNode> collector = currentClass.getNodeMetaData(AnnotationNode[].class);
        if (collector != null) {
            collector.add(node); // GROOVY-11179: defer resolve and inlining
        } else {
            resolveOrFail(node.getClassNode(), " for annotation", node, true);
            // duplicates part of AnnotationVisitor because we cannot wait until later
            for (Map.Entry<String, Expression> entry : node.getMembers().entrySet()) {
                // resolve constant-looking expressions statically
                // do it now since they get transformed away later
                Expression value = transform(entry.getValue());
                value = transformInlineConstants(value);
                checkAnnotationMemberValue(value);
                entry.setValue(value);
            }
        }
    }

    private void checkAnnotationMemberValue(final Expression value) {
        if (value instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) value;
            if (!(pe.getObjectExpression() instanceof ClassExpression)) {
                addError("unable to find class '" + pe.getText() + "' for annotation attribute constant", pe.getObjectExpression());
            }
        } else if (value instanceof ListExpression) {
            ListExpression le = (ListExpression) value;
            for (Expression e : le.getExpressions()) {
                checkAnnotationMemberValue(e);
            }
        }
    }

    @Override
    public void visitClass(final ClassNode node) {
        ClassNode oldNode = currentClass;
        currentClass = node;

        ModuleNode module = node.getModule();
        if (!module.hasImportsResolved()) {
            for (ImportNode importNode : module.getImports()) {
                currentImport = importNode;
                ClassNode type = importNode.getType();
                if (resolve(type, false, false, true)) {
                    currentImport = null;
                    continue;
                }
                currentImport = null;
                addError("unable to resolve class " + type.getName(), type);
            }
            for (ImportNode importNode : module.getStarImports()) {
                if (importNode.getLineNumber() > 0) {
                    currentImport = importNode;
                    String importName = importNode.getPackageName();
                    importName = importName.substring(0, importName.length()-1);
                    ClassNode type = ClassHelper.makeWithoutCaching(importName);
                    if (resolve(type, false, false, true)) {
                        importNode.setType(type);
                    }
                    currentImport = null;
                }
            }
            for (ImportNode importNode : module.getStaticImports().values()) {
                ClassNode type = importNode.getType();
                if (!resolve(type, true, true, true))
                    addError("unable to resolve class " + type.getName(), type);
            }
            for (ImportNode importNode : module.getStaticStarImports().values()) {
                ClassNode type = importNode.getType();
                if (!resolve(type, true, true, true))
                    addError("unable to resolve class " + type.getName(), type);
            }

            module.setImportsResolved(true);
        }

        //

        if (phase < 2) node.putNodeMetaData(AnnotationNode[].class, new LinkedHashSet<>());

        if (!(node instanceof InnerClassNode) || Modifier.isStatic(node.getModifiers())) {
            genericParameterNames = new HashMap<>();
        }
        resolveGenericsHeader(node.getGenericsTypes());
        switch (phase) { // GROOVY-9866, GROOVY-10466
          case 0:
          case 1:
            ClassNode sn = node.getUnresolvedSuperClass();
            if (sn != null) {
                resolveOrFail(sn, "", node, true);
            }
            for (ClassNode in : node.getInterfaces()) {
                resolveOrFail(in, "", node, true);
            }

            if (sn != null) checkCyclicInheritance(node, sn);
            for (ClassNode in : node.getInterfaces()) {
                checkCyclicInheritance(node, in);
            }
          case 2:
            // VariableScopeVisitor visits anon. inner class body inline, so resolve now
            for (Iterator<InnerClassNode> it = node.getInnerClasses(); it.hasNext(); ) {
                InnerClassNode cn = it.next();
                if (cn.isAnonymous()) {
                    MethodNode enclosingMethod = cn.getEnclosingMethod();
                    if (enclosingMethod != null) {
                        resolveGenericsHeader(enclosingMethod.getGenericsTypes()); // GROOVY-6977
                    }
                    resolveOrFail(cn.getUnresolvedSuperClass(false), cn); // GROOVY-9642
                }
            }
            if (phase == 1) break; // resolve other class headers before members, et al.

            // initialize scopes/variables now that imports and super types are resolved
            new VariableScopeVisitor(source).visitClass(node);

            visitPackage(node.getPackage());
            visitImports(node.getModule());
            visitAnnotations(node);

            @SuppressWarnings("unchecked") // grab the collected annotations and stop collecting
            var headerAnnotations = (Set<AnnotationNode>) node.putNodeMetaData(AnnotationNode[].class, null);

            node.visitContents(this);
            visitObjectInitializerStatements(node);
            // GROOVY-10750, GROOVY-11179: resolve and inline
            headerAnnotations.forEach(this::visitAnnotation);
        }
        currentClass = oldNode;
    }

    private void addFatalError(final String text, final ASTNode node) {
        source.addFatalError(text, node);
    }

    private void checkCyclicInheritance(final ClassNode node, final ClassNode type) {
        if (type.redirect() == node || type.getOuterClasses().contains(node)) {
            addFatalError("Cycle detected: the type " + node.getName() + " cannot extend/implement itself or one of its own member types", type);
        } else if (type != ClassHelper.OBJECT_TYPE) {
            Set<ClassNode> done = new HashSet<>();
            done.add(ClassHelper.OBJECT_TYPE);
            done.add(null);

            LinkedList<ClassNode> todo = new LinkedList<>();
            Collections.addAll(todo, type.getInterfaces());
            todo.add(type.getUnresolvedSuperClass());
            todo.add(type.getOuterClass());
            do {
                ClassNode next = todo.poll();
                if (!done.add(next)) continue;
                if (next.redirect() == node) {
                    ClassNode cn = type; while (cn.getOuterClass() != null) cn = cn.getOuterClass();
                    addFatalError("Cycle detected: a cycle exists in the type hierarchy between " + node.getName() + " and " + cn.getName(), type);
                }
                Collections.addAll(todo, next.getInterfaces());
                todo.add(next.getUnresolvedSuperClass());
                todo.add(next.getOuterClass());
            } while (!todo.isEmpty());
        }
    }

    @Override
    public void visitCatchStatement(final CatchStatement cs) {
        resolveOrFail(cs.getExceptionType(), cs);
        if (ClassHelper.isDynamicTyped(cs.getExceptionType())) {
            cs.getVariable().setType(ClassHelper.make(Exception.class));
        }
        super.visitCatchStatement(cs);
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        resolveOrFail(forLoop.getVariableType(), forLoop);
        super.visitForLoop(forLoop);
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        VariableScope oldScope = currentScope;
        currentScope = block.getVariableScope();
        super.visitBlockStatement(block);
        currentScope = oldScope;
    }

    private boolean resolveGenericsTypes(final GenericsType[] types) {
        if (types == null) return true;
        currentClass.setUsingGenerics(true);
        boolean resolved = true;
        for (GenericsType type : types) {
            // attempt resolution on all types, so don't short-circuit and stop if we've previously failed
            resolved = resolveGenericsType(type) && resolved;
        }
        return resolved;
    }

    private void resolveGenericsHeader(final GenericsType[] types) {
        if (types != null) resolveGenericsHeader(types, null, 0);
    }

    private void resolveGenericsHeader(final GenericsType[] types, final GenericsType rootType, final int level) {
        currentClass.setUsingGenerics(true);
        List<Tuple2<ClassNode, ClassNode>> upperBoundsToResolve = new LinkedList<>();
        List<Tuple2<ClassNode, GenericsType>> upperBoundsWithGenerics = new LinkedList<>();
        for (GenericsType type : types) {
            if (level > 0 && type.getName().equals(rootType.getName())) continue; // cycle!

            String name = type.getName();
            ClassNode typeType = type.getType();
            visitTypeAnnotations(typeType); // JSR 308 support
            GenericsTypeName gtn = new GenericsTypeName(name);
            boolean isWildcardGT = QUESTION_MARK.equals(name);
            boolean dealWithGenerics = (level == 0 || (level > 0 && genericParameterNames.get(gtn) != null));

            if (type.getUpperBounds() != null) {
                boolean nameAdded = false;
                for (ClassNode upperBound : type.getUpperBounds()) {
                    if (upperBound == null) continue; // TODO: error

                    if (!isWildcardGT) {
                        if (!nameAdded || !resolve(typeType)) {
                            if (dealWithGenerics) {
                                type.setPlaceholder(true);
                                typeType.setRedirect(upperBound);
                                genericParameterNames.put(gtn, type);
                                nameAdded = true;
                            }
                        }
                        upperBoundsToResolve.add(tuple(upperBound, typeType));
                    }
                    if (upperBound.getGenericsTypes() != null) {
                        upperBoundsWithGenerics.add(tuple(upperBound, type));
                    }
                }
            } else if (dealWithGenerics && !isWildcardGT) {
                type.setPlaceholder(true);
                GenericsType last = genericParameterNames.put(gtn, type);
                typeType.setRedirect(last != null ? last.getType().redirect() : ClassHelper.OBJECT_TYPE);
            }
        }

        for (var tuple : upperBoundsToResolve) {
            ClassNode upperBound = tuple.getV1();
            ClassNode sourceType = tuple.getV2();
            resolveOrFail(upperBound, sourceType);
            if (upperBound.isGenericsPlaceHolder()) {
                checkCyclicInheritance(upperBound, sourceType); // GROOVY-10113,GROOVY-10998
            }
        }

        for (var tuple : upperBoundsWithGenerics) {
            GenericsType[] bounds = tuple.getV1().getGenericsTypes();
            resolveGenericsHeader(bounds, level == 0 ? tuple.getV2() : rootType, level + 1);
        }
    }

    private boolean resolveGenericsType(final GenericsType genericsType) {
        if (genericsType.isResolved()) return true;
        currentClass.setUsingGenerics(true);
        ClassNode type = genericsType.getType();
        visitTypeAnnotations(type); // JSR 308 support
        GenericsType tp = genericParameterNames.get(new GenericsTypeName(type.getName()));
        if (tp != null) {
            ClassNode[] bounds = tp.getUpperBounds();
            if (bounds != null && (bounds.length > 1 || (bounds[0].isRedirectNode()
                                   && bounds[0].redirect().getGenericsTypes() != null))) {
                // GROOVY-10622: bounds are too complex for a redirect-only representation
                //genericsType.setUpperBounds(bounds);
                type.setGenericsPlaceHolder(true);
                type.setRedirect(bounds[0]);
            } else {
                type.setRedirect(tp.getType());
            }
            genericsType.setPlaceholder(true);
        } else {
            ClassNode[] upperBounds = genericsType.getUpperBounds();
            if (upperBounds != null) {
                for (ClassNode upperBound : upperBounds) {
                    resolveOrFail(upperBound, genericsType);
                    type.setRedirect(upperBound);
                    resolveGenericsTypes(upperBound.getGenericsTypes());
                }
            } else if (genericsType.isWildcard()) {
                type.setRedirect(ClassHelper.OBJECT_TYPE);
            } else {
                resolveOrFail(type, genericsType);
            }
        }
        if (genericsType.getLowerBound() != null) {
            resolveOrFail(genericsType.getLowerBound(), genericsType);
        }
        if (resolveGenericsTypes(type.getGenericsTypes())) {
            genericsType.setResolved(genericsType.getType().isResolved());
        }
        return genericsType.isResolved();
    }

    /**
     * For cases like "Foo&lt;? super Bar> -> Foo&lt;T extends Baz>" there is an
     * implicit upper bound on the wildcard type argument. It was unavailable at
     * the time "? super Bar" was resolved but is present in type's redirect now.
     */
    private static void resolveWildcardBounding(final GenericsType[] typeArguments, final ClassNode type) {
        for (int i = 0, n = typeArguments.length; i < n; i += 1) { GenericsType argument= typeArguments[i];
            if (!argument.isWildcard() || argument.getUpperBounds() != null) continue;
            GenericsType[] parameters = type.redirect().getGenericsTypes();
            if (parameters != null && i < parameters.length) {
                ClassNode implicitBound = parameters[i].getType();
                if (!ClassHelper.isObjectType(implicitBound))
                    argument.getType().setRedirect(implicitBound);
            }
        }
    }
}
