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

package org.codehaus.groovy.syntax.parser;

import java.util.ArrayList;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.syntax.*;
import org.codehaus.groovy.GroovyBugError;



/**
 *  A combination stack and helper class for parsing groovy expression.
 *  <p>
 *  Expressions are a little trickier to parse than the statements above.
 *  As a result, we are now doing a hybrid LL/LR parse at the expression
 *  level.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 */

public class ExpressionStack
{
    private ArrayList stack  = new ArrayList();
    private Parser    parser = null;
    private int       open   = 0;


    ExpressionStack( Parser context )
    {
        this.parser = context;
    }



  //---------------------------------------------------------------------------
  // PRIMARY OPERATIONS


   /**
    *  Returns true if the stack is empty.
    */

    boolean isEmpty()
    {
        return stack.isEmpty();
    }



   /**
    *  Returns true if the stack is in a state that can be considered
    *  a complete expression, provided lookahead is amenable, of course.
    */

    public boolean isComplete()
    {
        return size() == 1 && topIsAnExpression();
    }



   /**
    *  Returns true if the stack can be completed without further shifts.
    *  Used by Parser.la(ExpressionStack) to determine when ambiguous tokens
    *  can't be read across a newline.  The algorithm will guess true if it
    *  isn't sure.  If it returns false, you can rely on that analysis.
    */

    public boolean canComplete()
    {
        //
        // We can immediately return false if there is anything
        // open or if the top is not an expression...

        if( open > 0 || !topIsAnExpression() )
        {
            return false;
        }


        //
        // If it is complete, it can complete...

        if( size() == 1 )
        {
            return true;
        }


        //
        // For everything else, we guess true.  It is most likely
        // the case, because of the way the parser moves complex
        // stuff off to LL routines and validates the ordering of
        // nodes pushed on the stack, that if the top is an expression,
        // and nothing is "open" (ie. unmatched parenthesis, early
        // stage ternary operators), the stack can be reduced back
        // to the root without further shifts.  Our guarantee is
        // that our guess true might be wrong, but that we will only
        // return false when we are sure, so, no harm done...

        return true;
    }



   /**
    *  Returns the number of elements in the stack.
    */

    int size()
    {
        return stack.size();
    }



   /**
    *  Pushes a node onto the stack.
    */

    void push( CSTNode node )
    {
        if( (node.isA(Types.LEFT_PARENTHESIS) || node.isA(Types.QUESTION)) && node.size() == 1 )
        {
            open++;
        }

        stack.add( node );
    }



   /**
    *  Pops the node from the top of the stack.
    */

    CSTNode pop()
    {
        CSTNode node = (CSTNode)stack.remove( stack.size() - 1 );

        if( (node.isA(Types.LEFT_PARENTHESIS) || node.isA(Types.QUESTION)) && node.size() == 1 )
        {
            open--;
        }

        return node;
    }



   /**
    *  Returns the top node from the stack without removing it.
    */

    CSTNode top()
    {
        return top(0);
    }



   /**
    *  Returns some node from the stack.  <code>offset</code> is counted
    *  from the top of the stack.
    */

    CSTNode top( int offset )
    {
        if( offset < stack.size() )
        {
            return (CSTNode)stack.get( stack.size() - 1 - offset );
        }
        else
        {
            return Token.NULL;
        }
    }




  //---------------------------------------------------------------------------
  // PARSER OPERATIONS


   /**
    *  Shifts some number of (non-newline) tokens from the stream to the top
    *  of the stack.  They are pushed in order.
    */

    void shift( int count ) throws SyntaxException, CompilationFailedException
    {
        for( int i = 0; i < count; i++ )
        {
            push( parser.consume() );
        }
    }



   /**
    *  Shifts a token from the stream to the top of the stack.
    */

    void shift() throws SyntaxException, CompilationFailedException
    {
        push( parser.consume() );
    }



   /**
    *  Performs a reduce by taking some number of <code>CSTNode</code>s
    *  from the top of the stack, and making one of them a
    *  <code>Reduction</code> with the others as children, then pushes
    *  that new node back onto the stack.
    */

    void reduce( int count, int rootOffset, boolean mark )
    {
        if( count <= rootOffset || rootOffset < 0 || count > size() )
        {
            throw new GroovyBugError( "error in call to ExpressionStack.reduce(): count=" + count + ", rootOffset=" + rootOffset );
        }

        CSTNode   root     = null;
        CSTNode[] children = new CSTNode[count-1];

        for( int child = count - 2, element = 0; element < count; element++ )
        {
            if( element == rootOffset )
            {
                root = pop();
            }
            else
            {
                children[child--] = pop();
            }
        }

        root = root.asReduction();
        for( int i = 0; i < children.length; i++ )
        {
            root.add( children[i] );
        }

        if( mark )
        {
            root.markAsExpression();
        }

        push( root );

    }




  //---------------------------------------------------------------------------
  // TESTS


   /**
    *  Return true if the stack is at the start of an expression.  True if
    *  either the stack is empty or the top token is a left parenthesis.
    */

    boolean atStartOfExpression()
    {
        return isEmpty() || (top().isA(Types.LEFT_PARENTHESIS) && !top().hasChildren());
    }



   /**
    *  Returns true if the top element of the stack is an operator.
    */

    boolean topIsAnOperator( )
    {
        return ExpressionSupport.isAnOperator( top(), false );
    }



   /**
    *  Returns true if the element at the specified offset from the top
    *  of the stack is an operator.
    */

    boolean topIsAnOperator( int offset, boolean unknownReturns )
    {
        return ExpressionSupport.isAnOperator( top(offset), unknownReturns );
    }



   /**
    *  Returns true if the top element of the stack is a modifiable expression.
    */

    boolean topIsAModifiableExpression()
    {
        return ExpressionSupport.isAModifiableExpression( top() );
    }



   /**
    *  Returns true if the top element of the stack is an expression.
    */

    boolean topIsAnExpression( )
    {
        return top().isAnExpression();
    }



  //---------------------------------------------------------------------------
  // SUGAR


   /**
    *  Shifts if the specified flag is true, reports an error otherwise.
    */

    void shiftIf( boolean flag, String error ) throws SyntaxException, CompilationFailedException
    {
        if( flag )
        {
            push( parser.consume() );
        }
        else
        {
            parser.error( error );
        }
    }



   /**
    *  Shifts unless the specified flag is true, reports an error otherwise.
    */

    void shiftUnless( boolean flag, String error ) throws SyntaxException, CompilationFailedException
    {
        if( flag )
        {
            parser.error( error );
        }
        else
        {
            push( parser.consume() );
        }
    }



   /**
    *  Shifts if the top of the stack is an expression, reports an error
    *  otherwise.
    */

    void shiftIfTopIsAnExpression( String error ) throws SyntaxException, CompilationFailedException
    {
        shiftIf( ExpressionSupport.isAnExpression(top(), false), error );
    }



   /**
    *  Shifts if the top of the stack is a operator, reports an
    *  error otherwise.
    */

    void shiftIfTopIsAnOperator( String error ) throws SyntaxException, CompilationFailedException
    {
        shiftIf( ExpressionSupport.isAnOperator(top(), false), error );
    }



   /**
    *  Shifts unless the top of the stack is an expression, reports an
    *  error otherwise.
    */

    void shiftUnlessTopIsAnExpression( String error ) throws SyntaxException, CompilationFailedException
    {
        shiftUnless( ExpressionSupport.isAnExpression(top(), false), error );
    }



   /**
    *  Shifts unless the top of the stack is an operator, reports an
    *  error otherwise.
    */

    void shiftUnlessTopIsAnOperator( String error ) throws SyntaxException, CompilationFailedException
    {
        shiftUnless( ExpressionSupport.isAnOperator(top(), false), error );
    }




  //---------------------------------------------------------------------------
  // OUTPUT


   /**
    *  Creates a string representation of the stack.
    */

    public String toString( )
    {
        StringBuffer buffer = new StringBuffer();
        String newline = System.getProperty( "line.separator", "\n" );
        int count = stack.size();

        buffer.append( "ExpressionStack with " ).append( size() ).append( " elements" ).append( newline );
        for( int i = count - 1; i >= 0; i-- )
        {
            buffer.append( top(i).toString() ).append( newline );
        }

        return buffer.toString();
    }

}
