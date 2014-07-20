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
 * Derived from Boon all rights granted to Groovy project.
 */
package groovy.json.internal;



import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Sys {

    private static final boolean is1_7OorLater;
    private static final BigDecimal version;
    private static final boolean is1_7;
    private static final boolean is1_8;


    static {
        BigDecimal v = new BigDecimal ( "-1" );
        String sversion = System.getProperty ( "java.version" );
        if ( sversion.indexOf ( "_" ) != -1 ) {
            final String[] split = sversion.split ( "_" );
            try {

                String ver = split [0];
                if (ver.startsWith ( "1.8" )) {
                    v = new BigDecimal ("1.8" );
                }
                if (ver.startsWith ( "1.7" )) {
                    v = new BigDecimal ("1.7" );
                }

                if (ver.startsWith ( "1.6" )) {
                    v = new BigDecimal ("1.6" );
                }


                if (ver.startsWith ( "1.5" )) {
                    v = new BigDecimal ("1.5" );
                }


                if (ver.startsWith ( "1.9" )) {
                    v = new BigDecimal ("1.9" );
                }

            } catch ( Exception ex ) {
                ex.printStackTrace ();
                System.err.println ( "Unable to determine build number or version" );
            }
        } else if ("1.8.0".equals(sversion)) {
            v = new BigDecimal("1.8");
        } else {
            Pattern p = Pattern.compile("^([1-9]\\.[0-9]+)");
            Matcher matcher = p.matcher(sversion);
            if (matcher.find()) {
                v = new BigDecimal ( matcher.group(0) );
            }
        }

        version = v;

        is1_7OorLater = version.compareTo ( new BigDecimal ( "1.7" )) >=0;
        is1_7 = version.compareTo ( new BigDecimal ( "1.7" ))==0;
        is1_8 = version.compareTo ( new BigDecimal ( "1.8" ))==0;
    }



    public static boolean is1_7OrLater () {
        return is1_7OorLater;
    }

    public static boolean is1_7() {
        return is1_7;
    }
    public static boolean is1_8() {
        return is1_8;
    }
}
