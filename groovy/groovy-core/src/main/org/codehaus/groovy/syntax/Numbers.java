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

import java.math.BigInteger;
import java.math.BigDecimal;

import org.codehaus.groovy.syntax.parser.ParserException;

/**
 *  Helper class for processing Groovy numeric literals.
 *
 *  @author Brian Larson
 *  @author <a href="mailto:cpoirier@dreaming.org">Chris Poirier</a>
 *
 *  @version $Id$
 */

public class Numbers
{



  //---------------------------------------------------------------------------
  // LEXING SUPPORT


   /**
    *  Returns true if the specified character is a base-10 digit.
    */

    public static boolean isDigit( char c )
    {
        return c >= '0' && c <= '9';
    }


   /**
    *  Returns true if the specific character is a base-8 digit.
    */

    public static boolean isOctalDigit( char c )
    {
        return c >= '0' && c <= '7';
    }


   /**
    *  Returns true if the specified character is a base-16 digit.
    */

    public static boolean isHexDigit( char c )
    {
        return isDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }



   /**
    *  Returns true if the specified character is a valid type specifier
    *  for a numeric value.
    */

    public static boolean isNumericTypeSpecifier( char c, boolean isDecimal )
    {
        if( isDecimal )
        {
            switch( c )
            {
                case 'G':
                case 'g':
                case 'D':
                case 'd':
                case 'F':
                case 'f':
                    return true;
            }
        }
        else
        {
            switch( c )
            {
                case 'G':
                case 'g':
                case 'I':
                case 'i':
                case 'L':
                case 'l':
                    return true;
            }
        }

        return false;
    }





  //---------------------------------------------------------------------------
  // PARSING SUPPORT


    private static final BigInteger MAX_LONG    = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger MIN_LONG    = BigInteger.valueOf(Long.MIN_VALUE);

    private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INTEGER = BigInteger.valueOf(Integer.MIN_VALUE);

    private static final BigDecimal MAX_DOUBLE  = new BigDecimal(String.valueOf(Double.MAX_VALUE));
    private static final BigDecimal MIN_DOUBLE  = MAX_DOUBLE.negate();

    private static final BigDecimal MAX_FLOAT   = new BigDecimal(String.valueOf(Float.MAX_VALUE));
    private static final BigDecimal MIN_FLOAT   = MAX_FLOAT.negate();



   /**
    *  Builds a Number from the given integer descriptor.  Creates the narrowest
    *  type possible, or a specific type, if specified.
    *
    *  @param  text literal text to parse
    *  @return instantiated Number object
    *  @throws NumberFormatException if the number does not fit within the type
    *          requested by the type specifier suffix (invalid numbers don't make
    *          it here)
    */

    public static Number parseInteger( String text )
    {
        char c = ' ';
        int length = text.length();


        //
        // Strip off the sign, if present

        boolean negative = false;
        if( (c = text.charAt(0)) == '-' || c == '+' )
        {
            negative = (c == '-');
            text = text.substring( 1, length );
            length -= 1;
        }


        //
        // Determine radix (default is 10).

        int radix = 10;
        if( text.charAt(0) == '0' && length > 1 )
        {
            if( (c = text.charAt(1)) == 'X' || c == 'x' )
            {
                radix = 16;
                text = text.substring( 2, length);
                length -= 2;
            }
            else
            {
                radix = 8;
            }
        }


        //
        // Strip off any type specifier and convert it to lower
        // case, if present.

        char type = 'x';  // pick best fit
        if( isNumericTypeSpecifier(text.charAt(length-1), false) )
        {
            type = Character.toLowerCase( text.charAt(length-1) );
            text = text.substring( 0, length-1);

            length -= 1;
        }


        //
        // Add the sign back, if necessary

        if( negative )
        {
            text = "-" + text;
        }


        //
        // Build the specified type or, if no type was specified, the
        // smallest type in which the number will fit.

        switch (type)
        {
            case 'i':
                return new Integer( Integer.parseInt(text, radix) );

            case 'l':
                return new Long( Long.parseLong(text, radix) );

            case 'g':
                return new BigInteger( text, radix );

            default:

                //
                // If not specified, we will return the narrowest possible
                // of Integer, Long, and BigInteger.

                BigInteger value = new BigInteger( text, radix );

                if( value.compareTo(MAX_INTEGER) <= 0 && value.compareTo(MIN_INTEGER) >= 0 )
                {
                    return new Integer(value.intValue());
                }
                else if( value.compareTo(MAX_LONG) <= 0 && value.compareTo(MIN_LONG) >= 0 )
                {
                    return new Long(value.longValue());
                }

                return value;
        }
    }



   /**
    *  Builds a Number from the given decimal descriptor.  Uses BigDecimal,
    *  unless, Double or Float is requested.
    *
    *  @param  text literal text to parse
    *  @return instantiated Number object
    *  @throws NumberFormatException if the number does not fit within the type
    *          requested by the type specifier suffix (invalid numbers don't make
    *          it here)
    */

    public static Number parseDecimal( String text )
    {
        int length = text.length();


        //
        // Strip off any type specifier and convert it to lower
        // case, if present.

        char type = 'x';
        if( isNumericTypeSpecifier(text.charAt(length-1), true) )
        {
            type = Character.toLowerCase( text.charAt(length-1) );
            text = text.substring( 0, length-1 );

            length -= 1;
        }


        //
        // Build the specified type or default to BigDecimal

        BigDecimal value = new BigDecimal( text );
        switch( type )
        {
            case 'f':
                if( value.compareTo(MAX_FLOAT) <= 0 && value.compareTo(MIN_FLOAT) >= 0)
                {
                    return new Float( text );
                }
                throw new NumberFormatException( "out of range" );

            case 'd':
                if( value.compareTo(MAX_DOUBLE) <= 0 && value.compareTo(MIN_DOUBLE) >= 0)
                {
                    return new Double( text );
                }
                throw new NumberFormatException( "out of range" );

            case 'g':
            default:
                return new BigDecimal( text );
        }
    }

}
