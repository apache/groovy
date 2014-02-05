/*
 * Copyright 2003-2014 the original author or authors.
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
public class CharScanner {


    protected static final int LETTER_E = 'e';
    protected static final int LETTER_BIG_E = 'E';


    protected static final int DECIMAL_POINT = '.';


    protected static final int ALPHA_0 = '0';
    protected static final int ALPHA_9 = '9';

    protected static final int MINUS = '-';
    protected static final int PLUS = '+';


    public static boolean isDigit( int c ) {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }


    public static boolean isDecimalDigit( int c ) {
        return isDigit( c ) || isDecimalChar( c );
    }


    public static boolean isDecimalChar( int currentChar ) {
        switch ( currentChar ) {
            case MINUS:
            case PLUS:
            case LETTER_E:
            case LETTER_BIG_E:
            case DECIMAL_POINT:
                return true;
        }
        return false;

    }


    public static boolean hasDecimalChar( char[] chars ) {

        for ( int index = 0; index < chars.length; index++ ) {
            switch ( chars[ index ] ) {
                case MINUS:
                case PLUS:
                case LETTER_E:
                case LETTER_BIG_E:
                case DECIMAL_POINT:
                    return true;
            }
        }
        return false;

    }


    public static char[][] splitByCharsFromToDelims( final char[] inputArray, int from, int to,
                                                     final char... delims ) {
        /** Holds the results. */
        char[][] results = new char[ 16 ][];

        final int length = to - from;

        int resultIndex = 0;
        int startCurrentLineIndex = 0;
        int currentLineLength = 1;


        char c = '\u0000';
        int index = from;
        int j;
        char split;


        for (; index < length; index++, currentLineLength++ ) {

            c = inputArray[ index ];

            inner:
            for ( j = 0; j < delims.length; j++ ) {
                split = delims[ j ];
                if ( c == split ) {

                    if ( resultIndex == results.length ) {

                        results = _grow( results );
                    }


                    results[ resultIndex ] = Chr.copy(
                            inputArray, startCurrentLineIndex, currentLineLength - 1 );
                    startCurrentLineIndex = index + 1; //skip the char

                    currentLineLength = 0;
                    resultIndex++;
                    break inner;
                }
            }
        }

        if ( !Chr.in( c, delims ) ) {

            results[ resultIndex ] = Chr.copy(
                    inputArray, startCurrentLineIndex, currentLineLength - 1 );
            resultIndex++;
        }


        int actualLength = resultIndex;
        if ( actualLength < results.length ) {
            final int newSize = results.length - actualLength;
            results = __shrink( results, newSize );
        }
        return results;
    }


    public static char[][] splitByCharsNoneEmpty( final char[] inputArray, int from, int to,
                                                  final char... delims ) {

        final char[][] results = splitByCharsFromToDelims( inputArray, from, to, delims );
        return compact( results );
    }

    public static char[][] compact( char[][] array ) {

        int nullCount = 0;
        for ( char[] ch : array ) {

            if ( ch == null || ch.length == 0 ) {
                nullCount++;
            }
        }
        char[][] newArray = new char[ array.length - nullCount ][];

        int j = 0;
        for ( char[] ch : array ) {

            if ( ch == null || ch.length == 0 ) {
                continue;
            }

            newArray[ j ] = ch;
            j++;
        }
        return newArray;
    }


    private static char[][] _grow( char[][] array ) {

        char[][] newArray = new char[ array.length * 2 ][];
        System.arraycopy( array, 0, newArray, 0, array.length );
        return newArray;
    }

    private static char[][] __shrink( char[][] array, int size ) {
        char[][] newArray = new char[ array.length - size ][];

        System.arraycopy( array, 0, ( char[][] ) newArray, 0, array.length - size );
        return newArray;
    }


    final static String MIN_LONG_STR_NO_SIGN = String.valueOf( Long.MIN_VALUE ).substring( 1 );
    final static String MAX_LONG_STR = String.valueOf( Long.MAX_VALUE );


    final static String MIN_INT_STR_NO_SIGN = String.valueOf( Integer.MIN_VALUE ).substring( 1 );
    final static String MAX_INT_STR = String.valueOf( Integer.MAX_VALUE );

    public static boolean isLong( char[] digitChars, int offset, int len,
                                  boolean negative ) {
        String cmpStr = negative ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        if ( len < cmpLen ) return true;
        if ( len > cmpLen ) return false;

        for ( int i = 0; i < cmpLen; ++i ) {
            int diff = digitChars[ offset + i ] - cmpStr.charAt( i );
            if ( diff != 0 ) {
                return ( diff < 0 );
            }
        }
        return true;
    }


    public static boolean isLong( char[] digitChars ) {
        return isLong( digitChars, 0, digitChars.length );
    }

    public static boolean isLong( char[] digitChars, int offset, int len
    ) {
        String cmpStr = digitChars[ offset ] == '-' ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        if ( len < cmpLen ) return true;
        if ( len > cmpLen ) return false;

        for ( int i = 0; i < cmpLen; ++i ) {
            int diff = digitChars[ offset + i ] - cmpStr.charAt( i );
            if ( diff != 0 ) {
                return ( diff < 0 );
            }
        }
        return true;
    }

    public static boolean isInteger( char[] digitChars, int offset, int len,
                                     boolean negative ) {
        String cmpStr = negative ? MIN_INT_STR_NO_SIGN : MAX_INT_STR;
        int cmpLen = cmpStr.length();
        if ( len < cmpLen ) return true;
        if ( len > cmpLen ) return false;

        for ( int i = 0; i < cmpLen; ++i ) {
            int diff = digitChars[ offset + i ] - cmpStr.charAt( i );
            if ( diff != 0 ) {
                return ( diff < 0 );
            }
        }
        return true;
    }


    public static boolean isInteger( char[] digitChars ) {
        return isInteger( digitChars, 0, digitChars.length );
    }

    public static boolean isInteger( char[] digitChars, int offset, int len ) {


        String cmpStr = ( digitChars[ offset ] == '-' ) ? MIN_INT_STR_NO_SIGN : MAX_INT_STR;
        int cmpLen = cmpStr.length();
        if ( len < cmpLen ) return true;
        if ( len > cmpLen ) return false;

        for ( int i = 0; i < cmpLen; ++i ) {
            int diff = digitChars[ offset + i ] - cmpStr.charAt( i );
            if ( diff != 0 ) {
                return ( diff < 0 );
            }
        }
        return true;
    }

    public static int parseInt( char[] digitChars ) {
        return parseInt( digitChars, 0, digitChars.length );
    }

    public static int parseInt( char[] digitChars, int offset, int len ) {
        int num = digitChars[ offset ] - '0';
        int to = len + offset;
        // This looks ugly, but appears the fastest way (as per measurements)
        if ( ++offset < to ) {
            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
            if ( ++offset < to ) {
                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                if ( ++offset < to ) {
                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                    if ( ++offset < to ) {
                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                        if ( ++offset < to ) {
                            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                            if ( ++offset < to ) {
                                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                if ( ++offset < to ) {
                                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                    if ( ++offset < to ) {
                                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return num;
    }


    public static int parseIntFromTo( char[] digitChars, int offset, int to ) {
        int num = digitChars[ offset ] - '0';
        // This looks ugly, but appears the fastest way (as per measurements)
        if ( ++offset < to ) {
            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
            if ( ++offset < to ) {
                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                if ( ++offset < to ) {
                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                    if ( ++offset < to ) {
                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                        if ( ++offset < to ) {
                            num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                            if ( ++offset < to ) {
                                num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                if ( ++offset < to ) {
                                    num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                    if ( ++offset < to ) {
                                        num = ( num * 10 ) + ( digitChars[ offset ] - '0' );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return num;
    }

    public static int parseIntIgnoreDot( char[] digitChars, int offset, int len ) {
        int num = digitChars[ offset ] - '0';
        int to = len + offset;
        // This looks ugly, but appears the fastest way (as per measurements)
        if ( ++offset < to ) {
            num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
            if ( ++offset < to ) {
                num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                if ( ++offset < to ) {
                    num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                    if ( ++offset < to ) {
                        num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                        if ( ++offset < to ) {
                            num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                            if ( ++offset < to ) {
                                num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                if ( ++offset < to ) {
                                    num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                    if ( ++offset < to ) {
                                        num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                        if ( ++offset < to ) {
                                            num = digitChars[ offset ] != '.' ? ( num * 10 ) + ( digitChars[ offset ] - '0' ) : num;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return num;
    }

    public static long parseLong( char[] digitChars, int offset, int len ) {
        int len1 = len - 9;
        long val = parseInt( digitChars, offset, len1 ) * L_BILLION;
        return val + ( long ) parseInt( digitChars, offset + len1, 9 );
    }


    public static long parseLong( char[] digitChars ) {
        return parseLong( digitChars, 0, digitChars.length );
    }

    public static long parseLongIgnoreDot( char[] digitChars, int offset, int len ) {
        int len1 = len - 9;
        long val = parseIntIgnoreDot( digitChars, offset, len1 ) * L_BILLION;
        return val + ( long ) parseIntIgnoreDot( digitChars, offset + len1, 9 );
    }

    private final static long L_BILLION = 1000000000;

    public static double doubleValue( char[] buffer ) {
        return doubleValue( buffer, 0, buffer.length );
    }

    public static double doubleValue( char[] buffer, int startIndex, int endIndex ) {

        boolean simple = true;
        int digitsPastPoint = 0;
        boolean foundPoint = false;
        boolean negative = false;

        double sign;

        if ( buffer[ startIndex ] == '-' ) {
            startIndex++;
            negative = true;
            sign = -1.0;
        } else {
            negative = false;
            sign = 1.0;
        }

        loop:
        for ( int index = startIndex; index < endIndex; index++ ) {
            char ch = buffer[ index ];
            switch ( ch ) {
                case 'e':
                    simple = false;
                    break loop;
                case 'E':
                    simple = false;
                    break loop;
                case 'F':
                    simple = false;
                    break loop;
                case 'f':
                    simple = false;
                    break loop;
                case '.':
                    foundPoint = true;
                    continue loop;
            }
            if ( foundPoint ) {
                digitsPastPoint++;
                if ( digitsPastPoint >= powersOf10.length ) {
                    simple = true;
                    break;
                }
            }
        }

        if ( simple ) {
            long value;
            final int length = endIndex - startIndex;

            if ( isInteger( buffer, startIndex, length, negative ) ) {
                value = parseIntIgnoreDot( buffer, startIndex, length );
            } else {
                value = parseLongIgnoreDot( buffer, startIndex, length );
            }
            if ( digitsPastPoint < powersOf10.length ) {
                double power = powersOf10[ digitsPastPoint ] * sign;
                return value / power;

            }


        }

        return Double.parseDouble( new String( buffer, startIndex, ( endIndex - startIndex ) ) ) * sign;
    }


    public static double simpleDouble( char[] buffer, boolean simple, boolean negative, int digitsPastPoint, int startIndex, int endIndex ) {

        double sign;

        if ( negative ) {
            sign = -1.0;
        } else {
            sign = 1.0;
        }


        if ( simple ) {
            long value;
            final int length = endIndex - startIndex;

            if ( isInteger( buffer, startIndex, length, negative ) ) {
                value = parseIntIgnoreDot( buffer, startIndex, length );
            } else {
                value = parseLongIgnoreDot( buffer, startIndex, length );
            }
            if ( digitsPastPoint < powersOf10.length ) {
                double power = powersOf10[ digitsPastPoint ] * sign;
                return value / power;

            }


        }

        return Double.parseDouble( new String( buffer, startIndex, ( endIndex - startIndex ) ) ) * sign;
    }


    private static double powersOf10[] = {
            1.0,
            10.0,
            100.0,
            1000.0,
            10000.0,
            100000.0,
            1000000.0,
            10000000.0,
            100000000.0,
            1000000000.0,
            10000000000.0,
            100000000000.0,
            1000000000000.0,
            10000000000000.0,
            100000000000000.0,
    };


    public static int skipWhiteSpace( char[] array, int index, final int length ) {
        int c;
        for (; index < length; index++ ) {
            c = array[ index ];
            if ( c > 32 ) {

                return index;
            }
        }
        return index;
    }


    public static char[] readNumber( char[] array, int idx, final int len ) {
        final int startIndex = idx;

        while ( true ) {
            if ( !CharScanner.isDecimalDigit( array[ idx ] ) ) {
                break;
            } else {
                idx++;
                if ( idx >= len ) break;
            }
        }

        return ArrayUtils.copyRange( array, startIndex, idx );


    }


    public static String errorDetails( String message, char[] array, int index, int ch ) {
        CharBuf buf = CharBuf.create( 255 );

        buf.addLine( message );


        buf.addLine( "" );
        buf.addLine( "The current character read is " + debugCharDescription( ch ) );


        buf.addLine( message );

        int line = 0;
        int lastLineIndex = 0;

        for ( int i = 0; i < index && i < array.length; i++ ) {
            if ( array[ i ] == '\n' ) {
                line++;
                lastLineIndex = i + 1;
            }
        }

        int count = 0;

        for ( int i = lastLineIndex; i < array.length; i++, count++ ) {
            if ( array[ i ] == '\n' ) {
                break;
            }
        }


        buf.addLine( "line number " + ( line + 1 ) );
        buf.addLine( "index number " + index );


        try {
            buf.addLine( new String( array, lastLineIndex, count ) );
        } catch ( Exception ex ) {

            try {
                int start = index = ( index - 10 < 0 ) ? 0 : index - 10;

                buf.addLine( new String( array, start, index ) );
            } catch ( Exception ex2 ) {
                buf.addLine( new String( array, 0, array.length ) );
            }
        }
        for ( int i = 0; i < ( index - lastLineIndex ); i++ ) {
            buf.add( '.' );
        }
        buf.add( '^' );

        return buf.toString();
    }


    public static String debugCharDescription( int c ) {
        String charString;
        if ( c == ' ' ) {
            charString = "[SPACE]";
        } else if ( c == '\t' ) {
            charString = "[TAB]";

        } else if ( c == '\n' ) {
            charString = "[NEWLINE]";

        } else {
            charString = "'" + ( char ) c + "'";
        }

        charString = charString + " with an int value of " + ( ( int ) c );
        return charString;
    }


}

