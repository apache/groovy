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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyClassLoader;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * A class loader used for debugging the bytecode generation.
 * This will log all bytecode being loaded
 */
public class DumpingClassLoader extends GroovyClassLoader implements Opcodes {

    protected static boolean CHECK_CLASS = false;
    protected static boolean DUMP_CLASS = true;

    public DumpingClassLoader(ClassLoader parentLoader) {
        super(parentLoader);
    }


    protected class DebugCollector extends ClassCollector {

        DebugCollector(GroovyClassLoader cl, CompilationUnit unit, SourceUnit su) {
            super(new GroovyClassLoader.InnerLoader(cl), unit, su);
        }

        public void call(ClassVisitor classWriter, ClassNode classNode) {
            // lets test out the class verifier
            if (DUMP_CLASS) {
                dumper.visitClass(classNode);
            }

            if (CHECK_CLASS) {
                checker.visitClass(classNode);
            }

            super.call(classWriter, classNode);
        }
    }

    protected ClassCollector createCollector(CompilationUnit unit) {
        return new DebugCollector(this, unit, null);
    }

    protected TraceClassVisitor dumpVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(new OutputStreamWriter(System.out)));
    protected TraceClassVisitor invisibleDumpVisitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(new StringBuilderWriter()));
    protected CompileUnit unit = new CompileUnit(this, new CompilerConfiguration());
    protected ClassGenerator checker =
            new AsmClassGenerator(null,new GeneratorContext(unit), new CheckClassAdapter(invisibleDumpVisitor), null);
    protected ClassGenerator dumper = new AsmClassGenerator(null,new GeneratorContext(unit), dumpVisitor, null);

}
