/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.ui;

import groovy.lang.GroovyShell;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Base class for console
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class ConsoleSupport {

    Style promptStyle;
    Style commandStyle;
    Style outputStyle;
    private GroovyShell shell;
    private int counter;

    protected void addStylesToDocument(JTextPane outputArea) {
        StyledDocument doc = outputArea.getStyledDocument();

        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        promptStyle = doc.addStyle("prompt", regular);
        StyleConstants.setForeground(promptStyle, Color.BLUE);

        commandStyle = doc.addStyle("command", regular);
        StyleConstants.setForeground(commandStyle, Color.MAGENTA);

        outputStyle = doc.addStyle("output", regular);
        StyleConstants.setBold(outputStyle, true);
        
        System.out.println("promptStyle: " + promptStyle);
        System.out.println("commandStyle: " + commandStyle);
        System.out.println("outputStyle: " + outputStyle);
    }

    public Style getCommandStyle() {
        return commandStyle;
    }

    public Style getOutputStyle() {
        return outputStyle;
    }

    public Style getPromptStyle() {
        return promptStyle;
    }

    protected Object evaluate(String text) {
        System.out.println("Evaluating: " + text);
        
        if (shell == null) {
            shell = new GroovyShell();
        }

        String name = "Script" + counter++;
        try {
            return shell.evaluate(text, name);
        }
        catch (Exception e) {
            handleException(text, e);
            return null;
        }
    }

    protected abstract void handleException(String text, Exception e);
}
