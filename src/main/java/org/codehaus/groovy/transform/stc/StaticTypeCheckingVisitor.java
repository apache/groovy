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
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.GenericsType.GenericsTypeName;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
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
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.WarningMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenUtil;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.Opcodes;

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
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.groovy.ast.tools.MethodNodeUtils.withDefaultArgumentMethods;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.apache.groovy.util.BeanUtils.decapitalize;
import static org.codehaus.groovy.ast.ClassHelper.AUTOCLOSEABLE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Character_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Iterator_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.METACLASS_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Number_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.PATTERN_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.RANGE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SET_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STREAM_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.dynamicType;
import static org.codehaus.groovy.ast.ClassHelper.findSAM;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getNextSuperClass;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.isBigDecimalType;
import static org.codehaus.groovy.ast.ClassHelper.isBigIntegerType;
import static org.codehaus.groovy.ast.ClassHelper.isClassType;
import static org.codehaus.groovy.ast.ClassHelper.isDynamicTyped;
import static org.codehaus.groovy.ast.ClassHelper.isFunctionalInterface;
import static org.codehaus.groovy.ast.ClassHelper.isGStringType;
import static org.codehaus.groovy.ast.ClassHelper.isNumberType;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveByte;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveChar;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveDouble;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveFloat;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveInt;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveLong;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveShort;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveVoid;
import static org.codehaus.groovy.ast.ClassHelper.isSAMType;
import static org.codehaus.groovy.ast.ClassHelper.isStringType;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperByte;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperCharacter;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperDouble;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperFloat;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperInteger;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperLong;
import static org.codehaus.groovy.ast.ClassHelper.isWrapperShort;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getResolveStrategyName;
import static org.codehaus.groovy.ast.tools.ClosureUtils.hasImplicitParameter;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.binX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.elvisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe0;
import static org.codehaus.groovy.ast.tools.ParameterUtils.isVargs;
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
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.asBoolean;
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.init;
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.last;
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
import static org.codehaus.groovy.syntax.Types.MINUS_MINUS;
import static org.codehaus.groovy.syntax.Types.MOD;
import static org.codehaus.groovy.syntax.Types.MOD_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS_PLUS;
import static org.codehaus.groovy.syntax.Types.REMAINDER;
import static org.codehaus.groovy.syntax.Types.REMAINDER_EQUAL;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.ArrayList_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.Collection_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.LinkedHashMap_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.LinkedHashSet_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.Matcher_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.NUMBER_OPS;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.UNKNOWN_PARAMETER_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.applyGenericsConnections;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.applyGenericsContext;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.boundUnboundedWildcards;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.checkCompatibleAssignmentTypes;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.checkPossibleLossOfPrecision;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.chooseBestMethod;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.evaluateExpression;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.extractGenericsConnections;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.extractGenericsParameterMapOfThis;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.filterMethodsByVisibility;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsForClassNode;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findTargetVariable;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.fullyResolve;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.fullyResolveType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.getCombinedBoundType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.getCombinedGenericsType;
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
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isGStringOrGStringStringLUB;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isOperationInGroup;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isParameterizedWithGStringOrGStringString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isParameterizedWithString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isPowerOperator;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isShiftOperation;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isTraitSelf;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isUsingGenericsOrIsArrayUsingGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isWildcardLeftHandSide;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.missesGenericsTypes;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.prettyPrintType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.prettyPrintTypeName;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.resolveClassNodeGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.toMethodParametersString;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.typeCheckMethodArgumentWithGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.typeCheckMethodsWithGenerics;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.CLOSURE_ARGUMENTS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DECLARATION_INFERRED_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DELEGATION_METADATA;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DYNAMIC_RESOLUTION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.IMPLICIT_RECEIVER;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_RETURN_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PARAMETER_TYPE;
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
    protected static final ClassNode CLOSUREPARAMS_CLASSNODE = ClassHelper.make(ClosureParams.class);
    protected static final ClassNode NAMED_PARAMS_CLASSNODE = ClassHelper.make(NamedParams.class);
    protected static final ClassNode NAMED_PARAM_CLASSNODE = ClassHelper.make(NamedParam.class);
    @Deprecated(forRemoval = true, since = "4.0.0")
    protected static final ClassNode LINKEDHASHMAP_CLASSNODE = LinkedHashMap_TYPE;
    protected static final ClassNode ENUMERATION_TYPE = ClassHelper.make(Enumeration.class);
    protected static final ClassNode MAP_ENTRY_TYPE = ClassHelper.make(Map.Entry.class);
    protected static final ClassNode ITERABLE_TYPE = ClassHelper.ITERABLE_TYPE;

    private static final List<ClassNode> TUPLE_TYPES = Arrays.stream(ClassHelper.TUPLE_CLASSES).map(ClassHelper::makeWithoutCaching).collect(Collectors.toList());

    public static final MethodNode CLOSURE_CALL_NO_ARG  = CLOSURE_TYPE.getDeclaredMethod("call", Parameter.EMPTY_ARRAY);
    public static final MethodNode CLOSURE_CALL_ONE_ARG = CLOSURE_TYPE.getDeclaredMethod("call", new Parameter[]{new Parameter(OBJECT_TYPE, "arg")});
    public static final MethodNode CLOSURE_CALL_VARGS   = CLOSURE_TYPE.getDeclaredMethod("call", new Parameter[]{new Parameter(OBJECT_TYPE.makeArray(), "args")});

    public static final Statement GENERATED_EMPTY_STATEMENT = EmptyStatement.INSTANCE;

    protected final ReturnAdder.ReturnStatementListener returnListener = returnStatement -> {
        if (returnStatement.isReturningNullOrVoid()) return;
        ClassNode returnType = checkReturnType(returnStatement);
        if (this.typeCheckingContext.getEnclosingClosure() != null) {
            addClosureReturnType(returnType);
        } else if (this.typeCheckingContext.getEnclosingMethod() == null) {
            throw new GroovyBugError("Unexpected return statement at " + returnStatement.getLineNumber() + ":" + returnStatement.getColumnNumber() + " " + returnStatement.getText());
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

    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        typeCheckingContext.setCompilationUnit(compilationUnit);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return typeCheckingContext.getSource();
    }

    public void initialize() {
        extension.setup();
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

    private List<TypeCheckingExtension> getTypeCheckingExtensions(final AnnotatedNode classOrMethod) {
        List<Expression> ex = new ArrayList<>();
        for (ClassNode type : getTypeCheckingAnnotations()) {
            for (AnnotationNode anno : classOrMethod.getAnnotations(type)) {
                Expression extensions = anno.getMember("extensions");
                if (extensions instanceof ConstantExpression) {
                    ex.add(extensions);
                } else if (extensions instanceof ListExpression) {
                    ex.addAll(((ListExpression) extensions).getExpressions());
                }
            }
        }
        if (ex.isEmpty()) return Collections.emptyList();
        return ex.stream().filter(e -> e instanceof ConstantExpression).map(pathOrType ->
            new GroovyTypeCheckingExtensionSupport(this, pathOrType.getText(), typeCheckingContext.getCompilationUnit())
        ).collect(Collectors.toList());
    }

    private <Node extends AnnotatedNode> void doWithTypeCheckingExtensions(final Node classOrMethod, final Consumer<Node> visitor) {
        List<TypeCheckingExtension> extensions = getTypeCheckingExtensions(classOrMethod);
        if (extensions.isEmpty()) {
            visitor.accept(classOrMethod);
        } else { // GROOVY-10770: extension composition
            List<TypeCheckingExtension> added = new ArrayList<>();
            for (TypeCheckingExtension ext : extensions) {
                if (extension.addHandler(ext)) {
                    added.add(ext);
                    ext.setup();
                }
            }
            try {
                visitor.accept(classOrMethod);
            } finally {
                for (TypeCheckingExtension ext : added) {
                    extension.removeHandler(ext);
                    ext.finish();
                }
            }
        }
    }

    //--------------------------------------------------------------------------

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

            doWithTypeCheckingExtensions(node, super::visitClass);
            node.getInnerClasses().forEachRemaining(this::visitClass);

            typeCheckingContext.alreadyVisitedMethods = oldSet;
            typeCheckingContext.popEnclosingClassNode();
            if (type != null) {
                typeCheckingContext.popErrorCollector();
            }

            node.putNodeMetaData(INFERRED_TYPE, node);
            node.putNodeMetaData(StaticTypeCheckingVisitor.class, Boolean.TRUE);
            // mark all methods as visited. We can't do this in visitMethod because the type checker
            // works in a two pass sequence and we don't want to skip the second pass
            node.getMethods().forEach(n -> n.putNodeMetaData(StaticTypeCheckingVisitor.class, Boolean.TRUE));
            node.getDeclaredConstructors().forEach(n -> n.putNodeMetaData(StaticTypeCheckingVisitor.class, Boolean.TRUE));
        }
        extension.afterVisitClass(node);
    }

    protected boolean shouldSkipClassNode(final ClassNode node) {
        return Boolean.TRUE.equals(node.getNodeMetaData(StaticTypeCheckingVisitor.class)) || isSkipMode(node);
    }

    protected boolean shouldSkipMethodNode(final MethodNode node) {
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
        if (fn != null && fn.isPrivate() && !fn.isSynthetic()) {
            ClassNode declaringClass = fn.getDeclaringClass();
            ClassNode enclosingClass = typeCheckingContext.getEnclosingClassNode();
            if (declaringClass == enclosingClass && typeCheckingContext.getEnclosingClosure() == null) return;
            if (declaringClass == enclosingClass || getOutermost(declaringClass) == getOutermost(enclosingClass)) {
                StaticTypesMarker accessKind = lhsOfAssignment ? PV_FIELDS_MUTATION : PV_FIELDS_ACCESS;
                addPrivateFieldOrMethodAccess(source, declaringClass, accessKind, fn);
            }
        }
    }

    /**
     * Checks for private method call from inner or outer class.
     */
    private void checkOrMarkPrivateAccess(final Expression source, final MethodNode mn) {
        ClassNode declaringClass = mn.getDeclaringClass();
        ClassNode enclosingClassNode = typeCheckingContext.getEnclosingClassNode();
        if (declaringClass != enclosingClassNode || typeCheckingContext.getEnclosingClosure() != null) {
            int mods = mn.getModifiers();
            boolean sameModule = declaringClass.getModule() == enclosingClassNode.getModule();
            String packageName = declaringClass.getPackageName();
            if (packageName == null) {
                packageName = "";
            }
            if (Modifier.isPrivate(mods) && sameModule) {
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

        if (accessedVariable instanceof DynamicVariable) {
            // a dynamic variable is either a closure property, a class member referenced from a closure, or an undeclared variable

            if (enclosingClosure != null) {
                switch (name) {
                  case "delegate":
                    var dm = getDelegationMetadata(enclosingClosure.getClosureExpression());
                    if (dm != null) {
                        vexp.putNodeMetaData(INFERRED_TYPE, dm.getType());
                        return;
                    }
                    // falls through
                  case "owner":
                    if (typeCheckingContext.getEnclosingClosureStack().size() > 1) {
                        vexp.putNodeMetaData(INFERRED_TYPE, CLOSURE_TYPE.getPlainNodeReference());
                        return;
                    }
                    // falls through
                  case "thisObject":
                    vexp.putNodeMetaData(INFERRED_TYPE, makeThis());
                    return;
                  case "parameterTypes":
                    vexp.putNodeMetaData(INFERRED_TYPE, CLASS_Type.getPlainNodeReference().makeArray());
                    return;
                  case "maximumNumberOfParameters":
                  case "resolveStrategy":
                  case "directive":
                    vexp.putNodeMetaData(INFERRED_TYPE, int_TYPE);
                    return;
                  case "metaClass": // GROOVY-11386
                    vexp.putNodeMetaData(INFERRED_TYPE, METACLASS_TYPE);
                    return;
                }
            }

            if (tryVariableExpressionAsProperty(vexp, name)) return;

            if (!extension.handleUnresolvedVariableExpression(vexp)) {
                addStaticTypeError("The variable [" + name + "] is undeclared.", vexp);
            }
        } else if (accessedVariable instanceof FieldNode) {
            if (tryVariableExpressionAsProperty(vexp, name)) {
                ClassNode temporaryType = getInferredTypeFromTempInfo(vexp, null); // GROOVY-9454
                if (temporaryType != null && !isObjectType(temporaryType)) {
                    vexp.putNodeMetaData(INFERRED_TYPE, temporaryType);
                }
                if (vexp.getAccessedVariable() != accessedVariable) { // GROOVY-11360: field hidden by dynamic property
                    if (vexp.getLineNumber() > 0 && !typeCheckingContext.reportedErrors.contains(((long) vexp.getLineNumber()) << 16 + vexp.getColumnNumber())) {
                        String text = "The field: " + accessedVariable.getName() + " of class: " + prettyPrintTypeName(((FieldNode) accessedVariable).getDeclaringClass()) +
                                                " is hidden by a dynamic property. A qualifier is required to reference it.";
                        Token token = new Token(0, name, vexp.getLineNumber(), vexp.getColumnNumber()); // ASTNode to CSTNode
                        typeCheckingContext.getErrorCollector().addWarning(new WarningMessage(WarningMessage.POSSIBLE_ERRORS, text, token, getSourceUnit()));
                    }
                }
            } else if (!extension.handleUnresolvedVariableExpression(vexp)) { // GROOVY-11356
                addStaticTypeError("No such property: " + name + " for class: " + prettyPrintTypeName(typeCheckingContext.getEnclosingClassNode()), vexp);
            }
        } else if (accessedVariable instanceof PropertyNode) {
            // we must be careful -- the property node may be of a wrong type:
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
            } else if (!extension.handleUnresolvedVariableExpression(vexp)) { // GROOVY-11356
                addStaticTypeError("No such property: " + name + " for class: " + prettyPrintTypeName(typeCheckingContext.getEnclosingClassNode()), vexp);
            }
        } else if (accessedVariable != null) {
            VariableExpression localVariable;
            if (accessedVariable instanceof Parameter) {
                Parameter prm = (Parameter) accessedVariable;
                localVariable = new ParameterVariableExpression(prm);
            } else {
                localVariable = (VariableExpression) accessedVariable;
            }

            ClassNode inferredType = getInferredTypeFromTempInfo(localVariable, localVariable.getNodeMetaData(INFERRED_TYPE));
            if (inferredType != null && !isObjectType(inferredType) && !inferredType.equals(isTraitSelf(vexp))) {
                vexp.putNodeMetaData(INFERRED_TYPE, inferredType);
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
        storeType(vexp, !isObjectType(vexp.getType()) ? vexp.getType() : makeThis());
        return true;
    }

    private boolean tryVariableExpressionAsProperty(final VariableExpression vexp, final String dynName) {
        PropertyExpression pexp = thisPropX(true, dynName);
        if (existsProperty(pexp, !typeCheckingContext.isTargetOfEnclosingAssignment(vexp))) {
            vexp.copyNodeMetaData(pexp.getObjectExpression());
            for (Object key : new Object[]{IMPLICIT_RECEIVER, READONLY_PROPERTY, PV_FIELDS_ACCESS, PV_FIELDS_MUTATION, DIRECT_METHOD_CALL_TARGET}) {
                vexp.putNodeMetaData(key, pexp.getNodeMetaData(key));
            }
            ClassNode type = pexp.getNodeMetaData(INFERRED_TYPE);
            if (vexp.isClosureSharedVariable()) {
                type = wrapTypeIfNecessary(type);
            }
            if (type == null) type = OBJECT_TYPE;
            vexp.putNodeMetaData(INFERRED_TYPE, type); // GROOVY-11007
            String receiver = vexp.getNodeMetaData(IMPLICIT_RECEIVER);
            Boolean dynamic = pexp.getNodeMetaData(DYNAMIC_RESOLUTION);
            // GROOVY-7701, GROOVY-7996: correct false assumption made by VariableScopeVisitor
            if (((receiver != null && !receiver.endsWith("owner")) || Boolean.TRUE.equals(dynamic))
                    && !(vexp.getAccessedVariable() instanceof DynamicVariable)) {
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
            var objectExpression = expression.getObjectExpression();
            var objectExpressionType = (objectExpression instanceof ClassExpression
                    ? objectExpression.getType() : wrapTypeIfNecessary(getType(objectExpression)));
            objectExpressionType = findCurrentInstanceOfClass(objectExpression, objectExpressionType);
            addStaticTypeError("No such property: " + expression.getPropertyAsString() + " for class: " + prettyPrintTypeName(objectExpressionType), expression);
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        if (existsProperty(expression, true)) return;

        if (!extension.handleUnresolvedAttribute(expression)) {
            var objectExpression = expression.getObjectExpression();
            var objectExpressionType = (objectExpression instanceof ClassExpression
                    ? objectExpression.getType() : wrapTypeIfNecessary(getType(objectExpression)));
            objectExpressionType = findCurrentInstanceOfClass(objectExpression, objectExpressionType);
            addStaticTypeError("No such attribute: " + expression.getPropertyAsString() + " for class: " + prettyPrintTypeName(objectExpressionType), expression);
        }
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        super.visitRangeExpression(expression);
        ClassNode fromType = getWrapper(getType(expression.getFrom()));
        ClassNode toType = getWrapper(getType(expression.getTo()));
        if (isWrapperInteger(fromType) && isWrapperInteger(toType)) {
            storeType(expression, ClassHelper.make(IntRange.class));
        } else {
            ClassNode rangeType = RANGE_TYPE.getPlainNodeReference();
            rangeType.setGenericsTypes(new GenericsType[]{new GenericsType(lowestUpperBound(fromType, toType))});
            storeType(expression, rangeType);
        }
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        typeCheckingContext.pushTemporaryTypeInfo();
        super.visitNotExpression(expression);
        // GROOVY-9455: !(x instanceof T) shouldn't propagate T as inferred type
        typeCheckingContext.temporaryIfBranchTypeInformation.pop().forEach(this::putNotInstanceOfTypeInfo);
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
                lType = getType(leftExpression);
            } else {
                if (op != EQUAL && op != ELVIS_EQUAL) {
                    lType = getType(leftExpression);
                } else {
                    lType = getOriginalDeclarationType(leftExpression);

                    applyTargetType(lType, rightExpression);
                }
                rightExpression.visit(this);
            }

            ClassNode rType = isNullConstant(rightExpression) && !isPrimitiveType(lType)
                    ? UNKNOWN_PARAMETER_TYPE // null to primitive type is handled elsewhere
                    : getInferredTypeFromTempInfo(rightExpression, getType(rightExpression));
            ClassNode resultType;

            if (op == KEYWORD_IN || op == COMPARE_NOT_IN) {
                // for the "in" or "!in" operator, the receiver and the arguments are reversed
                BinaryExpression reverseExpression = binX(rightExpression, expression.getOperation(), leftExpression);
                resultType = getResultType(rType, op, lType, reverseExpression);
                if (resultType == null) resultType = boolean_TYPE; // GROOVY-10239
                storeTargetMethod(expression, reverseExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET));
            } else {
                resultType = getResultType(lType, op, rType, expression);
                if (op == ELVIS_EQUAL) {
                    // TODO: Should this transform and visit be done before left and right are visited above?
                    Expression fullExpression = new ElvisOperatorExpression(leftExpression, rightExpression);
                    fullExpression.setSourcePosition(expression);
                    fullExpression.visit(this);

                    resultType = getType(fullExpression);
                }
            }
            if (resultType == null) {
                resultType = lType;
            }

            if (isArrayOp(op)) {
                if (leftExpression instanceof VariableExpression) {//GROOVY-6782
                    if (leftExpression.getNodeMetaData(INFERRED_TYPE) == null) {
                        leftExpression.removeNodeMetaData(INFERRED_RETURN_TYPE);
                        storeType(leftExpression, lType);
                    }
                }
                if (!lType.isArray()
                        && enclosingBinaryExpression != null
                        && enclosingBinaryExpression.getLeftExpression() == expression
                        && isAssignment(enclosingBinaryExpression.getOperation().getType())) {
                    // left hand side of a subscript assignment: map['foo'] = ...
                    Expression enclosingExpressionRHS = enclosingBinaryExpression.getRightExpression();
                    if (!(enclosingExpressionRHS instanceof ClosureExpression)) {
                        enclosingExpressionRHS.visit(this);
                    }
                    ClassNode[] arguments = {rType, getType(enclosingExpressionRHS)};
                    List<MethodNode> methods = findMethod(lType, "putAt", arguments);
                    if (methods.isEmpty()) {
                        addNoMatchingMethodError(lType, "putAt", arguments, enclosingBinaryExpression);
                    } else if (methods.size() == 1) {
                        typeCheckMethodsWithGenericsOrFail(lType, arguments, methods.get(0), enclosingExpressionRHS);
                    }
                }
            }

            boolean isEmptyDeclaration = (expression instanceof DeclarationExpression
                    && (rightExpression instanceof EmptyExpression || rType == UNKNOWN_PARAMETER_TYPE));
            if (!isEmptyDeclaration && isAssignment(op)) {
                if (rightExpression instanceof ConstructorCallExpression)
                    inferDiamondType((ConstructorCallExpression) rightExpression, lType);

                // handle unchecked assignment: List<Type> list = []
                resultType = adjustForTargetType(resultType, lType);

                ClassNode originType = getOriginalDeclarationType(leftExpression);
                typeCheckAssignment(expression, leftExpression, originType, rightExpression, resultType);
                // check for implicit conversion like "String a = 123", "int[] b = [1,2,3]", "List c = [].stream()", etc.
                if (!implementsInterfaceOrIsSubclassOf(wrapTypeIfNecessary(resultType), wrapTypeIfNecessary(originType))) {
                    resultType = originType;
                } else if (isPrimitiveType(originType) && resultType.equals(getWrapper(originType))) {
                    resultType = originType; // retain primitive semantics
                } else {
                    // GROOVY-7549: RHS type may not be accessible to enclosing class
                    int modifiers = resultType.getModifiers();
                    ClassNode enclosingType = typeCheckingContext.getEnclosingClassNode();
                    if (!Modifier.isPublic(modifiers) && !enclosingType.equals(resultType)
                            && !getOutermost(enclosingType).equals(getOutermost(resultType))
                            && (Modifier.isPrivate(modifiers) || !Objects.equals(enclosingType.getPackageName(), resultType.getPackageName()))) {
                        resultType = originType; // TODO: Find accessible type in hierarchy of resultType?
                    } else if (GenericsUtils.hasUnresolvedGenerics(resultType)) { // GROOVY-9033, GROOVY-10089, et al.
                        Map<GenericsTypeName, GenericsType> enclosing = extractGenericsParameterMapOfThis(typeCheckingContext);
                        resultType = fullyResolveType(resultType, Optional.ofNullable(enclosing).orElseGet(Collections::emptyMap));
                    }
                }

                // track conditional assignment
                if (leftExpression instanceof VariableExpression
                        && typeCheckingContext.ifElseForWhileAssignmentTracker != null) {
                    var targetVariable = ((VariableExpression) leftExpression).getAccessedVariable();
                    if (targetVariable instanceof Parameter) {
                        targetVariable = new ParameterVariableExpression((Parameter) targetVariable);
                    }
                    if (targetVariable instanceof VariableExpression) {
                        recordAssignment((VariableExpression) targetVariable, resultType);
                    }
                }

                storeType(leftExpression, resultType);

                // propagate closure parameter information
                if (leftExpression instanceof VariableExpression
                        // GROOVY-11400: save to declared variable; skip field, property, parameter or dynamic variable
                        && ((VariableExpression) leftExpression).getAccessedVariable() instanceof VariableExpression) {
                    var targetVariable = (VariableExpression) ((VariableExpression) leftExpression).getAccessedVariable();
                    if (rightExpression instanceof ClosureExpression) {
                        var closure = (ClosureExpression) rightExpression;
                        if (!hasImplicitParameter(closure)) // GROOVY-11394: arrow means zero parameters
                            targetVariable.putNodeMetaData(CLOSURE_ARGUMENTS, getParametersSafe(closure));
                    } else if (rightExpression instanceof VariableExpression
                            && ((VariableExpression) rightExpression).getAccessedVariable() instanceof VariableExpression) {
                        var sourceVariable = (VariableExpression) ((VariableExpression) rightExpression).getAccessedVariable();
                        targetVariable.putNodeMetaData(CLOSURE_ARGUMENTS, sourceVariable.getNodeMetaData(CLOSURE_ARGUMENTS));
                    }
                }
            } else if (op == KEYWORD_INSTANCEOF) {
                pushInstanceOfTypeInfo(leftExpression, rightExpression);
            } else if (op == COMPARE_NOT_INSTANCEOF) { // GROOVY-6429, GROOVY-8321, GROOVY-8412, GROOVY-8523, GROOVY-9931
                putNotInstanceOfTypeInfo(extractTemporaryTypeInfoKey(leftExpression), Collections.singleton(rightExpression.getType()));
            }
            if (!isEmptyDeclaration) {
                storeType(expression, resultType);
            }

            validateResourceInARM(expression, resultType);

            // GROOVY-5874: if left expression is a closure shared variable, a second pass should be done
            if (leftExpression instanceof VariableExpression && ((VariableExpression) leftExpression).isClosureSharedVariable()) {
                typeCheckingContext.secondPassExpressions.add(new SecondPassExpression<>(expression));
            }
        } finally {
            typeCheckingContext.popEnclosingBinaryExpression();
        }
    }

    private void validateResourceInARM(final BinaryExpression expression, final ClassNode lType) {
        if (expression instanceof DeclarationExpression
                && TryCatchStatement.isResource(expression)
                && !isOrImplements(lType, AUTOCLOSEABLE_TYPE)) {
            addError("Resource[" + lType.getName() + "] in ARM should be of type AutoCloseable", expression);
        }
    }

    private void applyTargetType(final ClassNode target, final Expression source) {
        if (isClosureWithType(target)) {
            if (source instanceof ClosureExpression) {
                GenericsType returnType = target.getGenericsTypes()[0];
                storeInferredReturnType(source, getCombinedBoundType(returnType));
            }
        } else if (isFunctionalInterface(target)) {
            if (source instanceof ClosureExpression) {
                inferParameterAndReturnTypesOfClosureOnRHS(target, (ClosureExpression) source);
            } else if (source instanceof MapExpression) { // GROOVY-7141
                List<MapEntryExpression> spec = ((MapExpression) source).getMapEntryExpressions();
                if (spec.size() == 1 && spec.get(0).getValueExpression() instanceof ClosureExpression
                        && findSAM(target).getName().equals(spec.get(0).getKeyExpression().getText())) {
                    inferParameterAndReturnTypesOfClosureOnRHS(target, (ClosureExpression) spec.get(0).getValueExpression());
                }
            } else if (source instanceof MethodReferenceExpression) {
                LambdaExpression lambda = constructLambdaExpressionForMethodReference(target, (MethodReferenceExpression) source);

                inferParameterAndReturnTypesOfClosureOnRHS(target, lambda);
                source.putNodeMetaData(PARAMETER_TYPE, lambda.getNodeMetaData(PARAMETER_TYPE));
                source.putNodeMetaData(CLOSURE_ARGUMENTS, extractTypesFromParameters(lambda.getParameters()));
            }
        }
    }

    private void inferParameterAndReturnTypesOfClosureOnRHS(final ClassNode lhsType, final ClosureExpression rhsExpression) {
        ClassNode[] samParameterTypes;
        {
            Tuple2<ClassNode[], ClassNode> typeInfo = GenericsUtils.parameterizeSAM(lhsType);
            storeInferredReturnType(rhsExpression, typeInfo.getV2());
            samParameterTypes = typeInfo.getV1();
        }

        Parameter[] closureParameters = getParametersSafe(rhsExpression);
        if (samParameterTypes.length == 1 && hasImplicitParameter(rhsExpression)) {
            Variable it = rhsExpression.getVariableScope().getDeclaredVariable("it"); // GROOVY-7141
            closureParameters = new Parameter[]{it instanceof Parameter ? (Parameter) it : new Parameter(dynamicType(),"it")};
        }

        int n = closureParameters.length;
        ClassNode[] expectedTypes = samParameterTypes;
out:    if ((samParameterTypes.length == 1 && isOrImplements(samParameterTypes[0], LIST_TYPE)) // GROOVY-11092
                && (n != 1 || !typeCheckMethodArgumentWithGenerics(GenericsUtils.nonGeneric(closureParameters[0].getOriginType()), samParameterTypes[0], false))) {
            int itemCount = TUPLE_TYPES.indexOf(samParameterTypes[0]);
            if (itemCount >= 0) {//Tuple[0-16]
                if (itemCount != n) break out;
                GenericsType[] spec = samParameterTypes[0].getGenericsTypes();
                if (spec != null) { // edge case: raw type or Tuple0 falls through
                    expectedTypes = Arrays.stream(spec).map(GenericsType::getType).toArray(ClassNode[]::new);
                    break out;
                }
            }
            expectedTypes = new ClassNode[n];
            Arrays.fill(expectedTypes, inferLoopElementType(samParameterTypes[0]));
        }

        if (n == expectedTypes.length) {
            for (int i = 0; i < n; i += 1) {
                Parameter parameter = closureParameters[i];
                if (parameter.isDynamicTyped()) {
                    parameter.setType(expectedTypes[i]); // GROOVY-11083, GROOVY-11085, et al.
                } else {
                    checkParamType(parameter, expectedTypes[i], i == n-1, rhsExpression instanceof LambdaExpression);
                }
            }
            rhsExpression.putNodeMetaData(CLOSURE_ARGUMENTS, expectedTypes);
        } else {
            addStaticTypeError("Wrong number of parameters for method target: " + toMethodParametersString(findSAM(lhsType).getName(), samParameterTypes), rhsExpression);
        }

        rhsExpression.putNodeMetaData(PARAMETER_TYPE, expectedTypes == samParameterTypes ? lhsType : null);
    }

    private void checkParamType(final Parameter source, final ClassNode target, final boolean isLast, final boolean lambda) {
        if (/*lambda ? !source.getOriginType().isDerivedFrom(target) :*/!typeCheckMethodArgumentWithGenerics(source.getOriginType(), target, isLast))
            addStaticTypeError("Expected type " + prettyPrintType(target) + " for " + (lambda ? "lambda" : "closure") + " parameter: " + source.getName(), source);
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
        VariableExpression receiver = varX("%", setterInfo.receiverType);
        receiver.setType(setterInfo.receiverType); // same as origin type

        Function<Expression, MethodNode> setterCall = (value) -> {
            typeCheckingContext.pushEnclosingBinaryExpression(null); // GROOVY-10628: LHS re-purposed
            try {
                MethodCallExpression call = callX(receiver, setterInfo.name, value);
                call.setImplicitThis(false);
                visitMethodCallExpression(call);
                return call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            } finally {
                typeCheckingContext.popEnclosingBinaryExpression();
            }
        };

        Function<MethodNode, ClassNode> setterType = (setter) -> {
            ClassNode type = setter.getParameters()[0].getOriginType();
            if (!setter.isStatic() && !(setter instanceof ExtensionMethodNode) && GenericsUtils.hasUnresolvedGenerics(type)) {
                type = applyGenericsContext(extractPlaceHolders(setterInfo.receiverType, setter.getDeclaringClass()), type);
            }
            return type;
        };

        Expression valueExpression = rightExpression;
        // for "x op= y", find type as if it was "x = x op y"
        if (isCompoundAssignment(expression)) {
            Token op = ((BinaryExpression) expression).getOperation();
            if (op.getType() == ELVIS_EQUAL) { // GROOVY-10419: "x ?= y"
                valueExpression = elvisX(leftExpression, rightExpression);
            } else {
                op = Token.newSymbol(TokenUtil.removeAssignment(op.getType()), op.getStartLine(), op.getStartColumn());
                valueExpression = binX(leftExpression, op, rightExpression);
            }
        }

        MethodNode methodTarget = setterCall.apply(valueExpression);
        if (methodTarget == null && !isCompoundAssignment(expression)) {
            // if no direct match, try implicit conversion
            for (MethodNode setter : setterInfo.setters) {
                ClassNode lType = setterType.apply(setter);
                ClassNode rType = getDeclaredOrInferredType(valueExpression);
                if (lType.isArray() && valueExpression instanceof ListExpression) {
                    rType = inferLoopElementType(rType).makeArray(); // GROOVY-7506
                }
                if (checkCompatibleAssignmentTypes(lType, rType, valueExpression, false)) {
                    methodTarget = setterCall.apply(castX(lType, valueExpression));
                    if (methodTarget != null) {
                        break;
                    }
                }
            }
        }

        if (methodTarget != null) {
            for (MethodNode setter : setterInfo.setters) {
                if (setter == methodTarget) {
                    leftExpression.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, methodTarget);
                    leftExpression.removeNodeMetaData(INFERRED_TYPE); // clear assumption
                    storeType(leftExpression, setterType.apply(methodTarget));
                    break;
                }
            }
            return false;
        } else {
            List<MethodNode> visibleSetters = filterMethodsByVisibility(setterInfo.setters, typeCheckingContext.getEnclosingClassNode());
            if (visibleSetters.isEmpty()) {
                String message; // GROOVY-11223
                if (setterInfo.setters.size() == 1) {
                    message = String.format("Cannot access method: %2$s of class: %1$s",
                        prettyPrintTypeName(setterInfo.setters.get(0).getDeclaringClass()),
                        toMethodParametersString(setterInfo.name, extractTypesFromParameters(setterInfo.setters.get(0).getParameters())));
                } else {
                    message = "Cannot access methods: " + prettyPrintMethodList(setterInfo.setters);
                }
                addStaticTypeError(message, leftExpression);
            } else {
                ClassNode[] tergetTypes = visibleSetters.stream().map(setterType).toArray(ClassNode[]::new);
                addAssignmentError(tergetTypes.length == 1 ? tergetTypes[0] : new UnionTypeClassNode(tergetTypes), getType(valueExpression), expression);
            }
            return true;
        }
    }

    private static boolean isClosureWithType(final ClassNode type) {
        return CLOSURE_TYPE.equals(type) && Optional.ofNullable(type.getGenericsTypes()).filter(gts -> gts != null && gts.length == 1).isPresent();
    }

    private static boolean isCompoundAssignment(final Expression exp) {
        if (exp instanceof BinaryExpression) {
            Token op = ((BinaryExpression) exp).getOperation();
            return isAssignment(op.getType()) && op.getType() != EQUAL;
        }
        return false;
    }

    protected ClassNode getOriginalDeclarationType(final Expression lhs) {
        Variable var = null;
        if (lhs instanceof FieldExpression) {
            var = ((FieldExpression) lhs).getField();
        } else if (lhs instanceof VariableExpression) {
            var = findTargetVariable((VariableExpression) lhs);
        }
        return var != null && !(var instanceof DynamicVariable) ? var.getOriginType() : getType(lhs);
    }

    protected void inferDiamondType(final ConstructorCallExpression cce, final ClassNode lType) {
        ClassNode cceType = cce.getType(), inferredType = lType;
        // check if constructor call expression makes use of the diamond operator
        if (cceType.getGenericsTypes() != null && cceType.getGenericsTypes().length == 0) {
            ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(cce.getArguments());
            ConstructorNode constructor = cce.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (!argumentList.getExpressions().isEmpty() && constructor != null) {
                ClassNode type = GenericsUtils.parameterizeType(cceType, cceType);
                type = inferReturnTypeGenerics(type, constructor, argumentList);
                if (lType.getGenericsTypes() != null // GROOVY-10367: nothing to inspect
                        && (type.toString(false).indexOf('#') > 0 // GROOVY-9983, GROOVY-10291, GROOVY-10368: unresolved generic
                        // GROOVY-6232, GROOVY-9956: if cce not assignment compatible, process target as additional type witness
                        || checkCompatibleAssignmentTypes(lType, type, cce) && !GenericsUtils.buildWildcardType(lType).isCompatibleWith(type))) {
                    // allow covariance of each type parameter, but maintain semantics for nested generics

                    ClassNode pType = GenericsUtils.parameterizeType(lType, type);
                    GenericsType[] lhs = pType.getGenericsTypes(), rhs = type.getGenericsTypes();
                    if (lhs == null || rhs == null || lhs.length != rhs.length) throw new GroovyBugError(
                            "Parameterization failed: " + prettyPrintType(pType) + " ~ " + prettyPrintType(type));

                    if (IntStream.range(0, lhs.length).allMatch(i ->
                            GenericsUtils.buildWildcardType(getCombinedBoundType(lhs[i])).isCompatibleWith(rhs[i].getType()))) {
                        type = pType; // lType proved to be a viable type witness
                    }
                }
                inferredType = type;
            }
            // GROOVY-10344, GROOVY-10847: "T t = new C<>()" where "T" may extend another type parameter
            while (inferredType.isGenericsPlaceHolder() && asBoolean(inferredType.getGenericsTypes())) {
                inferredType = getCombinedBoundType(inferredType.getGenericsTypes()[0]);
            }
            adjustGenerics(inferredType, cceType);
            storeType(cce, cceType);
        }
    }

    private void adjustGenerics(final ClassNode source, final ClassNode target) {
        GenericsType[] genericsTypes = source.getGenericsTypes();
        if (genericsTypes == null) { // Map foo = new HashMap<>()
            genericsTypes = target.redirect().getGenericsTypes().clone();
            for (int i = 0, n = genericsTypes.length; i < n; i += 1) {
                GenericsType gt = genericsTypes[i];
                // GROOVY-10055: handle diamond or raw
                ClassNode cn = gt.getUpperBounds() != null
                        ? gt.getUpperBounds()[0] : gt.getType().redirect();
                genericsTypes[i] = cn.getPlainNodeReference().asGenericsType();
            }
        } else {
            // GROOVY-11192: mapping between source and target type parameter(s)
            if (!source.equals(target)) {
                assert source.isInterface() ? target.implementsInterface(source) : target.isDerivedFrom(source);
                ClassNode mapped = adjustForTargetType(target, source);
                genericsTypes = mapped.getGenericsTypes();
            }
            genericsTypes = genericsTypes.clone();
            for (int i = 0, n = genericsTypes.length; i < n; i += 1) {
                GenericsType gt = genericsTypes[i];
                genericsTypes[i] = new GenericsType(gt.getType(),
                        gt.getUpperBounds(), gt.getLowerBound());
                genericsTypes[i].setWildcard(gt.isWildcard()); // GROOVY-10310
            }
        }
        target.setGenericsTypes(genericsTypes);
    }

    private boolean typeCheckMultipleAssignmentAndContinue(final Expression leftExpression, Expression rightExpression) {
        if (rightExpression instanceof VariableExpression || rightExpression instanceof PropertyExpression || rightExpression instanceof MethodCall) {
            ClassNode inferredType = Optional.ofNullable(getType(rightExpression)).orElseGet(rightExpression::getType);
            GenericsType[] genericsTypes = inferredType.getGenericsTypes();
            ListExpression listExpression = new ListExpression();
            listExpression.setSourcePosition(rightExpression);

            // convert Tuple[1-16] bearing expressions to mock list for checking
            for (int n = TUPLE_TYPES.indexOf(inferredType), i = 0; i < n; i += 1) {
                ClassNode type = (genericsTypes != null ? genericsTypes[i].getType() : OBJECT_TYPE);
                listExpression.addExpression(varX("v" + (i + 1), type));
            }
            if (!listExpression.getExpressions().isEmpty()) {
                rightExpression = listExpression;
            }
        } else if (rightExpression instanceof RangeExpression) {
            ListExpression listExpression = new ListExpression();
            listExpression.setSourcePosition(rightExpression);
            ClassNode type = getType(((RangeExpression) rightExpression).getFrom());
            TupleExpression tuple = (TupleExpression) leftExpression;
            for (int i = 0; i < tuple.getExpressions().size(); i++) {
                Expression expression = indexX(rightExpression, constX(i, true));
                expression.putNodeMetaData(INFERRED_TYPE, type);
                listExpression.addExpression(expression);
            }
            if (!listExpression.getExpressions().isEmpty()) {
                rightExpression = listExpression;
            }
        }

        if (!(rightExpression instanceof ListExpression)) {
            addStaticTypeError("Multiple assignments without list or tuple on the right-hand side are unsupported in static type checking mode", rightExpression);
            return false;
        }

        TupleExpression tuple = (TupleExpression) leftExpression;
        ListExpression values = (ListExpression) rightExpression;
        List<Expression> tupleExpressions = tuple.getExpressions();
        List<Expression> valueExpressions = values.getExpressions();

        if (tupleExpressions.size() > valueExpressions.size()) {
            addStaticTypeError("Incorrect number of values. Expected:" + tupleExpressions.size() + " Was:" + valueExpressions.size(), values);
            return false;
        }

        for (int i = 0, n = tupleExpressions.size(); i < n; i += 1) {
            ClassNode valueType = getType(valueExpressions.get(i));
            ClassNode targetType = getType(tupleExpressions.get(i));
            if (!isAssignableTo(valueType, targetType)) {
                addStaticTypeError("Cannot assign value of type " + prettyPrintType(valueType) + " to variable of type " + prettyPrintType(targetType), rightExpression);
                return false;
            }
            storeType(tupleExpressions.get(i), valueType);
        }

        return true;
    }

    private ClassNode adjustTypeForSpreading(final ClassNode rightExpressionType, final Expression leftExpression) {
        // given "list*.foo = 100" or "map*.value = 100", then the assignment must be checked against [100], not 100
        if (leftExpression instanceof PropertyExpression && ((PropertyExpression) leftExpression).isSpreadSafe()) {
            return extension.buildListType(rightExpressionType);
        }
        return rightExpressionType;
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
                addStaticTypeError("Possible loss of precision from " + prettyPrintType(rhsType) + " to " + prettyPrintType(lhsType), rightExpression);
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
                    addStaticTypeError("Cannot assign value of type " + prettyPrintType(rightComponentType) + " into array of type " + prettyPrintType(lhsType), rightExpression);
                }
            }
        } else if (rhsType.redirect().isArray()) {
            ClassNode leftComponentType = leftRedirect.getComponentType();
            ClassNode rightComponentType = rhsType.redirect().getComponentType();
            if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType)) {
                addStaticTypeError("Cannot assign value of type " + prettyPrintType(rightComponentType) + " into array of type " + prettyPrintType(lhsType), rightExpression);
            }
        }
    }

    private void addListAssignmentConstructorErrors(final ClassNode leftRedirect, final ClassNode leftExpressionType, final ClassNode inferredRightExpressionType, final Expression rightExpression, final Expression assignmentExpression) {
        if (isWildcardLeftHandSide(leftRedirect) && !isClassType(leftRedirect)) return; // GROOVY-6802, GROOVY-6803
        // if left type is not a list but right type is a list, then we're in the case of a groovy
        // constructor type : Dimension d = [100,200]
        // In that case, more checks can be performed
        if (!implementsInterfaceOrIsSubclassOf(LIST_TYPE, leftRedirect)
                && (!leftRedirect.isAbstract() || leftRedirect.isArray())
                && !ArrayList_TYPE.isDerivedFrom(leftRedirect) && !LinkedHashSet_TYPE.isDerivedFrom(leftRedirect)) {
            ClassNode[] types = getArgumentTypes(args(((ListExpression) rightExpression).getExpressions()));
            MethodNode methodNode = checkGroovyStyleConstructor(leftRedirect, types, assignmentExpression);
            if (methodNode != null) {
                rightExpression.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, methodNode);
            }
        } else if (implementsInterfaceOrIsSubclassOf(inferredRightExpressionType, LIST_TYPE)
                && !implementsInterfaceOrIsSubclassOf(inferredRightExpressionType, leftRedirect)) {
            if (!extension.handleIncompatibleAssignment(leftExpressionType, inferredRightExpressionType, assignmentExpression)) {
                addAssignmentError(leftExpressionType, inferredRightExpressionType, assignmentExpression);
            }
        }
    }

    private void addMapAssignmentConstructorErrors(final ClassNode leftRedirect, final Expression leftExpression, final MapExpression rightExpression) {
        if (!isConstructorAbbreviation(leftRedirect, rightExpression)
                // GROOVY-6802, GROOVY-6803: Object, String or [Bb]oolean target
                || (isWildcardLeftHandSide(leftRedirect) && !isClassType(leftRedirect))) {
            return;
        }

        // groovy constructor shorthand: A a = [x:2, y:3]
        ClassNode[] argTypes = {getType(rightExpression)};
        checkGroovyStyleConstructor(leftRedirect, argTypes, rightExpression);
        // perform additional type checking on arguments
        checkGroovyConstructorMap(leftExpression, leftRedirect, rightExpression);
    }

    private void checkTypeGenerics(final ClassNode leftExpressionType, final ClassNode rightExpressionType, final Expression rightExpression) {
        if (leftExpressionType.isUsingGenerics()
                && !missesGenericsTypes(rightExpressionType)
                && !(rightExpression instanceof ClosureExpression) // GROOVY-10277
                && !isNullConstant(rightExpression) && !UNKNOWN_PARAMETER_TYPE.equals(rightExpressionType)
                && !GenericsUtils.buildWildcardType(leftExpressionType).isCompatibleWith(wrapTypeIfNecessary(rightExpressionType)))
            addStaticTypeError("Incompatible generic argument types. Cannot assign " + prettyPrintType(rightExpressionType) + " to: " + prettyPrintType(leftExpressionType), rightExpression);
    }

    private boolean hasGStringStringError(final ClassNode leftExpressionType, final ClassNode wrappedRHS, final Expression rightExpression) {
        if (isParameterizedWithString(leftExpressionType) && isParameterizedWithGStringOrGStringString(wrappedRHS)) {
            addStaticTypeError("You are trying to use a GString in place of a String in a type which explicitly declares accepting String. " +
                    "Make sure to call toString() on all GString values.", rightExpression);
            return true;
        }
        return false;
    }

    private static boolean isConstructorAbbreviation(final ClassNode leftType, final Expression rightExpression) {
        if (rightExpression instanceof ListExpression) {
            return !(ArrayList_TYPE.isDerivedFrom(leftType) || ArrayList_TYPE.implementsInterface(leftType)
                    || LinkedHashSet_TYPE.isDerivedFrom(leftType) || LinkedHashSet_TYPE.implementsInterface(leftType));
        }
        if (rightExpression instanceof MapExpression) {
            return !(LinkedHashMap_TYPE.isDerivedFrom(leftType) || LinkedHashMap_TYPE.implementsInterface(leftType));
        }
        return false;
    }

    protected void typeCheckAssignment(final BinaryExpression assignmentExpression, final Expression leftExpression, final ClassNode leftExpressionType, final Expression rightExpression, final ClassNode rightExpressionType) {
        if (leftExpression instanceof TupleExpression) {
            if (!typeCheckMultipleAssignmentAndContinue(leftExpression, rightExpression)) return;
        }

        // TODO: need errors for write-only too!
        if (addedReadOnlyPropertyError(leftExpression)) return;

        ClassNode rType = adjustTypeForSpreading(rightExpressionType, leftExpression);

        if (!checkCompatibleAssignmentTypes(leftExpressionType, rType, rightExpression)) {
            if (!extension.handleIncompatibleAssignment(leftExpressionType, rType, assignmentExpression)) {
                addAssignmentError(leftExpressionType, rightExpressionType, rightExpression);
            }
        } else {
            ClassNode lTypeRedirect = leftExpressionType.redirect();
            addPrecisionErrors(lTypeRedirect, leftExpressionType, rType, rightExpression);
            if (rightExpression instanceof ListExpression) {
                addListAssignmentConstructorErrors(lTypeRedirect, leftExpressionType, rightExpressionType, rightExpression, assignmentExpression);
            } else if (rightExpression instanceof MapExpression) {
                addMapAssignmentConstructorErrors(lTypeRedirect, leftExpression, (MapExpression)rightExpression);
            }
            if (!hasGStringStringError(leftExpressionType, rType, rightExpression) && !isConstructorAbbreviation(leftExpressionType, rightExpression)) {
                checkTypeGenerics(leftExpressionType, rType, rightExpression);
            }
        }
    }

    protected void checkGroovyConstructorMap(final Expression receiver, final ClassNode receiverType, final MapExpression mapExpression) {
        for (MapEntryExpression entryExpression : mapExpression.getMapEntryExpressions()) {
            Expression keyExpression = entryExpression.getKeyExpression();
            if (!(keyExpression instanceof ConstantExpression)) {
                addStaticTypeError("Dynamic keys in map-style constructors are unsupported in static type checking", keyExpression);
            } else {
                String propName = keyExpression.getText();
                Set<ClassNode> propertyTypes = new HashSet<>();
                Expression valueExpression = entryExpression.getValueExpression();
                typeCheckingContext.pushEnclosingBinaryExpression(assignX(keyExpression, valueExpression, entryExpression));
                if (!existsProperty(propX(varX("_", receiverType), propName), false, new PropertyLookup(receiverType, propertyTypes::add))) {
                    typeCheckingContext.popEnclosingBinaryExpression();
                    addStaticTypeError("No such property: " + propName + " for class: " + prettyPrintTypeName(receiverType), receiver);
                } else {
                    ClassNode valueType = getType(valueExpression);
                    BinaryExpression kv = typeCheckingContext.popEnclosingBinaryExpression();
                    if (propertyTypes.stream().noneMatch(targetType -> checkCompatibleAssignmentTypes(targetType, getResultType(targetType, ASSIGN, valueType, kv), valueExpression))) {
                        ClassNode targetType = propertyTypes.size() == 1 ? propertyTypes.iterator().next() : new UnionTypeClassNode(propertyTypes.toArray(ClassNode.EMPTY_ARRAY));
                        if (!extension.handleIncompatibleAssignment(targetType, valueType, entryExpression)) {
                            addAssignmentError(targetType, valueType, entryExpression);
                        }
                    }
                }
            }
        }
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
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
     */
    protected MethodNode checkGroovyStyleConstructor(final ClassNode node, final ClassNode[] arguments, final ASTNode origin) {
        if (isObjectType(node) || isDynamicTyped(node)) {
            // in that case, we are facing a list constructor assigned to a def or object
            return null;
        }
        List<? extends MethodNode> constructors = node.getDeclaredConstructors();
        if (constructors.isEmpty() && arguments.length == 0) {
            return null;
        }
        constructors = findMethod(node, "<init>", arguments);
        if (constructors.isEmpty()) {
            if (isBeingCompiled(node) && !node.isAbstract() && arguments.length == 1 && arguments[0].equals(LinkedHashMap_TYPE)) {
                // there will be a default hash map constructor added later
                return new ConstructorNode(Opcodes.ACC_PUBLIC, new Parameter[]{new Parameter(LinkedHashMap_TYPE, "args")}, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
            } else {
                addNoMatchingMethodError(node, "<init>", arguments, origin);
                return null;
            }
        } else if (constructors.size() > 1) {
            addStaticTypeError("Ambiguous constructor call " + prettyPrintTypeName(node) + toMethodParametersString("", arguments), origin);
            return null;
        }
        return constructors.get(0);
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

        final String propertyName = pexp.getPropertyAsString();
        if (propertyName == null) return false;

        Expression objectExpression = pexp.getObjectExpression();
        if (objectExpression instanceof ConstructorCallExpression) {
            ClassNode rawType = getType(objectExpression).getPlainNodeReference();
            inferDiamondType((ConstructorCallExpression) objectExpression, rawType);
        }
        // enclosing excludes classes that skip STC
        Set<ClassNode> enclosingTypes = new LinkedHashSet<>();
        enclosingTypes.add(typeCheckingContext.getEnclosingClassNode());
        enclosingTypes.addAll(enclosingTypes.iterator().next().getOuterClasses());

        if (objectExpression instanceof ClassExpression) {
            if (propertyName.equals("this")) {
                // handle "Outer.this" for any level of nesting
                ClassNode outer = getType(objectExpression).getGenericsTypes()[0].getType();

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
            } else if (propertyName.equals("super")) {
                // GROOVY-8299: handle "Iface.super" for interface default methods
                ClassNode enclosingType = typeCheckingContext.getEnclosingClassNode();
                ClassNode accessingType = getType(objectExpression).getGenericsTypes()[0].getType();
                if (accessingType.isInterface() && enclosingType.implementsInterface(accessingType)) {
                    storeType(pexp, accessingType);
                    return true;
                }
                return false;
            }
        }

        final String isserName  = getGetterName(propertyName, Boolean.TYPE);
        final String getterName = getGetterName(propertyName);
        final String setterName = getSetterName(propertyName);

        boolean foundGetterOrSetter = false;
        Set<ClassNode> handledTypes = new HashSet<>();
        List<Receiver<String>> receivers = new ArrayList<>();
        addReceivers(receivers, makeOwnerList(objectExpression), pexp.isImplicitThis());

        for (Receiver<String> receiver : receivers) {
            ClassNode receiverType = receiver.getType();

            if (receiverType.isArray() && propertyName.equals("length")) {
                pexp.putNodeMetaData(READONLY_PROPERTY, Boolean.TRUE);
                storeType(pexp, int_TYPE);
                if (visitor != null) {
                    FieldNode length = new FieldNode("length", Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, int_TYPE, receiverType, null);
                    length.setDeclaringClass(receiverType);
                    visitor.visitField(length);
                }
                return true;
            }

            // in case of a lookup on java.lang.Class, look for instance methods on Class
            // in case of static property access, Type (static) and Class<Type> are tried
            boolean staticOnly = !receiver.isObject();
            // GROOVY-11367, GROOVY-11384, GROOVY-11387: entry before a non-public member
            boolean publicOnly = !staticOnly && isOrImplements(receiverType, MAP_TYPE);

            List<MethodNode> setters = new ArrayList<>(4);
            for (MethodNode method : findMethodsWithGenerated(wrapTypeIfNecessary(receiverType), setterName)) {
                if (method.getParameters().length == 1 && (!staticOnly || method.isStatic()) && (!publicOnly || method.isPublic())
                        // GROOVY-11319:
                        && hasAccessToMember(typeCheckingContext.getEnclosingClassNode(), method.getDeclaringClass(), method.getModifiers())) {
                    setters.add(method);
                }
            }
            // GROOVY-11372:
            var loader = getSourceUnit().getClassLoader();
            var dgmSet = (TreeSet<MethodNode>) findDGMMethodsForClassNode(loader,            receiverType,  setterName);
            if (isPrimitiveType(receiverType)) findDGMMethodsForClassNode(loader, getWrapper(receiverType), setterName, dgmSet);
            for (MethodNode method : dgmSet) {
                if (method.getParameters().length == 1 && (!staticOnly || method.isStatic())) {
                    setters.add(method);
                }
            }

            var queue = new LinkedList<ClassNode>();
            queue.add(receiverType);
            if (isPrimitiveType(receiverType)) {
                queue.add(getWrapper(receiverType));
            } else if (receiverType.isInterface()) {
                queue.add(OBJECT_TYPE);//GROOVY-5585
            }
            while (!queue.isEmpty()) {
                ClassNode current = queue.remove();
                if (!handledTypes.add(current)) continue;

                FieldNode field = current.getDeclaredField(propertyName);
                if (field == null) {
                    if (current.getSuperClass() != null)
                        queue.addFirst(current.getSuperClass());
                    Collections.addAll(queue, current.getInterfaces());
                }
                else field = allowStaticAccessToMember(field, staticOnly);

                // skip property/accessor checks for "x.@field"
                if (pexp instanceof AttributeExpression) {
                    if (field != null && storeField(field, pexp, receiverType, visitor, receiver.getData(), !readMode)) {
                        return true;
                    }
                    continue;
                }

                if (field != null && publicOnly && !field.isPublic()
                        // GROOVY-11367, GROOVY-11402, GROOVY-11403:
                        && (!receiverType.equals(current) || !isThisExpression(objectExpression) || typeCheckingContext.getEnclosingClosure() != null)) {
                    field = null;
                }
                // skip property/accessor checks for "field", "this.field", "this.with { field }", etc. within the declaring class of the field
                else if (field != null && enclosingTypes.contains(current) && storeField(field, pexp, receiverType, visitor, receiver.getData(), !readMode)) {
                    return true;
                }

                MethodNode getter = current.getGetterMethod(isserName);
                getter = allowStaticAccessToMember(getter, staticOnly);
                if (getter == null) {
                    getter = current.getGetterMethod(getterName);
                    getter = allowStaticAccessToMember(getter, staticOnly);
                }
                if (getter != null && ((publicOnly && (!getter.isPublic() || propertyName.equals("class") || propertyName.equals("empty")))
                        // GROOVY-11319:
                        || !hasAccessToMember(typeCheckingContext.getEnclosingClassNode(), getter.getDeclaringClass(), getter.getModifiers()))) {
                    getter = null;
                }

                if (readMode && getter != null && visitor != null) visitor.visitMethod(getter);

                PropertyNode property = current.getProperty(propertyName);
                property = allowStaticAccessToMember(property, staticOnly);
                // prefer explicit getter or setter over property if receiver is not 'this'
                if (property == null || !enclosingTypes.contains(receiverType)) {
                    if (readMode) {
                        if (getter != null) {
                            ClassNode returnType = inferReturnTypeGenerics(receiverType, getter, ArgumentListExpression.EMPTY_ARGUMENTS);
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
                                for (MethodNode setter : setters) {
                                    // visiting setter will not infer the property type since return type is void, so visit a dummy field instead
                                    FieldNode virtual = new FieldNode(propertyName, 0, setter.getParameters()[0].getOriginType(), current, null);
                                    virtual.setDeclaringClass(setter.getDeclaringClass());
                                    visitor.visitField(virtual);
                                }
                            }
                            SetterInfo info = new SetterInfo(current, setterName, setters);
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
                        } else if (getter != null && (field == null || field.isFinal())) {
                            pexp.putNodeMetaData(READONLY_PROPERTY, Boolean.TRUE); // GROOVY-9127
                        }
                    }
                }

                if (property != null && storeProperty(property, pexp, receiverType, visitor, receiver.getData(), !readMode)) return true;

                if (field != null && storeField(field, pexp, receiverType, visitor, receiver.getData(), !readMode)) return true;

                foundGetterOrSetter = (foundGetterOrSetter || getter != null || !setters.isEmpty());
            }

            // GROOVY-5568, GROOVY-9115, GROOVY-9123: the property may be defined by an extension
            for (ClassNode dgmReceiver : isPrimitiveType(receiverType) ? new ClassNode[]{receiverType, getWrapper(receiverType)} : new ClassNode[]{receiverType}) {
                Set<MethodNode> methods = findDGMMethodsForClassNode(loader, dgmReceiver, getterName);
                for (MethodNode method : findDGMMethodsForClassNode(loader, dgmReceiver, isserName)) {
                    if (isPrimitiveBoolean(method.getReturnType())) methods.add(method);
                }
                if (!receiver.isObject()) {
                    // GROOVY-10820: ensure static extension when property accessed in static manner
                    methods.removeIf(method -> !((ExtensionMethodNode) method).isStaticExtension());
                }
                if (isUsingGenericsOrIsArrayUsingGenerics(dgmReceiver)) {
                    methods.removeIf(method -> // GROOVY-10075: "List<Integer>" vs "List<String>"
                        !typeCheckMethodsWithGenerics(dgmReceiver, ClassNode.EMPTY_ARRAY, method)
                    );
                }
                List<MethodNode> bestMethods = chooseBestMethod(dgmReceiver, methods, ClassNode.EMPTY_ARRAY);
                if (bestMethods.size() == 1) {
                    if (readMode) {
                        MethodNode getter = bestMethods.get(0);
                        if (visitor != null) {
                            visitor.visitMethod(getter);
                        }
                        ClassNode returnType = inferReturnTypeGenerics(dgmReceiver, getter, ArgumentListExpression.EMPTY_ARGUMENTS);
                        storeInferredTypeForPropertyExpression(pexp, returnType);
                        storeTargetMethod(pexp, getter);
                        return true;
                    } else if (setters.isEmpty()) { // GROOVY-11372
                        pexp.putNodeMetaData(READONLY_PROPERTY, Boolean.TRUE);
                    }
                }
            }

            // GROOVY-7996: check if receiver declares get(String), getProperty(String), propertyMissing(String) or $static_propertyMissing(String)
            if (pexp.isImplicitThis() && !receiverType.isArray() && !receiverType.isScriptBody() && !isPrimitiveType(getUnwrapper(receiverType))) {
                MethodNode mopMethod;
                if (readMode) {
                    Parameter[] name = {new Parameter(STRING_TYPE, "name")};
                    mopMethod = receiverType.getMethod("get", name);
                    if (mopMethod == null) mopMethod = receiverType.getMethod("getProperty", name);
                    if (mopMethod == null || mopMethod.isStatic() || mopMethod.isSynthetic()) mopMethod = receiverType.getMethod("propertyMissing", name);
                } else {
                    Parameter[] nameAndValue = {new Parameter(STRING_TYPE, "name"), new Parameter(OBJECT_TYPE, "value")};
                    mopMethod = receiverType.getMethod("set", nameAndValue);
                    if (mopMethod == null) mopMethod = receiverType.getMethod("setProperty", nameAndValue);
                    if (mopMethod == null || mopMethod.isStatic() || mopMethod.isSynthetic()) mopMethod = receiverType.getMethod("propertyMissing", nameAndValue);
                }
                if (mopMethod != null && !mopMethod.isStatic() && !mopMethod.isSynthetic()) {
                    pexp.putNodeMetaData(DYNAMIC_RESOLUTION, Boolean.TRUE);
                    pexp.removeNodeMetaData(DECLARATION_INFERRED_TYPE);
                    pexp.removeNodeMetaData(INFERRED_TYPE);
                    if (visitor != null)
                        visitor.visitMethod(mopMethod);
                    return true;
                }
            }

            // GROOVY-11401
            if (!staticOnly && isOrImplements(receiverType, MAP_TYPE)) break;
        }

        for (Receiver<String> receiver : receivers) {
            if (receiver.isObject()) {
                ClassNode receiverType = receiver.getType();
                ClassNode propertyType = getTypeForMapPropertyExpression(receiverType, pexp);
                if (propertyType == null)
                    propertyType = getTypeForListPropertyExpression(receiverType, pexp);
                if (propertyType == null)
                    propertyType = getTypeForSpreadExpression(receiverType, pexp);
                if (propertyType == null)
                    continue;
                if (visitor != null) {
                    PropertyNode node = new PropertyNode(propertyName, Opcodes.ACC_PUBLIC, propertyType, receiverType, null, null, null);
                    node.setDeclaringClass(receiverType);
                    visitor.visitProperty(node);
                }
                storeType(pexp, propertyType);
                String delegationData = receiver.getData();
                if (delegationData != null) pexp.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
                return true;
            }
        }

        if (pexp.isImplicitThis() && isThisExpression(objectExpression)) {
            Iterator<ClassNode> iter = enclosingTypes.iterator(); // first enclosing is "this" type
            boolean staticOnly = Modifier.isStatic(iter.next().getModifiers()) || typeCheckingContext.isInStaticContext;
            while (iter.hasNext()) {
                ClassNode outer = iter.next();
                // GROOVY-7994, GROOVY-11198: try "this.propertyName" as "Outer.propertyName" or "Outer.this.propertyName"
                PropertyExpression pe = propX(staticOnly ? classX(outer) : propX(classX(outer), "this"), pexp.getProperty());
                if (existsProperty(pe, readMode, visitor)) {
                    pexp.copyNodeMetaData(pe);
                    return true;
                }
                staticOnly = staticOnly || Modifier.isStatic(outer.getModifiers());
            }
        }

        return foundGetterOrSetter;
    }

    private static boolean hasAccessToMember(final ClassNode accessor, final ClassNode receiver, final int modifiers) {
        if (Modifier.isPublic(modifiers)
                || accessor.equals(receiver)
                || accessor.getOuterClasses().contains(receiver)) {
            return true;
        }
        if (!Modifier.isPrivate(modifiers) && Objects.equals(accessor.getPackageName(), receiver.getPackageName())) {
            return true;
        }
        return Modifier.isProtected(modifiers) && accessor.isDerivedFrom(receiver);
    }

    private ClassNode getTypeForMultiValueExpression(final ClassNode compositeType, final Expression prop) {
        GenericsType[] gts = compositeType.getGenericsTypes();
        ClassNode itemType = (gts != null && gts.length == 1 ? getCombinedBoundType(gts[0]) : OBJECT_TYPE);

        List<ClassNode> propertyTypes = new ArrayList<>();
        if (existsProperty(propX(varX("_", itemType), prop), true, new PropertyLookup(itemType, propertyTypes::add))) {
            return extension.buildListType(propertyTypes.get(0));
        }
        return null;
    }

    private ClassNode getTypeForSpreadExpression(final ClassNode testClass, final PropertyExpression pexp) {
        if (pexp.isSpreadSafe()) {
            MethodCallExpression mce = callX(varX("_", testClass), "iterator");
            mce.setImplicitThis(false);
            mce.visit(this);
            ClassNode iteratorType = getType(mce);
            if (isOrImplements(iteratorType, Iterator_TYPE)) {
                return getTypeForMultiValueExpression(iteratorType, pexp.getProperty());
            }
        }
        return null;
    }

    private ClassNode getTypeForListPropertyExpression(final ClassNode testClass, final PropertyExpression pexp) {
        if (isOrImplements(testClass, LIST_TYPE)) {
            ClassNode listType = testClass.equals(LIST_TYPE) ? testClass
                    : GenericsUtils.parameterizeType(testClass, LIST_TYPE);
            return getTypeForMultiValueExpression(listType, pexp.getProperty());
        }
        return null;
    }

    private ClassNode getTypeForMapPropertyExpression(final ClassNode testClass, final PropertyExpression pexp) {
        if (isOrImplements(testClass, MAP_TYPE)) {
            ClassNode mapType = testClass.equals(MAP_TYPE) ? testClass
                    : GenericsUtils.parameterizeType(testClass, MAP_TYPE);
            GenericsType[] gts = mapType.getGenericsTypes();//<K,V> params
            if (gts == null || gts.length != 2) gts = new GenericsType[] {
                OBJECT_TYPE.asGenericsType(), OBJECT_TYPE.asGenericsType()
            };

            if (!pexp.isSpreadSafe()) {
                if (pexp.isImplicitThis() && isThisExpression(pexp.getObjectExpression()))
                    pexp.putNodeMetaData(DYNAMIC_RESOLUTION,Boolean.TRUE); // GROOVY-11387
                return getCombinedBoundType(gts[1]);
            } else {
                // map*.property syntax acts on Entry
                switch (pexp.getPropertyAsString()) {
                  case "key":
                    pexp.putNodeMetaData(READONLY_PROPERTY,Boolean.TRUE); // GROOVY-10326
                    return makeClassSafe0(LIST_TYPE, gts[0]);
                  case "value":
                    GenericsType v = gts[1];
                    if (!v.isWildcard()
                            && !Modifier.isFinal(v.getType().getModifiers())
                            && typeCheckingContext.isTargetOfEnclosingAssignment(pexp)) {
                        v = GenericsUtils.buildWildcardType(v.getType()); // GROOVY-10325
                    }
                    return makeClassSafe0(LIST_TYPE, v);
                  default:
                    addStaticTypeError("Spread operator on map only allows one of [key,value]", pexp);
                }
            }
        }
        return null;
    }

    /**
     * Filters search result to prevent access to instance members from a static
     * context.
     * <p>
     * Return null if the given member is not static, but we want to access in a
     * static way (staticOnly=true). If we want to access in a non-static way we
     * always return the member, since then access to static members and
     * non-static members is allowed.
     *
     * @return {@code member} or null
     */
    private <T> T allowStaticAccessToMember(final T member, final boolean staticOnly) {
        if (member == null || !staticOnly) return member;

        if (member instanceof List) {
            @SuppressWarnings("unchecked")
            T list = (T) ((List<MethodNode>) member).stream()
                    .map(m -> allowStaticAccessToMember(m, true))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            return list;
        }

        boolean isStatic;
        if (member instanceof FieldNode) {
            isStatic = ((FieldNode) member).isStatic();
        } else if (member instanceof PropertyNode) {
            isStatic = ((PropertyNode) member).isStatic();
        } else { // assume member instanceof MethodNode
            isStatic = isStaticInContext((MethodNode) member);
        }
        return (isStatic ? member : null);
    }

    /**
     * Is the method called in a static or non-static manner?
     */
    private boolean isStaticInContext(final MethodNode method) {
        return method instanceof ExtensionMethodNode ? ((ExtensionMethodNode) method).isStaticExtension() : method.isStatic();
    }

    private boolean storeField(final FieldNode field, final PropertyExpression expressionToStoreOn, final ClassNode receiver, final ClassCodeVisitorSupport visitor, final String delegationData, final boolean lhsOfAssignment) {
        boolean superField = isSuperExpression(expressionToStoreOn.getObjectExpression());
        boolean accessible = (!superField && receiver.equals(field.getDeclaringClass()) && !field.getDeclaringClass().isAbstract()) // GROOVY-7300, GROOVY-11358
                || hasAccessToMember(typeCheckingContext.getEnclosingClassNode(), field.getDeclaringClass(), field.getModifiers());
        if (!accessible) {
            if (expressionToStoreOn instanceof AttributeExpression) {
                addStaticTypeError("Cannot access field: " + field.getName() + " of class: " + prettyPrintTypeName(field.getDeclaringClass()), expressionToStoreOn.getProperty());
            } else if (!field.isProtected()) { // private or package-private
                return false;
            }
        }

        if (visitor != null) visitor.visitField(field);
        checkOrMarkPrivateAccess(expressionToStoreOn, field, lhsOfAssignment);
        storeWithResolve(field.getOriginType(), receiver, field.getDeclaringClass(), field.isStatic(), expressionToStoreOn);
        if (delegationData != null) {
            expressionToStoreOn.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
        }
        if (field.isFinal()) {
            MethodNode enclosing = typeCheckingContext.getEnclosingMethod();
            if (enclosing == null || !enclosing.getName().endsWith("init>"))
                expressionToStoreOn.putNodeMetaData(READONLY_PROPERTY, Boolean.TRUE); // GROOVY-5450
        } else if (accessible) {
            expressionToStoreOn.removeNodeMetaData(READONLY_PROPERTY); // GROOVY-9127
        }
        return true;
    }

    private boolean storeProperty(final PropertyNode property, final PropertyExpression expression, final ClassNode receiver, final ClassCodeVisitorSupport visitor, final String delegationData, final boolean lhsOfAssignment) {
        if (visitor != null) visitor.visitProperty(property);

        ClassNode propertyType = property.getOriginType();
        storeWithResolve(propertyType, receiver, property.getDeclaringClass(), property.isStatic(), expression);

        if (delegationData != null) {
            expression.putNodeMetaData(IMPLICIT_RECEIVER, delegationData);
        }
        if (property.isFinal()) {
            expression.putNodeMetaData(READONLY_PROPERTY, Boolean.TRUE);
        } else {
            expression.removeNodeMetaData(READONLY_PROPERTY);
        }

        String methodName;
        ClassNode returnType;
        Parameter[] parameters;
        if (!lhsOfAssignment) {
            methodName = property.getGetterNameOrDefault();
            returnType = propertyType;
            parameters = Parameter.EMPTY_ARRAY;
        } else {
            methodName = property.getSetterNameOrDefault();
            returnType = VOID_TYPE;
            parameters = new Parameter[] {new Parameter(propertyType, "value")};
        }
        MethodNode accessMethod = new MethodNode(methodName, Opcodes.ACC_PUBLIC | (property.isStatic() ? Opcodes.ACC_STATIC : 0), returnType, parameters, ClassNode.EMPTY_ARRAY, null);
        accessMethod.setDeclaringClass(property.getDeclaringClass());
        accessMethod.setSynthetic(true);

        expression.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, accessMethod); // GROOVY-11029
        extension.onMethodSelection(expression, accessMethod);
        return true;
    }

    private void storeWithResolve(ClassNode type, final ClassNode receiver, final ClassNode declaringClass, final boolean isStatic, final Expression expressionToStoreOn) {
        if (!isStatic && GenericsUtils.hasUnresolvedGenerics(type)) {
            type = resolveGenericsWithContext(extractPlaceHolders(receiver, declaringClass), type);
        }
        if (expressionToStoreOn instanceof PropertyExpression) {
            storeInferredTypeForPropertyExpression((PropertyExpression) expressionToStoreOn, type);
        } else {
            storeType(expressionToStoreOn, type);
        }
    }

    private ClassNode resolveGenericsWithContext(final Map<GenericsTypeName, GenericsType> resolvedPlaceholders, final ClassNode currentType) {
        Map<GenericsTypeName, GenericsType> placeholdersFromContext = extractGenericsParameterMapOfThis(typeCheckingContext);
        return resolveClassNodeGenerics(resolvedPlaceholders, placeholdersFromContext, currentType);
    }

    private void storeInferredTypeForPropertyExpression(final PropertyExpression pexp, final ClassNode type) {
        if (pexp.isSpreadSafe()) {
            storeType(pexp, extension.buildListType(type));
        } else {
            storeType(pexp, type);
        }
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        boolean osc = typeCheckingContext.isInStaticContext;
        try {
            typeCheckingContext.isInStaticContext = node.isInStaticContext();
            currentProperty = node;
            visitAnnotations(node);
            visitClassCodeContainer(node.getGetterBlock());
            visitClassCodeContainer(node.getSetterBlock());
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
            visitAnnotations(node);
            visitInitialExpression(node.getInitialExpression(), new FieldExpression(node), node);
        } finally {
            currentField = null;
            typeCheckingContext.isInStaticContext = osc;
        }
    }

    private void visitInitialExpression(final Expression value, final Expression target, final ASTNode origin) {
        if (value != null) {
            ClassNode lType = target.getType();
            applyTargetType(lType, value); // GROOVY-9977

            typeCheckingContext.pushEnclosingBinaryExpression(assignX(target, value, origin));

            value.visit(this);
            ClassNode rType = getType(value);
            if (value instanceof ConstructorCallExpression) {
                inferDiamondType((ConstructorCallExpression) value, lType);
            }

            BinaryExpression dummy = typeCheckingContext.popEnclosingBinaryExpression();
            typeCheckAssignment(dummy, target, lType, value, getResultType(lType, ASSIGN, rType, dummy));
        }
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        // collect every variable expression used in the loop body
        Map<VariableExpression, ClassNode> varTypes = new HashMap<>();
        forLoop.getLoopBlock().visit(new VariableExpressionTypeMemoizer(varTypes));

        // visit body
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        Expression collectionExpression = forLoop.getCollectionExpression();
        if (collectionExpression instanceof ClosureListExpression) {
            // for (int i=0; i<...; i++) style loop
            super.visitForLoop(forLoop);
        } else {
            visitStatement(forLoop);
            collectionExpression.visit(this);
            ClassNode collectionType = getType(collectionExpression);
            ClassNode forLoopVariableType = forLoop.getVariableType();
            ClassNode componentType;
            if (isWrapperCharacter(getWrapper(forLoopVariableType)) && isStringType(collectionType)) {
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
                addStaticTypeError("Cannot loop with element of type " + prettyPrintType(forLoopVariableType) + " with collection of type " + prettyPrintType(collectionType), forLoop);
            }
            if (!isDynamicTyped(forLoopVariableType)) {
                // user has specified a type, prefer it over the inferred type
                componentType = forLoopVariableType;
            }
            typeCheckingContext.controlStructureVariables.put(forLoop.getVariable(), componentType);
            try {
                forLoop.getLoopBlock().visit(this);
            } finally {
                typeCheckingContext.controlStructureVariables.remove(forLoop.getVariable());
            }
        }
        if (isSecondPassNeededForControlStructure(varTypes, oldTracker)) {
            visitForLoop(forLoop);
        }
    }

    /**
     * Returns the inferred loop element type given a loop collection type. Used,
     * for example, to infer the element type of a {@code for (e in list)} loop.
     *
     * @param collectionType the type of the collection
     * @return the inferred component type
     * @see #inferComponentType
     */
    public static ClassNode inferLoopElementType(final ClassNode collectionType) {
        ClassNode componentType;
        if (collectionType.isArray()) { // GROOVY-11335
            componentType = collectionType.getComponentType();

        } else if (isOrImplements(collectionType, ITERABLE_TYPE)) {
            ClassNode col = GenericsUtils.parameterizeType(collectionType, ITERABLE_TYPE);
            componentType = getCombinedBoundType(col.getGenericsTypes()[0]);

        } else if (isOrImplements(collectionType, MAP_TYPE)) { // GROOVY-6240
            ClassNode col = GenericsUtils.parameterizeType(collectionType, MAP_TYPE);
            componentType = makeClassSafe0(MAP_ENTRY_TYPE, col.getGenericsTypes());

        } else if (isOrImplements(collectionType, STREAM_TYPE)) { // GROOVY-10476
            ClassNode col = GenericsUtils.parameterizeType(collectionType, STREAM_TYPE);
            componentType = getCombinedBoundType(col.getGenericsTypes()[0]);

        } else if (isOrImplements(collectionType, Iterator_TYPE)) { // GROOVY-10712
            ClassNode col = GenericsUtils.parameterizeType(collectionType, Iterator_TYPE);
            componentType = getCombinedBoundType(col.getGenericsTypes()[0]);

        } else if (isOrImplements(collectionType, ENUMERATION_TYPE)) { // GROOVY-6123
            ClassNode col = GenericsUtils.parameterizeType(collectionType, ENUMERATION_TYPE);
            componentType = getCombinedBoundType(col.getGenericsTypes()[0]);

        } else if (isStringType(collectionType)) {
            componentType = STRING_TYPE;
        } else {
            componentType = OBJECT_TYPE;
        }
        return componentType;
    }

    protected boolean isSecondPassNeededForControlStructure(final Map<VariableExpression, ClassNode> startTypes, final Map<VariableExpression, List<ClassNode>> oldTracker) {
        for (Map.Entry<VariableExpression, ClassNode> entry : popAssignmentTracking(oldTracker).entrySet()) {
            Variable key = findTargetVariable(entry.getKey());
            if (startTypes.containsKey(key)
                    && !startTypes.get(key).equals(entry.getValue())) {
                return true;
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
        } else if (isStringType(typeRe) || isGStringType(typeRe)) {
            resultType = PATTERN_TYPE;
        } else if (typeRe.equals(ArrayList_TYPE)) {
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
        visitPrefixOrPostixExpression(expression, operand, operator);
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        Expression operand = expression.getExpression();
        int operator = expression.getOperation().getType();
        visitPrefixOrPostixExpression(expression, operand, operator);
    }

    private void visitPrefixOrPostixExpression(final Expression origin, final Expression operand, final int operator) {
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
        if (isPrimitiveByte(type) || isPrimitiveShort(type) || isPrimitiveInt(type)) {
            return int_TYPE;
        }
        if (isWrapperByte(type) || isWrapperShort(type) || isWrapperInteger(type)) {
            return Integer_TYPE;
        }
        if (isPrimitiveFloat(type)) {
            return double_TYPE;
        }
        if (isWrapperFloat(type)) {
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
        } else if (typeRe.equals(ArrayList_TYPE)) {
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
    public void visitExpressionStatement(final ExpressionStatement statement) {
        typeCheckingContext.pushTemporaryTypeInfo();
        super.visitExpressionStatement(statement);
        Map<?,List<ClassNode>> tti = typeCheckingContext.temporaryIfBranchTypeInformation.pop();
        if (!tti.isEmpty() && !typeCheckingContext.temporaryIfBranchTypeInformation.isEmpty()) {
            tti.forEach((k, tempTypes) -> {
                if (tempTypes.contains(VOID_TYPE))
                    typeCheckingContext.temporaryIfBranchTypeInformation.peek()
                        .computeIfAbsent(k, x -> new LinkedList<>()).add(VOID_TYPE);
            });
        }
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        if (typeCheckingContext.getEnclosingClosure() == null) {
            MethodNode method = typeCheckingContext.getEnclosingMethod();
            if (method != null && !method.isVoidMethod() && !method.isDynamicReturnType()) {
                applyTargetType(method.getReturnType(), statement.getExpression()); // GROOVY-10660
            }
        }
        super.visitReturnStatement(statement);
        returnListener.returnStatementAdded(statement);
    }

    protected ClassNode checkReturnType(final ReturnStatement statement) {
        Expression expression = statement.getExpression();
        ClassNode type = getType(expression);

        TypeCheckingContext.EnclosingClosure enclosingClosure = typeCheckingContext.getEnclosingClosure();
        if (enclosingClosure != null) {
            if (enclosingClosure.getClosureExpression().getNodeMetaData(INFERRED_TYPE) != null) return null; // re-visit

            ClassNode inferredReturnType = getInferredReturnType(enclosingClosure.getClosureExpression());
            // GROOVY-9995: return ctor call with diamond operator
            if (expression instanceof ConstructorCallExpression) {
                inferDiamondType((ConstructorCallExpression) expression, inferredReturnType != null ? inferredReturnType : /*GROOVY-10080:*/dynamicType());
            }
            if (inferredReturnType != null
                    && !inferredReturnType.equals(type)
                    && !isObjectType(inferredReturnType)
                    && !isPrimitiveVoid(inferredReturnType)
                    && !isPrimitiveBoolean(inferredReturnType)
                    && !GenericsUtils.hasUnresolvedGenerics(inferredReturnType)) {
                if (isStringType(inferredReturnType) && isGStringOrGStringStringLUB(type)) {
                    type = STRING_TYPE; // GROOVY-9971: convert GString to String at point of return
                } else if (GenericsUtils.buildWildcardType(wrapTypeIfNecessary(inferredReturnType)).isCompatibleWith(wrapTypeIfNecessary(type))) {
                    type = inferredReturnType; // GROOVY-8310, GROOVY-10082, GROOVY-10091, GROOVY-10128, GROOVY-10306: allow simple covariance
                } else if (!isPrimitiveVoid(type) && !extension.handleIncompatibleReturnType(statement, type)) { // GROOVY-10277: incompatible return value
                    addStaticTypeError("Cannot return value of type " + prettyPrintType(type) + " for " + (enclosingClosure.getClosureExpression() instanceof LambdaExpression ? "lambda" : "closure") + " expecting " + prettyPrintType(inferredReturnType), expression);
                }
            }
            return type;
        }

        MethodNode enclosingMethod = typeCheckingContext.getEnclosingMethod();
        if (enclosingMethod != null && !enclosingMethod.isVoidMethod() && !enclosingMethod.isDynamicReturnType()) {
            ClassNode returnType = enclosingMethod.getReturnType();
            if (!isPrimitiveVoid(getUnwrapper(type)) && !checkCompatibleAssignmentTypes(returnType, type, null, false)) {
                if (!extension.handleIncompatibleReturnType(statement, type)) {
                    addStaticTypeError("Cannot return value of type " + prettyPrintType(type) + " for method returning " + prettyPrintType(returnType), expression);
                }
            } else if (implementsInterfaceOrIsSubclassOf(type, returnType)) {
                BinaryExpression dummy = assignX(varX("{target}", returnType), expression, statement);
                ClassNode resultType = getResultType(returnType, ASSIGN, type, dummy); // GROOVY-10295
                checkTypeGenerics(returnType, resultType, expression);
            }
        }
        return null;
    }

    protected void addClosureReturnType(final ClassNode returnType) {
        if (returnType != null && !isPrimitiveVoid(returnType)) // GROOVY-8202
            typeCheckingContext.getEnclosingClosure().addReturnType(returnType);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        if (!extension.beforeMethodCall(call)) {
            ClassNode receiver;
            if (call.isThisCall()) {
                receiver = makeThis();
            } else if (call.isSuperCall()) {
                receiver = makeSuper();
            } else {
                receiver = call.getType();
            }
            Expression arguments = call.getArguments();
            ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(arguments);

            // visit functional arguments *after* method has been chosen
            visitMethodCallArguments(receiver, argumentList, false, null);
            final ClassNode[] argumentTypes = getArgumentTypes(argumentList);

            MethodNode ctor;
            if (looksLikeNamedArgConstructor(receiver, argumentTypes)
                    && findMethod(receiver, "<init>", argumentTypes).isEmpty()
                    && findMethod(receiver, "<init>", init(argumentTypes)).size() == 1) {
                ctor = typeCheckMapConstructor(call, receiver, arguments);
            } else {
                ctor = findMethodOrFail(call, receiver, "<init>", argumentTypes);
                visitMethodCallArguments(receiver, argumentList, true, ctor);
                if (ctor != null) {
                    Parameter[] parameters = ctor.getParameters();
                    GenericsType[] typeParameters = ctor.getDeclaringClass().getGenericsTypes();
                    if (typeParameters != null) { // GROOVY-10283, GROOVY-10316, GROOVY-10482, GROOVY-10624, GROOVY-10698
                        Map<GenericsTypeName, GenericsType> context = extractGenericsConnectionsFromArguments(typeParameters, parameters, argumentList, receiver.getGenericsTypes());
                        if (!context.isEmpty()) parameters = Arrays.stream(parameters).map(p -> new Parameter(applyGenericsContext(context, p.getType()), p.getName())).toArray(Parameter[]::new);
                    }
                    resolvePlaceholdersFromImplicitTypeHints(argumentTypes, argumentList, parameters);
                    typeCheckMethodsWithGenericsOrFail(receiver, argumentTypes, ctor, call);
                    checkForbiddenSpreadArgument(argumentList, parameters);
                } else {
                    checkForbiddenSpreadArgument(argumentList);
                }
            }
            if (ctor != null) storeTargetMethod(call, ctor);
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
            return isOrImplements(argumentTypes[argumentTypes.length - 1], MAP_TYPE);
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
                    node.setSynthetic(true);
                }
            }
        }
        return node;
    }

    protected ClassNode[] getArgumentTypes(final ArgumentListExpression argumentList) {
        return argumentList.getExpressions().stream().map(exp ->
            isNullConstant(exp) ? UNKNOWN_PARAMETER_TYPE : getType(exp)
        ).toArray(ClassNode[]::new);
    }

    private Map<VariableExpression, ClassNode> scanVars(final ClosureExpression expr) {
        Map<VariableExpression, ClassNode> varTypes = new HashMap<>();

        GroovyCodeVisitor visitor = new VariableExpressionTypeMemoizer(varTypes, true);
        if (expr.isParameterSpecified()) {
            for (Parameter p : expr.getParameters()) {
                if (p.hasInitialExpression()) {
                    p.getInitialExpression().visit(visitor);
                }
            }
        }
        expr.getCode().visit(visitor);

        return varTypes;
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        Map<VariableExpression, ClassNode> varTypes = scanVars(expression);
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        SharedVariableCollector collector = new SharedVariableCollector(getSourceUnit());
        expression.visit(collector); // collect every variable expression the closure references
        Set<VariableExpression> closureSharedVariables = collector.getClosureSharedExpressions();

        Map<VariableExpression, Map<StaticTypesMarker, Object>> variableMetadata = new HashMap<>();
        if (!closureSharedVariables.isEmpty()) {
            // GROOVY-6921: do getType in order to update closure shared variables
            // whose types are inferred thanks to closure parameter type inference
            for (VariableExpression vexp : closureSharedVariables) getType(vexp);
            saveVariableExpressionMetadata(closureSharedVariables, variableMetadata);
        }

        // perform visit
        typeCheckingContext.pushEnclosingClosureExpression(expression);
        DelegationMetadata dmd = getDelegationMetadata(expression);
        if (dmd != null) {
            typeCheckingContext.delegationMetadata = newDelegationMetadata(dmd.getType(), dmd.getStrategy());
        } else {
            typeCheckingContext.delegationMetadata = newDelegationMetadata(typeCheckingContext.getEnclosingClassNode(), Closure.OWNER_FIRST);
        }
        expression.getCode().visit(this);
        typeCheckingContext.delegationMetadata = typeCheckingContext.delegationMetadata.getParent();

        returnAdder.visitMethod(new MethodNode("dummy", 0, OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, expression.getCode()));
        TypeCheckingContext.EnclosingClosure enclosingClosure = typeCheckingContext.popEnclosingClosure();
        if (!enclosingClosure.getReturnTypes().isEmpty()) { // populated by ReturnAdder
            ClassNode returnType = lowestUpperBound(enclosingClosure.getReturnTypes());
            storeInferredReturnType(expression, wrapTypeIfNecessary(returnType));
            storeType(expression, wrapClosureType(returnType));
        }

        // check types of closure shared variables for change
        if (isSecondPassNeededForControlStructure(varTypes, oldTracker)) {
            visitClosureExpression(expression);
        }

        // restore original metadata
        restoreVariableExpressionMetadata(variableMetadata);
        for (Parameter parameter : getParametersSafe(expression)) {
            typeCheckingContext.controlStructureVariables.remove(parameter);
            // GROOVY-10072: visit param default argument expression if present
            visitInitialExpression(parameter.getInitialExpression(), varX(parameter), parameter);
        }
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        super.visitMethodPointerExpression(expression);
        Expression nameExpr = expression.getMethodName();
        if (nameExpr instanceof ConstantExpression
                && isStringType(getType(nameExpr))) {
            String nameText = nameExpr.getText();

            if ("new".equals(nameText)) {
                ClassNode type = getType(expression.getExpression());
                if (isClassClassNodeWrappingConcreteType(type)){
                    type = type.getGenericsTypes()[0].getType();
                    storeType(expression,wrapClosureType(type));
                    // GROOVY-11385: check if create possible
                    if (type.isAbstract() && !type.isArray())
                        addStaticTypeError("Cannot instantiate the type " +
                                    prettyPrintTypeName(type), expression);
                    // GROOVY-10930: check constructor reference
                    ClassNode[] signature = expression.getNodeMetaData(CLOSURE_ARGUMENTS);
                    if (signature != null) { Expression[] mocks = Arrays.stream(signature)
                            .map(t -> typedValueExpression(t)).toArray(Expression[]::new);
                        Expression dummy = ctorX(type, args(mocks));
                        dummy.setSourcePosition(expression);
                        dummy.visit(this);
                    }
                }
                return;
            }

            List<Receiver<String>> receivers = new ArrayList<>();
            addReceivers(receivers, makeOwnerList(expression.getExpression()), false);

            ClassNode receiverType = null;
            List<MethodNode> candidates = EMPTY_METHODNODE_LIST;
            for (Receiver<String> currentReceiver : receivers) {
                receiverType = wrapTypeIfNecessary(currentReceiver.getType());

                candidates = findMethodsWithGenerated(receiverType, nameText);
                // GROOVY-10741: check for reference to a property node's method
                MethodNode generated = findPropertyMethod(receiverType, nameText);
                if (generated != null && candidates.stream().noneMatch(m -> m.getName().equals(generated.getName()))) {
                    candidates.add(generated);
                }
                candidates.addAll(findDGMMethodsForClassNode(getSourceUnit().getClassLoader(), receiverType, nameText));

                if (candidates.size() > 1) {
                    candidates = filterMethodCandidates(candidates, expression.getExpression(), expression.getNodeMetaData(CLOSURE_ARGUMENTS));
                }
                if (!candidates.isEmpty()) {
                    break;
                }
            }

            if (candidates.isEmpty()) {
                candidates = extension.handleMissingMethod(
                    getType(expression.getExpression()), nameText, null, null, null);
            } else if (candidates.size() > 1) {
                candidates = extension.handleAmbiguousMethods(candidates, expression);
            }

            if (!candidates.isEmpty()) {
                ClassNode[] arguments = expression.getNodeMetaData(CLOSURE_ARGUMENTS);
                if (asBoolean(arguments) && asBoolean(receiverType.redirect().getGenericsTypes())
                                         && expression.getExpression() instanceof ClassExpression) {
                    receiverType = GenericsUtils.parameterizeType(arguments[0], receiverType); // GROOVY-11241
                }

                ClassNode ownerType = receiverType;
                candidates.stream()
                        .map(candidate -> {
                            ClassNode returnType = candidate.getReturnType();
                            if (!candidate.isStatic() && GenericsUtils.hasUnresolvedGenerics(returnType)) {
                                Map<GenericsTypeName, GenericsType> spec = new HashMap<>(); // GROOVY-11364
                                extractGenericsConnections(spec, ownerType, candidate.getDeclaringClass());
                                returnType = applyGenericsContext(spec, returnType);
                            }
                            return returnType;
                        })
                        .reduce(WideningCategories::lowestUpperBound).ifPresent(returnType -> {
                            ClassNode closureType = wrapClosureType(returnType);
                            storeType(expression, closureType);
                            // GROOVY-10858: check method return type
                            ClassNode targetType = expression.getNodeMetaData(PARAMETER_TYPE);
                            if (!returnType.isGenericsPlaceHolder() && isSAMType(targetType)) {
                                targetType = GenericsUtils.parameterizeSAM(targetType).getV2();
                                if (!isPrimitiveVoid(targetType) && !checkCompatibleAssignmentTypes(targetType, returnType, null, false)) // TODO: ext.handleIncompatibleReturnType
                                    addStaticTypeError("Invalid return type: " + prettyPrintType(returnType) + " is not convertible to " + prettyPrintType(targetType), expression);
                            }
                        });
                expression.putNodeMetaData(MethodNode.class, candidates);

                if (asBoolean(arguments)) {
                    ClassNode[] parameters = collateMethodReferenceParameterTypes(expression, candidates.get(0));
                    for (int i = 0; i < arguments.length; i += 1) {
                        ClassNode at = arguments[i];
                        ClassNode pt = parameters[Math.min(i, parameters.length - 1)];
                        if (!pt.equals(at) && (at.isInterface() ? pt.implementsInterface(at) : pt.isDerivedFrom(at)))
                            arguments[i] = pt; // GROOVY-10734, GROOVY-10807, GROPOVY-11026: expected type is refined
                    }
                }
            } else if (!(expression instanceof MethodReferenceExpression)
                    || this.getClass() == StaticTypeCheckingVisitor.class) {
                ClassNode type = wrapTypeIfNecessary(getType(expression.getExpression()));
                if (isClassClassNodeWrappingConcreteType(type)) type = type.getGenericsTypes()[0].getType();
                addStaticTypeError("Cannot find matching method " + prettyPrintTypeName(type) + "#" + nameText + ". Please check if the declared type is correct and if the method exists.", nameExpr);
            }
        }
    }

    private static Expression typedValueExpression(final ClassNode type) {
        if (isObjectType(type) && !type.isGenericsPlaceHolder()) {
            return nullX(); // GROOVY-10971: unknown argument type
        }
        return castX(type, defaultValueX(type));
    }

    private static ClassNode wrapClosureType(final ClassNode returnType) {
        return makeClassSafe0(CLOSURE_TYPE, wrapTypeIfNecessary(returnType).asGenericsType());
    }

    private static MethodNode findPropertyMethod(final ClassNode type, final String name) {
        for (ClassNode cn = type; cn != null; cn = cn.getSuperClass()) {
            for (PropertyNode pn : cn.getProperties()) {
                if (name.equals(pn.getGetterNameOrDefault())) {
                    MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC | (pn.isStatic() ? Opcodes.ACC_STATIC : 0), pn.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
                    node.setDeclaringClass(pn.getDeclaringClass());
                    node.setSynthetic(true);
                    return node;
                } else if (name.equals(pn.getSetterNameOrDefault()) && !Modifier.isFinal(pn.getModifiers())) {
                    MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC | (pn.isStatic() ? Opcodes.ACC_STATIC : 0), VOID_TYPE, new Parameter[]{new Parameter(pn.getType(), pn.getName())}, ClassNode.EMPTY_ARRAY, null);
                    node.setDeclaringClass(pn.getDeclaringClass());
                    node.setSynthetic(true);
                    return node;
                }
            }
        }
        return null;
    }

    private List<MethodNode> filterMethodCandidates(final List<MethodNode> candidates, final Expression objectOrType, /*@Nullable*/ ClassNode[] signature) {
        List<MethodNode> result = filterMethodsByVisibility(candidates, typeCheckingContext.getEnclosingClassNode());
        // assignment or parameter target type may help reduce the list
        if (result.size() > 1 && signature != null) {
            ClassNode type = getType(objectOrType);
            if (!isClassClassNodeWrappingConcreteType(type)) {
                result = chooseBestMethod(type, result, signature);
            } else {
                type = type.getGenericsTypes()[0].getType(); // Class<Type> --> Type
                Map<Boolean, List<MethodNode>> staticAndNonStatic = result.stream().collect(Collectors.partitioningBy(this::isStaticInContext));

                result = new ArrayList<>(result.size());
                result.addAll(chooseBestMethod(type, staticAndNonStatic.get(Boolean.TRUE), signature));
                if (result.isEmpty() && !staticAndNonStatic.get(Boolean.FALSE).isEmpty()) { // GROOVY-11009
                    if (signature.length > 0) signature= Arrays.copyOfRange(signature, 1, signature.length);
                    result.addAll(chooseBestMethod(type, staticAndNonStatic.get(Boolean.FALSE), signature));
                }
            }
        }
        return result;
    }

    protected DelegationMetadata getDelegationMetadata(final ClosureExpression expression) {
        return expression.getNodeMetaData(DELEGATION_METADATA);
    }

    private DelegationMetadata newDelegationMetadata(final ClassNode delegateType, final int resolveStrategy) {
        return new DelegationMetadata(delegateType, resolveStrategy, typeCheckingContext.delegationMetadata);
    }

    protected void restoreVariableExpressionMetadata(final Map<VariableExpression, Map<StaticTypesMarker, Object>> typesBeforeVisit) {
        if (typesBeforeVisit != null) {
            typesBeforeVisit.forEach((var, map) -> {
                for (StaticTypesMarker marker : StaticTypesMarker.values()) {
                    // GROOVY-9344, GROOVY-9516: keep type
                    if (marker == INFERRED_TYPE) continue;

                    Object value = map.get(marker);
                    if (value == null) {
                        var.removeNodeMetaData(marker);
                    } else {
                        var.putNodeMetaData(marker, value);
                    }
                }
                var.removeNodeMetaData(DECLARATION_INFERRED_TYPE); // GROOVY-8946
            });
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

    @Override
    public void visitConstructor(final ConstructorNode node) {
        if (shouldSkipMethodNode(node)) {
            return;
        }
        super.visitConstructor(node);
    }

    @Override
    public void visitMethod(final MethodNode node) {
        if (shouldSkipMethodNode(node)) {
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

    protected void startMethodInference(final MethodNode node, final ErrorCollector collector) {
        // second, we must ensure that this method MUST be statically checked
        // for example, in a mixed mode where only some methods are statically checked
        // we must not visit a method which used dynamic dispatch.
        // We do not check for an annotation because some other AST transformations
        // may use this visitor without the annotation being explicitly set
        if ((typeCheckingContext.methodsToBeVisited.isEmpty() || typeCheckingContext.methodsToBeVisited.contains(node))
                && typeCheckingContext.alreadyVisitedMethods.add(node)) { // prevent re-visiting method (infinite loop)
            typeCheckingContext.pushErrorCollector(collector);
            boolean osc = typeCheckingContext.isInStaticContext;
            try {
                // GROOVY-7890: non-static trait method is static in helper type
                typeCheckingContext.isInStaticContext = isNonStaticHelperMethod(node) ? false : node.isStatic();

                super.visitMethod(node);
            } finally {
                typeCheckingContext.isInStaticContext = osc;
            }
            typeCheckingContext.popErrorCollector();
            node.putNodeMetaData(ERROR_COLLECTOR, collector);
        }
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        typeCheckingContext.pushEnclosingMethod(node);
        final ClassNode returnType = node.getReturnType(); // GROOVY-10660: implicit return case
        if (!isConstructor && (isClosureWithType(returnType) || isFunctionalInterface(returnType))) {
            new ReturnAdder(returnStmt -> applyTargetType(returnType, returnStmt.getExpression())).visitMethod(node);
        }
        readClosureParameterAnnotation(node); // GROOVY-6603
        doWithTypeCheckingExtensions(node, it -> super.visitConstructorOrMethod(it, isConstructor));
        if (node.hasDefaultValue()) {
            visitDefaultParameterArguments(node.getParameters());
        }
        if (!isConstructor) {
            returnAdder.visitMethod(node); // GROOVY-7753: we cannot count these auto-generated return statements, see `typeCheckingContext.pushEnclosingReturnStatement`
        }
        typeCheckingContext.popEnclosingMethod();
    }

    private void readClosureParameterAnnotation(final MethodNode node) {
        for (Parameter parameter : node.getParameters()) {
            for (AnnotationNode annotation : parameter.getAnnotations()) {
                if (annotation.getClassNode().equals(CLOSUREPARAMS_CLASSNODE)) {
                    // GROOVY-6603: propagate closure parameter types
                    Expression value = annotation.getMember("value");
                    Expression options = annotation.getMember("options");
                    List<ClassNode[]> signatures = getSignaturesFromHint(node, value, options, annotation);
                    if (signatures.size() == 1) { // TODO: handle multiple signatures
                        parameter.putNodeMetaData(CLOSURE_ARGUMENTS, Arrays.stream(signatures.get(0)).map(t -> new Parameter(t,"")).toArray(Parameter[]::new));
                    }
                }
            }
        }
    }

    private void visitDefaultParameterArguments(final Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (!parameter.hasInitialExpression()) continue;
            // GROOVY-10094: visit param default argument expression
            visitInitialExpression(parameter.getInitialExpression(), varX(parameter), parameter);
            // GROOVY-10104: remove direct target setting to prevent errors
            parameter.getInitialExpression().visit(new CodeVisitorSupport() {
                @Override
                public void visitMethodCallExpression(final MethodCallExpression mce) {
                    super.visitMethodCallExpression(mce);
                    mce.setMethodTarget(null);
                }
            });
        }
    }

    @Override
    protected void visitObjectInitializerStatements(final ClassNode node) {
        // GROOVY-5450: create fake constructor node so final field analysis can allow write within non-static initializer block(s)
        ConstructorNode init = new ConstructorNode(0, null, null, new BlockStatement(node.getObjectInitializerStatements(), null));
        typeCheckingContext.pushEnclosingMethod(init);
        super.visitObjectInitializerStatements(node);
        typeCheckingContext.popEnclosingMethod();
    }

    protected void addTypeCheckingInfoAnnotation(final MethodNode node) {
        // TypeChecked$TypeCheckingInfo cannot be applied on constructors
        if (node.isConstructor()) return;

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
        ClassNode type = call.getOwnerType();
        if (type.isEnum() && name.equals("$INIT")) { // GROOVY-10845: $INIT(Object[]) delegates to constructor
            Expression target = typeCheckingContext.getEnclosingBinaryExpression().getLeftExpression();
            ConstructorCallExpression cce = new ConstructorCallExpression(type, call.getArguments());
            cce.setSourcePosition(((FieldExpression) target).getField());
            visitConstructorCallExpression(cce);

            MethodNode init = type.getDeclaredMethods("$INIT").get(0);
            call.putNodeMetaData(INFERRED_TYPE, init.getReturnType());
            call.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, init);
            return;
        }
        if (extension.beforeMethodCall(call)) {
            extension.afterMethodCall(call);
            return;
        }
        try {
            ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(call.getArguments());

            boolean functorsVisited = false; // visit *after* method selection
            visitMethodCallArguments(type, argumentList, functorsVisited, null);

            {
                ClassNode[] args = getArgumentTypes(argumentList);
                List<MethodNode> opts = findMethod(type, name, args);
                if (opts.isEmpty()) {
                    opts = extension.handleMissingMethod(type, name, argumentList, args, call);
                }
                if (opts.isEmpty()) {
                    addNoMatchingMethodError(type, name, args, call);
                } else {
                    opts = disambiguateMethods(opts, type, args, call);
                    if (opts.size() != 1) {
                        addAmbiguousErrorMessage(opts, name, args, call);
                    } else {
                        MethodNode targetMethod = opts.get(0);
                        storeTargetMethod(call, targetMethod); // trigger onMethodSelection
                        if (call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET) == targetMethod) {
                            // GROOVY-8909, GROOVY-8961, GROOVY-9734, GROOVY-9844, GROOVY-9915, et al.
                            resolvePlaceholdersFromImplicitTypeHints(args, argumentList, targetMethod.getParameters());
                            typeCheckMethodsWithGenericsOrFail(type, args, targetMethod, call);

                            ClassNode returnType = getType(targetMethod);
                            if (isUsingGenericsOrIsArrayUsingGenerics(returnType)) {
                                functorsVisited = true; // visit functional argument(s) with selected method
                                visitMethodCallArguments(type, argumentList, functorsVisited, targetMethod);
                                ClassNode rt = inferReturnTypeGenerics(type, targetMethod, argumentList);
                                if (rt != null && implementsInterfaceOrIsSubclassOf(rt, returnType)) {
                                    returnType = rt;
                                }
                            }
                            storeType(call, returnType);
                        }
                    }
                }
            }

            // type-checking extension may have changed or cleared target method
            MethodNode target = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (!functorsVisited) {
                visitMethodCallArguments(type, argumentList, true, target);
            }
            if (target != null) { Parameter[] params = target.getParameters();
                checkClosureMetadata(argumentList.getExpressions(), params);
                checkForbiddenSpreadArgument(argumentList, params);
            } else {
                checkForbiddenSpreadArgument(argumentList);
            }
        } finally {
            extension.afterMethodCall(call);
        }
    }

    /**
     * @deprecated this method is unused, replaced with {@link DelegatesTo} inference.
     */
    @Deprecated(forRemoval = true, since = "2.5.0")
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
                    addStaticTypeError("Expected parameter type: " + prettyPrintType(receiver) + " but was: " + prettyPrintType(param.getType()), param);
                }
            }
            closure.putNodeMetaData(DELEGATION_METADATA, newDelegationMetadata(receiver, Closure.DELEGATE_FIRST));
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

    protected void visitMethodCallArguments(final ClassNode receiver, final ArgumentListExpression arguments, final boolean visitFunctors, final MethodNode selectedMethod) {
        Parameter[] parameters;
        List<Expression> expressions = new ArrayList<>();
        if (selectedMethod instanceof ExtensionMethodNode) {
            parameters = ((ExtensionMethodNode) selectedMethod).getExtensionMethodNode().getParameters();
            expressions.add(varX("$self", receiver));
        } else {
            parameters = selectedMethod != null ? selectedMethod.getParameters() : Parameter.EMPTY_ARRAY;
        }
        expressions.addAll(arguments.getExpressions());

        int nExpressions = expressions.size();
        int nthParameter = parameters.length - 1;
        for (int i = 0; i < nExpressions; i += 1) {
            Expression expression = expressions.get(i);
            if (visitFunctors == (expression instanceof ClosureExpression || expression instanceof MethodPointerExpression)) {
                if (visitFunctors && nthParameter != -1) { // GROOVY-10636: vargs call
                    Parameter target = parameters[Math.min(i, nthParameter)];
                    ClassNode targetType = target.getType();
                    if (targetType.isArray() && i >= nthParameter)
                        targetType = targetType.getComponentType();
                    boolean coerceToMethod = isSAMType(targetType);
                    if (coerceToMethod) { // resolve the target parameter's type
                        Map<GenericsTypeName, GenericsType> context = extractPlaceHoldersVisibleToDeclaration(receiver, selectedMethod, arguments);
                        targetType = applyGenericsContext(context, targetType);
                        expression.putNodeMetaData(PARAMETER_TYPE, targetType);
                    }
                    if (expression instanceof ClosureExpression) {
                        checkClosureWithDelegatesTo(receiver, selectedMethod, args(expressions), parameters, expression, target);
                        if (i > 0 || !(selectedMethod instanceof ExtensionMethodNode)) {
                            inferClosureParameterTypes(receiver, arguments, (ClosureExpression) expression, target, selectedMethod);
                        }
                        if (coerceToMethod && targetType.isInterface()) { // @FunctionalInterface
                            storeInferredReturnType(expression, GenericsUtils.parameterizeSAM(targetType).getV2());
                        } else if (isClosureWithType(targetType)) {
                            storeInferredReturnType(expression, getCombinedBoundType(targetType.getGenericsTypes()[0]));
                        }
                    } else if (expression instanceof MethodReferenceExpression) {
                        if (coerceToMethod && targetType.isInterface()) { // @FunctionalInterface
                            LambdaExpression lambda = constructLambdaExpressionForMethodReference(
                                                        targetType, (MethodReferenceExpression) expression);
                            inferClosureParameterTypes(receiver, arguments, lambda, target, selectedMethod);
                            expression.putNodeMetaData(PARAMETER_TYPE, lambda.getNodeMetaData(PARAMETER_TYPE));
                            expression.putNodeMetaData(CLOSURE_ARGUMENTS, lambda.getNodeMetaData(CLOSURE_ARGUMENTS));
                        } else {
                            addError("The argument is a method reference, but the parameter type is not a functional interface", expression);
                        }
                    }
                }
                expression.visit(this);
                expression.removeNodeMetaData(DELEGATION_METADATA);
            }
            if (i == 0 && parameters.length > 0 && expression instanceof MapExpression) {
                checkNamedParamsAnnotation(parameters[0], (MapExpression) expression);
            }
        }
    }

    private static LambdaExpression constructLambdaExpressionForMethodReference(final ClassNode functionalInterface, final MethodReferenceExpression methodReference) {
        Parameter[] parameters = findSAM(functionalInterface).getParameters();
        int nParameters = parameters.length;
        if (nParameters > 0) {
            parameters = new Parameter[nParameters];
            for (int i = 0; i < nParameters; i += 1)
                parameters[i] = new Parameter(dynamicType(), "p" + i);
        }
        return new LambdaExpression(parameters, GENERATED_EMPTY_STATEMENT);
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
        List<String> collectedNames = new ArrayList<>();
        List<AnnotationNode> annotations = param.getAnnotations(NAMED_PARAMS_CLASSNODE);
        if (annotations != null && !annotations.isEmpty()) {
            AnnotationNode an = null;
            for (AnnotationNode next : annotations) {
                if (next.getClassNode().getName().equals(NamedParams.class.getName())) {
                    an = next;
                }
            }
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
            }
        }
        annotations = param.getAnnotations(NAMED_PARAM_CLASSNODE);
        if (annotations != null && !annotations.isEmpty()) {
            for (AnnotationNode next : annotations) {
                if (next.getClassNode().getName().equals(NamedParam.class.getName())) {
                    processNamedParam(next, entries, args, collectedNames);
                }
            }
        }
        if (!collectedNames.isEmpty()) {
            for (Map.Entry<Object, Expression> entry : entries.entrySet()) {
                if (!collectedNames.contains(entry.getKey())) {
                    addStaticTypeError("unexpected named arg: " + entry.getKey(), args);
                }
            }
        }
    }

    private void processNamedParam(final AnnotationConstantExpression value, final Map<Object, Expression> entries, final Expression expression, final List<String> collectedNames) {
        AnnotationNode namedParam = (AnnotationNode) value.getValue();
        if (!namedParam.getClassNode().getName().equals(NamedParam.class.getName())) return;
        processNamedParam(namedParam, entries, expression, collectedNames);
    }

    private void processNamedParam(final AnnotationNode namedParam, final Map<Object, Expression> entries, final Expression expression, final List<String> collectedNames) {
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
                addStaticTypeError("required named param '" + name + "' not found.", expression);
            }
        } else if (expectedType != null) {
            ClassNode argumentType = getDeclaredOrInferredType(entries.get(name));
            if (!isAssignableTo(argumentType, expectedType)) {
                addStaticTypeError("argument for named param '" + name + "' has type '" + prettyPrintType(argumentType) + "' but expected '" + prettyPrintType(expectedType) + "'.", expression);
            }
        }
    }

    /**
     * Performs type inference on closure argument types whenever code like this
     * is found: <code>foo.collect { it.toUpperCase() }</code>.
     * <p>
     * In this case the type checker tries to find if the {@code collect} method
     * has its {@link Closure} argument annotated with {@link ClosureParams}. If
     * so, then additional type inference can be performed and the type of
     * {@code it} may be inferred.
     *
     * @param receiver
     * @param arguments
     * @param expression closure or lambda expression for which the argument types should be inferred
     * @param target     parameter which may provide {@link ClosureParams} annotation or SAM type
     * @param method     method that declares {@code target}
     */
    protected void inferClosureParameterTypes(final ClassNode receiver, final Expression arguments, final ClosureExpression expression, final Parameter target, final MethodNode method) {
        List<AnnotationNode> annotations = target.getAnnotations(CLOSUREPARAMS_CLASSNODE);
        if (annotations != null && !annotations.isEmpty()) {
            for (AnnotationNode annotation : annotations) {
                Expression value = annotation.getMember("value");
                Expression options = annotation.getMember("options");
                Expression conflictResolver = annotation.getMember("conflictResolutionStrategy");
                processClosureParams(receiver, arguments, expression, method, value, conflictResolver, options);
            }
        } else if (isSAMType(target.getOriginType())) { // SAM-type coercion
            boolean isConstructor = method.isConstructor();
            boolean hasTypeArguments = asBoolean(receiver.getGenericsTypes());
            Map<GenericsTypeName, GenericsType> context = isConstructor && !hasTypeArguments ? new HashMap<>() // GROOVY-10699
                                                        : extractPlaceHoldersVisibleToDeclaration(receiver, method, arguments);
            GenericsType[] typeParameters = isConstructor ? method.getDeclaringClass().getGenericsTypes() : applyGenericsContext(context, method.getGenericsTypes());

            if (typeParameters != null) {
                boolean typeParametersResolved = false;
                // first check for explicit type arguments
                if (isConstructor) {
                    typeParametersResolved = hasTypeArguments;
                } else {
                    Expression emc = typeCheckingContext.getEnclosingMethodCall();
                    if (emc instanceof MethodCallExpression) {
                        MethodCallExpression mce = (MethodCallExpression) emc;
                        if (mce.getArguments() == arguments) {
                            GenericsType[] typeArguments = mce.getGenericsTypes();
                            if (typeArguments != null) {
                                int n = typeParameters.length;
                                if (n == typeArguments.length) {
                                    typeParametersResolved = true;
                                    for (int i = 0; i < n; i += 1) {
                                        context.put(new GenericsTypeName(typeParameters[i].getName()), typeArguments[i]);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!typeParametersResolved) {
                    // check for implicit type arguments
                    int i = -1; Parameter[] p = method.getParameters();
                    for (Expression argument : (ArgumentListExpression) arguments) { i += 1;
                        if (isNullConstant(argument)) continue;

                        ClassNode pType = p[Math.min(i, p.length - 1)].getType();
                        Map<GenericsTypeName, GenericsType> gc = new HashMap<>();
                        extractGenericsConnections(gc, wrapTypeIfNecessary(getType(argument)), pType);
                        // GROOVY-10436: extract generics connections from closure parameter declaration(s)
                        if (argument == expression || (argument instanceof ClosureExpression && isSAMType(pType))) {
                            Parameter[] q = getParametersSafe((ClosureExpression) argument);
                            ClassNode[] r = extractTypesFromParameters(q); // maybe typed
                            ClassNode[] s = GenericsUtils.parameterizeSAM(pType).getV1();
                            for (int j = 0; j < r.length && j < s.length; j += 1)
                                if (!q[j].isDynamicTyped()) extractGenericsConnections(gc, r[j], s[j]);
                        }

                        gc.forEach((key, gt) -> {
                            for (GenericsType tp : typeParameters) {
                                if (tp.getName().equals(key.getName())) {
                                    context.putIfAbsent(key, gt); // TODO: merge
                                    break;
                                }
                            }
                        });
                    }

                    for (GenericsType tp : typeParameters) {
                        GenericsTypeName name = new GenericsTypeName(tp.getName());
                        context.computeIfAbsent(name, x -> fullyResolve(tp, context));
                    }
                }
            }

            ClassNode[] paramTypes = expression.getNodeMetaData(CLOSURE_ARGUMENTS);
            if (paramTypes == null) {
                ClassNode targetType = target.getType();
                if (targetType != null && targetType.isGenericsPlaceHolder())
                    targetType = getCombinedBoundType(targetType.asGenericsType());
                targetType = applyGenericsContext(context, targetType); // fill "T"
                inferParameterAndReturnTypesOfClosureOnRHS(targetType, expression);
            }
        }
    }

    private void processClosureParams(final ClassNode receiver, final Expression arguments, final ClosureExpression expression, final MethodNode selectedMethod, final Expression hintClass, final Expression resolverClass, final Expression options) {
        Parameter[] parameters = hasImplicitParameter(expression) ? new Parameter[]{new Parameter(dynamicType(),"it")} : getParametersSafe(expression);

        List<ClassNode[]> closureSignatures = new LinkedList<>(getSignaturesFromHint(selectedMethod, hintClass, options, expression));
        List<ClassNode[]> candidates = new LinkedList<>();
        for (var it = closureSignatures.listIterator(); it.hasNext();) { ClassNode[] signature = it.next();
            resolveGenericsFromTypeHint(receiver, arguments, selectedMethod, signature);
            if (parameters.length == signature.length) {
                candidates.add(signature);
            }
            if ((parameters.length != 1 || !isObjectType(parameters[0].getOriginType()))
                    && signature.length == 1 && isOrImplements(signature[0], LIST_TYPE)) {
                // list element(s) spread across closure parameter(s) : ClosureMetaClass
                int itemCount = TUPLE_TYPES.indexOf(signature[0]);
                if (itemCount >= 0) { // GROOVY-11090: Tuple[0-16]
                    if (itemCount != parameters.length) {
                        // for param count error messages
                        it.add(new ClassNode[itemCount]);
                        continue;
                    }
                    GenericsType[] spec = signature[0].getGenericsTypes();
                    if (spec != null) { // edge case: Tuple0 falls through
                        signature = Arrays.stream(spec).map(GenericsType::getType).toArray(ClassNode[]::new);
                        candidates.add(signature);
                        continue;
                    }
                }
                ClassNode itemType = inferLoopElementType(signature[0]);
                signature = new ClassNode[parameters.length];
                Arrays.fill(signature, itemType);
                candidates.add(signature);
            }
        }

        if (candidates.isEmpty() && !closureSignatures.isEmpty()) {
            String spec = closureSignatures.stream().mapToInt(sig -> sig.length).distinct()
                                           .sorted().mapToObj(Integer::toString).collect(Collectors.joining(" or "));
            addError("Incorrect number of parameters. Expected " + spec + " but found " + parameters.length, expression);
        }

        if (candidates.size() > 1) {
            closureSignatures = new ArrayList<>(candidates);
            for (Iterator<ClassNode[]> candIt = candidates.iterator(); candIt.hasNext(); ) {
                ClassNode[] inferredTypes = candIt.next();
                for (int i = 0; i < inferredTypes.length; i += 1) {
                    ClassNode inferredType = inferredTypes[i], parameterType = parameters[i].getOriginType();
                    if (parameterType.getGenericsTypes() != null) parameterType = GenericsUtils.nonGeneric(parameterType);
                    if (!typeCheckMethodArgumentWithGenerics(parameterType, inferredType, false)) { candIt.remove(); break; }
                }
            }
            if (candidates.size() > 1 && resolverClass instanceof ClassExpression) {
                candidates = resolveWithResolver(candidates, receiver, arguments, expression, selectedMethod, resolverClass, options);
            }
            if (candidates.isEmpty()) {
                String actual = toMethodParametersString("", extractTypesFromParameters(parameters));
                String expect = closureSignatures.stream().map(sig -> toMethodParametersString("",sig)).collect(Collectors.joining(" or "));
                addStaticTypeError("Incorrect parameter type(s). Expected " + expect + " but found " + actual, expression); // at least one fails
            } else if (candidates.size() > 1) {
                addError("Ambiguous prototypes for closure. More than one target method matches. Please use explicit argument types.", expression);
            }
        }

        if (candidates.size() == 1) {
            ClassNode[] inferredTypes = candidates.get(0);
            if (inferredTypes.length == 1 && hasImplicitParameter(expression)) {
                expression.putNodeMetaData(CLOSURE_ARGUMENTS, inferredTypes);
            } else {
                for (int i = 0; i < inferredTypes.length; i += 1) {
                    if (isDynamicTyped(inferredTypes[i])) continue; // GROOVY-6939
                    checkParamType(parameters[i], inferredTypes[i], false, false);
                    typeCheckingContext.controlStructureVariables.put(parameters[i], inferredTypes[i]);
                }
            }
        }
    }

    private List<ClassNode[]> getSignaturesFromHint(final MethodNode method, final Expression hintType, final Expression options, final ASTNode usage) {
        String hintTypeName = hintType.getText();
        try {
            @SuppressWarnings("unchecked")
            // load hint class using compiler's ClassLoader
            Class<? extends ClosureSignatureHint> hintClass = (Class<? extends ClosureSignatureHint>) getTransformLoader().loadClass(hintTypeName);
            ClosureSignatureHint hint = hintClass.getDeclaredConstructor().newInstance();
            return hint.getClosureSignatures(
                    method instanceof ExtensionMethodNode ? ((ExtensionMethodNode) method).getExtensionMethodNode() : method,
                    typeCheckingContext.getSource(),
                    typeCheckingContext.getCompilationUnit(),
                    convertToStringArray(options),
                    usage);
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
    }

    private List<ClassNode[]> resolveWithResolver(final List<ClassNode[]> candidates, final ClassNode receiver, final Expression arguments, final ClosureExpression expression, final MethodNode method, final Expression resolverType, final Expression options) {
        String resolverTypeName = resolverType.getText();
        try {
            @SuppressWarnings("unchecked")
            // load conflict resolver class using compiler's ClassLoader
            Class<? extends ClosureSignatureConflictResolver> resolverClass = (Class<? extends ClosureSignatureConflictResolver>) getTransformLoader().loadClass(resolverTypeName);
            ClosureSignatureConflictResolver resolver = resolverClass.getDeclaredConstructor().newInstance();
            return resolver.resolve(
                    candidates,
                    receiver,
                    arguments,
                    expression,
                    method instanceof ExtensionMethodNode ? ((ExtensionMethodNode) method).getExtensionMethodNode() : method,
                    typeCheckingContext.getSource(),
                    typeCheckingContext.getCompilationUnit(),
                    convertToStringArray(options));
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
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

    private ClassLoader getTransformLoader() {
        return Optional.ofNullable(typeCheckingContext.getCompilationUnit()).map(CompilationUnit::getTransformLoader).orElseGet(() -> getSourceUnit().getClassLoader());
    }

    /**
     * Computes the inferred types of the closure parameters using the following trick:
     * <ol>
     * <li> creates a dummy MethodNode for which the return type is a class node
     *      for which the generic types are the types returned by the hint
     * <li> calls {@link #inferReturnTypeGenerics}
     * <li> returns inferred types from the result
     * </ol>
     * In practice it could be done differently but it has the main advantage of
     * reusing existing code, hence reducing the amount of code to debug in case
     * of failure.
     */
    private void resolveGenericsFromTypeHint(final ClassNode receiver, final Expression arguments, final MethodNode selectedMethod, final ClassNode[] signature) {
        ClassNode returnType = new ClassNode("ClForInference$" + UNIQUE_LONG.incrementAndGet(), 0, null).getPlainNodeReference();
        returnType.setGenericsTypes(Arrays.stream(signature).map(ClassNode::asGenericsType).toArray(GenericsType[]::new));

        MethodNode methodNode = selectedMethod instanceof ExtensionMethodNode ? ((ExtensionMethodNode) selectedMethod).getExtensionMethodNode() : selectedMethod;
        methodNode = new MethodNode("$$", methodNode.getModifiers(), returnType, methodNode.getParameters(), methodNode.getExceptions(), null);
        methodNode.setDeclaringClass(selectedMethod.getDeclaringClass());
        methodNode.setGenericsTypes(selectedMethod.getGenericsTypes());
        if (selectedMethod instanceof ExtensionMethodNode) {
            methodNode = new ExtensionMethodNode(
                    methodNode,
                    methodNode.getName(),
                    methodNode.getModifiers(),
                    returnType,
                    selectedMethod.getParameters(),
                    selectedMethod.getExceptions(),
                    null,
                    ((ExtensionMethodNode) selectedMethod).isStaticExtension()
            );
            methodNode.setDeclaringClass(selectedMethod.getDeclaringClass());
            methodNode.setGenericsTypes(selectedMethod.getGenericsTypes());
        }

        GenericsType[] typeArguments = null;
        Expression emc = typeCheckingContext.getEnclosingMethodCall(); // GROOVY-7789, GROOVY-11168
        if (emc instanceof MethodCallExpression) { MethodCallExpression call = (MethodCallExpression) emc;
            if (arguments == call.getArguments() || InvocationWriter.makeArgumentList(arguments).getExpressions().stream().anyMatch(arg ->
                    arg instanceof ClosureExpression && DefaultGroovyMethods.contains(InvocationWriter.makeArgumentList(call.getArguments()), arg)))
                typeArguments = call.getGenericsTypes();
        }

        returnType = inferReturnTypeGenerics(receiver, methodNode, arguments, typeArguments);
        GenericsType[] returnTypeGenerics = returnType.getGenericsTypes();
        for (int i = 0, n = returnTypeGenerics.length; i < n; i += 1) {
            GenericsType gt = returnTypeGenerics[i];
            if (gt.isPlaceholder()) { // GROOVY-9968, GROOVY-10528, et al.
                signature[i] = gt.getUpperBounds() != null ? gt.getUpperBounds()[0] : gt.getType().redirect();
            } else {
                signature[i] = getCombinedBoundType(gt);
            }
        }
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
                    stInt = (Integer) evaluateExpression(castX(Integer_TYPE, strategy), getSourceUnit().getConfiguration(), getSourceUnit().getClassLoader());
                }
                if (value instanceof ClassExpression && !value.getType().equals(DELEGATES_TO_TARGET)) {
                    if (genericTypeIndex != null) {
                        addStaticTypeError("Cannot use @DelegatesTo(genericTypeIndex=" + genericTypeIndex.getText()
                                + ") without @DelegatesTo.Target because generic argument types are not available at runtime", value);
                    }
                    // temporarily store the delegation strategy and the delegate type
                    expression.putNodeMetaData(DELEGATION_METADATA, newDelegationMetadata(value.getType(), stInt));
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
                            resolveGenericsFromTypeHint(receiver, arguments, mn, resolved);
                            expression.putNodeMetaData(DELEGATION_METADATA, newDelegationMetadata(resolved[0], stInt));
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
                                                addStaticTypeError("Unable to map actual type [" + prettyPrintType(actualType) + "] onto " + prettyPrintType(paramType), methodParam);
                                            }
                                        }
                                    }
                                    expression.putNodeMetaData(DELEGATION_METADATA, newDelegationMetadata(actualType, stInt));
                                    break;
                                }
                            }
                        }
                    }
                    if (expression.getNodeMetaData(DELEGATION_METADATA) == null) {
                        addError("Not enough arguments found for a @DelegatesTo method call. Please check that you either use an explicit class or @DelegatesTo.Target with a correct id", annotation);
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
        if (isClassClassNodeWrappingConcreteType(delegate)) { // GROOVY-11393: Type from Class<Type>
            receivers.add(new Receiver<>(delegate.getGenericsTypes()[0].getType(), false, path));
        }
        if (receivers.stream().map(Receiver::getType).noneMatch(delegate::equals)) {
            receivers.add(new Receiver<>(delegate, path));
        }
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        String name = call.getMethodAsString();
        if (name == null) {
            addStaticTypeError("Cannot resolve dynamic method name at compile time", call.getMethod());
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

        ClassNode receiver = getType(objectExpression);
        if (objectExpression instanceof ConstructorCallExpression) { // GROOVY-10228
            inferDiamondType((ConstructorCallExpression) objectExpression, receiver.getPlainNodeReference());
        }
        if (call.isSpreadSafe()) {
            ClassNode componentType = inferComponentType(receiver, null);
            if (componentType == null) {
                addStaticTypeError("Spread-dot operator can only be used on iterable types", objectExpression);
            } else {
                MethodCallExpression subcall = callX(varX("item", componentType), name, call.getArguments());
                subcall.setLineNumber(call.getLineNumber()); subcall.setColumnNumber(call.getColumnNumber());
                subcall.setImplicitThis(call.isImplicitThis());
                visitMethodCallExpression(subcall);
                // inferred type should be a list of what sub-call returns
                storeType(call, extension.buildListType(getType(subcall)));
                storeTargetMethod(call, subcall.getNodeMetaData(DIRECT_METHOD_CALL_TARGET));
            }
            typeCheckingContext.popEnclosingMethodCall();
            return;
        }

        Expression callArguments = call.getArguments();
        ArgumentListExpression argumentList = InvocationWriter.makeArgumentList(callArguments);

        // visit functional arguments *after* target method selection
        visitMethodCallArguments(receiver, argumentList, false, null);

        boolean isThisObjectExpression = isThisExpression(objectExpression);
        boolean isCallOnClosure = false;
        FieldNode fieldNode = null;
        switch (name) {
          case "call":
          case "doCall":
            if (!isThisObjectExpression) {
                isCallOnClosure = receiver.equals(CLOSURE_TYPE);
                break;
            }
          default:
            if (isThisObjectExpression) {
                ClassNode enclosingType = typeCheckingContext.getEnclosingClassNode();
                fieldNode = enclosingType.getDeclaredField(name);
                if (fieldNode != null && getType(fieldNode).equals(CLOSURE_TYPE)
                        && !enclosingType.hasPossibleMethod(name, callArguments)) {
                    isCallOnClosure = true;
                }
            }
        }

        try {
            ClassNode[] args = getArgumentTypes(argumentList);
            boolean functorsVisited = false;
            if (isCallOnClosure) {
                if (fieldNode != null) {
                    GenericsType[] genericsTypes = getType(fieldNode).getGenericsTypes();
                    if (genericsTypes != null) {
                        Parameter[] parameters = fieldNode.getNodeMetaData(CLOSURE_ARGUMENTS);
                        if (parameters != null) {
                            typeCheckClosureCall(callArguments, args, parameters);
                        }
                        ClassNode closureReturnType = genericsTypes[0].getType();
                        storeType(call, closureReturnType);
                    }
                } else if (objectExpression instanceof VariableExpression) {
                    Variable variable = findTargetVariable((VariableExpression) objectExpression);
                    if (variable instanceof ASTNode) {
                        Parameter[] parameters = ((ASTNode) variable).getNodeMetaData(CLOSURE_ARGUMENTS);
                        if (parameters != null) {
                            checkForbiddenSpreadArgument(argumentList, parameters);
                            typeCheckClosureCall(callArguments, args, parameters);
                        }
                        ClassNode type = getType((ASTNode) variable);
                        if (type.equals(CLOSURE_TYPE)) { // GROOVY-10098, et al.
                            GenericsType[] genericsTypes = type.getGenericsTypes();
                            if (genericsTypes != null && genericsTypes.length == 1
                                    && genericsTypes[0].getLowerBound() == null) {
                                type = genericsTypes[0].getType();
                            } else {
                                type = OBJECT_TYPE;
                            }
                        }
                        if (type != null) {
                            storeType(call, type);
                        }
                    }
                } else if (objectExpression instanceof ClosureExpression) {
                    // we can get actual parameters directly
                    Parameter[] parameters = ((ClosureExpression) objectExpression).getParameters();
                    if (parameters != null) {
                        typeCheckClosureCall(callArguments, args, parameters);
                    }
                    ClassNode type = getInferredReturnType(objectExpression);
                    if (type != null) {
                        storeType(call, type);
                    }
                }

                List<Expression> list = argumentList.getExpressions();
                int nArgs = list.stream().noneMatch(e -> e instanceof SpreadExpression) ? list.size() : Integer.MAX_VALUE;
                storeTargetMethod(call, nArgs == 0 ? CLOSURE_CALL_NO_ARG : nArgs == 1 ? CLOSURE_CALL_ONE_ARG : CLOSURE_CALL_VARGS);
            } else {
                List<MethodNode> mn = null;
                Receiver<String> chosenReceiver = null;
                // GROOVY-11386:
                if (isThisObjectExpression && call.isImplicitThis()
                    && typeCheckingContext.getEnclosingClosure() != null
                    && !name.equals("call") && !name.equals("doCall")) { // GROOVY-9662
                    mn = CLOSURE_TYPE.getMethods(name);
                    if (!mn.isEmpty()) {
                        receiver = CLOSURE_TYPE.getPlainNodeReference();
                        objectExpression.removeNodeMetaData(INFERRED_TYPE);
                    }
                }
                if (mn == null || mn.isEmpty()) {
                    // method call receivers are:
                    //   - closure delegate(s) or owner(s) and trait self type(s)
                    //   - the actual receiver as found in the method call expression
                    //   - any of the potential receivers found in the instanceof temporary table
                    // in that order
                    List<Receiver<String>> receivers = new ArrayList<>();
                    addReceivers(receivers, makeOwnerList(objectExpression), call.isImplicitThis());

                    MethodNode first = null;
                    for (Receiver<String> currentReceiver : receivers) {
                        mn = findMethod(currentReceiver.getType().getPlainNodeReference(), name, args);
                        if (!mn.isEmpty()) {
                            first = mn.get(0); // capture for error reporting
                            chosenReceiver = currentReceiver;
                            // GROOVY-10819, GROOVY-10820, GROOVY-11393, et al.:
                            // for a static receiver, only static methods are compatible
                            mn = allowStaticAccessToMember(mn, !currentReceiver.isObject());
                            if (!mn.isEmpty()) {
                                break;
                            }
                        }
                    }
                    if (mn.isEmpty()) {
                        mn = extension.handleMissingMethod(receiver, name, argumentList, args, call);
                        if (mn == null || mn.isEmpty()) {
                            if (first != null) mn = List.of(first); // instance method
                            else addNoMatchingMethodError(receiver, name, args, call);
                        }
                    }
                }
                if (!mn.isEmpty()) {
                    if (areCategoryMethodCalls(mn, name, args)) addCategoryMethodCallError(call);

                    {
                        ClassNode obj = chosenReceiver != null ? chosenReceiver.getType() : null;
                        if (mn.size() > 1 && obj instanceof UnionTypeClassNode) { // GROOVY-8965: support duck-typing using dynamic resolution
                            ClassNode returnType = mn.stream().map(MethodNode::getReturnType).reduce(WideningCategories::lowestUpperBound).get();
                            call.putNodeMetaData(DYNAMIC_RESOLUTION, returnType);

                            MethodNode tmp = new MethodNode(name, 0, returnType, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
                            tmp.setDeclaringClass(obj); // for storeTargetMethod
                            mn = Collections.singletonList(tmp);
                        } else {
                            mn = disambiguateMethods(mn, obj, args, call);
                        }
                    }

out:                if (mn.size() != 1) {
                        addAmbiguousErrorMessage(mn, name, args, call);
                    } else {
                        MethodNode targetMethod = mn.get(0);
                        // note second pass to differentiate from extension that sets type
                        boolean mergeType = (call.getNodeMetaData(INFERRED_TYPE) != null);
                        storeTargetMethod(call, targetMethod); // trigger onMethodSelection
                        if (call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET) != targetMethod) {
                            break out; // type-checking script intervention
                        }
                        ClassNode declaringClass = targetMethod.getDeclaringClass();
                        if (chosenReceiver == null) {
                            chosenReceiver = Receiver.make(declaringClass.getPlainNodeReference());
                        }
                        if (!targetMethod.isStatic() && !(isClassType(declaringClass) || isObjectType(declaringClass)) // GROOVY-10939: Class or Object
                                && !chosenReceiver.isObject() && !Boolean.TRUE.equals(call.getNodeMetaData(DYNAMIC_RESOLUTION))) {
                            addStaticTypeError("Non-static method " + prettyPrintTypeName(declaringClass) + "#" + targetMethod.getName() + " cannot be called from static context", call);
                        } else if ((chosenReceiver.getType().isInterface() || targetMethod.isAbstract()) && isSuperExpression(objectExpression)) { // GROOVY-8299, GROOVY-10341
                            String target = toMethodParametersString(targetMethod.getName(), extractTypesFromParameters(targetMethod.getParameters()));
                            if (targetMethod.isDefault() || Traits.hasDefaultImplementation(targetMethod)) { // GROOVY-10494
                                if (objectExpression instanceof VariableExpression)
                                  addStaticTypeError("Default method " + target + " requires qualified super", call);
                            } else {
                                addStaticTypeError("Abstract method " + target + " cannot be called directly", call);
                            }
                        }

                        visitMethodCallArguments(chosenReceiver.getType(), argumentList, true, targetMethod); functorsVisited = true;

                        ClassNode returnType = getType(targetMethod);
                        // GROOVY-5470, GROOVY-6091, GROOVY-9604, GROOVY-11399:
                        if (isThisObjectExpression && call.isImplicitThis() && typeCheckingContext.getEnclosingClosure() != null
                                && (targetMethod == GET_DELEGATE || targetMethod == GET_OWNER || targetMethod == GET_THISOBJECT)) {
                            switch (name) {
                              case "getDelegate":
                                var dm = getDelegationMetadata(typeCheckingContext.getEnclosingClosure().getClosureExpression());
                                if (dm != null) {
                                    returnType = dm.getType();
                                    break;
                                }
                                // falls through
                              case "getOwner":
                                if (typeCheckingContext.getEnclosingClosureStack().size() > 1) {
                                    returnType = CLOSURE_TYPE.getPlainNodeReference();
                                    break;
                                }
                                // falls through
                              case "getThisObject":
                                returnType = makeThis();
                            }
                        } else if (isUsingGenericsOrIsArrayUsingGenerics(returnType)) {
                            ClassNode irtg = inferReturnTypeGenerics(chosenReceiver.getType(), targetMethod, callArguments, call.getGenericsTypes());
                            if (irtg != null && implementsInterfaceOrIsSubclassOf(irtg, returnType))
                                returnType = irtg;
                        }
                        // GROOVY-7106, GROOVY-7274, GROOVY-8909, GROOVY-8961, GROOVY-9734, GROOVY-9844, GROOVY-9915, et al.
                        Parameter[] parameters = targetMethod.getParameters();
                        if (chosenReceiver.getType().getGenericsTypes() != null && !targetMethod.isStatic() && !(targetMethod instanceof ExtensionMethodNode)) {
                            Map<GenericsTypeName, GenericsType> context = extractPlaceHoldersVisibleToDeclaration(chosenReceiver.getType(), targetMethod, argumentList);
                            parameters = Arrays.stream(parameters).map(p -> new Parameter(applyGenericsContext(context, p.getType()), p.getName())).toArray(Parameter[]::new);
                        }
                        resolvePlaceholdersFromImplicitTypeHints(args, argumentList, parameters);

                        if (typeCheckMethodsWithGenericsOrFail(chosenReceiver.getType(), args, targetMethod, call)) {
                            String data = chosenReceiver.getData();
                            if (data != null) {
                                // the method which has been chosen is supposed to be a call on delegate or owner
                                // so we store the information so that the static compiler may reuse it
                                call.putNodeMetaData(IMPLICIT_RECEIVER, data);
                            }
                            receiver = chosenReceiver.getType();
                            if (mergeType || call.getNodeMetaData(INFERRED_TYPE) == null)
                                storeType(call, adjustWithTraits(targetMethod, receiver, args, returnType));

                            if (objectExpression instanceof VariableExpression && ((VariableExpression) objectExpression).isClosureSharedVariable()) {
                                // if the object expression is a closure shared variable, we will have to perform a second pass
                                typeCheckingContext.secondPassExpressions.add(new SecondPassExpression<>(call, args));
                            }
                        } else {
                            call.removeNodeMetaData(DIRECT_METHOD_CALL_TARGET);
                        }
                    }
                }
            }
            // adjust typing for explicit math methods which have special handling; operator variants handled elsewhere
            if (args.length == 1 && isNumberType(args[0]) && isNumberType(receiver) && NUMBER_OPS.containsKey(name)) {
                ClassNode resultType = getMathResultType(NUMBER_OPS.get(name), receiver, args[0], name);
                if (resultType != null) {
                    storeType(call, resultType);
                }
            }

            // type-checking extension may have changed or cleared target method
            MethodNode target = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (!functorsVisited) {
                visitMethodCallArguments(receiver, argumentList, true, target);
            }
            if (target != null) { Parameter[] params = target.getParameters();
                checkClosureMetadata(argumentList.getExpressions(), params);
                checkForbiddenSpreadArgument(argumentList, params);
            } else {
                checkForbiddenSpreadArgument(argumentList);
            }
        } finally {
            typeCheckingContext.popEnclosingMethodCall();
            extension.afterMethodCall(call);
        }
    }

    private void checkClosureMetadata(final List<Expression> arguments, final Parameter[] parameters) {
        // TODO: Check additional delegation metadata like type (see checkClosureWithDelegatesTo).
        for (int i = 0, n = Math.min(arguments.size(), parameters.length); i < n; i += 1) {
            Expression argument = arguments.get(i);
            ClassNode aType = getType(argument), pType = parameters[i].getType();
            // GROOVY-7996: check delegation metadata of closure parameter used as method call argument
            if (CLOSURE_TYPE.equals(aType) && CLOSURE_TYPE.equals(pType) && argument instanceof VariableExpression && ((VariableExpression) argument).getAccessedVariable() instanceof Parameter) {
                int incomingStrategy = getResolveStrategy((Parameter) ((VariableExpression) argument).getAccessedVariable());
                int outgoingStrategy = getResolveStrategy(parameters[i]);
                if (incomingStrategy != outgoingStrategy) {
                    addStaticTypeError("Closure parameter with resolve strategy " + getResolveStrategyName(incomingStrategy) + " passed to method with resolve strategy " + getResolveStrategyName(outgoingStrategy), argument);
                }
            }
        }
    }

    private int getResolveStrategy(final Parameter parameter) {
        List<AnnotationNode> annotations = parameter.getAnnotations(DELEGATES_TO);
        if (annotations != null && !annotations.isEmpty()) {
            Expression strategy = annotations.get(0).getMember("strategy");
            if (strategy != null) {
                return (Integer) evaluateExpression(castX(Integer_TYPE, strategy), getSourceUnit().getConfiguration(), getSourceUnit().getClassLoader());
            }
        }
        return Closure.OWNER_FIRST;
    }

    /**
     * A special method handling the "withTraits" call for which the type checker
     * knows more than what the type signature is able to tell. If "withTraits"
     * is detected, then a new class node is created representing the receiver
     * type interfaces and the trait interface(s).
     *
     * @param directMethodCallCandidate a method selected by the type checker
     * @param receiver                  the receiver of the method call
     * @param argumentTypes             the argument types of the method call
     * @param returnType                the return type as inferred by the type checker
     * @return proxy return type if the selected method is {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#withTraits(Object, Class[]) withTraits}
     */
    private static ClassNode adjustWithTraits(final MethodNode directMethodCallCandidate, final ClassNode receiver, final ClassNode[] argumentTypes, final ClassNode returnType) {
        if ("withTraits".equals(directMethodCallCandidate.getName()) && isDefaultExtension(directMethodCallCandidate)) {
            List<ClassNode> interfaces = new ArrayList<>(Arrays.asList(receiver.getInterfaces()));
            for (ClassNode argumentType : argumentTypes) {
                if (isClassClassNodeWrappingConcreteType(argumentType)) {
                    argumentType = argumentType.getGenericsTypes()[0].getType();
                }
                if (argumentType.isInterface()) {
                    interfaces.add(argumentType);
                }
            }
            return new WideningCategories.LowestUpperBoundClassNode("ProxyOf$" + receiver.getNameWithoutPackage(), OBJECT_TYPE, interfaces.toArray(ClassNode.EMPTY_ARRAY));
        }
        return returnType;
    }

    /**
     * In the case of a <em>Object.with { ... }</em> call, this method is supposed to retrieve
     * the inferred closure return type.
     *
     * @param callArguments the argument list from the <em>Object#with(Closure)</em> call, i.e. a single closure expression
     * @return the inferred closure return type or <em>null</em>
     */
    protected ClassNode getInferredReturnTypeFromWithClosureArgument(final Expression callArguments) {
        if (!(callArguments instanceof ArgumentListExpression)) return null;

        ArgumentListExpression argList = (ArgumentListExpression) callArguments;
        ClosureExpression closure = (ClosureExpression) argList.getExpression(0);

        visitClosureExpression(closure);

        return getInferredReturnType(closure);
    }

    /**
     * Given an object expression (a message receiver expression), generate list
     * of possible types.
     *
     * @param objectExpression the message receiver
     * @return types and qualifiers of the receiver
     */
    protected List<Receiver<String>> makeOwnerList(final Expression objectExpression) {
        ClassNode receiver = getType(objectExpression);
        List<Receiver<String>> owners = new ArrayList<>();
        if (typeCheckingContext.delegationMetadata != null
                && objectExpression instanceof VariableExpression
                && ((Variable) objectExpression).getName().equals("owner")
                && /*isNested:*/typeCheckingContext.delegationMetadata.getParent() != null) {
            owners.add(new Receiver<>(receiver, "owner")); // if nested, Closure is the first owner
            List<Receiver<String>> thisType = new ArrayList<>(2); // and the enclosing class is the
            addDelegateReceiver(thisType, makeThis(), null);      // end of the closure owner chain
            addReceivers(owners, thisType, typeCheckingContext.delegationMetadata.getParent(), "owner.");
        } else {
            List<ClassNode> temporaryTypes = getTemporaryTypesForExpression(objectExpression);
            int temporaryTypesCount = (temporaryTypes != null ? temporaryTypes.size() : 0);
            if (temporaryTypesCount > 0) { // GROOVY-8965, GROOVY-10180, GROOVY-10668
                owners.add(Receiver.make(lowestUpperBound(temporaryTypes)));
            }
            if (isClassClassNodeWrappingConcreteType(receiver)) {
                ClassNode staticType = receiver.getGenericsTypes()[0].getType();
                owners.add(new Receiver<>(staticType, false, null)); // Type from Class<Type>
                addTraitType(staticType, owners); // T in Class<T$Trait$Helper>
                owners.add(Receiver.make(receiver)); // Class<Type>
            } else {
                addBoundType(receiver, owners);
                addSelfTypes(receiver, owners);
                addTraitType(receiver, owners);
                if (isSuperExpression(objectExpression)) { // GROOVY-9909: super.defaultMethod()
                    for (ClassNode in : typeCheckingContext.getEnclosingClassNode().getInterfaces()) {
                        if (!receiver.implementsInterface(in)) owners.add(Receiver.make(in));
                    }
                }
            }
            if (temporaryTypesCount > 1 && !(objectExpression instanceof VariableExpression)) {
                owners.add(Receiver.make(new UnionTypeClassNode(temporaryTypes.toArray(ClassNode.EMPTY_ARRAY))));
            }
        }
        return owners;
    }

    private static void addBoundType(final ClassNode receiver, final List<Receiver<String>> owners) {
        if (!receiver.isGenericsPlaceHolder() || receiver.getGenericsTypes() == null) {
            owners.add(Receiver.make(receiver));
            return;
        }

        GenericsType gt = receiver.getGenericsTypes()[0];
        if (gt.getLowerBound() == null && gt.getUpperBounds() != null) {
            for (ClassNode cn : gt.getUpperBounds()) { // T extends C & I
                addBoundType(cn, owners);
                addSelfTypes(cn, owners);
            }
        } else {
            ClassNode cn = gt.getType().redirect(); // GROOVY-10846
            owners.add(Receiver.make(cn)); // T or T super Type
        }
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

    private   void checkForbiddenSpreadArgument(final ArgumentListExpression arguments, final Parameter[] parameters) {
        if (!isVargs(parameters)) {
            checkForbiddenSpreadArgument(arguments);
        } else { // GROOVY-10597: allow spread for the ... param
            List<Expression> list = arguments.getExpressions();
            list = list.subList(0, parameters.length - 1);
            checkForbiddenSpreadArgument(args(list));
        }
    }

    protected void checkForbiddenSpreadArgument(final ArgumentListExpression arguments) {
        for (Expression argument : arguments) {
            if (argument instanceof SpreadExpression) {
                addStaticTypeError("The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time", argument);
            }
        }
    }

    protected void storeTargetMethod(final Expression call, final MethodNode target) {
        if (target == null) {
            call.removeNodeMetaData(DIRECT_METHOD_CALL_TARGET); return;
        }
        call.putNodeMetaData(DIRECT_METHOD_CALL_TARGET,target);

        checkInterfaceStaticCall(call, target);
        checkOrMarkPrivateAccess(call, target);
        checkSuperCallFromClosure(call, target);
        extension.onMethodSelection(call, target);
    }

    private void checkInterfaceStaticCall(final Expression call, final MethodNode target) {
        if (target instanceof ExtensionMethodNode) return;
        ClassNode declaringClass = target.getDeclaringClass();
        if (declaringClass.isInterface() && target.isStatic()) {
            Expression objectExpression = getObjectExpression(call);
            if (objectExpression != null) { ClassNode type = getType(objectExpression);
                if (!isClassClassNodeWrappingConcreteType(type) || !type.getGenericsTypes()[0].getType().equals(declaringClass)) {
                    addStaticTypeError("static method of interface " + prettyPrintTypeName(declaringClass) + " can only be accessed with class qualifier", call);
                }
            }
        }
    }

    private void checkSuperCallFromClosure(final Expression call, final MethodNode target) {
        if (isSuperExpression(getObjectExpression(call)) && typeCheckingContext.getEnclosingClosure() != null) {
            ClassNode current = typeCheckingContext.getEnclosingClassNode();
            current.getNodeMetaData(SUPER_MOP_METHOD_REQUIRED, x -> new LinkedList<>()).add(target);
            call.putNodeMetaData(SUPER_MOP_METHOD_REQUIRED, current);
        }
    }

    private static Expression getObjectExpression(final Expression expression) {
        if (expression instanceof MethodCallExpression) {
            return ((MethodCallExpression) expression).getObjectExpression();
        }
        if (expression instanceof PropertyExpression) {
            return ((PropertyExpression) expression).getObjectExpression();
        }
        /*if (expression instanceof MethodPointerExpression) {
            return ((MethodPointerExpression) expression).getExpression();
        }*/
        return null;
    }

    protected void typeCheckClosureCall(final Expression arguments, final ClassNode[] argumentTypes, final Parameter[] parameters) {
        int nArguments = argumentTypes.length;
        List<ClassNode[]> signatures = new LinkedList<>();
        signatures.add(extractTypesFromParameters(parameters));

        var n = Arrays.stream(parameters).filter(Parameter::hasInitialExpression).count();
        for (int i = 1; i <= n; i += 1) { // drop parameters with value from right to left
            ClassNode[] signature = new ClassNode[parameters.length - i];
            int j = 1, index = 0;
            for (Parameter parameter : parameters) {
                if (j > n - i && parameter.hasInitialExpression()) {
                    // skip parameter with default argument
                } else {
                    signature[index++] = parameter.getType();
                }
                if (parameter.hasInitialExpression()) j += 1;
            }
            signatures.add(signature);
        }

        for (var it = signatures.listIterator(); it.hasNext(); ) { var signature = it.next();
            if (asBoolean(signature) && last(signature).isArray()) {
                int vaIndex = signature.length - 1;
                if (vaIndex == nArguments) { // empty array
                    it.add(Arrays.copyOf(signature, nArguments));
                } else if (vaIndex < nArguments) { // spread arg(s)?
                    var subType = last(signature).getComponentType();
                    signature = Arrays.copyOf(signature, nArguments);
                    Arrays.fill(signature, vaIndex, nArguments, subType);
                    it.add(signature);
                }
            }
        }

        ClassNode[] firstMatch = null;

trying: for (ClassNode[] signature : signatures) {
            if (nArguments == signature.length) {
                if (firstMatch == null)
                    firstMatch = signature;
                for (int i = 0; i < nArguments; i += 1) {
                    if (!isAssignableTo(argumentTypes[i], signature[i])) {
                        continue trying;
                    }
                }
                return; // arguments match
            }
        }

        String actual = formatArgumentList(argumentTypes);
        String expect = formatArgumentList(firstMatch != null ? firstMatch : signatures.get(0));
        addStaticTypeError("Cannot call closure that accepts " + expect + " with " + actual, arguments);
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        try {
            Statement thenPath = ifElse.getIfBlock(), elsePath = ifElse.getElseBlock();

            // create a new scope for instanceof testing
            typeCheckingContext.pushTemporaryTypeInfo();
            visitStatement(ifElse);
            ifElse.getBooleanExpression().visit(this);

            thenPath.visit(this);

            Map<Object, List<ClassNode>> tti = typeCheckingContext.temporaryIfBranchTypeInformation.pop();
            // GROOVY-6099: isolate assignment tracking
            restoreTypeBeforeConditional();

            // GROOVY-6429: reverse instanceof tracking
            typeCheckingContext.pushTemporaryTypeInfo();
            tti.forEach(this::putNotInstanceOfTypeInfo);

            elsePath.visit(this);

            typeCheckingContext.popTemporaryTypeInfo();
            // GROOVY-8523: propagate tracking to outer scope; keep simple for now
            if (elsePath.isEmpty() && !GeneralUtils.maybeFallsThrough(thenPath)) {
                tti.forEach(this::putNotInstanceOfTypeInfo);
            }
            // GROOVY-9786: if chaining: "if (...) x=?; else if (...) x=?;"
            Map<VariableExpression, ClassNode> updates = elsePath.getNodeMetaData("assignments");
            if (updates != null) {
                updates.forEach(this::recordAssignment);
            }
        } finally {
            ifElse.putNodeMetaData("assignments", popAssignmentTracking(oldTracker));
        }
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
            typeCheckingContext.popTemporaryTypeInfo();
            typeCheckingContext.popEnclosingSwitchStatement();
        }
    }

    @Override
    protected void afterSwitchConditionExpressionVisited(final SwitchStatement statement) {
        typeCheckingContext.pushTemporaryTypeInfo();
        Expression conditionExpression = statement.getExpression();
        conditionExpression.putNodeMetaData(TYPE, getType(conditionExpression));
    }

    @Override
    protected void afterSwitchCaseStatementsVisited(final SwitchStatement statement) {
        // GROOVY-8411: if any "case Type:" then "default:" contributes condition type
        if (!statement.getDefaultStatement().isEmpty() && !typeCheckingContext.temporaryIfBranchTypeInformation.peek().isEmpty())
            pushInstanceOfTypeInfo(statement.getExpression(), new ClassExpression(statement.getExpression().getNodeMetaData(TYPE)));
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        Expression selectable = typeCheckingContext.getEnclosingSwitchStatement().getExpression();
        Expression expression = statement.getExpression();
        if (expression instanceof ClassExpression) { // GROOVY-8411: refine the switch type
            pushInstanceOfTypeInfo(selectable, expression);
        } else if (expression instanceof ClosureExpression) { // GROOVY-9854: propagate the switch type
            ClassNode inf = selectable.getNodeMetaData(TYPE);
            expression.putNodeMetaData(CLOSURE_ARGUMENTS, new ClassNode[]{inf});
            Parameter[] params = ((ClosureExpression) expression).getParameters();
            if (params != null && params.length == 1) {
                boolean lambda = (expression instanceof LambdaExpression);
                checkParamType(params[0], wrapTypeIfNecessary(inf), false, lambda);
            } else if (params == null || params.length > 1) {
                int paramCount = (params != null ? params.length : 0);
                addError("Incorrect number of parameters. Expected 1 but found " + paramCount, expression);
            }
        }
        super.visitCaseStatement(statement);
        if (!maybeFallsThrough(statement.getCode())) {
            typeCheckingContext.temporaryIfBranchTypeInformation
                .peek().remove(extractTemporaryTypeInfoKey(selectable));
            restoreTypeBeforeConditional(); // isolate assignment branch
        }
    }

    private static boolean maybeFallsThrough(Statement statement) {
        if (statement.isEmpty()) return true;
        if (statement instanceof BlockStatement)
            statement = DefaultGroovyMethods.last(((BlockStatement) statement).getStatements());
        // end break, continue, return or throw
        if (statement instanceof BreakStatement
                || statement instanceof ContinueStatement
                || !GeneralUtils.maybeFallsThrough(statement)) {
            return false;
        }
        return true;
    }

    private void recordAssignment(final VariableExpression lhsExpr, final ClassNode rhsType) {
        typeCheckingContext.ifElseForWhileAssignmentTracker.computeIfAbsent(lhsExpr, lhs -> {
            ClassNode lhsType = lhs.getNodeMetaData(INFERRED_TYPE);
            List<ClassNode> types = new ArrayList<>(2);
            types.add(lhsType);
            return types;
        }).add(rhsType);
    }

    private void restoreTypeBeforeConditional() {
        typeCheckingContext.ifElseForWhileAssignmentTracker.forEach((var, types) -> {
            var.putNodeMetaData(INFERRED_TYPE, types.get(0));
        });
    }

    protected Map<VariableExpression, List<ClassNode>> pushAssignmentTracking() {
        Map<VariableExpression, List<ClassNode>> oldTracker = typeCheckingContext.ifElseForWhileAssignmentTracker;
        typeCheckingContext.ifElseForWhileAssignmentTracker = new HashMap<>();
        return oldTracker;
    }

    protected Map<VariableExpression, ClassNode> popAssignmentTracking(final Map<VariableExpression, List<ClassNode>> oldTracker) {
        Map<VariableExpression, ClassNode> assignments = new HashMap<>();
        typeCheckingContext.ifElseForWhileAssignmentTracker.forEach((var, types) -> {
            types.stream().filter(t -> t != null && t != UNKNOWN_PARAMETER_TYPE) // GROOVY-6099, GROOVY-10294
                    .reduce(WideningCategories::lowestUpperBound).ifPresent(type -> {
                assignments.put(var, type);
                storeType(var, type);
            });
        });
        typeCheckingContext.ifElseForWhileAssignmentTracker = oldTracker;
        return assignments;
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        super.visitArrayExpression(expression);
        ClassNode elementType;
        List<Expression> expressions;
        if (expression.hasInitializer()) {
            elementType = expression.getElementType();
            expressions = expression.getExpressions();
        } else {
            elementType = int_TYPE;
            expressions = expression.getSizeExpression();
        }
        for (Expression elementExpr : expressions) {
            if (!checkCompatibleAssignmentTypes(elementType, getType(elementExpr), elementExpr, false)) {
                addStaticTypeError("Cannot convert from " + prettyPrintType(getType(elementExpr)) + " to " + prettyPrintType(elementType), elementExpr);
            }
        }
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        ClassNode target = expression.getType();
        Expression source = expression.getExpression();
        applyTargetType(target, source); // GROOVY-9997

        source.visit(this);

        if (!expression.isCoerce() && !checkCast(target, source)) {
            addStaticTypeError("Inconvertible types: cannot cast " + prettyPrintType(getType(source)) + " to " + prettyPrintType(target), expression);
        }
    }

    protected boolean checkCast(final ClassNode targetType, final Expression source) {
        boolean sourceIsNull = isNullConstant(source);
        ClassNode expressionType = getType(source);
        if (targetType.isArray() && expressionType.isArray()) {
            return checkCast(targetType.getComponentType(), varX("foo", expressionType.getComponentType()));
        } else if (isPrimitiveChar(targetType) && isStringType(expressionType)
                && source instanceof ConstantExpression && source.getText().length() == 1) {
            // ex: (char) 'c'
        } else if (isWrapperCharacter(targetType) && (isStringType(expressionType) || sourceIsNull)
                && (sourceIsNull || source instanceof ConstantExpression && source.getText().length() == 1)) {
            // ex : (Character) 'c'
        } else if (isNumberCategory(getWrapper(targetType)) && (isNumberCategory(getWrapper(expressionType)) || isPrimitiveChar(expressionType))) {
            // ex: short s = (short) 0
        } else if (sourceIsNull && !isPrimitiveType(targetType)) {
            // ex: (Date)null
        } else if (isPrimitiveChar(targetType) && isPrimitiveType(expressionType) && isNumberType(expressionType)) {
            // char c = (char) ...
        } else if (sourceIsNull && isPrimitiveType(targetType) && !isPrimitiveBoolean(targetType)) {
            return false;
        } else if (!Modifier.isFinal(expressionType.getModifiers()) && targetType.isInterface()) {
            return true;
        } else if (!Modifier.isFinal(targetType.getModifiers()) && expressionType.isInterface()) {
            return true;
        } else if (!isAssignableTo(targetType, expressionType) && !implementsInterfaceOrIsSubclassOf(expressionType, targetType)) {
            return false;
        }
        return true;
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        typeCheckingContext.pushTemporaryTypeInfo(); // capture instanceof restriction
        if (!(expression instanceof ElvisOperatorExpression)) {
            expression.getBooleanExpression().visit(this);
        }
        Expression trueExpression = expression.getTrueExpression();
        ClassNode typeOfTrue = findCurrentInstanceOfClass(trueExpression, null);
        typeOfTrue = Optional.ofNullable(typeOfTrue).orElse(visitValueExpression(trueExpression));
        Map<Object, List<ClassNode>> tti = typeCheckingContext.temporaryIfBranchTypeInformation.pop();

        typeCheckingContext.pushTemporaryTypeInfo();
        tti.forEach(this::putNotInstanceOfTypeInfo); // GROOVY-8412
        Expression falseExpression = expression.getFalseExpression();
        ClassNode typeOfFalse = visitValueExpression(falseExpression);
        typeCheckingContext.popTemporaryTypeInfo();

        ClassNode resultType;
        if (isNullConstant(trueExpression) && isNullConstant(falseExpression)) { // GROOVY-5523
            resultType = checkForTargetType(trueExpression, UNKNOWN_PARAMETER_TYPE);
        } else if (isNullConstant(trueExpression) || (isEmptyCollection(trueExpression)
                && isOrImplements(typeOfTrue, typeOfFalse))) { // [] : List/Collection/Iterable
            resultType = wrapTypeIfNecessary(checkForTargetType(falseExpression, typeOfFalse));
        } else if (isNullConstant(falseExpression) || (isEmptyCollection(falseExpression)
                && isOrImplements(typeOfFalse, typeOfTrue))) { // List/Collection/Iterable : []
            resultType = wrapTypeIfNecessary(checkForTargetType(trueExpression, typeOfTrue));
        } else {
            typeOfFalse = checkForTargetType(falseExpression, typeOfFalse);
            typeOfTrue = checkForTargetType(trueExpression, typeOfTrue);
            resultType = lowestUpperBound(typeOfTrue, typeOfFalse);
        }
        storeType(expression, resultType);
        popAssignmentTracking(oldTracker);
    }

    /**
     * @param expr true or false branch of ternary expression
     * @return the inferred type of {@code expr}
     */
    private ClassNode visitValueExpression(final Expression expr) {
        if (expr instanceof ClosureExpression) {
            applyTargetType(checkForTargetType(expr, null), expr);
        }
        expr.visit(this);
        return getType(expr);
    }

    /**
     * @param expr true or false branch of ternary expression
     * @param type the inferred type of {@code expr}
     */
    private ClassNode checkForTargetType(final Expression expr, final ClassNode type) {
        ClassNode targetType = null;
        MethodNode enclosingMethod = typeCheckingContext.getEnclosingMethod();
        MethodCall enclosingMethodCall = (MethodCall)typeCheckingContext.getEnclosingMethodCall();
        BinaryExpression enclosingExpression = typeCheckingContext.getEnclosingBinaryExpression();
        if (enclosingExpression != null
                && isAssignment(enclosingExpression.getOperation().getType())
                && isTypeSource(expr, enclosingExpression.getRightExpression())) {
            targetType = getDeclaredOrInferredType(enclosingExpression.getLeftExpression());
        } else if (enclosingMethodCall != null
                && InvocationWriter.makeArgumentList(enclosingMethodCall.getArguments())
                        .getExpressions().stream().anyMatch(arg -> isTypeSource(expr, arg))) {
            // TODO: locate method target parameter
        } else if (enclosingMethod != null
                && !enclosingMethod.isAbstract()
                && !enclosingMethod.isVoidMethod()
                && isTypeSource(expr, enclosingMethod)) {
             targetType = enclosingMethod.getReturnType();
        }

        if (expr instanceof ClosureExpression) { // GROOVY-10271, GROOVY-10272
            return isSAMType(targetType) ? targetType : type;
        }

        if (targetType == null)
            targetType = type.getPlainNodeReference();
        if (type == UNKNOWN_PARAMETER_TYPE) return targetType;

        if (expr instanceof ConstructorCallExpression) { // GROOVY-9972, GROOVY-9983, GROOVY-10114
            inferDiamondType((ConstructorCallExpression) expr, targetType);
        }

        return adjustForTargetType(type, targetType);
    }

    private static ClassNode adjustForTargetType(final ClassNode resultType, final ClassNode targetType) {
        if (targetType.isUsingGenerics()
                && missesGenericsTypes(resultType)
                // GROOVY-10324, GROOVY-10342, et al.
                && !resultType.isGenericsPlaceHolder()) {
            // unchecked assignment
            // List<Type> list = new LinkedList()
            // Iterable<Type> iter = new LinkedList()
            // Collection<Type> col1 = Collections.emptyList()
            // Collection<Type> col2 = Collections.emptyList() ?: []
            // Collection<Type> view = ConcurrentHashMap.newKeySet()

            // the inferred type of the binary expression is the type of the RHS
            // "completed" with generics type information available from the LHS
            if (targetType.equals(resultType)) {
                // GROOVY-6126, GROOVY-6558, GROOVY-6564, et al.
                if (!targetType.isGenericsPlaceHolder()) return targetType;
            } else {
                // GROOVY-5640, GROOVY-9033, GROOVY-10220, GROOVY-10235, GROOVY-10688, GROOVY-11192, et al.
                Map<GenericsTypeName, GenericsType> gt = new HashMap<>();
                ClassNode sc = resultType;
                for (;;) {
                    sc = getNextSuperClass(sc,targetType);
                    if (!gt.isEmpty()) {
                        // propagate resultType's generics
                        sc = applyGenericsContext(gt, sc);
                    }
                    if (sc == null || sc.equals(targetType)) {
                        gt.clear();
                        break;
                    }
                    // map of sc's type vars to resultType's type vars
                    extractGenericsConnections(gt, sc, sc.redirect());
                }
                extractGenericsConnections(gt, resultType, resultType.redirect());
                extractGenericsConnections(gt, targetType, sc); // maps rt's tv(s)

                return applyGenericsContext(gt, resultType.redirect());
            }
        }

        return resultType;
    }

    private static boolean isTypeSource(final Expression expr, final Expression right) {
        if (right instanceof TernaryExpression) {
            return isTypeSource(expr, ((TernaryExpression) right).getTrueExpression())
                || isTypeSource(expr, ((TernaryExpression) right).getFalseExpression());
        }
        return expr == right;
    }

    private static boolean isTypeSource(final Expression expr, final MethodNode mNode) {
        boolean[] returned = new boolean[1];

        mNode.getCode().visit(new CodeVisitorSupport() {
            @Override
            public void visitReturnStatement(final ReturnStatement returnStatement) {
                if (isTypeSource(expr, returnStatement.getExpression())) {
                    returned[0] = true;
                }
            }
            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
            }
        });

        if (!returned[0]) {
            new ReturnAdder(returnStatement -> {
                if (isTypeSource(expr, returnStatement.getExpression())) {
                    returned[0] = true;
                }
            }).visitMethod(mNode);
        }

        return returned[0];
    }

    private static boolean isEmptyCollection(final Expression expr) {
        return isEmptyList(expr) || isEmptyMap(expr);
    }

    private static boolean isEmptyList(final Expression expr) {
        return expr instanceof ListExpression && ((ListExpression) expr).getExpressions().isEmpty();
    }

    private static boolean isEmptyMap(final Expression expr) {
        return expr instanceof MapExpression && ((MapExpression) expr).getMapEntryExpressions().isEmpty();
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
        if (cn == UNKNOWN_PARAMETER_TYPE) { // null for assignment or parameter
            // instead of storing an "unknown" type, reset the type information
            // by determining the declaration type of the expression
            cn = getOriginalDeclarationType(exp);
            // GROOVY-10356, GROOVY-10623: "def"
            if (isDynamicTyped(cn)) return;
        }
        if (cn != null && isPrimitiveType(cn)) {
            if (exp instanceof VariableExpression && ((VariableExpression) exp).isClosureSharedVariable()) {
                cn = getWrapper(cn);
            } else if (exp instanceof MethodCallExpression && ((MethodCallExpression) exp).isSafe()) {
                cn = getWrapper(cn);
            } else if (exp instanceof PropertyExpression && ((PropertyExpression) exp).isSafe()) {
                cn = getWrapper(cn);
            }
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
            if (accessedVariable instanceof VariableExpression) {
                if (accessedVariable != var)
                    storeType((VariableExpression) accessedVariable, cn);
            } else if (accessedVariable instanceof Parameter) {
                ((Parameter) accessedVariable).putNodeMetaData(INFERRED_TYPE, cn);
            }
            if (cn != null && var.isClosureSharedVariable()) {
                List<ClassNode> assignedTypes = typeCheckingContext.closureSharedVariablesAssignmentTypes.computeIfAbsent(var, k -> new LinkedList<>());
                assignedTypes.add(cn);
            }
            if (!var.isThisExpression() && !var.isSuperExpression() && !typeCheckingContext.temporaryIfBranchTypeInformation.isEmpty()) {
                // GROOVY-5226, GROOVY-11290: assignment voids instanceof
                pushInstanceOfTypeInfo(var, classX(VOID_TYPE));
            }
        }
    }

    protected ClassNode getResultType(ClassNode left, final int op, final ClassNode right, final BinaryExpression expr) {
        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();

        Expression leftExpression = expr.getLeftExpression();
        Expression rightExpression = expr.getRightExpression();

        if (op == EQUAL || op == ELVIS_EQUAL) {
            if (leftExpression instanceof VariableExpression) {
                ClassNode initialType = getOriginalDeclarationType(leftExpression);

                if (isPrimitiveType(rightRedirect) && initialType.isDerivedFrom(Number_TYPE)) {
                    return getWrapper(right);
                }

                if (isPrimitiveType(initialType) && rightRedirect.isDerivedFrom(Number_TYPE)) {
                    return getUnwrapper(right);
                }

                // as anything can be assigned to a String, Class or [Bb]oolean, return the left type instead
                if (isWildcardLeftHandSide(initialType) && !isObjectType(initialType)) {
                    return initialType;
                }
            }

            if (!isObjectType(leftRedirect)) {
                if (rightExpression instanceof ListExpression) {
                    if (LIST_TYPE.equals(leftRedirect)
                            || ITERABLE_TYPE.equals(leftRedirect)
                            || Collection_TYPE.equals(leftRedirect)
                            || ArrayList_TYPE.isDerivedFrom(leftRedirect)) { // GROOVY-6912
                        return getLiteralResultType(left, right, ArrayList_TYPE); // GROOVY-7128
                    }
                    if (SET_TYPE.equals(leftRedirect)
                            || LinkedHashSet_TYPE.isDerivedFrom(leftRedirect)) { // GROOVY-6912
                        return getLiteralResultType(left, right, LinkedHashSet_TYPE); // GROOVY-7128
                    }
                } else if (rightExpression instanceof MapExpression) {
                    if (MAP_TYPE.equals(leftRedirect)
                            || LinkedHashMap_TYPE.isDerivedFrom(leftRedirect)) {
                        return getLiteralResultType(left, right, LinkedHashMap_TYPE); // GROOVY-7128, GROOVY-9844
                    }
                } else if (rightExpression instanceof ClosureExpression
                        || rightExpression instanceof MethodPointerExpression) {
                    if (isSAMType(leftRedirect)) {
                        return left; // coercion
                    }
                }
            }

            return right;
        }

        if (isBoolIntrinsicOp(op)) {
            return boolean_TYPE;
        }

        if (op == FIND_REGEX) {
            return Matcher_TYPE;
        }

        if (isArrayOp(op)) {
            if (isOrImplements(left, MAP_TYPE) && (isStringType(right) || isGStringOrGStringStringLUB(right))) { // GROOVY-5700, GROOVY-6668, GROOVY-8212, GROOVY-8788
                PropertyExpression prop = propX(leftExpression, rightExpression); // m['xx'] -> m.xx
                return existsProperty(prop, !typeCheckingContext.isTargetOfEnclosingAssignment(expr))
                            ? getType(prop) : getTypeForMapPropertyExpression(left, prop);
            }
            Expression copy = binX(leftExpression, expr.getOperation(), rightExpression);
            copy.setSourcePosition(expr); // do not propagate BINARY_EXP_TARGET, etc.
            MethodNode method = findMethodOrFail(copy, left, "getAt", rightRedirect);
            if (method != null && !isNumberCategory(getWrapper(rightRedirect))) {
                return inferReturnTypeGenerics(left, method, rightExpression);
            }
            return inferComponentType(left, right);
        }

        String operationName = getOperationName(op);
        if (operationName == null) throw new GroovyBugError(
                "Unknown result type for binary operator " + op);
        // the left operand is determining the result of the operation
        // for primitives and their wrapper we use a fixed table here:
        ClassNode mathResultType = getMathResultType(op, leftRedirect, rightRedirect, operationName);
        if (mathResultType != null) {
            return mathResultType;
        }
        // GROOVY-9006: compare to null for types that overload equals
        if ("equals".equals(operationName) && (left == UNKNOWN_PARAMETER_TYPE
                                            || right == UNKNOWN_PARAMETER_TYPE)) {
            return boolean_TYPE;
        }
        // GROOVY-5890: do not mix Class<Type> with Type
        if (leftExpression instanceof ClassExpression) {
            left = CLASS_Type.getPlainNodeReference();
        }
        MethodNode method = findMethodOrFail(expr, left, operationName, right);
        if (method != null) {
            if (op == COMPARE_NOT_IN && isDefaultExtension(method)) {
                // GROOVY-10915: check if left implements its own isCase method
                MethodNode isCase = findMethodOrFail(expr, left, "isCase", right);
                if (isCase != null && !isDefaultExtension(isCase)) return null; // require dynamic dispatch
            }

            storeTargetMethod(expr, method);
            typeCheckMethodsWithGenericsOrFail(left, new ClassNode[]{right}, method, expr);

            if (isAssignment(op)) return left;
            if (!"compareTo".equals(operationName))
                return inferReturnTypeGenerics(left, method, args(rightExpression));
        }

        if (isCompareToBoolean(op)) return boolean_TYPE;
        if (op == COMPARE_TO) return int_TYPE;
        return null;
    }

    /**
     * For "{@code List<Type> x = [...]}" or "{@code Set<Type> y = [...]}", etc.
     * the literal may be composed of subtypes of {@code Type}. In these cases,
     * {@code ArrayList<Type>} is an appropriate result type for the expression.
     */
    private static ClassNode getLiteralResultType(final ClassNode targetType, final ClassNode sourceType, final ClassNode baseType) {
        ClassNode resultType = sourceType.equals(baseType) ? sourceType
                : GenericsUtils.parameterizeType(sourceType, baseType.getPlainNodeReference());

        if (targetType.getGenericsTypes() != null
                && !GenericsUtils.buildWildcardType(targetType).isCompatibleWith(resultType)) {
            BiPredicate<GenericsType, GenericsType> isEqualOrSuper = (target, source) -> {
                if (target.isCompatibleWith(source.getType())) {
                    return true;
                }
                if (!target.isPlaceholder() && !target.isWildcard()) {
                    return GenericsUtils.buildWildcardType(getCombinedBoundType(target)).isCompatibleWith(source.getType());
                }
                return false;
            };

            GenericsType[] lgt = targetType.getGenericsTypes(), rgt = resultType.getGenericsTypes();
            if (IntStream.range(0, lgt.length).allMatch(i -> isEqualOrSuper.test(lgt[i], rgt[i]))) {
                resultType = GenericsUtils.parameterizeType(targetType, baseType.getPlainNodeReference());
            }
        }

        return resultType;
    }

    private static ClassNode getMathResultType(final int op, final ClassNode leftRedirect, final ClassNode rightRedirect, final String operationName) {
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
        } else if (isPrimitiveChar(leftRedirect) && isPrimitiveChar(rightRedirect)) {
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
        if (isNumberCategory(getWrapper(rightRedirect)) && isNumberCategory(getWrapper(leftRedirect)) &&
            (REMAINDER == op || REMAINDER_EQUAL == op || MOD == op || MOD_EQUAL == op)) {
            return leftRedirect;
        }
        return null;
    }

    protected static ClassNode getGroupOperationResultType(final ClassNode a, final ClassNode b) {
        if (isBigIntCategory(a) && isBigIntCategory(b)) return BigInteger_TYPE;
        if (isBigDecCategory(a) && isBigDecCategory(b)) return BigDecimal_TYPE;
        if (isBigDecimalType(a) || isBigDecimalType(b)) return BigDecimal_TYPE;
        if (isBigIntegerType(a) || isBigIntegerType(b)) {
            if (isBigIntCategory(a) && isBigIntCategory(b)) return BigInteger_TYPE;
            return BigDecimal_TYPE;
        }
        if (isPrimitiveDouble(a) || isPrimitiveDouble(b)) return double_TYPE;
        if (isWrapperDouble(a) || isWrapperDouble(b)) return Double_TYPE;
        if (isPrimitiveFloat(a) || isPrimitiveFloat(b)) return float_TYPE;
        if (isWrapperFloat(a) || isWrapperFloat(b)) return Float_TYPE;
        if (isPrimitiveLong(a) || isPrimitiveLong(b)) return long_TYPE;
        if (isWrapperLong(a) || isWrapperLong(b)) return Long_TYPE;
        if (isPrimitiveInt(a) || isPrimitiveInt(b)) return int_TYPE;
        if (isWrapperInteger(a) || isWrapperInteger(b)) return Integer_TYPE;
        if (isPrimitiveShort(a) || isPrimitiveShort(b)) return short_TYPE;
        if (isWrapperShort(a) || isWrapperShort(b)) return Short_TYPE;
        if (isPrimitiveByte(a) || isPrimitiveByte(b)) return byte_TYPE;
        if (isWrapperByte(a) || isWrapperByte(b)) return Byte_TYPE;
        if (isPrimitiveChar(a) || isPrimitiveChar(b)) return char_TYPE;
        if (isWrapperCharacter(a) || isWrapperCharacter(b)) return Character_TYPE;
        return Number_TYPE;
    }

    protected ClassNode inferComponentType(final ClassNode receiverType, final ClassNode subscriptType) {
        ClassNode componentType = null;
        if (receiverType.isArray()) { // GROOVY-11335
            componentType = receiverType.getComponentType();
        } else {
            MethodCallExpression mce;
            if (subscriptType != null) { // GROOVY-5521: check for a suitable "getAt(T)" method
                mce = callX(varX("#", receiverType), "getAt", varX("selector", subscriptType));
            } else { // GROOVY-8133: check for an "iterator()" method
                mce = callX(varX("#", receiverType), "iterator");
            }
            mce.setImplicitThis(false); // GROOVY-8943

            typeCheckingContext.pushErrorCollector();
            try {
                visitMethodCallExpression(mce);
            } finally {
                typeCheckingContext.popErrorCollector();
            }

            if (subscriptType != null) {
                componentType = getType(mce);
            } else {
                ClassNode iteratorType = getType(mce);
                if (isOrImplements(iteratorType, Iterator_TYPE) && (iteratorType.getGenericsTypes() != null
                        // ignore the iterator(Object) extension method, since it makes *everything* appear iterable
                        || !mce.<MethodNode>getNodeMetaData(DIRECT_METHOD_CALL_TARGET).getDeclaringClass().equals(OBJECT_TYPE))) {
                    componentType = Optional.ofNullable(iteratorType.getGenericsTypes()).map(gt -> getCombinedBoundType(gt[0])).orElse(OBJECT_TYPE);
                }
            }
        }
        return componentType;
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

    private List<MethodNode> disambiguateMethods(List<MethodNode> methods, final ClassNode receiver, final ClassNode[] arguments, final Expression call) {
        if (methods.size() > 1 && receiver != null && arguments != null) {
            List<MethodNode> filteredWithGenerics = new LinkedList<>();
            for (MethodNode method : methods) {
                if (typeCheckMethodsWithGenerics(receiver, arguments, method)
                        && (method.getModifiers() & Opcodes.ACC_BRIDGE) == 0) {
                    filteredWithGenerics.add(method);
                }
            }
            if (filteredWithGenerics.size() == 1) {
                return filteredWithGenerics;
            }

            methods = extension.handleAmbiguousMethods(methods, call);
        }

        if (methods.size() > 1 && call instanceof MethodCall) {
            String methodName = ((MethodCall) call).getMethodAsString();
            methods = methods.stream().filter(m -> m.getName().equals(methodName)).collect(Collectors.toList());
        }

        return methods;
    }

    protected static String prettyPrintMethodList(final List<MethodNode> nodes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0, n = nodes.size(); i < n; i += 1) {
            MethodNode node = nodes.get(i);
            sb.append(prettyPrintType(node.getReturnType()));
            sb.append(" ");
            sb.append(prettyPrintTypeName(node.getDeclaringClass()));
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
                if (!isDefaultExtension(method)) {
                    category = false;
                    break;
                }
            }
        }
        return category;
    }

    /**
     * Returns methods defined for the specified receiver and adds "non-existing"
     * methods that will be generated afterwards by the compiler; for example if
     * a method is using default values and the class node is not compiled yet.
     *
     * @param receiver the type to search for methods
     * @param name     the name of the methods to return
     * @return the methods that are defined on the receiver completed with stubs for future methods
     */
    protected List<MethodNode> findMethodsWithGenerated(final ClassNode receiver, final String name) {
        if (receiver.isArray()) {
            if (name.equals("clone")) { // GROOVY-10319: array clone -- <https://docs.oracle.com/javase/specs/jls/se8/html/jls-10.html#jls-10.7>
                MethodNode clone = new MethodNode("clone", Opcodes.ACC_PUBLIC, OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null);
                clone.setDeclaringClass(OBJECT_TYPE); // retain Object for declaringClass and returnType
                clone.setNodeMetaData(INFERRED_RETURN_TYPE, receiver);
                return Collections.singletonList(clone);
            } else {
                return OBJECT_TYPE.getMethods(name);
            }
        }

        List<MethodNode> methods = receiver.getMethods(name);

        // GROOVY-5166, GROOVY-9890, GROOVY-10700: non-static interface/trait methods
        Set<ClassNode> done = new HashSet<>();
        for (ClassNode next = receiver; next != null; next = next.getSuperClass()) {
            done.add(next);
            for (ClassNode face : next.getAllInterfaces()) {
                if (done.add(face)) {
                    for (MethodNode mn : face.getDeclaredMethods(name)) {
                        if (mn.isPublic() && !mn.isStatic()) methods.add(mn);
                    }
                }
            }
        }

        if (receiver.isInterface()) {
            methods.addAll(OBJECT_TYPE.getMethods(name));
        }

        if (!receiver.isResolved() && !methods.isEmpty()) {
            methods = withDefaultArgumentMethods(methods);
        }

        return methods;
    }

    protected List<MethodNode> findMethod(ClassNode receiver, final String name, final ClassNode... args) {
        if (isPrimitiveType(receiver)) receiver = getWrapper(receiver);

        List<MethodNode> methods;
        if ("<init>".equals(name) && !receiver.isInterface()) {
            methods = withDefaultArgumentMethods(receiver.getDeclaredConstructors());
            if (methods.isEmpty()) {
                MethodNode node = new ConstructorNode(Opcodes.ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                node.setDeclaringClass(receiver);
                methods.add(node);
                if (receiver.isArray()) {
                    // No need to check the arguments against an array constructor: it just needs to exist. The array is
                    // created through coercion or by specifying its dimension(s), anyway, and would not match an
                    // arbitrary number of parameters.
                    return methods;
                }
            }
        } else {
            methods = findMethodsWithGenerated(receiver, name);
            if ("call".equals(name) && receiver.isInterface()) {
                MethodNode sam = findSAM(receiver);
                if (sam != null) {
                    MethodNode callMethod = new MethodNode("call", sam.getModifiers(), sam.getReturnType(), sam.getParameters(), sam.getExceptions(), sam.getCode());
                    callMethod.setDeclaringClass(sam.getDeclaringClass());
                    callMethod.setSourcePosition(sam);
                    methods.add(callMethod);
                }
            }
            if (typeCheckingContext.getEnclosingClassNodes().contains(receiver)) {
                boolean staticOnly = Modifier.isStatic(receiver.getModifiers());
                for (ClassNode outer = receiver; (outer = outer.getOuterClass()) != null;
                        staticOnly = staticOnly || Modifier.isStatic(outer.getModifiers())) {
                    methods.addAll(allowStaticAccessToMember(findMethodsWithGenerated(outer, name), staticOnly));
                }
            }
            if (args == null || args.length == 0) {
                // check for property accessor
                String pname = extractPropertyNameFromMethodName("get", name);
                if (pname == null) {
                    pname = extractPropertyNameFromMethodName("is", name);
                }
                PropertyNode property = null;
                if (pname != null) {
                    property = findProperty(receiver, pname);
                } else {
                    out: // look for property via getGetterName() for non-canonical case
                    for (ClassNode cn = receiver; cn != null; cn = cn.getSuperClass()) {
                        for (PropertyNode pn : cn.getProperties()) {
                            if (name.equals(pn.getGetterName())) {
                                property = pn;
                                break out;
                            }
                        }
                    }
                }
                if (property != null && property.getDeclaringClass().getGetterMethod(name) == null) {
                    MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC | (property.isStatic() ? Opcodes.ACC_STATIC : 0),
                            property.getOriginType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                    node.setDeclaringClass(property.getDeclaringClass());
                    node.setSynthetic(true);
                    methods.add(node);
                }
            } else if (args.length == 1 && (methods.isEmpty()
                    || methods.stream().allMatch(MethodNode::isAbstract))) { // GROOVY-10922
                // check for property mutator
                String pname = extractPropertyNameFromMethodName("set", name);
                if (pname != null) {
                    PropertyNode property = findProperty(receiver, pname);
                    if (property != null && !property.isFinal()) {
                        ClassNode type = property.getOriginType();
                        if (implementsInterfaceOrIsSubclassOf(wrapTypeIfNecessary(args[0]), wrapTypeIfNecessary(type))) {
                            MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC | (property.isStatic() ? Opcodes.ACC_STATIC : 0),
                                    VOID_TYPE, new Parameter[]{new Parameter(type, name)}, ClassNode.EMPTY_ARRAY, GENERATED_EMPTY_STATEMENT);
                            node.setDeclaringClass(property.getDeclaringClass());
                            node.setSynthetic(true);
                            methods.add(node);
                        }
                    }
                }
            }
        }

        if (!name.endsWith("init>")) { // also search for extension methods
            methods.addAll(findDGMMethodsForClassNode(getSourceUnit().getClassLoader(), receiver, name));
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

        if (isClassClassNodeWrappingConcreteType(receiver)) { // GROOVY-6802, GROOVY-6803, GROOVY-9415
            List<MethodNode> result = findMethod(receiver.getGenericsTypes()[0].getType(), name, args);
            result = allowStaticAccessToMember(result, true); // GROOVY-11427
            if (!result.isEmpty()) return result;
        }

        if (isGStringType(receiver)) {
            return findMethod(STRING_TYPE, name, args);
        }

        return EMPTY_METHODNODE_LIST;
    }

    private PropertyNode findProperty(final ClassNode receiver, final String name) {
        for (ClassNode cn = receiver; cn != null; cn = cn.getSuperClass()) {
            PropertyNode property = cn.getProperty(name);
            if (property != null) return property;

            if (!cn.isStaticClass() && cn.getOuterClass() != null
                    && typeCheckingContext.getEnclosingClassNodes().contains(cn)) {
                ClassNode outer = cn.getOuterClass();
                do {
                    property = outer.getProperty(name);
                    if (property != null) return property;
                } while (!outer.isStaticClass() && (outer = outer.getOuterClass()) != null);
            }
        }
        return null;
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

    protected ClassNode getType(final ASTNode node) {
        ClassNode type = node.getNodeMetaData(INFERRED_TYPE);
        if (type != null) {
            return type;
        }

        if (node instanceof ClassExpression) {
            type = ((ClassExpression) node).getType();
            return makeClassSafe0(CLASS_Type, new GenericsType(type));
        }

        if (node instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) node;
            type = isTraitSelf(vexp);
            if (type != null) return makeSelf(type);
            if (vexp.isThisExpression()) return makeThis();
            if (vexp.isSuperExpression()) return makeSuper();

            Variable variable = vexp.getAccessedVariable();
            if (variable instanceof FieldNode) {
                FieldNode fieldNode = (FieldNode) variable;
                ClassNode fieldType = fieldNode.getOriginType();
                if (!fieldNode.isStatic() && GenericsUtils.hasUnresolvedGenerics(fieldType)) {
                    ClassNode declType = fieldNode.getDeclaringClass(), thisType = typeCheckingContext.getEnclosingClassNode();
                    fieldType = resolveGenericsWithContext(extractPlaceHolders(thisType, declType), fieldType);
                }
                return fieldType;
            }
            if (variable != vexp && variable instanceof VariableExpression) {
                return getType((VariableExpression) variable);
            }
            if (variable instanceof Parameter) {
                Parameter parameter = (Parameter) variable;
                // check if param part of control structure - but not if inside instanceof
                List<ClassNode> temporaryTypesForExpression = getTemporaryTypesForExpression(vexp);
                if (temporaryTypesForExpression == null || temporaryTypesForExpression.isEmpty()) {
                    type = typeCheckingContext.controlStructureVariables.get(parameter);
                }
                // now check for closure override
                if (type == null && temporaryTypesForExpression == null) {
                    type = getTypeFromClosureArguments(parameter);
                }
                if (type != null) {
                    storeType(vexp, type);
                    return type;
                }
                return getType((Parameter) variable);
            }
            return vexp.getOriginType();
        }

        if (node instanceof Parameter || node instanceof FieldNode || node instanceof PropertyNode) {
            return ((Variable) node).getOriginType();
        }

        if (node instanceof MethodNode) {
            type = ((MethodNode) node).getReturnType();
            return Optional.ofNullable(getInferredReturnType(node)).orElse(type);
        }

        if (node instanceof MethodCall) {
            if (node instanceof ConstructorCallExpression) {
                return ((ConstructorCallExpression) node).getType();
            }
            MethodNode target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (target != null) {
                return getType(target);
            }
        }

        if (node instanceof ClosureExpression) {
            type = CLOSURE_TYPE.getPlainNodeReference();
            ClassNode returnType = getInferredReturnType(node);
            if (returnType != null) {
                type.setGenericsTypes(new GenericsType[]{
                    new GenericsType(wrapTypeIfNecessary(returnType))
                });
            }
            Parameter[] parameters = ((ClosureExpression) node).getParameters();
            int nParameters = parameters == null ? 0
               : parameters.length == 0 ? -1 : parameters.length;
            type.putNodeMetaData(CLOSURE_ARGUMENTS, nParameters);
            return type;
        }

        if (node instanceof ListExpression) {
            return inferListExpressionType((ListExpression) node);
        }

        if (node instanceof MapExpression) {
            return inferMapExpressionType((MapExpression) node);
        }

        if (node instanceof RangeExpression) {
            RangeExpression re = (RangeExpression) node;
            ClassNode fromType = getType(re.getFrom());
            ClassNode toType = getType(re.getTo());
            if (fromType.equals(toType)) {
                type = wrapTypeIfNecessary(fromType);
            } else {
                type = wrapTypeIfNecessary(lowestUpperBound(fromType, toType));
            }
            return makeClassSafe0(RANGE_TYPE, new GenericsType(type));
        }

        if (node instanceof SpreadExpression) {
            type = getType(((SpreadExpression) node).getExpression());
            return inferComponentType(type, null); // for list literal
        }

        if (node instanceof UnaryPlusExpression) {
            return getType(((UnaryPlusExpression) node).getExpression());
        }

        if (node instanceof UnaryMinusExpression) {
            return getType(((UnaryMinusExpression) node).getExpression());
        }

        if (node instanceof BitwiseNegationExpression) {
            return getType(((BitwiseNegationExpression) node).getExpression());
        }

        return ((Expression) node).getType();
    }

    private ClassNode getTypeFromClosureArguments(final Parameter parameter) {
        for (TypeCheckingContext.EnclosingClosure enclosingClosure : typeCheckingContext.getEnclosingClosureStack()) {
            ClosureExpression closureExpression = enclosingClosure.getClosureExpression();
            ClassNode[] closureParamTypes = closureExpression.getNodeMetaData(CLOSURE_ARGUMENTS);
            if (closureParamTypes != null) {
                Parameter[] parameters = closureExpression.getParameters();
                if (parameters != null) {
                    final int n = parameters.length;
                    String parameterName = parameter.getName();
                    if (n == 0 && parameterName.equals("it")) {
                        return closureParamTypes.length > 0 ? closureParamTypes[0] : null;
                    }
                    for (int i = 0; i < n; i += 1) {
                        if (parameterName.equals(parameters[i].getName())) {
                            return closureParamTypes.length > i ? closureParamTypes[i] : null;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ClassNode makeSelf(final ClassNode trait) {
        Set<ClassNode> selfTypes = Traits.collectSelfTypes(trait, new LinkedHashSet<>());
        if (!selfTypes.isEmpty()) { // TODO: reduce to the most-specific type(s)
            ClassNode superclass = selfTypes.stream().filter(t -> !t.isInterface()).findFirst().orElse(OBJECT_TYPE);
            selfTypes.remove(superclass); selfTypes.add(trait);
            return new WideningCategories.LowestUpperBoundClassNode("TypesOf$" + trait.getNameWithoutPackage(), superclass, selfTypes.toArray(ClassNode.EMPTY_ARRAY));
        }
        return trait;
    }

    private ClassNode makeSuper() {
        return makeType(typeCheckingContext.getEnclosingClassNode().getUnresolvedSuperClass(), typeCheckingContext.isInStaticContext);
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
     * Stores the inferred return type of a closure or method.  We are using a
     * separate key to store inferred return type because the inferred type of
     * a closure is {@link Closure}, which is different from the inferred type
     * of the code of the closure.
     *
     * @param node a {@link ClosureExpression} or {@link MethodNode}
     * @param type the inferred return type of the code
     * @return The old value of the inferred type.
     */
    protected ClassNode storeInferredReturnType(final ASTNode node, final ClassNode type) {
        if (node instanceof ClosureExpression) {
            if (node.getNodeMetaData(INFERRED_TYPE) != null) {
                return getInferredReturnType(node); // GROOVY-11079
            } else {
                return (ClassNode) node.putNodeMetaData(INFERRED_RETURN_TYPE, type);
            }
        }
        throw new IllegalArgumentException("Storing inferred return type is only allowed on closures but found " + node.getClass());
    }

    /**
     * Returns the inferred return type of a closure or method, if stored on the
     * AST node. This method doesn't perform any type inference by itself.
     *
     * @param node a {@link ClosureExpression} or {@link MethodNode}
     * @return The expected return type.
     */
    protected ClassNode getInferredReturnType(final ASTNode node) {
        return node.getNodeMetaData(INFERRED_RETURN_TYPE);
    }

    protected static boolean isNullConstant(final Expression expression) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).isNullExpression();
    }

    protected static boolean isThisExpression(final Expression expression) {
        return expression instanceof VariableExpression && ((VariableExpression) expression).isThisExpression();
    }

    protected static boolean isSuperExpression(final Expression expression) {
        if (expression instanceof PropertyExpression) { // GROOVY-11256
            return "super".equals(((PropertyExpression) expression).getPropertyAsString())
                && ((PropertyExpression) expression).getObjectExpression() instanceof ClassExpression;
        }
        return expression instanceof VariableExpression && ((VariableExpression) expression).isSuperExpression();
    }

    protected ClassNode inferListExpressionType(final ListExpression list) {
        ClassNode listType = list.getType();

        GenericsType[] genericsTypes = listType.getGenericsTypes();
        if (!asBoolean(genericsTypes)
                || (genericsTypes.length == 1 && genericsTypes[0].isPlaceholder())) {
            // maybe infer the element type
            List<ClassNode> expressionTypes = list.getExpressions().stream()
                .filter(e -> !isNullConstant(e)).map(this::getType).collect(Collectors.toList());
            if (!expressionTypes.isEmpty()) {
                ClassNode subType = lowestUpperBound(expressionTypes);
                genericsTypes = new GenericsType[]{new GenericsType(wrapTypeIfNecessary(subType))};
            } else { // GROOVY-11028
                GenericsType[] typeVars = ArrayList_TYPE.getGenericsTypes();
                Map<GenericsTypeName, GenericsType> spec = extractGenericsConnectionsFromArguments(
                    typeVars, Parameter.EMPTY_ARRAY, ArgumentListExpression.EMPTY_ARGUMENTS, null);
                genericsTypes = applyGenericsContext(spec, typeVars);
            }
            listType = ArrayList_TYPE.getPlainNodeReference();
            listType.setGenericsTypes(genericsTypes);
        }

        return listType;
    }

    protected ClassNode inferMapExpressionType(final MapExpression map) {
        ClassNode mapType = map.getType();

        GenericsType[] genericsTypes = mapType.getGenericsTypes();
        if (!asBoolean(genericsTypes)
                || (genericsTypes.length == 2 && genericsTypes[0].isPlaceholder() && genericsTypes[1].isPlaceholder())) {
            // maybe infer the entry type
            List<MapEntryExpression> entryExpressions = map.getMapEntryExpressions();
            int nExpressions = entryExpressions.size();
            if (nExpressions != 0) {
                ClassNode keyType;
                ClassNode valueType;
                List<ClassNode> keyTypes = new ArrayList<>(nExpressions);
                List<ClassNode> valueTypes = new ArrayList<>(nExpressions);
                for (MapEntryExpression entryExpression : entryExpressions) {
                    valueType = getType(entryExpression.getValueExpression());
                    if (!(entryExpression.getKeyExpression() instanceof SpreadMapExpression)) {
                        keyType = getType(entryExpression.getKeyExpression());
                    } else { // GROOVY-7247
                        valueType = GenericsUtils.parameterizeType(valueType, MAP_TYPE);
                        keyType = getCombinedBoundType(valueType.getGenericsTypes()[0]);
                        valueType = getCombinedBoundType(valueType.getGenericsTypes()[1]);
                    }
                    keyTypes.add(keyType);
                    valueTypes.add(valueType); // TODO: skip null value
                }
                keyType = lowestUpperBound(keyTypes);
                valueType = lowestUpperBound(valueTypes);
                genericsTypes = new GenericsType[]{new GenericsType(wrapTypeIfNecessary(keyType)), new GenericsType(wrapTypeIfNecessary(valueType))};
            } else { // GROOVY-11028
                GenericsType[] typeVars = LinkedHashMap_TYPE.getGenericsTypes();
                Map<GenericsTypeName, GenericsType> spec = extractGenericsConnectionsFromArguments(
                    typeVars, Parameter.EMPTY_ARRAY, ArgumentListExpression.EMPTY_ARGUMENTS, null);
                genericsTypes = applyGenericsContext(spec, typeVars);
            }
            mapType = LinkedHashMap_TYPE.getPlainNodeReference();
            mapType.setGenericsTypes(genericsTypes);
        }

        return mapType;
    }

    /**
     * If a method call returns a parameterized type, then perform additional
     * inference on the return type, so that the type gets actual type arguments.
     * For example, the method {@code Arrays.asList(T...)} is parameterized with
     * {@code T}, which can be deduced type arguments or call arguments.
     *
     * @param method            the method node
     * @param arguments         the method call arguments
     * @param receiver          the object expression type
     */
    protected ClassNode inferReturnTypeGenerics(final ClassNode receiver, final MethodNode method, final Expression arguments) {
        return inferReturnTypeGenerics(receiver, method, arguments, null);
    }

    /**
     * If a method call returns a parameterized type, then perform additional
     * inference on the return type, so that the type gets actual type arguments.
     * For example, the method {@code Arrays.asList(T...)} is parameterized with
     * {@code T}, which can be deduced type arguments or call arguments.
     *
     * @param method            the method node
     * @param arguments         the method call arguments
     * @param receiver          the object expression type
     * @param explicitTypeHints type arguments (optional), for example {@code Collections.<String>emptyList()}
     */
    protected ClassNode inferReturnTypeGenerics(final ClassNode receiver, final MethodNode method, final Expression arguments, final GenericsType[] explicitTypeHints) {
        ClassNode returnType = method.isConstructor() ? method.getDeclaringClass() : method.getReturnType();
        if (!GenericsUtils.hasUnresolvedGenerics(returnType)) {
            // GROOVY-7538: replace "Type<?>" with "Type<? extends/super X>" for any "Type<T extends/super X>"
            if (getGenericsWithoutArray(returnType) != null) returnType = boundUnboundedWildcards(returnType);

            return returnType;
        }

        if (method instanceof ExtensionMethodNode) {
            ArgumentListExpression args = getExtensionArguments(receiver, method, arguments);
            MethodNode extension = ((ExtensionMethodNode) method).getExtensionMethodNode();
            return inferReturnTypeGenerics(receiver, extension, args, explicitTypeHints);
        }

        Map<GenericsTypeName, GenericsType> context = method.isStatic() || method.isConstructor()
                                            ? null : extractPlaceHoldersVisibleToDeclaration(receiver, method, arguments);
        GenericsType[] methodGenericTypes = method.isConstructor() ? method.getDeclaringClass().getGenericsTypes() : applyGenericsContext(context, method.getGenericsTypes());

        // 1) resolve type parameters of method

        if (methodGenericTypes != null) {
            Map<GenericsTypeName, GenericsType> resolvedPlaceholders = new HashMap<>();
            for (GenericsType gt : methodGenericTypes) resolvedPlaceholders.put(new GenericsTypeName(gt.getName()), gt);
            applyGenericsConnections(extractGenericsConnectionsFromArguments(methodGenericTypes, Arrays.stream(method.getParameters()).map(param ->
                new Parameter(applyGenericsContext(context, param.getType()), param.getName())
            ).toArray(Parameter[]::new), arguments, explicitTypeHints), resolvedPlaceholders);

            returnType = applyGenericsContext(resolvedPlaceholders, returnType);
        }

        // 2) resolve type parameters of method's enclosing context

        if (context != null) {
            returnType = applyGenericsContext(context, returnType);

            if (receiver.getGenericsTypes() == null && receiver.redirect().getGenericsTypes() != null
                    && !receiver.isGenericsPlaceHolder() && GenericsUtils.hasUnresolvedGenerics(returnType)) {
                returnType = returnType.getPlainNodeReference(); // GROOVY-10049: not "Stream<E>" for raw type
            }
        }

        // 3) resolve bounds of type parameters from calling context

        returnType = applyGenericsContext(extractGenericsParameterMapOfThis(typeCheckingContext), returnType);

        return returnType;
    }

    /**
     * Resolves type parameters declared by method from type or call arguments.
     */
    private Map<GenericsTypeName, GenericsType> extractGenericsConnectionsFromArguments(final GenericsType[] methodGenericTypes, final Parameter[] parameters, final Expression arguments, final GenericsType[] explicitTypeHints) {
        Map<GenericsTypeName, GenericsType> resolvedPlaceholders = new HashMap<>();

        if (asBoolean(explicitTypeHints)) { // resolve type parameters from type arguments
            int n = methodGenericTypes.length;
            if (n == explicitTypeHints.length) {
                for (int i = 0; i < n; i += 1) {
                    resolvedPlaceholders.put(new GenericsTypeName(methodGenericTypes[i].getName()), explicitTypeHints[i]);
                }
            }
        } else if (parameters.length > 0) { // resolve type parameters from call arguments
            List<Expression> expressions = InvocationWriter.makeArgumentList(arguments).getExpressions();
            boolean isVargs = isVargs(parameters);
            int nArguments = expressions.size();
            int nParams = parameters.length;

            if (isVargs ? nArguments >= nParams - 1 : nArguments == nParams) {
                for (int i = 0; i < nArguments; i += 1) {
                    Expression argument = expressions.get(i);
                    if (isNullConstant(argument)) continue; // GROOVY-9984: skip
                    ClassNode argumentType = getDeclaredOrInferredType(argument);
                    ClassNode paramType = parameters[Math.min(i, nParams - 1)].getType();

                    if (GenericsUtils.hasUnresolvedGenerics(paramType)) {
                        // if supplying array param with multiple arguments or single non-array argument, infer using element type
                        if (isVargs && (i >= nParams || (i == nParams - 1 && (nArguments > nParams || !argumentType.isArray())))) {
                            paramType = paramType.getComponentType();
                        }

                        Map<GenericsTypeName, GenericsType> connections = new HashMap<>();

                        if ((argument instanceof ClosureExpression || argument instanceof MethodPointerExpression) && isSAMType(paramType)) {
                            // target type information
                            Tuple2<ClassNode[], ClassNode> samParamsAndReturnType = GenericsUtils.parameterizeSAM(paramType);
                            ClassNode[] q = samParamsAndReturnType.getV1();

                            // source type information
                            ClassNode returnType = isClosureWithType(argumentType)
                                    ? getCombinedBoundType(argumentType.getGenericsTypes()[0])
                                        : wrapTypeIfNecessary(getInferredReturnType(argument));
                            ClassNode[] p;
                            if (argument instanceof ClosureExpression) {
                                ClosureExpression closure = (ClosureExpression) argument;
                                p = extractTypesFromParameters(getParametersSafe(closure));
                            } else { // argument instanceof MethodPointerExpression
                                List<MethodNode> candidates = argument.getNodeMetaData(MethodNode.class);
                                if (candidates != null && !candidates.isEmpty()) {
                                    MethodPointerExpression methodPointer = (MethodPointerExpression) argument;
                                    p = collateMethodReferenceParameterTypes(methodPointer, candidates.get(0));
                                    if (p.length > 0 && GenericsUtils.hasUnresolvedGenerics(returnType)) {
                                        // GROOVY-11241: implicit receiver for "Optional::get" is resolved
                                        if (!candidates.get(0).isStatic() && isClassClassNodeWrappingConcreteType(getType(methodPointer.getExpression()))) {
                                            extractGenericsConnections(connections, q[0], p[0].redirect());
                                        }
                                        for (int j = 0; j < q.length; j += 1) {
                                            // SAM parameters are like arguments in this case
                                            extractGenericsConnections(connections, q[j], p[j]);
                                        }
                                        // convert the method's generics into the SAM's generics
                                        returnType = applyGenericsContext(connections, returnType);
                                        p          = applyGenericsContext(connections, p         );

                                        connections.clear();
                                    }
                                } else {
                                    p = ClassNode.EMPTY_ARRAY;
                                }
                            }

                            // parameters and return type correspond to the SAM's
                            for (int j = 0; j < p.length && j < q.length; j += 1) {
                                if (!isDynamicTyped(p[j]))
                                    // GROOVY-10054, GROOVY-10699, GROOVY-10749, et al.
                                    extractGenericsConnections(connections, wrapTypeIfNecessary(p[j]), q[j]);
                            }
                            extractGenericsConnections(connections, returnType, samParamsAndReturnType.getV2());
                        } else {
                            extractGenericsConnections(connections, wrapTypeIfNecessary(argumentType), paramType);
                        }

                        connections.forEach((gtn, gt) -> resolvedPlaceholders.merge(gtn, gt, (gt1, gt2) -> {
                            // GROOVY-10339: incorporate another witness
                            return getCombinedGenericsType(gt1, gt2);
                        }));
                    }
                }
            }

            // in case of "<T, U extends Type<T>>", we can learn about "T" from a resolved "U"
            extractGenericsConnectionsForBoundTypes(methodGenericTypes, resolvedPlaceholders);
        }

        for (GenericsType gt : methodGenericTypes) {
            // GROOVY-8409, GROOVY-10067, et al.: provide "no type witness" mapping for param
            resolvedPlaceholders.computeIfAbsent(new GenericsTypeName(gt.getName()), gtn -> {
                ClassNode hash = ClassHelper.makeWithoutCaching("#" + gt.getName());
                hash.setRedirect(getCombinedBoundType(gt));
                hash.setGenericsPlaceHolder(true);

                GenericsType gtx = new GenericsType(hash, applyGenericsContext(resolvedPlaceholders, gt.getUpperBounds()), null);
                gtx.putNodeMetaData(GenericsType.class, gt);
                gtx.setResolved(true);
                return gtx;
            });
        }

        return resolvedPlaceholders;
    }

    /**
     * Given method call like "m(Collections.emptyList())", the type of the call
     * argument is {@code List<T>} without explicit type arguments. Now, knowing
     * the method target of "m", {@code T} could be resolved.
     */
    private void resolvePlaceholdersFromImplicitTypeHints(final ClassNode[] actuals, final ArgumentListExpression argumentList, final Parameter[] parameterArray) {
        int np = parameterArray.length;
        for (int i = 0, n = actuals.length; np > 0 && i < n; i += 1) {
            Parameter p = parameterArray[Math.min(i, np - 1)];
            ClassNode pt = p.getOriginType(), at = actuals[i];
            if (!isUsingGenericsOrIsArrayUsingGenerics(pt)) continue;
            if (i >= (np - 1) && pt.isArray() && !at.isArray()) pt = pt.getComponentType();

            var a = argumentList.getExpression(i);

            if (a instanceof ListExpression) {
                actuals[i] = getLiteralResultType(pt, at, ArrayList_TYPE);
            } else if (a instanceof MapExpression) {
                actuals[i] = getLiteralResultType(pt, at, LinkedHashMap_TYPE);
            } else if (a instanceof ConstructorCallExpression) {
                inferDiamondType((ConstructorCallExpression) a, pt); // GROOVY-8974, GROOVY-9983, GROOVY-10086, GROOVY-10890, et al.
            } else if (a instanceof TernaryExpression && at.getGenericsTypes() != null && at.getGenericsTypes().length == 0) {
                // GROOVY-9983: double diamond scenario -- "m(flag ? new Type<>(...) : new Type<>(...))"
                typeCheckingContext.pushEnclosingBinaryExpression(assignX(varX(p), a, a));
                a.visit(this); // re-visit with target type witness
                typeCheckingContext.popEnclosingBinaryExpression();
                actuals[i] = getType(a);
            }

            // check for method call without type arguments, with a known target
            if (!(a instanceof MethodCall) || (a instanceof MethodCallExpression
                    && ((MethodCallExpression) a).isUsingGenerics())) continue;
            MethodNode aNode = a.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
            if (aNode == null || aNode.getGenericsTypes() == null) continue;

            // and unknown generics
            if (!GenericsUtils.hasUnresolvedGenerics(at)) continue;

            while (!at.equals(pt) && !isObjectType(at) && !isGenericsPlaceHolderOrArrayOf(at) && !isGenericsPlaceHolderOrArrayOf(pt)) {
                at = applyGenericsContext(GenericsUtils.extractPlaceholders(at), getNextSuperClass(at, pt));
            }

            // try to resolve placeholder(s) in argument type using parameter type

            Map<GenericsTypeName, GenericsType> linked = new HashMap<>();
            Map<GenericsTypeName, GenericsType> source = GenericsUtils.extractPlaceholders(at);
            Map<GenericsTypeName, GenericsType> target = GenericsUtils.extractPlaceholders(pt);

            if (at.isGenericsPlaceHolder()) // GROOVY-10482: call argument via "def <T> T m()"
                target.put(new GenericsTypeName(at.getUnresolvedName()), pt.asGenericsType());

            // connect E:T from source to E:Type from target
            for (GenericsType placeholder : aNode.getGenericsTypes()) {
                for (Map.Entry<GenericsTypeName, GenericsType> e : source.entrySet()) {
                    if (e.getValue().getNodeMetaData(GenericsType.class) == placeholder) {
                        Optional.ofNullable(target.get(e.getKey()))
                            // skip "f(g())" for "f(T<String>)" and "<U extends Number> U g()"
                            .filter(gt -> isAssignableTo(gt.getType(), placeholder.getType()))
                            .ifPresent(gt -> linked.put(new GenericsTypeName(e.getValue().getName()), gt));
                        break;
                    }
                }
            }

            actuals[i] = applyGenericsContext(linked, at);
        }
    }

    private static void extractGenericsConnectionsForBoundTypes(final GenericsType[] spec, final Map<GenericsTypeName, GenericsType> target) {
        if (spec.length < 2) return;
        Map<GenericsTypeName, GenericsType> outer = new HashMap<>();
        for (GenericsType tp : spec) {
            ClassNode[] bounds = tp.getUpperBounds();
            if (bounds == null || bounds.length == 0) continue;

            GenericsTypeName key = new GenericsTypeName(tp.getName());
            GenericsType value = target.get(key); // look for specific resolved type
            if (value == null || value.isPlaceholder() || value.isWildcard()) continue;

            Map<GenericsTypeName, GenericsType> inner = new HashMap<>();
            for (ClassNode bound : bounds) {
                extractGenericsConnections(inner,value.getType(),bound);
            }
            inner.forEach((k, v) -> outer.merge(k, v, StaticTypeCheckingSupport::getCombinedGenericsType)); // GROOVY-5893
        }
        outer.forEach(target::putIfAbsent);
    }

    private static ClassNode[] collateMethodReferenceParameterTypes(final MethodPointerExpression source, final MethodNode target) {
        Parameter[] params;

        if (target instanceof ExtensionMethodNode && !((ExtensionMethodNode) target).isStaticExtension()) {
            params = ((ExtensionMethodNode) target).getExtensionMethodNode().getParameters();
        } else if (!target.isStatic() && source.getExpression() instanceof ClassExpression) {
            ClassNode thisType = ((ClassExpression) source.getExpression()).getType();
            // there is an implicit parameter for "String::length"
            int n = target.getParameters().length;
            params = new Parameter[n + 1];
            params[0] = new Parameter(thisType, "");
            System.arraycopy(target.getParameters(), 0, params, 1, n);
        } else {
            params = target.getParameters();
            // GROOVY-10974, GROOVY-10975: propagate type args
            if (!target.isStatic() && target.getDeclaringClass().getGenericsTypes() != null) {
                ClassNode objectExprType = source.getExpression().getNodeMetaData(INFERRED_TYPE);
                if (objectExprType == null && source.getExpression() instanceof VariableExpression) {
                    Variable variable = ((VariableExpression) source.getExpression()).getAccessedVariable();
                    if (variable instanceof ASTNode) objectExprType = ((ASTNode) variable).getNodeMetaData(INFERRED_TYPE);
                }
                if (objectExprType == null) objectExprType = source.getExpression().getType();
                var spec = extractPlaceHolders(objectExprType, target.getDeclaringClass());
                return applyGenericsContext(spec, extractTypesFromParameters(params));
            }
        }

        return extractTypesFromParameters(params);
    }

    private ClassNode getDeclaredOrInferredType(final Expression expression) {
        ClassNode declaredOrInferred;
        // in case of "T t = new ExtendsOrImplementsT()", return T for the expression type
        if (expression instanceof Variable && !((Variable) expression).isDynamicTyped()) {
            declaredOrInferred = getOriginalDeclarationType(expression); // GROOVY-9996
        } else {
            declaredOrInferred = getType(expression);
        }
        // GROOVY-10011: apply instanceof constraints to either option
        return getInferredTypeFromTempInfo(expression, declaredOrInferred);
    }

    private static class ExtensionMethodDeclaringClass {
    }

    private static ArgumentListExpression getExtensionArguments(final ClassNode receiver, final MethodNode method, final Expression arguments) {
        VariableExpression self = varX("$self", receiver); // implicit first argument
        self.putNodeMetaData(ExtensionMethodDeclaringClass.class, method.getDeclaringClass());

        ArgumentListExpression args = new ArgumentListExpression();
        args.addExpression(self);
        if (arguments instanceof TupleExpression) {
            for (Expression argument : (TupleExpression) arguments) {
                args.addExpression(argument);
            }
        } else {
            args.addExpression(arguments);
        }
        return args;
    }

    private static boolean isDefaultExtension(final MethodNode method) {
        return method instanceof ExtensionMethodNode && ((ExtensionMethodNode) method).getExtensionMethodNode().getDeclaringClass().equals(DGM_CLASSNODE);
    }

    private static boolean isGenericsPlaceHolderOrArrayOf(ClassNode cn) {
        while (cn.isArray()) cn = cn.getComponentType();
        return cn.isGenericsPlaceHolder();
    }

    private static Map<GenericsTypeName, GenericsType> extractPlaceHolders(final ClassNode receiver, final ClassNode declaringClass) {
        Map<GenericsTypeName, GenericsType> result = null;
        ClassNode[] todo;
        if (receiver instanceof UnionTypeClassNode) {
            todo = ((UnionTypeClassNode) receiver).getDelegates();
        } else {
            todo = new ClassNode[] {!isPrimitiveType(declaringClass) ? wrapTypeIfNecessary(receiver) : receiver};
        }
        for (ClassNode type : todo) {
            for (ClassNode current = type; current != null; ) {
                Map<GenericsTypeName, GenericsType> placeHolders = new HashMap<>();
                if (current.isGenericsPlaceHolder())
                    // GROOVY-10622: type param bound "T extends Set<Type>"
                    current = current.asGenericsType().getUpperBounds()[0];
                // GROOVY-10055: handle diamond or raw type
                else if (current.getGenericsTypes() != null
                        ? current.getGenericsTypes().length == 0
                        : current.redirect().getGenericsTypes() != null) {
                    for (GenericsType gt : current.redirect().getGenericsTypes()) {
                        ClassNode cn = gt.getUpperBounds() != null ? gt.getUpperBounds()[0] : gt.getType().redirect();
                        placeHolders.put(new GenericsTypeName(gt.getName()), cn.getPlainNodeReference().asGenericsType());
                    }
                }

                boolean currentIsDeclaring = current.equals(declaringClass) || isGenericsPlaceHolderOrArrayOf(declaringClass);
                if (currentIsDeclaring) {
                    extractGenericsConnections(placeHolders, current, declaringClass);
                } else {
                    GenericsUtils.extractPlaceholders(current, placeHolders);
                }

                if (result != null) { // merge maps
                    for (Map.Entry<GenericsTypeName, GenericsType> entry : placeHolders.entrySet()) {
                        GenericsType gt = entry.getValue();
                        if (!gt.isPlaceholder()) continue;
                        GenericsType referenced = result.get(new GenericsTypeName(gt.getName()));
                        if (referenced == null) continue;
                        entry.setValue(referenced);
                    }
                }
                result = placeHolders;

                // we are done if we are now in the declaring class
                if (currentIsDeclaring) break;

                current = getNextSuperClass(current, declaringClass);
                if (current == null && isClassType(declaringClass)) {
                    // this can happen if the receiver is Class<Foo>, then
                    // the actual receiver is Foo and declaringClass is Class
                    current = declaringClass;
                } else {
                    current = applyGenericsContext(placeHolders, current);
                }
            }
        }
        if (result == null) {
            throw new GroovyBugError("Declaring class " + prettyPrintTypeName(declaringClass) + " was not matched with receiver " + prettyPrintTypeName(receiver) + ". This should not have happened!");
        }
        return result;
    }

    private static Map<GenericsTypeName, GenericsType> extractPlaceHoldersVisibleToDeclaration(final ClassNode receiver, final MethodNode method, final Expression argument) {
        Map<GenericsTypeName, GenericsType> result;
        if (method.isStatic() || (method.isConstructor() && !asBoolean(receiver.getGenericsTypes()))) {
            result = new HashMap<>();
        } else {
            ClassNode declaring = method.getDeclaringClass();
            if (argument instanceof TupleExpression) { // resolve extension method class
                List<Expression> arguments = ((TupleExpression) argument).getExpressions();
                if (!arguments.isEmpty()) {
                    ClassNode cn = arguments.get(0).getNodeMetaData(ExtensionMethodDeclaringClass.class);
                    if (cn != null)
                        declaring = cn;
                }
            }
            result = extractPlaceHolders(receiver, declaring);
            if (!result.isEmpty()) Optional.ofNullable(method.getGenericsTypes()).ifPresent(methodGenerics ->
                Arrays.stream(methodGenerics).map(gt -> new GenericsTypeName(gt.getName())).forEach(result::remove)); // GROOVY-10322
        }
        return result;
    }

    protected boolean typeCheckMethodsWithGenericsOrFail(final ClassNode receiver, final ClassNode[] arguments, final MethodNode candidateMethod, final Expression location) {
        if (!typeCheckMethodsWithGenerics(receiver, arguments, candidateMethod)) {
            ClassNode r = receiver, at[] = arguments; MethodNode m = candidateMethod;
            if (candidateMethod instanceof ExtensionMethodNode) {
                m = ((ExtensionMethodNode) candidateMethod).getExtensionMethodNode();
                r = m.getDeclaringClass();
                at = new ClassNode[arguments.length + 1];
                at[0] = receiver; // object expression is first argument
                System.arraycopy(arguments, 0, at, 1, arguments.length);
            } else {
                r = wrapTypeIfNecessary(r); // GROOVY-11383
            }
            Map<GenericsTypeName, GenericsType> spec = extractPlaceHoldersVisibleToDeclaration(r, m, null);
            GenericsType[] gt = applyGenericsContext(spec, m.getGenericsTypes()); // class params in bounds
            GenericsUtils.extractPlaceholders(makeClassSafe0(OBJECT_TYPE, gt), spec);

            Parameter[] parameters = m.getParameters();
            ClassNode[] paramTypes = new ClassNode[parameters.length];
            for (int i = 0, n = parameters.length; i < n; i += 1) {
                paramTypes[i] = fullyResolveType(parameters[i].getType(), spec);
                // GROOVY-10010: check for List<String> parameter and ["foo","$bar"] argument
                if (i < at.length && hasGStringStringError(paramTypes[i], at[i], location)) {
                    return false;
                }
            }

            addStaticTypeError("Cannot call " + (gt == null ? "" : GenericsUtils.toGenericTypesString(gt)) +
                    prettyPrintTypeName(r) + "#" + toMethodParametersString(m.getName(), paramTypes) + " with arguments " + formatArgumentList(at), location);
            return false;
        }
        return true;
    }

    protected static String formatArgumentList(final ClassNode[] nodes) {
        if (nodes == null || nodes.length == 0) return "[]";

        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (ClassNode node : nodes) {
            joiner.add(prettyPrintType(node));
        }
        return joiner.toString();
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
    public void addError(final String msg, final ASTNode node) {
        Long err = ((long) node.getLineNumber()) << 16 + node.getColumnNumber();
        if ((DEBUG_GENERATED_CODE && node.getLineNumber() < 0) || !typeCheckingContext.reportedErrors.contains(err)) {
            typeCheckingContext.getErrorCollector().addErrorAndContinue(msg + '\n', node, getSourceUnit());
            typeCheckingContext.reportedErrors.add(err);
        }
    }

    protected void addStaticTypeError(final String msg, final ASTNode node) {
        if (node.getColumnNumber() > 0 && node.getLineNumber() > 0) {
            addError(StaticTypesTransformation.STATIC_ERROR_PREFIX + msg, node);
        } else {
            if (DEBUG_GENERATED_CODE) {
                addError(StaticTypesTransformation.STATIC_ERROR_PREFIX + "Error in generated code [" + node.getText() + "] - " + msg, node);
            }
            // ignore errors which are related to unknown source locations
            // because they are likely related to generated code
        }
    }

    protected void addNoMatchingMethodError(final ClassNode receiver, final String name, ClassNode[] args, final Expression exp) {
        addNoMatchingMethodError(receiver, name, args, (ASTNode)exp);
    }

    protected void addNoMatchingMethodError(final ClassNode receiver, final String name, ClassNode[] args, final ASTNode origin) {
        String classifier;
        String descriptor;
        if ("<init>".equals(name)) {
            classifier = "constructor";
            // remove implicit agruments [String, int] from enum constant construction
            if (receiver.isEnum() && args.length >= 2) args = Arrays.copyOfRange(args, 2, args.length);
            descriptor = prettyPrintTypeName(receiver) + toMethodParametersString("", args); // type is name
        } else {
            classifier = "method";
            descriptor = prettyPrintTypeName(wrapTypeIfNecessary(receiver)) + "#" + toMethodParametersString(name, args);
            if (isClassClassNodeWrappingConcreteType(receiver)) { ClassNode t = receiver.getGenericsTypes()[0].getType();
                descriptor += " or static method " + prettyPrintTypeName(t) + "#" + toMethodParametersString(name, args);
            }
        }
        addStaticTypeError("Cannot find matching " + classifier + " " + descriptor + ". Please check if the declared type is correct and if the method exists.", origin);
    }

    protected void addAmbiguousErrorMessage(final List<MethodNode> foundMethods, final String name, final ClassNode[] args, final Expression expr) {
        addStaticTypeError("Reference to method is ambiguous. Cannot choose between " + prettyPrintMethodList(foundMethods), expr);
    }

    protected void addCategoryMethodCallError(final Expression call) {
        addStaticTypeError("Due to their dynamic nature, usage of categories is not possible with static type checking active", call);
    }

    protected void addAssignmentError(final ClassNode leftType, final ClassNode rightType, final Expression expression) {
        addStaticTypeError("Cannot assign value of type " + prettyPrintType(rightType) + " to variable of type " + prettyPrintType(leftType), expression);
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
        for (SecondPassExpression<?> wrapper : typeCheckingContext.secondPassExpressions) {
            Expression expression = wrapper.getExpression();
            if (expression instanceof BinaryExpression) {
                Expression left = ((BinaryExpression) expression).getLeftExpression();
                if (left instanceof VariableExpression) {
                    Variable target = findTargetVariable((VariableExpression) left);
                    if (target instanceof VariableExpression) {
                        List<ClassNode> classNodes = typeCheckingContext.closureSharedVariablesAssignmentTypes.get(target);
                        if (classNodes != null && classNodes.size() > 1) {
                            ClassNode type = lowestUpperBound(classNodes);
                            String message = getOperationName(((BinaryExpression) expression).getOperation().getType());
                            if (message != null) {
                                List<MethodNode> methods = findMethod(type, message, getType(((BinaryExpression) expression).getRightExpression()));
                                if (methods.isEmpty()) {
                                    String methSpec = toMethodParametersString(message, getType(((BinaryExpression) expression).getRightExpression()));
                                    String stcError = String.format("The closure shared variable \"%s\" has been assigned with various types and the method %s does not exist in the lowest upper bound of those types: %s", target.getName(), methSpec, prettyPrintTypeName(type));
                                    addStaticTypeError(stcError + ". In general, this style of variable reuse is a bad practice because the compiler cannot determine safely what is the type of the variable at the moment of the call in a multi-threaded context.", expression );
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
                        List<ClassNode> classNodes = typeCheckingContext.closureSharedVariablesAssignmentTypes.get(target);
                        if (classNodes != null && classNodes.size() > 1) {
                            ClassNode type = lowestUpperBound(classNodes);
                            MethodNode mct = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
                            // we must check that such a method exists for the common type(s)
                            List<MethodNode> methods = findMethod(type, mct.getName(), (ClassNode[]) wrapper.getData());
                            if (methods.size() != 1) {
                                String methSpec = toMethodParametersString(mct.getName(), extractTypesFromParameters(mct.getParameters()));
                                String stcError = String.format("The closure shared variable \"%s\" has been assigned with various types and the method %s does not exist in the lowest upper bound of those types: %s", target.getName(), methSpec, prettyPrintTypeName(type));
                                addStaticTypeError(stcError + ". In general, this style of variable reuse is a bad practice because the compiler cannot determine safely what is the type of the variable at the moment of the call in a multi-threaded context.", expression );
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
        return (type != null && isPrimitiveType(type) ? getWrapper(type) : type);
    }

    protected static boolean isClassInnerClassOrEqualTo(final ClassNode toBeChecked, final ClassNode start) {
        if (start == toBeChecked) return true;
        ClassNode outer = start.getOuterClass();
        if (outer != null) {
            return isClassInnerClassOrEqualTo(toBeChecked, outer);
        }
        return false;
    }

    private static boolean isNonStaticHelperMethod(final MethodNode method) {
        Parameter[] parameters = method.getParameters(); // check first param is "$self"
        if (parameters.length > 0 && parameters[0].getName().equals(Traits.THIS_OBJECT)) {
            return !method.getName().contains("$init$") && Traits.isTrait(method.getDeclaringClass().getOuterClass());
        }
        return false;
    }

    private static BinaryExpression assignX(final Expression lhs, final Expression rhs, final ASTNode pos) {
        BinaryExpression exp = (BinaryExpression) GeneralUtils.assignX(lhs, rhs);
        exp.setSourcePosition(pos);
        exp.setSynthetic(true);
        return exp;
    }

    //--------------------------------------------------------------------------
    // temporaryIfBranchTypeInformation support; migrate to TypeCheckingContext?

    /**
     * Stores information about types when [objectOfInstanceof instanceof typeExpression] is visited.
     *
     * @param objectOfInstanceOf the expression to be checked against instanceof
     * @param typeExpression     the expression which represents the target type
     */
    protected void pushInstanceOfTypeInfo(final Expression objectOfInstanceOf, final Expression typeExpression) {
        Object ttiKey = extractTemporaryTypeInfoKey(objectOfInstanceOf); ClassNode type = typeExpression.getType();
        typeCheckingContext.temporaryIfBranchTypeInformation.peek().computeIfAbsent(ttiKey, x -> new LinkedList<>()).add(type);
    }

    private void putNotInstanceOfTypeInfo(final Object key, final Collection<ClassNode> types) {
        Object notKey = key instanceof Object[] ? ((Object[]) key)[1] : new Object[]{"!instanceof", key}; // stash negative type(s)
        typeCheckingContext.temporaryIfBranchTypeInformation.peek().computeIfAbsent(notKey, x -> new LinkedList<>()).addAll(types);
    }

    /**
     * Computes the key to use for {@link TypeCheckingContext#temporaryIfBranchTypeInformation}.
     */
    protected Object extractTemporaryTypeInfoKey(final Expression expression) {
        return expression instanceof VariableExpression ? findTargetVariable((VariableExpression) expression) : expression.getText();
    }

    /**
     * A helper method which determines which receiver class should be used in error messages when a field or attribute
     * is not found. The returned type class depends on whether we have temporary type information available (due to
     * instanceof checks) and whether there is a single candidate in that case.
     *
     * @param expression the expression for which an unknown field has been found
     * @param type the type of the expression (used as fallback type)
     * @return if temporary information is available and there's only one type, returns the temporary type class
     * otherwise falls back to the provided type class.
     */
    protected ClassNode findCurrentInstanceOfClass(final Expression expression, final ClassNode type) {
        List<ClassNode> tempTypes = getTemporaryTypesForExpression(expression);
        if (tempTypes.size() == 1) return tempTypes.get(0);
        return type;
    }

    protected List<ClassNode> getTemporaryTypesForExpression(final Expression expression) {
        Object key = extractTemporaryTypeInfoKey(expression);
        List<ClassNode> tempTypes = typeCheckingContext.temporaryIfBranchTypeInformation.stream().flatMap(tti ->
            tti.getOrDefault(key, Collections.emptyList()).stream()
        ).collect(Collectors.toList());
        int i = tempTypes.lastIndexOf(VOID_TYPE);
        if (i != -1) { // assignment overwrites instanceof
            tempTypes = tempTypes.subList(i + 1, tempTypes.size());
        }
        return DefaultGroovyMethods.unique(tempTypes); // GROOVY-6429
    }

    private ClassNode getInferredTypeFromTempInfo(final Expression expression, final ClassNode expressionType) {
        if (expression instanceof VariableExpression) {
            List<ClassNode> tempTypes = getTemporaryTypesForExpression(expression);
            if (!tempTypes.isEmpty()) {
                ClassNode   superclass;
                ClassNode[] interfaces;
                if (expressionType instanceof WideningCategories.LowestUpperBoundClassNode) {
                    superclass = expressionType.getSuperClass();
                    interfaces = expressionType.getInterfaces();
                } else if (expressionType != null && expressionType.isInterface()) {
                    superclass = OBJECT_TYPE;
                    interfaces = new ClassNode[]{expressionType};
                } else {
                    superclass = expressionType;
                    interfaces = ClassNode.EMPTY_ARRAY;
                }

                List<ClassNode> types = new ArrayList<>();
                if (superclass != null && !superclass.equals(OBJECT_TYPE) // GROOVY-7333
                        && tempTypes.stream().noneMatch(t -> !t.equals(superclass) && t.isDerivedFrom(superclass))) { // GROOVY-9769
                    types.add(superclass);
                }
                for (ClassNode anInterface : interfaces) {
                    if (tempTypes.stream().noneMatch(t -> t.implementsInterface(anInterface))) { // GROOVY-9769
                        types.add(anInterface);
                    }
                }
                int tempTypesCount = tempTypes.size();
                if (tempTypesCount == 1 && types.isEmpty()) {
                    types.add(tempTypes.get(0));
                } else for (ClassNode tempType : tempTypes) {
                    if (!tempType.isInterface() // GROOVY-11290: keep most-specific types
                            ? (superclass == null || !superclass.isDerivedFrom(tempType))
                                    && (tempTypesCount == 1 || tempTypes.stream().noneMatch(t -> !t.equals(tempType) && t.isDerivedFrom(tempType)))
                            : (expressionType == null || !isOrImplements(expressionType, tempType))
                                    && (tempTypesCount == 1 || tempTypes.stream().noneMatch(t -> t != tempType && t.implementsInterface(tempType)))) {
                        types.add(tempType);
                    }
                }

                int typesCount = types.size();
                if (typesCount == 1) {
                    return types.get(0);
                } else if (typesCount > 1) {
                    return new UnionTypeClassNode(types.toArray(ClassNode.EMPTY_ARRAY));
                }
            }
        }
        return expressionType;
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

    private class PropertyLookup extends ClassCodeVisitorSupport {
        private final ClassNode receiverType;
        private final Consumer<ClassNode> propertyType;

        PropertyLookup(final ClassNode receiverType, final Consumer<ClassNode> propertyType) {
            this.receiverType = receiverType;
            this.propertyType = propertyType;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return StaticTypeCheckingVisitor.this.getSourceUnit();
        }

        @Override
        public void visitField(final FieldNode node) {
            reportPropertyType(node.getType(), node.isStatic() ? null : node.getDeclaringClass());
        }

        @Override
        public void visitMethod(final MethodNode node) {
            reportPropertyType(node.getReturnType(), node.isStatic() ? null : node.getDeclaringClass());
        }

        @Override
        public void visitProperty(final PropertyNode node) {
            reportPropertyType(node.getOriginType(), node.isStatic() ? null : node.getDeclaringClass());
        }

        private void reportPropertyType(ClassNode type, final ClassNode declaringClass) {
            if (declaringClass != null && GenericsUtils.hasUnresolvedGenerics(type)) { // GROOVY-10787
                Map<GenericsTypeName, GenericsType> spec = extractPlaceHolders(receiverType, declaringClass);
                type = applyGenericsContext(spec, type);
            }
            propertyType.accept(type);
        }
    }

    /**
     * Wrapper for a Parameter so it can be treated like a VariableExpression
     * and tracked in the {@code ifElseForWhileAssignmentTracker}.
     */
    private class ParameterVariableExpression extends VariableExpression {
        private final Parameter parameter;

        ParameterVariableExpression(final Parameter parameter) {
            super(parameter);
            this.parameter = parameter;

            ClassNode inferredType = getNodeMetaData(INFERRED_TYPE);
            if (inferredType == null) {
                inferredType = typeCheckingContext.controlStructureVariables.get(parameter); // for/catch/closure
                if (inferredType == null) {
                    inferredType = getTypeFromClosureArguments(parameter); // @ClosureParams or SAM-type coercion
                }
                setNodeMetaData(INFERRED_TYPE, inferredType != null ? inferredType : parameter.getType()); // GROOVY-10651
            }
        }

        @Override
        public Map<?, ?> getMetaDataMap() {
            return parameter.getMetaDataMap();
        }

        @Override
        public void setMetaDataMap(final Map<?, ?> metaDataMap) {
            parameter.setMetaDataMap(metaDataMap);
        }
    }

    protected class VariableExpressionTypeMemoizer extends ClassCodeVisitorSupport {
        private final boolean onlySharedVariables;
        private final Set<VariableExpression> decl = new HashSet<>();
        private final Map<VariableExpression, ClassNode> varOrigType;

        public VariableExpressionTypeMemoizer(final Map<VariableExpression, ClassNode> varOrigType) {
            this(varOrigType, false);
        }

        public VariableExpressionTypeMemoizer(final Map<VariableExpression, ClassNode> varOrigType, final boolean onlySharedVariables) {
            this.varOrigType = varOrigType;
            this.onlySharedVariables = onlySharedVariables;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return StaticTypeCheckingVisitor.this.getSourceUnit();
        }

        private boolean isOuterScopeShared(final Variable var) {
            return var.isClosureSharedVariable() && !decl.contains(var);
        }

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            Variable var = findTargetVariable(expression);
            if ((!onlySharedVariables || isOuterScopeShared(var)) && var instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) var;
                ClassNode cn = ve.getNodeMetaData(INFERRED_TYPE);
                if (cn == null) cn = ve.getOriginType();
                varOrigType.put(ve, cn);
            }
            super.visitVariableExpression(expression);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void visitDeclarationExpression(final DeclarationExpression expression) {
            if (expression.isMultipleAssignmentDeclaration()) {
                List<?> vars = expression.getTupleExpression().getExpressions();
                decl.addAll((List<VariableExpression>) vars);
            } else {
                decl.add(expression.getVariableExpression());
            }
            super.visitDeclarationExpression(expression);
        }
    }
}
