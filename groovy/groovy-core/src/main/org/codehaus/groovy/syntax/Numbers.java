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
    

   /**
    *  Builds a Number from the given integer descriptor.  Creates the narrowest
    *  type possible, or a specific type, if specified.  Note that negative
    *  numbers don't currently make it here (the sign is a separate token).
    */

    public static Number parseInteger( String text ) 
    {
        int length = text.length();

        //
        // Short cut for hex and octal data...

        if( text.charAt(0) == '0' && length > 1 ) 
        {
            char c;
            if( (c = text.charAt(1)) == 'X' || c == 'x' ) 
            {
                return parseHexadecimalInteger( text.substring(2, length) );
            }
            else
            {
                return parseOctalInteger( text.substring(1, length) );
            }
        }


        //
        // Strip off any type specifier, if present.

        char type = 'x';

        if( isNumericTypeSpecifier(text.charAt(length-1), false) )
        {
            type = Character.toLowerCase( text.charAt(length-1) );
            text = text.substring( 0, length );

            length -= 1;
        }

    
        //
        // Get the value

        BigInteger value = new BigInteger( text );

        
        //
        // Produce and return the correct type

        switch( type )
        {
            case 'i':
                return new Integer( value.intValue() );

            case 'l':
                return new Long( value.longValue() );

            case 'g':
                return value;

            default:

                //
                // If not specified, we will return the narrowest possible
                // of Integer, Long, and BigInteger.  Note that because we
                // don't know the sign, we cannot use the full positive range.
                // We test the negative range anyway, to defend against changes
                // in external code.

                if( value.compareTo(MAX_LONG) < 0 && value.compareTo(MIN_LONG) >= 0 ) 
                {
                    if( value.compareTo(MAX_INTEGER) < 0 && value.compareTo(MIN_INTEGER) >= 0 )
                    {
                        return new Integer( value.intValue() );
                    }
                    else
                    {
                        return new Long( value.longValue() );
                    }
                }
        }

        return value;
    }
                


   /**
    *  Parses an integer written in hexadecimal notation (already stripped
    *  of any marker).  Returns a value of width appropriate to the number of 
    *  digits.
    */

    public static Number parseHexadecimalInteger( String text ) 
    {
        int length = text.length();

        if( length > 16 )
        {
            return new BigInteger( text, 16 );
        }
        else if( length > 8 )
        {
            return new Long( Long.parseLong(text, 16) );
        }
        else 
        {
            return new Integer( Integer.parseInt(text, 16) );
        }
    }
        


   /**
    *  Parses an integer written in octal notation (already stripped
    *  of any marker).  Returns a value of width appropriate to the number of 
    *  digits.
    */

    public static Number parseOctalInteger( String text ) 
    {
        int length = text.length();

        if( length > 24 )
        {
            return new BigInteger( text, 8 );
        }
        else if( length > 12 )
        {
            return new Long( Long.parseLong(text, 8) );
        }
        else
        {
            return new Integer( Integer.parseInt(text, 8) );
        }
    }
        



   /**
    *  Builds a Number from the given decimal descriptor.  Uses BigDecimal,
    *  unless, Double or Float is requested.
    */

    public static Number parseDecimal( String text ) 
    {
        //
        // We allow decimal strings to omit the leading "0", but
        // BigDecimal doesn't, so we'll patch it up if necessary.

        if( text.charAt(0) == '.' ) 
        {
            text = "0" + text;
        }


        //
        // Strip off any type specifier, if present.

        char type = 'x';
        int length = text.length();

        if( isNumericTypeSpecifier(text.charAt(length-1), true) )
        {
            type = Character.toLowerCase( text.charAt(length-1) );
            text = text.substring( 0, length );

            length -= 1;
        }

    
        //
        // Get the value

        BigDecimal value = new BigDecimal( text );


        //
        // Produce and return the correct type

        switch( type )
        {
            case 'f':
                return new Float( value.floatValue() );

            case 'd':
                return new Double( value.doubleValue() );

            case 'g':
                return value;
        }

        return value;
    }
    
}
