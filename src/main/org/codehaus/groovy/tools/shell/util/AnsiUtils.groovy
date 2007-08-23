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

import jline.Terminal
import jline.ANSIBuffer.ANSICodes

/**
 * Provides support for ANSI muck.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class AnsiUtils
{
    static final Map CODES
    
    static {
        def map = [
            red:            ANSICodes.FG_RED,
            blue:           ANSICodes.FG_BLUE,
            green:          ANSICodes.FG_GREEN,
            black:          ANSICodes.FG_BLACK,
            yellow:         ANSICodes.FG_YELLOW,
            magenta:        ANSICodes.FG_MAGENTA,
            cyan:           ANSICodes.FG_CYAN,
            bold:           ANSICodes.BOLD,
            b:              ANSICodes.BOLD,              // alias to bold
            underscore:     ANSICodes.UNDERSCORE,
            _:              ANSICodes.UNDERSCORE,        // alias to underscore
            blink:          ANSICodes.BLINK,
            reverse:        ANSICodes.REVERSE,
            off:            ANSICodes.OFF,
        ]
        
        //
        // TODO: Add upper-case?
        //
        
        CODES = map
    }
    
    //
    // NOTE: Snagged from commons-lang to test this hack out....
    //
    
    private static final String OS_NAME = System.getProperty("os.name")

    private static final String OS_NAME_WINDOWS_PREFIX = "Windows"

    private static final boolean IS_OS_WINDOWS = getOSMatches(OS_NAME_WINDOWS_PREFIX);

    private static boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    }
    
    private static boolean detect() {
        boolean detected = Terminal.terminal.isANSISupported()
        
        if (!detected && IS_OS_WINDOWS) {
            def path = System.getenv('PATH')
            
            if (path.indexOf('/bin')) {
                
                System.err.println("HACK: Looks like this might by Cygwin; so enable ANSI");
                
                detected = true
            }
        }
        
        return detected
    }
    
    static final boolean DETECTED = detect()
    
    static boolean ENABLED = DETECTED
    
    static String attrib(final int code) {
        return ANSICodes.attrib(code)
    }
    
    static String render(final String text) {
        return AnsiString.render(text)
    }
}

