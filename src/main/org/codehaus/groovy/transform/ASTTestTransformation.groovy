package org.codehaus.groovy.transform

import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.io.ReaderSource
import org.codehaus.groovy.tools.Utilities
import org.codehaus.groovy.control.*
import groovy.transform.CompilationUnitAware
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.runtime.MethodClosure

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ASTTestTransformation extends AbstractASTTransformation implements CompilationUnitAware {
    private CompilationUnit compilationUnit

    void visit(final org.codehaus.groovy.ast.ASTNode[] nodes, final org.codehaus.groovy.control.SourceUnit source) {
        AnnotationNode annotationNode = nodes[0]
        def member = annotationNode.getMember('phase')
        def phase = CompilePhase.SEMANTIC_ANALYSIS
        if (member) {
            if (member instanceof VariableExpression) {
                phase = CompilePhase.valueOf(member.text)
            } else if (member instanceof PropertyExpression) {
                phase = CompilePhase.valueOf(member.propertyAsString)
            }
        }
        member = annotationNode.getMember('value')
        if (member && !(member instanceof ClosureExpression)) {
            throw new GroovyBugError("ASTTest value must be a closure")
        }
        if (!member && !annotationNode.getNodeMetaData(ASTTestTransformation)) {
            throw new GroovyBugError("Missing test expression at line " + annotationNode.getLineNumber())
        }
        // convert value into node metadata so that the expression doesn't mix up with other AST xforms like type checking
        annotationNode.putNodeMetaData(ASTTestTransformation, member)
        annotationNode.getMembers().remove('value')

        def pcallback = compilationUnit.progressCallback
        def callback = new CompilationUnit.ProgressCallback() {
            @Override
            void call(final ProcessingUnit context, final int phaseRef) {
                if (phaseRef == phase.phaseNumber) {
                    ClosureExpression testClosure = nodes[0].getNodeMetaData(ASTTestTransformation)
                    StringBuilder sb = new StringBuilder()
                    for (int i = testClosure.lineNumber; i <= testClosure.lastLineNumber; i++) {
                        sb.append(source.source.getLine(i, new Janitor())).append('\n')
                    }
                    def testSource = sb.substring(testClosure.columnNumber + 1, sb.length())
                    testSource = testSource.substring(0, testSource.lastIndexOf('}'))
                    CompilerConfiguration config = new CompilerConfiguration()
                    def customizer = new ImportCustomizer()
                    config.addCompilationCustomizers(customizer)
                    def binding = new Binding()
                    binding['node'] = nodes[1]
                    binding['lookup'] = new MethodClosure(LabelFinder, "lookup").curry(nodes[1])

                    GroovyShell shell = new GroovyShell(binding, config)

                    source.AST.imports.each {
                        customizer.addImport(it.alias, it.type.name)
                    }
                    source.AST.starImports.each {
                        customizer.addStarImports(it.packageName)
                    }
                    source.AST.staticImports.each {
                        customizer.addStaticImport(it.value.alias, it.value.type.name, it.value.fieldName)
                    }
                    source.AST.staticStarImports.each {
                        customizer.addStaticStars(it.value.className)
                    }
                    shell.evaluate(testSource)
                }
            }
        }
        
        if (pcallback!=null) {
            if (pcallback instanceof ProgressCallbackChain) {
                pcallback.addCallback(callback)                
            } else {
                pcallback = new ProgressCallbackChain(pcallback, callback)
            }
            callback = pcallback
        }
        
        compilationUnit.setProgressCallback(callback)

    }

    void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit
    }

    private static class AssertionSourceDelegatingSourceUnit extends SourceUnit {
        private final ReaderSource delegate

        AssertionSourceDelegatingSourceUnit(final String name, final ReaderSource source, final CompilerConfiguration flags, final GroovyClassLoader loader, final ErrorCollector er) {
            super(name, '', flags, loader, er)
            delegate = source
        }

        @Override
        String getSample(final int line, final int column, final Janitor janitor) {
            String sample = null;
            String text = delegate.getLine(line, janitor);

            if (text != null) {
                if (column > 0) {
                    String marker = Utilities.repeatString(" ", column - 1) + "^";

                    if (column > 40) {
                        int start = column - 30 - 1;
                        int end = (column + 10 > text.length() ? text.length() : column + 10 - 1);
                        sample = "   " + text.substring(start, end) + Utilities.eol() + "   " +
                                marker.substring(start, marker.length());
                    } else {
                        sample = "   " + text + Utilities.eol() + "   " + marker;
                    }
                } else {
                    sample = text;
                }
            }

            return sample;

        }

    }
    
    private static class ProgressCallbackChain extends CompilationUnit.ProgressCallback {

        private final List<CompilationUnit.ProgressCallback> chain = new LinkedList<CompilationUnit.ProgressCallback>()

        ProgressCallbackChain(CompilationUnit.ProgressCallback... callbacks) {
            if (callbacks!=null) {
                callbacks.each { addCallback(it) }
            }
        }

        public void addCallback(CompilationUnit.ProgressCallback callback) {
            chain << callback
        }
        
        @Override
        void call(final ProcessingUnit context, final int phase) {
            chain*.call(context, phase)
        }
    }

    public static class LabelFinder extends ClassCodeVisitorSupport {


        public static List<Statement> lookup(MethodNode node, String label) {
            LabelFinder finder = new LabelFinder(label, null)
            node.code.visit(finder)

            finder.targets
        }

        public static List<Statement> lookup(ClassNode node, String label) {
            LabelFinder finder = new LabelFinder(label, null)
            node.methods*.code*.visit(finder)
            node.declaredConstructors*.code*.visit(finder)

            finder.targets
        }

        private final String label
        private final SourceUnit unit

        private List<Statement> targets = new LinkedList<Statement>();

        LabelFinder(final String label, final SourceUnit unit) {
            this.label = label
            this.unit = unit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            unit
        }

        @Override
        protected void visitStatement(final Statement statement) {
            super.visitStatement(statement)
            if (statement.statementLabel==label) targets << statement
        }

        List<Statement> getTargets() {
            return Collections.unmodifiableList(targets)
        }
    }

}
