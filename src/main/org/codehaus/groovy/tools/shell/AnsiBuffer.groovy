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

package org.codehaus.groovy.tools.shell

//
// Based roughly on jline.ANSIBuffer
//

import jline.Terminal
import jline.ANSIBuffer.ANSICodes as ANSI

/**
 * Provides support for rendering ANSI escape muck.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class AnsiBuffer
{
    static final boolean ANSI_ENABLED = Terminal.getTerminal().isANSISupported()
    
    private final StringBuffer buffer = new StringBuffer()
    
    boolean ansiEnabled = ANSI_ENABLED
    
    void setAnsiEnabled(boolean flag) {
        if (buffer.size() != 0) {
            throw new IllegalStateException('Buffer is not empty; can not toggle ANSI state')
        }
        
        ansiEnabled = flag
    }
    
    String toString() {
        return buffer.toString()
    }
    
    void clear() {
        buffer.length = 0
    }
    
    AnsiBuffer append(final String text) {
        buffer << text

        return this
    }
    
    def leftShift(final String text) {
        return append(text)
    }
    
    AnsiBuffer attrib(final String text, final int code) {
        if (ansiEnabled) {
            buffer << ANSI.attrib(code) << text << ANSI.attrib(ANSI.OFF)
        }
        else {
            buffer << text
        }
        
        return this
    }
    
    //
    // TODO: Could probably map these, then use invokeMethod to simplify this code...
    //
    
    AnsiBuffer red(final String text) {
        return attrib(text, ANSI.FG_RED)
    }

    AnsiBuffer blue(final String text) {
        return attrib(text, ANSI.FG_BLUE)
    }

    AnsiBuffer green(final String text) {
        return attrib(text, ANSI.FG_GREEN)
    }

    AnsiBuffer black(final String text) {
        return attrib(text, ANSI.FG_BLACK)
    }

    AnsiBuffer yellow(final String text) {
        return attrib(text, ANSI.FG_YELLOW)
    }

    AnsiBuffer magenta(final String text) {
        return attrib(text, ANSI.FG_MAGENTA)
    }

    AnsiBuffer cyan(final String text) {
        return attrib(text, ANSI.FG_CYAN)
    }

    AnsiBuffer bold(final String text) {
        return attrib(text, ANSI.BOLD)
    }

    AnsiBuffer underscore(final String text) {
        return attrib(text, ANSI.UNDERSCORE)
    }

    AnsiBuffer blink(final String text) {
        return attrib(text, ANSI.BLINK)
    }

    AnsiBuffer reverse(final String text) {
        return attrib(text, ANSI.REVERSE)
    }
    
    private AnsiBufferSelector selector = new AnsiBufferSelector(buffer: this)
    
    def getProperty(final String name) {
        // Is there a better way to handle this muck?
        if (name in [ 'selected', 'ansiEnabled' ]) {
            return super.getProperty(name)
        }
        
        return selector.select(name)
    }
}

class AnsiBufferSelector
{
    AnsiBuffer buffer
    
    String selected
    
    def leftShift(final String text) {
        return buffer.invokeMethod(selected, text)
    }
    
    def select(final String name) {
        assert name
        
        selected = name
        
        return this
    }
}

