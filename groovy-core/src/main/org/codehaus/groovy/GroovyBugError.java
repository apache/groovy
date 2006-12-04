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
package org.codehaus.groovy;

/**
 * This class represents an error that is thrown when a bug is 
 * recognized inside the runtime. Basically it is thrown when
 * a constraint is not fullfilled that should be fullfiled. 
 * 
 * @author Jochen Theodorou
 */
public class GroovyBugError extends AssertionError {
    
    // message string
    private String    message;
    // optional exception
    private Exception exception;

    /**
     * constructs a bug error using the given text
     * @param message the error message text
     */
    public GroovyBugError( String message ) {
        this.message = message;
    }
    
    /**
     * Constructs a bug error using the given exception
     * @param exception cause of this error
     */
    public GroovyBugError( Exception exception ) {
        this.exception = exception;
    }
    
    /**
     * Constructs a bug error using the given exception and
     * a text with additional information about the cause 
     * @param msg additional information about this error
     * @param exception cause of this error
     */
    public GroovyBugError( String msg, Exception exception )
    {
        this.exception = exception;
        this.message = msg;
    }

    /**
     * Returns a String representation of this class by calling <code>getMessage()</code>.  
     * @see #getMessage()
     */
    public String toString() {
        return getMessage();
    }
    
    /**
     * Returns the detail message string of this error. The message 
     * will consist of the bug text prefixed by "BUG! " if there this
     * isntance was created using a message. If this error was 
     * constructed without using a bug text the message of the cause 
     * is used prefixed by "BUG! UNCAUGHT EXCEPTION: "
     *  
     * @return the detail message string of this error.
     */
    public String getMessage() {
        if( message != null )
        {
            return "BUG! "+message;
        }
        else
        {
            return "BUG! UNCAUGHT EXCEPTION: " + exception.getMessage();
        }
    }
    
    public Throwable getCause() {
        return this.exception;
    }    
    
    /**
     * Returns the bug text to describe this error
     */
    public String getBugText(){
        if( message != null ){
            return message;
        } else {
            return exception.getMessage();
        }
    }
    
    /**
     * Sets the bug text to describe this error
     */
    public void setBugText(String msg) {
        this.message = msg;
    }
}
