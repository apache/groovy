/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *  Various utility functions for use in the compiler.
 */

public abstract class Utilities
{
    private static final Set<String> INVALID_JAVA_IDENTIFIERS = new HashSet<String>(Arrays.asList((
            "abstract assert boolean break byte case catch char class const continue default do double else enum " +
            "extends final finally float for goto if implements import instanceof int interface long native new " +
            "package private protected public short static strictfp super switch synchronized this throw throws " +
            "transient try void volatile while true false null").split(" ")));

   /**
    *  Returns a string made up of repetitions of the specified string.
    */

    public static String repeatString( String pattern, int repeats )
    {
        StringBuilder buffer = new StringBuilder( pattern.length() * repeats );
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

    /**
     * Tells if the given string is a valid Java identifier.
     */
    public static boolean isJavaIdentifier(String name) {
        if (name.length() == 0 || INVALID_JAVA_IDENTIFIERS.contains(name)) return false;
        char[] chars = name.toCharArray();
        if (!Character.isJavaIdentifierStart(chars[0])) return false;
        for (int i = 1; i < chars.length; i++ ) {
            if (!Character.isJavaIdentifierPart(chars[i])) return false;
        }
        return true;
    }    
    
    private static final String eol = System.lineSeparator();

}
