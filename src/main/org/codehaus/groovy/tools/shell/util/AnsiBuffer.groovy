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

import org.codehaus.groovy.tools.shell.util.AnsiUtils as ANSI

/**
 * Provides simplified access to creating string with ANSI escape-codes embedded.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class AnsiBuffer
{
    private final StringBuffer buffer = new StringBuffer()
    
    private final AnsiBufferSelector selector = new AnsiBufferSelector(buffer: this)
    
    boolean autoClear = true
    
    String toString() {
        if (autoClear) {
            String str = buffer
            
            clear()
            
            return str
        }
        else {
            return buffer
        }
    }
    
    void clear() {
        buffer.length = 0
    }
    
    AnsiBuffer append(final String text) {
        assert text != null
        
        buffer << text
        
        return this
    }
    
    AnsiBuffer append(final Object obj) {
        return append(String.valueOf(obj))
    }
    
    def leftShift(final String text) {
        return append(text)
    }
    
    def leftShift(final Object obj) {
        return append(String.valueOf(obj))
    }
    
    AnsiBuffer attrib(final String text, final int code) {
        if (ANSI.ENABLED) {
            buffer << ANSI.attrib(code) << text << ANSI.attrib(ANSI.CODES.off)
        }
        else {
            buffer << text
        }
        
        return this
    }
    
    def invokeMethod(String name, Object args) {
        assert name
        
        def code = ANSI.CODES[name]
        
        if (code) {
            name = 'attrib'
            
            if (args != null && args.class.array) {
                args << code
            }
            else {
                args = [ args, code ]
            }
        }
        
        return metaClass.invokeMethod(this, name, args)
    }
    
    def getProperty(final String name) {
        return selector.select(name)
    }
}

class AnsiBufferSelector
{
    AnsiBuffer buffer
    
    String selected
    
    def leftShift(final String text) {
        assert selected
        
        return buffer.invokeMethod(selected, text)
    }
    
    def select(final String name) {
        assert name
        
        selected = name
        
        return this
    }
}

