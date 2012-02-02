package groovy.bugs

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.tools.javac.JavaStubGenerator
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.ConstantExpression

class Groovy5260Bug extends GroovyTestCase implements Opcodes {
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

    private void checkConstant(ConstantExpression constant, String expectation) {
        ClassNode cn = new ClassNode("Foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        ModuleNode module = new ModuleNode(new CompileUnit(null,null))
        cn.setModule(module)
        cn.addField(new FieldNode("constant", ACC_PUBLIC+ACC_STATIC+ACC_FINAL, constant.type, cn, constant))
        StringWriter wrt = new StringWriter()
        PrintWriter out = new PrintWriter(wrt)
        JavaStubGenerator generator = new StringJavaStubGenerator(outputDir, out)
        generator.generateClass(cn)

        String stub = wrt.toString()
        assert (stub =~ expectation)
    }

    void testLongConstant() {
        def constant = new ConstantExpression(666, true)
        constant.type = ClassHelper.long_TYPE
        checkConstant(constant, /\(long\) 666L/)
    }

    void testIntConstant() {
        def constant = new ConstantExpression(666, true)
        constant.type = ClassHelper.int_TYPE
        checkConstant(constant, /\(int\) 666/)
    }

    void testByteConstant() {
        def constant = new ConstantExpression(123, true)
        constant.type = ClassHelper.byte_TYPE
        checkConstant(constant, /\(byte\) 123/)
    }

    void testBooleanConstant() {
        def constant = new ConstantExpression(false, true)
        constant.type = ClassHelper.boolean_TYPE
        // boolean type is not optimized yet
        checkConstant(constant, /new java.lang.Boolean\(\(boolean\)false\)/)
    }

    void testStringConstant() {
        def constant = new ConstantExpression('foo', true)
        constant.type = ClassHelper.STRING_TYPE
        checkConstant(constant, /"foo"/)
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
