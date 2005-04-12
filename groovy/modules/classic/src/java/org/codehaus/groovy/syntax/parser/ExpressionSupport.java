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

import org.codehaus.groovy.syntax.*;



/**
 *  A helper for the expression parsing system that provides in-depth
 *  analysis of <code>CSTNode</code>s.
 *
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 */

public class ExpressionSupport
{

  //---------------------------------------------------------------------------
  // EXPRESSION IDENTIFICATION


   /**
    *  Returns true if the node is a complete expression (something that has
    *  a value).
    */

    public static boolean isAnExpression( CSTNode node, boolean unknownReturns )
    {
        if( node.isA(Types.UNKNOWN) )
        {
            return unknownReturns;
        }

        return node.isAnExpression();
    }



   /**
    *  A synonym for <code>isAnExpression( node, false )</code>.
    */

    public static boolean isAnExpression( CSTNode node )
    {
        return isAnExpression( node, false );
    }




  //---------------------------------------------------------------------------
  // OPERATOR IDENTIFICATION


   /**
    *  Returns true if the node is an operator and not an expression (see
    *  above).
    */

    public static boolean isAnOperator( CSTNode node, boolean unknownReturns )
    {
        if( node.isA(Types.UNKNOWN) )
        {
            return unknownReturns;
        }

        return !node.isAnExpression();
    }



   /**
    *  A synonym for <code>isAnOperator(node, false)</code>.
    */

    public static boolean isAnOperator( CSTNode node )
    {
        return isAnOperator( node, false );
    }




  //---------------------------------------------------------------------------
  // VARIABLE IDENTIFICATION


   /**
    *  Returns true if the node might be a variable.
    */

    public static boolean isAVariable( CSTNode node )
    {
        switch( node.getMeaning() )
        {
            case Types.LEFT_SQUARE_BRACKET:
                if( node.isAnExpression() )
                {
                   return isAVariable( node.get(1) );
                }
                break;

            case Types.DOT:
            case Types.NAVIGATE:
            {
                if( node.isAnExpression() && node.get(2).getMeaning() == Types.IDENTIFIER )
                {
                    return true;
                }
                break;
            }

            case Types.IDENTIFIER:
            {
                return true;
            }
        }

        return false;
    }




  //---------------------------------------------------------------------------
  // METHOD IDENTIFICATION


   /**
    *  Returns true if the node might be a method.
    */

    public static boolean isInvokable( CSTNode node )
    {
        switch( node.getMeaning() )
        {
            case Types.SYNTH_CLOSURE:
            case Types.SYNTH_METHOD_CALL:
            case Types.KEYWORD_SUPER:
            case Types.KEYWORD_THIS:
                return true;

            default:
                return isAVariable(node);
        }
    }




  //---------------------------------------------------------------------------
  // ASSIGNMENT TARGET IDENTIFICATION


   /**
    *  Returns true if the node is a modifiable expression (ie. something that
    *  can be the target of an assignment).  Note that this determination is
    *  approximate: false negatives won't happen, but false positives are
    *  distinctly possible, and must be resolved in later phases.
    */

    public static boolean isAModifiableExpression( CSTNode node, boolean unknownReturns )
    {
        if( isAnExpression(node, unknownReturns) )
        {
            if( isAVariable(node) )
            {
                return true;
            }

            else if( node.getMeaning() == Types.SYNTH_LIST )
            {
                boolean is = true;
                for( int i = 1; i < node.size(); i++ )
                {
                    if( !isAModifiableExpression(node.get(i), unknownReturns) )
                    {
                        is = false;
                        break;
                    }
                }
                return is;
            }

        }

        return false;
    }



   /**
    *  A synonym for <code>isAModifiableExpression( node, false )</code>.
    */

    public static boolean isAModifiableExpression( CSTNode node )
    {
        return isAModifiableExpression( node, false );
    }




  //---------------------------------------------------------------------------
  // TYPE OPERATIONS IDENTIFICATION


   /**
    *  Returns true if the node is potentially a cast operator.
    */

    public static boolean isPotentialCastOperator( CSTNode node )
    {
        if( node.isA(Types.LEFT_PARENTHESIS) && node.isAnExpression() )
        {
            return isAPotentialTypeName( node.get(1), false );
        }

        return false;
    }



   /**
    *  Returns true if the node is potentially a type name.
    */

    public static boolean isAPotentialTypeName( CSTNode node, boolean allowVoid )
    {
        if( node.isA(allowVoid ? Types.TYPE_NAME : Types.CREATABLE_TYPE_NAME) )
        {
            return true;
        }
        else if( node.isA(Types.DOT) && node.isAnExpression() )
        {
            return isAPotentialTypeName(node.get(2), allowVoid) && isAPotentialTypeName(node.get(1), allowVoid);
        }
        else if( node.isA(Types.LEFT_SQUARE_BRACKET) && node.isAnExpression() && node.size() == 2 )
        {
            return isAPotentialTypeName(node.get(1), allowVoid );
        }

        return false;
    }




}
