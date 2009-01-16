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
package groovy.text;

import java.util.regex.Pattern;

public class RegexUtils {
    private static final String BS = "\\";
    private static final String E = "\\E";
    private static final String Q = "\\Q";
    private static final int NO_MATCH = -1;

    /**
     * Replacement for Pattern.quote from JDK 1.5
     */
    public static String quote(String s) {
        if (s.indexOf(E) < 0) {
            // JDK 1.4 String.replaceAll has a bug when a quoted pattern contains a BS.
            if (s.indexOf(BS) >= 0) {
                // So we end the quotation, escape the BS with a BS, then back to the quote.
                s = s.replaceAll(BS + BS, BS + E + BS + BS + BS + BS + BS + Q);
            }
            
            return Q + s + E;
        }
        
        final int len = s.length();
        final StringBuffer sb = new StringBuffer(len * 2);
        
        final Pattern p = Pattern.compile(BS + BS);

        sb.append(Q);
        
        int cur = 0;
        int eIndex;
        
        while ((eIndex = s.indexOf(E, cur)) >= 0) {
            // JDK String.replaceAll has a bug when a quoted pattern contains a BS.
            sb.append(p.matcher(s.substring(cur, eIndex)).replaceAll(BS + E + BS + BS + BS + BS + BS + Q));
            sb.append(E + BS + E + Q);
            cur = eIndex + 2;
        }
        
        // JDK 1.4 String.replaceAll has a bug when a quoted pattern contains a BS.
        sb.append(p.matcher(s.substring(cur, len)).replaceAll(BS + E + BS + BS + BS + BS + BS + Q));
        
        return sb.toString();
    }
        
    /**
     * Replacement for Matcher.quoteReplacement from JDK 1.5
     */
    public static String quoteReplacement(final String str) {
        if ((str.indexOf('$') < 0) && (str.indexOf('\\') < 0)) {
            return str;
        }
        
        return str.replaceAll("[$\\\\]", "\\\\$0");
    }
}
