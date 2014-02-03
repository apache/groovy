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

import static groovy.json.internal.Exceptions.sputs;

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
    private static final boolean DISABLE = Boolean.parseBoolean( System.getProperty( "groovy.json.faststringutils.disable", "false" ) );


    static {

        boolean enabled = !DISABLE; //Check to see if it is forced to disabled.
        Unsafe unsafe = null;
        boolean hasCountAndOffset = false;
        long valueFieldOffset = -1L;
        long offsetFieldOffset = -1L;
        long countFieldOffset = -1L;


        if ( enabled ) {
            try {
                /* Lookup unsafe field, if there are any problems abort. */
                Field unsafeField = Unsafe.class.getDeclaredField( "theUnsafe" );
                unsafeField.setAccessible( true );
                unsafe = ( Unsafe ) unsafeField.get( null );

            } catch ( Throwable cause ) {
                unsafe = null;
                enabled = false;
            }
        }


        /* Now that we know Unsafe works, let's grab the string fields. */
        if ( enabled ) {
            try {
                /* Older strings have value, offset, and count. Newer strings only have value. */
                valueFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "value" ) );
                offsetFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "offset" ) );
                countFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "count" ) );
                hasCountAndOffset = true;
            } catch ( Throwable cause ) {
                hasCountAndOffset = false;
            }

            /* If for some reason we did not find value, then disable the whole thing. */
            enabled = valueFieldOffset != -1;
        }


        STRING_VALUE_FIELD_OFFSET = valueFieldOffset;
        STRING_OFFSET_FIELD_OFFSET = offsetFieldOffset;
        STRING_COUNT_FIELD_OFFSET = countFieldOffset;
        ENABLED = enabled && !hasCountAndOffset;
        UNSAFE = unsafe;

    }


    public static char[] toCharArray( final String string ) {
        if ( ENABLED  ) {
            return ( char[] ) UNSAFE.getObject( string, STRING_VALUE_FIELD_OFFSET );
        } else {
            /* Here we just go ahead an use the default, the only downside is an extra buffer copy. */
            return string.toCharArray();
        }
    }

    private static char[] toCharArrayWithCountOffset( String string ) {

        try {
            char[] value = ( char[] ) UNSAFE.getObject( string, STRING_VALUE_FIELD_OFFSET );

                /* old String version with offset and count  */
            Integer offset = ( Integer ) UNSAFE.getObject( string, STRING_OFFSET_FIELD_OFFSET );
            Integer count = ( Integer ) UNSAFE.getObject( string, STRING_COUNT_FIELD_OFFSET );



            if ( (offset == 0 && count == value.length) ) {
                    /* no need to copy since the offset is 0 and the length and count are the same. */
                    return value;

            } else {
                    /* A subset of the string was used, so only copy a subset over. */
                    char result[] = new char[ count ];
                    System.arraycopy( value, offset, result, 0, count );
                    return result;
            }
        } catch (Exception ex) {
            return Exceptions.handle(char[].class, sputs( "STRING str", string, ex.getMessage() ), ex);
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
