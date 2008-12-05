package groovy.ui.view

import groovy.ui.ConsoleTextEditor
import java.awt.*
import java.awt.image.BufferedImage
import static javax.swing.JSplitPane.VERTICAL_SPLIT
import javax.swing.text.Style
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument

splitPane = splitPane(resizeWeight: 0.50F,
    orientation: VERTICAL_SPLIT)
{
    inputEditor = widget(new ConsoleTextEditor(), border:emptyBorder(0), constraints:BorderLayout.CENTER)
    scrollPane(border:emptyBorder(0)) {
        outputArea = textPane(
            editable: false,
            contentType: "text/html",
            background: new Color(255,255,218),
            font:new Font("Monospaced", Font.PLAIN, 12),
            border:emptyBorder(4)
        )
    }
}


inputArea = inputEditor.textEditor
// attach ctrl-enter to input area
// need to wrap in actions to keep it from being added as a component
actions {
    container(inputArea, font:new Font("Monospaced", Font.PLAIN, 12), border:emptyBorder(4)) {
        action(runAction)
        action(runSelectionAction)
    }
}

// add styles to the output area, shuold this be moved into SwingBuidler somehow?
outputArea.setFont(new Font("Monospaced", outputArea.font.style, outputArea.font.size))
StyledDocument doc = outputArea.styledDocument

Style defStyle = StyleContext.defaultStyleContext.getStyle(StyleContext.DEFAULT_STYLE)

def applyStyle = {Style style, values -> values.each{k, v -> style.addAttribute(k, v)}}

Style regular = doc.addStyle("regular", defStyle)
applyStyle(regular, styles.regular)

promptStyle = doc.addStyle("prompt", regular)
applyStyle(promptStyle, styles.prompt)

commandStyle = doc.addStyle("command", regular)
applyStyle(commandStyle, styles.command)

outputStyle = doc.addStyle("output", regular)
applyStyle(outputStyle, styles.output)

resultStyle = doc.addStyle("result", regular)
applyStyle(resultStyle, styles.result)

stacktraceStyle = doc.addStyle("stacktrace", regular)
applyStyle(stacktraceStyle, styles.stacktrace)

hyperlinkStyle = doc.addStyle("hyperlink", regular)
applyStyle(hyperlinkStyle, styles.hyperlink)

// redo styles for editor
doc = inputArea.getStyledDocument()
StyleContext styleContext = StyleContext.getDefaultStyleContext()
styles.each {styleName, defs ->
    Style style = styleContext.getStyle(styleName)
    if (style) {
        applyStyle(style, defs)
    }
}

// set the preferred size of the input and output areas
// this is a good enough solution, there are margins and scrollbars and such to worry about for 80x12x2
Graphics g = GraphicsEnvironment.localGraphicsEnvironment.createGraphics (new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
FontMetrics fm = g.getFontMetrics(outputArea.font)
outputArea.preferredSize = [
    fm.charWidth(0x77) * 81,
    (fm.getHeight() + fm.leading) * 12] as Dimension

//inputArea.setFont(outputArea.font)
inputEditor.preferredSize = outputArea.preferredSize

