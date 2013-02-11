/*
 * Copyright 2003-2010 the original author or authors.
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

package groovy.inspect.swingui

import groovy.text.GStringTemplateEngine
import groovy.text.Template
import org.codehaus.groovy.classgen.asm.BytecodeHelper

import java.util.concurrent.atomic.AtomicBoolean
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilationFailedException

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
 *
 * @author Hamlet D'Arcy 
 * @author Roshan Dawrani
 */
class ScriptToTreeNodeAdapter {

    static Properties classNameToStringForm
    boolean showScriptFreeForm, showScriptClass
    final GroovyClassLoader classLoader
    final AstBrowserNodeMaker nodeMaker

    static {
        try {
            URL url =  ClassLoader.getSystemResource("groovy/inspect/swingui/AstBrowserProperties.groovy")
            if (!url) {
                url = ScriptToTreeNodeAdapter.class.classLoader.getResource("groovy/inspect/swingui/AstBrowserProperties.groovy")
            }
    
            def config = new ConfigSlurper().parse(url)
            classNameToStringForm = config.toProperties()
    
            String home = System.getProperty("user.home")
            if (home) {
                File userFile = new File(home + File.separator + ".groovy/AstBrowserProperties.groovy")
                if (userFile.exists()) {
                    def customConfig = new ConfigSlurper().parse(userFile.toURL())
                    // layer custom string forms onto defaults with putAll, do not replace them
                    classNameToStringForm.putAll(customConfig.toProperties())
                }
            }
        }catch(ex) {
            // on restricted environments like, such calls may fail, but that should not prevent the class
            // from being loaded. Tree nodes can still get rendered with their simple names.
            classNameToStringForm = new Properties()  
        }
    }
    
    def ScriptToTreeNodeAdapter(classLoader, showScriptFreeForm, showScriptClass, nodeMaker) {
        this.classLoader = classLoader ?: new GroovyClassLoader(getClass().classLoader)
        this.showScriptFreeForm = showScriptFreeForm
        this.showScriptClass = showScriptClass
        this.nodeMaker = nodeMaker
    }

    /**
    * Performs the conversion from script to TreeNode.
     *
     * @param script
     *      a Groovy script in String form
     * @param compilePhase
     *      the int based CompilePhase to compile it to.
    */
    def compile(String script, int compilePhase) {
        def scriptName = "script" + System.currentTimeMillis() + ".groovy"
        GroovyCodeSource codeSource = new GroovyCodeSource(script, scriptName, "/groovy/script")
        CompilationUnit cu = new CompilationUnit(CompilerConfiguration.DEFAULT, codeSource.codeSource, classLoader)
        cu.setClassgenCallback(classLoader.createCollector(cu, null))
        TreeNodeBuildingNodeOperation operation = new TreeNodeBuildingNodeOperation(this, showScriptFreeForm, showScriptClass)
        cu.addPhaseOperation(operation, compilePhase)
        cu.addSource(codeSource.getName(), script);
        try {
            cu.compile(compilePhase)
        } catch (CompilationFailedException cfe) {
            operation.root.add(nodeMaker.makeNode("Unable to produce AST for this phase due to earlier compilation error:"))
            cfe.message.eachLine {
                operation.root.add(nodeMaker.makeNode(it))
            }
            operation.root.add(nodeMaker.makeNode("Fix the above error(s) and then press Refresh"))
        } catch (Throwable t) {
            operation.root.add(nodeMaker.makeNode("Unable to produce AST for this phase due to an error:"))
            operation.root.add(nodeMaker.makeNode(t))
            operation.root.add(nodeMaker.makeNode("Fix the above error(s) and then press Refresh"))
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
            sort() { it[0] }
    }

    /**
     * Handles the property file templating for node types.
     */
    private String getStringForm(node) {
        def templateTextForNode = classNameToStringForm[node.class.name] 
        if (templateTextForNode) {
            GStringTemplateEngine engine = new GStringTemplateEngine()
            Template template = engine.createTemplate(templateTextForNode)
            Writable writable = template.make([expression: node])
            StringWriter result = new StringWriter()
            writable.writeTo(result)
            result.toString()
        } else {
            node.class.simpleName
        }
    }
}

/**
 * This Node Operation builds up a root tree node for the viewer.
 * @author Hamlet D'Arcy
 */
class TreeNodeBuildingNodeOperation extends PrimaryClassNodeOperation {

    final def root
    final def sourceCollected = new AtomicBoolean(false)
    final ScriptToTreeNodeAdapter adapter
    final def showScriptFreeForm
    final def showScriptClass
    final def nodeMaker

    def TreeNodeBuildingNodeOperation(ScriptToTreeNodeAdapter adapter, showScriptFreeForm, showScriptClass) {
        if (!adapter) throw new IllegalArgumentException("Null: adapter")
        this.adapter = adapter
        this.showScriptFreeForm = showScriptFreeForm
        this.showScriptClass = showScriptClass
        nodeMaker = adapter.nodeMaker
        root = nodeMaker.makeNode("root")
    }

    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        // module node
        if (!sourceCollected.getAndSet(true) && showScriptFreeForm) {
            // display the source unit AST
            ModuleNode ast = source.getAST()
            TreeNodeBuildingVisitor visitor = new TreeNodeBuildingVisitor(adapter)
            ast.getStatementBlock().visit(visitor)
            if (visitor.currentNode) root.add(visitor.currentNode)
            collectModuleNodeMethodData("Methods", ast.getMethods())
        }

        if(classNode.isScript() && !showScriptClass) return
        
        def child = adapter.make(classNode)
        root.add(child)

        collectConstructorData(child, "Constructors", classNode)
        collectMethodData(child, "Methods", classNode)
        collectFieldData(child, "Fields", classNode)
        collectPropertyData(child, "Properties", classNode)
        collectAnnotationData(child, "Annotations", classNode)
    }

    private List collectAnnotationData(parent, String name, ClassNode classNode) {
        def allAnnotations = nodeMaker.makeNode(name)
        if (classNode.annotations) parent.add(allAnnotations)
        classNode.annotations?.each {AnnotationNode annotationNode ->
            def ggrandchild = adapter.make(annotationNode)
            allAnnotations.add(ggrandchild)
        }
    }

    private def collectPropertyData(parent, String name, ClassNode classNode) {
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

    private def collectFieldData(parent, String name, ClassNode classNode) {
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

    private def collectMethodData(parent, String name, ClassNode classNode) {
        def allMethods = nodeMaker.makeNode(name)
        if (classNode.methods) parent.add(allMethods)

        doCollectMethodData(allMethods, classNode.methods)
    }

    private def collectModuleNodeMethodData(String name, List methods) {
        if(!methods) return
        def allMethods = nodeMaker.makeNode(name)
        root.add(allMethods)

        doCollectMethodData(allMethods, methods)
    }
    
    private def doCollectMethodData(allMethods, List methods) {
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

    private def collectConstructorData(parent, String name, ClassNode classNode) {
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
}

/**
* This AST visitor builds up a TreeNode.
 *
 * @author Hamlet D'Arcy
*/
@groovy.transform.PackageScope class TreeNodeBuildingVisitor extends CodeVisitorSupport {

    def currentNode
    private adapter

    /**
     * Creates the visitor. A file named AstBrowserProperties.groovy is located which is
     * a property files the describes how to represent ASTNode types as Strings.
     */
    private TreeNodeBuildingVisitor(adapter) {
        if (!adapter) throw new IllegalArgumentException("Null: adapter")
        this.adapter = adapter;
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
                def temp = currentNode;
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

    public void visitBlockStatement(BlockStatement node) {
        addNode(node, BlockStatement, { super.visitBlockStatement(it) });
    }

    public void visitForLoop(ForStatement node) {
        addNode(node, ForStatement, { super.visitForLoop(it) });
    }

    public void visitWhileLoop(WhileStatement node) {
        addNode(node, WhileStatement, { super.visitWhileLoop(it) });
    }

    public void visitDoWhileLoop(DoWhileStatement node) {
        addNode(node, DoWhileStatement, { super.visitDoWhileLoop(it) });
    }

    public void visitIfElse(IfStatement node) {
        addNode(node, IfStatement, { super.visitIfElse(it) });
    }

    public void visitExpressionStatement(ExpressionStatement node) {
        addNode(node, ExpressionStatement, { super.visitExpressionStatement(it) });
    }

    public void visitReturnStatement(ReturnStatement node) {
        addNode(node, ReturnStatement, { super.visitReturnStatement(it) });
    }

    public void visitAssertStatement(AssertStatement node) {
        addNode(node, AssertStatement, { super.visitAssertStatement(it) });
    }

    public void visitTryCatchFinally(TryCatchStatement node) {
        addNode(node, TryCatchStatement, { super.visitTryCatchFinally(it) });
    }
    
    protected void visitEmptyStatement(EmptyStatement node) {
        addNode(node, EmptyStatement, { super.visitEmptyStatement(it) });
    }

    public void visitSwitch(SwitchStatement node) {
        addNode(node, SwitchStatement, { super.visitSwitch(it) });
    }

    public void visitCaseStatement(CaseStatement node) {
        addNode(node, CaseStatement, { super.visitCaseStatement(it) });
    }

    public void visitBreakStatement(BreakStatement node) {
        addNode(node, BreakStatement, { super.visitBreakStatement(it) });
    }

    public void visitContinueStatement(ContinueStatement node) {
        addNode(node, ContinueStatement, { super.visitContinueStatement(it) });
    }

    public void visitSynchronizedStatement(SynchronizedStatement node) {
        addNode(node, SynchronizedStatement, { super.visitSynchronizedStatement(it) });
    }

    public void visitThrowStatement(ThrowStatement node) {
        addNode(node, ThrowStatement, { super.visitThrowStatement(it) });
    }

    public void visitMethodCallExpression(MethodCallExpression node) {
        addNode(node, MethodCallExpression, { super.visitMethodCallExpression(it) });
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression node) {
        addNode(node, StaticMethodCallExpression, { super.visitStaticMethodCallExpression(it) });
    }

    public void visitConstructorCallExpression(ConstructorCallExpression node) {
        addNode(node, ConstructorCallExpression, { super.visitConstructorCallExpression(it) });
    }

    public void visitBinaryExpression(BinaryExpression node) {
        addNode(node, BinaryExpression, { super.visitBinaryExpression(it) });
    }

    public void visitTernaryExpression(TernaryExpression node) {
        addNode(node, TernaryExpression, { super.visitTernaryExpression(it) });
    }

    public void visitShortTernaryExpression(ElvisOperatorExpression node) {
        addNode(node, ElvisOperatorExpression, { super.visitShortTernaryExpression(it) });
    }

    public void visitPostfixExpression(PostfixExpression node) {
        addNode(node, PostfixExpression, { super.visitPostfixExpression(it) });
    }

    public void visitPrefixExpression(PrefixExpression node) {
        addNode(node, PrefixExpression, { super.visitPrefixExpression(it) });
    }

    public void visitBooleanExpression(BooleanExpression node) {
        addNode(node, BooleanExpression, { super.visitBooleanExpression(it) });
    }

    public void visitNotExpression(NotExpression node) {
        addNode(node, NotExpression, { super.visitNotExpression(it) });
    }

    public void visitClosureExpression(ClosureExpression node) {
        addNode(node, ClosureExpression, { 
          it.parameters?.each { parameter -> visitParameter(parameter) }
          super.visitClosureExpression(it) 
        });
    }

    /**
     * Makes walking parameters look like others in the visitor.
     */
    public void visitParameter(Parameter node) {
        addNode(node, Parameter, {
          if (node.initialExpression) {
            node.initialExpression?.visit(this)
          }
        });
    }

    public void visitTupleExpression(TupleExpression node) {
        addNode(node, TupleExpression, { super.visitTupleExpression(it) });
    }

    public void visitListExpression(ListExpression node) {
        addNode(node, ListExpression, { super.visitListExpression(it) });
    }

    public void visitArrayExpression(ArrayExpression node) {
        addNode(node, ArrayExpression, { super.visitArrayExpression(it) });
    }

    public void visitMapExpression(MapExpression node) {
        addNode(node, MapExpression, { super.visitMapExpression(it) });
    }

    public void visitMapEntryExpression(MapEntryExpression node) {
        addNode(node, MapEntryExpression, { super.visitMapEntryExpression(it) });
    }

    public void visitRangeExpression(RangeExpression node) {
        addNode(node, RangeExpression, { super.visitRangeExpression(it) });
    }

    public void visitSpreadExpression(SpreadExpression node) {
        addNode(node, SpreadExpression, { super.visitSpreadExpression(it) });
    }

    public void visitSpreadMapExpression(SpreadMapExpression node) {
        addNode(node, SpreadMapExpression, { super.visitSpreadMapExpression(it) });
    }

    public void visitMethodPointerExpression(MethodPointerExpression node) {
        addNode(node, MethodPointerExpression, { super.visitMethodPointerExpression(it) });
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression node) {
        addNode(node, UnaryMinusExpression, { super.visitUnaryMinusExpression(it) });
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression node) {
        addNode(node, UnaryPlusExpression, { super.visitUnaryPlusExpression(it) });
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression node) {
        addNode(node, BitwiseNegationExpression, { super.visitBitwiseNegationExpression(it) });
    }

    public void visitCastExpression(CastExpression node) {
        addNode(node, CastExpression, { super.visitCastExpression(it) });
    }

    public void visitConstantExpression(ConstantExpression node) {
        addNode(node, ConstantExpression, { super.visitConstantExpression(it) });
    }

    public void visitClassExpression(ClassExpression node) {
        addNode(node, ClassExpression, { super.visitClassExpression(it) });
    }

    public void visitVariableExpression(VariableExpression node) {
        addNode(node, VariableExpression, { VariableExpression it ->
            if (it.accessedVariable) {
                if (it.accessedVariable instanceof Parameter) {
                    visitParameter((Parameter)it.accessedVariable)
                } else if (it.accessedVariable instanceof DynamicVariable) {
                    addNode(it.accessedVariable, DynamicVariable,{ it.initialExpression?.visit(this)});
                }
            }
        });
    }

    public void visitDeclarationExpression(DeclarationExpression node) {
        addNode(node, DeclarationExpression, { super.visitDeclarationExpression(it) });
    }

    public void visitPropertyExpression(PropertyExpression node) {
        addNode(node, PropertyExpression, { super.visitPropertyExpression(it) });
    }

    public void visitAttributeExpression(AttributeExpression node) {
        addNode(node, AttributeExpression, { super.visitAttributeExpression(it) });
    }

    public void visitFieldExpression(FieldExpression node) {
        addNode(node, FieldExpression, { super.visitFieldExpression(it) });
    }

    public void visitGStringExpression(GStringExpression node) {
        addNode(node, GStringExpression, { super.visitGStringExpression(it) });
    }

    public void visitCatchStatement(CatchStatement node) {
        addNode(node, CatchStatement, { 
            if (it.variable) visitParameter(it.variable) 
            super.visitCatchStatement(it) 
        });
    }

    public void visitArgumentlistExpression(ArgumentListExpression node) {
        addNode(node, ArgumentListExpression, { super.visitArgumentlistExpression(it) });
    }

    public void visitClosureListExpression(ClosureListExpression node) {
        addNode(node, ClosureListExpression, { super.visitClosureListExpression(it) });
    }

    public void visitBytecodeExpression(BytecodeExpression node) {
        addNode(node, BytecodeExpression, { super.visitBytecodeExpression(it) });
    }

    protected void visitListOfExpressions(List<? extends Expression> list) {
        list.each { Expression node ->
            if (node instanceof NamedArgumentListExpression ) {
                addNode(node, NamedArgumentListExpression, { it.visit(this) });
            } else {
                node.visit(this)
            }
        }
    }
}
