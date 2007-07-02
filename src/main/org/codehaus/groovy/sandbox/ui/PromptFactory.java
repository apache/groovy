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

package org.codehaus.groovy.sandbox.ui;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Factory to build a command line prompt.  Should build the most featureful
 * prompt available.
 * <p/>
 * Currently readline prompt will be looked up dynamically, and defaults to
 * normal System.in prompt.
 */
public class PromptFactory
{
    public static Prompt buildPrompt(InputStream in, PrintStream out, PrintStream err)
    {
        try
        {
            return (Prompt) Class.forName("org.codehaus.groovy.sandbox.ui.ReadlinePrompt").newInstance();
        }
        catch (ClassNotFoundException e)
        {
            return new JavaPrompt(in, out, err);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new JavaPrompt(in, out, err);
        }
    }
}
