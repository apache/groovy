/*
 * Copyright 2003-2013 the original author or authors.
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
 *
 * Derived from Boon all rights granted to Groovy project for this fork.
 */
package groovy.json.internal;


/**
 * @author Rick Hightower
 */
public class Chr {


    public static final char[] DEFAULT_SPLIT = { ' ', '\t', ',', ':', ';' };
    public static final char[] NEWLINE_CHARS = { '\n', '\r' };

    /**
     * Creates an array of chars
     *
     * @param size size of the array you want to make
     * @return
     */
    public static char[] arrayOfChar( final int size ) {
        return new char[ size ];
    }


    /**
     * @param array
     * @return
     */
    public static char[] array( final char... array ) {
        return array;
    }

    public static char[] chars( final String array ) {
        return array.toCharArray();
    }

    public static int len( char[] array ) {
        return array.length;
    }


    public static char idx( final char[] array, final int index ) {
        final int i = calculateIndex( array, index );

        return array[ i ];
    }


    public static void idx( final char[] array, int index, char value ) {
        final int i = calculateIndex( array, index );

        array[ i ] = value;
    }


    public static void idx( final char[] array, int index, char[] input ) {
        final int i = calculateIndex( array, index );

        _idx( array, i, input );
    }

    public static char[] slc( char[] array, int startIndex, int endIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int end = calculateIndex( array, endIndex );
        final int newLength = end - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, end index %d, length %d",
                            startIndex, endIndex, array.length )
            );
        }

        char[] newArray = new char[ newLength ];
        Chr.arraycopy( array, start, newArray, 0, newLength );
        return newArray;
    }

    public static char[] slc( char[] array, int startIndex ) {

        final int start = calculateIndex( array, startIndex );
        final int newLength = array.length - start;

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            startIndex, array.length )
            );
        }

        char[] newArray = new char[ newLength ];
        arraycopy ( array, start, newArray, 0, newLength );
        return newArray;
    }

    public static char[] slcEnd( char[] array, int endIndex ) {

        final int end = calculateIndex( array, endIndex );
        final int newLength = end; // +    (endIndex < 0 ? 1 : 0);

        if ( newLength < 0 ) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format( "start index %d, length %d",
                            endIndex, array.length )
            );
        }

        char[] newArray = new char[ newLength ];
        arraycopy ( array, 0, newArray, 0, newLength );
        return newArray;
    }

    public static boolean in( char value, char[] array ) {
        for ( char currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }



    public static boolean in( int value, char[] array ) {
        for ( int currentValue : array ) {
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }

    public static boolean in( char value, int offset, char[] array ) {
        for ( int index = offset; index < array.length; index++ ) {
            char currentValue = array[ index ];
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }


    public static boolean in( char value, int offset, int end, char[] array ) {
        for ( int index = offset; index < end; index++ ) {
            char currentValue = array[ index ];
            if ( currentValue == value ) {
                return true;
            }
        }
        return false;
    }


    public static char[] grow( char[] array, final int size ) {
        char[] newArray = new char[ array.length + size ];
        arraycopy ( array, 0, newArray, 0, array.length );
        return newArray;
    }


    public static char[] grow( char[] array ) {
        char[] newArray = new char[ array.length * 2 ];
        arraycopy ( array, 0, newArray, 0, array.length );
        return newArray;
    }

    public static char[] shrink( char[] array, int size ) {
        char[] newArray = new char[ array.length - size ];

        arraycopy ( array, 0, newArray, 0, array.length - size );
        return newArray;
    }


    public static char[] compact( char[] array ) {

        int nullCount = 0;
        for ( char ch : array ) {

            if ( ch == '\0' ) {
                nullCount++;
            }
        }
        char[] newArray = new char[ array.length - nullCount ];

        int j = 0;
        for ( char ch : array ) {

            if ( ch == '\0' ) {
                continue;
            }

            newArray[ j ] = ch;
            j++;
        }
        return newArray;
    }

    public static char[] copy( char[] array ) {
        char[] newArray = new char[ array.length ];
        arraycopy ( array, 0, newArray, 0, array.length );
        return newArray;
    }

    public static char[] copy( char[] array, int offset, int length ) {
        char[] newArray = new char[ length ];
        arraycopy ( array, offset, newArray, 0, length );
        return newArray;
    }


    public static char[] add( char[] array, char v ) {
        char[] newArray = new char[ array.length + 1 ];
        arraycopy ( array, 0, newArray, 0, array.length );
        newArray[ array.length ] = v;
        return newArray;
    }


    public static char[] add( char[] array, String str ) {
        return add( array, str.toCharArray() );
    }

    public static char[] add( char[] array, StringBuilder stringBuilder ) {
        return add( array, getCharsFromStringBuilder( stringBuilder ) );
    }


    public static char[] add( char[] array, char[] array2 ) {
        char[] newArray = new char[ array.length + array2.length ];
        arraycopy ( array, 0, newArray, 0, array.length );
        arraycopy ( array2, 0, newArray, array.length, array2.length );
        return newArray;
    }


    public static char[] insert( final char[] array, final int idx, final char v ) {

        if ( idx >= array.length ) {
            return add( array, v );
        }

        final int index = calculateIndex( array, idx );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        char[] newArray = new char[ array.length + 1 ];

        if ( index != 0 ) {
            /* Copy up to the length in the array before the index. */
            /*                 src     sbegin  dst       dbegin   length of copy */
            arraycopy ( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;
        int remainingIndex = array.length - index;

        if ( lastIndex ) {
            /* Copy the area after the insert. Make sure we don't write over the end. */
            /*                 src  sbegin   dst       dbegin     length of copy */
            arraycopy ( array, index, newArray, index + 1, remainingIndex );

        } else {
            /* Copy the area after the insert.  */
            /*                 src  sbegin   dst       dbegin     length of copy */
            arraycopy ( array, index, newArray, index + 1, remainingIndex );

        }

        newArray[ index ] = v;
        return newArray;
    }


    public static char[] insert( final char[] array, final int fromIndex, String values ) {
        return insert( array, fromIndex, values.toCharArray() );
    }


    public static char[] insert( final char[] array, final int fromIndex, StringBuilder values ) {
        return insert( array, fromIndex, getCharsFromStringBuilder( values ) );
    }


    public static char[] insert( final char[] array, final int fromIndex, final char[] values ) {

        if ( fromIndex >= array.length ) {
            return add( array, values );
        }

        final int index = calculateIndex( array, fromIndex );

        //Object newArray = Array.newInstance(array.getClass().getComponentType(), array.length+1);
        char[] newArray = new char[ array.length + values.length ];

        if ( index != 0 ) {
            /* Copy up to the length in the array before the index. */
            /*                 src     sbegin  dst       dbegin   length of copy */
            arraycopy ( array, 0, newArray, 0, index );
        }


        boolean lastIndex = index == array.length - 1;

        int toIndex = index + values.length;
        int remainingIndex = newArray.length - toIndex;

        if ( lastIndex ) {
            /* Copy the area after the insert. Make sure we don't write over the end. */
            /*                 src  sbegin   dst       dbegin     length of copy */
            arraycopy ( array, index, newArray, index + values.length, remainingIndex );

        } else {
            /* Copy the area after the insert.  */
            /*                 src  sbegin   dst       dbegin     length of copy */
            arraycopy ( array, index, newArray, index + values.length, remainingIndex );

        }

        for ( int i = index, j = 0; i < toIndex; i++, j++ ) {
            newArray[ i ] = values[ j ];
        }
        return newArray;
    }



    /* End universal methods. */


    private static char[] getCharsFromStringBuilder( StringBuilder sbuf ) {
        int length = sbuf.length();
        char[] array2 = new char[ sbuf.length() ];
        sbuf.getChars( 0, sbuf.length(), array2, 0 );
        return array2;
    }

    private static int calculateIndex( char[] array, int originalIndex ) {
        final int length = array.length;



        int index = originalIndex;

        /* Adjust for reading from the right as in
        -1 reads the 4th element if the length is 5
         */
        if ( index < 0 ) {
            index = length + index;
        }

        /* Bounds check
            if it is still less than 0, then they
            have an negative index that is greater than length
         */
         /* Bounds check
            if it is still less than 0, then they
            have an negative index that is greater than length
         */
        if ( index < 0 ) {
            index = 0;
        }
        if ( index >= length ) {
            index = length - 1;
        }
        return index;
    }


    //
    //
    //


    public static char[] rpad( final char[] in, final int size, char pad ) {

        if ( in.length >= size ) {
            return in;
        }

        int index = 0;
        char[] newArray = new char[ size ];

        for ( index = 0; index < in.length; index++ ) {
            newArray[ index ] = in[ index ];
        }


        for (; index < size; index++ ) {
            newArray[ index ] = pad;
        }

        return newArray;
    }


    public static char[] lpad( final char[] in, final int size, char pad ) {

        if ( in.length >= size ) {
            return in;
        }

        int delta = size - in.length;
        int index = 0;
        char[] newArray = new char[ size ];


        for (; index < delta; index++ ) {
            newArray[ index ] = pad;
        }


        for ( int index2 = 0; index2 < in.length; index++, index2++ ) {
            newArray[ index ] = in[ index2 ];
        }

        return newArray;
    }


    public static char[] underBarCase( char[] in ) {

        if ( in == null || in.length == 0 || in.length == 1 ) {
            return in;
        }

        char[] out = null;
        int count = 0;

        boolean wasLower = false;

        for ( int index = 0; index < in.length; index++ ) {
            char ch = in[ index ];
            boolean isUpper;

            isUpper = Character.isUpperCase( ch );

            if ( wasLower && isUpper ) {
                count++;
            }

            wasLower = Character.isLowerCase( ch );

        }

        out = new char[ in.length + count ];

        wasLower = false;

        for ( int index = 0, secondIndex = 0; index < in.length; index++, secondIndex++ ) {
            char ch = in[ index ];
            boolean isUpper;

            isUpper = Character.isUpperCase( ch );

            if ( wasLower && isUpper ) {
                out[ secondIndex ] = '_';
                secondIndex++;
            }

            if ( ch == ' ' || ch == '-' || ch == '\t' ) {
                out[ secondIndex ] = '_';
            } else {
                out[ secondIndex ] = Character.toUpperCase( ch );
            }
            wasLower = Character.isLowerCase( ch );

        }

        return out;
    }


    public static char[] camelCase( char[] in, boolean upper ) {

        if ( in == null || in.length == 0 || in.length == 1 ) {
            return in;
        }

        char[] out = null;
        int count = 0;
        for ( int index = 0; index < in.length; index++ ) {
            char ch = in[ index ];
            if ( ch == '_' || ch == ' ' || ch == '\t' ) {
                count++;
            }
        }

        out = new char[ in.length - count ];


        boolean upperNext = false;

        for ( int index = 0, secondIndex = 0; index < in.length; index++ ) {
            char ch = in[ index ];
            if ( ch == '_' || ch == ' ' || ch == '\t' ) {
                upperNext = true;
            } else {
                out[ secondIndex ] = upperNext ? Character.toUpperCase( ch ) : Character.toLowerCase( ch );
                upperNext = false;
                secondIndex++;
            }
        }

        if ( upper ) {
            out[ 0 ] = Character.toUpperCase( out[ 0 ] );
        } else {
            out[ 0 ] = Character.toLowerCase( out[ 0 ] );
        }

        return out;
    }




    public static boolean contains( char[] chars, char c ) {
        for ( int index = 0; index < chars.length; index++ ) {
            char ch = chars[ index ];
            if ( ch == c ) {
                return true;
            }
        }
        return false;
    }


    public static boolean contains( char[] chars, char c, int start, final int length ) {
        final int to = length + start;
        for ( int index = start; index < to; index++ ) {
            char ch = chars[ index ];
            if ( ch == c ) {
                return true;
            }
        }
        return false;
    }

    public static void _idx( char[] buffer, int location, byte[] chars ) {
        int index2 = 0;
        int endLocation = ( location + chars.length );
        for ( int index = location; index < endLocation; index++, index2++ ) {
            buffer[ index ] = ( char ) chars[ index2 ];
        }
    }


    public static void _idx( final char[] array, int startIndex, char[] input ) {
        try {

            arraycopy ( input, 0, array, startIndex, input.length );
        } catch ( Exception ex ) {
            Exceptions.handle ( String.format ( "array size %d, startIndex %d, input length %d",
                    array.length, startIndex, input.length ), ex );
        }
    }


    private static void arraycopy (final char [] src, final int srcPos, final char [] dest, final int destPos, final int length)  {
        System.arraycopy( src, srcPos, dest, destPos,length );
    }


    public static void _idx( final char[] array, int startIndex, char[] input, final int inputLength ) {
        try {

            arraycopy ( input, 0, array, startIndex, inputLength );
        } catch ( Exception ex ) {
            Exceptions.handle( String.format( "array size %d, startIndex %d, input length %d",
                    array.length, startIndex, input.length ), ex );
        }
    }

    public static void _idx( char[] buffer, int location, byte[] chars, int start, int end ) {

        int index2 = start;
        int endLocation = ( location + ( end - start ) );
        for ( int index = location; index < endLocation; index++, index2++ ) {
            buffer[ index ] = ( char ) chars[ index2 ];
        }

    }


    public static char[] trim( char[] buffer, int start, int to ) {


        while ( ( start < to ) && (
                buffer[ start ] <= ' ' ) ) {
            start++;
        }
        while ( ( start < to ) && (
                buffer[ to - 1 ] <= ' ' ) ) {
            to--;
        }
        return ( ( start > 0 ) || ( to < buffer.length ) ) ?
                java.util.Arrays.copyOfRange( buffer, start, to ) : buffer;
    }


    public static boolean equals( char[] chars1, char[] chars2 ) {
        if (chars1==null && chars2 == null) {
            return true;
        }

        if (chars1==null || chars2 == null) {
            return false;
        }

        if (chars1.length != chars2.length) {
            return false;
        }

        for (int index = 0; index < chars1.length; index++) {
            if (chars1[index]!=chars2[index]) return false;
        }

        return true;
    }

    public static boolean equalsNoNullCheck( char[] chars1, char[] chars2 ) {

        if (chars1.length != chars2.length) {
            return false;
        }

        for (int index = 0; index < chars1.length; index++) {
            if (chars1[index]!=chars2[index]) return false;
        }

        return true;
    }



    public static char[] add( char[]... strings ) {
        int length = 0;
        for ( char[] str : strings ) {
            if ( str == null ) {
                continue;
            }
            length += str.length;
        }
        CharBuf builder = CharBuf.createExact( length );
        for ( char[] str : strings ) {
            if ( str == null ) {
                continue;
            }
            builder.add( str );
        }
        return builder.toCharArray ();
    }
}
