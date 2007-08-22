/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package groovy.text

import java.util.ResourceBundle
import java.text.MessageFormat

/**
 * Message source backed up by a {@link java.util.ResourceBundle} for simple i18n support.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class MessageSource
{
    //
    // TODO: Allow merging bundles?
    //
    
    private final ResourceBundle bundle

    MessageSource(final String name) {
        assert name

        bundle = ResourceBundle.getBundle(name)
    }

    //
    // NOTE: Using getName() and getClass() here to avoid crazy IAE on
    //       property access (which smells like a Groovy BUG).
    //
    
    MessageSource(final Class type) {
        this(type.getName())
    }

    MessageSource(final Object obj) {
        this(obj.getClass())
    }
    
    /**
     * Get a vanilla message from the resource bundle using the given code.
     */
    String getMessage(final String code) {
        assert code
        
        return bundle.getString(code)
    }

    /**
     * Format a message (based on {@link MessageFormat} using the message
     * from the resource bunding using the given code as a pattern and the
     * given objects as arguments.
     */
    String format(final String code, final Object[] args) {
        assert args
        
        String pattern = getMessage(code)
        
        return MessageFormat.format(pattern, args)
    }
    
    /**
     * @see #getMessage(String)
     */
    def getProperty(final String name) {
        return getMessage(name)
    }
}