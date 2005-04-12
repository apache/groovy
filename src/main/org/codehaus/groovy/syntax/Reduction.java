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

package org.codehaus.groovy.syntax;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.syntax.Token;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


/** 
 *  A syntax reduction, produced by the <code>Parser</code>.
 *
 *  @see Parser
 *  @see Token
 *  @see CSTNode
 *  @see Types
 *
 *  @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class Reduction extends CSTNode
{
    public static final Reduction EMPTY = new Reduction();


  //---------------------------------------------------------------------------
  // INITIALIZATION AND SUCH

    private List    elements  = null;    // The set of child nodes   
    private boolean marked    = false;   // Used for completion marking by some parts of the parser


   /**
    *  Initializes the <code>Reduction</code> with the specified root.
    */

    public Reduction( Token root ) 
    {
        elements = new ArrayList();
        set( 0, root );
    }


   /**
    *  Initializes the <code>Reduction</code> to empty.
    */

    private Reduction() 
    {
        elements = Collections.EMPTY_LIST;
    }


   /**
    *  Creates a new <code>Reduction</code> with <code>Token.NULL</code>
    *  as it's root.
    */

    public static Reduction newContainer() 
    {
        return new Reduction( Token.NULL );
    }




  //---------------------------------------------------------------------------
  // MEMBER ACCESS


   /**
    *  Returns true if the node is completely empty (no root, even).
    */

    public boolean isEmpty() 
    {
        return size() == 0;
    }



   /**
    *  Returns the number of elements in the node.
    */

    public int size() 
    {
        return elements.size();
    }



   /**
    *  Returns the specified element, or null.
    */

    public CSTNode get( int index ) 
    {
        CSTNode element = null;

        if( index < size() ) 
        {
            element = (CSTNode)elements.get( index );
        }

        return element;
    }



   /**
    *  Returns the root of the node, the Token that indicates it's
    *  type.  Returns null if there is no root (usually only if the
    *  node is a placeholder of some kind -- see isEmpty()).
    */

    public Token getRoot() 
    {
        if( size() > 0 )
        {
            return (Token)elements.get(0);
        }
        else
        {
            return null;
        }
    }



   /**
    *  Marks the node a complete expression.
    */

    public void markAsExpression() 
    {
        marked = true;
    }



   /**
    *  Returns true if the node is a complete expression.
    */

    public boolean isAnExpression() 
    {
        if( isA(Types.COMPLEX_EXPRESSION) ) 
        {
            return true;
        }

        return marked;
    }




  //---------------------------------------------------------------------------
  // OPERATIONS


   /**
    *  Adds an element to the node.
    */

    public CSTNode add( CSTNode element ) 
    {
        return set( size(), element );
    }



   /**
    *  Sets an element in at the specified index.
    */

    public CSTNode set( int index, CSTNode element ) 
    {
        
        if( elements == null ) 
        {
            throw new GroovyBugError( "attempt to set() on a EMPTY Reduction" );
        }

        if( index == 0 && !(element instanceof Token) ) 
        {

            //
            // It's not the greatest of design that the interface allows this, but it
            // is a tradeoff with convenience, and the convenience is more important.

            throw new GroovyBugError( "attempt to set() a non-Token as root of a Reduction" );
        }


        //
        // Fill slots with nulls, if necessary.

        int count = elements.size();
        if( index >= count ) 
        {
            for( int i = count; i <= index; i++ ) 
            {
                elements.add( null );
            }
        }

        //
        // Then set in the element.

        elements.set( index, element );

        return element;
    }



   /**
    *  Removes a node from the <code>Reduction</code>.  You cannot remove 
    *  the root node (index 0).
    */

    public CSTNode remove( int index )
    {
        if( index < 1 ) 
        {
            throw new GroovyBugError( "attempt to remove() root node of Reduction" );
        }

        return (CSTNode)elements.remove( index );
    }



   /**
    *  Creates a <code>Reduction</code> from this node.  Returns self if the
    *  node is already a <code>Reduction</code>.
    */

    public Reduction asReduction() 
    {
        return this;
    }

}

