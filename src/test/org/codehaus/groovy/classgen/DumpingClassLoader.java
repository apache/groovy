/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.classgen;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A class loader used for debugging the bytecode generation.
 * This will log all bytecode being loaded
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
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

    protected ASMifierClassVisitor dumpVisitor = new ASMifierClassVisitor(new PrintWriter(new OutputStreamWriter(System.out)));
    protected ASMifierClassVisitor invisibleDumpVisitor = new ASMifierClassVisitor(new PrintWriter(new StringWriter()));
    protected CompileUnit unit = new CompileUnit(this, new CompilerConfiguration());
    protected ClassGenerator checker =
            new AsmClassGenerator(new GeneratorContext(unit), new CheckClassAdapter(invisibleDumpVisitor), this, null);
    protected ClassGenerator dumper = new AsmClassGenerator(new GeneratorContext(unit), dumpVisitor, this, null);

}
