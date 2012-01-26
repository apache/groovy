package org.codehaus.groovy.classgen.asm.sc

import org.objectweb.asm.ClassVisitor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationUnit
import java.security.CodeSource
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.ClassNode
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.util.CheckClassAdapter

class StaticCompilationTestSupport {
    private Map<String, Object[]> astTrees
    private ClassVisitor currentClassVisitor

    void extraSetup() {
        astTrees = [:]
        currentClassVisitor = null
        def mixed = metaClass.owner
        mixed.config = new CompilerConfiguration()
        mixed.config.addCompilationCustomizers(new ASTTransformationCustomizer(CompileStatic), new ASTTreeCollector())
        mixed.configure()
        mixed.shell = new GroovyShell(mixed.config)
        // trick because GroovyShell doesn't allow to provide our own GroovyClassLoader
        // to be fixed when this will be possible
        mixed.shell.loader = new GroovyClassLoader(this.class.classLoader, mixed.config) {
            @Override
            protected CompilationUnit createCompilationUnit(final CompilerConfiguration config, final CodeSource source) {
                return new CustomCompilationUnit(config, source, this)
            }
        }
    }

    private class CustomCompilationUnit extends CompilationUnit {
        CustomCompilationUnit(final CompilerConfiguration configuration, final CodeSource security, final GroovyClassLoader loader) {
            super(configuration, security, loader)
        }

        @Override
        protected ClassVisitor createClassVisitor() {
            def visitor = super.createClassVisitor()
            StaticCompilationTestSupport.this.currentClassVisitor = visitor
            return visitor
        }
    }

    private class ASTTreeCollector extends CompilationCustomizer {

        ASTTreeCollector() {
            super(CompilePhase.CLASS_GENERATION)
        }

        @Override
        void call(final org.codehaus.groovy.control.SourceUnit source, final org.codehaus.groovy.classgen.GeneratorContext context, final ClassNode classNode) {
            StringWriter stringWriter = new StringWriter()
            try {
                ClassReader cr = new ClassReader(((ClassWriter)StaticCompilationTestSupport.this.currentClassVisitor).toByteArray())
                CheckClassAdapter.verify(cr, true, new PrintWriter(stringWriter))
            } catch (Throwable e) {
                // not a problem
            }
            StaticCompilationTestSupport.this.astTrees[classNode.name] = [classNode, stringWriter.toString()] as Object[]
        }
    }

}
