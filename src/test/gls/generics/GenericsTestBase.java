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
package gls.generics;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyClassLoader.InnerLoader;
import groovy.util.GroovyTestCase;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

import java.util.HashMap;
import java.util.Map;

public abstract class GenericsTestBase extends GroovyTestCase {
    MyLoader loader;
    HashMap<String, String> signatures;

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
            GenericsTester classVisitor = new GenericsTester(new org.objectweb.asm.tree.ClassNode());
            cr.accept(classVisitor, ClassWriter.COMPUTE_MAXS);
            return super.createClass(code, classNode);
        }
    }

    private class GenericsTester extends ClassVisitor {
        GenericsTester(ClassVisitor cv) {
            super(CompilerConfiguration.ASM_API_VERSION, cv);
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (signature != null) signatures.put("class", signature);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (signature != null) signatures.put(name, signature);
            return super.visitField(access, name, desc, signature, value);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (signature != null) signatures.put(name + desc, signature);
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    public void setUp() {
        loader = new MyLoader(this.getClass().getClassLoader());
        signatures = new HashMap<>();
    }

    public void createClassInfo(String script) {
        loader.parseClass(script);
    }

    public Map<String, String> getSignatures() {
        return signatures;
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
