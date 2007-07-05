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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Pure Java prompt using just System.in.
 */
public class JavaPrompt implements Prompt
{
    private String prompt;
    private BufferedReader input;
    private final PrintStream out;
    private final PrintStream err;

    public JavaPrompt(InputStream in, PrintStream out, PrintStream err)
    {
        this.out = out;
        this.err = err;
        this.input = new BufferedReader(new InputStreamReader(in));
    }

    public JavaPrompt()
    {
        this(System.in, System.out, System.err);
    }

    public String readLine() throws IOException
    {
        out.print(prompt);
        out.flush();
        return input.readLine();
    }

    public String getPrompt()
    {
        return prompt;
    }

    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public void setCompleter(Completer completer)
    {
        // completer not supported
    }

    public void close()
    {
        try
        {
            input.close();
        }
        catch (IOException e)
        {
            e.printStackTrace(err);
        }
    }
}
