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
package gls.annotations;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyClassLoader.InnerLoader;
import groovy.test.GroovyTestCase;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

public abstract class AnnotationsTestBase extends GroovyTestCase {
    MyLoader loader;
    List<String> annotations;
    String current = "";

    private class MyLoader extends GroovyClassLoader {
        MyLoader(ClassLoader classLoader) {
            super(classLoader);
        }

        protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
            return new MyCollector(new InnerLoader(this), unit, su);
        }
    }

    private class MyCollector extends GroovyClassLoader.ClassCollector {
        MyCollector(InnerLoader myLoader, CompilationUnit unit, SourceUnit su) {
            super(myLoader, unit, su);
        }

        protected Class createClass(byte[] code, ClassNode classNode) {
            ClassReader cr = new ClassReader(code);
            AnnotationsTester classVisitor = new AnnotationsTester(new org.objectweb.asm.tree.ClassNode());
            cr.accept(classVisitor, CompilerConfiguration.ASM_PARSE_MODE);
            return super.createClass(code, classNode);
        }
    }

    private class FieldAnnotationScanner extends FieldVisitor {
        private final String field;

        FieldAnnotationScanner(String field) {
            super(CompilerConfiguration.ASM_API_VERSION);
            this.field = field;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            annotations.add("visiting field " + field + ", found annotation: desc=" + desc + ", visible=" + visible);
            return super.visitAnnotation(desc, visible);
        }
    }

    private class MethodAnnotationScanner extends MethodVisitor {
        private final String method;

        MethodAnnotationScanner(String method) {
            super(CompilerConfiguration.ASM_API_VERSION);
            this.method = method;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            annotations.add("visiting method " + method + ", found annotation: desc=" + desc + ", visible=" + visible);
            return super.visitAnnotation(desc, visible);
        }
    }

    private class AnnotationsTester extends ClassVisitor {
        AnnotationsTester(ClassVisitor cv) {
            super(CompilerConfiguration.ASM_API_VERSION, cv);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            current = name;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible){
            annotations.add("visiting class " + current + " found annotation: desc=" + desc + ", visible=" + visible);
            return super.visitAnnotation(desc,visible);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return new FieldAnnotationScanner(current + "#" + name);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodAnnotationScanner(current + "#" + name);
        }
    }

    public void setUp() {
        loader = new MyLoader(this.getClass().getClassLoader());
        annotations = new ArrayList<>();
    }

    public void createClassInfo(String script) {
        loader.parseClass(script);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    protected void shouldNotCompile(String script) {
        try {
            loader.parseClass(script);
        } catch (CompilationFailedException cfe) {
            return;
        }
        throw new AssertionError("compilation of script '" + script + "' should have failed, but did not.");
    }
}
