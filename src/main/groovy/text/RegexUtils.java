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

public class RegexUtils {
    private static final String BS = "\\";
    private static final String E = "\\E";
    private static final String Q = "\\Q";
    private static final int NO_MATCH = -1;

    // Replacement for Pattern.quote from JDK 1.5
    public static String quote(String s) {
        final int len = s.length();
        final StringBuffer sb = new StringBuffer(len * 2);
        int eIndex = s.indexOf(E);
        if (eIndex == NO_MATCH)
            return sb.append(Q).append(s).append(E).toString();

        sb.append(Q);
        eIndex = 0;
        int cur = 0;
        while ((eIndex = s.indexOf(E, cur)) != NO_MATCH) {
            sb.append(s.substring(cur, eIndex)).append(E + BS + E + Q);
            cur = eIndex + 2;
        }
        return sb.append(s.substring(cur, len)).append(E).toString();
    }
}
