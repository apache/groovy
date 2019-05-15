/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.console.ui;

import groovy.lang.GroovyShell;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;

/**
 * Base class for console
 */
public abstract class ConsoleSupport {

    Style promptStyle;
    Style commandStyle;
    Style outputStyle;
    private GroovyShell shell;
    int counter;

    protected void addStylesToDocument(JTextPane outputArea) {
        StyledDocument doc = outputArea.getStyledDocument();

        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "Monospaced");

        promptStyle = doc.addStyle("prompt", regular);
        StyleConstants.setForeground(promptStyle, Color.BLUE);

        commandStyle = doc.addStyle("command", regular);
        StyleConstants.setForeground(commandStyle, Color.MAGENTA);

        outputStyle = doc.addStyle("output", regular);
        StyleConstants.setBold(outputStyle, true);
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

    public GroovyShell getShell() {
        if (shell == null) {
            shell = new GroovyShell();
        }
        return shell;
    }

    protected Object evaluate(String text) {
        String name = "Script" + counter++;
        try {
            return getShell().evaluate(text, name);
        } catch (Exception e) {
            handleException(text, e);
            return null;
        }
    }

    protected abstract void handleException(String text, Exception e);
}