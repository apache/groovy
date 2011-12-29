package groovy.bugs

import org.codehaus.groovy.ast.ClassNode
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.tools.javac.JavaStubGenerator
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.antlr.EnumHelper

class Groovy5212Bug extends GroovyTestCase implements Opcodes {
    File outputDir
    
    @Override
    protected void setUp() {
        super.setUp()
        outputDir = File.createTempFile("stub","groovy")
        outputDir.mkdirs()
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        outputDir.delete()
    }

    void testGeneratedEnumJavaStubShouldNotHaveFinalModifier() {
        ClassNode cn = EnumHelper.makeEnumNode("MyEnum", ACC_PUBLIC, ClassNode.EMPTY_ARRAY, null)
        ModuleNode module = new ModuleNode(new CompileUnit(null,null))
        cn.setModule(module)
        StringWriter wrt = new StringWriter()
        PrintWriter out = new PrintWriter(wrt)
        JavaStubGenerator generator = new StringJavaStubGenerator(outputDir, out)
        generator.generateClass(cn)
        
        String stub = wrt.toString()
        assert !(stub =~ /final/)
    }

    void testShouldNotAllowExtendingEnum() {
        shouldFail {
            assertScript '''
                enum MyEnum { a }
                enum MyExtendedEnum extends MyEnum { b }
            '''
        }
    }

    void testShouldNotAllowExtendingEnumWithClass() {
        shouldFail {
            assertScript '''
                enum MyEnum { a }
                class MyExtendedEnum extends MyEnum { }
                new MyExtendedEnum()
            '''
        }
    }

    /**
     * Helper class, which generates code in a string instead of an output file.
     */
    private final static class StringJavaStubGenerator extends JavaStubGenerator {
        PrintWriter out
        StringJavaStubGenerator(File outFile, PrintWriter out) {
            super(outFile)
            this.out = out
        }
        public void generateClass(ClassNode classNode) throws FileNotFoundException {


            try {
                String packageName = classNode.getPackageName();
                if (packageName != null) {
                    out.println("package " + packageName + ";\n");
                }

                super.printImports(out, classNode);
                super.printClassContents(out, classNode);

            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
