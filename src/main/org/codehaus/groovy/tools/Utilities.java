package org.codehaus.groovy.tools;

/**
 *  Various utility functions for use in the compiler.
 */

public abstract class Utilities
{
   /**
    *  Returns a string made up of repetitions of the specified string.
    */

    public static String repeatString( String pattern, int repeats )
    {
        StringBuffer buffer = new StringBuffer( pattern.length() * repeats );
        for( int i = 0; i < repeats; i++ )
        {
            buffer.append( pattern );
        }

        return new String( buffer );
    }


   /**
    *  Returns the end-of-line marker.
    */

    public static String eol()
    {
        return eol;
    }
    
    private static String eol = System.getProperty( "line.separator", "\n" ); 

}
