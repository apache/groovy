/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;

/**
 * Abstract base class for generator of Java class versions of Groovy AST classes
 *
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @author Russel Winder
 * @version $Revision$
 */
public abstract class ClassGenerator extends ClassCodeVisitorSupport implements Opcodes {
    protected ClassLoader classLoader;
    // inner classes created while generating bytecode
    protected LinkedList innerClasses = new LinkedList();

    public ClassGenerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public LinkedList getInnerClasses() {
        return innerClasses;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * A constant that is the ASM representation of the JDK version number for use in the
     * <code>ClassWriter.visitor</code> method calls.
     * <p/>
     * <p>Prior to version 1.5 of ASM, the code generated was always JDK1.3 compliant.  As of ASM version
     * 1.5 there is an extra (first) parameter to specify the bytecode version to generate.  In
     * version 1.5 these are in Constants.  The CVS (as at 2004.12.12) and presumably in version 2.0,
     * the interface Constants is replaced by Opcodes.</p>
     */
    public static final int asmJDKVersion = V1_3;
    //  We can use V1_3 and not org.objectweb.asm.Opcodes.V1_3 because this abstract class
    //  implements org.objectweb.asm.Opcodes so all its constants are available directly.


    protected SourceUnit getSourceUnit() {
        return null;
    }

    public void visitBytecodeSequence(BytecodeSequence bytecodeSequence) {
    }
}
