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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.IntRange;
import groovy.lang.Tuple2;
import groovy.transform.NamedParam;
import groovy.transform.NamedParams;
import groovy.transform.TypeChecked;
import groovy.transform.TypeCheckingMode;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.ClosureSignatureConflictResolver;
import groovy.transform.stc.ClosureSignatureHint;
import org.apache.groovy.util.SystemUtil;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenUtil;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.apache.groovy.util.BeanUtils.decapitalize;
import static org.codehaus.groovy.ast.ClassHelper.AUTOCLOSEABLE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Character_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.DYNAMIC_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GROOVY_OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GSTRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Iterator_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.PATTERN_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.RANGE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.TUPLE_CLASSES;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.findSAM;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getNextSuperClass;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isFunctionalInterface;
import static org.codehaus.groovy.ast.ClassHelper.isNumberType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isSAMType;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.void_WRAPPER_TYPE;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getResolveStrategyName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.toGenericTypesString;
import static org.codehaus.groovy.ast.tools.WideningCategories.isBigDecCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isBigIntCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isDouble;
import static org.codehaus.groovy.ast.tools.WideningCategories.isDoubleCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isFloat;
import static org.codehaus.groovy.ast.tools.WideningCategories.isFloatingCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isIntCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isLongCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.isNumberCategory;
import static org.codehaus.groovy.ast.tools.WideningCategories.lowestUpperBound;
import static org.codehaus.groovy.classgen.AsmClassGenerator.MINIMUM_BYTECODE_VERSION;
import static org.codehaus.groovy.syntax.Types.ASSIGN;
import static org.codehaus.groovy.syntax.Types.COMPARE_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_IN;
import static org.codehaus.groovy.syntax.Types.COMPARE_NOT_INSTANCEOF;
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL;
import static org.codehaus.groovy.syntax.Types.ELVIS_EQUAL;
import static org.codehaus.groovy.syntax.Types.EQUAL;
import static org.codehaus.groovy.syntax.Types.FIND_REGEX;
import static org.codehaus.groovy.syntax.Types.INTDIV;
import static org.codehaus.groovy.syntax.Types.INTDIV_EQUAL;
import static org.codehaus.groovy.syntax.Types.KEYWORD_IN;
import static org.codehaus.groovy.syntax.Types.KEYWORD_INSTANCEOF;
import static org.codehaus.groovy.syntax.Types.LEFT_SQUARE_BRACKET;
import static org.codehaus.groovy.syntax.Types.MINUS_MINUS;
import static org.codehaus.groovy.syntax.Types.MOD;
import static org.codehaus.groovy.syntax.Types.MOD_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS_PLUS;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.ArrayList_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.Collection_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.Matcher_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.NUMBER_OPS;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.UNKNOWN_PARAMETER_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.addMethodLevelDeclaredGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.allParametersAndArgumentsMatch;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.applyGenericsConnections;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.applyGenericsContext;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.applyGenericsContextToParameterClass;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.boundUnboundedWildcards;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.checkCompatibleAssignmentTypes;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.checkPossibleLossOfPrecision;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.chooseBestMethod;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.evaluateExpression;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.extractGenericsConnections;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.extractGenericsParameterMapOfThis;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.filterMethodsByVisibility;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findSetters;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findTargetVariable;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.fullyResolveType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.getCorrectedClassNode;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.getGenericsWithoutArray;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.getOperationName;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isArrayOp;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isAssignableTo;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isAssignment;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isBeingCompiled;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isBitOperator;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isBoolIntrinsicOp;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isClassClassNodeWrappingConcreteType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isCompareToBoolean;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isOperationInGroup;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isParameterizedWithGStringOrGStringString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isParameterizedWithString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isPowerOperator;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isShiftOperation;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isTraitSelf;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isUsingGenericsOrIsArrayUsingGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isVargs;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isWildcardLeftHandSide;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.lastArgMatchesVarg;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.missesGenericsTypes;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.prettyPrintType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.resolveClassNodeGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.toMethodParametersString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.typeCheckMethodArgumentWithGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.typeCheckMethodsWithGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.CLOSURE_ARGUMENTS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.CONSTRUCTED_LAMBDA_EXPRESSION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DECLARATION_INFERRED_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DELEGATION_METADATA;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DYNAMIC_RESOLUTION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.IMPLICIT_RECEIVER;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_RETURN_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PV_FIELDS_ACCESS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PV_FIELDS_MUTATION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PV_METHODS_ACCESS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.READONLY_PROPERTY;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.SUPER_MOP_METHOD_REQUIRED;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.TYPE;

/**
 * The main class code visitor responsible for static type checking. It will perform various inspections like checking
 * assignment types, type inference, ... Eventually, class nodes may be annotated with inferred type information.
 */
public class StaticTypeCheckingVisitor extends ClassCodeVisitorSupport {

    private static final boolean DEBUG_GENERATED_CODE = SystemUtil.getBooleanSafe("groovy.stc.debug");
    private static final AtomicLong UNIQUE_LONG = new AtomicLong();

    protected static final Object ERROR_COLLECTOR = ErrorCollector.class;
    protected static final ClassNode ITERABLE_TYPE = ClassHelper.make(Iterable.class);
    protected static final List<MethodNode> EMPTY_METHODNODE_LIST = Collections.emptyList();
    protected static final ClassNode TYPECHECKED_CLASSNODE = ClassHelper.make(TypeChecked.class);
    protected static final ClassNode[] TYPECHECKING_ANNOTATIONS = new ClassNode[]{TYPECHECKED_CLASSNODE};
    protected static final ClassNode TYPECHECKING_INFO_NODE = ClassHelper.make(TypeChecked.TypeCheckingInfo.class);
    protected static final ClassNode DGM_CLASSNODE = ClassHelper.make(DefaultGroovyMethods.class);
    protected static final int CURRENT_SIGNATURE_PROTOCOL_VERSION = 1;
    protected static final Expression CURRENT_SIGNATURE_PROTOCOL = new ConstantExpression(CURRENT_SIGNATURE_PROTOCOL_VERSION, true);
    protected static final MethodNode GET_DELEGATE = CLOSURE_TYPE.getGetterMethod("getDelegate");
    protected static final MethodNode GET_OWNER = CLOSURE_TYPE.getGetterMethod("getOwner");
    protected static final MethodNode GET_THISOBJECT = CLOSURE_TYPE.getGetterMethod("getThisObject");
    protected static final ClassNode DELEGATES_TO = ClassHelper.make(DelegatesTo.class);
    protected static final ClassNode DELEGATES_TO_TARGET = ClassHelper.make(DelegatesTo.Target.class);
    protected static final ClassNode LINKEDHASHMAP_CLASSNODE = ClassHelper.make(LinkedHashMap.class);
    protected static final ClassNode CLOSUREPARAMS_CLASSNODE = ClassHelper.make(ClosureParams.class);
    protected static final ClassNode NAMED_PARAMS_CLASSNODE = ClassHelper.make(NamedParams.class);
    protected static final ClassNode MAP_ENTRY_TYPE = ClassHelper.make(Map.Entry.class);
    protected static final ClassNode ENUMERATION_TYPE = ClassHelper.make(Enumeration.class);

    public static final Statement GENERATED_EMPTY_STATEMENT = EmptyStatement.INSTANCE;

    // Cache closure call methods
    public static final MethodNode CLOSURE_CALL_NO_ARG = CLOSURE_TYPE.getDeclaredMethod("call", Parameter.EMPTY_ARRAY);
    public static final MethodNode CLOSURE_CALL_ONE_ARG = CLOSURE_TYPE.getDeclaredMethod("call", new Parameter[]{new Parameter(OBJECT_TYPE, "arg")});
    public static final MethodNode CLOSURE_CALL_VARGS = CLOSURE_TYPE.getDeclaredMethod("call", new Parameter[]{new Parameter(OBJECT_TYPE.makeArray(), "args")});

    protected final ReturnAdder.ReturnStatementListener returnListener = new ReturnAdder.ReturnStatementListener() {
        @Override
        public void returnStatementAdded(final ReturnStatement returnStatement) {
            if (isNullConstant(returnStatement.getExpression())) return;
            checkReturnType(returnStatement);
            if (typeCheckingContext.getEnclosingClosure() != null) {
                addClosureReturnType(getType(returnStatement.getExpression()));
            } else if (typeCheckingContext.getEnclosingMethod() == null) {
                throw new GroovyBugError("Unexpected return statement at " + returnStatement.getLineNumber() + ":" + returnStatement.getColumnNumber() + " " + returnStatement.getText());
            }
        }
    };

    protected final ReturnAdder returnAdder = new ReturnAdder(returnListener);

    protected FieldNode currentField;
    protected PropertyNode currentProperty;
    protected DefaultTypeCheckingExtension extension;
    protected TypeCheckingContext typeCheckingContext;

    public StaticTypeCheckingVisitor(final SourceUnit source, final ClassNode classNode) {
        this.typeCheckingContext = new TypeCheckingContext(this);
        this.typeCheckingContext.pushEnclosingClassNode(classNode);
        this.typeCheckingContext.pushTemporaryTypeInfo();
        this.typeCheckingContext.pushErrorCollector(
                        source.getErrorCollector());
        this.typeCheckingContext.source = source;

        this.extension = new DefaultTypeCheckingExtension(this);
        this.extension.addHandler(new EnumTypeCheckingExtension(this));
        this.extension.addHandler(new TraitTypeCheckingExtension(this));
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return typeCheckingContext.getSource();
    }

    public void initialize() {
        extension.setup();
    }

    /**
     * Returns the current type checking context. The context is used internally by the type
     * checker during type checking to store various state data.
     *
     * @return the type checking context
     */
    public TypeCheckingContext getTypeCheckingContext() {
        return typeCheckingContext;
    }

    public void addTypeCheckingExtension(final TypeCheckingExtension extension) {
        this.extension.addHandler(extension);
    }

    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        typeCheckingContext.setCompilationUnit(compilationUnit);
    }

    @Override
    public void visitClass(final ClassNode node) {
        if (shouldSkipClassNode(node)) return;
        if (!extension.beforeVisitClass(node)) {
            Object type = node.getNodeMetaData(INFERRED_TYPE);
            if (type != null) {
                // transformation has already been run on this class node
                // so use a silent collector in order not to duplicate errors
                typeCheckingContext.pushErrorCollector();
            }
            typeCheckingContext.pushEnclosingClassNode(node);
            Set<MethodNode> oldSet = typeCheckingContext.alreadyVisitedMethods;
            typeCheckingContext.alreadyVisitedMethods = new LinkedHashSet<>();

            super.visitClass(node);
            node.getInnerClasses().forEachRemaining(this::visitClass);

            typeCheckingContext.alreadyVisitedMethods = oldSet;
            typeCheckingContext.popEnclosingClassNode();
            if (type != null) {
                typeCheckingContext.popErrorCollector();
            }

            node.putNodeMetaData(INFERRED_TYPE, node);
            // mark all methods as visited. We can't do this in visitMethod because the type checker
            // works in a two pass sequence and we don't want to skip the second pass
            node.getMethods().forEach(n -> n.putNodeMetaData(StaticTypeCheckingVisitor.class, Boolean.TRUE));
            node.getDeclaredConstructors().forEach(n -> n.putNodeMetaData(StaticTypeCheckingVisitor.class, Boolean.TRUE));
        }
        extension.afterVisitClass(node);
    }

    /**
     * Returns array of type checking annotations. Subclasses may override this
     * method in order to provide additional types which must be looked up when
     * checking if a method or a class node should be skipped.
     * <p>
     * The default implementation returns {@link TypeChecked}.
     */
    protected ClassNode[] getTypeCheckingAnnotations() {
        return TYPECHECKING_ANNOTATIONS;
    }

    protected boolean shouldSkipClassNode(final ClassNode node) {
        return Boolean.TRUE.equals(node.getNodeMetaData(StaticTypeCheckingVisitor.class)) || isSkipMode(node);
    }

    public boolean isSkipMode(final AnnotatedNode node) {
        if (node == null) return false;
        for (ClassNode tca : getTypeCheckingAnnotations()) {
            List<AnnotationNode> annotations = node.getAnnotations(tca);
            if (annotations != null) {
                for (AnnotationNode annotation : annotations) {
                    Expression value = annotation.getMember("value");
                    if (value != null) {
                        if (value instanceof ConstantExpression) {
                            ConstantExpression ce = (ConstantExpression) value;
                            if (TypeCheckingMode.SKIP.toString().equals(ce.getValue().toString())) return true;
                        } else if (value instanceof PropertyExpression) {
                            PropertyExpression pe = (PropertyExpression) value;
                            if (TypeCheckingMode.SKIP.toString().equals(pe.getPropertyAsString())) return true;
                        }
                    }
                }
            }
        }
        if (node instanceof MethodNode) {
            return isSkipMode(node.getDeclaringClass());
        }
        return isSkippedInnerClass(node);
    }

    /**
     * Tests if a node is an inner class node, and if it is, then checks if the enclosing method is skipped.
     *
     * @return true if the inner class node should be skipped
     */
    protected boolean isSkippedInnerClass(final AnnotatedNode node) {
        if (node instanceof ClassNode) {
            ClassNode type = (ClassNode) node;
            if (type.getOuterClass() != null) {
                MethodNode enclosingMethod = type.getEnclosingMethod();
                if (enclosingMethod != null && isSkipMode(enclosingMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        super.visitClassExpression(expression);
        ClassNode cn = expression.getNodeMetaData(INFERRED_TYPE);
        if (cn == null) {
            storeType(expression, getType(expression));
        }
    }

    private static ClassNode getOutermost(ClassNode cn) {
        while (cn.getOuterClass() != null) {
            cn = cn.getOuterClass();
        }
        return cn;
    }

    private static void addPrivateFieldOrMethodAccess(final Expression source, final ClassNode cn, final StaticTypesMarker key, final ASTNode accessedMember) {
        cn.getNodeMetaData(key, x -> new LinkedHashSet<>()).add(accessedMember);
        source.putNodeMetaData(key, accessedMember);
    }

    /**
     * Checks for private field access from inner or outer class.
     */
    private void checkOrMarkPrivateAccess(final Expression source, final FieldNode fn, final boolean lhsOfAssignment) {
        if (fn == null || !fn.isPrivate()) return;
        ClassNode declaringClass = fn.getDeclaringClass();
        ClassNode enclosingClass = typeCheckingContext.getEnclosingClassNode();
        if (declaringClass == enclosingClass && typeCheckingContext.getEnclosingClosure() == null) return;

        if (declaringClass == enclosingClass || getOutermost(declaringClass) == getOutermost(enclosingClass)) {
            StaticTypesMarker accessKind = lhsOfAssignment ? PV_FIELDS_MUTATION : PV_FIELDS_ACCESS;
            addPrivateFieldOrMethodAccess(source, declaringClass, accessKind, fn);
        }
    }

    /**
     * Checks for private method call from inner or outer class.
     */
    private void checkOrMarkPrivateAccess(final Expression source, final MethodNode mn) {
        if (mn == null) {
            return;
        }
        ClassNode declaringClass = mn.getDeclaringClass();
        ClassNode enclosingClassNode = typeCheckingContext.getEnclosingClassNode();
        if (declaringClass != enclosingClassNode || typeCheckingContext.getEnclosingClosure() != null) {
            int mods = mn.getModifiers();
            boolean sameModule = declaringClass.getModule() == enclosingClassNode.getModule();
            String packageName = declaringClass.getPackageName();
            if (packageName == null) {
                packageName = "";
            }
            if ((Modifier.isPrivate(mods) && sameModule)) {
                addPrivateFieldOrMethodAccess(source, declaringClass, PV_METHODS_ACCESS, mn);
            } else if (Modifier.isProtected(mods) && !packageName.equals(enclosingClassNode.getPackageName())
                    && !implementsInterfaceOrIsSubclassOf(enclosingClassNode, declaringClass)) {
                ClassNode cn = enclosingClassNode;
                while ((cn = cn.getOuterClass()) != null) {
                    if (implementsInterfaceOrIsSubclassOf(cn, declaringClass)) {
                        addPrivateFieldOrMethodAccess(source, cn, PV_METHODS_ACCESS, mn);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void visitVariableExpression(final VariableExpression vexp) {
        super.visitVariableExpression(vexp);
        if (storeTypeForSuper(vexp)) return;
        if (storeTypeForThis(vexp)) return;

        final String name = vexp.getName();
        final Variable accessedVariable = vexp.getAccessedVariable();
        final TypeCheckingContext.EnclosingClosure enclosingClosure = typeCheckingContext.getEnclosingClosure();

        if (accessedVariable == null) {
            return;
        }

        if (accessedVariable instanceof DynamicVariable) {
            // a dynamic variable is either a closure property, a class member referenced from a closure, or an undeclared variable

            if (enclosingClosure != null) {
                switch (name) {
                    case "delegate":
                        DelegationMetadata dm = getDelegationMetadata(enclosingClosure.getClosureExpression());
                        if (dm != null) {
                            storeType(vexp, dm.getType());
                            return;
                        }
                        // falls through
                    case "owner":
                        if (typeCheckingContext.getEnclosingClosureStack().size() > 1) {
                            storeType(vexp, CLOSURE_TYPE);
                            return;
                        }
                        // falls through
                    case "thisObject":
                        storeType(vexp, typeCheckingContext.getEnclosingClassNode());
                        return;
                    case "parameterTypes":
                        storeType(vexp, CLASS_Type.makeArray());
                        return;
                    case "maximumNumberOfParameters":
                    case "resolveStrategy":
                    case "directive":
                        storeType(vexp, int_TYPE);
                        return;
                }
            }

            if (tryVariableExpressionAsProperty(vexp, name)) return;

            if (!extension.handleUnresolvedVariableExpression(vexp)) {
                addStaticTypeError("The variable [" + name + "] is undeclared.", vexp);
            }
        } else if (accessedVariable instanceof FieldNode) {
            if (enclosingClosure != null) {
                tryVariableExpressionAsProperty(vexp, name);
            } else {
                checkOrMarkPrivateAccess(vexp, (FieldNode) accessedVariable, typeCheckingContext.isTargetOfEnclosingAssignment(vexp));

                // GROOVY-9454
                ClassNode inferredType = getInferredTypeFromTempInfo(vexp, null);
                if (inferredType != null && !inferredType.equals(OBJECT_TYPE)) {
                    vexp.putNodeMetaData(INFERRED_RETURN_TYPE, inferredType);
                } else {
                    storeType(vexp, getType(vexp));
                }
            }
        } else if (accessedVariable instanceof PropertyNode) {
            // we must be careful, because the property node may be of a wrong type:
            // if a class contains a getter and a setter of different types or
            // overloaded setters, the type of the property node is arbitrary!
            if (tryVariableExpressionAsProperty(vexp, name)) {
                BinaryExpression enclosingBinaryExpression = typeCheckingContext.getEnclosingBinaryExpression();
                if (enclosingBinaryExpression != null) {
                    Expression leftExpression = enclosingBinaryExpression.getLeftExpression();
                    SetterInfo setterInfo = removeSetterInfo(leftExpression);
                    if (setterInfo != null) {
                        Expression rightExpression = enclosingBinaryExpression.getRightExpression();
                        ensureValidSetter(vexp, leftExpression, rightExpression, setterInfo);
                    }
                }
            }
        } else if (enclosingClosure == null) {
            VariableExpression localVariable;
            if (accessedVariable instanceof Parameter) {
                Parameter parameter = (Parameter) accessedVariable;
                localVariable = new ParameterVariableExpression(parameter);
            } else {
                localVariable = (VariableExpression) accessedVariable;
            }

            ClassNode inferredType = localVariable.getNodeMetaData(INFERRED_TYPE);
            inferredType = getInferredTypeFromTempInfo(localVariable, inferredType);
            if (inferredType != null && !inferredType.equals(OBJECT_TYPE)) {
                vexp.putNodeMetaData(INFERRED_RETURN_TYPE, inferredType);
            }
        }
    }

    private boolean storeTypeForSuper(final VariableExpression vexp) {
        if (vexp == VariableExpression.SUPER_EXPRESSION) return true;
        if (!vexp.isSuperExpression()) return false;
        storeType(vexp, makeSuper());
        return true;
    }

    private boolean storeTypeForThis(final VariableExpression vexp) {
        if (vexp == VariableExpression.THIS_EXPRESSION) return true;
        if (!vexp.isThisExpression()) return false;
        // GROOVY-6904, GROOVY-9422: non-static inner class constructor call sets type
        storeType(vexp, !OBJECT_TYPE.equals(vexp.getType()) ? vexp.getType() : makeThis());
        return true;
    }

    private boolean tryVariableExpressionAsProperty(final VariableExpression vexp, final String dynName) {
        PropertyExpression pexp = thisPropX(true, dynName);
        if (existsProperty(pexp, !typeCheckingContext.isTargetOfEnclosingAssignment(vexp))) {
            vexp.copyNodeMetaData(pexp.getObjectExpression());
            for (Object key : new Object[]{IMPLICIT_RECEIVER, READONLY_PROPERTY, PV_FIELDS_ACCESS, PV_FIELDS_MUTATION, DECLARATION_INFERRED_TYPE, DIRECT_METHOD_CALL_TARGET}) {
                Object val = pexp.getNodeMetaData(key);
                if (val != null) vexp.putNodeMetaData(key, val);
            }
            vexp.removeNodeMetaData(INFERRED_TYPE);
            ClassNode type = pexp.getNodeMetaData(INFERRED_TYPE);
            storeType(vexp, Optional.ofNullable(type).orElseGet(pexp::getType));

            String receiver = vexp.getNodeMetaData(IMPLICIT_RECEIVER);
            // GROOVY-7701: correct false assumption made by VariableScopeVisitor
            if (receiver != null && !receiver.endsWith("owner") && !(vexp.getAccessedVariable() instanceof DynamicVariable)) {
                vexp.setAccessedVariable(new DynamicVariable(dynName, false));
            }
            return true;
        }
        return false;
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        if (existsProperty(expression, !typeCheckingContext.isTargetOfEnclosingAssignment(expression))) return;

        if (!extension.handleUnresolvedProperty(expression)) {
            Expression objectExpression = expression.getObjectExpression();
            addStaticTypeError("No such property: " + expression.getPropertyAsString() + " for class: " +
                    findCurrentInstanceOfClass(objectExpression, getType(objectExpression)).toString(false), expression);
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        if (existsProperty(expression, true)) return;

        if (!extension.handleUnresolvedAttribute(expression)) {
            Expression objectExpression = expression.getObjectExpression();
            addStaticTypeError("No such attribute: " + expression.getPropertyAsString() + " for class: " +
                    findCurrentInstanceOfClass(objectExpression, getType(objectExpression)).toString(false), expression);
        }
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        super.visitRangeExpression(expression);
        ClassNode fromType = getWrapper(getType(expression.getFrom()));
        ClassNode toType = getWrapper(getType(expression.getTo()));
        if (Integer_TYPE.equals(fromType) && Integer_TYPE.equals(toType)) {
            storeType(expression, ClassHelper.make(IntRange.class));
        } else {
            ClassNode rangeType = RANGE_TYPE.getPlainNodeReference();
            rangeType.setGenericsTypes(new GenericsType[]{new GenericsType(WideningCategories.lowestUpperBound(fromType, toType))});
            storeType(expression, rangeType);
        }
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        // GROOVY-9455: !(x instanceof T) shouldn't propagate T as inferred type
        typeCheckingContext.pushTemporaryTypeInfo();
        super.visitNotExpression(expression);
        typeCheckingContext.popTemporaryTypeInfo();
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        BinaryExpression enclosingBinaryExpression = typeCheckingContext.getEnclosingBinaryExpression();
        typeCheckingContext.pushEnclosingBinaryExpression(expression);
        try {
            int op = expression.getOperation().getType();
            Expression leftExpression = expression.getLeftExpression();
            Expression rightExpression = expression.getRightExpression();

            leftExpression.visit(this);
            SetterInfo setterInfo = removeSetterInfo(leftExpression);
            ClassNode lType = null;
            if (setterInfo != null) {
                if (ensureValidSetter(expression, leftExpression, rightExpression, setterInfo)) {
                    return;
                }
            } else {
                lType = getType(leftExpression);
                boolean isFunctionalInterface = isFunctionalInterface(lType);
                if (isFunctionalInterface && rightExpression instanceof MethodReferenceExpression) {
                    LambdaExpression lambdaExpression = constructLambdaExpressionForMethodReference(lType);
                    if (op == ASSIGN) {
                        inferParameterAndReturnTypesOfClosureOnRHS(lType, lambdaExpression);
                    }
                    rightExpression.putNodeMetaData(CONSTRUCTED_LAMBDA_EXPRESSION, lambdaExpression);
                    rightExpression.putNodeMetaData(CLOSURE_ARGUMENTS, Arrays.stream(lambdaExpression.getParameters()).map(Parameter::getType).toArray(ClassNode[]::new));

                } else if (op == ASSIGN && isFunctionalInterface && rightExpression instanceof ClosureExpression) {
                    inferParameterAndReturnTypesOfClosureOnRHS(lType, (ClosureExpression) rightExpression);
                }

                rightExpression.visit(this);
            }

            if (lType == null) lType = getType(leftExpression);
            ClassNode rType = (isNullConstant(rightExpression) && !isPrimitiveType(lType)
                    // primitive types should be ignored as they will result in another failure
                    ? UNKNOWN_PARAMETER_TYPE
                    : getType(rightExpression)
            );

            BinaryExpression reversedBinaryExpression = binX(rightExpression, expression.getOperation(), leftExpression);
            ClassNode resultType = (op == KEYWORD_IN || op == COMPARE_NOT_IN)
                    ? getResultType(rType, op, lType, reversedBinaryExpression)
                    : getResultType(lType, op, rType, expression);
            if (op == KEYWORD_IN || op == COMPARE_NOT_IN) {
                // in case of the "in" operator, the receiver and the arguments are reversed
                // so we use the reversedExpression and get the target method from it
                storeTargetMethod(expression, reversedBinaryExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET));
            } else if (op == LEFT_SQUARE_BRACKET
                    && leftExpression instanceof VariableExpression
                    && leftExpression.getNodeMetaData(INFERRED_TYPE) == null) {
                storeType(leftExpression, lType);
            } else if (op == ELVIS_EQUAL) {
                ElvisOperatorExpression elvisOperatorExpression = new ElvisOperatorExpression(leftExpression, rightExpression);
                elvisOperatorExpression.setSourcePosition(expression);
                elvisOperatorExpression.visit(this);
                resultType = getType(elvisOperatorExpression);
                storeType(leftExpression, resultType);
            }

            if (resultType == null) {
                resultType = lType;
            }

            // GROOVY-5874: if left expression is a closure shared variable, a second pass should be done
            if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).isClosureSharedVariable()) {
                typeCheckingContext.secondPassExpressions.add(new SecondPassExpression(expression));
            }

            boolean isAssignment = isAssignment(expression.getOperation().getType());
            if (isAssignment && lType.isUsingGenerics() && missesGenericsTypes(resultType)) {
                // unchecked assignment
                // examples:
                // List<A> list = []
                // List<A> list = new LinkedList()
                // Iterable<A> list = new LinkedList()

                // in that case, the inferred type of the binary expression is the type of the RHS
                // "completed" with generics type information available in the LHS
                ClassNode completedType = GenericsUtils.parameterizeType(lType, resultType.getPlainNodeReference());

                resultType = completedType;
            }

            if (isArrayOp(op)
                    && !lType.isArray()
                    && enclosingBinaryExpression != null
                    && enclosingBinaryExpression.getLeftExpression() == expression
                    && isAssignment(enclosingBinaryExpression.getOperation().getType())) {
                // left hand side of an assignment : map['foo'] = ...
                Expression enclosingBE_rightExpr = enclosingBinaryExpression.getRightExpression();
                if (!(enclosingBE_rightExpr instanceof ClosureExpression)) {
                    enclosingBE_rightExpr.visit(this);
                }
                ClassNode[] arguments = {rType, getType(enclosingBE_rightExpr)};
                List<MethodNode> nodes = findMethod(lType.redirect(), "putAt", arguments);
                if (nodes.size() == 1) {
                    typeCheckMethodsWithGenericsOrFail(lType, arguments, nodes.get(0), enclosingBE_rightExpr);
                } else if (nodes.isEmpty()) {
                    addNoMatchingMethodError(lType, "putAt", arguments, enclosingBinaryExpression);
                }
            }

            boolean isEmptyDeclaration = (expression instanceof DeclarationExpression && rightExpression instanceof EmptyExpression);
            if (isAssignment && !isEmptyDeclaration) {
                if (rightExpression instanceof ConstructorCallExpression) {
                    inferDiamondType((ConstructorCallExpression) rightExpression, lType);
                }

                ClassNode originType = getOriginalDeclarationType(leftExpression);
                typeCheckAssignment(expression, leftExpression, originType, rightExpression, resultType);
                // if assignment succeeds but result type is not a subtype of original type, then we are in a special cast handling
                // and we must update the result type
                if (!implementsInterfaceOrIsSubclassOf(getWrapper(resultType), getWrapper(originType))) {
                    resultType = originType;
                } else if (lType.isUsingGenerics() && !lType.isEnum() && hasRHSIncompleteGenericTypeInfo(resultType)) {
                    // for example, LHS is List<ConcreteClass> and RHS is List<T> where T is a placeholder
                    resultType = lType;
                } else {
                    // GROOVY-7549: RHS type may not be accessible to enclosing class
                    int modifiers = resultType.getModifiers();
                    ClassNode enclosingType = typeCheckingContext.getEnclosingClassNode();
                    if (!Modifier.isPublic(modifiers) && !enclosingType.equals(resultType)
                            && !getOutermost(enclosingType).equals(getOutermost(resultType))
                            && (Modifier.isPrivate(modifiers) || !Objects.equals(enclosingType.getPackageName(), resultType.getPackageName()))) {
                        resultType = originType; // TODO: Find accesible type in hierarchy of resultType?
                    }
                }

                // make sure we keep primitive types
                if (isPrimitiveType(originType) && resultType.equals(getWrapper(originType))) {
                    resultType = originType;
                }

                // if we are in an if/else branch, keep track of assignment
                if (typeCheckingContext.ifElseForWhileAssignmentTracker != null && leftExpression instanceof VariableExpression
                        && !isNullConstant(rightExpression)) {
                    Variable accessedVariable = ((VariableExpression) leftExpression).getAccessedVariable();
                    if (accessedVariable instanceof Parameter) {
                        accessedVariable = new ParameterVariableExpression((Parameter) accessedVariable);
                    }
                    if (accessedVariable instanceof VariableExpression) {
                        VariableExpression var = (VariableExpression) accessedVariable;
                        List<ClassNode> types = typeCheckingContext.ifElseForWhileAssignmentTracker.get(var);
                        if (types == null) {
                            types = new LinkedList<>();
                            ClassNode type = var.getNodeMetaData(INFERRED_TYPE);
                            types.add(type);
                            typeCheckingContext.ifElseForWhileAssignmentTracker.put(var, types);
                        }
                        types.add(resultType);
                    }
                }
                storeType(leftExpression, resultType);

                // if right expression is a ClosureExpression, store parameter type information
                if (leftExpression instanceof VariableExpression) {
                    if (rightExpression instanceof ClosureExpression) {
                        leftExpression.putNodeMetaData(CLOSURE_ARGUMENTS, ((ClosureExpression) rightExpression).getParameters());
                    } else if (rightExpression instanceof VariableExpression
                            && ((VariableExpression) rightExpression).getAccessedVariable() instanceof Expression
                            && ((Expression) ((VariableExpression) rightExpression).getAccessedVariable()).getNodeMetaData(CLOSURE_ARGUMENTS) != null) {
                        Variable targetVariable = findTargetVariable((VariableExpression) leftExpression);
                        if (targetVariable instanceof ASTNode) {
                            ((ASTNode) targetVariable).putNodeMetaData(CLOSURE_ARGUMENTS, ((Expression) ((VariableExpression) rightExpression).getAccessedVariable()).getNodeMetaData(CLOSURE_ARGUMENTS));
                        }
                    }
                }
            } else if (op == KEYWORD_INSTANCEOF /*|| op == COMPARE_NOT_INSTANCEOF*/) {
                pushInstanceOfTypeInfo(leftExpression, rightExpression);
            }
            if (!isEmptyDeclaration) {
                storeType(expression, resultType);
            }

            validateResourceInARM(expression, resultType);
        } finally {
            typeCheckingContext.popEnclosingBinaryExpression();
        }
    }

    private void validateResourceInARM(final BinaryExpression expression, final ClassNode lType) {
        if (expression instanceof DeclarationExpression) {
            if (TryCatchStatement.isResource(expression)) {
                if (!lType.implementsInterface(AUTOCLOSEABLE_TYPE)) {
                    addError("Resource[" + lType.getName() + "] in ARM should be of type AutoCloseable", expression);
                }
            }
        }
    }

    private void inferParameterAndReturnTypesOfClosureOnRHS(final ClassNode lhsType, final ClosureExpression rhsExpression) {
        Tuple2<ClassNode[], ClassNode> typeInfo = GenericsUtils.parameterizeSAM(lhsType);
        Parameter[] closureParameters = getParametersSafe(rhsExpression);
        ClassNode[] parameterTypes = typeInfo.getV1();

        int n = closureParameters.length;
        if (n == parameterTypes.length) {
            for (int i = 0; i < n; i += 1) {
                Parameter parameter = closureParameters[i];
                if (parameter.isDynamicTyped()) {
                    parameter.setType(parameterTypes[i]);
                    parameter.setOriginType(parameterTypes[i]);
                }
            }
        } else {
            addStaticTypeError("Wrong number of parameters: ", rhsExpression);
        }

        storeInferredReturnType(rhsExpression, typeInfo.getV2());
    }

    /**
     * Given a binary expression corresponding to an assignment, will check that
     * the type of the RHS matches one of the possible setters and if not, throw
     * a type checking error.
     *
     * @param expression      the assignment expression
     * @param leftExpression  left expression of the assignment
     * @param rightExpression right expression of the assignment
     * @param setterInfo      possible setters
     * @return {@code false} if valid setter found or {@code true} if type checking error created
     */
    private boolean ensureValidSetter(final Expression expression, final Expression leftExpression, final Expression rightExpression, final SetterInfo setterInfo) {
        // for expressions like foo = { ... }
        // we know that the RHS type is a closure
        // but we must check if the binary expression is an assignment
        // because we need to check if a setter uses @DelegatesTo
        VariableExpression ve = varX("%", setterInfo.receiverType);
        // for compound assignment "x op= y" find type as if it was "x = (x op y)"
        Expression newRightExpression = isCompoundAssignment(expression)
                ? binX(leftExpression, getOpWithoutEqual(expression), rightExpression)
                : rightExpression;
        MethodCallExpression call = callX(ve, setterInfo.name, newRightExpression);
        call.setImplicitThis(false);
        visitMethodCallExpression(call);
        MethodNode directSetterCandidate = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (directSetterCandidate == null) {
            // this may happen if there's a setter of type boolean/String/Class, and that we are using the property
            // notation AND that the RHS is not a boolean/String/Class
            for (MethodNode setter : setterInfo.setters) {
                ClassNode type = getWrapper(setter.getParameters()[0].getOriginType());
                if (Boolean_TYPE.equals(type) || STRING_TYPE.equals(type) || CLASS_Type.equals(type)) {
                    call = callX(ve, setterInfo.name, castX(type, newRightExpression));
                    call.setImplicitThis(false);
                    visitMethodCallExpression(call);
                    directSetterCandidate = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
                    if (directSetterCandidate != null) {
                        break;
                    }
                }
            }
        }
        if (directSetterCandidate != null) {
            for (MethodNode setter : setterInfo.setters) {
                if (setter == directSetterCandidate) {
                    leftExpression.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, directSetterCandidate);
                    leftExpression.removeNodeMetaData(INFERRED_TYPE); // clear assumption
                    storeType(leftExpression, getType(newRightExpression));
                    break;
                }
            }
            return false;
        } else {
            ClassNode firstSetterType = setterInfo.setters.get(0).getParameters()[0].getOriginType();
            addAssignmentError(firstSetterType, getType(newRightExpression), expression);
            return true;
        }
    }

    private boolean isCompoundAssignment(final Expression exp) {
        if (!(exp instanceof BinaryExpression)) return false;
        int type = ((BinaryExpression) exp).getOperation().getType();
        return isAssignment(type) && type != ASSIGN;
    }

    private Token getOpWithoutEqual(final Expression exp) {
        if (!(exp instanceof BinaryExpression)) return null; // should never happen
        Token op = ((BinaryExpression) exp).getOperation();
        int typeWithoutEqual = TokenUtil.removeAssignment(op.getType());
        return new Token(typeWithoutEqual, op.getText() /* will do */, op.getStartLine(), op.getStartColumn());
    }

    protected ClassNode getOriginalDeclarationType(final Expression lhs) {
        if (lhs instanceof VariableExpression) {
            Variable var = findTargetVariable((VariableExpression) lhs);
            if (var instanceof PropertyNode) {
                // Do NOT trust the type of the property node!
                return getType(lhs);
            }
            if (var instanceof DynamicVariable) return getType(lhs);
            return var.getOriginType();
        }
        if (lhs instanceof FieldExpression) {
            return ((FieldExpression) lhs).getField().getOriginType();
        }
        return getType(lhs);
    }

    protected void inferDiamondType(final ConstructorCallExpression cce, final ClassNode lType) {
        // check if constructor call expression makes use of the diamond operator
        ClassNode node = cce.getType();
        if (node.isUsingGenerics() && node.getGenericsTypes() != null && node.getGenericsTypes().length == 0) {
            ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(cce.getArguments());
            if (argumentListExpression.getExpressions().isEmpty()) {
                adjustGenerics(lType, node);
            } else {
                ClassNode type = getType(argumentListExpression.getExpression(0));
                if (type.isUsingGenerics()) {
                    adjustGenerics(type, node);
                }
            }
            // store inferred type on CCE
            storeType(cce, node);
        }
    }

    private void adjustGenerics(final ClassNode from, final ClassNode to) {
        GenericsType[] genericsTypes = from.getGenericsTypes();
        if (genericsTypes == null) {
            // case of: def foo = new HashMap<>()
            genericsTypes = to.redirect().getGenericsTypes();
        }
        GenericsType[] copy = new GenericsType[genericsTypes.length];
        for (int i = 0; i < genericsTypes.length; i++) {
            GenericsType genericsType = genericsTypes[i];
            copy[i] = new GenericsType(
                    wrapTypeIfNecessary(genericsType.getType()),
                    genericsType.getUpperBounds(),
                    genericsType.getLowerBound()
            );
        }
        to.setGenericsTypes(copy);
    }

    /**
     * Stores information about types when [objectOfInstanceof instanceof typeExpression] is visited.
     *
     * @param objectOfInstanceOf the expression which must be checked against instanceof
     * @param typeExpression     the expression which represents the target type
     */
    protected void pushInstanceOfTypeInfo(final Expression objectOfInstanceOf, final Expression typeExpression) {
        List<ClassNode> potentialTypes = typeCheckingContext.temporaryIfBranchTypeInformation.peek()
            .computeIfAbsent(extractTemporaryTypeInfoKey(objectOfInstanceOf), key -> new LinkedList<>());
        potentialTypes.add(typeExpression.getType());
    }

    private boolean typeCheckMultipleAssignmentAndContinue(final Expression leftExpression, Expression rightExpression) {
        // multiple assignment check
        if (!(leftExpression instanceof TupleExpression)) return true;

        Expression transformedRightExpression = transformRightExpressionToSupportMultipleAssignment(rightExpression);
        if (transformedRightExpression == null) {
            addStaticTypeError("Multiple assignments without list expressions on the right hand side are unsupported in static type checking mode", rightExpression);
            return false;
        }

        rightExpression = transformedRightExpression;

        TupleExpression tuple = (TupleExpression) leftExpression;
        ListExpression list = (ListExpression) rightExpression;
        List<Expression> listExpressions = list.getExpressions();
        List<Expression> tupleExpressions = tuple.getExpressions();
        if (listExpressions.size() < tupleExpressions.size()) {
            addStaticTypeError("Incorrect number of values. Expected:" + tupleExpressions.size() + " Was:" + listExpressions.size(), list);
            return false;
        }
        for (int i = 0, tupleExpressionsSize = tupleExpressions.size(); i < tupleExpressionsSize; i++) {
            Expression tupleExpression = tupleExpressions.get(i);
            Expression listExpression = listExpressions.get(i);
            ClassNode elemType = getType(listExpression);
            ClassNode tupleType = getType(tupleExpression);
            if (!isAssignableTo(elemType, tupleType)) {
                addStaticTypeError("Cannot assign value of type " + elemType.toString(false) + " to variable of type " + tupleType.toString(false), rightExpression);
                return false; // avoids too many errors
            } else {
                storeType(tupleExpression, elemType);
            }
        }

        return true;
    }

    private Expression transformRightExpressionToSupportMultipleAssignment(final Expression rightExpression) {
        if (rightExpression instanceof ListExpression) {
            return rightExpression;
        }

        ClassNode cn = null;
        if (rightExpression instanceof MethodCallExpression || rightExpression instanceof ConstructorCallExpression || rightExpression instanceof VariableExpression) {
            ClassNode inferredType = getType(rightExpression);
            cn = (inferredType == null ? rightExpression.getType() : inferredType);
        }

        if (cn == null) {
            return null;
        }

        for (int i = 0, n = TUPLE_CLASSES.length; i < n; i += 1) {
            Class<?> tcn = TUPLE_CLASSES[i];
            if (tcn.equals(cn.getTypeClass())) {
                ListExpression listExpression = new ListExpression();
                GenericsType[] genericsTypes = cn.getGenericsTypes();
                for (int j = 0; j < i; j += 1) {
                    // the index of element in tuple starts with 1
                    MethodCallExpression mce = new MethodCallExpression(rightExpression, "getV" + (j + 1), ArgumentListExpression.EMPTY_ARGUMENTS);
                    ClassNode elementType = (genericsTypes != null ? genericsTypes[j].getType() : OBJECT_TYPE);
                    mce.setType(elementType);
                    storeType(mce, elementType);
                    listExpression.addExpression(mce);
                }

                listExpression.setSourcePosition(rightExpression);

                return listExpression;
            }
        }

        return null;
    }

    private static ClassNode adjustTypeForSpreading(final ClassNode inferredRightExpressionType, final Expression leftExpression) {
        // imagine we have: list*.foo = 100
        // then the assignment must be checked against [100], not 100
        ClassNode wrappedRHS = inferredRightExpressionType;
        if (leftExpression instanceof PropertyExpression && ((PropertyExpression) leftExpression).isSpreadSafe()) {
            wrappedRHS = LIST_TYPE.getPlainNodeReference();
            wrappedRHS.setGenericsTypes(new GenericsType[]{
                    new GenericsType(getWrapper(inferredRightExpressionType))
            });
        }
        return wrappedRHS;
    }

    private boolean addedReadOnlyPropertyError(final Expression expr) {
        // if expr is of READONLY_PROPERTY_RETURN type, then it means we are on a missing property
        if (expr.getNodeMetaData(READONLY_PROPERTY) == null) return false;
        String name;
        if (expr instanceof VariableExpression) {
            name = ((VariableExpression) expr).getName();
        } else {
            name = ((PropertyExpression) expr).getPropertyAsString();
        }
        addStaticTypeError("Cannot set read-only property: " + name, expr);
        return true;
    }

    private void addPrecisionErrors(final ClassNode leftRedirect, final ClassNode lhsType, final ClassNode rhsType, final Expression rightExpression) {
        if (isNumberType(leftRedirect)) {
            if (isNumberType(rhsType) && checkPossibleLossOfPrecision(leftRedirect, rhsType, rightExpression)) {
                addStaticTypeError("Possible loss of precision from " + rhsType.toString(false) + " to " + lhsType.toString(false), rightExpression);
            }
            return;
        }
        if (!leftRedirect.isArray()) return;
        // left type is an array, check the right component types
        if (rightExpression instanceof ListExpression) {
            ClassNode leftComponentType = leftRedirect.getComponentType();
            for (Expression expression : ((ListExpression) rightExpression).getExpressions()) {
                ClassNode rightComponentType = getType(expression);
                if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType) && !(isNullConstant(expression) && !isPrimitiveType(leftComponentType))) {
                    addStaticTypeError("Cannot assign value of type " + rightComponentType.toString(false) + " into array of type " + lhsType.toString(false), rightExpression);
                }
            }
        } else if (rhsType.redirect().isArray()) {
            ClassNode leftComponentType = leftRedirect.getComponentType();
            ClassNode rightComponentType = rhsType.redirect().getComponentType();
            if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType)) {
                addStaticTypeError("Cannot assign value of type " + rightComponentType.toString(false) + " into array of type " + lhsType.toString(false), rightExpression);
            }
        }
    }

    private void addListAssignmentConstructorErrors(final ClassNode leftRedirect, final ClassNode leftExpressionType, final ClassNode inferredRightExpressionType, final Expression rightExpression, final Expression assignmentExpression) {
        // if left type is not a list but right type is a list, then we're in the case of a groovy
        // constructor type : Dimension d = [100,200]
        // In that case, more checks can be performed
        if (rightExpression instanceof ListExpression && !implementsInterfaceOrIsSubclassOf(LIST_TYPE, leftRedirect)) {
            ArgumentListExpression argList = args(((ListExpression) rightExpression).getExpressions());
            ClassNode[] args = getArgumentTypes(argList);
            MethodNode methodNode = checkGroovyStyleConstructor(leftRedirect, args, assignmentExpression);
            if (methodNode != null) {
                rightExpression.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, methodNode);
            }
        } else if (!implementsInterfaceOrIsSubclassOf(inferredRightExpressionType, leftRedirect)
                && implementsInterfaceOrIsSubclassOf(inferredRightExpressionType, LIST_TYPE)
                && !isWildcardLeftHandSide(leftExpressionType)) {
            if (!extension.handleIncompatibleAssignment(leftExpressionType, inferredRightExpressionType, assignmentExpression)) {
                addAssignmentError(leftExpressionType, inferredRightExpressionType, assignmentExpression);
            }
        }
    }

    private void addMapAssignmentConstructorErrors(final ClassNode leftRedirect, final Expression leftExpression, final Expression rightExpression) {
        if (!(rightExpression instanceof MapExpression) || (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).isDynamicTyped())
                || leftRedirect.equals(OBJECT_TYPE) || implementsInterfaceOrIsSubclassOf(leftRedirect, MAP_TYPE)) {
            return;
        }

        // groovy constructor shorthand: A a = [x:2, y:3]
        ClassNode[] argTypes = getArgumentTypes(args(rightExpression));
        checkGroovyStyleConstructor(leftRedirect, argTypes, rightExpression);
        // perform additional type checking on arguments
        MapExpression mapExpression = (MapExpression) rightExpression;
        checkGroovyConstructorMap(leftExpression, leftRedirect, mapExpression);
    }

    private void checkTypeGenerics(final ClassNode leftExpressionType, final ClassNode wrappedRHS, final Expression rightExpression) {
        // last, check generic type information to ensure that inferred types are compatible
        if (!leftExpressionType.isUsingGenerics()) return;
        // List<Foo> l = new List() is an example for incomplete generics type info
        // we assume arity related errors are already handled here.
        if (hasRHSIncompleteGenericTypeInfo(wrappedRHS)) return;

        GenericsType gt = GenericsUtils.buildWildcardType(leftExpressionType);
        if (UNKNOWN_PARAMETER_TYPE.equals(wrappedRHS) ||
                gt.isCompatibleWith(wrappedRHS) ||
                isNullConstant(rightExpression)) return;

        addStaticTypeError("Incompatible generic argument types. Cannot assign "
                + wrappedRHS.toString(false)
                + " to: " + leftExpressionType.toString(false), rightExpression);
    }

    private boolean hasGStringStringError(final ClassNode leftExpressionType, final ClassNode wrappedRHS, final Expression rightExpression) {
        if (isParameterizedWithString(leftExpressionType) && isParameterizedWithGStringOrGStringString(wrappedRHS)) {
            addStaticTypeError("You are trying to use a GString in place of a String in a type which explicitly declares accepting String. " +
                    "Make sure to call toString() on all GString values.", rightExpression);
            return true;
        }
        return false;
    }

    protected void typeCheckAssignment(final BinaryExpression assignmentExpression, final Expression leftExpression, final ClassNode leftExpressionType, final Expression rightExpression, final ClassNode inferredRightExpressionTypeOrig) {
        ClassNode inferredRightExpressionType = inferredRightExpressionTypeOrig;
        if (!typeCheckMultipleAssignmentAndContinue(leftExpression, rightExpression)) return;

        // TODO: need errors for write-only too!
        if (addedReadOnlyPropertyError(leftExpression)) return;

        ClassNode leftRedirect = leftExpressionType.redirect();
        // see if instanceof applies
        if (rightExpression instanceof VariableExpression && hasInferredReturnType(rightExpression) && assignmentExpression.getOperation().getType() == EQUAL) {
            inferredRightExpressionType = rightExpression.getNodeMetaData(INFERRED_RETURN_TYPE);
        }
        ClassNode wrappedRHS = adjustTypeForSpreading(inferredRightExpressionType, leftExpression);

        // check types are compatible for assignment
        boolean compatible = checkCompatibleAssignmentTypes(leftRedirect, wrappedRHS, rightExpression);


        if (!compatible) {
            if (!extension.handleIncompatibleAssignment(leftExpressionType, inferredRightExpressionType, assignmentExpression)) {
                addAssignmentError(leftExpressionType, inferredRightExpressionType, assignmentExpression.getRightExpression());
            }
        } else {
            addPrecisionErrors(leftRedirect, leftExpressionType, inferredRightExpressionType, rightExpression);
            addListAssignmentConstructorErrors(leftRedirect, leftExpressionType, inferredRightExpressionType, rightExpression, assignmentExpression);
            addMapAssignmentConstructorErrors(leftRedirect, leftExpression, rightExpression);
            if (hasGStringStringError(leftExpressionType, wrappedRHS, rightExpression)) return;
            checkTypeGenerics(leftExpressionType, wrappedRHS, rightExpression);
        }
    }

    protected void checkGroovyConstructorMap(final Expression receiver, final ClassNode receiverType, final MapExpression mapExpression) {
        // workaround for map-style checks putting setter info on wrong AST nodes
        typeCheckingContext.pushEnclosingBinaryExpression(null);
        for (MapEntryExpression entryExpression : mapExpression.getMapEntryExpressions()) {
            Expression keyExpr = entryExpression.getKeyExpression();
            if (!(keyExpr instanceof ConstantExpression)) {
                addStaticTypeError("Dynamic keys in map-style constructors are unsupported in static type checking", keyExpr);
            } else {
                AtomicReference<ClassNode> lookup = new AtomicReference<>();
                PropertyExpression pexp = new PropertyExpression(varX("_", receiverType), keyExpr.getText());
                boolean hasProperty = existsProperty(pexp, false, new PropertyLookupVisitor(lookup));
                if (!hasProperty) {
                    addStaticTypeError("No such property: " + keyExpr.getText() +
                            " for class: " + receiverType.getName(), receiver);
                } else {
                    ClassNode valueType = getType(entryExpression.getValueExpression());
                    MethodNode setter = receiverType.getSetterMethod(getSetterName(pexp.getPropertyAsString()), false);
                    ClassNode toBeAssignedTo = setter == null ? lookup.get() : setter.getParameters()[0].getType();
                    if (!isAssignableTo(valueType, toBeAssignedTo)
                            && !extension.handleIncompatibleAssignment(toBeAssignedTo, valueType, entryExpression)) {
                        addAssignmentError(toBeAssignedTo, valueType, entryExpression);
                    }
                }
            }
        }
        typeCheckingContext.popEnclosingBinaryExpression();
    }

    protected static boolean hasRHSIncompleteGenericTypeInfo(final ClassNode inferredRightExpressionType) {
        boolean replaceType = false;
        GenericsType[] genericsTypes = inferredRightExpressionType.getGenericsTypes();
        if (genericsTypes != null) {
            for (GenericsType genericsType : genericsTypes) {
                if (genericsType.isPlaceholder()) {
                    replaceType = true;
                    break;
                }
            }
        }
        return replaceType;
    }

    /**
     * Checks that a constructor style expression is valid regarding the number
     * of arguments and the argument types.
     *
     * @param node      the class node for which we will try to find a matching constructor
     * @param arguments the constructor arguments
     * @deprecated use {@link #checkGroovyStyleConstructor(org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.ClassNode[], org.codehaus.groovy.ast.ASTNode)} )}
     */
    @Deprecated
    protected void checkGroovyStyleConstructor(final ClassNode node, final ClassNode[] arguments) {
        checkGroovyStyleConstructor(node, arguments, typeCheckingContext.getEnclosingClassNode());
    }

    /**
     * Checks that a constructor style expression is valid regarding the number
     * of arguments and the argument types.
     *
     * @param node      the class node for which we will try to find a matching constructor
     * @param arguments the constructor arguments
     */
    protected MethodNode checkGroovyStyleConstructor(final ClassNode node, final ClassNode[] arguments, final ASTNode source) {
        if (node.equals(OBJECT_TYPE) || node.equals(DYNAMIC_TYPE)) {
            // in that case, we are facing a list constructor assigned to a def or object
            return null;
        }
        List<ConstructorNode> constructors = node.getDeclaredConstructors();
        if (constructors.isEmpty() && arguments.length == 0) {
            return null;
        }
        List<MethodNode> constructorList = findMethod(node, "<init>", arguments);
        if (constructorList.isEmpty()) {
            if (isBeingCompiled(node) && arguments.length == 1 && LINKEDHASHMAP_CLASSNODE.equals(arguments[0])) {
                // there will be a default hash map constructor added later
                ConstructorNode cn = new ConstructorNode(Opcodes.ACC_PUBLIC, new Parameter[]{new Parameter(LINKEDHASHMAP_CLASSNODE, "args")}, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
                return cn;
            } else {
                addStaticTypeError("No matching constructor found: " + node + toMethodParametersString("<init>", arguments), source);
                return null;
            }
        } else if (constructorList.size() > 1) {
            addStaticTypeError("Ambiguous constructor call " + node + toMethodParametersString("<init>", arguments), source);
            return null;
        }
        return constructorList.get(0);
    }

    /**
     * When instanceof checks are found in the code, we store temporary type
     * information data in the {@link TypeCheckingContext#temporaryIfBranchTypeInformation}
     * table. This method computes the key which must be used to store this type
     * info.
     *
     * @param expression the expression for which to compute the key
     * @return a key to be used for {@link TypeCheckingContext#temporaryIfBranchTypeInformation}
     */
    protected Object extractTemporaryTypeInfoKey(final Expression expression) {
        return expression instanceof VariableExpression ? findTargetVariable((VariableExpression) expression) : expression.getText();
    }

    /**
     * A helper method which determines which receiver class should be used in error messages when a field or attribute
     * is not found. The returned type class depends on whether we have temporary type information available (due to
     * instanceof checks) and whether there is a single candidate in that case.
     *
     * @param expr the expression for which an unknown field has been found
     * @param type the type of the expression (used as fallback type)
     * @return if temporary information is available and there's only one type, returns the temporary type class
     * otherwise falls back to the provided type class.
     */
    protected ClassNode findCurrentInstanceOfClass(final Expression expr, final ClassNode type) {
        if (!typeCheckingContext.temporaryIfBranchTypeInformation.isEmpty()) {
            List<ClassNode> nodes = getTemporaryTypesForExpression(expr);
            if (nodes != null && nodes.size() == 1) return nodes.get(0);
        }
        return type;
    }

    protected boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly) {
        return existsProperty(pexp, checkForReadOnly, null);
    }

    /**
     * Checks whether a property exists on the receiver, or on any of the possible receiver classes (found in the
     * temporary type information table)
     *
     * @param pexp     a property expression
     * @param readMode if true, look for property read, else for property set
     * @param visitor  if not null, when the property node is found, visit it with the provided visitor
     * @return true if the property is defined in any of the possible receiver classes
     */
    protected boolean existsProperty(final PropertyExpression pexp, final boolean readMode, final ClassCodeVisitorSupport visitor) {
        super.visitPropertyExpression(pexp);

        String propertyName = pexp.getPropertyAsString();
        if (propertyName == null) return false;

        Expression objectExpression = pexp.getObjectExpression();
        ClassNode objectExpressionType = getType(objectExpression);
        List<ClassNode> enclosingTypes = typeCheckingContext.getEnclosingClassNodes();

        boolean staticOnlyAccess = isClassClassNodeWrappingConcreteType(objectExpressionType);
        if (staticOnlyAccess && "this".equals(propertyName)) {
            // handle "Outer.this" for any level of nesting
            ClassNode outer = objectExpressionType.getGenericsTypes()[0].getType();

            ClassNode found = null;
            for (ClassNode enclosingType : enclosingTypes) {
                if (!enclosingType.isStaticClass() && outer.equals(enclosingType.getOuterClass())) {
                    found = enclosingType;
                    break;
                }
            }
            if (found != null) {
                storeType(pexp, outer);
                return true;
            }
        }

        boolean foundGetterOrSetter = false;
        String capName = capitalize(propertyName);
        Set<ClassNode> handledNodes = new HashSet<>();
        List<Receiver<String>> receivers = new ArrayList<>();
        addReceivers(receivers, makeOwnerList(objectExpression), pexp.isImplicitThis());

        for (Receiver<String> receiver : receivers) {
            ClassNode receiverType = receiver.getType();

            if (receiverType.isArray() && "length".equals(propertyName)) {
                storeType(pexp, int_TYPE);
                if (visitor != null) {
                    FieldNode length = new FieldNode("length", Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, int_TYPE, receiverType, null);
                    length.setDeclaringClass(receiverType);
                    visitor.visitField(length);
                }
                return true;
            }

            LinkedList<ClassNode> queue = new LinkedList<>();
            queue.add(receiverType);
            if (isPrimitiveType(receiverType)) {
                queue.add(getWrapper(receiverType));
            }
            while (!queue.isEmpty()) {
                ClassNode current = queue.remove();
                if (!handledNodes.add(current)) continue;

                FieldNode field = current.getDeclaredField(propertyName);
                if (field == null) {
                    if (current.getSuperClass() != null) {
                        queue.addFirst(current.getUnresolvedSuperClass());
                    }
                    for (ClassNode face : current.getAllInterfaces()) {
                        queue.add(GenericsUtils.parameterizeType(current, face));
                    }
                }

                // in case of a lookup on Class we look for instance methods on Class
                // as well, since in case of a static property access we have the class
                // itself in the list of receivers already;
                boolean staticOnly;
                if (isClassClassNodeWrappingConcreteType(current)) {
                    staticOnly = false;
                } else {
                    staticOnly = staticOnlyAccess;
                }

                field = allowStaticAccessToMember(field, staticOnly);

                // skip property/accessor checks for "x.@field"
                if (pexp instanceof AttributeExpression) {
                    if (field != null && storeField(field, pexp, receiverType, visitor, receiver.getData(), !readMode)) {
                        pexp.removeNodeMetaData(READONLY_PROPERTY);
                        return true;
                    }
                    continue;
                }

                // skip property/accessor checks for "field", "this.field", "this.with { field }", etc. in declaring class of field
                if (field != null && enclosingTypes.contains(current)) {
                    if (storeField(field, pexp, receiverType, visitor, receiver.getData(), !readMode)) {
                        pexp.removeNodeMetaData(READONLY_PROPERTY);
                        return true;
                    }
                }

                MethodNode getter = findGetter(current, "get" + capName, pexp.isImplicitThis());
                getter = allowStaticAccessToMember(getter, staticOnly);
                if (getter == null) getter = findGetter(current, "is" + capName, pexp.isImplicitThis());
                getter = allowStaticAccessToMember(getter, staticOnly);
                List<MethodNode> setters = findSetters(current, getSetterName(propertyName), false);
                setters = allowStaticAccessToMember(setters, staticOnly);

                // need to visit even if we only look for setters for compatibility
                if (visitor != null && getter != null) visitor.visitMethod(getter);

                PropertyNode property = current.getProperty(propertyName);
                property = allowStaticAccessToMember(property, staticOnly);
                // prefer explicit getter or setter over property if receiver is not 'this'
                if (property == null || !enclosingTypes.contains(receiverType)) {
                    if (readMode) {
                        if (getter != null) {
                            ClassNode returnType = inferReturnTypeGenerics(current, getter, ArgumentListExpression.EMPTY_ARGUMENTS);
                            storeInferredTypeForPropertyExpression(pexp, returnType);
                            storeTargetMethod(pexp, getter);
                            String delegationData = receiver.getData();
                            if (delegationData != null) {
                                pexp.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
                            }
                            return true;
                        }
                    } else {
                        if (!setters.isEmpty()) {
                            if (visitor != null) {
                                if (field != null) {
                                    visitor.visitField(field);
                                } else {
                                    for (MethodNode setter : setters) {
                                        // visiting setter will not infer the property type since return type is void, so visit a dummy field instead
                                        FieldNode virtual = new FieldNode(propertyName, 0, setter.getParameters()[0].getOriginType(), current, null);
                                        virtual.setDeclaringClass(setter.getDeclaringClass());
                                        visitor.visitField(virtual);
                                    }
                                }
                            }
                            SetterInfo info = new SetterInfo(current, getSetterName(propertyName), setters);
                            BinaryExpression enclosingBinaryExpression = typeCheckingContext.getEnclosingBinaryExpression();
                            if (enclosingBinaryExpression != null) {
                                putSetterInfo(enclosingBinaryExpression.getLeftExpression(), info);
                            }
                            String delegationData = receiver.getData();
                            if (delegationData != null) {
                                pexp.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
                            }
                            pexp.removeNodeMetaData(READONLY_PROPERTY);
                            return true;
                        } else if (property == null) {
                            if (field != null && hasAccessToField(typeCheckingContext.getEnclosingClassNode(), field)) {
                                pexp.removeNodeMetaData(READONLY_PROPERTY);
                            } else if (getter != null) {
                                pexp.putNodeMetaData(READONLY_PROPERTY, Boolean.TRUE);
                            }
                        }
                    }
                }
                foundGetterOrSetter = (foundGetterOrSetter || !setters.isEmpty() || getter != null);

                if (property != null && storeProperty(property, pexp, receiverType, visitor, receiver.getData())) return true;

                if (field != null && storeField(field, pexp, receiverType, visitor, receiver.getData(), !readMode)) return true;
            }

            // GROOVY-5568: the property may be defined by DGM
            List<ClassNode> dgmReceivers = new ArrayList<>(2);
            dgmReceivers.add(receiverType);
            if (isPrimitiveType(receiverType))
                dgmReceivers.add(getWrapper(receiverType));
            for (ClassNode dgmReceiver : dgmReceivers) {
                List<MethodNode> methods = findDGMMethodsByNameAndArguments(getSourceUnit().getClassLoader(), dgmReceiver, "get" + capName, ClassNode.EMPTY_ARRAY);
                for (MethodNode method : findDGMMethodsByNameAndArguments(getSourceUnit().getClassLoader(), dgmReceiver, "is" + capName, ClassNode.EMPTY_ARRAY)) {
                    if (Boolean_TYPE.equals(getWrapper(method.getReturnType()))) methods.add(method);
                }
                if (!methods.isEmpty()) {
                    List<MethodNode> bestMethods = chooseBestMethod(dgmReceiver, methods, ClassNode.EMPTY_ARRAY);
                    if (bestMethods.size() == 1) {
                        MethodNode getter = bestMethods.get(0);
                        if (visitor != null) {
                            visitor.visitMethod(getter);
                        }
                        ClassNode returnType = inferReturnTypeGenerics(dgmReceiver, getter, ArgumentListExpression.EMPTY_ARGUMENTS);
                        storeInferredTypeForPropertyExpression(pexp, returnType);
                        if (readMode) storeTargetMethod(pexp, getter);
                        return true;
                    }
                }
            }

            // GROOVY-7996: check if receiver implements get(String)/set(String,Object) or propertyMissing(String)
            if (!receiverType.isArray() && !isPrimitiveType(getUnwrapper(receiverType))
                    && pexp.isImplicitThis() && typeCheckingContext.getEnclosingClosure() != null) {
                MethodNode mopMethod;
                if (readMode) {
                    mopMethod = receiverType.getMethod("get", new Parameter[]{new Parameter(STRING_TYPE, "name")});
                } else {
                    mopMethod = receiverType.getMethod("set", new Parameter[]{new Parameter(STRING_TYPE, "name"), new Parameter(OBJECT_TYPE, "value")});
                }
                if (mopMethod == null) mopMethod = receiverType.getMethod("propertyMissing", new Parameter[]{new Parameter(STRING_TYPE, "propertyName")});

                if (mopMethod != null && !mopMethod.isSynthetic()) {
                    pexp.putNodeMetaData(DYNAMIC_RESOLUTION, Boolean.TRUE);
                    pexp.removeNodeMetaData(DECLARATION_INFERRED_TYPE);
                    pexp.removeNodeMetaData(INFERRED_TYPE);
                    return true;
                }
            }
        }

        for (Receiver<String> receiver : receivers) {
            ClassNode receiverType = receiver.getType();
            ClassNode propertyType = getTypeForMapPropertyExpression(receiverType, objectExpressionType, pexp);
            if (propertyType == null)
                propertyType = getTypeForListPropertyExpression(receiverType, objectExpressionType, pexp);
            if (propertyType == null)
                propertyType = getTypeForSpreadExpression(receiverType, objectExpressionType, pexp);
            if (propertyType == null)
                continue;
            if (visitor != null) {
                // TODO: type inference on maps and lists, if possible
                PropertyNode node = new PropertyNode(propertyName, Opcodes.ACC_PUBLIC, propertyType, receiver.getType(), null, null, null);
                node.setDeclaringClass(receiver.getType());
                visitor.visitProperty(node);
            }
            storeType(pexp, propertyType);
            String delegationData = receiver.getData();
            if (delegationData != null) pexp.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
            return true;
        }

        return foundGetterOrSetter;
    }

    private static boolean hasAccessToField(final ClassNode accessor, final FieldNode field) {
        if (field.isPublic() || accessor.equals(field.getDeclaringClass())) {
            return true;
        }
        if (field.isProtected()) {
            return accessor.isDerivedFrom(field.getDeclaringClass());
        } else {
            return !field.isPrivate() && Objects.equals(accessor.getPackageName(), field.getDeclaringClass().getPackageName());
        }
    }

    private MethodNode findGetter(final ClassNode current, String name, final boolean searchOuterClasses) {
        MethodNode getterMethod = current.getGetterMethod(name);
        if (getterMethod == null && searchOuterClasses && current.getOuterClass() != null) {
            return findGetter(current.getOuterClass(), name, true);
        }
        return getterMethod;
    }

    private ClassNode getTypeForSpreadExpression(final ClassNode testClass, final ClassNode objectExpressionType, final PropertyExpression pexp) {
        if (!pexp.isSpreadSafe()) return null;
        MethodCallExpression mce = callX(varX("_", testClass), "iterator", ArgumentListExpression.EMPTY_ARGUMENTS);
        mce.setImplicitThis(false);
        mce.visit(this);
        ClassNode callType = getType(mce);
        if (!implementsInterfaceOrIsSubclassOf(callType, Iterator_TYPE)) return null;
        GenericsType[] types = callType.getGenericsTypes();
        ClassNode contentType = OBJECT_TYPE;
        if (types != null && types.length == 1) contentType = types[0].getType();
        PropertyExpression subExp = new PropertyExpression(varX("{}", contentType), pexp.getPropertyAsString());
        AtomicReference<ClassNode> result = new AtomicReference<>();
        if (existsProperty(subExp, true, new PropertyLookupVisitor(result))) {
            ClassNode intf = LIST_TYPE.getPlainNodeReference();
            intf.setGenericsTypes(new GenericsType[]{new GenericsType(getWrapper(result.get()))});
            return intf;
        }
        return null;
    }

    private ClassNode getTypeForListPropertyExpression(final ClassNode testClass, final ClassNode objectExpressionType, final PropertyExpression pexp) {
        if (!implementsInterfaceOrIsSubclassOf(testClass, LIST_TYPE)) return null;
        ClassNode intf = GenericsUtils.parameterizeType(objectExpressionType, LIST_TYPE.getPlainNodeReference());
        GenericsType[] types = intf.getGenericsTypes();
        if (types == null || types.length != 1) return OBJECT_TYPE;

        PropertyExpression subExp = new PropertyExpression(varX("{}", types[0].getType()), pexp.getPropertyAsString());
        AtomicReference<ClassNode> result = new AtomicReference<>();
        if (existsProperty(subExp, true, new PropertyLookupVisitor(result))) {
            intf = LIST_TYPE.getPlainNodeReference();
            ClassNode itemType = result.get();
            intf.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(itemType))});
            return intf;
        }
        return null;
    }

    private ClassNode getTypeForMapPropertyExpression(final ClassNode testClass, final ClassNode objectExpressionType, final PropertyExpression pexp) {
        if (!implementsInterfaceOrIsSubclassOf(testClass, MAP_TYPE)) return null;
        ClassNode intf;
        if (objectExpressionType.getGenericsTypes() != null) {
            intf = GenericsUtils.parameterizeType(objectExpressionType, MAP_TYPE.getPlainNodeReference());
        } else {
            intf = MAP_TYPE.getPlainNodeReference();
        }
        // 0 is the key, 1 is the value
        GenericsType[] types = intf.getGenericsTypes();
        if (types == null || types.length != 2) return OBJECT_TYPE;

        if (pexp.isSpreadSafe()) {
            // map*.property syntax
            // only "key" and "value" are allowed
            if ("key".equals(pexp.getPropertyAsString())) {
                ClassNode listKey = LIST_TYPE.getPlainNodeReference();
                listKey.setGenericsTypes(new GenericsType[]{types[0]});
                return listKey;
            } else if ("value".equals(pexp.getPropertyAsString())) {
                ClassNode listValue = LIST_TYPE.getPlainNodeReference();
                listValue.setGenericsTypes(new GenericsType[]{types[1]});
                return listValue;
            } else {
                addStaticTypeError("Spread operator on map only allows one of [key,value]", pexp);
            }
        } else {
            return types[1].getType();
        }
        return null;
    }

    /**
     * This method is used to filter search results in which null means "no match",
     * to filter out illegal access to instance members from a static context.
     * <p>
     * Return null if the given member is not static, but we want to access in
     * a static way (staticOnly=true). If we want to access in a non-static way
     * we always return the member, since then access to static members and
     * non-static members is allowed.
     */
    @SuppressWarnings("unchecked")
    private <T> T allowStaticAccessToMember(final T member, final boolean staticOnly) {
        if (member == null) return null;
        if (!staticOnly) return member;
        boolean isStatic;
        if (member instanceof Variable) {
            Variable v = (Variable) member;
            isStatic = Modifier.isStatic(v.getModifiers());
        } else if (member instanceof List) {
            List<MethodNode> list = (List<MethodNode>) member;
            if (list.size() == 1) {
                return (T) Collections.singletonList(allowStaticAccessToMember(list.get(0), staticOnly));
            }
            return (T) Collections.emptyList();
        } else {
            MethodNode mn = (MethodNode) member;
            isStatic = mn.isStatic();
        }
        if (staticOnly && !isStatic) return null;
        return member;
    }

    private void storeWithResolve(final ClassNode typeToResolve, final ClassNode receiver, final ClassNode declaringClass, final boolean isStatic, final Expression expressionToStoreOn) {
        ClassNode type = typeToResolve;
        if (missesGenericsTypes(type)) {
            Map<GenericsTypeName, GenericsType> resolvedPlaceholders = resolvePlaceHoldersFromDeclaration(receiver, declaringClass, null, isStatic);
            type = resolveGenericsWithContext(resolvedPlaceholders, type);
        }
        if (expressionToStoreOn instanceof PropertyExpression) {
            storeInferredTypeForPropertyExpression((PropertyExpression) expressionToStoreOn, type);
        } else {
            storeType(expressionToStoreOn, type);
        }
    }

    private boolean storeField(final FieldNode field, final PropertyExpression expressionToStoreOn, final ClassNode receiver, final ClassCodeVisitorSupport visitor, final String delegationData, final boolean lhsOfAssignment) {
        if (visitor != null) visitor.visitField(field);
        checkOrMarkPrivateAccess(expressionToStoreOn, field, lhsOfAssignment);

        if (expressionToStoreOn instanceof AttributeExpression) { // TODO: expand to include PropertyExpression
            if (!hasAccessToField(isSuperExpression(expressionToStoreOn.getObjectExpression()) ? typeCheckingContext.getEnclosingClassNode() : receiver, field)) {
                addStaticTypeError("The field " + field.getDeclaringClass().getNameWithoutPackage() + "." + field.getName() + " is not accessible", expressionToStoreOn.getProperty());
            }
        }

        storeWithResolve(field.getOriginType(), receiver, field.getDeclaringClass(), field.isStatic(), expressionToStoreOn);
        if (delegationData != null) {
            expressionToStoreOn.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
        }
        return true;
    }

    private boolean storeProperty(final PropertyNode property, final PropertyExpression expressionToStoreOn, final ClassNode receiver, final ClassCodeVisitorSupport visitor, final String delegationData) {
        if (visitor != null) visitor.visitProperty(property);
        storeWithResolve(property.getOriginType(), receiver, property.getDeclaringClass(), property.isStatic(), expressionToStoreOn);
        if (delegationData != null) {
            expressionToStoreOn.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
        }
        return true;
    }

    protected void storeInferredTypeForPropertyExpression(final PropertyExpression pexp, final ClassNode flatInferredType) {
        if (pexp.isSpreadSafe()) {
            ClassNode list = LIST_TYPE.getPlainNodeReference();
            list.setGenericsTypes(new GenericsType[]{new GenericsType(flatInferredType)});
            storeType(pexp, list);
        } else {
            storeType(pexp, flatInferredType);
        }
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        boolean osc = typeCheckingContext.isInStaticContext;
        try {
            typeCheckingContext.isInStaticContext = node.isInStaticContext();
            currentProperty = node;
            super.visitProperty(node);
        } finally {
            currentProperty = null;
            typeCheckingContext.isInStaticContext = osc;
        }
    }

    @Override
    public void visitField(final FieldNode node) {
        boolean osc = typeCheckingContext.isInStaticContext;
        try {
            typeCheckingContext.isInStaticContext = node.isInStaticContext();
            currentField = node;
            super.visitField(node);
            Expression init = node.getInitialExpression();
            if (init != null) {
                FieldExpression left = new FieldExpression(node);
                BinaryExpression bexp = binX(
                        left,
                        Token.newSymbol("=", node.getLineNumber(), node.getColumnNumber()),
                        init
                );
                bexp.setSourcePosition(init);
                typeCheckAssignment(bexp, left, node.getOriginType(), init, getType(init));
                if (init instanceof ConstructorCallExpression) {
                    inferDiamondType((ConstructorCallExpression) init, node.getOriginType());
                }
            }
        } finally {
            currentField = null;
            typeCheckingContext.isInStaticContext = osc;
        }
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        // collect every variable expression used in the loop body
        Map<VariableExpression, ClassNode> varOrigType = new HashMap<>();
        forLoop.getLoopBlock().visit(new VariableExpressionTypeMemoizer(varOrigType));

        // visit body
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        Expression collectionExpression = forLoop.getCollectionExpression();
        if (collectionExpression instanceof ClosureListExpression) {
            // for (int i=0; i<...; i++) style loop
            super.visitForLoop(forLoop);
        } else {
            collectionExpression.visit(this);
            ClassNode collectionType = getType(collectionExpression);
            ClassNode forLoopVariableType = forLoop.getVariableType();
            ClassNode componentType;
            if (Character_TYPE.equals(getWrapper(forLoopVariableType)) && STRING_TYPE.equals(collectionType)) {
                // we allow auto-coercion here
                componentType = forLoopVariableType;
            } else {
                componentType = inferLoopElementType(collectionType);
            }
            if (getUnwrapper(componentType) == forLoopVariableType) {
                // prefer primitive type over boxed type
                componentType = forLoopVariableType;
            }
            if (!checkCompatibleAssignmentTypes(forLoopVariableType, componentType)) {
                addStaticTypeError("Cannot loop with element of type " + forLoopVariableType.toString(false) + " with collection of type " + collectionType.toString(false), forLoop);
            }
            if (forLoopVariableType != DYNAMIC_TYPE) {
                // user has specified a type, prefer it over the inferred type
                componentType = forLoopVariableType;
            }
            typeCheckingContext.controlStructureVariables.put(forLoop.getVariable(), componentType);
            try {
                super.visitForLoop(forLoop);
            } finally {
                typeCheckingContext.controlStructureVariables.remove(forLoop.getVariable());
            }
        }
        boolean typeChanged = isSecondPassNeededForControlStructure(varOrigType, oldTracker);
        if (typeChanged) visitForLoop(forLoop);
    }

    /**
     * Given a loop collection type, returns the inferred type of the loop element. Used, for
     * example, to infer the element type of a (for e in list) loop.
     *
     * @param collectionType the type of the collection
     * @return the inferred component type
     */
    public static ClassNode inferLoopElementType(final ClassNode collectionType) {
        ClassNode componentType = collectionType.getComponentType();
        if (componentType == null) {
            if (implementsInterfaceOrIsSubclassOf(collectionType, ITERABLE_TYPE)) {
                ClassNode intf = GenericsUtils.parameterizeType(collectionType, ITERABLE_TYPE);
                GenericsType[] genericsTypes = intf.getGenericsTypes();
                componentType = genericsTypes[0].getType();
            } else if (implementsInterfaceOrIsSubclassOf(collectionType, MAP_TYPE)) {
                // GROOVY-6240
                ClassNode intf = GenericsUtils.parameterizeType(collectionType, MAP_TYPE);
                GenericsType[] genericsTypes = intf.getGenericsTypes();
                componentType = MAP_ENTRY_TYPE.getPlainNodeReference();
                componentType.setGenericsTypes(genericsTypes);
            } else if (STRING_TYPE.equals(collectionType)) {
                componentType = STRING_TYPE;
            } else if (ENUMERATION_TYPE.equals(collectionType)) {
                // GROOVY-6123
                ClassNode intf = GenericsUtils.parameterizeType(collectionType, ENUMERATION_TYPE);
                GenericsType[] genericsTypes = intf.getGenericsTypes();
                componentType = genericsTypes[0].getType();
            } else {
                componentType = OBJECT_TYPE;
            }
        }
        return componentType;
    }

    protected boolean isSecondPassNeededForControlStructure(final Map<VariableExpression, ClassNode> varOrigType, final Map<VariableExpression, List<ClassNode>> oldTracker) {
        for (Map.Entry<VariableExpression, ClassNode> entry : popAssignmentTracking(oldTracker).entrySet()) {
            Variable key = findTargetVariable(entry.getKey());
            if (key instanceof VariableExpression && varOrigType.containsKey(key)) {
                ClassNode origType = varOrigType.get(key);
                ClassNode newType = entry.getValue();
                if (!newType.equals(origType)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        super.visitWhileLoop(loop);
        popAssignmentTracking(oldTracker);
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        super.visitBitwiseNegationExpression(expression);
        ClassNode type = getType(expression);
        ClassNode typeRe = type.redirect();
        ClassNode resultType;
        if (isBigIntCategory(typeRe)) {
            // allow any internal number that is not a floating point one
            resultType = type;
        } else if (typeRe == STRING_TYPE || typeRe == GSTRING_TYPE) {
            resultType = PATTERN_TYPE;
        } else if (typeRe == ArrayList_TYPE) {
            resultType = ArrayList_TYPE;
        } else if (typeRe.equals(PATTERN_TYPE)) {
            resultType = PATTERN_TYPE;
        } else {
            MethodNode mn = findMethodOrFail(expression, type, "bitwiseNegate");
            if (mn != null) {
                resultType = mn.getReturnType();
            } else {
                resultType = OBJECT_TYPE;
            }
        }
        storeType(expression, resultType);
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        super.visitUnaryPlusExpression(expression);
        negativeOrPositiveUnary(expression, "positive");
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        super.visitUnaryMinusExpression(expression);
        negativeOrPositiveUnary(expression, "negative");
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        Expression operand = expression.getExpression();
        int operator = expression.getOperation().getType();
        visitPrefixOrPostifExpression(expression, operand, operator);
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        Expression operand = expression.getExpression();
        int operator = expression.getOperation().getType();
        visitPrefixOrPostifExpression(expression, operand, operator);
    }

    private void visitPrefixOrPostifExpression(final Expression origin, final Expression operand, final int operator) {
        Optional<Token> token = TokenUtil.asAssignment(operator);
        // push "operand += 1" or "operand -= 1" onto stack for LHS checks
        token.ifPresent(value -> typeCheckingContext.pushEnclosingBinaryExpression(binX(operand, value, constX(1))));
        try {
            operand.visit(this);
            SetterInfo setterInfo = removeSetterInfo(operand);
            if (setterInfo != null) {
                BinaryExpression rewrite = typeCheckingContext.getEnclosingBinaryExpression();
                rewrite.setSourcePosition(origin);
                if (ensureValidSetter(rewrite, operand, rewrite.getRightExpression(), setterInfo)) {
                    return;
                }
            }

            ClassNode operandType = getType(operand);
            boolean isPostfix = (origin instanceof PostfixExpression);
            String name = (operator == PLUS_PLUS ? "next" : operator == MINUS_MINUS ? "previous" : null);

            if (name != null && isNumberType(operandType)) {
                if (!isPrimitiveType(operandType)) {
                    MethodNode node = findMethodOrFail(varX("_dummy_", operandType), operandType, name);
                    if (node != null) {
                        storeTargetMethod(origin, node);
                        storeType(origin, isPostfix ? operandType : getMathWideningClassNode(operandType));
                        return;
                    }
                }
                storeType(origin, operandType);
                return;
            }
            if (name != null && operandType.isDerivedFrom(Number_TYPE)) {
                // special case for numbers, improve type checking as we can expect ++ and -- to return the same type
                MethodNode node = findMethodOrFail(operand, operandType, name);
                if (node != null) {
                    storeTargetMethod(origin, node);
                    storeType(origin, getMathWideningClassNode(operandType));
                    return;
                }
            }
            if (name == null) {
                addUnsupportedPreOrPostfixExpressionError(origin);
                return;
            }

            MethodNode node = findMethodOrFail(operand, operandType, name);
            if (node != null) {
                storeTargetMethod(origin, node);
                storeType(origin, isPostfix ? operandType : inferReturnTypeGenerics(operandType, node, ArgumentListExpression.EMPTY_ARGUMENTS));
            }
        } finally {
            if (token.isPresent()) typeCheckingContext.popEnclosingBinaryExpression();
        }
    }

    private static ClassNode getMathWideningClassNode(final ClassNode type) {
        if (byte_TYPE.equals(type) || short_TYPE.equals(type) || int_TYPE.equals(type)) {
            return int_TYPE;
        }
        if (Byte_TYPE.equals(type) || Short_TYPE.equals(type) || Integer_TYPE.equals(type)) {
            return Integer_TYPE;
        }
        if (float_TYPE.equals(type)) {
            return double_TYPE;
        }
        if (Float_TYPE.equals(type)) {
            return Double_TYPE;
        }
        return type;
    }

    private void negativeOrPositiveUnary(final Expression expression, final String name) {
        ClassNode type = getType(expression);
        ClassNode typeRe = type.redirect();
        ClassNode resultType;
        if (isDoubleCategory(getUnwrapper(typeRe))) {
            resultType = type;
        } else if (typeRe == ArrayList_TYPE) {
            resultType = ArrayList_TYPE;
        } else {
            MethodNode mn = findMethodOrFail(expression, type, name);
            if (mn != null) {
                resultType = mn.getReturnType();
            } else {
                resultType = type;
            }
        }
        storeType(expression, resultType);
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        typeCheckingContext.pushEnclosingMethod(node);
        if (!isSkipMode(node) && !shouldSkipMethodNode(node)) {
            super.visitConstructorOrMethod(node, isConstructor);
        }
        if (!isConstructor) {
            returnAdder.visitMethod(node); // return statement added after visitConstructorOrMethod finished... we can not count these auto-generated return statements(GROOVY-7753), see `typeCheckingContext.pushEnclosingReturnStatement`
        }
        typeCheckingContext.popEnclosingMethod();
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        typeCheckingContext.pushTemporaryTypeInfo();
        super.visitExpressionStatement(statement);
        typeCheckingContext.popTemporaryTypeInfo();
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        super.visitReturnStatement(statement);
        returnListener.returnStatementAdded(statement);
    }

    private ClassNode infer(final ClassNode target, final ClassNode source) {
        DeclarationExpression virtualDecl = new DeclarationExpression(
                varX("{target}", target),
                Token.newSymbol(EQUAL, -1, -1),
                varX("{source}", source)
        );
        virtualDecl.visit(this);
        ClassNode newlyInferred = virtualDecl.getNodeMetaData(INFERRED_TYPE);

        return !missesGenericsTypes(newlyInferred) ? newlyInferred : null;
    }

    protected ClassNode checkReturnType(final ReturnStatement statement) {
        Expression expression = statement.getExpression();
        ClassNode type = getType(expression);

        if (typeCheckingContext.getEnclosingClosure() != null) {
            return type;
        }
        // handle instanceof cases
        if ((expression instanceof VariableExpression) && hasInferredReturnType(expression)) {
            type = expression.getNodeMetaData(INFERRED_RETURN_TYPE);
        }
        MethodNode enclosingMethod = typeCheckingContext.getEnclosingMethod();
        if (enclosingMethod != null && typeCheckingContext.getEnclosingClosure() == null) {
            if (!enclosingMethod.isVoidMethod()
                    && !type.equals(void_WRAPPER_TYPE)
                    && !type.equals(VOID_TYPE)
                    && !checkCompatibleAssignmentTypes(enclosingMethod.getReturnType(), type, null, false)
                    && !(isNullConstant(expression))) {
                if (!extension.handleIncompatibleReturnType(statement, type)) {
                    addStaticTypeError("Cannot return value of type " + type.toString(false) + " on method returning type " + enclosingMethod.getReturnType().toString(false), expression);
                }
            } else if (!enclosingMethod.isVoidMethod()) {
                ClassNode previousType = getInferredReturnType(enclosingMethod);
                ClassNode inferred = previousType == null ? type : lowestUpperBound(type, previousType);
                if (implementsInterfaceOrIsSubclassOf(inferred, enclosingMethod.getReturnType())) {
                    if (missesGenericsTypes(inferred)) {
                        ClassNode newlyInferred = infer(enclosingMethod.getReturnType(), type);
                        if (newlyInferred != null) {
                            type = newlyInferred;
                        }
                    } else {
                        checkTypeGenerics(enclosingMethod.getReturnType(), inferred, expression);
                    }
                    return type;
                } else {
                    return enclosingMethod.getReturnType();
                }
            }
        }
        return type;
    }

    protected void addClosureReturnType(final ClassNode returnType) {
        typeCheckingContext.getEnclosingClosure().addReturnType(returnType);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        if (extension.beforeMethodCall(call)) {
            extension.afterMethodCall(call);
            return;
        }
        ClassNode receiver = call.getType();
        if (call.isThisCall()) {
            receiver = typeCheckingContext.getEnclosingClassNode();
        } else if (call.isSuperCall()) {
            receiver = typeCheckingContext.getEnclosingClassNode().getSuperClass();
        }
        Expression arguments = call.getArguments();
        ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(arguments);

        checkForbiddenSpreadArgument(argumentList);
        visitMethodCallArguments(receiver, argumentList, false, null);

        ClassNode[] args = getArgumentTypes(argumentList);

        MethodNode node;
        if (looksLikeNamedArgConstructor(receiver, args)
                && findMethod(receiver, "<init>", DefaultGroovyMethods.init(args)).size() == 1
                && findMethod(receiver, "<init>", args).isEmpty()) {
            // bean-style constructor
            node = typeCheckMapConstructor(call, receiver, arguments);
            if (node != null) {
                storeTargetMethod(call, node);
                extension.afterMethodCall(call);
                return;
            }
        }
        node = findMethodOrFail(call, receiver, "<init>", args);
        if (node != null) {
            if (looksLikeNamedArgConstructor(receiver, args) && node.getParameters().length + 1 == args.length) {
                node = typeCheckMapConstructor(call, receiver, arguments);
            } else {
                typeCheckMethodsWithGenericsOrFail(receiver, args, node, call);
            }
            if (node != null) {
                storeTargetMethod(call, node);
                visitMethodCallArguments(receiver, argumentList, true, node);
            }
        }

        // GROOVY-9327: check for AIC in STC method with non-STC enclosing class
        if (call.isUsingAnonymousInnerClass()) {
            Set<MethodNode> methods = typeCheckingContext.methodsToBeVisited;
            if (!methods.isEmpty()) { // indicates specific methods have STC
                typeCheckingContext.methodsToBeVisited = Collections.emptySet();

                ClassNode anonType = call.getType();
                visitClass(anonType); // visit anon. inner class inline with method
                anonType.putNodeMetaData(StaticTypeCheckingVisitor.class, Boolean.TRUE);

                typeCheckingContext.methodsToBeVisited = methods;
            }
        }

        extension.afterMethodCall(call);
    }

    private boolean looksLikeNamedArgConstructor(final ClassNode receiver, final ClassNode[] argumentTypes) {
        if (argumentTypes.length == 1 || argumentTypes.length == 2 && argumentTypes[0].equals(receiver.getOuterClass())) {
            return argumentTypes[argumentTypes.length - 1].implementsInterface(MAP_TYPE);
        }
        return false;
    }

    protected MethodNode typeCheckMapConstructor(final ConstructorCallExpression call, final ClassNode receiver, final Expression arguments) {
        MethodNode node = null;
        if (arguments instanceof TupleExpression) {
            TupleExpression texp = (TupleExpression) arguments;
            List<Expression> expressions = texp.getExpressions();
            // should only get here with size = 2 when inner class constructor
            if (expressions.size() == 1 || expressions.size() == 2) {
                Expression expression = expressions.get(expressions.size() - 1);
                if (expression instanceof MapExpression) {
                    MapExpression argList = (MapExpression) expression;
                    checkGroovyConstructorMap(call, receiver, argList);
                    Parameter[] params = expressions.size() == 1
                            ? new Parameter[]{new Parameter(MAP_TYPE, "map")}
                            : new Parameter[]{new Parameter(receiver.redirect().getOuterClass(), "$p$"), new Parameter(MAP_TYPE, "map")};
                    node = new ConstructorNode(Opcodes.ACC_PUBLIC, params, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                    node.setDeclaringClass(receiver);
                }
            }
        }
        return node;
    }

    protected ClassNode[] getArgumentTypes(final ArgumentListExpression args) {
        return args.getExpressions().stream().map(exp ->
            isNullConstant(exp) ? UNKNOWN_PARAMETER_TYPE : getInferredTypeFromTempInfo(exp, getType(exp))
        ).toArray(ClassNode[]::new);
    }

    private ClassNode getInferredTypeFromTempInfo(final Expression exp, ClassNode result) {
        if (exp instanceof VariableExpression && !typeCheckingContext.temporaryIfBranchTypeInformation.isEmpty()) {
            List<ClassNode> classNodes = getTemporaryTypesForExpression(exp);
            if (classNodes != null && !classNodes.isEmpty()) {
                List<ClassNode> types = new ArrayList<>(classNodes.size() + 1);
                if (result != null && !classNodes.contains(result)) types.add(result);
                types.addAll(classNodes);
                // GROOVY-7333: filter out Object
                types.removeIf(OBJECT_TYPE::equals);

                if (types.isEmpty()) {
                    result = OBJECT_TYPE.getPlainNodeReference();
                } else if (types.size() == 1) {
                    result = types.get(0);
                } else {
                    result = new UnionTypeClassNode(types.toArray(ClassNode.EMPTY_ARRAY));
                }
            }
        }
        return result;
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        boolean oldStaticContext = typeCheckingContext.isInStaticContext;
        typeCheckingContext.isInStaticContext = false;

        // collect every variable expression used in the closure body
        Map<VariableExpression, ClassNode> variableTypes = new HashMap<>();
        expression.getCode().visit(new VariableExpressionTypeMemoizer(variableTypes));
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        SharedVariableCollector collector = new SharedVariableCollector(getSourceUnit());
        collector.visitClosureExpression(expression);

        Set<VariableExpression> closureSharedVariables = collector.getClosureSharedExpressions();
        Map<VariableExpression, Map<StaticTypesMarker, Object>> variableMetadata;
        if (!closureSharedVariables.isEmpty()) {
            // GROOVY-6921: call getType in order to update closure shared variables
            // whose types are inferred thanks to closure parameter type inference
            for (VariableExpression ve : closureSharedVariables) {
                getType(ve);
            }
            variableMetadata = new HashMap<>();
            saveVariableExpressionMetadata(closureSharedVariables, variableMetadata);
        } else {
            variableMetadata = null;
        }

        // perform visit
        typeCheckingContext.pushEnclosingClosureExpression(expression);
        DelegationMetadata dmd = getDelegationMetadata(expression);
        if (dmd == null) {
            typeCheckingContext.delegationMetadata = new DelegationMetadata(
                    typeCheckingContext.getEnclosingClassNode(), Closure.OWNER_FIRST, typeCheckingContext.delegationMetadata
            );
        } else {
            typeCheckingContext.delegationMetadata = new DelegationMetadata(
                    dmd.getType(),
                    dmd.getStrategy(),
                    typeCheckingContext.delegationMetadata
            );
        }
        super.visitClosureExpression(expression);
        typeCheckingContext.delegationMetadata = typeCheckingContext.delegationMetadata.getParent();
        MethodNode node = new MethodNode("dummy", 0, OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, expression.getCode());
        returnAdder.visitMethod(node);

        TypeCheckingContext.EnclosingClosure enclosingClosure = typeCheckingContext.getEnclosingClosure();
        if (!enclosingClosure.getReturnTypes().isEmpty()) {
            ClassNode returnType = lowestUpperBound(enclosingClosure.getReturnTypes());

            ClassNode expectedReturnType = getInferredReturnType(expression);
            // type argument can not be of primitive type, we should convert it to the wrapper type
            if (expectedReturnType != null && isPrimitiveType(returnType) && expectedReturnType.equals(getWrapper(returnType))) {
                returnType = expectedReturnType;
            }

            storeInferredReturnType(expression, returnType);
            ClassNode inferredType = wrapClosureType(returnType);
            storeType(enclosingClosure.getClosureExpression(), inferredType);
        }

        typeCheckingContext.popEnclosingClosure();

        boolean typeChanged = isSecondPassNeededForControlStructure(variableTypes, oldTracker);
        if (typeChanged) visitClosureExpression(expression);

        // restore original metadata
        restoreVariableExpressionMetadata(variableMetadata);
        typeCheckingContext.isInStaticContext = oldStaticContext;
        for (Parameter parameter : getParametersSafe(expression)) {
            typeCheckingContext.controlStructureVariables.remove(parameter);
        }
    }

    private static ClassNode wrapClosureType(final ClassNode returnType) {
        ClassNode inferredType = CLOSURE_TYPE.getPlainNodeReference();
        inferredType.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(returnType))});
        return inferredType;
    }

    protected DelegationMetadata getDelegationMetadata(final ClosureExpression expression) {
        return (DelegationMetadata) expression.getNodeMetaData(DELEGATION_METADATA);
    }

    protected void restoreVariableExpressionMetadata(final Map<VariableExpression, Map<StaticTypesMarker, Object>> typesBeforeVisit) {
        if (typesBeforeVisit != null) {
            for (Map.Entry<VariableExpression, Map<StaticTypesMarker, Object>> entry : typesBeforeVisit.entrySet()) {
                for (StaticTypesMarker marker : StaticTypesMarker.values()) {
                    // GROOVY-9344, GROOVY-9516
                    if (marker == INFERRED_TYPE) continue;

                    Object value = entry.getValue().get(marker);
                    if (value == null) {
                        entry.getKey().removeNodeMetaData(marker);
                    } else {
                        entry.getKey().putNodeMetaData(marker, value);
                    }
                }
            }
        }
    }

    protected void saveVariableExpressionMetadata(final Set<VariableExpression> closureSharedExpressions, final Map<VariableExpression, Map<StaticTypesMarker, Object>> typesBeforeVisit) {
        for (VariableExpression ve : closureSharedExpressions) {
            Variable v;
            while ((v = ve.getAccessedVariable()) != ve && v instanceof VariableExpression) {
                ve = (VariableExpression) v;
            }

            Map<StaticTypesMarker, Object> metadata = new EnumMap<>(StaticTypesMarker.class);
            for (StaticTypesMarker marker : StaticTypesMarker.values()) {
                Object value = ve.getNodeMetaData(marker);
                if (value != null) {
                    metadata.put(marker, value);
                }
            }
            typesBeforeVisit.put(ve, metadata);
        }
    }

    protected boolean shouldSkipMethodNode(final MethodNode node) {
        return Boolean.TRUE.equals(node.getNodeMetaData(StaticTypeCheckingVisitor.class));
    }

    @Override
    public void visitMethod(final MethodNode node) {
        if (shouldSkipMethodNode(node)) {
            // method has already been visited by a static type checking visitor
            return;
        }
        if (!extension.beforeVisitMethod(node)) {
            ErrorCollector collector = node.getNodeMetaData(ERROR_COLLECTOR);
            if (collector != null) {
                typeCheckingContext.getErrorCollector().addCollectorContents(collector);
            } else {
                startMethodInference(node, typeCheckingContext.getErrorCollector());
            }
            node.removeNodeMetaData(ERROR_COLLECTOR);
        }
        extension.afterVisitMethod(node);
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        if (shouldSkipMethodNode(node)) {
            // method has already been visited by a static type checking visitor
            return;
        }
        for (Parameter parameter : node.getParameters()) {
            if (parameter.getInitialExpression() != null) {
                parameter.getInitialExpression().visit(this);
            }
        }
        super.visitConstructor(node);
    }

    protected void startMethodInference(final MethodNode node, final ErrorCollector collector) {
        if (isSkipMode(node)) return;

        // second, we must ensure that this method MUST be statically checked
        // for example, in a mixed mode where only some methods are statically checked
        // we must not visit a method which used dynamic dispatch.
        // We do not check for an annotation because some other AST transformations
        // may use this visitor without the annotation being explicitly set
        if (!typeCheckingContext.methodsToBeVisited.isEmpty() && !typeCheckingContext.methodsToBeVisited.contains(node))
            return;

        // alreadyVisitedMethods prevents from visiting the same method multiple times
        // and prevents from infinite loops
        if (typeCheckingContext.alreadyVisitedMethods.contains(node)) return;
        typeCheckingContext.alreadyVisitedMethods.add(node);

        typeCheckingContext.pushErrorCollector(collector);

        boolean osc = typeCheckingContext.isInStaticContext;
        try {
            typeCheckingContext.isInStaticContext = node.isStatic();
            super.visitMethod(node);
            for (Parameter parameter : node.getParameters()) {
                if (parameter.getInitialExpression() != null) {
                    parameter.getInitialExpression().visit(this);
                }
            }
/*
            ClassNode rtype = getInferredReturnType(node);
            if (rtype == null) {
                storeInferredReturnType(node, node.getReturnType());
            }
            addTypeCheckingInfoAnnotation(node);
*/
        } finally {
            typeCheckingContext.isInStaticContext = osc;
        }

        typeCheckingContext.popErrorCollector();
        node.putNodeMetaData(ERROR_COLLECTOR, collector);
    }

    protected void addTypeCheckingInfoAnnotation(final MethodNode node) {
        // TypeChecked$TypeCheckingInfo can not be applied on constructors
        if (node instanceof ConstructorNode) return;

        // if a returned inferred type is available and no @TypeCheckingInfo is on node, then add an
        // annotation to the method node
        ClassNode rtype = getInferredReturnType(node);
        if (rtype != null && node.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()) {
            AnnotationNode anno = new AnnotationNode(TYPECHECKING_INFO_NODE);
            anno.setMember("version", CURRENT_SIGNATURE_PROTOCOL);
            SignatureCodec codec = SignatureCodecFactory.getCodec(CURRENT_SIGNATURE_PROTOCOL_VERSION, getTransformLoader());
            String genericsSignature = codec.encode(rtype);
            if (genericsSignature != null) {
                ConstantExpression signature = new ConstantExpression(genericsSignature);
                signature.setType(STRING_TYPE);
                anno.setMember("inferredType", signature);
                node.addAnnotation(anno);
            }
        }
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        String name = call.getMethod();
        if (name == null) {
            addStaticTypeError("cannot resolve dynamic method name at compile time.", call);
            return;
        }

        if (extension.beforeMethodCall(call)) {
            extension.afterMethodCall(call);
            return;
        }

        Expression callArguments = call.getArguments();

        ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(callArguments);

        checkForbiddenSpreadArgument(argumentList);

        ClassNode receiver = call.getOwnerType();
        visitMethodCallArguments(receiver, argumentList, false, null);

        ClassNode[] args = getArgumentTypes(argumentList);

        try {

            // method call receivers are :
            //   - possible "with" receivers
            //   - the actual receiver as found in the method call expression
            //   - any of the potential receivers found in the instanceof temporary table
            // in that order
            List<Receiver<String>> receivers = new LinkedList<>();
            addReceivers(receivers, makeOwnerList(new ClassExpression(receiver)), false);
            List<MethodNode> mn = null;
            Receiver<String> chosenReceiver = null;
            for (Receiver<String> currentReceiver : receivers) {
                mn = findMethod(currentReceiver.getType(), name, args);
                if (!mn.isEmpty()) {
                    if (mn.size() == 1)
                        typeCheckMethodsWithGenericsOrFail(currentReceiver.getType(), args, mn.get(0), call);
                    chosenReceiver = currentReceiver;
                    break;
                }
            }
            if (mn.isEmpty()) {
                mn = extension.handleMissingMethod(receiver, name, argumentList, args, call);
            }
            boolean callArgsVisited = false;
            if (mn.isEmpty()) {
                addNoMatchingMethodError(receiver, name, args, call);
            } else {
                mn = disambiguateMethods(mn, receiver, args, call);
                if (mn.size() == 1) {
                    MethodNode directMethodCallCandidate = mn.get(0);
                    ClassNode returnType = getType(directMethodCallCandidate);
                    if (returnType.isUsingGenerics() && !returnType.isEnum()) {
                        visitMethodCallArguments(receiver, argumentList, true, directMethodCallCandidate);
                        ClassNode irtg = inferReturnTypeGenerics(chosenReceiver.getType(), directMethodCallCandidate, callArguments);
                        returnType = irtg != null && implementsInterfaceOrIsSubclassOf(irtg, returnType) ? irtg : returnType;
                        callArgsVisited = true;
                    }
                    storeType(call, returnType);
                    storeTargetMethod(call, directMethodCallCandidate);

                } else {
                    addAmbiguousErrorMessage(mn, name, args, call);
                }
                if (!callArgsVisited) {
                    visitMethodCallArguments(receiver, argumentList, true, call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET));
                }
            }
        } finally {
            extension.afterMethodCall(call);
        }
    }

    /**
     * @deprecated this method is unused, replaced with {@link DelegatesTo} inference.
     */
    @Deprecated
    protected void checkClosureParameters(final Expression callArguments, final ClassNode receiver) {
        if (callArguments instanceof ArgumentListExpression) {
            ArgumentListExpression argList = (ArgumentListExpression) callArguments;
            ClosureExpression closure = (ClosureExpression) argList.getExpression(0);
            Parameter[] parameters = closure.getParameters();
            if (parameters.length > 1) {
                addStaticTypeError("Unexpected number of parameters for a with call", argList);
            } else if (parameters.length == 1) {
                Parameter param = parameters[0];
                if (!param.isDynamicTyped() && !isAssignableTo(receiver, param.getType().redirect())) {
                    addStaticTypeError("Expected parameter type: " + receiver.toString(false) + " but was: " + param.getType().redirect().toString(false), param);
                }
            }
            closure.putNodeMetaData(DELEGATION_METADATA, new DelegationMetadata(
                    receiver,
                    Closure.DELEGATE_FIRST,
                    typeCheckingContext.delegationMetadata
            ));
        }
    }

    /**
     * Visits a method call target, to infer the type. Don't report errors right
     * away, that will be done by a later visitMethod call.
     */
    protected void silentlyVisitMethodNode(final MethodNode directMethodCallCandidate) {
        // visit is authorized because the classnode belongs to the same source unit
        ErrorCollector collector = new ErrorCollector(typeCheckingContext.getErrorCollector().getConfiguration());
        startMethodInference(directMethodCallCandidate, collector);
    }

    protected void visitMethodCallArguments(final ClassNode receiver, final ArgumentListExpression arguments, final boolean visitClosures, final MethodNode selectedMethod) {
        Parameter[] params = selectedMethod != null ? selectedMethod.getParameters() : Parameter.EMPTY_ARRAY;
        List<Expression> expressions = new LinkedList<>(arguments.getExpressions());
        if (selectedMethod instanceof ExtensionMethodNode) {
            params = ((ExtensionMethodNode) selectedMethod).getExtensionMethodNode().getParameters();
            expressions.add(0, varX("$self", receiver));
        }
        ArgumentListExpression newArgs = args(expressions);

        for (int i = 0, expressionsSize = expressions.size(); i < expressionsSize; i++) {
            Expression expression = expressions.get(i);
            if (visitClosures && expression instanceof ClosureExpression
                    || !visitClosures && !(expression instanceof ClosureExpression)) {
                if (i < params.length && visitClosures) {
                    Parameter param = params[i];
                    checkClosureWithDelegatesTo(receiver, selectedMethod, newArgs, params, expression, param);
                    if (selectedMethod instanceof ExtensionMethodNode) {
                        if (i > 0) {
                            inferClosureParameterTypes(receiver, arguments, (ClosureExpression) expression, param, selectedMethod);
                        }
                    } else {
                        inferClosureParameterTypes(receiver, newArgs, (ClosureExpression) expression, param, selectedMethod);
                    }
                }
                expression.visit(this);
                if (expression.getNodeMetaData(DELEGATION_METADATA) != null) {
                    expression.removeNodeMetaData(DELEGATION_METADATA);
                }
            }
        }
        if (expressions.size() > 0 && expressions.get(0) instanceof MapExpression && params.length > 0) {
            checkNamedParamsAnnotation(params[0], (MapExpression) expressions.get(0));
        }
    }

    private void checkNamedParamsAnnotation(final Parameter param, final MapExpression args) {
        if (!isOrImplements(param.getType(), MAP_TYPE)) return;
        List<MapEntryExpression> entryExpressions = args.getMapEntryExpressions();
        Map<Object, Expression> entries = new LinkedHashMap<>();
        for (MapEntryExpression entry : entryExpressions) {
            Object key = entry.getKeyExpression();
            if (key instanceof ConstantExpression) {
                key = ((ConstantExpression) key).getValue();
            }
            entries.put(key, entry.getValueExpression());
        }
        List<AnnotationNode> annotations = param.getAnnotations(NAMED_PARAMS_CLASSNODE);
        if (annotations != null && !annotations.isEmpty()) {
            AnnotationNode an = null;
            for (AnnotationNode next : annotations) {
                if (next.getClassNode().getName().equals(NamedParams.class.getName())) {
                    an = next;
                }
            }
            List<String> collectedNames = new ArrayList<>();
            if (an != null) {
                Expression value = an.getMember("value");
                if (value instanceof AnnotationConstantExpression) {
                    processNamedParam((AnnotationConstantExpression) value, entries, args, collectedNames);
                } else if (value instanceof ListExpression) {
                    ListExpression le = (ListExpression) value;
                    for (Expression next : le.getExpressions()) {
                        if (next instanceof AnnotationConstantExpression) {
                            processNamedParam((AnnotationConstantExpression) next, entries, args, collectedNames);
                        }
                    }
                }
                for (Map.Entry<Object, Expression> entry : entries.entrySet()) {
                    if (!collectedNames.contains(entry.getKey())) {
                        addStaticTypeError("unexpected named arg: " + entry.getKey(), args);
                    }
                }
            }
        }
    }

    private void processNamedParam(final AnnotationConstantExpression value, final Map<Object, Expression> entries, final Expression expression, final List<String> collectedNames) {
        AnnotationNode namedParam = (AnnotationNode) value.getValue();
        if (!namedParam.getClassNode().getName().equals(NamedParam.class.getName())) return;
        String name = null;
        boolean required = false;
        ClassNode expectedType = null;
        ConstantExpression constX = (ConstantExpression) namedParam.getMember("value");
        if (constX != null) {
            name = (String) constX.getValue();
            collectedNames.add(name);
        }
        constX = (ConstantExpression) namedParam.getMember("required");
        if (constX != null) {
            required = (Boolean) constX.getValue();
        }
        ClassExpression typeX = (ClassExpression) namedParam.getMember("type");
        if (typeX != null) {
            expectedType = typeX.getType();
        }
        if (!entries.containsKey(name)) {
            if (required) {
                addStaticTypeError("required named arg '" + name + "' not found.", expression);
            }
        } else {
            Expression supplied = entries.get(name);
            if (isCompatibleType(expectedType, expectedType != null, supplied.getType())) {
                addStaticTypeError("parameter for named arg '" + name + "' has type '" + prettyPrintType(supplied.getType()) +
                        "' but expected '" + prettyPrintType(expectedType) + "'.", expression);
            }
        }
    }

    private boolean isCompatibleType(final ClassNode expectedType, final boolean b, final ClassNode type) {
        return b && !isAssignableTo(type, expectedType);
    }

    /**
     * This method is responsible for performing type inference on closure argument types whenever code like this is
     * found: <code>foo.collect { it.toUpperCase() }</code>.
     * In this case, the type checker tries to find if the <code>collect</code> method has its {@link Closure} argument
     * annotated with {@link groovy.transform.stc.ClosureParams}. If yes, then additional type inference can be performed
     * and the type of <code>it</code> may be inferred.
     *
     * @param receiver
     * @param arguments
     * @param expression     a closure expression for which the argument types should be inferred
     * @param param          the parameter where to look for a {@link groovy.transform.stc.ClosureParams} annotation.
     * @param selectedMethod the method accepting a closure
     */
    protected void inferClosureParameterTypes(final ClassNode receiver, final Expression arguments, final ClosureExpression expression, final Parameter param, final MethodNode selectedMethod) {
        List<AnnotationNode> annotations = param.getAnnotations(CLOSUREPARAMS_CLASSNODE);
        if (annotations != null && !annotations.isEmpty()) {
            for (AnnotationNode annotation : annotations) {
                Expression hintClass = annotation.getMember("value");
                Expression options = annotation.getMember("options");
                Expression resolverClass = annotation.getMember("conflictResolutionStrategy");
                if (hintClass instanceof ClassExpression) {
                    doInferClosureParameterTypes(receiver, arguments, expression, selectedMethod, hintClass, resolverClass, options);
                }
            }
        } else if (isSAMType(param.getOriginType())) {
            // SAM coercion
            inferSAMType(param, receiver, selectedMethod, InvocationWriter.makeArgumentList(arguments), expression);
        }
    }

    /**
     * In a method call with SAM coercion the inference is to be understood as a
     * two phase process.  We have the normal method call to the target method
     * with the closure argument and we have the SAM method that will be called
     * inside the normal target method.  To infer correctly we have to "simulate"
     * this process. We know the call to the closure will be done through the SAM
     * type, so the SAM type generics deliver information about the Closure.  At
     * the same time the SAM class is used in the target method parameter,
     * providing a connection from the SAM type and the target method's class.
     */
    private void inferSAMType(final Parameter param, final ClassNode receiver, final MethodNode methodWithSAMParameter, final ArgumentListExpression originalMethodCallArguments, final ClosureExpression openBlock) {
        // first we try to get as much information about the declaration class through the receiver
        Map<GenericsTypeName, GenericsType> targetMethodConnections = new HashMap<>();
        for (ClassNode face : receiver.getAllInterfaces()) {
            extractGenericsConnections(targetMethodConnections, getCorrectedClassNode(receiver, face, true), face.redirect());
        }
        if (!receiver.isInterface()) {
            extractGenericsConnections(targetMethodConnections, receiver, receiver.redirect());
        }

        // then we use the method with the SAM-type parameter to get more information about the declaration
        Parameter[] parametersOfMethodContainingSAM = methodWithSAMParameter.getParameters();
        for (int i = 0, n = parametersOfMethodContainingSAM.length; i < n; i += 1) {
            ClassNode parameterType = parametersOfMethodContainingSAM[i].getType();
            // potentially skip empty varargs
            if (i == (n - 1) && i == originalMethodCallArguments.getExpressions().size() && parameterType.isArray()) {
                continue;
            }
            Expression callArg = originalMethodCallArguments.getExpression(i);
            // we look at the closure later in detail, so skip it here
            if (callArg == openBlock) {
                continue;
            }
            extractGenericsConnections(targetMethodConnections, getType(callArg), parameterType);
        }

        // To make a connection to the SAM class we use that new information
        // to replace the generics in the SAM type parameter of the target
        // method and than that to make the connections to the SAM type generics
        ClassNode paramTypeWithReceiverInformation = applyGenericsContext(targetMethodConnections, param.getOriginType());
        Map<GenericsTypeName, GenericsType> samTypeConnections = new HashMap<>();
        ClassNode samTypeRedirect = paramTypeWithReceiverInformation.redirect();
        extractGenericsConnections(samTypeConnections, paramTypeWithReceiverInformation, samTypeRedirect);

        // should the open block provide final information we apply that
        // to the corresponding parameters of the SAM type method
        MethodNode abstractMethod = findSAM(samTypeRedirect);
        ClassNode[] abstractMethodParamTypes = extractTypesFromParameters(abstractMethod.getParameters());
        ClassNode[] blockParamTypes = openBlock.getNodeMetaData(CLOSURE_ARGUMENTS);
        if (blockParamTypes == null) {
            Parameter[] p = openBlock.getParameters();
            if (p == null) {
                // zero parameter closure e.g. { -> println 'no args' }
                blockParamTypes = ClassNode.EMPTY_ARRAY;
            } else if (p.length == 0 && abstractMethodParamTypes.length != 0) {
                // implicit it
                blockParamTypes = abstractMethodParamTypes;
            } else {
                blockParamTypes = new ClassNode[p.length];
                for (int i = 0, n = p.length; i < n; i += 1) {
                    if (p[i] != null && !p[i].isDynamicTyped()) {
                        blockParamTypes[i] = p[i].getType();
                    } else {
                        blockParamTypes[i] = typeOrNull(abstractMethodParamTypes, i);
                    }
                }
            }
        }
        for (int i = 0, n = blockParamTypes.length; i < n; i += 1) {
            extractGenericsConnections(samTypeConnections, blockParamTypes[i], typeOrNull(abstractMethodParamTypes, i));
        }

        // finally apply the generics information to the parameters and
        // store the type of parameter and block type as meta information
        for (int i = 0, n = blockParamTypes.length; i < n; i += 1) {
            blockParamTypes[i] = applyGenericsContext(samTypeConnections, typeOrNull(abstractMethodParamTypes, i));
        }

        tryToInferUnresolvedBlockParameterType(paramTypeWithReceiverInformation, abstractMethod, blockParamTypes);

        openBlock.putNodeMetaData(CLOSURE_ARGUMENTS, blockParamTypes);
    }

    private void tryToInferUnresolvedBlockParameterType(final ClassNode paramTypeWithReceiverInformation, final MethodNode methodForSAM, final ClassNode[] blockParameterTypes) {
        List<Integer> indexList = new LinkedList<>();
        for (int i = 0, n = blockParameterTypes.length; i < n; i += 1) {
            ClassNode blockParameterType = blockParameterTypes[i];
            if (blockParameterType != null && blockParameterType.isGenericsPlaceHolder()) {
                indexList.add(i);
            }
        }

        if (!indexList.isEmpty()) {
            // If the parameter type failed to resolve, try to find the parameter type through the class hierarchy
            Map<GenericsType, GenericsType> genericsTypeMap = GenericsUtils.makeDeclaringAndActualGenericsTypeMapOfExactType(methodForSAM.getDeclaringClass(), paramTypeWithReceiverInformation);

            for (Integer index : indexList) {
                for (Map.Entry<GenericsType, GenericsType> entry : genericsTypeMap.entrySet()) {
                    if (entry.getKey().getName().equals(blockParameterTypes[index].getUnresolvedName())) {
                        ClassNode type = entry.getValue().getType();
                        if (type != null && !type.isGenericsPlaceHolder()) {
                            blockParameterTypes[index] = type;
                        }
                        break;
                    }
                }
            }
        }
    }

    private static ClassNode typeOrNull(final ClassNode[] parameterTypesForSAM, final int i) {
        return i < parameterTypesForSAM.length ? parameterTypesForSAM[i] : null;
    }

    private List<ClassNode[]> getSignaturesFromHint(final ClosureExpression expression, final MethodNode selectedMethod, final Expression hintClass, final Expression options) {
        // initialize hints
        List<ClassNode[]> closureSignatures;
        try {
            ClassLoader transformLoader = getTransformLoader();
            @SuppressWarnings("unchecked")
            Class<? extends ClosureSignatureHint> hint = (Class<? extends ClosureSignatureHint>) transformLoader.loadClass(hintClass.getText());
            ClosureSignatureHint hintInstance = hint.getDeclaredConstructor().newInstance();
            closureSignatures = hintInstance.getClosureSignatures(
                    selectedMethod instanceof ExtensionMethodNode ? ((ExtensionMethodNode) selectedMethod).getExtensionMethodNode() : selectedMethod,
                    typeCheckingContext.getSource(),
                    typeCheckingContext.getCompilationUnit(),
                    convertToStringArray(options), expression);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new GroovyBugError(e);
        }
        return closureSignatures;
    }

    private List<ClassNode[]> resolveWithResolver(final List<ClassNode[]> candidates, final ClassNode receiver, final Expression arguments, final ClosureExpression expression, final MethodNode selectedMethod, final Expression resolverClass, final Expression options) {
        // initialize resolver
        try {
            ClassLoader transformLoader = getTransformLoader();
            @SuppressWarnings("unchecked")
            Class<? extends ClosureSignatureConflictResolver> resolver = (Class<? extends ClosureSignatureConflictResolver>) transformLoader.loadClass(resolverClass.getText());
            ClosureSignatureConflictResolver resolverInstance = resolver.getDeclaredConstructor().newInstance();
            return resolverInstance.resolve(
                    candidates,
                    receiver,
                    arguments,
                    expression,
                    selectedMethod instanceof ExtensionMethodNode ? ((ExtensionMethodNode) selectedMethod).getExtensionMethodNode() : selectedMethod,
                    typeCheckingContext.getSource(),
                    typeCheckingContext.getCompilationUnit(),
                    convertToStringArray(options));
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new GroovyBugError(e);
        }
    }

    private ClassLoader getTransformLoader() {
        return Optional.ofNullable(typeCheckingContext.getCompilationUnit()).map(CompilationUnit::getTransformLoader).orElseGet(() -> getSourceUnit().getClassLoader());
    }

    private void doInferClosureParameterTypes(final ClassNode receiver, final Expression arguments, final ClosureExpression expression, final MethodNode selectedMethod, final Expression hintClass, final Expression resolverClass, final Expression options) {
        Parameter[] closureParams = expression.getParameters();
        if (closureParams == null) return; // no-arg closure

        List<ClassNode[]> closureSignatures = getSignaturesFromHint(expression, selectedMethod, hintClass, options);
        List<ClassNode[]> candidates = new LinkedList<>();
        for (ClassNode[] signature : closureSignatures) {
            // in order to compute the inferred types of the closure parameters, we're using the following trick:
            // 1. create a dummy MethodNode for which the return type is a class node for which the generic types are the types returned by the hint
            // 2. call inferReturnTypeGenerics
            // 3. fetch inferred types from the result of inferReturnTypeGenerics
            // In practice, it could be done differently but it has the main advantage of reusing
            // existing code, hence reducing the amount of code to debug in case of failure.
            ClassNode[] inferred = resolveGenericsFromTypeHint(receiver, arguments, selectedMethod, signature);
            if (signature.length == closureParams.length // same number of arguments
                    || (signature.length == 1 && closureParams.length == 0) // implicit it
                    || (closureParams.length > signature.length && inferred[inferred.length - 1].isArray())) { // vargs
                candidates.add(inferred);
            }
        }
        if (candidates.size() > 1) {
            Iterator<ClassNode[]> candIt = candidates.iterator();
            while (candIt.hasNext()) {
                ClassNode[] inferred = candIt.next();
                for (int i = 0, n = closureParams.length; i < n; i += 1) {
                    Parameter closureParam = closureParams[i];
                    ClassNode originType = closureParam.getOriginType();
                    ClassNode inferredType;
                    if (i < inferred.length - 1 || inferred.length == closureParams.length) {
                        inferredType = inferred[i];
                    } else { // vargs?
                        ClassNode lastArgInferred = inferred[inferred.length - 1];
                        if (lastArgInferred.isArray()) {
                            inferredType = lastArgInferred.getComponentType();
                        } else {
                            candIt.remove();
                            continue;
                        }
                    }
                    if (!typeCheckMethodArgumentWithGenerics(originType, inferredType, i == (n - 1))) {
                        candIt.remove();
                    }
                }
            }
            if (candidates.size() > 1 && resolverClass instanceof ClassExpression) {
                candidates = resolveWithResolver(candidates, receiver, arguments, expression, selectedMethod, resolverClass, options);
            }
            if (candidates.size() > 1) {
                addError("Ambiguous prototypes for closure. More than one target method matches. Please use explicit argument types.", expression);
            }
        }
        if (candidates.size() == 1) {
            ClassNode[] inferred = candidates.get(0);
            if (closureParams.length == 0 && inferred.length == 1) {
                expression.putNodeMetaData(CLOSURE_ARGUMENTS, inferred);
            } else {
                for (int i = 0, n = closureParams.length; i < n; i += 1) {
                    Parameter closureParam = closureParams[i];
                    ClassNode originType = closureParam.getOriginType();
                    ClassNode inferredType = OBJECT_TYPE;
                    if (i < inferred.length - 1 || inferred.length == closureParams.length) {
                        inferredType = inferred[i];
                    } else { // vargs?
                        ClassNode lastArgInferred = inferred[inferred.length - 1];
                        if (lastArgInferred.isArray()) {
                            inferredType = lastArgInferred.getComponentType();
                        } else {
                            addError("Incorrect number of parameters. Expected " + inferred.length + " but found " + closureParams.length, expression);
                        }
                    }
                    boolean lastArg = i == (n - 1);

                    if (!typeCheckMethodArgumentWithGenerics(originType, inferredType, lastArg)) {
                        addError("Expected parameter of type " + inferredType.toString(false) + " but got " + originType.toString(false), closureParam.getType());
                    }

                    typeCheckingContext.controlStructureVariables.put(closureParam, inferredType);
                }
            }
        }
    }

    private ClassNode[] resolveGenericsFromTypeHint(final ClassNode receiver, final Expression arguments, final MethodNode selectedMethod, final ClassNode[] signature) {
        ClassNode dummyResultNode = new ClassNode("ClForInference$" + UNIQUE_LONG.incrementAndGet(), 0, OBJECT_TYPE).getPlainNodeReference();
        GenericsType[] genericTypes = new GenericsType[signature.length];
        for (int i = 0, n = signature.length; i < n; i += 1) {
            genericTypes[i] = new GenericsType(signature[i]);
        }
        dummyResultNode.setGenericsTypes(genericTypes);
        MethodNode dummyMN = selectedMethod instanceof ExtensionMethodNode ? ((ExtensionMethodNode) selectedMethod).getExtensionMethodNode() : selectedMethod;
        dummyMN = new MethodNode(
                dummyMN.getName(),
                dummyMN.getModifiers(),
                dummyResultNode,
                dummyMN.getParameters(),
                dummyMN.getExceptions(),
                EmptyStatement.INSTANCE
        );
        dummyMN.setDeclaringClass(selectedMethod.getDeclaringClass());
        dummyMN.setGenericsTypes(selectedMethod.getGenericsTypes());
        if (selectedMethod instanceof ExtensionMethodNode) {
            ExtensionMethodNode orig = (ExtensionMethodNode) selectedMethod;
            dummyMN = new ExtensionMethodNode(
                    dummyMN,
                    dummyMN.getName(),
                    dummyMN.getModifiers(),
                    dummyResultNode,
                    orig.getParameters(),
                    orig.getExceptions(),
                    EmptyStatement.INSTANCE,
                    orig.isStaticExtension()
            );
            dummyMN.setDeclaringClass(orig.getDeclaringClass());
            dummyMN.setGenericsTypes(orig.getGenericsTypes());
        }
        ClassNode classNode = inferReturnTypeGenerics(receiver, dummyMN, arguments);
        ClassNode[] inferred = new ClassNode[classNode.getGenericsTypes().length];
        for (int i = 0; i < classNode.getGenericsTypes().length; i++) {
            GenericsType genericsType = classNode.getGenericsTypes()[i];
            ClassNode value = createUsableClassNodeFromGenericsType(genericsType);
            inferred[i] = value;
        }
        return inferred;
    }

    /**
     * Given a GenericsType instance, returns a ClassNode which can be used as an inferred type.
     *
     * @param genericsType a {@link org.codehaus.groovy.ast.GenericsType} representing either a type, a placeholder or a wildcard
     * @return a class node usable as an inferred type
     */
    private static ClassNode createUsableClassNodeFromGenericsType(final GenericsType genericsType) {
        ClassNode value = genericsType.getType();
        if (genericsType.isPlaceholder()) {
            value = OBJECT_TYPE;
        }
        ClassNode lowerBound = genericsType.getLowerBound();
        if (lowerBound != null) {
            value = lowerBound;
        } else {
            ClassNode[] upperBounds = genericsType.getUpperBounds();
            if (upperBounds != null) {
                value = WideningCategories.lowestUpperBound(Arrays.asList(upperBounds));
            }
        }
        return value;
    }

    private static String[] convertToStringArray(final Expression options) {
        if (options == null) {
            return ResolveVisitor.EMPTY_STRING_ARRAY;
        }
        if (options instanceof ConstantExpression) {
            return new String[]{options.getText()};
        }
        if (options instanceof ListExpression) {
            return ((ListExpression) options).getExpressions().stream().map(Expression::getText).toArray(String[]::new);
        }
        throw new IllegalArgumentException("Unexpected options for @ClosureParams:" + options);
    }

    private void checkClosureWithDelegatesTo(final ClassNode receiver, final MethodNode mn, final ArgumentListExpression arguments, final Parameter[] params, final Expression expression, final Parameter param) {
        List<AnnotationNode> annotations = param.getAnnotations(DELEGATES_TO);
        if (annotations != null && !annotations.isEmpty()) {
            for (AnnotationNode annotation : annotations) {
                // in theory, there can only be one annotation of that type
                Expression value = annotation.getMember("value");
                Expression strategy = annotation.getMember("strategy");
                Expression genericTypeIndex = annotation.getMember("genericTypeIndex");
                Expression type = annotation.getMember("type");
                Integer stInt = Closure.OWNER_FIRST;
                if (strategy != null) {
                    stInt = (Integer) evaluateExpression(castX(Integer_TYPE, strategy), getSourceUnit().getConfiguration());
                }
                if (value instanceof ClassExpression && !value.getType().equals(DELEGATES_TO_TARGET)) {
                    if (genericTypeIndex != null) {
                        addStaticTypeError("Cannot use @DelegatesTo(genericTypeIndex=" + genericTypeIndex.getText()
                                + ") without @DelegatesTo.Target because generic argument types are not available at runtime", value);
                    }
                    // temporarily store the delegation strategy and the delegate type
                    expression.putNodeMetaData(DELEGATION_METADATA, new DelegationMetadata(value.getType(), stInt, typeCheckingContext.delegationMetadata));
                } else if (type != null && !"".equals(type.getText()) && type instanceof ConstantExpression) {
                    String typeString = type.getText();
                    ClassNode[] resolved = GenericsUtils.parseClassNodesFromString(
                            typeString,
                            getSourceUnit(),
                            typeCheckingContext.getCompilationUnit(),
                            mn,
                            type
                    );
                    if (resolved != null) {
                        if (resolved.length == 1) {
                            resolved = resolveGenericsFromTypeHint(receiver, arguments, mn, resolved);
                            expression.putNodeMetaData(DELEGATION_METADATA, new DelegationMetadata(resolved[0], stInt, typeCheckingContext.delegationMetadata));
                        } else {
                            addStaticTypeError("Incorrect type hint found in method " + (mn), type);
                        }
                    }
                } else {
                    List<Expression> expressions = arguments.getExpressions();
                    int expressionsSize = expressions.size();
                    Expression parameter = annotation.getMember("target");
                    String parameterName = parameter instanceof ConstantExpression ? parameter.getText() : "";
                    // todo: handle vargs!
                    for (int j = 0, paramsLength = params.length; j < paramsLength; j++) {
                        Parameter methodParam = params[j];
                        List<AnnotationNode> targets = methodParam.getAnnotations(DELEGATES_TO_TARGET);
                        if (targets != null && targets.size() == 1) {
                            AnnotationNode targetAnnotation = targets.get(0); // @DelegatesTo.Target Obj foo
                            Expression idMember = targetAnnotation.getMember("value");
                            String id = idMember instanceof ConstantExpression ? idMember.getText() : "";
                            if (id.equals(parameterName)) {
                                if (j < expressionsSize) {
                                    Expression actualArgument = expressions.get(j);
                                    ClassNode actualType = getType(actualArgument);
                                    if (genericTypeIndex instanceof ConstantExpression) {
                                        int gti = Integer.parseInt(genericTypeIndex.getText());
                                        ClassNode paramType = methodParam.getType(); // type annotated with @DelegatesTo.Target
                                        GenericsType[] genericsTypes = paramType.getGenericsTypes();
                                        if (genericsTypes == null) {
                                            addStaticTypeError("Cannot use @DelegatesTo(genericTypeIndex=" + genericTypeIndex.getText()
                                                    + ") with a type that doesn't use generics", methodParam);
                                        } else if (gti < 0 || gti >= genericsTypes.length) {
                                            addStaticTypeError("Index of generic type @DelegatesTo(genericTypeIndex=" + genericTypeIndex.getText()
                                                    + ") " + (gti < 0 ? "lower" : "greater") + " than those of the selected type", methodParam);
                                        } else {
                                            ClassNode pType = GenericsUtils.parameterizeType(actualType, paramType);
                                            GenericsType[] pTypeGenerics = pType.getGenericsTypes();
                                            if (pTypeGenerics != null && pTypeGenerics.length > gti) {
                                                actualType = pTypeGenerics[gti].getType();
                                            } else {
                                                addStaticTypeError("Unable to map actual type [" + actualType.toString(false) + "] onto " + paramType.toString(false), methodParam);
                                            }
                                        }
                                    }
                                    expression.putNodeMetaData(DELEGATION_METADATA, new DelegationMetadata(actualType, stInt, typeCheckingContext.delegationMetadata));
                                    break;
                                }
                            }
                        }
                    }
                    if (expression.getNodeMetaData(DELEGATION_METADATA) == null) {
                        addError("Not enough arguments found for a @DelegatesTo method call. Please check that you either use an explicit class or @DelegatesTo.Target with a correct id", arguments);
                    }
                }
            }
        }
    }

    protected void addReceivers(final List<Receiver<String>> receivers, final Collection<Receiver<String>> owners, final boolean implicitThis) {
        if (!implicitThis || typeCheckingContext.delegationMetadata == null) {
            receivers.addAll(owners);
        } else {
            addReceivers(receivers, owners, typeCheckingContext.delegationMetadata, "");
        }
    }

    private static void addReceivers(final List<Receiver<String>> receivers, final Collection<Receiver<String>> owners, final DelegationMetadata dmd, final String path) {
        int strategy = dmd.getStrategy();
        switch (strategy) {
            case Closure.DELEGATE_ONLY:
            case Closure.DELEGATE_FIRST:
                addDelegateReceiver(receivers, dmd.getType(), path + "delegate");
                if (strategy == Closure.DELEGATE_FIRST) {
                    if (dmd.getParent() == null) {
                        receivers.addAll(owners);
                    } else {
                        //receivers.add(new Receiver<String>(CLOSURE_TYPE, path + "owner"));
                        addReceivers(receivers, owners, dmd.getParent(), path + "owner.");
                    }
                }
                break;
            case Closure.OWNER_ONLY:
            case Closure.OWNER_FIRST:
                if (dmd.getParent() == null) {
                    receivers.addAll(owners);
                } else {
                    //receivers.add(new Receiver<String>(CLOSURE_TYPE, path + "owner"));
                    addReceivers(receivers, owners, dmd.getParent(), path + "owner.");
                }
                if (strategy == Closure.OWNER_FIRST) {
                    addDelegateReceiver(receivers, dmd.getType(), path + "delegate");
                }
                break;
        }
    }

    private static void addDelegateReceiver(final List<Receiver<String>> receivers, final ClassNode delegate, final String path) {
        if (receivers.stream().map(Receiver::getType).noneMatch(delegate::equals)) {
            receivers.add(new Receiver<>(delegate, path));
        }
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        String name = call.getMethodAsString();
        if (name == null) {
            addStaticTypeError("cannot resolve dynamic method name at compile time.", call.getMethod());
            return;
        }
        if (extension.beforeMethodCall(call)) {
            extension.afterMethodCall(call);
            return;
        }
        typeCheckingContext.pushEnclosingMethodCall(call);
        Expression objectExpression = call.getObjectExpression();

        objectExpression.visit(this);
        call.getMethod().visit(this);

        // if the call expression is a spread operator call, then we must make sure that
        // the call is made on a collection type
        if (call.isSpreadSafe()) {
            // TODO: check if this should not be change to iterator based call logic
            ClassNode expressionType = getType(objectExpression);
            if (!implementsInterfaceOrIsSubclassOf(expressionType, Collection_TYPE) && !expressionType.isArray()) {
                addStaticTypeError("Spread operator can only be used on collection types", objectExpression);
                return;
            } else {
                // type check call as if it was made on component type
                ClassNode componentType = inferComponentType(expressionType, int_TYPE);
                MethodCallExpression subcall = callX(castX(componentType, EmptyExpression.INSTANCE), name, call.getArguments());
                subcall.setLineNumber(call.getLineNumber());
                subcall.setColumnNumber(call.getColumnNumber());
                subcall.setImplicitThis(call.isImplicitThis());
                visitMethodCallExpression(subcall);
                // the inferred type here should be a list of what the subcall returns
                ClassNode subcallReturnType = getType(subcall);
                ClassNode listNode = LIST_TYPE.getPlainNodeReference();
                listNode.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(subcallReturnType))});
                storeType(call, listNode);
                // store target method
                storeTargetMethod(call, subcall.getNodeMetaData(DIRECT_METHOD_CALL_TARGET));
                typeCheckingContext.popEnclosingMethodCall();
                return;
            }
        }

        Expression callArguments = call.getArguments();
        ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(callArguments);

        checkForbiddenSpreadArgument(argumentList);

        // for arguments, we need to visit closures *after* the method has been chosen

        ClassNode receiver = getType(objectExpression);
        visitMethodCallArguments(receiver, argumentList, false, null);

        ClassNode[] args = getArgumentTypes(argumentList);
        boolean isCallOnClosure = isClosureCall(name, objectExpression, callArguments);

        try {
            boolean callArgsVisited = false;
            if (isCallOnClosure) {
                // this is a closure.call() call
                if (isThisExpression(objectExpression)) {
                    // isClosureCall() check verified earlier that a field exists
                    FieldNode field = typeCheckingContext.getEnclosingClassNode().getDeclaredField(name);
                    GenericsType[] genericsTypes = field.getType().getGenericsTypes();
                    if (genericsTypes != null) {
                        ClassNode closureReturnType = genericsTypes[0].getType();
                        Object data = field.getNodeMetaData(CLOSURE_ARGUMENTS);
                        if (data != null) {
                            Parameter[] parameters = (Parameter[]) data;
                            typeCheckClosureCall(callArguments, args, parameters);
                        }
                        storeType(call, closureReturnType);
                    }
                } else if (objectExpression instanceof VariableExpression) {
                    Variable variable = findTargetVariable((VariableExpression) objectExpression);
                    if (variable instanceof ASTNode) {
                        Object data = ((ASTNode) variable).getNodeMetaData(CLOSURE_ARGUMENTS);
                        if (data != null) {
                            Parameter[] parameters = (Parameter[]) data;
                            typeCheckClosureCall(callArguments, args, parameters);
                        }
                        ClassNode type = getType(((ASTNode) variable));
                        if (type != null && type.equals(CLOSURE_TYPE)) {
                            GenericsType[] genericsTypes = type.getGenericsTypes();
                            type = OBJECT_TYPE;
                            if (genericsTypes != null) {
                                if (!genericsTypes[0].isPlaceholder()) {
                                    type = genericsTypes[0].getType();
                                }
                            }
                        }
                        if (type != null) {
                            storeType(call, type);
                        }
                    }
                } else if (objectExpression instanceof ClosureExpression) {
                    // we can get actual parameters directly
                    Parameter[] parameters = ((ClosureExpression) objectExpression).getParameters();
                    typeCheckClosureCall(callArguments, args, parameters);
                    ClassNode data = getInferredReturnType(objectExpression);
                    if (data != null) {
                        storeType(call, data);
                    }
                }

                int nbOfArgs;
                if (callArguments instanceof ArgumentListExpression) {
                    ArgumentListExpression list = (ArgumentListExpression) callArguments;
                    nbOfArgs = list.getExpressions().size();
                } else {
                    // todo : other cases
                    nbOfArgs = 0;
                }
                storeTargetMethod(call,
                        nbOfArgs == 0 ? CLOSURE_CALL_NO_ARG :
                                nbOfArgs == 1 ? CLOSURE_CALL_ONE_ARG :
                                        CLOSURE_CALL_VARGS);
            } else {
                // method call receivers are :
                //   - possible "with" receivers
                //   - the actual receiver as found in the method call expression
                //   - any of the potential receivers found in the instanceof temporary table
                // in that order
                List<Receiver<String>> receivers = new ArrayList<>();
                addReceivers(receivers, makeOwnerList(objectExpression), call.isImplicitThis());

                List<MethodNode> mn = null;
                Receiver<String> chosenReceiver = null;
                for (Receiver<String> currentReceiver : receivers) {
                    ClassNode receiverType = currentReceiver.getType();
                    mn = findMethod(receiverType, name, args);

                    // if the receiver is "this" or "implicit this", then we must make sure that the compatible
                    // methods are only static if we are in a static context
                    // if we are not in a static context but the current receiver is a static class, we must
                    // ensure that all methods are either static or declared by the current receiver or a superclass
                    if (!mn.isEmpty()
                            && (call.isImplicitThis() || isThisExpression(objectExpression))
                            && (typeCheckingContext.isInStaticContext || (receiverType.getModifiers() & Opcodes.ACC_STATIC) != 0)) {
                        // we create separate method lists just to be able to print out
                        // a nice error message to the user
                        // a method is accessible if it is static, or if we are not in a static context and it is
                        // declared by the current receiver or a superclass
                        List<MethodNode> accessibleMethods = new LinkedList<>();
                        List<MethodNode> inaccessibleMethods = new LinkedList<>();
                        for (final MethodNode node : mn) {
                            if (node.isStatic()
                                    || (!typeCheckingContext.isInStaticContext && implementsInterfaceOrIsSubclassOf(receiverType, node.getDeclaringClass()))) {
                                accessibleMethods.add(node);
                            } else {
                                inaccessibleMethods.add(node);
                            }
                        }
                        mn = accessibleMethods;
                        if (accessibleMethods.isEmpty()) {
                            // choose an arbitrary method to display an error message
                            MethodNode node = inaccessibleMethods.get(0);
                            ClassNode owner = node.getDeclaringClass();
                            addStaticTypeError("Non static method " + owner.getName() + "#" + node.getName() + " cannot be called from static context", call);
                        }
                    }

                    if (!mn.isEmpty()) {
                        chosenReceiver = currentReceiver;
                        break;
                    }
                }
                if (mn.isEmpty() && call.isImplicitThis() && isThisExpression(objectExpression) && typeCheckingContext.getEnclosingClosure() != null) {
                    mn = CLOSURE_TYPE.getDeclaredMethods(name);
                    if (!mn.isEmpty()) {
                        chosenReceiver = Receiver.make(CLOSURE_TYPE);
                        objectExpression.removeNodeMetaData(INFERRED_TYPE);
                    }
                }
                if (mn.isEmpty()) {
                    mn = extension.handleMissingMethod(receiver, name, argumentList, args, call);
                }
                if (mn.isEmpty()) {
                    addNoMatchingMethodError(receiver, name, args, call);
                } else {
                    if (areCategoryMethodCalls(mn, name, args)) {
                        addCategoryMethodCallError(call);
                    }
                    mn = disambiguateMethods(mn, chosenReceiver != null ? chosenReceiver.getType() : null, args, call);

                    if (mn.size() == 1) {
                        MethodNode directMethodCallCandidate = mn.get(0);
                        if (call.getNodeMetaData(DYNAMIC_RESOLUTION) == null
                                && !directMethodCallCandidate.isStatic() && objectExpression instanceof ClassExpression
                                && !"java.lang.Class".equals(directMethodCallCandidate.getDeclaringClass().getName())) {
                            ClassNode owner = directMethodCallCandidate.getDeclaringClass();
                            addStaticTypeError("Non static method " + owner.getName() + "#" + directMethodCallCandidate.getName() + " cannot be called from static context", call);
                        }
                        if (chosenReceiver == null) {
                            chosenReceiver = Receiver.make(directMethodCallCandidate.getDeclaringClass());
                        }

                        ClassNode returnType = getType(directMethodCallCandidate);
                        if (isUsingGenericsOrIsArrayUsingGenerics(returnType)) {
                            visitMethodCallArguments(chosenReceiver.getType(), argumentList, true, directMethodCallCandidate);
                            ClassNode irtg = inferReturnTypeGenerics(chosenReceiver.getType(), directMethodCallCandidate, callArguments, call.getGenericsTypes());
                            returnType = (irtg != null && implementsInterfaceOrIsSubclassOf(irtg, returnType) ? irtg : returnType);
                            callArgsVisited = true;
                        }
                        // GROOVY-6091: use of "delegate" or "getDelegate()" does not make use of @DelegatesTo metadata
                        if (directMethodCallCandidate == GET_DELEGATE && typeCheckingContext.getEnclosingClosure() != null) {
                            DelegationMetadata md = getDelegationMetadata(typeCheckingContext.getEnclosingClosure().getClosureExpression());
                            if (md != null) {
                                returnType = md.getType();
                            } else {
                                returnType = typeCheckingContext.getEnclosingClassNode();
                            }
                        }
                        // GROOVY-8961, GROOVY-9734
                        resolvePlaceholdersFromImplicitTypeHints(args, argumentList, directMethodCallCandidate);
                        if (typeCheckMethodsWithGenericsOrFail(chosenReceiver.getType(), args, directMethodCallCandidate, call)) {
                            returnType = adjustWithTraits(directMethodCallCandidate, chosenReceiver.getType(), args, returnType);

                            storeType(call, returnType);
                            storeTargetMethod(call, directMethodCallCandidate);

                            String data = chosenReceiver.getData();
                            if (data != null) {
                                // the method which has been chosen is supposed to be a call on delegate or owner
                                // so we store the information so that the static compiler may reuse it
                                call.putNodeMetaData(IMPLICIT_RECEIVER, data);
                            }
                            receiver = chosenReceiver.getType();

                            // if the object expression is a closure shared variable, we will have to perform a second pass
                            if (objectExpression instanceof VariableExpression) {
                                VariableExpression var = (VariableExpression) objectExpression;
                                if (var.isClosureSharedVariable()) {
                                    SecondPassExpression<ClassNode[]> wrapper = new SecondPassExpression<>(call, args);
                                    typeCheckingContext.secondPassExpressions.add(wrapper);
                                }
                            }
                        }
                    } else {
                        addAmbiguousErrorMessage(mn, name, args, call);
                    }
                }
            }
            // adjust typing for explicit math methods which have special handling - operator variants handled elsewhere
            if (NUMBER_OPS.containsKey(name) && isNumberType(receiver) && argumentList.getExpressions().size() == 1
                    && isNumberType(getType(argumentList.getExpression(0)))) {
                ClassNode right = getType(argumentList.getExpression(0));
                ClassNode resultType = getMathResultType(NUMBER_OPS.get(name), receiver, right, name);
                if (resultType != null) {
                    storeType(call, resultType);
                }
            }

            // now that a method has been chosen, we are allowed to visit the closures
            MethodNode target = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (!callArgsVisited) {
                visitMethodCallArguments(receiver, argumentList, true, target);
            }
            if (target != null) {
                List<Expression> arguments = argumentList.getExpressions();
                Parameter[] parameters = target.getParameters();
                for (int i = 0, n = Math.min(arguments.size(), parameters.length); i < n; i += 1) {
                    Expression argument = arguments.get(i);
                    ClassNode aType = getType(argument), pType = parameters[i].getType();
                    if (CLOSURE_TYPE.equals(aType) && CLOSURE_TYPE.equals(pType)) {
                        // GROOVY-8310: check closure generics
                        if (!isAssignableTo(aType, pType) /*&& !extension.handleIncompatibleReturnType(getReturnStatement(argument), aType)*/) {
                            addNoMatchingMethodError(receiver, name, getArgumentTypes(argumentList), call);
                            call.removeNodeMetaData(DIRECT_METHOD_CALL_TARGET);
                            break;
                        }
                        // GROOVY-7996: check delegation metadata of closure parameter used as method call argument
                        if (argument instanceof VariableExpression && ((VariableExpression) argument).getAccessedVariable() instanceof Parameter) {
                            // TODO: Check additional delegation metadata like type (see checkClosureWithDelegatesTo).
                            int incomingStrategy = getResolveStrategy((Parameter) ((VariableExpression) argument).getAccessedVariable());
                            int outgoingStrategy = getResolveStrategy(parameters[i]);
                            if (incomingStrategy != outgoingStrategy) {
                                addStaticTypeError("Closure parameter with resolve strategy " + getResolveStrategyName(incomingStrategy) + " passed to method with resolve strategy " + getResolveStrategyName(outgoingStrategy), argument);
                            }
                        }
                    }
                }
            }

            inferMethodReferenceType(call, receiver, argumentList);
        } finally {
            typeCheckingContext.popEnclosingMethodCall();
            extension.afterMethodCall(call);
        }
    }

    private int getResolveStrategy(final Parameter parameter) {
        List<AnnotationNode> annotations = parameter.getAnnotations(DELEGATES_TO);
        if (annotations != null && !annotations.isEmpty()) {
            Expression strategy = annotations.get(0).getMember("strategy");
            if (strategy != null) {
                return (Integer) evaluateExpression(castX(Integer_TYPE, strategy), getSourceUnit().getConfiguration());
            }
        }
        return Closure.OWNER_FIRST;
    }

    private void inferMethodReferenceType(final MethodCallExpression call, final ClassNode receiver, final ArgumentListExpression argumentList) {
        if (call == null) return;
        if (receiver == null) return;
        if (argumentList == null) return;

        List<Expression> argumentExpressionList = argumentList.getExpressions();
        if (argumentExpressionList == null) return;

        boolean noMethodReferenceParams = argumentExpressionList.stream().noneMatch(e -> e instanceof MethodReferenceExpression);
        if (noMethodReferenceParams) {
            return;
        }

        MethodNode selectedMethod = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (selectedMethod == null) return;

        Parameter[] parameters = selectedMethod.getParameters();

        List<Integer> methodReferenceParamIndexList = new LinkedList<>();
        List<Expression> newArgumentExpressionList = new LinkedList<>();
        for (int i = 0, n = argumentExpressionList.size(); i < n; i++) {
            Expression argumentExpression = argumentExpressionList.get(i);
            if (!(argumentExpression instanceof MethodReferenceExpression)) {
                newArgumentExpressionList.add(argumentExpression);
                continue;
            }

            Parameter param = parameters[i];
            ClassNode paramType = param.getType();

            if (!isFunctionalInterface(paramType.redirect())) {
                addError("The argument is a method reference, but the parameter type is not a functional interface", argumentExpression);
                newArgumentExpressionList.add(argumentExpression);
                continue;
            }

            LambdaExpression constructedLambdaExpression = constructLambdaExpressionForMethodReference(paramType);

            newArgumentExpressionList.add(constructedLambdaExpression);
            methodReferenceParamIndexList.add(i);
        }

        visitMethodCallArguments(receiver, new ArgumentListExpression(newArgumentExpressionList), true, selectedMethod);

        for (Integer methodReferenceParamIndex : methodReferenceParamIndexList) {
            LambdaExpression lambdaExpression = (LambdaExpression) newArgumentExpressionList.get(methodReferenceParamIndex);
            ClassNode[] argumentTypes = lambdaExpression.getNodeMetaData(CLOSURE_ARGUMENTS);
            argumentExpressionList.get(methodReferenceParamIndex).putNodeMetaData(CLOSURE_ARGUMENTS, argumentTypes);
        }
    }

    private LambdaExpression constructLambdaExpressionForMethodReference(final ClassNode paramType) {
        Parameter[] newParameters = createParametersForConstructedLambdaExpression(paramType);
        return new LambdaExpression(newParameters, block());
    }

    private Parameter[] createParametersForConstructedLambdaExpression(final ClassNode functionalInterfaceType) {
        MethodNode abstractMethodNode = findSAM(functionalInterfaceType);

        Parameter[] abstractMethodNodeParameters = abstractMethodNode.getParameters();
        if (abstractMethodNodeParameters == null) {
            abstractMethodNodeParameters = Parameter.EMPTY_ARRAY;
        }

        Parameter[] newParameters = new Parameter[abstractMethodNodeParameters.length];
        for (int i = 0; i < newParameters.length; i += 1) {
            newParameters[i] = new Parameter(DYNAMIC_TYPE, "p" + System.nanoTime());
        }
        return newParameters;
    }

    /**
     * A special method handling the "withTrait" call for which the type checker knows more than
     * what the type signature is able to tell. If "withTrait" is detected, then a new class node
     * is created representing the list of trait interfaces.
     *
     * @param directMethodCallCandidate a method selected by the type checker
     * @param receiver                  the receiver of the method call
     * @param args                      the arguments of the method call
     * @param returnType                the original return type, as inferred by the type checker
     * @return fixed return type if the selected method is {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#withTraits(Object, Class[]) withTraits}
     */
    private static ClassNode adjustWithTraits(final MethodNode directMethodCallCandidate, final ClassNode receiver, final ClassNode[] args, final ClassNode returnType) {
        if (directMethodCallCandidate instanceof ExtensionMethodNode) {
            ExtensionMethodNode emn = (ExtensionMethodNode) directMethodCallCandidate;
            if ("withTraits".equals(emn.getName()) && "DefaultGroovyMethods".equals(emn.getExtensionMethodNode().getDeclaringClass().getNameWithoutPackage())) {
                List<ClassNode> nodes = new LinkedList<>();
                Collections.addAll(nodes, receiver.getInterfaces());
                for (ClassNode arg : args) {
                    if (isClassClassNodeWrappingConcreteType(arg)) {
                        nodes.add(arg.getGenericsTypes()[0].getType());
                    } else {
                        nodes.add(arg);
                    }
                }
                return new WideningCategories.LowestUpperBoundClassNode(returnType.getName() + "Composed", OBJECT_TYPE, nodes.toArray(ClassNode.EMPTY_ARRAY));
            }
        }
        return returnType;
    }

    /**
     * Adds various getAt and setAt methods for primitive arrays.
     *
     * @param receiver the receiver class
     * @param name     the name of the method
     * @param args     the argument classes
     */
    private static void addArrayMethods(final List<MethodNode> methods, final ClassNode receiver, final String name, final ClassNode[] args) {
        if (args.length != 1) return;
        if (!receiver.isArray()) return;
        if (!isIntCategory(getUnwrapper(args[0]))) return;
        if ("getAt".equals(name)) {
            MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC, receiver.getComponentType(), new Parameter[]{new Parameter(args[0], "arg")}, null, null);
            node.setDeclaringClass(receiver.redirect());
            methods.add(node);
        } else if ("setAt".equals(name)) {
            MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC, VOID_TYPE, new Parameter[]{new Parameter(args[0], "arg")}, null, null);
            node.setDeclaringClass(receiver.redirect());
            methods.add(node);
        }
    }

    /**
     * In the case of a <em>Object.with { ... }</em> call, this method is supposed to retrieve
     * the inferred closure return type.
     *
     * @param callArguments the argument list from the <em>Object#with(Closure)</em> call, ie. a single closure expression
     * @return the inferred closure return type or <em>null</em>
     */
    protected ClassNode getInferredReturnTypeFromWithClosureArgument(final Expression callArguments) {
        if (!(callArguments instanceof ArgumentListExpression)) return null;

        ArgumentListExpression argList = (ArgumentListExpression) callArguments;
        ClosureExpression closure = (ClosureExpression) argList.getExpression(0);

        visitClosureExpression(closure);

        if (getInferredReturnType(closure) != null) {
            return getInferredReturnType(closure);
        }

        return null;
    }

    /**
     * Given an object expression (a receiver expression), generate the list of potential receiver types.
     *
     * @param objectExpression the receiver expression
     * @return the list of types the receiver may be
     */
    protected List<Receiver<String>> makeOwnerList(final Expression objectExpression) {
        ClassNode receiver = getType(objectExpression);
        List<Receiver<String>> owners = new ArrayList<>();
        if (isClassClassNodeWrappingConcreteType(receiver)) {
            ClassNode staticType = receiver.getGenericsTypes()[0].getType();
            owners.add(Receiver.make(staticType)); // Type from Class<Type>
            addTraitType(staticType, owners); // T in Class<T$Trait$Helper>
            owners.add(Receiver.make(receiver)); // Class<Type>
        } else {
            owners.add(Receiver.make(receiver));
            if (receiver.isInterface()) {
                owners.add(Receiver.make(OBJECT_TYPE));
            }
            addSelfTypes(receiver, owners);
            addTraitType(receiver, owners);
        }
        if (!typeCheckingContext.temporaryIfBranchTypeInformation.isEmpty()) {
            List<ClassNode> potentialReceiverType = getTemporaryTypesForExpression(objectExpression);
            if (potentialReceiverType != null && !potentialReceiverType.isEmpty()) {
                for (ClassNode node : potentialReceiverType) {
                    owners.add(Receiver.make(node));
                }
            }
        }
        if (typeCheckingContext.lastImplicitItType != null
                && objectExpression instanceof VariableExpression
                && ((VariableExpression) objectExpression).getName().equals("it")) {
            owners.add(Receiver.make(typeCheckingContext.lastImplicitItType));
        }
        if (typeCheckingContext.delegationMetadata != null
                && objectExpression instanceof VariableExpression
                && ((VariableExpression) objectExpression).getName().equals("owner")
                && /*isNested:*/typeCheckingContext.delegationMetadata.getParent() != null) {
            owners.clear();
            List<Receiver<String>> enclosingClass = Collections.singletonList(
                    Receiver.make(typeCheckingContext.getEnclosingClassNode()));
            addReceivers(owners, enclosingClass, typeCheckingContext.delegationMetadata.getParent(), "owner.");
        }
        return owners;
    }

    private static void addSelfTypes(final ClassNode receiver, final List<Receiver<String>> owners) {
        for (ClassNode selfType : Traits.collectSelfTypes(receiver, new LinkedHashSet<>())) {
            owners.add(Receiver.make(selfType));
        }
    }

    private static void addTraitType(final ClassNode receiver, final List<Receiver<String>> owners) {
        if (Traits.isTrait(receiver.getOuterClass()) && receiver.getName().endsWith("$Helper")) {
            ClassNode traitType = receiver.getOuterClass();
            owners.add(Receiver.make(traitType));
            addSelfTypes(traitType, owners);
        }
    }

    protected void checkForbiddenSpreadArgument(final ArgumentListExpression argumentList) {
        for (Expression arg : argumentList.getExpressions()) {
            if (arg instanceof SpreadExpression) {
                addStaticTypeError("The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time", arg);
            }
        }
    }

    protected List<ClassNode> getTemporaryTypesForExpression(final Expression objectExpression) {
        List<ClassNode> classNodes = null;
        int depth = typeCheckingContext.temporaryIfBranchTypeInformation.size();
        while (classNodes == null && depth > 0) {
            Map<Object, List<ClassNode>> tempo = typeCheckingContext.temporaryIfBranchTypeInformation.get(--depth);
            Object key = objectExpression instanceof ParameterVariableExpression
                    ? ((ParameterVariableExpression) objectExpression).parameter
                    : extractTemporaryTypeInfoKey(objectExpression);
            classNodes = tempo.get(key);
        }
        return classNodes;
    }

    protected void storeTargetMethod(final Expression call, final MethodNode directMethodCallCandidate) {
        call.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, directMethodCallCandidate);

        if (directMethodCallCandidate != null
                && directMethodCallCandidate.isStatic()
                && directMethodCallCandidate.getDeclaringClass().isInterface()
                && !(directMethodCallCandidate instanceof ExtensionMethodNode)) {
            typeCheckingContext.getEnclosingClassNode().putNodeMetaData(MINIMUM_BYTECODE_VERSION, Opcodes.V1_8);
        }

        checkOrMarkPrivateAccess(call, directMethodCallCandidate);
        checkSuperCallFromClosure(call, directMethodCallCandidate);
        extension.onMethodSelection(call, directMethodCallCandidate);
    }

    private void checkSuperCallFromClosure(final Expression call, final MethodNode directCallTarget) {
        if (call instanceof MethodCallExpression && typeCheckingContext.getEnclosingClosure() != null) {
            Expression objectExpression = ((MethodCallExpression) call).getObjectExpression();
            if (isSuperExpression(objectExpression)) {
                ClassNode current = typeCheckingContext.getEnclosingClassNode();
                current.getNodeMetaData(SUPER_MOP_METHOD_REQUIRED, x -> new LinkedList<>()).add(directCallTarget);
                call.putNodeMetaData(SUPER_MOP_METHOD_REQUIRED, current);
            }
        }
    }

    protected boolean isClosureCall(final String name, final Expression objectExpression, final Expression arguments) {
        if (objectExpression instanceof ClosureExpression && ("call".equals(name) || "doCall".equals(name))) return true;
        if (isThisExpression(objectExpression)) {
            FieldNode fieldNode = typeCheckingContext.getEnclosingClassNode().getDeclaredField(name);
            if (fieldNode != null && CLOSURE_TYPE.equals(fieldNode.getType())
                    && !typeCheckingContext.getEnclosingClassNode().hasPossibleMethod(name, arguments)) {
                return true;
            }
        } else if (!"call".equals(name) && !"doCall".equals(name)) {
            return false;
        }
        return getType(objectExpression).equals(CLOSURE_TYPE);
    }

    protected void typeCheckClosureCall(final Expression callArguments, final ClassNode[] args, final Parameter[] parameters) {
        if (allParametersAndArgumentsMatch(parameters, args) < 0 &&
                lastArgMatchesVarg(parameters, args) < 0) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0, parametersLength = parameters.length; i < parametersLength; i += 1) {
                Parameter parameter = parameters[i];
                sb.append(parameter.getType().getName());
                if (i < parametersLength - 1) sb.append(", ");
            }
            sb.append("]");
            addStaticTypeError("Closure argument types: " + sb + " do not match with parameter types: " + formatArgumentList(args), callArguments);
        }
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();

        try {
            // create a new temporary element in the if-then-else type info
            typeCheckingContext.pushTemporaryTypeInfo();
            visitStatement(ifElse);
            ifElse.getBooleanExpression().visit(this);
            ifElse.getIfBlock().visit(this);

            // pop if-then-else temporary type info
            typeCheckingContext.popTemporaryTypeInfo();

            // GROOVY-6099: restore assignment info as before the if branch
            restoreTypeBeforeConditional();

            ifElse.getElseBlock().visit(this);
        } finally {
            popAssignmentTracking(oldTracker);
        }
        BinaryExpression instanceOfExpression = findInstanceOfNotReturnExpression(ifElse);
        if (instanceOfExpression == null) {
            instanceOfExpression = findNotInstanceOfReturnExpression(ifElse);
        }
        if (instanceOfExpression != null) {
            if (!typeCheckingContext.enclosingBlocks.isEmpty()) {
                visitInstanceofNot(instanceOfExpression);
            }
        }
    }

    protected void visitInstanceofNot(final BinaryExpression be) {
        BlockStatement currentBlock = typeCheckingContext.enclosingBlocks.getFirst();
        assert currentBlock != null;
        if (typeCheckingContext.blockStatements2Types.containsKey(currentBlock)) {
            // another instanceOf_not was before, no need store vars
        } else {
            // saving type of variables to restoring them after returning from block
            Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
            getTypeCheckingContext().pushTemporaryTypeInfo();
            typeCheckingContext.blockStatements2Types.put(currentBlock, oldTracker);
        }
        pushInstanceOfTypeInfo(be.getLeftExpression(), be.getRightExpression());
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        if (block != null) {
            typeCheckingContext.enclosingBlocks.addFirst(block);
        }
        super.visitBlockStatement(block);
        if (block != null) {
            visitClosingBlock(block);
        }
    }

    public void visitClosingBlock(final BlockStatement block) {
        BlockStatement peekBlock = typeCheckingContext.enclosingBlocks.removeFirst();
        boolean found = typeCheckingContext.blockStatements2Types.containsKey(peekBlock);
        if (found) {
            Map<VariableExpression, List<ClassNode>> oldTracker = typeCheckingContext.blockStatements2Types.remove(peekBlock);
            getTypeCheckingContext().popTemporaryTypeInfo();
            popAssignmentTracking(oldTracker);
        }
    }

    /**
     * Check IfStatement matched pattern :
     * Object var1;
     * if (!(var1 instanceOf Runnable)) {
     * return
     * }
     * // Here var1 instance of Runnable
     * <p>
     * Return expression , which contains instanceOf (without not)
     * Return null, if not found
     */
    protected BinaryExpression findInstanceOfNotReturnExpression(final IfStatement ifElse) {
        Statement elseBlock = ifElse.getElseBlock();
        if (!(elseBlock instanceof EmptyStatement)) {
            return null;
        }
        Expression conditionExpression = ifElse.getBooleanExpression().getExpression();
        if (!(conditionExpression instanceof NotExpression)) {
            return null;
        }
        NotExpression notExpression = (NotExpression) conditionExpression;
        Expression expression = notExpression.getExpression();
        if (!(expression instanceof BinaryExpression)) {
            return null;
        }
        BinaryExpression instanceOfExpression = (BinaryExpression) expression;
        int op = instanceOfExpression.getOperation().getType();
        if (op != KEYWORD_INSTANCEOF) {
            return null;
        }
        if (notReturningBlock(ifElse.getIfBlock())) {
            return null;
        }
        return instanceOfExpression;
    }

    /**
     * Check IfStatement matched pattern :
     * Object var1;
     * if (var1 !instanceOf Runnable) {
     * return
     * }
     * // Here var1 instance of Runnable
     * <p>
     * Return expression , which contains instanceOf (without not)
     * Return null, if not found
     */
    protected BinaryExpression findNotInstanceOfReturnExpression(final IfStatement ifElse) {
        Statement elseBlock = ifElse.getElseBlock();
        if (!(elseBlock instanceof EmptyStatement)) {
            return null;
        }
        Expression conditionExpression = ifElse.getBooleanExpression().getExpression();
        if (!(conditionExpression instanceof BinaryExpression)) {
            return null;
        }
        BinaryExpression instanceOfExpression = (BinaryExpression) conditionExpression;
        int op = instanceOfExpression.getOperation().getType();
        if (op != COMPARE_NOT_INSTANCEOF) {
            return null;
        }
        if (notReturningBlock(ifElse.getIfBlock())) {
            return null;
        }
        return instanceOfExpression;
    }

    private boolean notReturningBlock(final Statement block) {
        if (!(block instanceof BlockStatement)) {
            return true;
        }
        BlockStatement bs = (BlockStatement) block;
        if (bs.getStatements().size() == 0) {
            return true;
        }
        Statement last = DefaultGroovyMethods.last(bs.getStatements());
        if (!(last instanceof ReturnStatement)) {
            return true;
        }
        return false;
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        typeCheckingContext.pushEnclosingSwitchStatement(statement);
        try {
            Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
            try {
                super.visitSwitch(statement);
            } finally {
                popAssignmentTracking(oldTracker);
            }
        } finally {
            typeCheckingContext.popEnclosingSwitchStatement();
        }
    }

    @Override
    protected void afterSwitchConditionExpressionVisited(final SwitchStatement statement) {
        Expression conditionExpression = statement.getExpression();
        conditionExpression.putNodeMetaData(TYPE, getType(conditionExpression));
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        super.visitCaseStatement(statement);
        restoreTypeBeforeConditional();
    }

    private void restoreTypeBeforeConditional() {
        Set<Map.Entry<VariableExpression, List<ClassNode>>> entries = typeCheckingContext.ifElseForWhileAssignmentTracker.entrySet();
        for (Map.Entry<VariableExpression, List<ClassNode>> entry : entries) {
            VariableExpression var = entry.getKey();
            List<ClassNode> items = entry.getValue();
            ClassNode originValue = items.get(0);
            storeType(var, originValue);
        }
    }

    protected Map<VariableExpression, ClassNode> popAssignmentTracking(final Map<VariableExpression, List<ClassNode>> oldTracker) {
        Map<VariableExpression, ClassNode> assignments = new HashMap<VariableExpression, ClassNode>();
        if (!typeCheckingContext.ifElseForWhileAssignmentTracker.isEmpty()) {
            for (Map.Entry<VariableExpression, List<ClassNode>> entry : typeCheckingContext.ifElseForWhileAssignmentTracker.entrySet()) {
                VariableExpression key = entry.getKey();
                List<ClassNode> allValues = entry.getValue();
                // GROOVY-6099: First element of the list may be null, if no assignment was made before the branch
                List<ClassNode> nonNullValues = new ArrayList<>(allValues.size());
                for (ClassNode value : allValues) {
                    if (value != null) nonNullValues.add(value);
                }
                ClassNode cn = lowestUpperBound(nonNullValues);
                storeType(key, cn);
                assignments.put(key, cn);
            }
        }
        typeCheckingContext.ifElseForWhileAssignmentTracker = oldTracker;
        return assignments;
    }

    protected Map<VariableExpression, List<ClassNode>> pushAssignmentTracking() {
        // memorize current assignment context
        Map<VariableExpression, List<ClassNode>> oldTracker = typeCheckingContext.ifElseForWhileAssignmentTracker;
        typeCheckingContext.ifElseForWhileAssignmentTracker = new HashMap<>();
        return oldTracker;
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        super.visitCastExpression(expression);
        if (!expression.isCoerce()) {
            ClassNode targetType = expression.getType();
            Expression source = expression.getExpression();
            ClassNode expressionType = getType(source);
            if (!checkCast(targetType, source) && !isDelegateOrOwnerInClosure(source)) {
                addStaticTypeError("Inconvertible types: cannot cast " + expressionType.toString(false) + " to " + targetType.toString(false), expression);
            }
        }
        storeType(expression, expression.getType());
    }

    private boolean isDelegateOrOwnerInClosure(final Expression exp) {
        return typeCheckingContext.getEnclosingClosure() != null && exp instanceof VariableExpression
                && (("delegate".equals(((VariableExpression) exp).getName())) || ("owner".equals(((VariableExpression) exp).getName())));
    }

    protected boolean checkCast(final ClassNode targetType, final Expression source) {
        boolean sourceIsNull = isNullConstant(source);
        ClassNode expressionType = getType(source);
        if (targetType.isArray() && expressionType.isArray()) {
            return checkCast(targetType.getComponentType(), varX("foo", expressionType.getComponentType()));
        } else if (targetType.equals(char_TYPE) && expressionType == STRING_TYPE
                && source instanceof ConstantExpression && source.getText().length() == 1) {
            // ex: (char) 'c'
        } else if (targetType.equals(Character_TYPE) && (expressionType == STRING_TYPE || sourceIsNull)
                && (sourceIsNull || source instanceof ConstantExpression && source.getText().length() == 1)) {
            // ex : (Character) 'c'
        } else if (isNumberCategory(getWrapper(targetType)) && (isNumberCategory(getWrapper(expressionType)) || char_TYPE == expressionType)) {
            // ex: short s = (short) 0
        } else if (sourceIsNull && !isPrimitiveType(targetType)) {
            // ex: (Date)null
        } else if (char_TYPE == targetType && isPrimitiveType(expressionType) && isNumberType(expressionType)) {
            // char c = (char) ...
        } else if (sourceIsNull && isPrimitiveType(targetType) && !boolean_TYPE.equals(targetType)) {
            return false;
        } else if ((expressionType.getModifiers() & Opcodes.ACC_FINAL) == 0 && targetType.isInterface()) {
            return true;
        } else if ((targetType.getModifiers() & Opcodes.ACC_FINAL) == 0 && expressionType.isInterface()) {
            return true;
        } else if (!isAssignableTo(targetType, expressionType) && !implementsInterfaceOrIsSubclassOf(expressionType, targetType)) {
            return false;
        }
        return true;
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        // create a new temporary element in the if-then-else type info
        typeCheckingContext.pushTemporaryTypeInfo();
        expression.getBooleanExpression().visit(this);
        Expression trueExpression = expression.getTrueExpression();
        Expression falseExpression = expression.getFalseExpression();
        trueExpression.visit(this);
        ClassNode typeOfTrue = findCurrentInstanceOfClass(trueExpression, getType(trueExpression));
        // pop if-then-else temporary type info
        typeCheckingContext.popTemporaryTypeInfo();
        falseExpression.visit(this);
        ClassNode typeOfFalse = getType(falseExpression);
        ClassNode resultType;
        // handle instanceof cases
        if (hasInferredReturnType(falseExpression)) {
            typeOfFalse = falseExpression.getNodeMetaData(INFERRED_RETURN_TYPE);
        }
        if (hasInferredReturnType(trueExpression)) {
            typeOfTrue = trueExpression.getNodeMetaData(INFERRED_RETURN_TYPE);
        }
        // TODO consider moving next two statements "up a level", i.e. have just one more widely invoked
        // check but determine no -ve consequences first
        typeOfFalse = checkForTargetType(falseExpression, typeOfFalse);
        typeOfTrue = checkForTargetType(trueExpression, typeOfTrue);
        if (isNullConstant(trueExpression) || isNullConstant(falseExpression)) {
            BinaryExpression enclosingBinaryExpression = typeCheckingContext.getEnclosingBinaryExpression();
            if (enclosingBinaryExpression != null && enclosingBinaryExpression.getRightExpression() == expression) {
                resultType = getType(enclosingBinaryExpression.getLeftExpression());
            } else if (isNullConstant(trueExpression) && isNullConstant(falseExpression)) {
                resultType = OBJECT_TYPE;
            } else if (isNullConstant(trueExpression)) {
                resultType = wrapTypeIfNecessary(typeOfFalse);
            } else {
                resultType = wrapTypeIfNecessary(typeOfTrue);
            }
        } else {
            // store type information
            resultType = lowestUpperBound(typeOfTrue, typeOfFalse);
        }
        storeType(expression, resultType);
        popAssignmentTracking(oldTracker);
    }

    // currently just for empty literals, not for e.g. Collections.emptyList() at present
    /// it seems attractive to want to do this for more cases but perhaps not all cases
    private ClassNode checkForTargetType(final Expression expr, final ClassNode type) {
        BinaryExpression enclosingBinaryExpression = typeCheckingContext.getEnclosingBinaryExpression();
        if (enclosingBinaryExpression instanceof DeclarationExpression
                && isEmptyCollection(expr) && isAssignment(enclosingBinaryExpression.getOperation().getType())) {
            VariableExpression target = (VariableExpression) enclosingBinaryExpression.getLeftExpression();
            return adjustForTargetType(target.getType(), type);
        }
        if (currentField != null) {
            return adjustForTargetType(currentField.getType(), type);
        }
        if (currentProperty != null) {
            return adjustForTargetType(currentProperty.getType(), type);
        }
        MethodNode enclosingMethod = typeCheckingContext.getEnclosingMethod();
        if (enclosingMethod != null) {
            return adjustForTargetType(enclosingMethod.getReturnType(), type);
        }
        return type;
    }

    private static ClassNode adjustForTargetType(final ClassNode targetType, final ClassNode resultType) {
        if (targetType.isUsingGenerics() && missesGenericsTypes(resultType)) {
            // unchecked assignment within ternary/elvis
            // examples:
            // List<A> list = existingAs ?: []
            // in that case, the inferred type of the RHS is the type of the RHS
            // "completed" with generics type information available in the LHS
            return GenericsUtils.parameterizeType(targetType, resultType.getPlainNodeReference());
        }
        return resultType;
    }

    private static boolean isEmptyCollection(final Expression expr) {
        return (expr instanceof ListExpression && ((ListExpression) expr).getExpressions().isEmpty())
                || (expr instanceof MapExpression && ((MapExpression) expr).getMapEntryExpressions().isEmpty());
    }

    private static boolean hasInferredReturnType(final Expression expression) {
        ClassNode type = expression.getNodeMetaData(INFERRED_RETURN_TYPE);
        return type != null && !type.getName().equals(ClassHelper.OBJECT);
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        List<CatchStatement> catchStatements = statement.getCatchStatements();
        for (CatchStatement catchStatement : catchStatements) {
            ClassNode exceptionType = catchStatement.getExceptionType();
            typeCheckingContext.controlStructureVariables.put(catchStatement.getVariable(), exceptionType);
        }
        try {
            super.visitTryCatchFinally(statement);
        } finally {
            for (CatchStatement catchStatement : catchStatements) {
                typeCheckingContext.controlStructureVariables.remove(catchStatement.getVariable());
            }
        }
    }

    protected void storeType(final Expression exp, ClassNode cn) {
        if (cn != null && isPrimitiveType(cn)) {
            if (exp instanceof VariableExpression && ((VariableExpression) exp).isClosureSharedVariable()) {
                cn = getWrapper(cn);
            } else if (exp instanceof MethodCallExpression && ((MethodCallExpression) exp).isSafe()) {
                cn = getWrapper(cn);
            } else if (exp instanceof PropertyExpression && ((PropertyExpression) exp).isSafe()) {
                cn = getWrapper(cn);
            }
        }
        if (cn == UNKNOWN_PARAMETER_TYPE) {
            // this can happen for example when "null" is used in an assignment or a method parameter.
            // In that case, instead of storing the virtual type, we must "reset" type information
            // by determining the declaration type of the expression
            storeType(exp, getOriginalDeclarationType(exp));
            return;
        }
        ClassNode oldValue = (ClassNode) exp.putNodeMetaData(INFERRED_TYPE, cn);
        if (oldValue != null) {
            // this may happen when a variable declaration type is wider than the subsequent assignment values
            // for example :
            // def o = 1 // first, an int
            // o = 'String' // then a string
            // o = new Object() // and eventually an object !
            // in that case, the INFERRED_TYPE corresponds to the current inferred type, while
            // DECLARATION_INFERRED_TYPE is the type which should be used for the initial type declaration
            ClassNode oldDIT = exp.getNodeMetaData(DECLARATION_INFERRED_TYPE);
            if (oldDIT != null) {
                exp.putNodeMetaData(DECLARATION_INFERRED_TYPE, cn == null ? oldDIT : lowestUpperBound(oldDIT, cn));
            } else {
                exp.putNodeMetaData(DECLARATION_INFERRED_TYPE, cn == null ? null : lowestUpperBound(oldValue, cn));
            }
        }
        if (exp instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) exp;
            Variable accessedVariable = var.getAccessedVariable();
            if (accessedVariable != exp && accessedVariable instanceof VariableExpression) {
                storeType((VariableExpression) accessedVariable, cn);
            }
            if (accessedVariable instanceof Parameter) {
                ((Parameter) accessedVariable).putNodeMetaData(INFERRED_TYPE, cn);
            }
            if (var.isClosureSharedVariable() && cn != null) {
                List<ClassNode> assignedTypes = typeCheckingContext.closureSharedVariablesAssignmentTypes.computeIfAbsent(var, k -> new LinkedList<ClassNode>());
                assignedTypes.add(cn);
            }
            if (!typeCheckingContext.temporaryIfBranchTypeInformation.isEmpty()) {
                List<ClassNode> temporaryTypesForExpression = getTemporaryTypesForExpression(exp);
                if (temporaryTypesForExpression != null && !temporaryTypesForExpression.isEmpty()) {
                    // a type inference has been made on a variable whose type was defined in an instanceof block
                    // we erase available information with the new type
                    temporaryTypesForExpression.clear();
                }
            }
        }
    }

    protected ClassNode getResultType(ClassNode left, final int op, final ClassNode right, final BinaryExpression expr) {
        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();

        Expression leftExpression = expr.getLeftExpression();
        Expression rightExpression = expr.getRightExpression();

        if (op == EQUAL || op == ELVIS_EQUAL) {
            if (rightRedirect.isDerivedFrom(CLOSURE_TYPE)) {
                ClosureExpression closureExpression = null;
                if (rightExpression instanceof ClosureExpression) {
                    closureExpression = (ClosureExpression) rightExpression;
                } else if (rightExpression instanceof MethodReferenceExpression) {
                    closureExpression = rightExpression.getNodeMetaData(CONSTRUCTED_LAMBDA_EXPRESSION);
                }
                if (closureExpression != null) {
                    MethodNode abstractMethod = findSAM(left);
                    if (abstractMethod != null) {
                        return inferSAMTypeGenericsInAssignment(left, abstractMethod, right, closureExpression);
                    }
                }
            }

            if (leftExpression instanceof VariableExpression) {
                ClassNode initialType = getOriginalDeclarationType(leftExpression).redirect();

                if (isPrimitiveType(right) && initialType.isDerivedFrom(Number_TYPE)) {
                    return getWrapper(right);
                }

                if (isPrimitiveType(initialType) && rightRedirect.isDerivedFrom(Number_TYPE)) {
                    return getUnwrapper(right);
                }

                // as anything can be assigned to a String, Class or [Bb]oolean, return the left type instead
                if (STRING_TYPE.equals(initialType)
                        || CLASS_Type.equals(initialType)
                        || Boolean_TYPE.equals(initialType)
                        || boolean_TYPE.equals(initialType)) {
                    return initialType;
                }
            }

            if (isOrImplements(rightRedirect, Collection_TYPE)) {
                if (leftRedirect.isArray()) {
                    return leftRedirect;
                }
                if (isOrImplements(leftRedirect, Collection_TYPE) &&
                        rightExpression instanceof ListExpression && isEmptyCollection(rightExpression)) {
                    return left;
                }
            }

            return right;
        }
        if (isBoolIntrinsicOp(op)) {
            return boolean_TYPE;
        }
        if (isArrayOp(op)) {
            // using getPNR() to ignore generics at this point
            // and a different binary expression not to pollute the AST
            BinaryExpression newExpr = binX(leftExpression, expr.getOperation(), rightExpression);
            newExpr.setSourcePosition(expr);
            MethodNode method = findMethodOrFail(newExpr, left.getPlainNodeReference(), "getAt", right.getPlainNodeReference());
            if (method != null && implementsInterfaceOrIsSubclassOf(right, RANGE_TYPE)) {
                return inferReturnTypeGenerics(left, method, rightExpression);
            }
            return method != null ? inferComponentType(left, right) : null;
        }
        if (op == FIND_REGEX) {
            // this case always succeeds the result is a Matcher
            return Matcher_TYPE;
        }
        // the left operand is determining the result of the operation
        // for primitives and their wrapper we use a fixed table here
        String operationName = getOperationName(op);
        ClassNode mathResultType = getMathResultType(op, leftRedirect, rightRedirect, operationName);
        if (mathResultType != null) {
            return mathResultType;
        }

        // GROOVY-5890
        // do not mix Class<Foo> with Foo
        if (leftExpression instanceof ClassExpression) {
            left = CLASS_Type.getPlainNodeReference();
        }

        MethodNode method = findMethodOrFail(expr, left, operationName, right);
        if (method != null) {
            storeTargetMethod(expr, method);
            typeCheckMethodsWithGenericsOrFail(left, new ClassNode[]{right}, method, expr);
            if (isAssignment(op)) return left;
            if (isCompareToBoolean(op)) return boolean_TYPE;
            if (op == COMPARE_TO) return int_TYPE;
            return inferReturnTypeGenerics(left, method, args(rightExpression));
        }
        //TODO: other cases
        return null;
    }

    private ClassNode getMathResultType(final int op, final ClassNode leftRedirect, final ClassNode rightRedirect, final String operationName) {
        if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) {
            if (isOperationInGroup(op)) {
                if (isIntCategory(leftRedirect) && isIntCategory(rightRedirect)) return int_TYPE;
                if (isLongCategory(leftRedirect) && isLongCategory(rightRedirect)) return long_TYPE;
                if (isFloat(leftRedirect) && isFloat(rightRedirect)) return float_TYPE;
                if (isDouble(leftRedirect) && isDouble(rightRedirect)) return double_TYPE;
            } else if (isPowerOperator(op)) {
                return Number_TYPE;
            } else if (isBitOperator(op) || op == INTDIV || op == INTDIV_EQUAL) {
                if (isIntCategory(getUnwrapper(leftRedirect)) && isIntCategory(getUnwrapper(rightRedirect)))
                    return int_TYPE;
                if (isLongCategory(getUnwrapper(leftRedirect)) && isLongCategory(getUnwrapper(rightRedirect)))
                    return long_TYPE;
                if (isBigIntCategory(getUnwrapper(leftRedirect)) && isBigIntCategory(getUnwrapper(rightRedirect)))
                    return BigInteger_TYPE;
            } else if (isCompareToBoolean(op) || op == COMPARE_EQUAL || op == COMPARE_NOT_EQUAL) {
                return boolean_TYPE;
            }
        } else if (char_TYPE.equals(leftRedirect) && char_TYPE.equals(rightRedirect)) {
            if (isCompareToBoolean(op) || op == COMPARE_EQUAL || op == COMPARE_NOT_EQUAL) {
                return boolean_TYPE;
            }
        }

        // try to find a method for the operation
        if (isShiftOperation(operationName) && isNumberCategory(leftRedirect) && (isIntCategory(rightRedirect) || isLongCategory(rightRedirect))) {
            return leftRedirect;
        }

        // Divisions may produce different results depending on operand types
        if (isNumberCategory(getWrapper(rightRedirect)) && (isNumberCategory(getWrapper(leftRedirect)) && (DIVIDE == op || DIVIDE_EQUAL == op))) {
            if (isFloatingCategory(leftRedirect) || isFloatingCategory(rightRedirect)) {
                if (!isPrimitiveType(leftRedirect) || !isPrimitiveType(rightRedirect)) {
                    return Double_TYPE;
                }
                return double_TYPE;
            }
            if (DIVIDE == op) {
                return BigDecimal_TYPE;
            }
            return leftRedirect;
        } else if (isOperationInGroup(op)) {
            if (isNumberCategory(getWrapper(leftRedirect)) && isNumberCategory(getWrapper(rightRedirect))) {
                return getGroupOperationResultType(leftRedirect, rightRedirect);
            }
        }
        if (isNumberCategory(getWrapper(rightRedirect)) && isNumberCategory(getWrapper(leftRedirect)) && (MOD == op || MOD_EQUAL == op)) {
            return leftRedirect;
        }
        return null;
    }

    private ClassNode inferSAMTypeGenericsInAssignment(final ClassNode samType, final MethodNode abstractMethod, final ClassNode closureType, final ClosureExpression closureExpression) {
        // if the sam type or closure type do not provide generics information,
        // we cannot infer anything, thus we simply return the provided samUsage
        GenericsType[] samTypeGenerics = samType.getGenericsTypes();
        GenericsType[] closureGenerics = closureType.getGenericsTypes();
        if (samTypeGenerics == null || closureGenerics == null) return samType;

        // extract the generics from the return type
        Map<GenericsTypeName, GenericsType> connections = new HashMap<>();
        extractGenericsConnections(connections, getInferredReturnType(closureExpression), abstractMethod.getReturnType());

        // next we get the block parameter types and set the generics
        // information just like before
        // TODO: add vargs handling
        if (closureExpression.isParameterSpecified()) {
            Parameter[] closureParams = closureExpression.getParameters();
            Parameter[] methodParams = abstractMethod.getParameters();
            for (int i = 0, n = closureParams.length; i < n; i += 1) {
                ClassNode closureParamType = closureParams[i].getType();
                ClassNode methodParamType = methodParams[i].getType();
                extractGenericsConnections(connections, closureParamType, methodParamType);
            }
        }
        return applyGenericsContext(connections, samType.redirect());
    }

    protected static ClassNode getGroupOperationResultType(final ClassNode a, final ClassNode b) {
        if (isBigIntCategory(a) && isBigIntCategory(b)) return BigInteger_TYPE;
        if (isBigDecCategory(a) && isBigDecCategory(b)) return BigDecimal_TYPE;
        if (BigDecimal_TYPE.equals(a) || BigDecimal_TYPE.equals(b)) return BigDecimal_TYPE;
        if (BigInteger_TYPE.equals(a) || BigInteger_TYPE.equals(b)) {
            if (isBigIntCategory(a) && isBigIntCategory(b)) return BigInteger_TYPE;
            return BigDecimal_TYPE;
        }
        if (double_TYPE.equals(a) || double_TYPE.equals(b)) return double_TYPE;
        if (Double_TYPE.equals(a) || Double_TYPE.equals(b)) return Double_TYPE;
        if (float_TYPE.equals(a) || float_TYPE.equals(b)) return float_TYPE;
        if (Float_TYPE.equals(a) || Float_TYPE.equals(b)) return Float_TYPE;
        if (long_TYPE.equals(a) || long_TYPE.equals(b)) return long_TYPE;
        if (Long_TYPE.equals(a) || Long_TYPE.equals(b)) return Long_TYPE;
        if (int_TYPE.equals(a) || int_TYPE.equals(b)) return int_TYPE;
        if (Integer_TYPE.equals(a) || Integer_TYPE.equals(b)) return Integer_TYPE;
        if (short_TYPE.equals(a) || short_TYPE.equals(b)) return short_TYPE;
        if (Short_TYPE.equals(a) || Short_TYPE.equals(b)) return Short_TYPE;
        if (byte_TYPE.equals(a) || byte_TYPE.equals(b)) return byte_TYPE;
        if (Byte_TYPE.equals(a) || Byte_TYPE.equals(b)) return Byte_TYPE;
        if (char_TYPE.equals(a) || char_TYPE.equals(b)) return char_TYPE;
        if (Character_TYPE.equals(a) || Character_TYPE.equals(b)) return Character_TYPE;
        return Number_TYPE;
    }

    protected ClassNode inferComponentType(final ClassNode containerType, final ClassNode indexType) {
        ClassNode componentType = containerType.getComponentType();
        if (componentType == null) {
            // GROOVY-5521
            // try to identify a getAt method
            typeCheckingContext.pushErrorCollector();
            MethodCallExpression vcall = callX(localVarX("_hash_", containerType), "getAt", varX("_index_", indexType));
            vcall.setImplicitThis(false); // GROOVY-8943
            try {
                visitMethodCallExpression(vcall);
            } finally {
                typeCheckingContext.popErrorCollector();
            }
            return getType(vcall);
        } else {
            return componentType;
        }
    }

    protected MethodNode findMethodOrFail(final Expression expr, final ClassNode receiver, final String name, final ClassNode... args) {
        List<MethodNode> methods = findMethod(receiver, name, args);
        if (methods.isEmpty() && (expr instanceof BinaryExpression)) {
            BinaryExpression be = (BinaryExpression) expr;
            MethodCallExpression call = callX(be.getLeftExpression(), name, be.getRightExpression());
            methods = extension.handleMissingMethod(receiver, name, args(be.getLeftExpression()), args, call);
        }
        if (methods.isEmpty()) {
            addNoMatchingMethodError(receiver, name, args, expr);
        } else {
            if (areCategoryMethodCalls(methods, name, args)) {
                addCategoryMethodCallError(expr);
            }
            methods = disambiguateMethods(methods, receiver, args, expr);
            if (methods.size() == 1) {
                return methods.get(0);
            } else {
                addAmbiguousErrorMessage(methods, name, args, expr);
            }
        }
        return null;
    }

    private List<MethodNode> disambiguateMethods(List<MethodNode> methods, final ClassNode receiver, final ClassNode[] argTypes, final Expression call) {
        if (methods.size() > 1 && receiver != null && argTypes != null) {
            List<MethodNode> filteredWithGenerics = new LinkedList<>();
            for (MethodNode methodNode : methods) {
                if (typeCheckMethodsWithGenerics(receiver, argTypes, methodNode)) {
                    if ((methodNode.getModifiers() & Opcodes.ACC_BRIDGE) == 0) {
                        filteredWithGenerics.add(methodNode);
                    }
                }
            }
            if (filteredWithGenerics.size() == 1) {
                return filteredWithGenerics;
            }
            methods = extension.handleAmbiguousMethods(methods, call);
        }

        if (methods.size() > 1) {
            if (call instanceof MethodCall) {
                List<MethodNode> methodNodeList = new LinkedList<>();

                String methodName = ((MethodCall) call).getMethodAsString();

                for (MethodNode methodNode : methods) {
                    if (!methodNode.getName().equals(methodName)) {
                        continue;
                    }
                    methodNodeList.add(methodNode);
                }

                methods = methodNodeList;
            }
        }

        return methods;
    }

    protected static String prettyPrintMethodList(final List<MethodNode> nodes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0, n = nodes.size(); i < n; i += 1) {
            MethodNode node = nodes.get(i);
            sb.append(node.getReturnType().toString(false));
            sb.append(" ");
            sb.append(node.getDeclaringClass().toString(false));
            sb.append("#");
            sb.append(toMethodParametersString(node.getName(), extractTypesFromParameters(node.getParameters())));
            if (i < n - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    protected boolean areCategoryMethodCalls(final List<MethodNode> foundMethods, final String name, final ClassNode[] args) {
        boolean category = false;
        if ("use".equals(name) && args != null && args.length == 2 && args[1].equals(CLOSURE_TYPE)) {
            category = true;
            for (MethodNode method : foundMethods) {
                if (!(method instanceof ExtensionMethodNode) || !((ExtensionMethodNode) method).getExtensionMethodNode().getDeclaringClass().equals(DGM_CLASSNODE)) {
                    category = false;
                    break;
                }
            }
        }
        return category;
    }

    /**
     * This method returns the list of methods named against the supplied parameter that
     * are defined on the specified receiver, but it will also add "non existing" methods
     * that will be generated afterwards by the compiler, for example if a method is using
     * default values and that the specified class node isn't compiled yet.
     *
     * @param receiver the receiver where to find methods
     * @param name     the name of the methods to return
     * @return the methods that are defined on the receiver completed with stubs for future methods
     */
    protected List<MethodNode> findMethodsWithGenerated(final ClassNode receiver, final String name) {
        List<MethodNode> methods = receiver.getMethods(name);
        if (methods.isEmpty() || receiver.isResolved()) return methods;
        return addGeneratedMethods(receiver, methods);
    }

    private static List<MethodNode> addGeneratedMethods(final ClassNode receiver, final List<MethodNode> methods) {
        // using a comparator of parameters
        List<MethodNode> result = new LinkedList<>();
        for (MethodNode method : methods) {
            result.add(method);
            Parameter[] parameters = method.getParameters();
            int counter = 0;
            int size = parameters.length;
            for (int i = size - 1; i >= 0; i--) {
                Parameter parameter = parameters[i];
                if (parameter != null && parameter.hasInitialExpression()) {
                    counter++;
                }
            }

            for (int j = 1; j <= counter; j++) {
                Parameter[] newParams = new Parameter[parameters.length - j];
                int index = 0;
                int k = 1;
                for (Parameter parameter : parameters) {
                    if (k > counter - j && parameter != null && parameter.hasInitialExpression()) {
                        k++;
                    } else if (parameter != null && parameter.hasInitialExpression()) {
                        newParams[index++] = parameter;
                        k++;
                    } else {
                        newParams[index++] = parameter;
                    }
                }
                MethodNode stubbed;
                if ("<init>".equals(method.getName())) {
                    stubbed = new ConstructorNode(
                            method.getModifiers(),
                            newParams,
                            method.getExceptions(),
                            GENERATED_EMPTY_STATEMENT
                    );

                } else {
                    stubbed = new MethodNode(
                            method.getName(),
                            method.getModifiers(),
                            method.getReturnType(),
                            newParams,
                            method.getExceptions(),
                            GENERATED_EMPTY_STATEMENT
                    );
                    stubbed.setGenericsTypes(method.getGenericsTypes());
                }
                stubbed.setDeclaringClass(method.getDeclaringClass());
                result.add(stubbed);
            }
        }
        return result;
    }

    protected List<MethodNode> findMethod(ClassNode receiver, final String name, final ClassNode... args) {
        if (isPrimitiveType(receiver)) receiver = getWrapper(receiver);
        List<MethodNode> methods;
        if (!receiver.isInterface() && "<init>".equals(name)) {
            methods = addGeneratedMethods(receiver, new ArrayList<>(receiver.getDeclaredConstructors()));
            if (methods.isEmpty()) {
                MethodNode node = new ConstructorNode(Opcodes.ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                node.setDeclaringClass(receiver);
                methods = Collections.singletonList(node);
                if (receiver.isArray()) {
                    // No need to check the arguments against an array constructor: it just needs to exist. The array is
                    // created through coercion or by specifying its dimension(s), anyway, and would not match an
                    // arbitrary number of parameters.
                    return methods;
                }
            }
        } else {
            methods = findMethodsWithGenerated(receiver, name);
            if (receiver.isInterface()) {
                collectAllInterfaceMethodsByName(receiver, name, methods);
                methods.addAll(OBJECT_TYPE.getMethods(name));

                if ("call".equals(name) && isFunctionalInterface(receiver)) {
                    MethodNode sam = findSAM(receiver);
                    MethodNode callMethodNode = new MethodNode("call", sam.getModifiers(), sam.getReturnType(), sam.getParameters(), sam.getExceptions(), sam.getCode());
                    callMethodNode.setDeclaringClass(sam.getDeclaringClass());
                    callMethodNode.setSourcePosition(sam);

                    methods.addAll(Collections.singletonList(callMethodNode));
                }
            }
            // TODO: investigate the trait exclusion a bit further, needed otherwise
            // CallMethodOfTraitInsideClosureAndClosureParamTypeInference fails saying
            // not static method can't be called from a static context
            if (typeCheckingContext.getEnclosingClosure() == null || (receiver.getOuterClass() != null && !receiver.getName().endsWith("$Trait$Helper"))) {
                // not in a closure or within an inner class
                ClassNode parent = receiver;
                while (parent.getOuterClass() != null && !parent.isStaticClass()) {
                    parent = parent.getOuterClass();
                    methods.addAll(findMethodsWithGenerated(parent, name));
                }
            }
            if (methods.isEmpty()) {
                addArrayMethods(methods, receiver, name, args);
            }
            if (methods.isEmpty() && (args == null || args.length == 0)) {
                // check if it's a property
                String pname = extractPropertyNameFromMethodName("get", name);
                if (pname == null) {
                    pname = extractPropertyNameFromMethodName("is", name);
                }
                if (pname != null) {
                    // we don't use property exists there because findMethod is called on super clases recursively
                    PropertyNode property = null;
                    ClassNode curNode = receiver;
                    while (property == null && curNode != null) {
                        property = curNode.getProperty(pname);
                        ClassNode svCur = curNode;
                        while (property == null && svCur.getOuterClass() != null && !svCur.isStaticClass()) {
                            svCur = svCur.getOuterClass();
                            property = svCur.getProperty(pname);
                            if (property != null) {
                                receiver = svCur;
                                break;
                            }
                        }
                        curNode = curNode.getSuperClass();
                    }
                    if (property != null) {
                        int mods = Opcodes.ACC_PUBLIC | (property.isStatic() ? Opcodes.ACC_STATIC : 0);
                        MethodNode node = new MethodNode(name, mods, property.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                        node.setDeclaringClass(property.getDeclaringClass());
                        return Collections.singletonList(node);
                    }
                }
            } else if (methods.isEmpty() && args != null && args.length == 1) {
                // maybe we are looking for a setter ?
                String pname = extractPropertyNameFromMethodName("set", name);
                if (pname != null) {
                    ClassNode curNode = receiver;
                    PropertyNode property = null;
                    while (property == null && curNode != null) {
                        property = curNode.getProperty(pname);
                        curNode = curNode.getSuperClass();
                    }
                    if (property != null) {
                        ClassNode type = property.getOriginType();
                        if (implementsInterfaceOrIsSubclassOf(wrapTypeIfNecessary(args[0]), wrapTypeIfNecessary(type))) {
                            int mods = Opcodes.ACC_PUBLIC | (property.isStatic() ? Opcodes.ACC_STATIC : 0);
                            MethodNode node = new MethodNode(name, mods, VOID_TYPE, new Parameter[]{new Parameter(type, name)}, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                            node.setDeclaringClass(property.getDeclaringClass());
                            return Collections.singletonList(node);
                        }
                    }
                }
            }
        }

        if (methods.isEmpty()) {
            // look at the interfaces, there's a chance that a method is not implemented and we should not hide the
            // error from the compiler
            collectAllInterfaceMethodsByName(receiver, name, methods);
        }

        if (!"<init>".equals(name) && !"<clinit>".equals(name)) {
            // lookup in DGM methods too
            findDGMMethodsByNameAndArguments(getSourceUnit().getClassLoader(), receiver, name, args, methods);
        }
        methods = filterMethodsByVisibility(methods, typeCheckingContext.getEnclosingClassNode());
        List<MethodNode> chosen = chooseBestMethod(receiver, methods, args);
        if (!chosen.isEmpty()) return chosen;

        // GROOVY-5566
        if (receiver instanceof InnerClassNode && ((InnerClassNode) receiver).isAnonymous() && methods.size() == 1 && args != null && "<init>".equals(name)) {
            MethodNode constructor = methods.get(0);
            if (constructor.getParameters().length == args.length) {
                return methods;
            }
        }

        if (receiver.equals(CLASS_Type) && receiver.getGenericsTypes() != null) {
            List<MethodNode> result = findMethod(receiver.getGenericsTypes()[0].getType(), name, args);
            if (!result.isEmpty()) return result;
        }

        if (GSTRING_TYPE.equals(receiver)) return findMethod(STRING_TYPE, name, args);

        if (isBeingCompiled(receiver)) {
            chosen = findMethod(GROOVY_OBJECT_TYPE, name, args);
            if (!chosen.isEmpty()) return chosen;
        }

        return EMPTY_METHODNODE_LIST;
    }

    /**
     * Given a method name and a prefix, returns the name of the property that should be looked up,
     * following the java beans rules. For example, "getName" would return "name", while
     * "getFullName" would return "fullName".
     * If the prefix is not found, returns null.
     *
     * @param prefix     the method name prefix ("get", "is", "set", ...)
     * @param methodName the method name
     * @return a property name if the prefix is found and the method matches the java beans rules, null otherwise
     */
    public static String extractPropertyNameFromMethodName(final String prefix, final String methodName) {
        if (prefix == null || methodName == null) return null;
        if (methodName.startsWith(prefix) && prefix.length() < methodName.length()) {
            String result = methodName.substring(prefix.length());
            String propertyName = decapitalize(result);
            if (result.equals(capitalize(propertyName))) return propertyName;
        }
        return null;
    }

    protected void collectAllInterfaceMethodsByName(final ClassNode receiver, final String name, final List<MethodNode> methods) {
        ClassNode cNode = receiver;
        while (cNode != null) {
            ClassNode[] interfaces = cNode.getInterfaces();
            if (interfaces != null && interfaces.length > 0) {
                for (ClassNode node : interfaces) {
                    List<MethodNode> intfMethods = node.getMethods(name);
                    methods.addAll(intfMethods);
                    collectAllInterfaceMethodsByName(node, name, methods);
                }
            }
            cNode = cNode.getSuperClass();
        }
    }

    protected ClassNode getType(final ASTNode exp) {
        ClassNode cn = exp.getNodeMetaData(INFERRED_TYPE);
        if (cn != null) {
            return cn;
        }
        if (exp instanceof ClassExpression) {
            ClassNode node = CLASS_Type.getPlainNodeReference();
            node.setGenericsTypes(new GenericsType[]{
                    new GenericsType(((ClassExpression) exp).getType())
            });
            return node;
        }
        if (exp instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) exp;
            ClassNode selfTrait = isTraitSelf(vexp);
            if (selfTrait != null) return makeSelf(selfTrait);
            if (vexp.isThisExpression()) return makeThis();
            if (vexp.isSuperExpression()) return makeSuper();
            Variable variable = vexp.getAccessedVariable();
            if (variable instanceof FieldNode) {
                ClassNode fieldType = variable.getOriginType();
                if (isUsingGenericsOrIsArrayUsingGenerics(fieldType)) {
                    boolean isStatic = (variable.getModifiers() & Opcodes.ACC_STATIC) != 0;
                    ClassNode thisType = typeCheckingContext.getEnclosingClassNode(), declType = ((FieldNode) variable).getDeclaringClass();
                    Map<GenericsTypeName, GenericsType> placeholders = resolvePlaceHoldersFromDeclaration(thisType, declType, null, isStatic);

                    fieldType = resolveGenericsWithContext(placeholders, fieldType);
                }
                return fieldType;
            }
            if (variable != vexp && variable instanceof VariableExpression) {
                return getType((Expression) variable);
            }
            if (variable instanceof Parameter) {
                Parameter parameter = (Parameter) variable;
                ClassNode type = null;
                // check if param part of control structure - but not if inside instanceof
                List<ClassNode> temporaryTypesForExpression = getTemporaryTypesForExpression(vexp);
                if (temporaryTypesForExpression == null || temporaryTypesForExpression.isEmpty()) {
                    type = typeCheckingContext.controlStructureVariables.get(parameter);
                }
                // now check for closure override
                TypeCheckingContext.EnclosingClosure enclosingClosure = typeCheckingContext.getEnclosingClosure();
                if (type == null && enclosingClosure != null && temporaryTypesForExpression == null) {
                    type = getTypeFromClosureArguments(parameter, enclosingClosure);
                }
                if (type != null) {
                    storeType(vexp, type);
                    return type;
                }
                return getType((Parameter) variable);
            }
            return vexp.getOriginType();
        }

        if (exp instanceof ListExpression) {
            return inferListExpressionType((ListExpression) exp);
        }
        if (exp instanceof MapExpression) {
            return inferMapExpressionType((MapExpression) exp);
        }
        if (exp instanceof ConstructorCallExpression) {
            return ((ConstructorCallExpression) exp).getType();
        }
        if (exp instanceof MethodNode) {
            if ((exp == GET_DELEGATE || exp == GET_OWNER || exp == GET_THISOBJECT) && typeCheckingContext.getEnclosingClosure() != null) {
                return typeCheckingContext.getEnclosingClassNode();
            }
            ClassNode ret = getInferredReturnType(exp);
            return ret != null ? ret : ((MethodNode) exp).getReturnType();
        }
        if (exp instanceof FieldNode || exp instanceof PropertyNode) {
            return ((Variable) exp).getOriginType();
        }
        if (exp instanceof RangeExpression) {
            ClassNode plain = RANGE_TYPE.getPlainNodeReference();
            RangeExpression re = (RangeExpression) exp;
            ClassNode fromType = getType(re.getFrom());
            ClassNode toType = getType(re.getTo());
            if (fromType.equals(toType)) {
                plain.setGenericsTypes(new GenericsType[]{
                        new GenericsType(wrapTypeIfNecessary(fromType))
                });
            } else {
                plain.setGenericsTypes(new GenericsType[]{
                        new GenericsType(wrapTypeIfNecessary(lowestUpperBound(fromType, toType)))
                });
            }
            return plain;
        }
        if (exp instanceof UnaryPlusExpression) {
            return getType(((UnaryPlusExpression) exp).getExpression());
        }
        if (exp instanceof UnaryMinusExpression) {
            return getType(((UnaryMinusExpression) exp).getExpression());
        }
        if (exp instanceof BitwiseNegationExpression) {
            return getType(((BitwiseNegationExpression) exp).getExpression());
        }
        if (exp instanceof Parameter) {
            return ((Parameter) exp).getOriginType();
        }
        if (exp instanceof ClosureExpression) {
            ClassNode irt = getInferredReturnType(exp);
            if (irt != null) {
                irt = wrapTypeIfNecessary(irt);
                ClassNode result = CLOSURE_TYPE.getPlainNodeReference();
                result.setGenericsTypes(new GenericsType[]{new GenericsType(irt)});
                return result;
            }
        } else if (exp instanceof MethodCall) {
            MethodNode target = exp.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (target != null) {
                return getType(target);
            }
        }
        return ((Expression) exp).getType();
    }

    private ClassNode getTypeFromClosureArguments(final Parameter parameter, final TypeCheckingContext.EnclosingClosure enclosingClosure) {
        ClosureExpression closureExpression = enclosingClosure.getClosureExpression();
        ClassNode[] closureParamTypes = closureExpression.getNodeMetaData(CLOSURE_ARGUMENTS);
        if (closureParamTypes == null) return null;
        Parameter[] parameters = closureExpression.getParameters();
        String name = parameter.getName();

        if (parameters != null) {
            if (parameters.length == 0) {
                return "it".equals(name) && closureParamTypes.length != 0 ? closureParamTypes[0] : null;
            }

            for (int index = 0; index < parameters.length; index++) {
                if (name.equals(parameters[index].getName())) {
                    return closureParamTypes.length > index ? closureParamTypes[index] : null;
                }
            }
        }

        return null;
    }

    private static ClassNode makeSelf(final ClassNode trait) {
        ClassNode selfType = trait;
        Set<ClassNode> selfTypes = Traits.collectSelfTypes(selfType, new LinkedHashSet<>());
        if (!selfTypes.isEmpty()) {
            selfTypes.add(selfType);
            selfType = new UnionTypeClassNode(selfTypes.toArray(ClassNode.EMPTY_ARRAY));
        }
        return selfType;
    }

    private ClassNode makeSuper() {
        return makeType(typeCheckingContext.getEnclosingClassNode().getSuperClass(), typeCheckingContext.isInStaticContext);
    }

    private ClassNode makeThis() {
        return makeType(typeCheckingContext.getEnclosingClassNode(), typeCheckingContext.isInStaticContext);
    }

    /**
     * Wrap type in Class&lt;&gt; if usingClass==true.
     */
    private static ClassNode makeType(final ClassNode cn, final boolean usingClass) {
        if (usingClass) {
            ClassNode clazzType = CLASS_Type.getPlainNodeReference();
            clazzType.setGenericsTypes(new GenericsType[]{new GenericsType(cn)});
            return clazzType;
        } else {
            return cn;
        }
    }

    /**
     * Stores the inferred return type of a closure or a method. We are using a separate key to store
     * inferred return type because the inferred type of a closure is {@link Closure}, which is different
     * from the inferred type of the code of the closure.
     *
     * @param node a {@link ClosureExpression} or a {@link MethodNode}
     * @param type the inferred return type of the code
     * @return the old value of the inferred type
     */
    protected ClassNode storeInferredReturnType(final ASTNode node, final ClassNode type) {
        if (!(node instanceof ClosureExpression)) {
            throw new IllegalArgumentException("Storing inferred return type is only allowed on closures but found " + node.getClass());
        }
        return (ClassNode) node.putNodeMetaData(INFERRED_RETURN_TYPE, type);
    }

    /**
     * Returns the inferred return type of a closure or a method, if stored on the AST node. This method
     * doesn't perform any type inference by itself.
     *
     * @param exp a {@link ClosureExpression} or {@link MethodNode}
     * @return the inferred type, as stored on node metadata.
     */
    protected ClassNode getInferredReturnType(final ASTNode exp) {
        return (ClassNode) exp.getNodeMetaData(INFERRED_RETURN_TYPE);
    }

    protected ClassNode inferListExpressionType(final ListExpression list) {
        List<Expression> expressions = list.getExpressions();
        int nExpressions = expressions.size();
        if (nExpressions == 0) {
            return list.getType();
        }
        ClassNode listType = list.getType();
        GenericsType[] genericsTypes = listType.getGenericsTypes();
        if ((genericsTypes == null
                || genericsTypes.length == 0
                || (genericsTypes.length == 1 && OBJECT_TYPE.equals(genericsTypes[0].getType())))) {
            // maybe we can infer the component type
            List<ClassNode> nodes = new ArrayList<>(nExpressions);
            for (Expression expression : expressions) {
                if (isNullConstant(expression)) {
                    // a null element is found in the list, skip it because we'll use the other elements from the list
                } else {
                    nodes.add(getType(expression));
                }
            }
            if (!nodes.isEmpty()) {
                ClassNode itemType = lowestUpperBound(nodes);

                listType = listType.getPlainNodeReference();
                listType.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(itemType))});
            }
        }
        return listType;
    }

    protected static boolean isNullConstant(final Expression expression) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).isNullExpression();
    }

    protected static boolean isThisExpression(final Expression expression) {
        return expression instanceof VariableExpression && ((VariableExpression) expression).isThisExpression();
    }

    protected static boolean isSuperExpression(final Expression expression) {
        return expression instanceof VariableExpression && ((VariableExpression) expression).isSuperExpression();
    }

    protected ClassNode inferMapExpressionType(final MapExpression map) {
        ClassNode mapType = LINKEDHASHMAP_CLASSNODE.getPlainNodeReference();
        List<MapEntryExpression> entryExpressions = map.getMapEntryExpressions();
        int nExpressions = entryExpressions.size();
        if (nExpressions == 0) return mapType;

        GenericsType[] genericsTypes = mapType.getGenericsTypes();
        if (genericsTypes == null
                || genericsTypes.length < 2
                || (genericsTypes.length == 2 && OBJECT_TYPE.equals(genericsTypes[0].getType()) && OBJECT_TYPE.equals(genericsTypes[1].getType()))) {
            List<ClassNode> keyTypes = new ArrayList<>(nExpressions);
            List<ClassNode> valueTypes = new ArrayList<>(nExpressions);
            for (MapEntryExpression entryExpression : entryExpressions) {
                keyTypes.add(getType(entryExpression.getKeyExpression()));
                valueTypes.add(getType(entryExpression.getValueExpression()));
            }
            ClassNode keyType = lowestUpperBound(keyTypes);
            ClassNode valueType = lowestUpperBound(valueTypes);
            if (!OBJECT_TYPE.equals(keyType) || !OBJECT_TYPE.equals(valueType)) {
                mapType = mapType.getPlainNodeReference();
                mapType.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(keyType)), new GenericsType(wrapTypeIfNecessary(valueType))});
            }
        }
        return mapType;
    }

    private static class ExtensionMethodDeclaringClass {
    }

    /**
     * If a method call returns a parameterized type, then we can perform additional inference on the
     * return type, so that the type gets actual type parameters. For example, the method
     * Arrays.asList(T...) is generified with type T which can be deduced from actual type
     * arguments.
     *
     * @param method    the method node
     * @param arguments the method call arguments
     * @return parameterized, infered, class node
     */
    protected ClassNode inferReturnTypeGenerics(final ClassNode receiver, final MethodNode method, final Expression arguments) {
        return inferReturnTypeGenerics(receiver, method, arguments, null);
    }

    /**
     * If a method call returns a parameterized type, then we can perform additional inference on the
     * return type, so that the type gets actual type parameters. For example, the method
     * Arrays.asList(T...) is generified with type T which can be deduced from actual type
     * arguments.
     *
     * @param method            the method node
     * @param arguments         the method call arguments
     * @param explicitTypeHints explicit type hints as found for example in Collections.&lt;String&gt;emptyList()
     * @return parameterized, infered, class node
     */
    protected ClassNode inferReturnTypeGenerics(final ClassNode receiver, final MethodNode method, final Expression arguments, final GenericsType[] explicitTypeHints) {
        ClassNode returnType = method.getReturnType();
        if (getGenericsWithoutArray(returnType) == null) {
            return returnType;
        }
        if (method instanceof ExtensionMethodNode) {
            // check if the placeholder corresponds to the placeholder of the first parameter
            ExtensionMethodNode emn = (ExtensionMethodNode) method;
            MethodNode dgm = emn.getExtensionMethodNode();
            ArgumentListExpression args = new ArgumentListExpression();
            VariableExpression vexp = varX("$self", receiver);
            args.addExpression(vexp);
            vexp.setNodeMetaData(ExtensionMethodDeclaringClass.class, emn.getDeclaringClass());
            if (arguments instanceof ArgumentListExpression) {
                for (Expression argument : (ArgumentListExpression) arguments) {
                    args.addExpression(argument);
                }
            } else {
                args.addExpression(arguments);
            }
            return inferReturnTypeGenerics(receiver, dgm, args);
        }
        Map<GenericsTypeName, GenericsType> resolvedPlaceholders = resolvePlaceHoldersFromDeclaration(receiver, getDeclaringClass(method, arguments), method, method.isStatic());
        resolvePlaceholdersFromExplicitTypeHints(method, explicitTypeHints, resolvedPlaceholders);
        if (resolvedPlaceholders.isEmpty()) {
            return boundUnboundedWildcards(returnType);
        }
        Map<GenericsTypeName, GenericsType> placeholdersFromContext = extractGenericsParameterMapOfThis(typeCheckingContext);
        applyGenericsConnections(placeholdersFromContext, resolvedPlaceholders);

        // then resolve receivers from method arguments
        List<Expression> expressions = InvocationWriter.makeArgumentList(arguments).getExpressions();
        Parameter[] parameters = method.getParameters();
        boolean isVargs = isVargs(parameters);
        int paramLength = parameters.length;
        if (expressions.size() >= paramLength) {
            for (int i = 0; i < paramLength; i += 1) {
                boolean lastArg = (i == paramLength - 1);
                ClassNode paramType = parameters[i].getType();
                ClassNode argumentType = getType(expressions.get(i));
                while (paramType.isArray() && argumentType.isArray()) {
                    paramType = paramType.getComponentType();
                    argumentType = argumentType.getComponentType();
                }
                if (isUsingGenericsOrIsArrayUsingGenerics(paramType)) {
                    if (implementsInterfaceOrIsSubclassOf(argumentType, CLOSURE_TYPE) && isSAMType(paramType)) {
                        // implicit closure coercion in action!
                        Map<GenericsTypeName, GenericsType> pholders = applyGenericsContextToParameterClass(resolvedPlaceholders, paramType);
                        argumentType = convertClosureTypeToSAMType(expressions.get(i), argumentType, paramType, pholders);
                    }
                    if (isVargs && lastArg && argumentType.isArray()) {
                        argumentType = argumentType.getComponentType();
                    }
                    if (isVargs && lastArg && paramType.isArray()) {
                        paramType = paramType.getComponentType();
                    }
                    argumentType = wrapTypeIfNecessary(argumentType);

                    Map<GenericsTypeName, GenericsType> connections = new HashMap<>();
                    extractGenericsConnections(connections, argumentType, paramType);
                    extractGenericsConnectionsForSuperClassAndInterfaces(resolvedPlaceholders, connections);
                    applyGenericsConnections(connections, resolvedPlaceholders);
                }
            }
        }

        return applyGenericsContext(resolvedPlaceholders, returnType);
    }

    private static void resolvePlaceholdersFromExplicitTypeHints(final MethodNode method, final GenericsType[] explicitTypeHints, final Map<GenericsTypeName, GenericsType> resolvedPlaceholders) {
        if (explicitTypeHints != null) {
            GenericsType[] methodGenericTypes = method.getGenericsTypes();
            if (methodGenericTypes != null && methodGenericTypes.length == explicitTypeHints.length) {
                for (int i = 0, n = methodGenericTypes.length; i < n; i += 1) {
                    GenericsType methodGenericType = methodGenericTypes[i];
                    GenericsType explicitTypeHint = explicitTypeHints[i];
                    resolvedPlaceholders.put(new GenericsTypeName(methodGenericType.getName()), explicitTypeHint);
                }
            }
        }
    }

    /**
     * Given method call like "m(Collections.emptyList())", the type of the call
     * argument is {@code List<T>} without explicit type arguments. Knowning the
     * method target of "m", {@code T} could be resolved.
     */
    private static void resolvePlaceholdersFromImplicitTypeHints(final ClassNode[] actuals, final ArgumentListExpression argumentList, final MethodNode inferredMethod) {
        for (int i = 0, n = actuals.length; i < n; i += 1) {
            // check for method call with known target
            Expression a = argumentList.getExpression(i);
            if (!(a instanceof MethodCallExpression)) continue;
            if (((MethodCallExpression) a).isUsingGenerics()) continue;
            MethodNode aNode = a.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (aNode == null || aNode.getGenericsTypes() == null) continue;

            // and unknown generics
            ClassNode at = actuals[i];
            if (!GenericsUtils.hasUnresolvedGenerics(at)) continue;

            int np = inferredMethod.getParameters().length;
            Parameter p = inferredMethod.getParameters()[Math.min(i, np - 1)];

            ClassNode pt = p.getOriginType();
            if (i >= (np - 1) && pt.isArray() && !at.isArray()) pt = pt.getComponentType();

            // try to resolve placeholder(s) in argument type using parameter type

            Map<GenericsTypeName, GenericsType> linked = new HashMap<>();
            Map<GenericsTypeName, GenericsType> source = GenericsUtils.extractPlaceholders(at);
            Map<GenericsTypeName, GenericsType> target = GenericsUtils.extractPlaceholders(pt);

            // connect E:T from source to E:Type from target
            for (GenericsType placeholder : aNode.getGenericsTypes()) {
                for (Map.Entry<GenericsTypeName, GenericsType> e : source.entrySet()) {
                    if (e.getValue() == placeholder) {
                        Optional.ofNullable(target.get(e.getKey()))
                            // skip "f(g())" for "f(T<String>)" and "<U extends Number> U g()"
                            .filter(gt -> isAssignableTo(gt.getType(), placeholder.getType()))
                            .ifPresent(gt -> linked.put(new GenericsTypeName(placeholder.getName()), gt));
                        break;
                    }
                }
            }

            actuals[i] = applyGenericsContext(linked, at);
        }
    }

    private static void extractGenericsConnectionsForSuperClassAndInterfaces(final Map<GenericsTypeName, GenericsType> resolvedPlaceholders, final Map<GenericsTypeName, GenericsType> connections) {
        for (GenericsType value : new HashSet<GenericsType>(connections.values())) {
            if (!value.isPlaceholder() && !value.isWildcard()) {
                ClassNode valueType = value.getType();
                List<ClassNode> deepNodes = new LinkedList<>();
                ClassNode unresolvedSuperClass = valueType.getUnresolvedSuperClass();
                if (unresolvedSuperClass != null && unresolvedSuperClass.isUsingGenerics()) {
                    deepNodes.add(unresolvedSuperClass);
                }
                for (ClassNode node : valueType.getUnresolvedInterfaces()) {
                    if (node.isUsingGenerics()) {
                        deepNodes.add(node);
                    }
                }
                if (!deepNodes.isEmpty()) {
                    for (GenericsType genericsType : resolvedPlaceholders.values()) {
                        ClassNode lowerBound = genericsType.getLowerBound();
                        if (lowerBound != null) {
                            for (ClassNode deepNode : deepNodes) {
                                if (lowerBound.equals(deepNode)) {
                                    extractGenericsConnections(connections, deepNode, lowerBound);
                                }
                            }
                        }
                        ClassNode[] upperBounds = genericsType.getUpperBounds();
                        if (upperBounds != null) {
                            for (ClassNode upperBound : upperBounds) {
                                for (ClassNode deepNode : deepNodes) {
                                    if (upperBound.equals(deepNode)) {
                                        extractGenericsConnections(connections, deepNode, upperBound);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method will convert a closure type to the appropriate SAM type, which will be used
     * to infer return type generics.
     *
     * @param closureType the inferred type of a closure (Closure&lt;ClosureReturnType&gt;)
     * @param samType     the type into which the closure is coerced into
     * @return same SAM type, but completed with information from the closure node
     */
    private static ClassNode convertClosureTypeToSAMType(final Expression expression, final ClassNode closureType, final ClassNode samType, final Map<GenericsTypeName, GenericsType> placeholders) {
        if (!samType.isUsingGenerics()) return samType;

        // use the generics information from the Closure to further specify the type
        MethodNode sam = findSAM(samType);
        if (closureType.isUsingGenerics() && sam != null) {
            //correct SAM type for generics
            //sam = applyGenericsContext(placeholders, sam);

            // the return type of the SAM method exactly corresponds to the inferred return type
            ClassNode samReturnType = sam.getReturnType();
            ClassNode closureReturnType = expression.getNodeMetaData(INFERRED_TYPE);
            if (closureReturnType != null && closureReturnType.isUsingGenerics()) {
                ClassNode unwrapped = closureReturnType.getGenericsTypes()[0].getType();
                extractGenericsConnections(placeholders, unwrapped, samReturnType);
            } else if (samReturnType.isGenericsPlaceHolder()) {
                placeholders.put(new GenericsTypeName(samReturnType.getGenericsTypes()[0].getName()), closureType.getGenericsTypes()[0]);
            }

            // now repeat the same for each parameter given in the ClosureExpression
            if (expression instanceof ClosureExpression && sam.getParameters().length > 0) {
                List<ClassNode[]> genericsToConnect = new LinkedList<>();
                Parameter[] closureParams = ((ClosureExpression) expression).getParameters();
                ClassNode[] closureParamTypes = extractTypesFromParameters(closureParams);
                if (expression.getNodeMetaData(CLOSURE_ARGUMENTS) != null) {
                    closureParamTypes = expression.getNodeMetaData(CLOSURE_ARGUMENTS);
                }
                Parameter[] parameters = sam.getParameters();
                for (int i = 0, n = parameters.length; i < n; i += 1) {
                    Parameter parameter = parameters[i];
                    if (parameter.getOriginType().isUsingGenerics() && closureParamTypes.length > i) {
                        genericsToConnect.add(new ClassNode[]{closureParamTypes[i], parameter.getOriginType()});
                    }
                }
                for (ClassNode[] classNodes : genericsToConnect) {
                    ClassNode found = classNodes[0];
                    ClassNode expected = classNodes[1];
                    if (!isAssignableTo(found, expected)) {
                        // probably facing a type mismatch
                        continue;
                    }
                    ClassNode generifiedType = GenericsUtils.parameterizeType(found, expected);
                    while (expected.isArray()) {
                        expected = expected.getComponentType();
                        generifiedType = generifiedType.getComponentType();
                    }
                    if (expected.isGenericsPlaceHolder()) {
                        placeholders.put(new GenericsTypeName(expected.getGenericsTypes()[0].getName()), new GenericsType(generifiedType));
                    } else {
                        GenericsType[] expectedGenericsTypes = expected.getGenericsTypes();
                        GenericsType[] foundGenericsTypes = generifiedType.getGenericsTypes();

                        for (int i = 0, n = expectedGenericsTypes.length; i < n; i += 1) {
                            GenericsType type = expectedGenericsTypes[i];
                            if (type.isPlaceholder()) {
                                String name = type.getName();
                                placeholders.put(new GenericsTypeName(name), foundGenericsTypes[i]);
                            }
                        }
                    }
                }
            }
        }
        ClassNode result = applyGenericsContext(placeholders, samType.redirect());
        return result;
    }

    private ClassNode resolveGenericsWithContext(final Map<GenericsTypeName, GenericsType> resolvedPlaceholders, final ClassNode currentType) {
        Map<GenericsTypeName, GenericsType> placeholdersFromContext = extractGenericsParameterMapOfThis(typeCheckingContext);
        return resolveClassNodeGenerics(resolvedPlaceholders, placeholdersFromContext, currentType);
    }

    private static ClassNode getDeclaringClass(final MethodNode method, final Expression arguments) {
        ClassNode declaringClass = method.getDeclaringClass();

        // correcting declaring class for extension methods:
        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression al = (ArgumentListExpression) arguments;
            List<Expression> list = al.getExpressions();
            if (list.isEmpty()) return declaringClass;
            Expression exp = list.get(0);
            ClassNode cn = exp.getNodeMetaData(ExtensionMethodDeclaringClass.class);
            if (cn != null) return cn;
        }
        return declaringClass;
    }

    private Map<GenericsTypeName, GenericsType> resolvePlaceHoldersFromDeclaration(final ClassNode receiver, final ClassNode declaration, final MethodNode method, final boolean isStaticTarget) {
        Map<GenericsTypeName, GenericsType> resolvedPlaceholders;
        if (isStaticTarget && CLASS_Type.equals(receiver) &&
                receiver.isUsingGenerics() &&
                receiver.getGenericsTypes().length > 0 &&
                !OBJECT_TYPE.equals(receiver.getGenericsTypes()[0].getType())) {
            return resolvePlaceHoldersFromDeclaration(receiver.getGenericsTypes()[0].getType(), declaration, method, isStaticTarget);
        } else {
            resolvedPlaceholders = extractPlaceHolders(method, receiver, declaration);
        }
        return resolvedPlaceholders;
    }

    private static boolean isGenericsPlaceHolderOrArrayOf(final ClassNode cn) {
        if (cn.isArray()) return isGenericsPlaceHolderOrArrayOf(cn.getComponentType());
        return cn.isGenericsPlaceHolder();
    }

    private static Map<GenericsTypeName, GenericsType> extractPlaceHolders(final MethodNode method, ClassNode receiver, final ClassNode declaringClass) {
        if (declaringClass.equals(OBJECT_TYPE)) {
            Map<GenericsTypeName, GenericsType> resolvedPlaceholders = new HashMap<>();
            if (method != null) addMethodLevelDeclaredGenerics(method, resolvedPlaceholders);
            return resolvedPlaceholders;
        }

        Map<GenericsTypeName, GenericsType> resolvedPlaceholders = null;
        if (isPrimitiveType(receiver) && !isPrimitiveType(declaringClass)) {
            receiver = getWrapper(receiver);
        }
        ClassNode[] todo;
        if (receiver instanceof UnionTypeClassNode) {
            todo = ((UnionTypeClassNode) receiver).getDelegates();
        } else {
            todo = new ClassNode[] {receiver};
        }
        for (ClassNode type : todo) {
            ClassNode current = type;
            while (current != null) {
                boolean continueLoop = true;
                // extract the place holders
                Map<GenericsTypeName, GenericsType> currentPlaceHolders = new HashMap<>();
                if (isGenericsPlaceHolderOrArrayOf(declaringClass) || declaringClass.equals(current)) {
                    extractGenericsConnections(currentPlaceHolders, current, declaringClass);
                    if (method != null) addMethodLevelDeclaredGenerics(method, currentPlaceHolders);
                    continueLoop = false;
                } else {
                    GenericsUtils.extractPlaceholders(current, currentPlaceHolders);
                }

                if (resolvedPlaceholders != null) {
                    // merge maps
                    Set<Map.Entry<GenericsTypeName, GenericsType>> entries = currentPlaceHolders.entrySet();
                    for (Map.Entry<GenericsTypeName, GenericsType> entry : entries) {
                        GenericsType gt = entry.getValue();
                        if (!gt.isPlaceholder()) continue;
                        GenericsType referenced = resolvedPlaceholders.get(new GenericsTypeName(gt.getName()));
                        if (referenced == null) continue;
                        entry.setValue(referenced);
                    }
                }
                resolvedPlaceholders = currentPlaceHolders;

                // we are done if we are now in the declaring class
                if (!continueLoop) break;

                current = getNextSuperClass(current, declaringClass);
                if (current == null && declaringClass.equals(CLASS_Type)) {
                    // this can happen if the receiver is Class<Foo>, then
                    // the actual receiver is Foo and declaringClass is Class
                    current = declaringClass;
                }
            }
        }
        if (resolvedPlaceholders == null) {
            String descriptor = "<>";
            if (method != null) descriptor = method.getTypeDescriptor();
            throw new GroovyBugError(
                    "Declaring class for method call to '" +
                            descriptor + "' declared in " + declaringClass.getName() +
                            " was not matched with found receiver " + receiver.getName() + "." +
                            " This should not have happened!");
        }
        return resolvedPlaceholders;
    }

    protected boolean typeCheckMethodsWithGenericsOrFail(final ClassNode receiver, final ClassNode[] arguments, final MethodNode candidateMethod, final Expression location) {
        if (!typeCheckMethodsWithGenerics(receiver, arguments, candidateMethod)) {
            Map<GenericsTypeName, GenericsType> classGTs = GenericsUtils.extractPlaceholders(receiver);
            Parameter[] parameters = candidateMethod.getParameters();
            ClassNode[] paramTypes = new ClassNode[parameters.length];
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                paramTypes[i] = fullyResolveType(parameters[i].getType(), classGTs);
            }
            addStaticTypeError("Cannot call " + toMethodGenericTypesString(candidateMethod) + receiver.toString(false) + "#" +
                    toMethodParametersString(candidateMethod.getName(), paramTypes) + " with arguments " + formatArgumentList(arguments), location);
            return false;
        }
        return true;
    }

    private static String toMethodGenericTypesString(final MethodNode node) {
        GenericsType[] genericsTypes = node.getGenericsTypes();
        if (genericsTypes == null) return "";
        return toGenericTypesString(genericsTypes);
    }

    protected static String formatArgumentList(final ClassNode[] nodes) {
        if (nodes == null || nodes.length == 0) return "[]";
        StringBuilder sb = new StringBuilder(24 * nodes.length);
        sb.append('[');
        for (ClassNode node : nodes) {
            sb.append(prettyPrintType(node));
            sb.append(", ");
        }
        if (sb.length() > 1) {
            sb.setCharAt(sb.length() - 2, ']');
        }
        return sb.toString();
    }

    private static void putSetterInfo(final Expression exp, final SetterInfo info) {
        exp.putNodeMetaData(SetterInfo.class, info);
    }

    private static SetterInfo removeSetterInfo(final Expression exp) {
        Object nodeMetaData = exp.getNodeMetaData(SetterInfo.class);
        if (nodeMetaData != null) {
            exp.removeNodeMetaData(SetterInfo.class);
            return (SetterInfo) nodeMetaData;
        }
        return null;
    }

    @Override
    public void addError(final String msg, final ASTNode expr) {
        Long err = ((long) expr.getLineNumber()) << 16 + expr.getColumnNumber();
        if ((DEBUG_GENERATED_CODE && expr.getLineNumber() < 0) || !typeCheckingContext.reportedErrors.contains(err)) {
            typeCheckingContext.getErrorCollector().addErrorAndContinue(msg + '\n', expr, getSourceUnit());
            typeCheckingContext.reportedErrors.add(err);
        }
    }

    protected void addStaticTypeError(final String msg, final ASTNode expr) {
        if (expr.getColumnNumber() > 0 && expr.getLineNumber() > 0) {
            addError(StaticTypesTransformation.STATIC_ERROR_PREFIX + msg, expr);
        } else {
            if (DEBUG_GENERATED_CODE) {
                addError(StaticTypesTransformation.STATIC_ERROR_PREFIX + "Error in generated code [" + expr.getText() + "] - " + msg, expr);
            }
            // ignore errors which are related to unknown source locations
            // because they are likely related to generated code
        }
    }

    protected void addNoMatchingMethodError(ClassNode receiver, final String name, final ClassNode[] args, final Expression call) {
        if (isClassClassNodeWrappingConcreteType(receiver)) {
            receiver = receiver.getGenericsTypes()[0].getType();
        }
        addStaticTypeError("Cannot find matching method " + receiver.getText() + "#" + toMethodParametersString(name, args) + ". Please check if the declared type is correct and if the method exists.", call);
    }

    protected void addAmbiguousErrorMessage(final List<MethodNode> foundMethods, final String name, final ClassNode[] args, final Expression expr) {
        addStaticTypeError("Reference to method is ambiguous. Cannot choose between " + prettyPrintMethodList(foundMethods), expr);
    }

    protected void addCategoryMethodCallError(final Expression call) {
        addStaticTypeError("Due to their dynamic nature, usage of categories is not possible with static type checking active", call);
    }

    protected void addAssignmentError(final ClassNode leftType, final ClassNode rightType, final Expression assignmentExpression) {
        addStaticTypeError("Cannot assign value of type " + rightType.toString(false) + " to variable of type " + leftType.toString(false), assignmentExpression);
    }

    protected void addUnsupportedPreOrPostfixExpressionError(final Expression expression) {
        if (expression instanceof PostfixExpression) {
            addStaticTypeError("Unsupported postfix operation type [" + ((PostfixExpression) expression).getOperation() + "]", expression);
        } else if (expression instanceof PrefixExpression) {
            addStaticTypeError("Unsupported prefix operation type [" + ((PrefixExpression) expression).getOperation() + "]", expression);
        } else {
            throw new IllegalArgumentException("Method should be called with a PostfixExpression or a PrefixExpression");
        }
    }

    public void setMethodsToBeVisited(final Set<MethodNode> methodsToBeVisited) {
        this.typeCheckingContext.methodsToBeVisited = methodsToBeVisited;
    }

    public void performSecondPass() {
        for (SecondPassExpression wrapper : typeCheckingContext.secondPassExpressions) {
            Expression expression = wrapper.getExpression();
            if (expression instanceof BinaryExpression) {
                Expression left = ((BinaryExpression) expression).getLeftExpression();
                if (left instanceof VariableExpression) {
                    Variable target = findTargetVariable((VariableExpression) left);
                    if (target instanceof VariableExpression) {
                        VariableExpression var = (VariableExpression) target;
                        List<ClassNode> classNodes = typeCheckingContext.closureSharedVariablesAssignmentTypes.get(var);
                        if (classNodes != null && classNodes.size() > 1) {
                            ClassNode lub = lowestUpperBound(classNodes);
                            String message = getOperationName(((BinaryExpression) expression).getOperation().getType());
                            if (message != null) {
                                List<MethodNode> method = findMethod(lub, message, getType(((BinaryExpression) expression).getRightExpression()));
                                if (method.isEmpty()) {
                                    addStaticTypeError("A closure shared variable [" + target.getName() + "] has been assigned with various types and the method" +
                                            " [" + toMethodParametersString(message, getType(((BinaryExpression) expression).getRightExpression())) + "]" +
                                            " does not exist in the lowest upper bound of those types: [" +
                                            lub.toString(false) + "]. In general, this is a bad practice (variable reuse) because the compiler cannot" +
                                            " determine safely what is the type of the variable at the moment of the call in a multithreaded context.", expression);
                                }
                            }
                        }
                    }
                }
            } else if (expression instanceof MethodCallExpression) {
                MethodCallExpression call = (MethodCallExpression) expression;
                Expression objectExpression = call.getObjectExpression();
                if (objectExpression instanceof VariableExpression) {
                    // this should always be the case, but adding a test is safer
                    Variable target = findTargetVariable((VariableExpression) objectExpression);
                    if (target instanceof VariableExpression) {
                        VariableExpression var = (VariableExpression) target;
                        List<ClassNode> classNodes = typeCheckingContext.closureSharedVariablesAssignmentTypes.get(var);
                        if (classNodes != null && classNodes.size() > 1) {
                            ClassNode lub = lowestUpperBound(classNodes);
                            MethodNode methodNode = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
                            // we must check that such a method exists on the LUB
                            Parameter[] parameters = methodNode.getParameters();
                            ClassNode[] params = extractTypesFromParameters(parameters);
                            ClassNode[] argTypes = (ClassNode[]) wrapper.getData();
                            List<MethodNode> method = findMethod(lub, methodNode.getName(), argTypes);
                            if (method.size() != 1) {
                                addStaticTypeError("A closure shared variable [" + target.getName() + "] has been assigned with various types and the method" +
                                        " [" + toMethodParametersString(methodNode.getName(), params) + "]" +
                                        " does not exist in the lowest upper bound of those types: [" +
                                        lub.toString(false) + "]. In general, this is a bad practice (variable reuse) because the compiler cannot" +
                                        " determine safely what is the type of the variable at the moment of the call in a multithreaded context.", call);
                            }
                        }
                    }
                }
            }
        }
        // give a chance to type checker extensions to throw errors based on information gathered afterwards
        extension.finish();
    }

    protected static ClassNode[] extractTypesFromParameters(final Parameter[] parameters) {
        return Arrays.stream(parameters).map(Parameter::getType).toArray(ClassNode[]::new);
    }

    /**
     * Returns a wrapped type if, and only if, the provided class node is a primitive type.
     * This method differs from {@link ClassHelper#getWrapper(org.codehaus.groovy.ast.ClassNode)} as it will
     * return the same instance if the provided type is not a generic type.
     *
     * @return the wrapped type
     */
    protected static ClassNode wrapTypeIfNecessary(final ClassNode type) {
        if (isPrimitiveType(type)) return getWrapper(type);
        return type;
    }

    protected static boolean isClassInnerClassOrEqualTo(final ClassNode toBeChecked, final ClassNode start) {
        if (start == toBeChecked) return true;
        ClassNode outer = start.getOuterClass();
        if (outer != null) {
            return isClassInnerClassOrEqualTo(toBeChecked, outer);
        }
        return false;
    }

    //--------------------------------------------------------------------------

    public static class SignatureCodecFactory {
        public static SignatureCodec getCodec(final int version, final ClassLoader classLoader) {
            switch (version) {
                case 1:
                    return new SignatureCodecVersion1(classLoader);
                default:
                    return null;
            }
        }
    }

    // class only used to store setter information when an expression of type
    // a.x = foo or x=foo is found and that it corresponds to a setter call
    private static class SetterInfo {
        final ClassNode receiverType;
        final String name;
        final List<MethodNode> setters;

        private SetterInfo(final ClassNode receiverType, final String name, final List<MethodNode> setters) {
            this.receiverType = receiverType;
            this.setters = setters;
            this.name = name;
        }
    }

    /**
     * Wrapper for a Parameter so it can be treated like a VariableExpression
     * and tracked in the ifElseForWhileAssignmentTracker.
     * <p>
     * This class purposely does not adhere to the normal equals and hashCode
     * contract on the Object class and delegates those calls to the wrapped
     * variable.
     */
    private static class ParameterVariableExpression extends VariableExpression {

        private final Parameter parameter;

        ParameterVariableExpression(final Parameter parameter) {
            super(parameter);
            this.parameter = parameter;
            ClassNode inferred = parameter.getNodeMetaData(INFERRED_TYPE);
            if (inferred == null) {
                inferred = infer(parameter);

                parameter.setNodeMetaData(INFERRED_TYPE, inferred);
            }
        }

        private static ClassNode infer(final Variable variable) {
            ClassNode originType = variable.getOriginType();

            if (originType.isGenericsPlaceHolder()) {
                GenericsType[] genericsTypes = originType.getGenericsTypes();

                if (genericsTypes != null && genericsTypes.length > 0) {
                    GenericsType gt = genericsTypes[0];
                    ClassNode[] upperBounds = gt.getUpperBounds();

                    if (upperBounds != null && upperBounds.length > 0) {
                        return upperBounds[0];
                    }
                }
            }

            return originType;
        }

        @Override
        public void copyNodeMetaData(final ASTNode other) {
            parameter.copyNodeMetaData(other);
        }

        @Override
        public Object putNodeMetaData(final Object key, final Object value) {
            return parameter.putNodeMetaData(key, value);
        }

        @Override
        public void removeNodeMetaData(final Object key) {
            parameter.removeNodeMetaData(key);
        }

        @Override
        public Map<?, ?> getNodeMetaData() {
            return parameter.getNodeMetaData();
        }

        @Override
        public <T> T getNodeMetaData(final Object key) {
            return parameter.getNodeMetaData(key);
        }

        @Override
        public void setNodeMetaData(final Object key, final Object value) {
            parameter.setNodeMetaData(key, value);
        }

        @Override
        public int hashCode() {
            return parameter.hashCode();
        }

        @Override
        public boolean equals(final Object other) {
            return parameter.equals(other);
        }
    }

    protected class VariableExpressionTypeMemoizer extends ClassCodeVisitorSupport {
        private final Map<VariableExpression, ClassNode> varOrigType;

        public VariableExpressionTypeMemoizer(final Map<VariableExpression, ClassNode> varOrigType) {
            this.varOrigType = varOrigType;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return StaticTypeCheckingVisitor.this.getSourceUnit();
        }

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            super.visitVariableExpression(expression);
            Variable var = findTargetVariable(expression);
            if (var instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) var;
                varOrigType.put(ve, ve.getNodeMetaData(INFERRED_TYPE));
            }
        }
    }
}
