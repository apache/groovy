package org.codehaus.groovy;

import junit.framework.TestCase;

public class GroovyTestCase
    extends TestCase
{

    public GroovyTestCase() {
    }
    
    protected void assertLength(int length,
                                char[] array)
    {
        assertEquals( length,
                      array.length );
    }

    protected void assertLength(int length,
                                int[] array)
    {
        assertEquals( length,
                      array.length );
    }

    protected void assertLength(int length,
                                Object[] array)
    {
        assertEquals( length,
                      array.length );
    }

    protected void assertContains(char expected,
                                  char[] array)
    {
        for ( int i = 0 ; i < array.length ; ++i )
        {
            if ( array[i] == expected )
            {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

        message.append( expected + " not in {" );

        for ( int i = 0 ; i < array.length ; ++i )
        {
            message.append( "'" + array[i] + "'" );

            if ( i < ( array.length - 1 ) )
            {
                message.append( ", " );
            }
        }
        
        message.append( " }" );

        fail( message.toString() );
    }

    protected void assertContains(int expected,
                                  int[] array)
    {
        for ( int i = 0 ; i < array.length ; ++i )
        {
            if ( array[i] == expected )
            {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

        message.append( expected + " not in {" );

        for ( int i = 0 ; i < array.length ; ++i )
        {
            message.append( "'" + array[i] + "'" );

            if ( i < ( array.length - 1 ) )
            {
                message.append( ", " );
            }
        }
        
        message.append( " }" );

        fail( message.toString() );
    }
}
