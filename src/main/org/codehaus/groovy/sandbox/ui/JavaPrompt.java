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
