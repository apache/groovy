/**
 *
 * Copyright 2004 James Strachan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.control;



/**
 * A factory of parser plugin instances
 *
 * @version $Revision$
 */
public abstract class ParserPluginFactory {
    protected static final boolean useNewParser = true;

    public static ParserPluginFactory newInstance() {
        if (useNewParser) {
            try {
                Class type = Class.forName("org.codehaus.groovy.antlr.AntlrParserPluginFactory");
                try {
                    return (ParserPluginFactory) type.newInstance();
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not create AntlrParserPluginFactory: " + e, e);
                }
            }
            catch (ClassNotFoundException e) {
                // ignore
            }
            // can't find Antlr parser, so lets use the Classic one
        }
        return new ClassicParserPluginFactory();
    }

    public abstract ParserPlugin createParserPlugin();
}
