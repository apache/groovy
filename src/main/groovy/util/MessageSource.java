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

package groovy.util;

//
// Borrowed and augmented from GShell ( http://svn.apache.org/repos/asf/geronimo/sandbox/gshell/trunk/gshell-core/src/main/java/org/apache/geronimo/gshell/command/MessageSourceImpl.java )
//

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * Message source backed up by a {@link java.util.ResourceBundle}.
 *
 * @deprecated Use {@link groovy.text.MessageSource} instead.
 * 
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class MessageSource
{
    //
    // TODO: We can probably make this a whole lot more Groovy... but for now just leave it as Java fluff
    //
    
    private final ResourceBundle bundle;

    public MessageSource(final String name) {
        assert name != null;

        bundle = ResourceBundle.getBundle(name);
    }

    public MessageSource(final Class type) {
        assert type != null;

        bundle = ResourceBundle.getBundle(type.getName());
    }

    public String getMessage(final String code) {
        return bundle.getString(code);
    }

    public String getMessage(final String code, final Object[] args) {
        String pattern = getMessage(code);
        return MessageFormat.format(pattern, args);
    }

    //
    // TODO: When moved to Java 5 use a Object... args method and the printf-like formatter but for now have these helpers
    //

    /*
    public String getMessage(final String code, final Object... args) {
        String format = getMessage(code);

        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format(format, args);

        return sb.toString();
    }
    */

    public String getMessage(final String code, final Object arg0) {
        return getMessage(code, new Object[] { arg0 });
    }

    public String getMessage(final String code, final Object arg0, final Object arg1) {
        return getMessage(code, new Object[] { arg0, arg1 });
    }

    public String getMessage(final String code, final Object arg0, final Object arg1, final Object arg2) {
        return getMessage(code, new Object[] { arg0, arg1, arg2 });
    }

    public String getMessage(final String code, final Object arg0, final Object arg1, final Object arg2, final Object arg3) {
        return getMessage(code, new Object[] { arg0, arg1, arg2, arg3 });
    }
}