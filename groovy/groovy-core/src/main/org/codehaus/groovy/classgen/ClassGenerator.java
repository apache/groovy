/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.objectweb.asm.Constants;

import java.util.LinkedList;

/**
 * Abstract base class for generator of Java class versions of Groovy AST classes
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class ClassGenerator extends CodeVisitorSupport implements GroovyClassVisitor, Constants {
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
   *  A constant that is the ASM representation of the JDK version number for use in the
   *  <code>CodeWriter.visitor</code> method calls.
   *
   *  <p>Prior to version 1.5 of ASM, the code generated was always JDK1.3 compliant.  As of ASM version
   *  1.5 there is an extra (first) parameter to specify the bytecode version to generate.  In
   *  version 1.5 these are in Constants.  The CVS (as at 2004.12.12) and presumably in version 2.0,
   *  the interface Constants is replaced by Opcodes.</p>
   *
   *  @author Russel Winder
   *  @version 2004.12.14
   */
  public final static int asmJDKVersion = V1_3 ;
  //  We can use V1_3 and not org.objectweb.asm.Constants.V1_3 because this abstract class
  //  implements org.objectweb.asm.Constants so all its constants are available directly.
}
