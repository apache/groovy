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
