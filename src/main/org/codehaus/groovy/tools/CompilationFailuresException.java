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
import org.codehaus.groovy.tools.ExceptionCollector;
import java.util.LinkedHashMap;
import java.util.Iterator;



/**
 *  Holds a map of Groovy compilation failures, keyed on source 
 *  descriptor.  Each failure is an <code>ExceptionCollector</code>
 *  of exceptions produced during compilation of a particular source.  
 *  Unlike <code>ExceptionCollector</code>, it never throws itself.
 *  <p>
 *  Created and filled by org.codehaus.groovy.tools.Compiler.
 *
 *  @author <a href="mailto:cpoirier%20AT%20tapestry_os%20DOT%20org">Chris Poirier</a>
 *  @version $Revision$
 */

public class CompilationFailuresException extends GroovyException
{
    private LinkedHashMap failures = new LinkedHashMap();  // The ExceptionCollectors


 //----------------------------------------------------------------------------
 // CONSTRUCTORS

   /**
    *  Creates an empty exception.
    */

    public CompilationFailuresException()
    {
        super( true );   // On creation, fatal is true
    }



 //----------------------------------------------------------------------------
 // OPERATIONS   


   /**
    *  Adds a <code>ExceptionCollector</code> to the set.  Old values
    *  will be merged.
    */

    public void add( String source, ExceptionCollector exceptions ) 
    {
        if( failures.containsKey(source) )
        {
            try { ((ExceptionCollector)failures.get(source)).merge( exceptions, false ); } catch( Exception e ) {} 
        }
        else
        {
            failures.put( source, exceptions );
        }
    }


   /**
    *  Merges in data from another set.  Old values will be merged.
    */

    public void merge( CompilationFailuresException other )
    {
        Iterator sources = other.iterator();
        while( sources.hasNext() )
        {
            String source = (String)sources.next();
            add( source, other.get(source) );
        }
    }



 //----------------------------------------------------------------------------
 // TESTS       


   /**
    *  Returns true if the collector is empty.
    */

    public boolean isEmpty()
    {
        return failures.isEmpty();
    }



 //----------------------------------------------------------------------------
 // EXCEPTION ACCESS

   
   /**
    *  Returns the number of sources in the set.
    */

    public int size()
    {
        return failures.size();
    }


   /**
    *  Returns the number of exceptions in the set.
    */

    public int total()
    {
        int total = 0;

        Iterator sources = iterator();
        while( sources.hasNext() )
        {
            String source = (String)sources.next();
            ExceptionCollector collector = get( source );
            total += collector.size();
        }

        return total;
    }

 
   /**
    *  Returns an <code>Iterator</code> to the keys in the set.
    */

    public Iterator iterator()
    {
        return failures.keySet().iterator();
    }


   /**
    *  Returns the <code>ExceptionCollector</code> for the specified source,
    *  or null.
    */

    public ExceptionCollector get( String source )
    {
       return (ExceptionCollector)failures.get( source );
    }


   /**
    *  Returns a string summary of the exception set.
    */

   public String getMessage()
   {
       if( failures.size() == 1 )
       {
           return "failures in 1 source";
       }
       else
       {
           return "failures in " + failures.size() + " sources";
       }
      
   }


   /**
    *  Returns a string representation of the exception set.
    */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        String       endl   = System.getProperty( "line.separator", "\n" );


        //
        // Output a summary message...

        if( failures.size() == 1 )
        {
           buffer.append("1 failure:");
        }
        else
        {
           buffer.append(failures.size()).append(" failures:");
        }
        buffer.append( endl );


        //
        // Then each individual message, one to a line

        Iterator sources = iterator();
        while( sources.hasNext() )
        {
            String source  = (String)sources.next();
            String message = get(source).getMessage();

            buffer.append("   ").append(source).append(": ").append(message).append(endl);
        }

        return buffer.toString();
    }
}

