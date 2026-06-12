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
package groovy.console.ui.view

import groovy.console.ui.Console
import groovy.console.ui.ConsoleTextEditor
import groovy.console.ui.ThemeManager
import groovy.console.ui.text.SmartDocumentFilter

import javax.swing.JSplitPane
import javax.swing.WindowConstants
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.GraphicsEnvironment
import java.awt.image.BufferedImage
import java.util.prefs.Preferences

import static javax.swing.JSplitPane.HORIZONTAL_SPLIT
import static javax.swing.JSplitPane.VERTICAL_SPLIT

Preferences prefs = Preferences.userNodeForPackage(Console)
def detachedOutputFlag = prefs.getBoolean('detachedOutput', false)
/** Detached output frame shown when console output is separated from the main window. */
outputWindow = frame(visible: false, defaultCloseOperation: WindowConstants.HIDE_ON_CLOSE) {
    /** Placeholder component swapped in while the output area is detached. */
    blank = glue()
    blank.preferredSize = [0, 0] as Dimension
}
/** Split pane that hosts the editor and the attached output area. */
splitPane = splitPane(resizeWeight: 0.5, orientation:
        prefs.getBoolean('orientationVertical', true) ? VERTICAL_SPLIT : HORIZONTAL_SPLIT) {
    def editor = new ConsoleTextEditor()
    boolean smartHighlighterEnabled = Console.smartHighlighter
    if (smartHighlighterEnabled) {
        editor.enableHighLighter(SmartDocumentFilter)
    }
    /** Editor wrapper used for script input. */
    inputEditor = widget(editor, border: emptyBorder(0))
    buildOutputArea(prefs)
}

private def buildOutputArea(prefs) {
    /** Scroll pane that contains the console output component. */
    scrollArea = scrollPane(border: emptyBorder(0)) {
        /** Read-only text pane used to render console output. */
        outputArea = textPane(
                editable: false,
                name: 'outputArea',
                contentType: 'text/html',
                background: ThemeManager.outputBackground,
                font: new Font('Monospaced', Font.PLAIN, prefs.getInt('fontSize', 12)),
                border: emptyBorder(4)
        )
    }
}


/** Text component backing the script input area. */
inputArea = inputEditor.textEditor
inputArea.background = ThemeManager.inputBackground
// attach ctrl-enter to input area
// need to wrap in actions to keep it from being added as a component
actions {
    container(inputArea, name: 'inputArea', font: new Font('Monospaced', Font.PLAIN, prefs.getInt('fontSize', 12)), border: emptyBorder(4)) {
        action(runAction)
        action(runSelectionAction)
        action(showOutputWindowAction)
    }
    container(outputArea, name: 'outputArea') {
        action(hideOutputWindowAction1)
        action(hideOutputWindowAction2)
        action(hideOutputWindowAction3)
        action(hideOutputWindowAction4)
    }
}

// add styles to the output area, should this be moved into SwingBuilder somehow?
outputArea.font = new Font('Monospaced', outputArea.font.style, outputArea.font.size)
StyledDocument doc = outputArea.styledDocument

Style defStyle = StyleContext.defaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE)

def applyStyle = { Style style, values -> values.each { k, v -> style.addAttribute(k, v) } }

Style regular = doc.addStyle('regular', defStyle)
applyStyle(regular, styles.regular)

/** Style used for console prompts. */
promptStyle = doc.addStyle('prompt', regular)
applyStyle(promptStyle, styles.prompt)

/** Style used for echoed commands. */
commandStyle = doc.addStyle('command', regular)
applyStyle(commandStyle, styles.command)

/** Style used for standard output. */
outputStyle = doc.addStyle('output', regular)
applyStyle(outputStyle, styles.output)

/** Style used for evaluation results. */
resultStyle = doc.addStyle('result', regular)
applyStyle(resultStyle, styles.result)

/** Style used for stack traces. */
stacktraceStyle = doc.addStyle('stacktrace', regular)
applyStyle(stacktraceStyle, styles.stacktrace)

/** Style used for hyperlinks in the output pane. */
hyperlinkStyle = doc.addStyle('hyperlink', regular)
applyStyle(hyperlinkStyle, styles.hyperlink)

// seed FontSize on every output style so initial output renders at the user's
// preferred size (the component Font is largely ignored by HTMLEditorKit).
// HTMLEditorKit scales up pt→px by 96/72 (~1.33x) so scale down to match input.
int initialFontSize = Console.outputFontSizeFor(prefs.getInt('fontSize', 12))
[regular, promptStyle, commandStyle, outputStyle, resultStyle, stacktraceStyle, hyperlinkStyle].each {
    StyleConstants.setFontSize(it, initialFontSize)
}

// redo styles for editor
doc = inputArea.styledDocument
StyleContext styleContext = StyleContext.defaultStyleContext
styles.each { styleName, defs ->
    Style style = styleContext.getStyle(styleName)
    if (style) {
        applyStyle(style, defs)
        String family = defs[StyleConstants.FontFamily]
        if (style.name == 'default' && family) {
            inputEditor.defaultFamily = family
            inputArea.font = new Font(family, Font.PLAIN, inputArea.font.size)
        }
    }
}

// set the preferred size of the input and output areas
// this is a good enough solution, there are margins and scrollbars and such to worry about for 80x12x2
Graphics g = GraphicsEnvironment.localGraphicsEnvironment.createGraphics(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
FontMetrics fm = g.getFontMetrics(outputArea.font)

outputArea.preferredSize = [
        prefs.getInt('outputAreaWidth', fm.charWidth(0x77) * 81),
        prefs.getInt('outputAreaHeight', (fm.getHeight() + fm.getLeading()) * 12)
] as Dimension

inputEditor.preferredSize = [
        prefs.getInt('inputAreaWidth', fm.charWidth(0x77) * 81),
        prefs.getInt('inputAreaHeight', (fm.getHeight() + fm.getLeading()) * 12)
] as Dimension

/** Original split-pane divider size restored when output is reattached. */
origDividerSize = -1
if (detachedOutputFlag) {
    splitPane.add(blank, JSplitPane.BOTTOM)
    origDividerSize = splitPane.dividerSize
    splitPane.dividerSize = 0
    splitPane.resizeWeight = 1.0
    outputWindow.add(scrollArea, BorderLayout.CENTER)
}
