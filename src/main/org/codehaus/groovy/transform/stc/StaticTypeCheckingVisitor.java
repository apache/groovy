/*
 * Copyright 2003-2012 the original author or authors.
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
package org.codehaus.groovy.transform.stc;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.lang.ObjectRange;
import groovy.transform.TypeChecked;
import groovy.transform.TypeCheckingMode;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.WideningCategories;
import org.codehaus.groovy.classgen.ReturnAdder;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.util.ListHashMap;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.codehaus.groovy.ast.ClassHelper.*;
import static org.codehaus.groovy.ast.tools.WideningCategories.*;
import static org.codehaus.groovy.syntax.Types.*;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.*;

/**
 * The main class code visitor responsible for static type checking. It will perform various inspections like checking
 * assignment types, type inference, ... Eventually, class nodes may be annotated with inferred type information.
 *
 * @author Cedric Champeau
 * @author Jochen Theodorou
 */
public class StaticTypeCheckingVisitor extends ClassCodeVisitorSupport {
    private static final ClassNode ITERABLE_TYPE = ClassHelper.make(Iterable.class);
    private final static List<MethodNode> EMPTY_METHODNODE_LIST = Collections.emptyList();
    private static final ClassNode TYPECHECKED_CLASSNODE = ClassHelper.make(TypeChecked.class);
    private static final ClassNode[] TYPECHECKING_ANNOTATIONS = new ClassNode[]{TYPECHECKED_CLASSNODE};
    private static final ClassNode TYPECHECKING_INFO_NODE = ClassHelper.make(TypeChecked.TypeCheckingInfo.class);
    private static final ClassNode DGM_CLASSNODE = ClassHelper.make(DefaultGroovyMethods.class);
    private static final int CURRENT_SIGNATURE_PROTOCOL_VERSION = 1;
    private static final Expression CURRENT_SIGNATURE_PROTOCOL = new ConstantExpression(CURRENT_SIGNATURE_PROTOCOL_VERSION, true);

    public static final MethodNode CLOSURE_CALL_NO_ARG;
    public static final MethodNode CLOSURE_CALL_ONE_ARG;
    public static final MethodNode CLOSURE_CALL_VARGS;

    static {
        // Cache closure call methods
        CLOSURE_CALL_NO_ARG = CLOSURE_TYPE.getDeclaredMethod("call", Parameter.EMPTY_ARRAY);
        CLOSURE_CALL_ONE_ARG = CLOSURE_TYPE.getDeclaredMethod("call", new Parameter[]{
                new Parameter(OBJECT_TYPE, "arg")
        });
        CLOSURE_CALL_VARGS = CLOSURE_TYPE.getDeclaredMethod("call", new Parameter[]{
                new Parameter(OBJECT_TYPE.makeArray(), "args")
        });
    }

    private SourceUnit source;
    private ClassNode classNode;
    private MethodNode methodNode;
    private Set<MethodNode> methodsToBeVisited = Collections.emptySet();
    private ErrorCollector errorCollector;
    private boolean isInStaticContext = false;

    // used for closure return type inference
    private ClosureExpression closureExpression;
    private List<ClassNode> closureReturnTypes;

    // whenever a "with" method call is detected, this list is updated
    // with the receiver type of the with method
    private LinkedList<ClassNode> withReceiverList = new LinkedList<ClassNode>();
    /**
     * The type of the last encountered "it" implicit parameter
     */
    private ClassNode lastImplicitItType;

    /**
     * This field is used to track assignments in if/else branches, for loops and while loops. For example, in the following code:
     * if (cond) { x = 1 } else { x = '123' }
     * the inferred type of x after the if/else statement should be the LUB of (int, String)
     */
    private Map<VariableExpression, List<ClassNode>> ifElseForWhileAssignmentTracker = null;

    /**
     * Stores information which is only valid in the "if" branch of an if-then-else statement. This is used when the if
     * condition expression makes use of an instanceof check
     */
    private Stack<Map<Object, List<ClassNode>>> temporaryIfBranchTypeInformation;

    private Set<MethodNode> alreadyVisitedMethods = new HashSet<MethodNode>();

    /**
     * Some expressions need to be visited twice, because type information may be insufficient at some
     * point. For example, for closure shared variables, we need a first pass to collect every type which
     * is assigned to a closure shared variable, then a second pass to ensure that every method call on
     * such a variable is made on a LUB.
     */
    private final LinkedHashSet<Expression> secondPassExpressions = new LinkedHashSet<Expression>();

    /**
     * A map used to store every type used in closure shared variable assignments. In a second pass, we will
     * compute the LUB of each type and check that method calls on those variables are valid.
     */
    private final Map<VariableExpression, List<ClassNode>> closureSharedVariablesAssignmentTypes = new HashMap<VariableExpression, List<ClassNode>>();

    /**
     * The plugin factory used to extend the type checker capabilities.
     */
    private final TypeCheckerPluginFactory pluginFactory;

    private Map<Parameter, ClassNode> forLoopVariableTypes = new HashMap<Parameter, ClassNode>();

    // this map is used to ensure that two errors are not reported on the same line/column
    private final Set<Long> reportedErrors = new TreeSet<Long>();

    private final ReturnAdder returnAdder = new ReturnAdder(new ReturnAdder.ReturnStatementListener() {
        public void returnStatementAdded(final ReturnStatement returnStatement) {
            if (returnStatement.getExpression().equals(ConstantExpression.NULL)) return;
            ClassNode returnType = checkReturnType(returnStatement);
            if (methodNode != null) {
                ClassNode mrt = methodNode.getReturnType();
                if (!returnType.implementsInterface(mrt) && !returnType.isDerivedFrom(mrt)) {
                    // there's an implicit type conversion, like Object -> String
                    // so we'll use the method return type instead
                    returnType = mrt;
                }
                ClassNode previousType = (ClassNode) methodNode.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
                ClassNode inferred = previousType == null ? returnType : lowestUpperBound(returnType, previousType);
                methodNode.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, inferred);
            }
        }
    });

    private final ReturnAdder closureReturnAdder = new ReturnAdder(new ReturnAdder.ReturnStatementListener() {
        public void returnStatementAdded(final ReturnStatement returnStatement) {
            if (returnStatement.getExpression().equals(ConstantExpression.NULL)) return;
            MethodNode currentNode = methodNode;
            methodNode = null;
            try {
                checkReturnType(returnStatement);
                if (closureExpression != null) {
                    addClosureReturnType(getType(returnStatement.getExpression()));
                }
            } finally {
                methodNode = currentNode;
            }
        }
    });

    public StaticTypeCheckingVisitor(SourceUnit source, ClassNode cn, TypeCheckerPluginFactory pluginFactory) {
        this.source = source;
        this.classNode = cn;
        this.temporaryIfBranchTypeInformation = new Stack<Map<Object, List<ClassNode>>>();
        this.pluginFactory = pluginFactory;
        this.errorCollector = source.getErrorCollector();
        pushTemporaryTypeInfo();
    }

    //        @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Sets an alternative error collector. This can be useful when you want to run static
     * type checking "silently", for example, without failing if errors are found.
     *
     * @param errorCollector an error collector
     */
    public void setErrorCollector(final ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    @Override
    public void visitClass(final ClassNode node) {
        if (shouldSkipClassNode(node)) return;
        ClassNode oldCN = classNode;
        classNode = node;
        Set<MethodNode> oldVisitedMethod = alreadyVisitedMethods;
        alreadyVisitedMethods = new LinkedHashSet<MethodNode>();
        super.visitClass(node);
        Iterator<InnerClassNode> innerClasses = classNode.getInnerClasses();
        while (innerClasses.hasNext()) {
            InnerClassNode innerClassNode = innerClasses.next();
            visitClass(innerClassNode);
        }
        alreadyVisitedMethods = oldVisitedMethod;
        classNode = oldCN;
        node.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, node);
    }

    protected boolean shouldSkipClassNode(final ClassNode node) {
        Object type = node.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (type != null) {
            // transformation has already been run on this class node
            // prevent it from running twice
            return true;
        }
        if (isSkipMode(node)) return true;
        return false;
    }

    /**
     * Returns the list of type checking annotations class nodes. Subclasses may override this method
     * in order to provide additional classes which must be looked up when checking if a method or
     * a class node should be skipped.
     * <p/>
     * The default implementation returns {@link TypeChecked}.
     *
     * @return array of class nodes
     */
    protected ClassNode[] getTypeCheckingAnnotations() {
        return TYPECHECKING_ANNOTATIONS;
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
        return false;
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        super.visitClassExpression(expression);
        ClassNode cn = (ClassNode) expression.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (cn == null) {
            storeType(expression, getType(expression));
        }
    }

    @Override
    public void visitVariableExpression(VariableExpression vexp) {
        super.visitVariableExpression(vexp);
        if (vexp != VariableExpression.THIS_EXPRESSION &&
                vexp != VariableExpression.SUPER_EXPRESSION) {
            if (vexp.getName().equals("this")) storeType(vexp, classNode);
            if (vexp.getName().equals("super")) storeType(vexp, classNode.getSuperClass());
        }
        if (vexp.getAccessedVariable() instanceof DynamicVariable) {
            // a dynamic variable is either an undeclared variable
            // or a member of a class used in a 'with'
            DynamicVariable dyn = (DynamicVariable) vexp.getAccessedVariable();
            // first, we must check the 'with' context
            String dynName = dyn.getName();

            List<ClassNode> checkList = new LinkedList<ClassNode>(withReceiverList);
            // force checking on classNode for ast injected properties
            checkList.add(classNode);

            for (ClassNode node : checkList) {
                if (node.getProperty(dynName) != null) {
                    storeType(vexp, node.getProperty(dynName).getType());
                    return;
                }
                if (node.getField(dynName) != null) {
                    storeType(vexp, node.getField(dynName).getType());
                    return;
                }
            }

            // lookup with plugin
            if (pluginFactory != null) {
                TypeCheckerPlugin plugin = pluginFactory.getTypeCheckerPlugin(classNode);
                if (plugin != null) {
                    ClassNode type = plugin.resolveDynamicVariableType(dyn);
                    if (type != null) {
                        storeType(vexp, type);
                        return;
                    }
                }
            }

            addStaticTypeError("The variable [" + vexp.getName() + "] is undeclared.", vexp);
        }
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression pexp) {
        super.visitPropertyExpression(pexp);
        if (!existsProperty(pexp, true)) {
            Expression objectExpression = pexp.getObjectExpression();
            addStaticTypeError("No such property: " + pexp.getPropertyAsString() +
                    " for class: " + findCurrentInstanceOfClass(objectExpression, getType(objectExpression)).toString(false), pexp);
        }
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        super.visitAttributeExpression(expression);
        if (!existsProperty(expression, true)) {
            Expression objectExpression = expression.getObjectExpression();
            addStaticTypeError("No such property: " + expression.getPropertyAsString() +
                    " for class: " + findCurrentInstanceOfClass(objectExpression, objectExpression.getType()), expression);
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
            storeType(expression, ClassHelper.make(ObjectRange.class));
        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        final Expression leftExpression = expression.getLeftExpression();
        ClassNode lType = getType(leftExpression);
        final Expression rightExpression = expression.getRightExpression();
        ClassNode rType = getType(rightExpression);
        if (rightExpression instanceof ConstantExpression && ((ConstantExpression) rightExpression).getValue() == null) {
            if (!isPrimitiveType(lType))
                rType = UNKNOWN_PARAMETER_TYPE; // primitive types should be ignored as they will result in another failure
        }
        int op = expression.getOperation().getType();
        ClassNode resultType = getResultType(lType, op, rType, expression);
        if (resultType == null) {
            resultType = lType;
        }
        boolean isEmptyDeclaration = expression instanceof DeclarationExpression && rightExpression instanceof EmptyExpression;
        if (!isEmptyDeclaration) storeType(expression, resultType);
        if (!isEmptyDeclaration && isAssignment(op)) {
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
            }

            // if we are in an if/else branch, keep track of assignment
            if (ifElseForWhileAssignmentTracker != null && leftExpression instanceof VariableExpression) {
                Variable accessedVariable = ((VariableExpression) leftExpression).getAccessedVariable();
                if (accessedVariable instanceof VariableExpression) {
                    VariableExpression var = (VariableExpression) accessedVariable;
                    List<ClassNode> types = ifElseForWhileAssignmentTracker.get(var);
                    if (types == null) {
                        types = new LinkedList<ClassNode>();
                        ClassNode type = (ClassNode) var.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
                        if (type != null) types.add(type);
                        ifElseForWhileAssignmentTracker.put(var, types);
                    }
                    types.add(resultType);
                }
            }
            storeType(leftExpression, resultType);

            // if right expression is a ClosureExpression, store parameter type information
            if (leftExpression instanceof VariableExpression && rightExpression instanceof ClosureExpression) {
                Parameter[] parameters = ((ClosureExpression) rightExpression).getParameters();
                leftExpression.putNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS, parameters);
            }


        } else if (op == KEYWORD_INSTANCEOF) {
            pushInstanceOfTypeInfo(leftExpression, rightExpression);
        }
    }

    private ClassNode getOriginalDeclarationType(Expression lhs) {
        if (lhs instanceof VariableExpression) {
            Variable var = findTargetVariable((VariableExpression) lhs);
            if (var instanceof DynamicVariable) return getType(lhs);
            return var.getOriginType();
        }
        if (lhs instanceof FieldExpression) {
            return ((FieldExpression) lhs).getField().getOriginType();
        }
        return getType(lhs);
    }

    private void inferDiamondType(final ConstructorCallExpression cce, final ClassNode lType) {
        // check if constructor call expression makes use of the diamond operator
        ClassNode node = cce.getType();
        if (node.isUsingGenerics() && node.getGenericsTypes().length == 0) {
            ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(cce.getArguments());
            if (argumentListExpression.getExpressions().isEmpty()) {
                GenericsType[] genericsTypes = lType.getGenericsTypes();
                GenericsType[] copy = new GenericsType[genericsTypes.length];
                for (int i = 0; i < genericsTypes.length; i++) {
                    GenericsType genericsType = genericsTypes[i];
                    copy[i] = new GenericsType(
                            wrapTypeIfNecessary(genericsType.getType()),
                            genericsType.getUpperBounds(),
                            genericsType.getLowerBound()
                    );
                }
                node.setGenericsTypes(copy);
            } else {
                ClassNode type = getType(argumentListExpression.getExpression(0));
                if (type.isUsingGenerics()) {
                    GenericsType[] genericsTypes = type.getGenericsTypes();
                    GenericsType[] copy = new GenericsType[genericsTypes.length];
                    for (int i = 0; i < genericsTypes.length; i++) {
                        GenericsType genericsType = genericsTypes[i];
                        copy[i] = new GenericsType(
                                wrapTypeIfNecessary(genericsType.getType()),
                                genericsType.getUpperBounds(),
                                genericsType.getLowerBound()
                        );
                    }
                    node.setGenericsTypes(copy);
                }
            }
        }
    }

    /**
     * Stores information about types when [objectOfInstanceof instanceof typeExpression] is visited
     *
     * @param objectOfInstanceOf the expression which must be checked against instanceof
     * @param typeExpression     the expression which represents the target type
     */
    private void pushInstanceOfTypeInfo(final Expression objectOfInstanceOf, final Expression typeExpression) {
        final Map<Object, List<ClassNode>> tempo = temporaryIfBranchTypeInformation.peek();
        Object key = extractTemporaryTypeInfoKey(objectOfInstanceOf);
        List<ClassNode> potentialTypes = tempo.get(key);
        if (potentialTypes == null) {
            potentialTypes = new LinkedList<ClassNode>();
            tempo.put(key, potentialTypes);
        }
        potentialTypes.add(typeExpression.getType());
    }

    private void typeCheckAssignment(
            final BinaryExpression assignmentExpression,
            final Expression leftExpression,
            final ClassNode leftExpressionType,
            final Expression rightExpression,
            final ClassNode inferredRightExpressionType) {
        ClassNode leftRedirect;
        if (isArrayAccessExpression(leftExpression) || leftExpression instanceof PropertyExpression
                || (leftExpression instanceof VariableExpression
                && ((VariableExpression) leftExpression).getAccessedVariable() instanceof DynamicVariable)) {
            // in case the left expression is in the form of an array access, we should use
            // the inferred type instead of the left expression type.
            // In case we have a variable expression which accessed variable is a dynamic variable, we are
            // in the "with" case where the type must be taken from the inferred type
            leftRedirect = leftExpressionType;
        } else {
            if (leftExpression instanceof VariableExpression && isPrimitiveType(((VariableExpression) leftExpression).getOriginType())) {
                leftRedirect = leftExpressionType;
            } else {
                leftRedirect = leftExpression.getType().redirect();
            }
        }
        if (leftExpression instanceof TupleExpression) {
            // multiple assignment
            if (!(rightExpression instanceof ListExpression)) {
                addStaticTypeError("Multiple assignments without list expressions on the right hand side are unsupported in static type checking mode", rightExpression);
                return;
            }
            TupleExpression tuple = (TupleExpression) leftExpression;
            ListExpression list = (ListExpression) rightExpression;
            List<Expression> listExpressions = list.getExpressions();
            List<Expression> tupleExpressions = tuple.getExpressions();
            if (listExpressions.size() < tupleExpressions.size()) {
                addStaticTypeError("Incorrect number of values. Expected:" + tupleExpressions.size() + " Was:" + listExpressions.size(), list);
                return;
            }
            for (int i = 0, tupleExpressionsSize = tupleExpressions.size(); i < tupleExpressionsSize; i++) {
                Expression tupleExpression = tupleExpressions.get(i);
                Expression listExpression = listExpressions.get(i);
                ClassNode elemType = getType(listExpression);
                ClassNode tupleType = getType(tupleExpression);
                if (!isAssignableTo(elemType, tupleType)) {
                    addStaticTypeError("Cannot assign value of type " + elemType.getText() + " to variable of type " + tupleType.getText(), rightExpression);
                    break; // avoids too many errors
                }
            }
            return;
        }
        boolean compatible = checkCompatibleAssignmentTypes(leftRedirect, inferredRightExpressionType, rightExpression);
        // if leftRedirect is of READONLY_PROPERTY_RETURN type, then it means we are on a missing property
        if (leftExpression.getNodeMetaData(StaticTypesMarker.READONLY_PROPERTY) != null && (leftExpression instanceof PropertyExpression)) {
            addStaticTypeError("Cannot set read-only property: " + ((PropertyExpression) leftExpression).getPropertyAsString(), leftExpression);
        }
        if (!compatible) {
            addAssignmentError(leftExpressionType, inferredRightExpressionType, assignmentExpression);
        } else {
            // if closure expression on RHS, then copy the inferred closure return type
            if (rightExpression instanceof ClosureExpression) {
                Object type = rightExpression.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
                if (type != null) {
                    leftExpression.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, type);
                }
            }

            boolean possibleLooseOfPrecision = false;
            if (isNumberType(leftRedirect) && isNumberType(inferredRightExpressionType)) {
                possibleLooseOfPrecision = checkPossibleLooseOfPrecision(leftRedirect, inferredRightExpressionType, rightExpression);
                if (possibleLooseOfPrecision) {
                    addStaticTypeError("Possible loose of precision from " + inferredRightExpressionType + " to " + leftRedirect, rightExpression);
                }
            }
            // if left type is array, we should check the right component types
            if (!possibleLooseOfPrecision && leftExpressionType.isArray()) {
                ClassNode leftComponentType = leftExpressionType.getComponentType();
                ClassNode rightRedirect = rightExpression.getType().redirect();
                if (rightRedirect.isArray()) {
                    ClassNode rightComponentType = rightRedirect.getComponentType();
                    if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType)) {
                        addStaticTypeError("Cannot assign value of type " + rightComponentType.getText() + " into array of type " + leftExpressionType.getText(), assignmentExpression);
                    }
                } else if (rightExpression instanceof ListExpression) {
                    for (Expression element : ((ListExpression) rightExpression).getExpressions()) {
                        ClassNode rightComponentType = element.getType().redirect();
                        if (!checkCompatibleAssignmentTypes(leftComponentType, rightComponentType)) {
                            addStaticTypeError("Cannot assign value of type " + rightComponentType.getText() + " into array of type " + leftExpressionType.getText(), assignmentExpression);
                        }
                    }
                }
            }

            // if left type is not a list but right type is a list, then we're in the case of a groovy
            // constructor type : Dimension d = [100,200]
            // In that case, more checks can be performed
            if (!implementsInterfaceOrIsSubclassOf(leftRedirect, LIST_TYPE) && rightExpression instanceof ListExpression) {
                ArgumentListExpression argList = new ArgumentListExpression(((ListExpression) rightExpression).getExpressions());
                ClassNode[] args = getArgumentTypes(argList);
                checkGroovyStyleConstructor(leftRedirect, args);
            } else if (!implementsInterfaceOrIsSubclassOf(inferredRightExpressionType, leftRedirect)
                    && implementsInterfaceOrIsSubclassOf(inferredRightExpressionType, LIST_TYPE)) {
                addAssignmentError(leftExpressionType, inferredRightExpressionType, assignmentExpression);
            }

            // if left type is not a list but right type is a map, then we're in the case of a groovy
            // constructor type : A a = [x:2, y:3]
            // In this case, more checks can be performed
            if (!implementsInterfaceOrIsSubclassOf(leftRedirect, MAP_TYPE) && rightExpression instanceof MapExpression) {
                if (!(leftExpression instanceof VariableExpression) || !((VariableExpression) leftExpression).isDynamicTyped()) {
                    ArgumentListExpression argList = new ArgumentListExpression(rightExpression);
                    ClassNode[] args = getArgumentTypes(argList);
                    checkGroovyStyleConstructor(leftRedirect, args);
                    // perform additional type checking on arguments
                    MapExpression mapExpression = (MapExpression) rightExpression;
                    checkGroovyConstructorMap(leftExpression, leftRedirect, mapExpression);
                }
            }

            // last, check generic type information to ensure that inferred types are compatible
            if (leftExpressionType.isUsingGenerics() && !leftExpressionType.isEnum()) {
                boolean incomplete = hasRHSIncompleteGenericTypeInfo(inferredRightExpressionType);
                if (!incomplete) {
                    GenericsType gt = GenericsUtils.buildWildcardType(leftExpressionType);
                    if (!gt.isCompatibleWith(inferredRightExpressionType)) {
                        addStaticTypeError("Incompatible generic argument types. Cannot assign "
                                + inferredRightExpressionType.toString(false)
                                + " to: " + leftExpressionType.toString(false), assignmentExpression);
                    }
                }
            }
        }
    }

    private void addAssignmentError(final ClassNode leftType, final ClassNode rightType, final Expression assignmentExpression) {
        addStaticTypeError("Cannot assign value of type " + rightType.getText() + " to variable of type " + leftType.getText(), assignmentExpression);
    }

    private void checkGroovyConstructorMap(final Expression receiver, final ClassNode receiverType, final MapExpression mapExpression) {
        for (MapEntryExpression entryExpression : mapExpression.getMapEntryExpressions()) {
            Expression keyExpr = entryExpression.getKeyExpression();
            if (!(keyExpr instanceof ConstantExpression)) {
                addStaticTypeError("Dynamic keys in map-style constructors are unsupported in static type checking", keyExpr);
            } else {
                String property = keyExpr.getText();
                ClassNode currentNode = receiverType;
                PropertyNode propertyNode = null;
                while (propertyNode == null && currentNode != null) {
                    propertyNode = currentNode.getProperty(property);
                    currentNode = currentNode.getSuperClass();
                }
                if (propertyNode == null) {
                    addStaticTypeError("No such property: " + property +
                            " for class: " + receiverType.getName(), receiver);
                } else if (propertyNode != null) {
                    ClassNode valueType = getType(entryExpression.getValueExpression());
                    if (!isAssignableTo(propertyNode.getType(), valueType)) {
                        addAssignmentError(propertyNode.getType(), valueType, entryExpression);
                    }
                }
            }
        }
    }

    private boolean hasRHSIncompleteGenericTypeInfo(final ClassNode inferredRightExpressionType) {
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
     * Checks that a constructor style expression is valid regarding the number of arguments and the argument types.
     *
     * @param node      the class node for which we will try to find a matching constructor
     * @param arguments the constructor arguments
     */
    private void checkGroovyStyleConstructor(final ClassNode node, final ClassNode[] arguments) {
        if (node.equals(ClassHelper.OBJECT_TYPE) || node.equals(ClassHelper.DYNAMIC_TYPE)) {
            // in that case, we are facing a list constructor assigned to a def or object
            return;
        }
        List<ConstructorNode> constructors = node.getDeclaredConstructors();
        if (constructors.isEmpty() && arguments.length == 0) return;
        List<MethodNode> constructorList = findMethod(node, "<init>", arguments);
        if (constructorList.isEmpty()) {
            addStaticTypeError("No matching constructor found: " + node + toMethodParametersString("<init>", arguments), classNode);
        }
    }

    /**
     * When instanceof checks are found in the code, we store temporary type information data in the {@link
     * #temporaryIfBranchTypeInformation} table. This method computes the key which must be used to store this type
     * info.
     *
     * @param expression the expression for which to compute the key
     * @return a key to be used for {@link #temporaryIfBranchTypeInformation}
     */
    private Object extractTemporaryTypeInfoKey(final Expression expression) {
        return expression instanceof VariableExpression ? findTargetVariable((VariableExpression) expression) : expression.getText();
    }

    /**
     * A helper method which determines which receiver class should be used in error messages when a field or attribute
     * is not found. The returned type class depends on whether we have temporary type information availble (due to
     * instanceof checks) and whether there is a single candidate in that case.
     *
     * @param expr the expression for which an unknown field has been found
     * @param type the type of the expression (used as fallback type)
     * @return if temporary information is available and there's only one type, returns the temporary type class
     *         otherwise falls back to the provided type class.
     */
    private ClassNode findCurrentInstanceOfClass(final Expression expr, final ClassNode type) {
        if (!temporaryIfBranchTypeInformation.empty()) {
            List<ClassNode> nodes = getTemporaryTypesForExpression(expr);
            if (nodes != null && nodes.size() == 1) return nodes.get(0);
        }
        return type;
    }

    private boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly) {
        return existsProperty(pexp, checkForReadOnly, null);
    }

    /**
     * Checks whether a property exists on the receiver, or on any of the possible receiver classes (found in the
     * temporary type information table)
     *
     * @param pexp             a property expression
     * @param checkForReadOnly also lookup for read only properties
     * @param visitor          if not null, when the property node is found, visit it with the provided visitor
     * @return true if the property is defined in any of the possible receiver classes
     */
    private boolean existsProperty(final PropertyExpression pexp, final boolean checkForReadOnly, final ClassCodeVisitorSupport visitor) {
        Expression objectExpression = pexp.getObjectExpression();
        ClassNode clazz = getType(objectExpression);
        if (clazz.isArray() && "length".equals(pexp.getPropertyAsString())) {
            if (visitor != null) {
                PropertyNode node = new PropertyNode("length", Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, int_TYPE, clazz, null, null, null);
                storeType(pexp, int_TYPE);
                visitor.visitProperty(node);
            }
            return true;
        }
        List<ClassNode> tests = new LinkedList<ClassNode>();
        tests.add(clazz);
        if (clazz.equals(CLASS_Type) && clazz.getGenericsTypes() != null) {
            tests.add(clazz.getGenericsTypes()[0].getType());
        }
        if (!temporaryIfBranchTypeInformation.empty()) {
            List<ClassNode> classNodes = getTemporaryTypesForExpression(objectExpression);
            if (classNodes != null) tests.addAll(classNodes);
        }
        if (lastImplicitItType != null
                && pexp.getObjectExpression() instanceof VariableExpression
                && ((VariableExpression) pexp.getObjectExpression()).getName().equals("it")) {
            tests.add(lastImplicitItType);
        }
        String propertyName = pexp.getPropertyAsString();
        if (propertyName == null) return false;
        String capName = MetaClassHelper.capitalize(propertyName);
        boolean isAttributeExpression = pexp instanceof AttributeExpression;
        for (ClassNode testClass : tests) {
            // maps and lists have special handling for property expressions
            if (!implementsInterfaceOrIsSubclassOf(testClass, MAP_TYPE) && !implementsInterfaceOrIsSubclassOf(testClass, LIST_TYPE)) {
                ClassNode current = testClass;
                while (current != null) {
                    current = current.redirect();
                    PropertyNode propertyNode = current.getProperty(propertyName);
                    if (propertyNode != null) {
                        if (visitor != null) visitor.visitProperty(propertyNode);
                        storeType(pexp, propertyNode.getOriginType());
                        return true;
                    }
                    MethodNode getter = current.getGetterMethod("get" + capName);
                    if (getter == null) getter = current.getGetterMethod("is" + capName);
                    if (getter != null) {
                        // check that a setter also exists
                        MethodNode setterMethod = current.getSetterMethod("set" + capName);
                        if (setterMethod != null) {
                            if (visitor != null) visitor.visitMethod(getter);
                            storeType(pexp, getter.getReturnType());
                            return true;
                        }
                    }
                    if (!isAttributeExpression) {
                        FieldNode field = current.getDeclaredField(propertyName);
                        if (field != null) {
                            if (visitor != null) visitor.visitField(field);
                            storeType(pexp, field.getOriginType());
                            return true;
                        }
                    }
                    // if the property expression is an attribute expression (o.@attr), then
                    // we stop now, otherwise we must check the parent class
                    current = isAttributeExpression ? null : current.getSuperClass();
                }
                if (checkForReadOnly) {
                    current = testClass;
                    while (current != null) {
                        current = current.redirect();

                        MethodNode getter = current.getGetterMethod("get" + capName);
                        if (getter == null) getter = current.getGetterMethod("is" + capName);
                        if (getter != null) {
                            if (visitor != null) visitor.visitMethod(getter);
                            pexp.putNodeMetaData(StaticTypesMarker.READONLY_PROPERTY, Boolean.TRUE);
                            storeType(pexp, getter.getReturnType());
                            return true;
                        }
                        if (pluginFactory != null) {
                            TypeCheckerPlugin plugin = pluginFactory.getTypeCheckerPlugin(classNode);
                            if (plugin != null) {
                                PropertyNode result = plugin.resolveProperty(current, propertyName);
                                if (result != null) {
                                    if (visitor != null) visitor.visitProperty(result);
                                    storeType(pexp, result.getType());
                                    return true;
                                }
                            }
                        }
                        // if the property expression is an attribute expression (o.@attr), then
                        // we stop now, otherwise we must check the parent class
                        current = isAttributeExpression ? null : current.getSuperClass();
                    }
                }
            } else {
                if (visitor != null) {
                    // todo : type inferrence on maps and lists, if possible
                    PropertyNode node = new PropertyNode(propertyName, Opcodes.ACC_PUBLIC, OBJECT_TYPE, clazz, null, null, null);
                    visitor.visitProperty(node);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void visitField(final FieldNode node) {
        final boolean osc = isInStaticContext;
        try {
            isInStaticContext = node.isInStaticContext();
            super.visitField(node);
            Expression init = node.getInitialExpression();
            if (init!=null) {
                FieldExpression left = new FieldExpression(node);
                BinaryExpression bexp = new BinaryExpression(
                        left,
                        Token.newSymbol("=", node.getLineNumber(), node.getColumnNumber()),
                        init
                );
                bexp.setSourcePosition(init);
                typeCheckAssignment(bexp, left, node.getOriginType(), init, getType(init));
            }
        } finally {
            isInStaticContext = osc;
        }
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        // collect every variable expression used in the loop body
        final Map<VariableExpression, ClassNode> varOrigType = new HashMap<VariableExpression, ClassNode>();
        forLoop.getLoopBlock().visit(new VariableExpressionTypeMemoizer(varOrigType));

        // visit body
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        Expression collectionExpression = forLoop.getCollectionExpression();
        if (collectionExpression instanceof ClosureListExpression) {
            // for (int i=0; i<...; i++) style loop
            super.visitForLoop(forLoop);
        } else {
            collectionExpression.visit(this);
            final ClassNode collectionType = getType(collectionExpression);
            ClassNode componentType = inferLoopElementType(collectionType);
            if (ClassHelper.getUnwrapper(componentType) == forLoop.getVariableType()) {
                // prefer primitive type over boxed type
                componentType = forLoop.getVariableType();
            }
            forLoopVariableTypes.put(forLoop.getVariable(), componentType);
            if (!checkCompatibleAssignmentTypes(forLoop.getVariableType(), componentType)) {
                addStaticTypeError("Cannot loop with element of type " + forLoop.getVariableType() + " with collection of type " + collectionType, forLoop);
            }
            try {
                super.visitForLoop(forLoop);
            } finally {
                forLoopVariableTypes.remove(forLoop.getVariable());
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
            if (collectionType.implementsInterface(ITERABLE_TYPE)) {
                ClassNode intf = GenericsUtils.parameterizeInterfaceGenerics(collectionType, ITERABLE_TYPE);
                GenericsType[] genericsTypes = intf.getGenericsTypes();
                componentType = genericsTypes[0].getType();
            } else if (collectionType == ClassHelper.STRING_TYPE) {
                componentType = ClassHelper.Character_TYPE;
            } else {
                componentType = ClassHelper.OBJECT_TYPE;
            }
        }
        return componentType;
    }

    private boolean isSecondPassNeededForControlStructure(final Map<VariableExpression, ClassNode> varOrigType, final Map<VariableExpression, List<ClassNode>> oldTracker) {
        Map<VariableExpression, ClassNode> assignedVars = popAssignmentTracking(oldTracker);
        for (Map.Entry<VariableExpression, ClassNode> entry : assignedVars.entrySet()) {
            Variable key = findTargetVariable(entry.getKey());
            if (key instanceof VariableExpression) {
                ClassNode origType = varOrigType.get((VariableExpression) key);
                ClassNode newType = entry.getValue();
                if (varOrigType.containsKey(key) && (origType == null || !newType.equals(origType))) {
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
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
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
        } else {
            MethodNode mn = findMethodOrFail(expression, type, "bitwiseNegate");
            resultType = mn.getReturnType();
        }
        storeType(expression, resultType);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        super.visitUnaryPlusExpression(expression);
        negativeOrPositiveUnary(expression, "positive");
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        super.visitUnaryMinusExpression(expression);
        negativeOrPositiveUnary(expression, "negative");
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        super.visitPostfixExpression(expression);
        Expression inner = expression.getExpression();
        ClassNode exprType = getType(inner);
        int type = expression.getOperation().getType();
        if (isPrimitiveType(exprType) || isPrimitiveType(getUnwrapper(exprType))) {
            if (type == PLUS_PLUS || type == MINUS_MINUS) return;
            addStaticTypeError("Unsupported postfix operation type [" + expression.getOperation() + "]", expression);
            return;
        }
        // not a primitive type. We must find a method which is called next
        String name = type == PLUS_PLUS ? "next" : type == MINUS_MINUS ? "previous" : null;
        if (name == null) {
            addStaticTypeError("Unsupported postfix operation type [" + expression.getOperation() + "]", expression);
            return;
        }
        MethodNode node = findMethodOrFail(inner, exprType, name);
        if (node != null) {
            storeTargetMethod(expression, node);
        }
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        super.visitPrefixExpression(expression);
        Expression inner = expression.getExpression();
        ClassNode exprType = getType(inner);
        int type = expression.getOperation().getType();
        if (isPrimitiveType(exprType) || isPrimitiveType(getUnwrapper(exprType))) {
            if (type == PLUS_PLUS || type == MINUS_MINUS) return;
            addStaticTypeError("Unsupported prefix operation type [" + expression.getOperation() + "]", expression);
            return;
        }
        // not a primitive type. We must find a method which is called next or previous
        String name = type == PLUS_PLUS ? "next" : type == MINUS_MINUS ? "previous" : null;
        if (name == null) {
            addStaticTypeError("Unsupported prefix operation type [" + expression.getOperation() + "]", expression);
            return;
        }
        MethodNode node = findMethodOrFail(inner, exprType, name);
        if (node != null) {
            storeTargetMethod(expression, node);
        }
    }

    private void negativeOrPositiveUnary(Expression expression, String name) {
        ClassNode type = getType(expression);
        ClassNode typeRe = type.redirect();
        ClassNode resultType;
        if (isDoubleCategory(ClassHelper.getUnwrapper(typeRe))) {
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
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        MethodNode old = this.methodNode;
        this.methodNode = node;
        super.visitConstructorOrMethod(node, isConstructor);
        if (!isConstructor) {
            returnAdder.visitMethod(node);
        }
        this.methodNode = old;
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        super.visitReturnStatement(statement);
        checkReturnType(statement);
        if (closureExpression != null && statement.getExpression() != ConstantExpression.NULL) {
            addClosureReturnType(getType(statement.getExpression()));
        }
    }

    private ClassNode checkReturnType(final ReturnStatement statement) {
        Expression expression = statement.getExpression();
        ClassNode type = getType(expression);
        if (methodNode != null) {
            if (!methodNode.isVoidMethod()
                    && !type.equals(void_WRAPPER_TYPE)
                    && !type.equals(VOID_TYPE)
                    && !checkCompatibleAssignmentTypes(methodNode.getReturnType(), type)
                    && !(expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue()==null)) {
                addStaticTypeError("Cannot return value of type " + type + " on method returning type " + methodNode.getReturnType(), expression);
            } else if (!methodNode.isVoidMethod()) {
                ClassNode previousType = (ClassNode) methodNode.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
                ClassNode inferred = previousType == null ? type : lowestUpperBound(type, previousType);
                if (implementsInterfaceOrIsSubclassOf(inferred, methodNode.getReturnType())) {
                    methodNode.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, inferred);
                } else {
                    methodNode.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, methodNode.getReturnType());
                }
            }
        }
        return type;
    }

    private void addClosureReturnType(ClassNode returnType) {
        if (closureReturnTypes == null) closureReturnTypes = new LinkedList<ClassNode>();
        closureReturnTypes.add(returnType);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        ClassNode receiver = call.isThisCall() ? classNode :
                call.isSuperCall() ? classNode.getSuperClass() : call.getType();
        Expression arguments = call.getArguments();
        ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(arguments));
        MethodNode node = findMethodOrFail(call, receiver, "<init>", args);
        if (node != null) {
            if (node.getParameters().length == 0 && args.length == 1 && implementsInterfaceOrIsSubclassOf(args[0], MAP_TYPE)) {
                if (arguments instanceof TupleExpression) {
                    TupleExpression texp = (TupleExpression) arguments;
                    List<Expression> expressions = texp.getExpressions();
                    if (expressions.size() == 1) {
                        Expression expression = expressions.get(0);
                        if (expression instanceof MapExpression) {
                            MapExpression argList = (MapExpression) expression;
                            checkGroovyConstructorMap(call, receiver, argList);
                            node = new ConstructorNode(Opcodes.ACC_PUBLIC, new Parameter[]{new Parameter(MAP_TYPE, "map")}, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
                            node.setDeclaringClass(receiver);

                        }
                    }
                }
            }
            storeTargetMethod(call, node);
        }
    }

    private ClassNode[] getArgumentTypes(ArgumentListExpression args) {
        List<Expression> arglist = args.getExpressions();
        ClassNode[] ret = new ClassNode[arglist.size()];
        int i = 0;
        Map<Object, List<ClassNode>> info = temporaryIfBranchTypeInformation.empty() ? null : temporaryIfBranchTypeInformation.peek();
        for (Expression exp : arglist) {
            if (exp instanceof ConstantExpression && ((ConstantExpression) exp).getValue() == null) {
                ret[i] = UNKNOWN_PARAMETER_TYPE;
            } else {
                ret[i] = getType(exp);
                if (exp instanceof VariableExpression && info != null) {
                    List<ClassNode> classNodes = getTemporaryTypesForExpression(exp);
                    if (classNodes != null && !classNodes.isEmpty()) {
                        ArrayList<ClassNode> arr = new ArrayList<ClassNode>(classNodes.size() + 1);
                        arr.add(ret[i]);
                        arr.addAll(classNodes);
                        ret[i] = new UnionTypeClassNode(arr.toArray(new ClassNode[arr.size()]));
                    }
                }
            }
            i++;
        }
        return ret;
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        // collect every variable expression used in the loop body
        final Map<VariableExpression, ClassNode> varOrigType = new HashMap<VariableExpression, ClassNode>();
        Statement code = expression.getCode();
        code.visit(new VariableExpressionTypeMemoizer(varOrigType));
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();

        // first, collect closure shared variables and reinitialize types
        SharedVariableCollector collector = new SharedVariableCollector(getSourceUnit());
        collector.visitClosureExpression(expression);
        Set<VariableExpression> closureSharedExpressions = collector.getClosureSharedExpressions();
        Map<VariableExpression, ListHashMap> typesBeforeVisit = null;
        if (!closureSharedExpressions.isEmpty()) {
            typesBeforeVisit = new HashMap<VariableExpression, ListHashMap>();
            saveVariableExpressionMetadata(closureSharedExpressions, typesBeforeVisit);
        }

        // perform visit
        ClosureExpression oldClosureExpr = closureExpression;
        List<ClassNode> oldClosureReturnTypes = closureReturnTypes;
        closureExpression = expression;
        super.visitClosureExpression(expression);
        MethodNode node = new MethodNode("dummy", 0, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, code);
        closureReturnAdder.visitMethod(node);

        if (closureReturnTypes != null) {
            expression.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, lowestUpperBound(closureReturnTypes));
        }

        closureExpression = oldClosureExpr;
        closureReturnTypes = oldClosureReturnTypes;

        boolean typeChanged = isSecondPassNeededForControlStructure(varOrigType, oldTracker);
        if (typeChanged) visitClosureExpression(expression);

        // restore original metadata
        restoreVariableExpressionMetadata(typesBeforeVisit);
    }

    private void restoreVariableExpressionMetadata(final Map<VariableExpression, ListHashMap> typesBeforeVisit) {
        if (typesBeforeVisit != null) {
            for (Map.Entry<VariableExpression, ListHashMap> entry : typesBeforeVisit.entrySet()) {
                VariableExpression ve = entry.getKey();
                ListHashMap metadata = entry.getValue();
                for (StaticTypesMarker marker : StaticTypesMarker.values()) {
                    ve.removeNodeMetaData(marker);
                    Object value = metadata.get(marker);
                    if (value != null) ve.setNodeMetaData(marker, value);
                }
            }
        }
    }

    private void saveVariableExpressionMetadata(final Set<VariableExpression> closureSharedExpressions, final Map<VariableExpression, ListHashMap> typesBeforeVisit) {
        for (VariableExpression ve : closureSharedExpressions) {
            ListHashMap<StaticTypesMarker, Object> metadata = new ListHashMap<StaticTypesMarker, Object>();
            for (StaticTypesMarker marker : StaticTypesMarker.values()) {
                Object value = ve.getNodeMetaData(marker);
                if (value != null) {
                    metadata.put(marker, value);
                }
            }
            typesBeforeVisit.put(ve, metadata);
            Variable accessedVariable = ve.getAccessedVariable();
            if (accessedVariable != ve && accessedVariable instanceof VariableExpression) {
                saveVariableExpressionMetadata(Collections.singleton((VariableExpression) accessedVariable), typesBeforeVisit);
            }
        }
    }

    @Override
    public void visitMethod(final MethodNode node) {
        if (isSkipMode(node)) return;
        // alreadyVisitedMethods prevents from visiting the same method multiple times
        // and prevents from infinite loops
        if (alreadyVisitedMethods.contains(node)) return;
        alreadyVisitedMethods.add(node);

        // second, we must ensure that this method MUST be statically checked
        // for example, in a mixed mode where only some methods are statically checked
        // we must not visit a method which used dynamic dispatch.
        // We do not check for an annotation because some other AST transformations
        // may use this visitor without the annotation being explicitely set
        if (!methodsToBeVisited.isEmpty() && !methodsToBeVisited.contains(node)) return;
        final boolean osc = isInStaticContext;
        try {
            isInStaticContext = node.isStatic();
        super.visitMethod(node);
        ClassNode rtype = (ClassNode) node.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
        if (rtype==null) {
            node.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, node.getReturnType());
        }
        addTypeCheckingInfoAnnotation(node);
        } finally {
            isInStaticContext = osc;
    }
    }

    protected void addTypeCheckingInfoAnnotation(final MethodNode node) {
        // if a returned inferred type is available and no @TypeCheckingInfo is on node, then add an
        // annotation to the method node
        ClassNode rtype = (ClassNode) node.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
        if (rtype != null && node.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()) {
            AnnotationNode anno = new AnnotationNode(TYPECHECKING_INFO_NODE);
            anno.setMember("version", CURRENT_SIGNATURE_PROTOCOL);
            SignatureCodec codec = SignatureCodecFactory.getCodec(CURRENT_SIGNATURE_PROTOCOL_VERSION);
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
        final String name = call.getMethod();
        if (name == null) {
            addStaticTypeError("cannot resolve dynamic method name at compile time.", call);
            return;
        }

        final ClassNode rememberLastItType = lastImplicitItType;
        Expression callArguments = call.getArguments();

        boolean isWithCall = isWithCall(name, callArguments);

        if (!isWithCall) {
            // if it is not a "with" call, arguments should be visited first
            callArguments.visit(this);
        }

        ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(callArguments));
        final ClassNode receiver = call.getOwnerType();

        if (isWithCall) {
            withReceiverList.add(0, receiver); // must be added first in the list
            lastImplicitItType = receiver;
            // if the provided closure uses an explicit parameter definition, we can
            // also check that the provided type is correct
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
            }
        }

        try {
            if (isWithCall) {
                // in case of a with call, arguments (the closure) should be visited now that we checked
                // the arguments
                callArguments.visit(this);
            }

            // method call receivers are :
            //   - possible "with" receivers
            //   - the actual receiver as found in the method call expression
            //   - any of the potential receivers found in the instanceof temporary table
            // in that order
            List<ClassNode> receivers = new LinkedList<ClassNode>();
            if (!withReceiverList.isEmpty()) receivers.addAll(withReceiverList);
            receivers.add(receiver);
            List<MethodNode> mn = null;
            ClassNode chosenReceiver = null;
            for (ClassNode currentReceiver : receivers) {
                mn = findMethod(currentReceiver, name, args);
                if (!mn.isEmpty()) {
                    if (mn.size() == 1) typeCheckMethodsWithGenerics(currentReceiver, args, mn.get(0), call);
                    chosenReceiver = currentReceiver;
                    break;
                }
            }
            if (mn.isEmpty()) {
                addStaticTypeError("Cannot find matching method " + receiver.getText() + "#" + toMethodParametersString(name, args), call);
            } else {
                if (mn.size() == 1) {
                    MethodNode directMethodCallCandidate = mn.get(0);
                    // visit the method to obtain inferred return type
                    ClassNode currentClassNode = classNode;
                    classNode = directMethodCallCandidate.getDeclaringClass();
                    for (ClassNode node : source.getAST().getClasses()) {
                        if (isClassInnerClassOrEqualTo(classNode, node)) {
                            silentlyVisitMethodNode(directMethodCallCandidate);
                            break;
                        }
                    }
                    pickInferredTypeFromMethodAnnotation(directMethodCallCandidate);
                    classNode = currentClassNode;
                    ClassNode returnType = getType(directMethodCallCandidate);
                    if (returnType.isUsingGenerics() && !returnType.isEnum()) {
                        ClassNode irtg = inferReturnTypeGenerics(chosenReceiver, directMethodCallCandidate, callArguments);
                        returnType = irtg!=null && implementsInterfaceOrIsSubclassOf(irtg, returnType)?irtg:returnType;
                    }
                    storeType(call, returnType);
                    storeTargetMethod(call, directMethodCallCandidate);

                } else {
                    addAmbiguousErrorMessage(mn, name, args, call);
                }
            }
        } finally {
            if (isWithCall) {
                lastImplicitItType = rememberLastItType;
                withReceiverList.removeFirst();
            }
        }
    }

    private void pickInferredTypeFromMethodAnnotation(final MethodNode node) {
        if (node.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE) == null
                && !node.getAnnotations(TYPECHECKING_INFO_NODE).isEmpty()) {
            List<AnnotationNode> annotations = node.getAnnotations(TYPECHECKING_INFO_NODE);
            AnnotationNode head = annotations.get(0);
            int version = Integer.valueOf(head.getMember("version").getText());
            String signature = head.getMember("inferredType").getText();
            SignatureCodec codec = SignatureCodecFactory.getCodec(version);
            ClassNode result = codec.decode(signature);
            node.putNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE, result);
        }
    }

    private void silentlyVisitMethodNode(final MethodNode directMethodCallCandidate) {
        // visit is authorized because the classnode belongs to the same source unit
        ErrorCollector currentCollector = errorCollector;
        // create a dummy collector, we don't want errors to be reported for this visit
        errorCollector = new ErrorCollector(new CompilerConfiguration());
        try {
            visitMethod(directMethodCallCandidate);
        } finally {
            // restore error collector
            errorCollector = currentCollector;
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        final String name = call.getMethodAsString();
        if (name == null) {
            addStaticTypeError("cannot resolve dynamic method name at compile time.", call.getMethod());
            return;
        }

        final Expression objectExpression = call.getObjectExpression();

        objectExpression.visit(this);
        call.getMethod().visit(this);

        // if the call expression is a spread operator call, then we must make sure that
        // the call is made on a collection type
        if (call.isSpreadSafe()) {
            ClassNode expressionType = getType(objectExpression);
            if (!(expressionType.equals(Collection_TYPE) || expressionType.implementsInterface(Collection_TYPE))) {
                addStaticTypeError("Spread operator can only be used on collection types", expressionType);
                return;
            } else {
                // type check call as if it was made on component type
                ClassNode componentType = inferComponentType(expressionType);
                MethodCallExpression subcall = new MethodCallExpression(
                        new CastExpression(componentType, EmptyExpression.INSTANCE),
                        name,
                        call.getArguments()
                );
                subcall.setLineNumber(call.getLineNumber());
                subcall.setColumnNumber(call.getColumnNumber());
                visitMethodCallExpression(subcall);
                // the inferred type here should be a list of what the subcall returns
                ClassNode subcallReturnType = getType(subcall);
                ClassNode listNode = LIST_TYPE.getPlainNodeReference();
                listNode.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(subcallReturnType))});
                storeType(call, listNode);
                // store target method
                storeTargetMethod(call, (MethodNode) subcall.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET));
                return;
            }
        }

        final ClassNode rememberLastItType = lastImplicitItType;
        Expression callArguments = call.getArguments();

        boolean isWithCall = isWithCall(name, callArguments);

        if (!isWithCall) {
            // if it is not a "with" call, arguments should be visited first
            callArguments.visit(this);
        }

        ClassNode[] args = getArgumentTypes(InvocationWriter.makeArgumentList(callArguments));
        final boolean isCallOnClosure = isClosureCall(name, objectExpression, callArguments);
        final ClassNode receiver = getType(objectExpression);

        if (isWithCall) {
            withReceiverList.add(0, receiver); // must be added first in the list
            lastImplicitItType = receiver;
            // if the provided closure uses an explicit parameter definition, we can
            // also check that the provided type is correct
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
            }
        }

        try {
            if (isWithCall) {
                // in case of a with call, arguments (the closure) should be visited now that we checked
                // the arguments
                callArguments.visit(this);
            }

            if (isCallOnClosure) {
                // this is a closure.call() call
                if (objectExpression == VariableExpression.THIS_EXPRESSION) {
                    // isClosureCall() check verified earlier that a field exists
                    FieldNode field = classNode.getDeclaredField(name);
                    ClassNode closureReturnType = field.getType().getGenericsTypes()[0].getType();
                    Object data = field.getNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS);
                    if (data != null) {
                        Parameter[] parameters = (Parameter[]) data;
                        typeCheckClosureCall(callArguments, args, parameters);
                    }
                    storeType(call, closureReturnType);
                } else if (objectExpression instanceof VariableExpression) {
                    Variable variable = findTargetVariable((VariableExpression) objectExpression);
                    if (variable instanceof Expression) {
                        Object data = ((Expression) variable).getNodeMetaData(StaticTypesMarker.CLOSURE_ARGUMENTS);
                        if (data != null) {
                            Parameter[] parameters = (Parameter[]) data;
                            typeCheckClosureCall(callArguments, args, parameters);
                        }
                        Object type = ((Expression) variable).getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
                        if (type == null) {
                            // if variable was declared as a closure and inferred type is unknown, we
                            // may face a recursive call. In that case, we will use the type of the
                            // generic return type of the closure declaration
                            if (variable.getType().equals(CLOSURE_TYPE)) {
                                GenericsType[] genericsTypes = variable.getType().getGenericsTypes();
                                if (genericsTypes != null && !genericsTypes[0].isPlaceholder()) {
                                    type = genericsTypes[0].getType();
                                } else {
                                    type = OBJECT_TYPE;
                                }
                            }
                        }
                        if (type != null) {
                            storeType(call, (ClassNode) type);
                        }
                    }
                } else if (objectExpression instanceof ClosureExpression) {
                    // we can get actual parameters directly
                    Parameter[] parameters = ((ClosureExpression) objectExpression).getParameters();
                    typeCheckClosureCall(callArguments, args, parameters);
                    Object data = objectExpression.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
                    if (data != null) {
                        storeType(call, (ClassNode) data);
                    }
                }
                int nbOfArgs = 0;
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
                List<ClassNode> receivers = new LinkedList<ClassNode>();
                if (!withReceiverList.isEmpty()) receivers.addAll(withReceiverList);
                receivers.add(receiver);
                if (receiver.equals(CLASS_Type) && receiver.getGenericsTypes() != null) {
                    GenericsType clazzGT = receiver.getGenericsTypes()[0];
                    receivers.add(clazzGT.getType());
                }
                if (!temporaryIfBranchTypeInformation.empty()) {
                    List<ClassNode> potentialReceiverType = getTemporaryTypesForExpression(objectExpression);
                    if (potentialReceiverType != null) receivers.addAll(potentialReceiverType);
                }
                List<MethodNode> mn = null;
                ClassNode chosenReceiver = null;
                for (ClassNode currentReceiver : receivers) {
                    mn = findMethod(currentReceiver, name, args);

                    // if the receiver is "this" or "implicit this", then we must make sure that the compatible
                    // methods are only static if we are in a static context
                    if (!mn.isEmpty() && isInStaticContext && (call.isImplicitThis()
                            || (objectExpression instanceof VariableExpression && ((VariableExpression)objectExpression).isThisExpression()))) {
                        // we create a separate method list just to be able to print out
                        // a nice error message to the user
                        List<MethodNode> staticMethods = new LinkedList<MethodNode>();
                        List<MethodNode> nonStaticMethods = new LinkedList<MethodNode>();
                        for (final MethodNode node : mn) {
                            if (node.isStatic()) {
                                staticMethods.add(node);
                            } else {
                                nonStaticMethods.add(node);
                            }
                        }
                        mn = staticMethods;
                        if (staticMethods.isEmpty()) {
                            // choose an arbitrary method to display an error message
                            MethodNode node = nonStaticMethods.get(0);
                            ClassNode owner = node.getDeclaringClass();
                            addStaticTypeError("Non static method "+owner.getName()+"#"+node.getName()+" cannot be called from static context", call);
                        }
                    }

                    if (!mn.isEmpty()) {
                        if (mn.size() == 1) typeCheckMethodsWithGenerics(currentReceiver, args, mn.get(0), call);
                        chosenReceiver = currentReceiver;
                        break;
                    }
                }
                if (mn.isEmpty()) {
                    addStaticTypeError("Cannot find matching method " + receiver.getText() + "#" + toMethodParametersString(name, args), call);
                } else {
                    if (areCategoryMethodCalls(mn, name, args)) {
                        addCategoryMethodCallError(call);
                    }
                    if (mn.size() == 1) {
                        MethodNode directMethodCallCandidate = mn.get(0);
                        // visit the method to obtain inferred return type
                        ClassNode currentClassNode = classNode;
                        classNode = directMethodCallCandidate.getDeclaringClass();
                        for (ClassNode node : source.getAST().getClasses()) {
                            if (isClassInnerClassOrEqualTo(classNode, node)) {
                                // visit is authorized because the classnode belongs to the same source unit
                                silentlyVisitMethodNode(directMethodCallCandidate);
                                break;
                            }
                        }
                        pickInferredTypeFromMethodAnnotation(directMethodCallCandidate);
                        classNode = currentClassNode;
                        ClassNode returnType = getType(directMethodCallCandidate);
                        if (isUsingGenericsOrIsArrayUsingGenerics(returnType)) {
                            ClassNode irtg = inferReturnTypeGenerics(chosenReceiver, directMethodCallCandidate, callArguments);
                            returnType = irtg!=null && implementsInterfaceOrIsSubclassOf(irtg, returnType)?irtg:returnType;
                        }
                        storeType(call, returnType);
                        storeTargetMethod(call, directMethodCallCandidate);

                        // if the object expression is a closure shared variable, we will have to perform a second pass
                        if (objectExpression instanceof VariableExpression) {
                            VariableExpression var = (VariableExpression) objectExpression;
                            if (var.isClosureSharedVariable()) secondPassExpressions.add(call);
                        }

                    } else {
                        addAmbiguousErrorMessage(mn, name, args, call);
                    }
                }
            }
        } finally {
            if (isWithCall) {
                lastImplicitItType = rememberLastItType;
                withReceiverList.removeFirst();
            }
        }
    }

    private List<ClassNode> getTemporaryTypesForExpression(final Expression objectExpression) {
        List<ClassNode> classNodes = null;
        int depth = temporaryIfBranchTypeInformation.size();
        while (classNodes == null && depth > 0) {
            final Map<Object, List<ClassNode>> tempo = temporaryIfBranchTypeInformation.get(--depth);
            Object key = extractTemporaryTypeInfoKey(objectExpression);
            classNodes = tempo.get(key);
        }
        return classNodes;
    }

    private void storeTargetMethod(final Expression call, final MethodNode directMethodCallCandidate) {
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, directMethodCallCandidate);
    }

    private boolean isClosureCall(final String name, final Expression objectExpression, final Expression arguments) {
        if (objectExpression instanceof ClosureExpression) return true;
        if (objectExpression == VariableExpression.THIS_EXPRESSION) {
            FieldNode fieldNode = classNode.getDeclaredField(name);
            if (fieldNode != null) {
                ClassNode type = fieldNode.getType();
                if (CLOSURE_TYPE.equals(type) && !classNode.hasPossibleMethod(name, arguments)) {
                    return true;
                }
            }
        } else {
            if (!"call".equals(name) && !"doCall".equals(name)) return false;
        }
        return (getType(objectExpression).equals(CLOSURE_TYPE));
    }

    private void typeCheckClosureCall(final Expression callArguments, final ClassNode[] args, final Parameter[] parameters) {
        if (allParametersAndArgumentsMatch(parameters, args) < 0 &&
                lastArgMatchesVarg(parameters, args) < 0) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                final Parameter parameter = parameters[i];
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
            pushTemporaryTypeInfo();
            visitStatement(ifElse);
            ifElse.getBooleanExpression().visit(this);
            ifElse.getIfBlock().visit(this);

            // pop if-then-else temporary type info
            temporaryIfBranchTypeInformation.pop();

            Statement elseBlock = ifElse.getElseBlock();
            if (elseBlock instanceof EmptyStatement) {
                // dispatching to EmptyStatement will not call back visitor,
                // must call our visitEmptyStatement explicitly
                visitEmptyStatement((EmptyStatement) elseBlock);
            } else {
                elseBlock.visit(this);
            }
        } finally {
            popAssignmentTracking(oldTracker);
        }
    }

    private Map<VariableExpression, ClassNode> popAssignmentTracking(final Map<VariableExpression, List<ClassNode>> oldTracker) {
        Map<VariableExpression, ClassNode> assignments = new HashMap<VariableExpression, ClassNode>();
        if (!ifElseForWhileAssignmentTracker.isEmpty()) {
            for (Map.Entry<VariableExpression, List<ClassNode>> entry : ifElseForWhileAssignmentTracker.entrySet()) {
                VariableExpression key = entry.getKey();
                ClassNode cn = lowestUpperBound(entry.getValue());
                storeType(key, cn);
                assignments.put(key, cn);
            }
        }
        ifElseForWhileAssignmentTracker = oldTracker;
        return assignments;
    }

    private Map<VariableExpression, List<ClassNode>> pushAssignmentTracking() {
        // memorize current assignment context
        Map<VariableExpression, List<ClassNode>> oldTracker = ifElseForWhileAssignmentTracker;
        ifElseForWhileAssignmentTracker = new HashMap<VariableExpression, List<ClassNode>>();
        return oldTracker;
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        super.visitCastExpression(expression);
        if (!expression.isCoerce()) {
            ClassNode targetType = expression.getType();
            Expression source = expression.getExpression();
            ClassNode expressionType = getType(source);
            if (!checkCast(targetType, source)) {
                addStaticTypeError("Inconvertible types: cannot cast " + expressionType.toString(false) + " to " + targetType.getName(), expression);
            }
        }
        storeType(expression, expression.getType());
    }

    private boolean checkCast(final ClassNode targetType, final Expression source) {
        boolean sourceIsNull = source instanceof ConstantExpression && ((ConstantExpression) source).getValue() == null;
        ClassNode expressionType = getType(source);
        if (targetType.isArray() && expressionType.isArray()) {
            return checkCast(targetType.getComponentType(), new VariableExpression("foo", expressionType.getComponentType()));
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
        } else if (sourceIsNull && isPrimitiveType(targetType)) {
            return false;
        } else if (!isAssignableTo(targetType, expressionType) && !implementsInterfaceOrIsSubclassOf(expressionType, targetType)) {
            return false;
        }
        return true;
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        Map<VariableExpression, List<ClassNode>> oldTracker = pushAssignmentTracking();
        // create a new temporary element in the if-then-else type info
        pushTemporaryTypeInfo();
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        // pop if-then-else temporary type info
        temporaryIfBranchTypeInformation.pop();
        expression.getFalseExpression().visit(this);
        // store type information
        final ClassNode typeOfTrue = getType(expression.getTrueExpression());
        final ClassNode typeOfFalse = getType(expression.getFalseExpression());
        storeType(expression, lowestUpperBound(typeOfTrue, typeOfFalse));
        popAssignmentTracking(oldTracker);
    }

    private void pushTemporaryTypeInfo() {
        Map<Object, List<ClassNode>> potentialTypes = new HashMap<Object, List<ClassNode>>();
        temporaryIfBranchTypeInformation.push(potentialTypes);
    }


    private void storeType(Expression exp, ClassNode cn) {
        if (cn == UNKNOWN_PARAMETER_TYPE) {
            // this can happen for example when "null" is used in an assignment or a method parameter.
            // In that case, instead of storing the virtual type, we must "reset" type information
            // by determining the declaration type of the expression
            storeType(exp, getOriginalDeclarationType(exp));
            return;
        }
        ClassNode oldValue = (ClassNode) exp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, cn);
        if (oldValue != null) {
            // this may happen when a variable declaration type is wider than the subsequent assignment values
            // for example :
            // def o = 1 // first, an int
            // o = 'String' // then a string
            // o = new Object() // and eventually an object !
            // in that case, the INFERRED_TYPE corresponds to the current inferred type, while
            // DECLARATION_INFERRED_TYPE is the type which should be used for the initial type declaration
            ClassNode oldDIT = (ClassNode) exp.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE);
            if (oldDIT != null) {
                exp.putNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE, lowestUpperBound(oldDIT, cn));
            } else {
                exp.putNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE, lowestUpperBound(oldValue, cn));
            }
        }
        if (exp instanceof VariableExpression) {
            VariableExpression var = (VariableExpression) exp;
            final Variable accessedVariable = var.getAccessedVariable();
            if (accessedVariable != null && accessedVariable != exp && accessedVariable instanceof VariableExpression) {
                storeType((Expression) accessedVariable, cn);
            }
            if (var.isClosureSharedVariable()) {
                List<ClassNode> assignedTypes = closureSharedVariablesAssignmentTypes.get(var);
                if (assignedTypes == null) {
                    assignedTypes = new LinkedList<ClassNode>();
                    closureSharedVariablesAssignmentTypes.put(var, assignedTypes);
                }
                assignedTypes.add(cn);
            }
            if (!temporaryIfBranchTypeInformation.empty()) {
                List<ClassNode> temporaryTypesForExpression = getTemporaryTypesForExpression(exp);
                if (temporaryTypesForExpression != null && !temporaryTypesForExpression.isEmpty()) {
                    // a type inference has been made on a variable which type was defined in an instanceof block
                    // we erase available information with the new type
                    temporaryTypesForExpression.clear();
                }
            }
        }
    }

    private ClassNode getResultType(ClassNode left, int op, ClassNode right, BinaryExpression expr) {
        ClassNode leftRedirect = left.redirect();
        ClassNode rightRedirect = right.redirect();

        Expression leftExpression = expr.getLeftExpression();
        if (op == ASSIGN || op == ASSIGNMENT_OPERATOR) {
            if (leftRedirect.isArray() && !rightRedirect.isArray()) return leftRedirect;
            if (leftRedirect.implementsInterface(Collection_TYPE) && rightRedirect.implementsInterface(Collection_TYPE)) {
                // because of type inferrence, we must perform an additional check if the right expression
                // is an empty list expression ([]). In that case and only in that case, the inferred type
                // will be wrong, so we will prefer the left type
                if (expr.getRightExpression() instanceof ListExpression) {
                    List<Expression> list = ((ListExpression) expr.getRightExpression()).getExpressions();
                    if (list.isEmpty()) return left;
                }
                return right;
            }
            if (rightRedirect.implementsInterface(Collection_TYPE) && rightRedirect.isDerivedFrom(leftRedirect)) {
                // ex : def foos = ['a','b','c']
                return right;
            }
            if (leftExpression instanceof VariableExpression) {
                ClassNode initialType = getOriginalDeclarationType(leftExpression).redirect();
                // as anything can be assigned to a String, Class or boolean, return the left type instead
                if (STRING_TYPE.equals(initialType)
                        || CLASS_Type.equals(initialType)
                        || Boolean_TYPE.equals(initialType)) {
                    return initialType;
                }
            }
            return right;
        } else if (isBoolIntrinsicOp(op)) {
            return boolean_TYPE;
        } else if (isArrayOp(op)) {
            if (ClassHelper.STRING_TYPE.equals(left)) {
                // special case here
                return ClassHelper.STRING_TYPE;
            }
            return inferComponentType(left);
        } else if (op == FIND_REGEX) {
            // this case always succeeds the result is a Matcher
            return Matcher_TYPE;
        }
        // the left operand is determining the result of the operation
        // for primitives and their wrapper we use a fixed table here
        else if (isNumberType(leftRedirect) && isNumberType(rightRedirect)) {
            if (isOperationInGroup(op)) {
                if (isIntCategory(leftRedirect) && isIntCategory(rightRedirect)) return int_TYPE;
                if (isLongCategory(leftRedirect) && isLongCategory(rightRedirect)) return long_TYPE;
                if (isFloat(leftRedirect) && isFloat(rightRedirect)) return float_TYPE;
                if (isDouble(leftRedirect) && isDouble(rightRedirect)) return double_TYPE;
            } else if (isPowerOperator(op)) {
                return Number_TYPE;
            } else if (isBitOperator(op)) {
                if (isIntCategory(leftRedirect) && isIntCategory(rightRedirect)) return int_TYPE;
                if (isLongCategory(leftRedirect) && isLongCategory(rightRedirect)) return Long_TYPE;
                if (isBigIntCategory(leftRedirect) && isBigIntCategory(rightRedirect)) return BigInteger_TYPE;
            } else if (isCompareToBoolean(op) || op == COMPARE_EQUAL) {
                return boolean_TYPE;
            }
        }


        // try to find a method for the operation
        String operationName = getOperationName(op);
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
        if (isNumberCategory(getWrapper(rightRedirect)) && isNumberCategory(getWrapper(leftRedirect)) && (MOD == op || MOD_EQUAL==op)) {
            return leftRedirect;
        }

        MethodNode method = findMethodOrFail(expr, left, operationName, right);
        if (method != null) {
            typeCheckMethodsWithGenerics(left, new ClassNode[]{right}, method, expr);
            if (isAssignment(op)) return left;
            if (isCompareToBoolean(op)) return boolean_TYPE;
            if (op == COMPARE_TO) return int_TYPE;
            return inferReturnTypeGenerics(left, method, new ArgumentListExpression(expr.getRightExpression()));
        }
        //TODO: other cases
        return null;
    }

    private static ClassNode getGroupOperationResultType(ClassNode a, ClassNode b) {
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

    private ClassNode inferComponentType(final ClassNode containerType) {
        final ClassNode componentType = containerType.getComponentType();
        if (componentType == null) {
            // check if any generic information could help
            GenericsType[] types = containerType.getGenericsTypes();
            if (types != null && types.length == 1) {
                GenericsType type = types[0];
                if (type.isWildcard()) {
                    ClassNode[] upperBounds = type.getUpperBounds();
                    if (upperBounds.length==1) {
                        return upperBounds[0];
                    }
                    ClassNode lowerBound = type.getLowerBound();
                    if (lowerBound!=null) return lowerBound;
                }
                return type.getType();
            }
            return OBJECT_TYPE;
        } else {
            return componentType;
        }
    }

    protected MethodNode findMethodOrFail(
            Expression expr,
            ClassNode receiver, String name, ClassNode... args) {
        final List<MethodNode> methods = findMethod(receiver, name, args);
        if (methods.isEmpty()) {
            addStaticTypeError("Cannot find matching method " + receiver.getText() + "#" + toMethodParametersString(name, args), expr);
        } else {
            if (areCategoryMethodCalls(methods, name, args)) {
                addCategoryMethodCallError(expr);
            }
            if (methods.size() == 1) {
                return methods.get(0);
            } else {
                addAmbiguousErrorMessage(methods, name, args, expr);
            }
        }
        return null;
    }

    private void addAmbiguousErrorMessage(final List<MethodNode> foundMethods, final String name, final ClassNode[] args, final Expression expr) {
        addStaticTypeError("Reference to method is ambiguous. Cannot choose between " + foundMethods, expr);
    }

    private void addCategoryMethodCallError(final Expression call) {
        addStaticTypeError("Due to their dynamic nature, usage of categories is not possible with static type checking active", call);
    }

    private boolean areCategoryMethodCalls(final List<MethodNode> foundMethods, final String name, final ClassNode[] args) {
        boolean category = false;
        if ("use".equals(name) && args!=null && args.length==2 && args[1].equals(ClassHelper.CLOSURE_TYPE)) {
            category = true;
            for (MethodNode method : foundMethods) {
                if (!(method instanceof ExtensionMethodNode) || !((ExtensionMethodNode)method).getExtensionMethodNode().getDeclaringClass().equals(DGM_CLASSNODE)) {
                    category = false;
                }
            }
        }
        return category;
    }

    private List<MethodNode> findMethod(
            ClassNode receiver, String name, ClassNode... args) {
        if (isPrimitiveType(receiver)) receiver = getWrapper(receiver);
        List<MethodNode> methods;
        if ("<init>".equals(name)) {
            methods = new ArrayList<MethodNode>(receiver.getDeclaredConstructors());
            if (methods.isEmpty()) {
                MethodNode node = new ConstructorNode(Opcodes.ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
                node.setDeclaringClass(receiver);
                return Collections.singletonList(node);
            }
        } else {
            methods = receiver.getMethods(name);
            if (receiver.isInterface()) {
                ClassNode[] interfaces = receiver.getInterfaces();
                if (interfaces!=null && interfaces.length>0) {
                    methods = new ArrayList<MethodNode>(methods);
                    for (ClassNode node : interfaces) {
                        List<MethodNode> intfMethods = node.getMethods(name);
                        methods.addAll(intfMethods);
                    }
                }
            }
            if (closureExpression == null) {
                // not in a closure
                ClassNode parent = receiver;
                while (parent instanceof InnerClassNode && !parent.isStaticClass()) {
                    parent = receiver.getOuterClass();
                    methods.addAll(parent.getMethods(name));
                }
            }
            if (methods.isEmpty() && (args == null || args.length == 0)) {
                // check if it's a property
                String pname = null;
                if (name.startsWith("get")) {
                    pname = java.beans.Introspector.decapitalize(name.substring(3));
                } else if (name.startsWith("is")) {
                    pname = java.beans.Introspector.decapitalize(name.substring(2));
                }
                if (pname != null) {
                    // we don't use property exists there because findMethod is called on super clases recursively
                    PropertyNode property = null;
                    ClassNode curNode = receiver;
                    while (property==null && curNode!=null) {
                        property = curNode.getProperty(pname);
                        curNode = curNode.getSuperClass();
                    }
                    if (property != null) {
                        MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC, property.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
                        node.setDeclaringClass(receiver);
                        return Collections.singletonList(
                                node);

                    }
                }
            } else if (methods.isEmpty() && args != null && args.length == 1) {
                // maybe we are looking for a setter ?
                if (name.startsWith("set")) {
                    String pname = java.beans.Introspector.decapitalize(name.substring(3));
                    ClassNode curNode = receiver;
                    PropertyNode property = null;
                    while (property==null && curNode!=null) {
                        property = curNode.getProperty(pname);
                        curNode = curNode.getSuperClass();
                    }
                    if (property != null) {
                        ClassNode type = property.getOriginType();
                        if (implementsInterfaceOrIsSubclassOf(args[0], type)) {
                            MethodNode node = new MethodNode(name, Opcodes.ACC_PUBLIC, VOID_TYPE, new Parameter[]{
                                    new Parameter(type, "arg")
                            }, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
                            node.setDeclaringClass(receiver);
                            return Collections.singletonList(node);
                        } else {
                            System.out.println();
                        }
                    }
                }
            }
        }


        List<MethodNode> chosen = chooseBestMethod(receiver, methods, args);
        if (!chosen.isEmpty()) return chosen;
        // perform a lookup in DGM methods
        methods.clear();
        chosen = findDGMMethodsByNameAndArguments(receiver, name, args, methods);
        if (!chosen.isEmpty()) {
            return chosen;
        }

        if (receiver == ClassHelper.GSTRING_TYPE) return findMethod(ClassHelper.STRING_TYPE, name, args);

        if (pluginFactory != null) {
            TypeCheckerPlugin plugin = pluginFactory.getTypeCheckerPlugin(classNode);
            if (plugin != null) {
                List<MethodNode> methodNodes = plugin.findMethod(receiver, name, args);
                if (methodNodes != null && !methodNodes.isEmpty()) return methodNodes;
            }
        }

        return EMPTY_METHODNODE_LIST;
    }

    protected ClassNode getType(ASTNode exp) {
        ClassNode cn = (ClassNode) exp.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (cn != null) return cn;
        if (exp instanceof ClassExpression) {
            ClassNode node = CLASS_Type.getPlainNodeReference();
            node.setGenericsTypes(new GenericsType[]{
                    new GenericsType(((ClassExpression) exp).getType())
            });
            return node;
        } else if (exp instanceof VariableExpression) {
            VariableExpression vexp = (VariableExpression) exp;
            if (vexp == VariableExpression.THIS_EXPRESSION) return classNode;
            if (vexp == VariableExpression.SUPER_EXPRESSION) return classNode.getSuperClass();
            final Variable variable = vexp.getAccessedVariable();
            if (variable != null && variable != vexp && variable instanceof VariableExpression) {
                return getType((Expression) variable);
            }
            if (variable instanceof Parameter) {
                Parameter parameter = (Parameter) variable;
                ClassNode type = forLoopVariableTypes.get(parameter);
                if (type != null) return type;
            }
        } else if (exp instanceof PropertyExpression) {
            PropertyExpression pexp = (PropertyExpression) exp;
            ClassNode objectExpType = getType(pexp.getObjectExpression());
            if ((LIST_TYPE.equals(objectExpType) || objectExpType.implementsInterface(LIST_TYPE)) && pexp.isSpreadSafe()) {
                // list*.property syntax
                // todo : type inferrence on list content when possible
                return LIST_TYPE;
            } else if ((objectExpType.equals(MAP_TYPE) || objectExpType.implementsInterface(MAP_TYPE)) && pexp.isSpreadSafe()) {
                // map*.property syntax
                // only "key" and "value" are allowed
                String propertyName = pexp.getPropertyAsString();
                GenericsType[] types = objectExpType.getGenericsTypes();
                if ("key".equals(propertyName)) {
                    if (types.length == 2) {
                        ClassNode listKey = LIST_TYPE.getPlainNodeReference();
                        listKey.setGenericsTypes(new GenericsType[]{types[0]});
                        return listKey;
                    }
                } else if ("value".equals(propertyName)) {
                    if (types.length == 2) {
                        ClassNode listValue = LIST_TYPE.getPlainNodeReference();
                        listValue.setGenericsTypes(new GenericsType[]{types[1]});
                        return listValue;
                    }
                } else {
                    addStaticTypeError("Spread operator on map only allows one of [key,value]", pexp);
                }
                return LIST_TYPE;
            } else if (objectExpType.isEnum()) {
                return objectExpType;
            } else {
                final AtomicReference<ClassNode> result = new AtomicReference<ClassNode>(ClassHelper.VOID_TYPE);
                existsProperty(pexp, false, new PropertyLookupVisitor(result));
                return result.get();
            }
        }
        if (exp instanceof ListExpression) {
            return inferListExpressionType((ListExpression) exp);
        } else if (exp instanceof MapExpression) {
            return inferMapExpressionType((MapExpression) exp);
        }
        if (exp instanceof MethodNode) {
            ClassNode ret = (ClassNode) exp.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
            return ret != null ? ret : ((MethodNode) exp).getReturnType();
        }
        if (exp instanceof ClosureExpression) {
            ClassNode irt = (ClassNode) exp.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE);
            if (irt != null) {
                irt = wrapTypeIfNecessary(irt);
                ClassNode result = CLOSURE_TYPE.getPlainNodeReference();
                result.setGenericsTypes(new GenericsType[]{new GenericsType(irt)});
                return result;
            }
        }
        if (exp instanceof RangeExpression) {
            ClassNode plain = ClassHelper.RANGE_TYPE.getPlainNodeReference();
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
        return exp instanceof VariableExpression ? ((VariableExpression) exp).getOriginType() : ((Expression) exp).getType();
    }

    private ClassNode inferListExpressionType(final ListExpression list) {
        List<Expression> expressions = list.getExpressions();
        if (expressions.isEmpty()) {
            // cannot infer, return list type
            return list.getType();
        }
        ClassNode listType = list.getType();
        GenericsType[] genericsTypes = listType.getGenericsTypes();
        if ((genericsTypes == null
                || genericsTypes.length == 0
                || (genericsTypes.length == 1 && OBJECT_TYPE.equals(genericsTypes[0].getType())))
                && (!expressions.isEmpty())) {
            // maybe we can infer the component type
            List<ClassNode> nodes = new LinkedList<ClassNode>();
            for (Expression expression : expressions) {
                if (expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue() == null) {
                    // a null element is found in the list, skip it because we'll use the other elements from the list
                } else {
                    nodes.add(getType(expression));
                }
            }
            if (nodes.isEmpty()) {
                // every element was the null constant
                return listType;
            }
            ClassNode superType = getWrapper(lowestUpperBound(nodes)); // to be used in generics, type must be boxed
            ClassNode inferred = listType.getPlainNodeReference();
            inferred.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(superType))});
            return inferred;
        }
        return listType;
    }

    private ClassNode inferMapExpressionType(final MapExpression map) {
        ClassNode mapType = map.getType();
        List<MapEntryExpression> entryExpressions = map.getMapEntryExpressions();
        if (entryExpressions.isEmpty()) return mapType;
        GenericsType[] genericsTypes = mapType.getGenericsTypes();
        if (genericsTypes == null
                || genericsTypes.length < 2
                || (genericsTypes.length == 2 && OBJECT_TYPE.equals(genericsTypes[0].getType()) && OBJECT_TYPE.equals(genericsTypes[1].getType()))) {
            List<ClassNode> keyTypes = new LinkedList<ClassNode>();
            List<ClassNode> valueTypes = new LinkedList<ClassNode>();
            for (MapEntryExpression entryExpression : entryExpressions) {
                keyTypes.add(getType(entryExpression.getKeyExpression()));
                valueTypes.add(getType(entryExpression.getValueExpression()));
            }
            ClassNode keyType = getWrapper(lowestUpperBound(keyTypes));  // to be used in generics, type must be boxed
            ClassNode valueType = getWrapper(lowestUpperBound(valueTypes));  // to be used in generics, type must be boxed
            if (!OBJECT_TYPE.equals(keyType) || !OBJECT_TYPE.equals(valueType)) {
                ClassNode inferred = mapType.getPlainNodeReference();
                inferred.setGenericsTypes(new GenericsType[]{new GenericsType(wrapTypeIfNecessary(keyType)), new GenericsType(wrapTypeIfNecessary(valueType))});
                return inferred;
            }
        }
        return mapType;
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
    private ClassNode inferReturnTypeGenerics(final ClassNode receiver, final MethodNode method, final Expression arguments) {
        ClassNode returnType = method.getReturnType();
        if (method instanceof ExtensionMethodNode
                && (isUsingGenericsOrIsArrayUsingGenerics(returnType))) {
            // check if the placeholder corresponds to the placeholder of the first parameter
            ExtensionMethodNode emn = (ExtensionMethodNode) method;
            MethodNode dgmMethod = emn.getExtensionMethodNode();
            ClassNode dc = emn.getDeclaringClass();
            ArgumentListExpression argList = new ArgumentListExpression();
            VariableExpression vexp = new VariableExpression("$foo", receiver);
            argList.addExpression(vexp);
            if (arguments instanceof ArgumentListExpression) {
                List<Expression> expressions = ((ArgumentListExpression) arguments).getExpressions();
                for (Expression arg : expressions) {
                    argList.addExpression(arg);
                }
            } else {
                argList.addExpression(arguments);
            }
            return inferReturnTypeGenerics(dc, dgmMethod, argList);
        }
        if (!isUsingGenericsOrIsArrayUsingGenerics(returnType)) return returnType;
        GenericsType[] returnTypeGenerics = returnType.isArray() ? returnType.getComponentType().getGenericsTypes() : returnType.getGenericsTypes();
        List<GenericsType> placeholders = new LinkedList<GenericsType>();
        for (GenericsType returnTypeGeneric : returnTypeGenerics) {
            if (returnTypeGeneric.isPlaceholder() || returnTypeGeneric.isWildcard()) {
                placeholders.add(returnTypeGeneric);
            }
        }
        if (placeholders.isEmpty()) return returnType; // nothing to infer
        Map<String, GenericsType> resolvedPlaceholders = new HashMap<String, GenericsType>();
        GenericsUtils.extractPlaceholders(receiver, resolvedPlaceholders);
        GenericsUtils.extractPlaceholders(method.getReturnType(), resolvedPlaceholders);
        // then resolve receivers from method arguments
        Parameter[] parameters = method.getParameters();
        boolean isVargs = isVargs(parameters);
        ArgumentListExpression argList = InvocationWriter.makeArgumentList(arguments);
        List<Expression> expressions = argList.getExpressions();
        int paramLength = parameters.length;
        if (expressions.size() >= paramLength) {
            for (int i = 0; i < paramLength; i++) {
                boolean lastArg = i == paramLength - 1;
                ClassNode type = parameters[i].getType();
                if (!type.isUsingGenerics() && type.isArray()) type = type.getComponentType();
                if (type.isUsingGenerics()) {
                    ClassNode actualType = getType(expressions.get(i));
                    if (isVargs && lastArg && actualType.isArray()) {
                        actualType = actualType.getComponentType();
                    }
                    actualType = wrapTypeIfNecessary(actualType);
                    Map<String, GenericsType> typePlaceholders = GenericsUtils.extractPlaceholders(type.isArray() ? type.getComponentType() : type);
                    if (OBJECT_TYPE.equals(type)) {
                        // special case for handing Object<E> -> Object
                        for (String key : typePlaceholders.keySet()) {
                            resolvedPlaceholders.put(key, new GenericsType(actualType));
                        }
                    } else {
                        while (!actualType.equals(type)) {
                            Set<ClassNode> interfaces = actualType.getAllInterfaces();
                            boolean intf = false;
                            for (ClassNode anInterface : interfaces) {
                                if (anInterface.equals(type)) {
                                    intf = true;
                                    actualType = GenericsUtils.parameterizeInterfaceGenerics(actualType, anInterface);
                                }
                            }
                            if (!intf) actualType = actualType.getUnresolvedSuperClass();
                        }
                        Map<String, GenericsType> actualTypePlaceholders = GenericsUtils.extractPlaceholders(actualType);
                        for (Map.Entry<String, GenericsType> typeEntry : actualTypePlaceholders.entrySet()) {
                            String key = typeEntry.getKey();
                            GenericsType value = typeEntry.getValue();
                            GenericsType alias = typePlaceholders.get(key);
                            if (alias != null && alias.isPlaceholder()) {
                                resolvedPlaceholders.put(alias.getName(), value);
                            }
                        }
                    }

                }
            }
        }
        GenericsType[] copy = new GenericsType[returnTypeGenerics.length];
        for (int i = 0; i < copy.length; i++) {
            GenericsType returnTypeGeneric = returnTypeGenerics[i];
            if (returnTypeGeneric.isPlaceholder() || returnTypeGeneric.isWildcard()) {
                GenericsType resolved = resolvedPlaceholders.get(returnTypeGeneric.getName());
                if (resolved == null) resolved = returnTypeGeneric;
                copy[i] = resolved;
            } else {
                copy[i] = returnTypeGeneric;
            }
        }
        if (returnType.equals(OBJECT_TYPE)) {
            if (copy[0].getType().isGenericsPlaceHolder()) return OBJECT_TYPE;
            return copy[0].getType();
        }
        if (returnType.isArray()) {
            returnType = returnType.getComponentType().getPlainNodeReference();
            returnType.setGenericsTypes(copy);
            if (OBJECT_TYPE.equals(returnType)) {
                // replace Object<Component> with Component
                returnType = copy[0].getType();
            }
            returnType = returnType.makeArray();
        } else {
            returnType = returnType.getPlainNodeReference();
            returnType.setGenericsTypes(copy);
        }
        if (returnType.equals(Annotation_TYPE) && returnType.getGenericsTypes() != null && !returnType.getGenericsTypes()[0].isPlaceholder()) {
            return returnType.getGenericsTypes()[0].getType();
        }
        return returnType;
    }

    private void typeCheckMethodsWithGenerics(ClassNode receiver, ClassNode[] arguments, MethodNode candidateMethod, Expression location) {
        if (!isUsingGenericsOrIsArrayUsingGenerics(receiver)) return;
        boolean failure = false;
        GenericsType[] methodGenericTypes = null;
        ClassNode methodNodeReceiver = candidateMethod.getDeclaringClass();
        if (!implementsInterfaceOrIsSubclassOf(receiver, methodNodeReceiver) || !isUsingGenericsOrIsArrayUsingGenerics(methodNodeReceiver))
            return;
        // both candidate method and receiver have generic information so a check is possible
        Parameter[] parameters = candidateMethod.getParameters();
        int argNum = 0;
        for (Parameter parameter : parameters) {
            ClassNode type = parameter.getType();
            if (type.isUsingGenerics()) {
                methodGenericTypes =
                        GenericsUtils.alignGenericTypes(
                                receiver.redirect().getGenericsTypes(),
                                receiver.getGenericsTypes(),
                                type.getGenericsTypes());
                if (methodGenericTypes.length == 1) {
                    ClassNode nodeType = getWrapper(methodGenericTypes[0].getType());
                    GenericsType[] argumentGenericTypes = arguments[argNum].getGenericsTypes();
                    ClassNode actualType = argumentGenericTypes != null ? getWrapper(argumentGenericTypes[0].getType()) : nodeType;
                    if (!implementsInterfaceOrIsSubclassOf(actualType, nodeType)) {
                        failure = true;
                    }
                } else {
                    // not sure this is possible !
                }
            } else if (type.isArray() && type.getComponentType().isUsingGenerics()) {
                ClassNode componentType = type.getComponentType();
                methodGenericTypes =
                        GenericsUtils.alignGenericTypes(
                                receiver.redirect().getGenericsTypes(),
                                receiver.getGenericsTypes(),
                                componentType.getGenericsTypes());
                if (methodGenericTypes.length == 1) {
                    ClassNode nodeType = getWrapper(methodGenericTypes[0].getType());
                    ClassNode actualType = getWrapper(arguments[argNum].getComponentType());
                    if (!implementsInterfaceOrIsSubclassOf(actualType, nodeType)) {
                        failure = true;
                        // for proper error message
                        GenericsType baseGT = methodGenericTypes[0];
                        methodGenericTypes[0] = new GenericsType(baseGT.getType(), baseGT.getUpperBounds(), baseGT.getLowerBound());
                        methodGenericTypes[0].setType(methodGenericTypes[0].getType().makeArray());
                    }
                } else {
                    // not sure this is possible !
                }
            }
            argNum++;
        }
        if (failure) {
            ClassNode[] parameterTypes = new ClassNode[methodGenericTypes.length];
            for (int i = 0; i < methodGenericTypes.length; i++) {
                parameterTypes[i] = methodGenericTypes[i].getType();
            }
            addStaticTypeError("Cannot call " + receiver.getName() + "#" +
                    toMethodParametersString(candidateMethod.getName(), parameterTypes) +
                    " with arguments " + formatArgumentList(arguments), location);
        }
    }

    private static String formatArgumentList(ClassNode[] nodes) {
        if (nodes == null) return "[]";
        StringBuilder sb = new StringBuilder(24 * nodes.length);
        sb.append("[");
        for (ClassNode node : nodes) {
            sb.append(node.toString(false));
            sb.append(", ");
        }
        if (sb.length() > 1) {
            sb.setCharAt(sb.length() - 2, ']');
        }
        return sb.toString();
    }

    @Override
    protected void addError(final String msg, final ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        Long err = ((long) expr.getLineNumber()) << 16 + expr.getColumnNumber();
        if (!reportedErrors.contains(err)) {
            errorCollector.addErrorAndContinue(
                    new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
            );
            reportedErrors.add(err);
        }
    }

    protected void addStaticTypeError(final String msg, final ASTNode expr) {
        if (expr.getColumnNumber() > 0 && expr.getLineNumber() > 0) {
            addError(StaticTypesTransformation.STATIC_ERROR_PREFIX + msg, expr);
        } else {
            // ignore errors which are related to unknown source locations
            // because they are likely related to generated code
        }
    }

    public void setMethodsToBeVisited(final Set<MethodNode> methodsToBeVisited) {
        this.methodsToBeVisited = methodsToBeVisited;
    }

    public void performSecondPass() {
        for (Expression expression : secondPassExpressions) {
            if (expression instanceof MethodCallExpression) {
                MethodCallExpression call = (MethodCallExpression) expression;
                Expression objectExpression = call.getObjectExpression();
                if (objectExpression instanceof VariableExpression) {
                    // this should always be the case, but adding a test is safer
                    Variable target = findTargetVariable((VariableExpression) objectExpression);
                    if (target instanceof VariableExpression) {
                        VariableExpression var = (VariableExpression) target;
                        List<ClassNode> classNodes = closureSharedVariablesAssignmentTypes.get(var);
                        if (classNodes != null && classNodes.size() > 1) {
                            ClassNode lub = lowestUpperBound(classNodes);
                            MethodNode methodNode = (MethodNode) call.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                            // we must check that such a method exists on the LUB
                            Parameter[] parameters = methodNode.getParameters();
                            ClassNode[] params = new ClassNode[parameters.length];
                            for (int i = 0; i < params.length; i++) {
                                params[i] = parameters[i].getType();
                            }
                            List<MethodNode> method = findMethod(lub, methodNode.getName(), params);
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
    }

    /**
     * Returns a wrapped type if, and only if, the provided class node is a primitive type.
     * This method differs from {@link ClassHelper#getWrapper(org.codehaus.groovy.ast.ClassNode)} as it will
     * return the same instance if the provided type is not a generic type.
     *
     * @param type
     * @return
     */
    private static ClassNode wrapTypeIfNecessary(ClassNode type) {
        if (isPrimitiveType(type)) return getWrapper(type);
        return type;
    }

    private static boolean isClassInnerClassOrEqualTo(ClassNode toBeChecked, ClassNode start) {
        if (start == toBeChecked) return true;
        if (start instanceof InnerClassNode) {
            return isClassInnerClassOrEqualTo(toBeChecked, start.getOuterClass());
        }
        return false;
    }

    /**
     * A visitor used as a callback to {@link StaticTypeCheckingVisitor#existsProperty(org.codehaus.groovy.ast.expr.PropertyExpression, boolean, org.codehaus.groovy.ast.ClassCodeVisitorSupport)}
     * which will return set the type of the found property in the provided reference.
     */
    private static class PropertyLookupVisitor extends ClassCodeVisitorSupport {
        private final AtomicReference<ClassNode> result;

        public PropertyLookupVisitor(final AtomicReference<ClassNode> result) {
            this.result = result;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitMethod(final MethodNode node) {
            result.set(node.getReturnType());
        }

        @Override
        public void visitProperty(final PropertyNode node) {
            result.set(node.getType());
        }

        @Override
        public void visitField(final FieldNode field) {
            result.set(field.getType());
        }
    }

    private class VariableExpressionTypeMemoizer extends ClassCodeVisitorSupport {
        private final Map<VariableExpression, ClassNode> varOrigType;

        public VariableExpressionTypeMemoizer(final Map<VariableExpression, ClassNode> varOrigType) {
            this.varOrigType = varOrigType;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return source;
        }

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            super.visitVariableExpression(expression);
            Variable var = findTargetVariable(expression);
            if (var instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) var;
                varOrigType.put(ve, (ClassNode) ve.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE));
            }
        }
    }

    // ------------------- codecs for method return type signatures ------------------------------

    protected static interface SignatureCodec {
        String encode(ClassNode node);

        ClassNode decode(String signature);
    }

    private static class SignatureCodecVersion1 implements SignatureCodec {

        private void doEncode(final ClassNode node, DataOutputStream dos) throws IOException {
            dos.writeUTF(node.getClass().getSimpleName());
            if (node instanceof UnionTypeClassNode) {
                UnionTypeClassNode union = (UnionTypeClassNode) node;
                ClassNode[] delegates = union.getDelegates();
                dos.writeInt(delegates.length);
                for (ClassNode delegate : delegates) {
                    doEncode(delegate, dos);
                }
                return;
            } else if (node instanceof LowestUpperBoundClassNode) {
                LowestUpperBoundClassNode lub = (LowestUpperBoundClassNode) node;
                dos.writeUTF(lub.getLubName());
                doEncode(lub.getUnresolvedSuperClass(), dos);
                ClassNode[] interfaces = lub.getInterfaces();
                if (interfaces == null) {
                    dos.writeInt(-1);
                } else {
                    dos.writeInt(interfaces.length);
                    for (ClassNode anInterface : interfaces) {
                        doEncode(anInterface, dos);
                    }
                }
                return;
            }
            if (node.isArray()) {
                dos.writeBoolean(true);
                doEncode(node.getComponentType(), dos);
            } else {
                dos.writeBoolean(false);
                dos.writeUTF(BytecodeHelper.getTypeDescription(node));
                dos.writeBoolean(node.isUsingGenerics());
                GenericsType[] genericsTypes = node.getGenericsTypes();
                if (genericsTypes == null) {
                    dos.writeInt(-1);
                } else {
                    dos.writeInt(genericsTypes.length);
                    for (GenericsType type : genericsTypes) {
                        dos.writeBoolean(type.isPlaceholder());
                        dos.writeBoolean(type.isWildcard());
                        doEncode(type.getType(), dos);
                        ClassNode lb = type.getLowerBound();
                        if (lb == null) {
                            dos.writeBoolean(false);
                        } else {
                            dos.writeBoolean(true);
                            doEncode(lb, dos);
                        }
                        ClassNode[] upperBounds = type.getUpperBounds();
                        if (upperBounds == null) {
                            dos.writeInt(-1);
                        } else {
                            dos.writeInt(upperBounds.length);
                            for (ClassNode bound : upperBounds) {
                                doEncode(bound, dos);
                            }
                        }
                    }
                }
            }
        }

        public String encode(final ClassNode node) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
            DataOutputStream dos = new DataOutputStream(baos);
            StringWriter wrt = new StringWriter();
            String encoded = null;
            try {
                doEncode(node, dos);
                EncodingGroovyMethods.encodeBase64(baos.toByteArray()).writeTo(wrt);
                encoded = wrt.toString();
            } catch (IOException e) {
                throw new GroovyRuntimeException("Unable to serialize type information", e);
            }
            return encoded;
        }

        private ClassNode doDecode(final DataInputStream dis) throws IOException {
            String classNodeType = dis.readUTF();
            if (UnionTypeClassNode.class.getSimpleName().equals(classNodeType)) {
                int len = dis.readInt();
                ClassNode[] delegates = new ClassNode[len];
                for (int i = 0; i < len; i++) {
                    delegates[i] = doDecode(dis);
                }
                return new UnionTypeClassNode(delegates);
            } else if (WideningCategories.LowestUpperBoundClassNode.class.getSimpleName().equals(classNodeType)) {
                String name = dis.readUTF();
                ClassNode upper = doDecode(dis);
                int len = dis.readInt();
                ClassNode[] interfaces = null;
                if (len >= 0) {
                    interfaces = new ClassNode[len];
                    for (int i = 0; i < len; i++) {
                        interfaces[i] = doDecode(dis);
                    }
                }
                return new LowestUpperBoundClassNode(name, upper, interfaces);
            }
            boolean makeArray = dis.readBoolean();
            if (makeArray) {
                return doDecode(dis).makeArray();
            }
            String typedesc = dis.readUTF();
            char typeCode = typedesc.charAt(0);
            ClassNode result = OBJECT_TYPE;
            if (typeCode == 'L') {
                // object type
                String className = typedesc.replace('/', '.').substring(1, typedesc.length() - 1);
                try {
                    result = ClassHelper.make(Class.forName(className)).getPlainNodeReference();
                } catch (ClassNotFoundException e) {
                    result = ClassHelper.make(className);
                }
                result.setUsingGenerics(dis.readBoolean());
                int len = dis.readInt();
                if (len >= 0) {
                    GenericsType[] gts = new GenericsType[len];
                    for (int i = 0; i < len; i++) {
                        boolean placeholder = dis.readBoolean();
                        boolean wildcard = dis.readBoolean();
                        ClassNode type = doDecode(dis);
                        boolean low = dis.readBoolean();
                        ClassNode lb = null;
                        if (low) {
                            lb = doDecode(dis);
                        }
                        int upc = dis.readInt();
                        ClassNode[] ups = null;
                        if (upc >= 0) {
                            ups = new ClassNode[upc];
                            for (int j = 0; j < upc; j++) {
                                ups[j] = doDecode(dis);
                            }
                        }
                        GenericsType gt = new GenericsType(
                                type, ups, lb
                        );
                        gt.setPlaceholder(placeholder);
                        gt.setWildcard(wildcard);
                        gts[i] = gt;
                    }
                    result.setGenericsTypes(gts);
                }
            } else {
                // primitive type
                switch (typeCode) {
                    case 'I': result = int_TYPE; break;
                    case 'Z': result = boolean_TYPE; break;
                    case 'B': result = byte_TYPE; break;
                    case 'C': result = char_TYPE; break;
                    case 'S': result = short_TYPE; break;
                    case 'D': result = double_TYPE; break;
                    case 'F': result = float_TYPE; break;
                    case 'J': result = long_TYPE; break;
                    case 'V': result = VOID_TYPE; break;
                }
            }
            return result;
        }

        public ClassNode decode(final String signature) {
            DataInputStream dis = new DataInputStream(
                    new ByteArrayInputStream(EncodingGroovyMethods.decodeBase64(signature)));
            try {
                return doDecode(dis);
            } catch (IOException e) {
                throw new GroovyRuntimeException("Unable to read type information", e);
            }
        }
    }

    protected static class SignatureCodecFactory {
        static SignatureCodec getCodec(int version) {
            switch (version) {
                case 1: return new SignatureCodecVersion1();
                default: return null;
            }
        }
    }
}
