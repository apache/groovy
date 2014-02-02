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

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.*;

/**
 * @author Rick Hightower
 * @author Stephane Landelle
 */
public class FastStringUtils {

    public static final Unsafe UNSAFE;
    public static final long STRING_VALUE_FIELD_OFFSET;
    public static final long STRING_OFFSET_FIELD_OFFSET;
    public static final long STRING_COUNT_FIELD_OFFSET;
    public static final boolean ENABLED;

    private static final boolean WRITE_TO_FINAL_FIELDS = Boolean.parseBoolean( System.getProperty( "groovy.json.faststringutils.write.to.final.fields", "false" ) );
    private static final boolean DISABLE = Boolean.parseBoolean( System.getProperty( "groovy.json.faststringutils.disable", "true" ) );

    static {

        if ( !DISABLE ) {
            Unsafe unsafe;
            try {
                Field unsafeField = Unsafe.class.getDeclaredField( "theUnsafe" );
                unsafeField.setAccessible( true );
                unsafe = ( Unsafe ) unsafeField.get( null );

            } catch ( Throwable cause ) {
                unsafe = null;
            }

            UNSAFE = unsafe;
            ENABLED = unsafe != null;

            long stringValueFieldOffset = -1L;
            long stringOffsetFieldOffset = -1L;
            long stringCountFieldOffset = -1L;

            if ( ENABLED ) {
                try {
                    stringValueFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "value" ) );
                    stringOffsetFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "offset" ) );
                    stringCountFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "count" ) );
                } catch ( Throwable cause ) {
                }
            }

            STRING_VALUE_FIELD_OFFSET = stringValueFieldOffset;
            STRING_OFFSET_FIELD_OFFSET = stringOffsetFieldOffset;
            STRING_COUNT_FIELD_OFFSET = stringCountFieldOffset;

        } else {
            STRING_VALUE_FIELD_OFFSET = -1;
            STRING_OFFSET_FIELD_OFFSET = -1;
            STRING_COUNT_FIELD_OFFSET = -1;
            UNSAFE = null;
            ENABLED = false;
        }
    }

    public static boolean hasUnsafe() {
        return ENABLED;
    }

    public static char[] toCharArray( final String string ) {
        if ( ENABLED ) {
            char[] value = ( char[] ) UNSAFE.getObject( string, STRING_VALUE_FIELD_OFFSET );

            if ( STRING_OFFSET_FIELD_OFFSET != -1 ) {
                // old String version with offset and count
                Integer offset = ( Integer ) UNSAFE.getObject( string, STRING_OFFSET_FIELD_OFFSET );
                Integer count = ( Integer ) UNSAFE.getObject( string, STRING_COUNT_FIELD_OFFSET );

                if ( (offset==null || value == null) || (offset == 0 && count == value.length) ) {
                    // no need to copy
                    return value;

                } else {
                    char result[] = new char[ count ];
                    System.arraycopy( value, offset, result, 0, count );
                    return result;
                }

            } else {
                return value;
            }

        } else {
            return string.toCharArray();
        }
    }

    public static char[] toCharArray( final CharSequence charSequence ) {
        return toCharArray( charSequence.toString() );
    }

    public static char[] toCharArrayFromBytes( final byte[] bytes, Charset charset ) {
        return toCharArray( new String( bytes, charset != null ? charset : Charsets.UTF_8 ) );
    }


    public static char[] toCharArrayFromBytes( final byte[] bytes, String charset ) {


        Charset cs = null;

        try {
            cs = Charset.forName( charset );
        } catch ( Exception ex ) {
            Exceptions.handle( char[].class, ex );
        }
        return toCharArray( new String( bytes, cs != null ? cs : Charsets.UTF_8 ) );
    }

    public static String noCopyStringFromChars( final char[] chars ) {

        if ( WRITE_TO_FINAL_FIELDS && ENABLED ) {

            final String string = new String();
            UNSAFE.putObject( string, STRING_VALUE_FIELD_OFFSET, chars );

            if ( STRING_COUNT_FIELD_OFFSET != -1 ) {
                UNSAFE.putObject( string, STRING_COUNT_FIELD_OFFSET, chars.length );
            }

            return string;
        } else {
            return new String( chars );
        }
    }
}
