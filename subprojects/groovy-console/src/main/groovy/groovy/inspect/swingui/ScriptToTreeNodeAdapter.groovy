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
package groovy.inspect.swingui

import groovy.text.GStringTemplateEngine
import groovy.text.Template
import groovy.transform.PackageScope
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.DynamicVariable
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ClosureListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.FieldExpression
import org.codehaus.groovy.ast.expr.GStringExpression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.MethodPointerExpression
import org.codehaus.groovy.ast.expr.MethodReferenceExpression
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.PostfixExpression
import org.codehaus.groovy.ast.expr.PrefixExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.RangeExpression
import org.codehaus.groovy.ast.expr.SpreadExpression
import org.codehaus.groovy.ast.expr.SpreadMapExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TernaryExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.ast.expr.UnaryPlusExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.BreakStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.ContinueStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.classgen.asm.BytecodeHelper
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit

import java.util.concurrent.atomic.AtomicBoolean

/**
 * This class controls the conversion from a Groovy script as a String into
 * a tree representation of the AST of that script. The script itself
 * will be a tree node, and each class in the script will be a tree node. The
 * conversion creates tree nodes for any concrete class found within an AST
 * visitor. So, if a tree node should be shown once for each ASTNode and the parent
 * types will not appear as nodes. Custom subclasses of expression types will
 * not appear in the tree.
 *
 * The String label of a tree node is defined by classname in AstBrowserProperties.properties.
 */
class ScriptToTreeNodeAdapter {

    static Properties classNameToStringForm
    boolean showScriptFreeForm, showScriptClass, showClosureClasses
    final GroovyClassLoader classLoader
    final AstBrowserNodeMaker nodeMaker
    private final CompilerConfiguration config

    static {
        try {
            URL url =  ClassLoader.getSystemResource('groovy/inspect/swingui/AstBrowserProperties.groovy')
            if (!url) {
                url = ScriptToTreeNodeAdapter.class.classLoader.getResource('groovy/inspect/swingui/AstBrowserProperties.groovy')
            }
    
            def config = new ConfigSlurper().parse(url)
            classNameToStringForm = config.toProperties()
    
            String home = System.getProperty('user.home')
            if (home) {
                File userFile = new File(home + File.separator + '.groovy/AstBrowserProperties.groovy')
                if (userFile.exists()) {
                    def customConfig = new ConfigSlurper().parse(userFile.toURL())
                    // layer custom string forms onto defaults with putAll, do not replace them
                    classNameToStringForm.putAll(customConfig.toProperties())
                }
            }
        } catch(ex) {
            // on restricted environments like, such calls may fail, but that should not prevent the class
            // from being loaded. Tree nodes can still get rendered with their simple names.
            classNameToStringForm = new Properties()  
        }
    }
    
    ScriptToTreeNodeAdapter(classLoader, showScriptFreeForm, showScriptClass, showClosureClasses, nodeMaker, config = null) {
        this.classLoader = classLoader ?: new GroovyClassLoader(getClass().classLoader)
        this.showScriptFreeForm = showScriptFreeForm
        this.showScriptClass = showScriptClass
        this.showClosureClasses = showClosureClasses
        this.nodeMaker = nodeMaker
        this.config = config
    }

    /**
    * Performs the conversion from script to TreeNode.
     *
     * @param script
     *      a Groovy script in String form
     * @param compilePhase
     *      the int based CompilePhase to compile it to.
     * @param indy
     *      if {@code true} InvokeDynamic (Indy) bytecode is generated
    */
    def compile(String script, int compilePhase, boolean indy=false) {
        def scriptName = 'script' + System.currentTimeMillis() + '.groovy'
        GroovyCodeSource codeSource = new GroovyCodeSource(script, scriptName, '/groovy/script')
        CompilerConfiguration cc = new CompilerConfiguration(config ?: CompilerConfiguration.DEFAULT)
        if (config) {
            cc.addCompilationCustomizers(*config.compilationCustomizers)
        }
        if (indy) {
            cc.optimizationOptions.put(CompilerConfiguration.INVOKEDYNAMIC, true)
        }
        CompilationUnit cu = new CompilationUnit(cc, codeSource.codeSource, classLoader)
        cu.setClassgenCallback(classLoader.createCollector(cu, null))

        TreeNodeBuildingNodeOperation operation = new TreeNodeBuildingNodeOperation(this, showScriptFreeForm, showScriptClass, showClosureClasses)
        cu.addPhaseOperation(operation, compilePhase)
        cu.addSource(codeSource.getName(), script)
        try {
            cu.compile(compilePhase)
        } catch (CompilationFailedException cfe) {
            operation.root.add(nodeMaker.makeNode('Unable to produce AST for this phase due to earlier compilation error:'))
            cfe.message.eachLine {
                operation.root.add(nodeMaker.makeNode(it))
            }
            operation.root.add(nodeMaker.makeNode('Fix the above error(s) and then press Refresh'))
        } catch (Throwable t) {
            operation.root.add(nodeMaker.makeNode('Unable to produce AST for this phase due to an error:'))
            operation.root.add(nodeMaker.makeNode(t))
            operation.root.add(nodeMaker.makeNode('Fix the above error(s) and then press Refresh'))
        }
        return operation.root
    }

    def make(node) {
        nodeMaker.makeNodeWithProperties(getStringForm(node), getPropertyTable(node))
    }

    def make(MethodNode node) {
        def table = getPropertyTable(node)
        extendMethodNodePropertyTable(table, node)

        nodeMaker.makeNodeWithProperties(getStringForm(node), table)
    }

    /**
     * Extends the method node property table by adding custom properties.
     */
    void extendMethodNodePropertyTable(List<List<String>> table, MethodNode node) {
        table << ['descriptor', BytecodeHelper.getMethodDescriptor(node), 'String']
    }

    /**
     * Creates the property table for the node so that the properties view can display nicely.
     */
    private List<List<String>> getPropertyTable(node) {
        node.metaClass.properties?.
            findAll { it.getter }?.
            collect {
                def name = it.name.toString()
                def value
                try {
                    // multiple assignment statements cannot be cast to VariableExpression so
                    // instead reference the value through the leftExpression property, which is the same
                    if (node instanceof DeclarationExpression &&
                            (name == 'variableExpression' || name == 'tupleExpression')) {
                        value = node.leftExpression.toString()
                    } else {
                        value = it.getProperty(node).toString()
                    }
                } catch (GroovyBugError reflectionArtefact) {
                    // compiler throws error if it thinks a field is being accessed
                    // before it is set under certain conditions. It wasn't designed
                    // to be walked reflectively like this.
                    value = null
                }
                def type = it.type.simpleName.toString()
                [name, value, type]
            }?.
            sort { it[0] }
    }

    /**
     * Handles the property file templating for node types.
     */
    private String getStringForm(node) {
        String templateTextForNode = classNameToStringForm[node.class.name]
        if (templateTextForNode) {
            GStringTemplateEngine engine = new GStringTemplateEngine()
            Template template = engine.createTemplate(templateTextForNode)
            Writable writable = template.make([expression: node])
            Writer result = new StringBuilderWriter()
            writable.writeTo(result)
            result.toString()
        } else {
            node.class.simpleName
        }
    }
}

/**
 * This Node Operation builds up a root tree node for the viewer.
 */
class TreeNodeBuildingNodeOperation extends PrimaryClassNodeOperation {

    final root
    final sourceCollected = new AtomicBoolean(false)
    final ScriptToTreeNodeAdapter adapter

    final showScriptFreeForm
    final showScriptClass
    final showClosureClasses

    final nodeMaker

    TreeNodeBuildingNodeOperation(ScriptToTreeNodeAdapter adapter, showScriptFreeForm, showScriptClass) {
        this(adapter, showScriptFreeForm, showScriptClass, false)
    }

    TreeNodeBuildingNodeOperation(ScriptToTreeNodeAdapter adapter, showScriptFreeForm, showScriptClass, showClosureClasses) {
        if (!adapter) throw new IllegalArgumentException('Null: adapter')
        this.adapter = adapter
        this.showScriptFreeForm = showScriptFreeForm
        this.showScriptClass = showScriptClass
        this.showClosureClasses = showClosureClasses
        nodeMaker = adapter.nodeMaker
        root = nodeMaker.makeNode('root')
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        // module node
        if (!sourceCollected.getAndSet(true) && showScriptFreeForm) {
            // display the source unit AST
            ModuleNode ast = source.getAST()
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            ast.getStatementBlock().visit(visitor)
            if (visitor.currentNode) root.add(visitor.currentNode)
            collectModuleNodeMethodData('Methods', ast.getMethods())
        }

        if(classNode.isScript() && !showScriptClass) return

        def child = adapter.make(classNode)
        root.add(child)

        collectConstructorData(child, 'Constructors', classNode)
        collectObjectInitializers(child, 'Object Initializers', classNode)
        collectMethodData(child, 'Methods', classNode)
        collectFieldData(child, 'Fields', classNode)
        collectPropertyData(child, 'Properties', classNode)
        collectAnnotationData(child, 'Annotations', classNode)

        if (showClosureClasses)  {
            makeClosureClassTreeNodes(classNode)
        }
    }

    protected void makeClosureClassTreeNodes(ClassNode classNode) {
        def compileUnit = classNode.compileUnit
        if (!compileUnit.generatedInnerClasses) return

        def innerClassNodes = compileUnit.generatedInnerClasses.values().sort { it.name }
        innerClassNodes.each { InnerClassNode innerClassNode ->
            if (!innerClassNode.implementsInterface(ClassHelper.GENERATED_CLOSURE_Type) && !innerClassNode.implementsInterface(ClassHelper.GENERATED_LAMBDA_TYPE)) return
            if (innerClassNode.outerMostClass != classNode) return

            def child = adapter.make(innerClassNode)
            root.add(child)

            collectConstructorData(child, 'Constructors', innerClassNode)
            collectObjectInitializers(child, 'Object Initializers', innerClassNode)
            collectMethodData(child, 'Methods', innerClassNode)
            collectFieldData(child, 'Fields', innerClassNode)
            collectPropertyData(child, 'Properties', innerClassNode)
            collectAnnotationData(child, 'Annotations', innerClassNode)
        }
    }

    private void collectAnnotationData(parent, String name, ClassNode classNode) {
        def allAnnotations = nodeMaker.makeNode(name)
        if (classNode.annotations) parent.add(allAnnotations)
        classNode.annotations?.each {AnnotationNode annotationNode ->
            def ggrandchild = adapter.make(annotationNode)
            allAnnotations.add(ggrandchild)
        }
    }

    private void collectPropertyData(parent, String name, ClassNode classNode) {
        def allProperties = nodeMaker.makeNode(name)
        if (classNode.properties) parent.add(allProperties)
        classNode.properties?.each {PropertyNode propertyNode ->
            def ggrandchild = adapter.make(propertyNode)
            allProperties.add(ggrandchild)
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            if (propertyNode.field?.initialValueExpression) {
                propertyNode.field.initialValueExpression.visit(visitor)
                ggrandchild.add(visitor.currentNode)
            }
        }
    }

    private void collectFieldData(parent, String name, ClassNode classNode) {
        def allFields = nodeMaker.makeNode(name)
        if (classNode.fields) parent.add(allFields)
        classNode.fields?.each {FieldNode fieldNode ->
            def ggrandchild = adapter.make(fieldNode)
            allFields.add(ggrandchild)
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            if (fieldNode.initialValueExpression) {
                fieldNode.initialValueExpression.visit(visitor)
                if (visitor.currentNode) ggrandchild.add(visitor.currentNode)
            }
        }
    }

    private void collectMethodData(parent, String name, ClassNode classNode) {
        def allMethods = nodeMaker.makeNode(name)
        if (classNode.methods) parent.add(allMethods)

        doCollectMethodData(allMethods, classNode.methods)
    }

    private void collectModuleNodeMethodData(String name, List methods) {
        if(!methods) return
        def allMethods = nodeMaker.makeNode(name)
        root.add(allMethods)

        doCollectMethodData(allMethods, methods)
    }
    
    private void doCollectMethodData(allMethods, List methods) {
        methods?.each {MethodNode methodNode ->
            def ggrandchild = adapter.make(methodNode)
            allMethods.add(ggrandchild)
    
            // print out parameters of method
            methodNode.parameters?.each {Parameter parameter ->
                def gggrandchild = adapter.make(parameter)
                ggrandchild.add(gggrandchild)
                if (parameter.initialExpression) {
                    TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
                    parameter.initialExpression.visit(visitor)
                    if (visitor.currentNode) gggrandchild.add(visitor.currentNode)
                }
            }
    
            // print out code of method
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            if (methodNode.code) {
                methodNode.code.visit(visitor)
                if (visitor.currentNode) ggrandchild.add(visitor.currentNode)
            }
        }
    }

    private void collectConstructorData(parent, String name, ClassNode classNode) {
        def allCtors = nodeMaker.makeNode(name)
        if (classNode.declaredConstructors) parent.add(allCtors)
        classNode.declaredConstructors?.each {ConstructorNode ctorNode ->

            def ggrandchild = adapter.make(ctorNode)
            allCtors.add(ggrandchild)
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            if (ctorNode.code) {
                ctorNode.code.visit(visitor)
                if (visitor.currentNode) ggrandchild.add(visitor.currentNode)
            }
        }

    }

    private void collectObjectInitializers(parent, String name, ClassNode node) {
        List<Statement> initStatements = node.getObjectInitializerStatements()
        if (!initStatements) {
            return
        }
        def allInitializers = nodeMaker.makeNode(name)
        parent.add(allInitializers)
        for (Statement stmt : initStatements) {
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            stmt.visit(visitor)
            if (visitor.currentNode) {
                allInitializers.add(visitor.currentNode)
            }
        }
    }

}

/**
* This AST visitor builds up a TreeNode.
*/
@PackageScope
class TreeNodeBuildingVisitor extends CodeVisitorSupport {

    def currentNode
    private final adapter

    /**
     * Creates the visitor. A file named AstBrowserProperties.groovy is located which is
     * a property files the describes how to represent ASTNode types as Strings.
     */
    TreeNodeBuildingVisitor(adapter) {
        if (!adapter) throw new IllegalArgumentException('Null: adapter')
        this.adapter = adapter
    }

    /**
    * This method looks at the AST node and decides how to represent it in a TreeNode, then it
     * continues walking the tree. If the node and the expectedSubclass are not exactly the same
     * Class object then the node is not added to the tree. This is to eliminate seeing duplicate
     * nodes, for instance seeing an ArgumentListExpression and a TupleExpression in the tree, when
     * an ArgumentList is-a Tuple.
    */
    private void addNode(node, Class expectedSubclass, Closure superMethod) {

        if (expectedSubclass.getName() == node.getClass().getName()) {
            if (currentNode == null) {
                currentNode = adapter.make(node)
                superMethod.call(node)
            } else {
                // visitor works off void methods... so we have to
                // perform a swap to get accumulation like behavior.
                def temp = currentNode
                currentNode = adapter.make(node)

                temp.add(currentNode)
                currentNode.parent = temp
                superMethod.call(node)
                currentNode = temp
            }
        } else {
            superMethod.call(node)
        }
    }

    @Override
    void visitBlockStatement(BlockStatement node) {
        addNode(node, BlockStatement, { super.visitBlockStatement(it) })
    }

    @Override
    void visitForLoop(ForStatement node) {
        addNode(node, ForStatement, { super.visitForLoop(it) })
    }

    @Override
    void visitWhileLoop(WhileStatement node) {
        addNode(node, WhileStatement, { super.visitWhileLoop(it) })
    }

    @Override
    void visitDoWhileLoop(DoWhileStatement node) {
        addNode(node, DoWhileStatement, { super.visitDoWhileLoop(it) })
    }

    @Override
    void visitIfElse(IfStatement node) {
        addNode(node, IfStatement, { super.visitIfElse(it) })
    }

    @Override
    void visitExpressionStatement(ExpressionStatement node) {
        addNode(node, ExpressionStatement, { super.visitExpressionStatement(it) })
    }

    @Override
    void visitReturnStatement(ReturnStatement node) {
        addNode(node, ReturnStatement, { super.visitReturnStatement(it) })
    }

    @Override
    void visitAssertStatement(AssertStatement node) {
        addNode(node, AssertStatement, { super.visitAssertStatement(it) })
    }

    @Override
    void visitTryCatchFinally(TryCatchStatement node) {
        addNode(node, TryCatchStatement, { super.visitTryCatchFinally(it) })
    }

    @Override
    protected void visitEmptyStatement(EmptyStatement node) {
        addNode(node, EmptyStatement, { super.visitEmptyStatement(it) })
    }

    @Override
    void visitSwitch(SwitchStatement node) {
        addNode(node, SwitchStatement, { super.visitSwitch(it) })
    }

    @Override
    void visitCaseStatement(CaseStatement node) {
        addNode(node, CaseStatement, { super.visitCaseStatement(it) })
    }

    @Override
    void visitBreakStatement(BreakStatement node) {
        addNode(node, BreakStatement, { super.visitBreakStatement(it) })
    }

    @Override
    void visitContinueStatement(ContinueStatement node) {
        addNode(node, ContinueStatement, { super.visitContinueStatement(it) })
    }

    @Override
    void visitSynchronizedStatement(SynchronizedStatement node) {
        addNode(node, SynchronizedStatement, { super.visitSynchronizedStatement(it) })
    }

    @Override
    void visitThrowStatement(ThrowStatement node) {
        addNode(node, ThrowStatement, { super.visitThrowStatement(it) })
    }

    @Override
    void visitMethodCallExpression(MethodCallExpression node) {
        addNode(node, MethodCallExpression, { super.visitMethodCallExpression(it) })
    }

    @Override
    void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        addNode(node, StaticMethodCallExpression, { super.visitStaticMethodCallExpression(it) })
    }

    @Override
    void visitConstructorCallExpression(ConstructorCallExpression node) {
        addNode(node, ConstructorCallExpression, { super.visitConstructorCallExpression(it) })
    }

    @Override
    void visitBinaryExpression(BinaryExpression node) {
        addNode(node, BinaryExpression, { super.visitBinaryExpression(it) })
    }

    @Override
    void visitTernaryExpression(TernaryExpression node) {
        addNode(node, TernaryExpression, { super.visitTernaryExpression(it) })
    }

    @Override
    void visitShortTernaryExpression(ElvisOperatorExpression node) {
        addNode(node, ElvisOperatorExpression, { super.visitShortTernaryExpression(it) })
    }

    @Override
    void visitPostfixExpression(PostfixExpression node) {
        addNode(node, PostfixExpression, { super.visitPostfixExpression(it) })
    }

    @Override
    void visitPrefixExpression(PrefixExpression node) {
        addNode(node, PrefixExpression, { super.visitPrefixExpression(it) })
    }

    @Override
    void visitBooleanExpression(BooleanExpression node) {
        addNode(node, BooleanExpression, { super.visitBooleanExpression(it) })
    }

    @Override
    void visitNotExpression(NotExpression node) {
        addNode(node, NotExpression, { super.visitNotExpression(it) })
    }

    @Override
    void visitClosureExpression(ClosureExpression node) {
        addNode(node, ClosureExpression, { 
            it.parameters?.each { parameter -> visitParameter(parameter) }
            super.visitClosureExpression(it)
        })
    }

    @Override
    void visitLambdaExpression(LambdaExpression node) {
        addNode(node, LambdaExpression, {
            // params will be catered for by super call
            //it.parameters?.each { parameter -> visitParameter(parameter) }
            super.visitLambdaExpression(it)
        })
    }

    /**
     * Makes walking parameters look like others in the visitor.
     */
    void visitParameter(Parameter node) {
        addNode(node, Parameter, {
            if (node.initialExpression) {
                node.initialExpression?.visit(this)
            }
        })
    }

    @Override
    void visitTupleExpression(TupleExpression node) {
        addNode(node, TupleExpression, { super.visitTupleExpression(it) })
    }

    @Override
    void visitListExpression(ListExpression node) {
        addNode(node, ListExpression, { super.visitListExpression(it) })
    }

    @Override
    void visitArrayExpression(ArrayExpression node) {
        addNode(node, ArrayExpression, { super.visitArrayExpression(it) })
    }

    @Override
    void visitMapExpression(MapExpression node) {
        addNode(node, MapExpression, { super.visitMapExpression(it) })
    }

    @Override
    void visitMapEntryExpression(MapEntryExpression node) {
        addNode(node, MapEntryExpression, { super.visitMapEntryExpression(it) })
    }

    @Override
    void visitRangeExpression(RangeExpression node) {
        addNode(node, RangeExpression, { super.visitRangeExpression(it) })
    }

    @Override
    void visitSpreadExpression(SpreadExpression node) {
        addNode(node, SpreadExpression, { super.visitSpreadExpression(it) })
    }

    @Override
    void visitSpreadMapExpression(SpreadMapExpression node) {
        addNode(node, SpreadMapExpression, { super.visitSpreadMapExpression(it) })
    }

    @Override
    void visitMethodPointerExpression(MethodPointerExpression node) {
        addNode(node, MethodPointerExpression, { super.visitMethodPointerExpression(it) })
    }

    @Override
    void visitMethodReferenceExpression(MethodReferenceExpression node) {
        addNode(node, MethodReferenceExpression, { super.visitMethodReferenceExpression(it) })
    }

    @Override
    void visitUnaryMinusExpression(UnaryMinusExpression node) {
        addNode(node, UnaryMinusExpression, { super.visitUnaryMinusExpression(it) })
    }

    @Override
    void visitUnaryPlusExpression(UnaryPlusExpression node) {
        addNode(node, UnaryPlusExpression, { super.visitUnaryPlusExpression(it) })
    }

    @Override
    void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
        addNode(node, BitwiseNegationExpression, { super.visitBitwiseNegationExpression(it) })
    }

    @Override
    void visitCastExpression(CastExpression node) {
        addNode(node, CastExpression, { super.visitCastExpression(it) })
    }

    @Override
    void visitConstantExpression(ConstantExpression node) {
        addNode(node, ConstantExpression, { super.visitConstantExpression(it) })
    }

    @Override
    void visitClassExpression(ClassExpression node) {
        addNode(node, ClassExpression, { super.visitClassExpression(it) })
    }

    @Override
    void visitVariableExpression(VariableExpression node) {
        addNode(node, VariableExpression, { VariableExpression it ->
            if (it.accessedVariable) {
                if (it.accessedVariable instanceof Parameter) {
                    visitParameter((Parameter)it.accessedVariable)
                } else if (it.accessedVariable instanceof DynamicVariable) {
                    addNode(it.accessedVariable, DynamicVariable,{ it.initialExpression?.visit(this)})
                }
            }
        })
    }

    @Override
    void visitDeclarationExpression(DeclarationExpression node) {
        addNode(node, DeclarationExpression, { super.visitDeclarationExpression(it) })
    }

    @Override
    void visitPropertyExpression(PropertyExpression node) {
        addNode(node, PropertyExpression, { super.visitPropertyExpression(it) })
    }

    @Override
    void visitAttributeExpression(AttributeExpression node) {
        addNode(node, AttributeExpression, { super.visitAttributeExpression(it) })
    }

    @Override
    void visitFieldExpression(FieldExpression node) {
        addNode(node, FieldExpression, { super.visitFieldExpression(it) })
    }

    @Override
    void visitGStringExpression(GStringExpression node) {
        addNode(node, GStringExpression, { super.visitGStringExpression(it) })
    }

    @Override
    void visitCatchStatement(CatchStatement node) {
        addNode(node, CatchStatement, { 
            if (it.variable) visitParameter(it.variable) 
            super.visitCatchStatement(it) 
        })
    }

    @Override
    void visitArgumentlistExpression(ArgumentListExpression node) {
        addNode(node, ArgumentListExpression, { super.visitArgumentlistExpression(it) })
    }

    @Override
    void visitClosureListExpression(ClosureListExpression node) {
        addNode(node, ClosureListExpression, { super.visitClosureListExpression(it) })
    }

    @Override
    void visitBytecodeExpression(BytecodeExpression node) {
        addNode(node, BytecodeExpression, { super.visitBytecodeExpression(it) })
    }

    @Override
    void visitListOfExpressions(List<? extends Expression> list) {
        list.each { Expression node ->
            if (node instanceof NamedArgumentListExpression ) {
                addNode(node, NamedArgumentListExpression, { it.visit(this) })
            } else {
                node.visit(this)
            }
        }
    }
}
