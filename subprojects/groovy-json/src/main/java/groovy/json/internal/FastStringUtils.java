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
    public static final boolean ENABLED;

    private static final boolean WRITE_TO_FINAL_FIELDS = Boolean.parseBoolean( System.getProperty( "groovy.json.faststringutils.write.to.final.fields", "false" ) );
    private static final boolean DISABLE = Boolean.parseBoolean( System.getProperty( "groovy.json.faststringutils.disable", "false" ) );


    static {

        boolean enabled = !DISABLE; //Check to see if it is forced to disabled.
        Unsafe unsafe = null;
        long valueFieldOffset = -1L;


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


        /* Now that we know Unsafe works, let's grab the string value field. */
        if ( enabled ) {
            try {
                valueFieldOffset = unsafe.objectFieldOffset( String.class.getDeclaredField( "value" ) );


            } catch ( Throwable cause ) {
                enabled = false;
            }

            /* If for some reason we did not find value, then disable the whole thing. */
            enabled &= enabled && valueFieldOffset != -1;
        }


        /* Disable support if we find offset or count.  */
        if ( enabled ) {
            try {
                unsafe.objectFieldOffset( String.class.getDeclaredField( "offset" ) );
                unsafe.objectFieldOffset( String.class.getDeclaredField( "count" ) );
                enabled = false;
            } catch ( Throwable cause ) {
            }

        }

        STRING_VALUE_FIELD_OFFSET = valueFieldOffset;
        ENABLED = enabled;
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


    public static char[] toCharArray( final CharSequence charSequence ) {
        return toCharArray( charSequence.toString() );
    }

    public static String noCopyStringFromChars( final char[] chars ) {

        if ( WRITE_TO_FINAL_FIELDS && ENABLED ) {

            final String string = new String();
            UNSAFE.putObject( string, STRING_VALUE_FIELD_OFFSET, chars );

            return string;
        } else {
            return new String( chars );
        }
    }
}
