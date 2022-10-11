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
package org.codehaus.groovy.transform.stc;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base class for type checking extensions written in Groovy. Compared to its superclass, {@link TypeCheckingExtension},
 * this class adds a number of utility methods aimed at leveraging the syntax of the Groovy language to improve
 * expressivity and conciseness.
 *
 * @since 2.1.0
 */
public class GroovyTypeCheckingExtensionSupport extends AbstractTypeCheckingExtension {

    // method name to DSL name
    private static final Map<String, String> METHOD_ALIASES = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                private static final long serialVersionUID = 8938707932245818749L;

                {
                put("onMethodSelection", "onMethodSelection");
                put("afterMethodCall", "afterMethodCall");
                put("beforeMethodCall", "beforeMethodCall");
                put("unresolvedVariable", "handleUnresolvedVariableExpression");
                put("unresolvedProperty", "handleUnresolvedProperty");
                put("unresolvedAttribute", "handleUnresolvedAttribute");
                put("ambiguousMethods", "handleAmbiguousMethods");
                put("methodNotFound", "handleMissingMethod");
                put("afterVisitMethod", "afterVisitMethod");
                put("beforeVisitMethod", "beforeVisitMethod");
                put("afterVisitClass", "afterVisitClass");
                put("beforeVisitClass", "beforeVisitClass");
                put("incompatibleAssignment", "handleIncompatibleAssignment");
                put("incompatibleReturnType", "handleIncompatibleReturnType");
                put("setup","setup");
                put("finish", "finish");
            }}
    );

    // the following fields are closures executed in event-based methods
    private final Map<String, List<Closure>> eventHandlers = new HashMap<String, List<Closure>>();

    private final String scriptPath;

    private final CompilationUnit compilationUnit;

    /**
     * Builds a type checking extension relying on a Groovy script (type checking DSL).
     *
     * @param typeCheckingVisitor the type checking visitor
     * @param scriptPath the path to the type checking script (in classpath)
     * @param compilationUnit
     */
    public GroovyTypeCheckingExtensionSupport(
            final StaticTypeCheckingVisitor typeCheckingVisitor,
            final String scriptPath, final CompilationUnit compilationUnit) {
        super(typeCheckingVisitor);
        this.scriptPath = scriptPath;
        this.compilationUnit = compilationUnit;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setup() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass("org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL");
        ImportCustomizer ic = new ImportCustomizer();
        ic.addStarImports("org.codehaus.groovy.ast.expr");
        ic.addStaticStars("org.codehaus.groovy.ast.ClassHelper");
        ic.addStaticStars("org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport");
        config.addCompilationCustomizers(ic);
        final GroovyClassLoader transformLoader = compilationUnit!=null?compilationUnit.getTransformLoader():typeCheckingVisitor.getSourceUnit().getClassLoader();

        // since Groovy 2.2, it is possible to use FQCN for type checking extension scripts
        TypeCheckingDSL script = null;
        try {
            Class<?> clazz = transformLoader.loadClass(scriptPath, false, true);
            if (TypeCheckingDSL.class.isAssignableFrom(clazz)) {
                script = (TypeCheckingDSL) clazz.getDeclaredConstructor().newInstance();
            } else if (TypeCheckingExtension.class.isAssignableFrom(clazz)) {
                // since 2.4, we can also register precompiled type checking extensions which are not scripts
                try {
                    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor(StaticTypeCheckingVisitor.class);
                    TypeCheckingExtension extension = (TypeCheckingExtension) declaredConstructor.newInstance(typeCheckingVisitor);
                    typeCheckingVisitor.addTypeCheckingExtension(extension);
                    extension.setup();
                    return;
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    addLoadingError(config);
                } catch (NoSuchMethodException e) {
                    context.getErrorCollector().addFatalError(
                            new SimpleMessage("Static type checking extension '" + scriptPath + "' could not be loaded because it doesn't have a constructor accepting StaticTypeCheckingVisitor.",
                                    config.getDebug(), typeCheckingVisitor.getSourceUnit())
                    );
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // silent
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            addLoadingError(config);
        }
        if (script==null) {
            ClassLoader cl = typeCheckingVisitor.getSourceUnit().getClassLoader();
            // cast to prevent incorrect @since 1.7 warning
            InputStream is = ((ClassLoader)transformLoader).getResourceAsStream(scriptPath);
            if (is == null) {
                // fallback to the source unit classloader
                is = cl.getResourceAsStream(scriptPath);
            }
            if (is == null) {
                // fallback to the compiler classloader
                cl = GroovyTypeCheckingExtensionSupport.class.getClassLoader();
                is = cl.getResourceAsStream(scriptPath);
            }
            if (is == null) {
                // if the input stream is still null, we've not found the extension
                context.getErrorCollector().addFatalError(
                        new SimpleMessage("Static type checking extension '" + scriptPath + "' was not found on the classpath.",
                                config.getDebug(), typeCheckingVisitor.getSourceUnit()));
            }
            try {
                GroovyShell shell = new GroovyShell(transformLoader, new Binding(), config);
                script = (TypeCheckingDSL) shell.parse(
                        new InputStreamReader(is, typeCheckingVisitor.getSourceUnit().getConfiguration().getSourceEncoding())
                );
            } catch (CompilationFailedException e) {
                throw new GroovyBugError("An unexpected error was thrown during custom type checking", e);
            } catch (UnsupportedEncodingException e) {
                throw new GroovyBugError("Unsupported encoding found in compiler configuration", e);
            }
        }
        if (script!=null) {
            script.extension = this;
            script.run();
            List<Closure> list = eventHandlers.get("setup");
            if (list != null) {
                for (Closure closure : list) {
                    safeCall(closure);
                }
            }
        }
    }

    private void addLoadingError(final CompilerConfiguration config) {
        context.getErrorCollector().addFatalError(
                new SimpleMessage("Static type checking extension '" + scriptPath + "' could not be loaded.",
                        config.getDebug(), typeCheckingVisitor.getSourceUnit())
        );
    }

    @Override
    public void finish() {
        List<Closure> list = eventHandlers.get("finish");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure);
            }
        }
    }

    @Override
    public void onMethodSelection(final Expression expression, final MethodNode target) {
        List<Closure> onMethodSelection = eventHandlers.get("onMethodSelection");
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                safeCall(closure, expression, target);
            }
        }
    }

    @Override
    public void afterMethodCall(final MethodCall call) {
        List<Closure> onMethodSelection = eventHandlers.get("afterMethodCall");
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                safeCall(closure, call);
            }
        }
    }

   @Override
    public boolean beforeMethodCall(final MethodCall call) {
       setHandled(false);
       List<Closure> onMethodSelection = eventHandlers.get("beforeMethodCall");
       if (onMethodSelection != null) {
           for (Closure closure : onMethodSelection) {
               safeCall(closure, call);
           }
       }
       return handled;
    }

    @Override
    public boolean handleUnresolvedVariableExpression(final VariableExpression vexp) {
        setHandled(false);
        List<Closure> onMethodSelection = eventHandlers.get("handleUnresolvedVariableExpression");
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                safeCall(closure, vexp);
            }
        }
        return handled;
    }

    @Override
    public boolean handleUnresolvedProperty(final PropertyExpression pexp) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleUnresolvedProperty");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, pexp);
            }
        }
        return handled;
    }

    @Override
    public boolean handleUnresolvedAttribute(final AttributeExpression aexp) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleUnresolvedAttribute");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, aexp);
            }
        }
        return handled;
    }

    @Override
    public void afterVisitMethod(final MethodNode node) {
        List<Closure> list = eventHandlers.get("afterVisitMethod");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
    }

    @Override
    public boolean beforeVisitClass(final ClassNode node) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("beforeVisitClass");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
        return handled;
    }

    @Override
    public void afterVisitClass(final ClassNode node) {
        List<Closure> list = eventHandlers.get("afterVisitClass");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
    }

    @Override
    public boolean beforeVisitMethod(final MethodNode node) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("beforeVisitMethod");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, node);
            }
        }
        return handled;
    }

    @Override
    public boolean handleIncompatibleAssignment(final ClassNode lhsType, final ClassNode rhsType, final Expression assignmentExpression) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleIncompatibleAssignment");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, lhsType, rhsType, assignmentExpression);
            }
        }
        return handled;
    }

    @Override
    public boolean handleIncompatibleReturnType(final ReturnStatement returnStatement, ClassNode inferredReturnType) {
        setHandled(false);
        List<Closure> list = eventHandlers.get("handleIncompatibleReturnType");
        if (list != null) {
            for (Closure closure : list) {
                safeCall(closure, returnStatement, inferredReturnType);
            }
        }
        return handled;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MethodNode> handleMissingMethod(final ClassNode receiver, final String name, final ArgumentListExpression argumentList, final ClassNode[] argumentTypes, final MethodCall call) {
        List<Closure> onMethodSelection = eventHandlers.get("handleMissingMethod");
        List<MethodNode> methodList = new LinkedList<MethodNode>();
        if (onMethodSelection != null) {
            for (Closure closure : onMethodSelection) {
                Object result = safeCall(closure, receiver, name, argumentList, argumentTypes, call);
                if (result != null) {
                    if (result instanceof MethodNode) {
                        methodList.add((MethodNode) result);
                    } else if (result instanceof Collection) {
                        methodList.addAll((Collection<? extends MethodNode>) result);
                    } else {
                        throw new GroovyBugError("Type checking extension returned unexpected method list: " + result);
                    }
                }
            }
        }
        return methodList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MethodNode> handleAmbiguousMethods(final List<MethodNode> nodes, final Expression origin) {
        List<Closure> onMethodSelection = eventHandlers.get("handleAmbiguousMethods");
        List<MethodNode> methodList = nodes;
        if (onMethodSelection != null) {
            Iterator<Closure> iterator = onMethodSelection.iterator();
            while (methodList.size()>1 && iterator.hasNext() ) {
                final Closure closure = iterator.next();
                Object result = safeCall(closure, methodList, origin);
                if (result != null) {
                    if (result instanceof MethodNode) {
                        methodList = Collections.singletonList((MethodNode) result);
                    } else if (result instanceof Collection) {
                        methodList = new LinkedList<MethodNode>((Collection<? extends MethodNode>) result);
                    } else {
                        throw new GroovyBugError("Type checking extension returned unexpected method list: " + result);
                    }
                }
            }
        }
        return methodList;
    }

    /**
     * Event handler registration:
     * <dl>
     *   <dt>setup</dt>                          <dd>Registers closure that runs after the type checker finishes initialization</dd>
     *   <dt>finish</dt>                         <dd>Registers closure that runs after the type checker completes type checking</dd>
     *   <dt>beforeVisitClass</dt>               <dd>Registers closure that runs before type checking a class</dd>
     *   <dt>afterVisitClass</dt>                <dd>Registers closure that runs after having finished the visit of a type checked class</dd>
     *   <dt>beforeVisitMethod</dt>              <dd>Registers closure that runs before type checking a method body</dd>
     *   <dt>afterVisitMethod</dt>               <dd>Registers closure that runs after type checking a method body</dd>
     *   <dt>beforeMethodCall</dt>               <dd>Registers closure that runs before the type checker starts type checking a method call</dd>
     *   <dt>afterMethodCall</dt>                <dd>Registers closure that runs once the type checker has finished type checking a method call</dd>
     *   <dt>methodNotFound</dt>                 <dd>Registers closure that runs when it fails to find an appropriate method for a method call</dd>
     *   <dt>ambiguousMethods</dt>               <dd>Registers closure that runs when the type checker cannot choose between several candidate methods</dd>
     *   <dt>onMethodSelection</dt>              <dd>Registers closure that runs when it finds a method appropriate for a method call</dd>
     *   <dt>unresolvedVariable</dt>             <dd>Registers closure that runs when the type checker finds an unresolved variable</dd>
     *   <dt>unresolvedProperty</dt>             <dd>Registers closure that runs when the type checker cannot find a property on the receiver</dd>
     *   <dt>unresolvedAttribute</dt>            <dd>Registers closure that runs when the type checker cannot find an attribute on the receiver</dd>
     *   <dt>incompatibleAssignment</dt>         <dd>Registers closure that runs when the type checker thinks that the right-hand side of an assignment is incompatible with the left-hand side</dd>
     *   <dt>incompatibleReturnType</dt>         <dd>Registers closure that runs when the type checker thinks that a return value is incompatibe with the return type</dd>
     * </dl>
     *
     * Expression categorization:
     * <dl>
     *   <dt>isAnnotationConstantExpression</dt> <dd>Determines if argument is an {@link org.codehaus.groovy.ast.expr.AnnotationConstantExpression AnnotationConstantExpression}</dd>
     *   <dt>isArgumentListExpression</dt>       <dd>Determines if argument is an {@link org.codehaus.groovy.ast.expr.ArgumentListExpression ArgumentListExpression}</dd>
     *   <dt>isArrayExpression</dt>              <dd>Determines if argument is an {@link org.codehaus.groovy.ast.expr.ArrayExpression ArrayExpression}</dd>
     *   <dt>isAttributeExpression</dt>          <dd>Determines if argument is an {@link org.codehaus.groovy.ast.expr.AttributeExpression AttributeExpression}</dd>
     *   <dt>isBinaryExpression</dt>             <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.BinaryExpression BinaryExpression}</dd>
     *   <dt>isBitwiseNegationExpression</dt>    <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.BitwiseNegationExpression BitwiseNegationExpression}</dd>
     *   <dt>isBooleanExpression</dt>            <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.BooleanExpression BooleanExpression}</dd>
     *   <dt>isCastExpression</dt>               <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.CastExpression CastExpression}</dd>
     *   <dt>isClassExpression</dt>              <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.ClassExpression ClassExpression}</dd>
     *   <dt>isClosureExpression</dt>            <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.ClosureExpression ClosureExpression}</dd>
     *   <dt>isConstantExpression</dt>           <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.ConstantExpression ConstantExpression}</dd>
     *   <dt>isConstructorCallExpression</dt>    <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.ConstructorCallExpression ConstructorCallExpression}</dd>
     *   <dt>isDeclarationExpression</dt>        <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.DeclarationExpression DeclarationExpression}</dd>
     *   <dt>isElvisOperatorExpression</dt>      <dd>Determines if argument is an {@link org.codehaus.groovy.ast.expr.ElvisOperatorExpression ElvisOperatorExpression}</dd>
     *   <dt>isEmptyExpression</dt>              <dd>Determines if argument is an {@link org.codehaus.groovy.ast.expr.EmptyExpression EmptyExpression}</dd>
     *   <dt>isFieldExpression</dt>              <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.FieldExpression FieldExpression}</dd>
     *   <dt>isGStringExpression</dt>            <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.GStringExpression GStringExpression}</dd>
     *   <dt>isLambdaExpression</dt>             <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.LambdaExpression LambdaExpression}</dd>
     *   <dt>isListExpression</dt>               <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.ListExpression ListExpression}</dd>
     *   <dt>isMapExpression</dt>                <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.MapExpression MapExpression}</dd>
     *   <dt>isMapEntryExpression</dt>           <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.MapEntryExpression MapEntryExpression}</dd>
     *   <dt>isMethodCallExpression</dt>         <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.MethodCallExpression MethodCallExpression}</dd>
     *   <dt>isMethodPointerExpression</dt>      <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.MethodPointerExpression MethodPointerExpression}</dd>
     *   <dt>isMethodReferenceExpression</dt>    <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.MethodReferenceExpression MethodReferenceExpression}</dd>
     *   <dt>isNamedArgumentListExpression</dt>  <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.NamedArgumentListExpression NamedArgumentListExpression}</dd>
     *   <dt>isNotExpression</dt>                <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.NotExpression NotExpression}</dd>
     *   <dt>isPostfixExpression</dt>            <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.PostfixExpression PostfixExpression}</dd>
     *   <dt>isPrefixExpression</dt>             <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.PrefixExpression PrefixExpression}</dd>
     *   <dt>isPropertyExpression</dt>           <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.PropertyExpression PropertyExpression}</dd>
     *   <dt>isRangeExpression</dt>              <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.RangeExpression RangeExpression}</dd>
     *   <dt>isSpreadExpression</dt>             <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.SpreadExpression SpreadExpression}</dd>
     *   <dt>isSpreadMapExpression</dt>          <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.SpreadMapExpression SpreadMapExpression}</dd>
     *   <dt>isStaticMethodCallExpression</dt>   <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.StaticMethodCallExpression StaticMethodCallExpression}</dd>
     *   <dt>isTernaryExpression</dt>            <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.TernaryExpression TernaryExpression}</dd>
     *   <dt>isTupleExpression</dt>              <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.TupleExpression TupleExpression}</dd>
     *   <dt>isUnaryMinusExpression</dt>         <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.UnaryMinusExpression UnaryMinusExpression}</dd>
     *   <dt>isUnaryPlusExpression</dt>          <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.UnaryPlusExpression UnaryPlusExpression}</dd>
     *   <dt>isVariableExpression</dt>           <dd>Determines if argument is a  {@link org.codehaus.groovy.ast.expr.VariableExpression VariableExpression}</dd>
     * </dl>
     *
     * General utility:
     * <ul>
     *   <li>Delegates to {@link AbstractTypeCheckingExtension}</li>
     *   <li>Imports static members of {@link org.codehaus.groovy.ast.ClassHelper ClassHelper}</li>
     *   <li>Imports static members of {@link org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport StaticTypeCheckingSupport}</li>
     * </ul>
     *
     * @see <a href="https://docs.groovy-lang.org/latest/html/documentation/#_a_dsl_for_type_checking">Groovy Language Documentation</a>
     */
    public abstract static class TypeCheckingDSL extends Script {
        private GroovyTypeCheckingExtensionSupport extension;

        @Override
        public Object getProperty(final String property) {
            try {
                return InvokerHelper.getProperty(extension, property);
            } catch (Exception e) {
                return super.getProperty(property);
            }
        }

        @Override
        public void setProperty(final String property, final Object newValue) {
            try {
                InvokerHelper.setProperty(extension, property, newValue);
            } catch (Exception e) {
                super.setProperty(property, newValue);
            }
        }

        @Override
        public Object invokeMethod(final String name, final Object args) {
            if (name.startsWith("is") && name.endsWith("Expression") && args instanceof Object[] && ((Object[]) args).length == 1) {
                String type = name.substring(2);
                Object target = ((Object[]) args)[0];
                if (target==null) return false;
                try {
                    Class typeClass = Class.forName("org.codehaus.groovy.ast.expr."+type);
                    return typeClass.isAssignableFrom(target.getClass());
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
            if (args instanceof Object[] && ((Object[]) args).length == 1 && ((Object[]) args)[0] instanceof Closure) {
                Object[] argsArray = (Object[]) args;
                String methodName = METHOD_ALIASES.get(name);
                if (methodName == null) {
                    return InvokerHelper.invokeMethod(extension, name, args);
                }
                List<Closure> closures = extension.eventHandlers.computeIfAbsent(methodName, k -> new LinkedList<Closure>());
                closures.add((Closure) argsArray[0]);
                return null;
            } else {
                return InvokerHelper.invokeMethod(extension, name, args);
            }
        }
    }
}
