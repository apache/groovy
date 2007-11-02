/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
