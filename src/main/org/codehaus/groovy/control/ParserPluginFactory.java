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
package org.codehaus.groovy.control;

import org.codehaus.groovy.antlr.AntlrParserPluginFactory;

/**
 * A factory of parser plugin instances
 *
 * @version $Revision$
 */
public abstract class ParserPluginFactory {
    public static ParserPluginFactory newInstance(boolean useNewParser) {
        if (useNewParser) {
            Class type = null;
            String name = "org.codehaus.groovy.antlr.AntlrParserPluginFactory";
            try {
                type = Class.forName(name);
            }
            catch (ClassNotFoundException e) {
                try {
                    type = ParserPluginFactory.class.getClassLoader().loadClass(name);
                }
                catch (ClassNotFoundException e1) {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    if (contextClassLoader != null) {
                        try {
                            type = contextClassLoader.loadClass(name);
                        }
                        catch (ClassNotFoundException e2) {
                            // ignore
                        }
                    }
                }
            }

            if (type != null) {
                try {
                    return (ParserPluginFactory) type.newInstance();
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not create AntlrParserPluginFactory: " + e, e);
                }
            }
            // can't find Antlr parser, so lets use the Classic one
        }
        return new AntlrParserPluginFactory();
    }

    public abstract ParserPlugin createParserPlugin();
}
