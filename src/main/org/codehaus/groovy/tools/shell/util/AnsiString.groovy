/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package org.codehaus.groovy.tools.shell.util

/**
 * Provides primative support for ANSI escape-code encoding by parsing out and replacing 
 * expressions like <code>@|code[,code] text|</code>.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class AnsiString
{
    private static final String BEGIN_TOKEN = '@|'
    
    private static final String END_TOKEN = '|'
    
    private static final AnsiBuffer buffer = new AnsiBuffer()

    public static String render(final String input) {
        assert input
        
        int c = 0, p, s

        while (c < input.size()) {
            p = input.indexOf(BEGIN_TOKEN, c)
            if (p < 0) {
                break
            }
            
            s = input.indexOf(END_TOKEN, p + BEGIN_TOKEN.size())
            assert s >= 0 : "Missing '$END_TOKEN': $input"
            
            String expr = input.substring(p + BEGIN_TOKEN.size(), s)
            
            buffer << input.substring(c, p)

            evaluate(expr)

            c = s + END_TOKEN.size()
        }

        buffer << input.substring(c)

        return buffer.toString()
    }
    
    private static void evaluate(final String input) {
        assert input
        
        int i = input.indexOf(' ')
        assert i >= 0 : "Missing space after ANSI code: $input"
        
        def codes = input[0..<i].tokenize(',')
        def text = input[i+1..-1]
        
        //
        // FIXME: This isn't working as desired
        //
        
        codes.each { code ->
            buffer.invokeMethod(code, text)
        }
    }
}

