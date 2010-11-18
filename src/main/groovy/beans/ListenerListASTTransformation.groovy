package groovy.beans

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

/**
 * Handles generation of code for the {@code @ListenerList} annotation.
 * <p/>
 * Generally, it adds the needed add&lt;Listener&gt;, remove&lt;Listener&gt; and
 * get&lt;Listener&gt;s methods to support the Java Beans API.
 * <p/>
 * Additionally it adds corresponding fire&lt;Event&gt; methods.
 * <p/>
 *
 * @author Alexander Klein
 * @author Hamlet D'Arcy
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class ListenerListASTTransformation implements ASTTransformation, Opcodes {
    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: ${node.class} / ${parent.class}")
        }
        AnnotationNode node = nodes[0]
        AnnotatedNode parent = nodes[1]
        FieldNode field

        ClassNode declaringClass = parent.declaringClass
        if (parent instanceof ClassNode) {
            declaringClass = (ClassNode) parent
            ClassNode listener = node.getMember('listener')?.type
            if (!listener) {
                listener = ClassHelper.make(Map)
            }
            def listName = node.getMember('name')?.value ?: listener.nameWithoutPackage
            listName = listName[0].toLowerCase() + listName[1..listName.length() - 1] + 'List'
            field = declaringClass.getDeclaredField(listName)
            if (!field) {
                field = new FieldNode(listName, Opcodes.ACC_PRIVATE, ClassHelper.make(List), declaringClass, new ListExpression())
                declaringClass.addField(field)
            }
        } else {
            field = parent
        }

        ClassNode parentClass = field.type
        boolean isCollection = parentClass.isDerivedFrom(ClassHelper.make(Collection)) || parentClass.implementsInterface(ClassHelper.make(Collection))
        boolean isObject = parentClass == ClassHelper.make(Object);
        if (!isCollection && !isObject) {
            source.errorCollector.addErrorAndContinue(
                    new SyntaxErrorMessage(new SyntaxException(
                            '@groovy.beans.ListenerList can only annotate object or collection properties.',
                            node.lineNumber,
                            node.columnNumber),
                            source));
        }
        if (!field.initialValueExpression) {
            field.initialValueExpression = new ListExpression()
        }


        ClassNode listener = node.getMember('listener')?.type
        if (!listener) {
            def types = field.type.genericsTypes
            if (types && !types[0].wildcard) {
                listener = types[0].type
            } else {
                listener = ClassHelper.make(Map)
            }
        }
        ClassNode event = node.getMember('event')?.type
        if (!event) {
            if (listener.abstractMethods && listener.abstractMethods[0].parameters?.size() == 1) {
                event = listener.abstractMethods[0].parameters[0].type
            } else {
                event = ClassHelper.make(Object)
            }
        }
        def name = node.getMember('name')?.value ?: listener.nameWithoutPackage
        def fire = node.getMember('fire')
        def fireList = []
        if (fire instanceof ListExpression) {
            fire.expressions.each {
                fireList << it?.value
            }
        } else if (fire) {
            fireList << fire?.value
        }
        if (!fireList) {
            for (MethodNode m: listener.methods) {
                // abstract method with exactly one parameter of type <event>
                if (m.isAbstract() && m.parameters.size() == 1 && event.isDerivedFrom(m.parameters[0].type)) {
                    fireList << m.name
                }
            }
        }

        def synchronize = node.getMember('synchronize') 
        addAddListener(source, node, declaringClass, field, listener, name, synchronize)
        addRemoveListener(source, node, declaringClass, field, listener, name, synchronize)
        addGetListeners(source, node, declaringClass, field, listener, name, synchronize)

        def constructors = [[event]]
        event.declaredConstructors.each { con ->
            constructors << con.parameters.type
        }

        fireList.each {
            def parts = it.split('->')
            def fireMethod = parts[0].trim()
            def eventMethod = (parts.size() > 1) ? parts[1].trim() : null
            if (!eventMethod) {
                boolean mapListener = listener.isDerivedFrom(ClassHelper.make(Map)) || listener.implementsInterface(ClassHelper.make(Map))
                eventMethod = (listener.abstractMethods.name.contains(fireMethod)) ? fireMethod : (mapListener ? fireMethod : listener.abstractMethods[0].name)
            }
            addFireMethods(source, node, declaringClass, field, event, eventMethod, fireMethod, constructors)
        }
    }

    /**
     * Adds the add&lt;Listener&gt; method like:
     * <p/>
     * <pre>
     * synchronized void add${name.capitalize}(${listener.name} listener) {*   if (listener == null)
     *     return
     *   if (${field.name} == null)
     *     ${field.name} = []
     *   ${field.name}.add(listener)
     *}* </pre>
     */
    void addAddListener(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode listener, String name, synchronize) {

        def methodModifiers = synchronize ? Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNCHRONIZED : Opcodes.ACC_PUBLIC 
        def methodReturnType = ClassHelper.make(Void.TYPE)
        def methodName = "add${name.capitalize()}"
        def methodParameter = [new Parameter(listener, 'listener')] as Parameter[]

        if (!declaringClass.hasMethod(methodName, methodParameter)) {
            BlockStatement block = new BlockStatement()
            block.addStatements([
                    new IfStatement(
                            new BooleanExpression(
                                    new BinaryExpression(
                                            new VariableExpression('listener'),
                                            Token.newSymbol(Types.COMPARE_EQUAL, 0, 0),
                                            ConstantExpression.NULL
                                    )
                            ),
                            new ReturnStatement(ConstantExpression.NULL),
                            new EmptyStatement()
                    ),
                    new IfStatement(
                            new BooleanExpression(
                                    new BinaryExpression(
                                            new VariableExpression(field.name),
                                            Token.newSymbol(Types.COMPARE_EQUAL, 0, 0),
                                            ConstantExpression.NULL
                                    )
                            ),
                            new ExpressionStatement(
                                    new BinaryExpression(
                                            new VariableExpression(field.name),
                                            Token.newSymbol(Types.EQUAL, 0, 0),
                                            new ListExpression()
                                    )
                            ),
                            new EmptyStatement()
                    ),
                    new ExpressionStatement(
                            new MethodCallExpression(new VariableExpression(field.name), new ConstantExpression('add'), new ArgumentListExpression(new VariableExpression('listener')))
                    )
            ])
            declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
        }
    }

    /**
     * Adds the remove<Listener> method like:
     * <p/>
     * <pre>
     * synchronized void remove${name.capitalize}(${listener.name} listener) {*   if (listener == null)
     *     return
     *   if (${field.name} == null)
     *     ${field.name} = []
     *   ${field.name}.remove(listener)
     *}* </pre>
     */
    void addRemoveListener(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode listener, String name, synchronize) {
        def methodModifiers = synchronize ? Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNCHRONIZED : Opcodes.ACC_PUBLIC
        def methodReturnType = ClassHelper.make(Void.TYPE)
        def methodName = "remove${name.capitalize()}"
        def methodParameter = [new Parameter(listener, 'listener')] as Parameter[]

        if (!declaringClass.hasMethod(methodName, methodParameter)) {
            BlockStatement block = new BlockStatement()
            block.addStatements([
                    new IfStatement(
                            new BooleanExpression(
                                    new BinaryExpression(
                                            new VariableExpression('listener'),
                                            Token.newSymbol(Types.COMPARE_EQUAL, 0, 0),
                                            ConstantExpression.NULL
                                    )
                            ),
                            new ReturnStatement(ConstantExpression.NULL),
                            new EmptyStatement()
                    ),
                    new IfStatement(
                            new BooleanExpression(
                                    new BinaryExpression(
                                            new VariableExpression(field.name),
                                            Token.newSymbol(Types.COMPARE_EQUAL, 0, 0),
                                            ConstantExpression.NULL
                                    )
                            ),
                            new ExpressionStatement(
                                    new BinaryExpression(
                                            new VariableExpression(field.name),
                                            Token.newSymbol(Types.EQUAL, 0, 0),
                                            new ListExpression()
                                    )
                            ),
                            new EmptyStatement()
                    ),
                    new ExpressionStatement(
                            new MethodCallExpression(new VariableExpression(field.name), new ConstantExpression('remove'), new ArgumentListExpression(new VariableExpression("listener")))
                    )
            ])
            declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
        }
    }

    /**
     * Adds the get&lt;Listener&gt;s method like:
     * <p/>
     * <pre>
     * synchronized ${name.capitalize}[] get${name.capitalize}s() {*   def __result = []
     *     if (${field.name} != null)
     *       __result.addAll(${field.name})
     *     return __result as ${name.capitalize}[]
     *}* </pre>
     */
    void addGetListeners(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode listener, String name, synchronize) {
        def methodModifiers = synchronize ? Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNCHRONIZED : Opcodes.ACC_PUBLIC
        def methodReturnType = listener.makeArray()
        def methodName = "get${name.capitalize()}s"
        def methodParameter = [] as Parameter[]

        if (!declaringClass.hasMethod(methodName, methodParameter)) {
            BlockStatement block = new BlockStatement()
            block.addStatements([
                    new ExpressionStatement(
                            new DeclarationExpression(
                                    new VariableExpression("__result", ClassHelper.DYNAMIC_TYPE),
                                    Token.newSymbol(Types.EQUALS, 0, 0),
                                    new ListExpression()
                            )),
                    new IfStatement(
                            new BooleanExpression(
                                    new BinaryExpression(
                                            new VariableExpression(field.name),
                                            Token.newSymbol(Types.COMPARE_NOT_EQUAL, 0, 0),
                                            ConstantExpression.NULL
                                    )
                            ),
                            new ExpressionStatement(
                                    new MethodCallExpression(new VariableExpression('__result'), new ConstantExpression('addAll'), new ArgumentListExpression(new VariableExpression(field.name)))
                            ),
                            new EmptyStatement()
                    ),
                    new ReturnStatement(
                            new CastExpression(
                                    methodReturnType,
                                    new VariableExpression('__result')
                            )
                    )
            ])
            declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
        }
    }

    /**
     * Adds the fire&lt;Event&gt; methods like:
     * <p/>
     * <pre>
     * void fire${fireMethod.capitalize()}(${parameterList.join(', ')}){*   if (${field.name} != null) {*     def __list = new ArrayList(${field.name})
     *     __list.each{ listener ->
     *         listener.$eventMethod(${evt})
     *}*}*}* </pre>
     */
    void addFireMethods(SourceUnit source, AnnotationNode node, ClassNode declaringClass, FieldNode field, ClassNode event, String eventMethod, String fireMethod, List constructors) {
        constructors.each { con ->
            def parameters = []
            def parameterMap = [:] as LinkedHashMap
            def parameterList = []
            con.eachWithIndex { item, idx ->
                parameters << new Parameter(item, "p$idx")
                parameterList << "$item.name p$idx"
                parameterMap["p$idx"] = item
            }
            def methodModifiers = Opcodes.ACC_PUBLIC
            def methodReturnType = ClassHelper.make(Void.TYPE)
            def methodName = "fire${fireMethod.capitalize()}"
            def methodParameter = parameters as Parameter[]

            if (!declaringClass.hasMethod(methodName, methodParameter)) {
                def args = new ArgumentListExpression()
                if (parameterMap.size() == 1 && parameterMap.p0 == event) {
                    args.addExpression(new VariableExpression('p0'))
                } else {
                    ArgumentListExpression constrArgs = new ArgumentListExpression()
                    parameterMap.keySet().each {
                        constrArgs.addExpression(new VariableExpression(it))
                    }
                    args.addExpression(new ConstructorCallExpression(event, constrArgs))
                }

                BlockStatement block = new BlockStatement()
                block.addStatements([
                        new IfStatement(
                                new BooleanExpression(
                                        new BinaryExpression(
                                                new VariableExpression(field.name),
                                                Token.newSymbol(Types.COMPARE_NOT_EQUAL, 0, 0),
                                                ConstantExpression.NULL
                                        )
                                ),
                                new BlockStatement([
                                        new ExpressionStatement(
                                                new DeclarationExpression(
                                                        new VariableExpression('__list', ClassHelper.make(ArrayList)),
                                                        Token.newSymbol(Types.EQUALS, 0, 0),
                                                        new ConstructorCallExpression(ClassHelper.make(ArrayList), new ArgumentListExpression(
                                                                new VariableExpression(field.name)
                                                        ))
                                                )
                                        ),
                                        new ForStatement(
                                                new Parameter(ClassHelper.DYNAMIC_TYPE, 'listener'),
                                                new VariableExpression('__list'),
                                                new BlockStatement([
                                                        new ExpressionStatement(
                                                                new MethodCallExpression(
                                                                        new VariableExpression('listener'),
                                                                        eventMethod,
                                                                        args
                                                                )
                                                        )
                                                ], new VariableScope())
                                        )
                                ], new VariableScope()),
                                new EmptyStatement()
                        )
                ])
                declaringClass.addMethod(new MethodNode(methodName, methodModifiers, methodReturnType, methodParameter, [] as ClassNode[], block))
            }
        }
    }
}
