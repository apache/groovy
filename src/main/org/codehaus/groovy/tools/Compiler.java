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

package org.codehaus.groovy.tools;

import java.io.File;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;




/**
 *  A convenience front end for getting standard compilations done.
 *  All compile() routines generate classes to the filesystem.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class Compiler
{
    public static Compiler DEFAULT = new Compiler();
    
    private CompilerConfiguration configuration = null;  // Optional configuration data
    
   /**
    *  Initializes the Compiler with default configuration.
    */
    
    public Compiler()
    {
        configuration = null;
    }
    
    
   /**
    *  Initializes the Compiler with the specified configuration.
    */
    
    public Compiler( CompilerConfiguration configuration )
    {
        this.configuration = configuration;
    }

    
   
   /**
    *  Compiles a single File.
    */
   
    public void compile( File file ) throws CompilationFailedException
    {
        CompilationUnit unit = new CompilationUnit( configuration );
        unit.addSource( file );
        unit.compile();
    }
    
    
    
   /**
    *  Compiles a series of Files.
    */
    
    public void compile( File[] files ) throws CompilationFailedException
    {
        CompilationUnit unit = new CompilationUnit( configuration );
        unit.addSources( files );
        unit.compile();
    }

    
    
   /**
    *  Compiles a series of Files from file names.
    */
    
    public void compile( String[] files ) throws CompilationFailedException
    {
        CompilationUnit unit = new CompilationUnit( configuration );
        unit.addSources( files );
        unit.compile();
    }

    
    
   /**
    *  Compiles a string of code.
    */
    
    public void compile( String name, String code ) throws CompilationFailedException
    {
        CompilationUnit unit = new CompilationUnit( configuration );
        unit.addSource( new SourceUnit(name, code, configuration, unit.getClassLoader()) );
        unit.compile();
    }

}




