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

package org.codehaus.groovy.control;




/**
 *  Compilation phase identifiers.  
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class Phases
{
    public static final int INITIALIZATION        = 1;   // Opening of files and such
    public static final int PARSING               = 2;   // Lexing, parsing, and AST building
    public static final int CONVERSION            = 3;   // CST to AST conversion
    public static final int SEMANTIC_ANALYSIS     = 4;   // AST semantic analysis and elucidation
    public static final int CANONICALIZATION      = 5;   // AST completion
    public static final int INSTRUCTION_SELECTION = 6;   // Class generation, phase 1
    public static final int CLASS_GENERATION      = 7;   // Class generation, phase 2
    public static final int OUTPUT                = 8;   // Output of class to disk
    public static final int FINALIZATION          = 9;   // Cleanup
    public static final int ALL                   = 9;   // Synonym for full compilation
    
    public static String[] descriptions = {
          "startup"
        , "initialization"
        , "parsing"
        , "conversion"
        , "semantic analysis"
        , "canonicalization"
        , "instruction selection"
        , "class generation"
        , "output"
        , "cleanup"
    };
    
    
    
   /**
    *  Returns a description of the specified phase.
    */
    
    public static String getDescription( int phase )
    {
        return descriptions[phase];
    }
    
}




