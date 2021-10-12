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
package groovy.bugs

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.tools.javac.JavaStubGenerator

import static org.codehaus.groovy.ast.tools.GeneralUtils.constX

final class Groovy5260Bug extends GroovyTestCase implements org.objectweb.asm.Opcodes {

    private File outputDir

    @Override
    protected void setUp() {
        super.setUp()
        outputDir = File.createTempFile('stub', 'groovy')
        outputDir.mkdirs()
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        outputDir.delete()
    }

    void testIntConstant() {
        checkConstant(ClassHelper.int_TYPE, 666, /final int constant = 666;/)
    }

    void testLongConstant() {
        checkConstant(ClassHelper.long_TYPE, 666, 'final long constant = 666L;')
    }

    void testFloatConstant() {
        checkConstant(ClassHelper.float_TYPE, 3.14f, 'final float constant = 3.14f;')
    }

    void testByteConstant() {
        checkConstant(ClassHelper.byte_TYPE, 123, /final byte constant = \(byte\)123;/)
    }

    void testBooleanConstant() {
        checkConstant(ClassHelper.boolean_TYPE, true, 'final boolean constant = true;')
    }

    void testCharConstant() {
        checkConstant(ClassHelper.char_TYPE, 'c', 'final char constant = \'c\';')
    }

    void testStringConstant() {
        checkConstant(ClassHelper.STRING_TYPE, 'foo', 'final java.lang.String constant = "foo";')
    }

    //--------------------------------------------------------------------------

    private void checkConstant(ClassNode type, Object value, String expectation) {
        def constant = constX(value, true)
        constant.type = type

        ClassNode cn = new ClassNode('script', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        cn.addField('constant', ACC_PUBLIC | ACC_STATIC | ACC_FINAL, constant.type, constant)
        ModuleNode module = new ModuleNode(new CompileUnit(null, null))
        cn.setModule(module)

        StringWriter wrt = new StringWriter()
        PrintWriter out = new PrintWriter(wrt)
        JavaStubGenerator generator = new StringJavaStubGenerator(outputDir, out)
        generator.generateClass(cn)

        String stub = wrt.toString()
        assert (stub =~ expectation)
    }

    /**
     * Helper class, which generates code in a string instead of an output file.
     */
    private static final class StringJavaStubGenerator extends JavaStubGenerator {

        PrintWriter out

        StringJavaStubGenerator(File outFile, PrintWriter out) {
            super(outFile)
            this.out = out
        }

        public void generateClass(ClassNode classNode) throws FileNotFoundException {
            try (out) {
                String packageName = classNode.getPackageName();
                if (packageName != null) {
                    out.println("package " + packageName + ";\n");
                }
                super.printImports(out, classNode);
                super.printClassContents(out, classNode);
            }
        }
    }
}
