/*
 $Id$

 Copyright 2004 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

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

import org.codehaus.groovy.GroovyException;
import java.util.LinkedList;
import java.util.Iterator;



/**
 *  Provides a collector for Groovy compilation exceptions that
 *  throws itself when the volume of compilation errors pass a 
 *  specified threshold.  The threshold is met when either a 
 *  single fatal exception or some configured maximum of non-fatal
 *  exceptions make it into the set.
 *  <p>
 *  Created by org.codehaus.groovy.tools.Compiler, and filled by 
 *  the lexer, parser, and code generation subsystems.
 *
 *  @author <a href="mailto:cpoirier%20AT%20tapestry_os%20DOT%20org">Chris Poirier</a>
 *  @version $Revision$
 */

public class ExceptionCollector extends GroovyException
{
    private LinkedList collection = new LinkedList();  // The exceptions
    private int        limit      = 0;                 // The maximum number of exceptions in the set


  //---------------------------------------------------------------------------
  // CONSTRUCTORS

   /**
    *  Initializes the exception set so that all exceptions are considered
    *  fatal.
    */

    public ExceptionCollector()
    {
        super( false );   // On creation, fatal is false
        this.limit = 1;
    }


   /**
    *  Initializes the exception set so that some number of non-fatal
    *  exceptions triggers fatality.
    */

    public ExceptionCollector( int limit ) 
    {
        super( false );
        this.limit = limit;
    }
    

   /**
    *  Initializes the exception set so that some number of non-fatal
    *  exceptions triggers fatality.
    */

    public ExceptionCollector( String message, int limit ) 
    {
        super( message, false );
        this.limit = limit;
    }



  //---------------------------------------------------------------------------
  // OPERATIONS   


   /**
    *  Adds a <code>GroovyException</code> to the collector.  Throws the 
    *  collector if <code>hasCause()</code>.
    */

    public void add( GroovyException exception ) throws ExceptionCollector
    {
        add( exception, true );
    }


   /**
    *  Adds a <code>GroovyException</code> to the collector.  Throws the 
    *  collector if <code>hasCause()</code> and <code>withThrow</code>.
    */

    public void add( GroovyException exception, boolean withThrow ) throws ExceptionCollector
    {
        collection.add( exception );

        if( exception.isFatal() )
        { 
            setFatal( true );
        }

        if( withThrow )
        {
           throwIfCause( );
        }
    }


   /**
    *  Merges in data from another collector.  Throws this collector 
    *  if <code>hasCause()</code>.
    */

    public void merge( ExceptionCollector other ) throws ExceptionCollector
    {
       merge( other, true );
    }


   /**
    *  Merges in data from another collector.  Throws this collector 
    *  if <code>hasCause()</code> and <code>withThrow</code>.
    */

    public void merge( ExceptionCollector other, boolean withThrow ) throws ExceptionCollector
    {
        if( other != this )
        {
           collection.addAll( other.collection );
           if( other.isFatal() )
           {
              setFatal( true );
           }
        }

        if( withThrow )
        {
           throwIfCause();
        }
    }



  //---------------------------------------------------------------------------
  // THROWING     


   /**
    *  Throws this collector if there is sufficient cause.
    */

    public void throwIfCause() throws ExceptionCollector
    {
        if( hasCause() )
        {
            throw this;
        }
    }


   /**
    *  Throws this collector if it isn't empty.
    */

    public void throwUnlessEmpty() throws ExceptionCollector
    {
        if( !isEmpty() )
        {
            throw this;
        }
    }


   

  //---------------------------------------------------------------------------
  // TESTS       


   /**
    *  Returns true if the collector is empty.
    */

    public boolean isEmpty()
    {
        return collection.isEmpty();
    }


   /**
    *  Returns true if the set has reached its limit.
    */

    public boolean isFull()
    {
        return collection.size() >= limit;
    }


   /**
    *  Returns true if <code>throwIfCause()</code> should throw.
    */

    public boolean hasCause()
    {
        return isFatal() || isFull();
    }
  


  //---------------------------------------------------------------------------
  // EXCEPTION ACCESS

 
   /**
    *  Returns the number of exceptions in the set.
    */

    public int size()
    {
        return collection.size();
    }


   /**
    *  Returns the Exception at the specified offset.
    */

    public GroovyException get( int child )
    {
        return (GroovyException)collection.get( child );
    }


   /**
    *  Throws the first exception.  For convenience in testing.
    */

    public void throwFirstChild() throws GroovyException
    {
        throw get(0);
    }


   /**
    *  Returns an <code>Iterator</code> to the exceptions in the set.
    */

    public Iterator iterator()
    {
        return collection.iterator();
    }


   /**
    *  Returns a string summary of the exception set.
    */

    public String getMessage()
    {
        if( collection.size() == 1 )
        {
            return "1 exception";
        }
        else
        {
            return "" + collection.size() + " exceptions";
        }
    }


   /**
    *  Returns a string representation of the exception set.
    */

    public String toString()
    {
        return getMessage();
    }


}

