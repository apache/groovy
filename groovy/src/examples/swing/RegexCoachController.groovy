// Groovy Regex Coach - Copyright 2007 Jeremy Rayner

import groovy.swing.SwingBuilder
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.regex.PatternSyntaxException
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter

// inspired by http://weitz.de/regex-coach/

// define the view
def swing = new SwingBuilder()

def gui = swing.build(RegexCoachView)

def highlighter = new RegexHighliter(swing: swing)
swing.regexPane.addKeyListener(highlighter)
swing.targetPane.addKeyListener(highlighter)
swing.scanLeft.addActionListener(highlighter)
swing.scanRight.addActionListener(highlighter)
gui.show()

class RegexHighliter extends KeyAdapter implements ActionListener {
    def swing // reference to the view
    int scanIndex // how many times to execute matcher.find()
    def orange = new DefaultHighlightPainter(Color.ORANGE)
    def yellow = new DefaultHighlightPainter(Color.YELLOW)
    def red = new DefaultHighlightPainter(Color.RED)

    // react to user actions
    public void actionPerformed(ActionEvent event) {
        if (event.actionCommand == '<<-') {scanIndex = Math.max(scanIndex - 1, 0)}
        if (event.actionCommand == '->>') {scanIndex++}
        doHighlights()
    }
    public void keyReleased(KeyEvent event) {
        scanIndex = 0
        doHighlights()
    }

    private resetView() {
        swing.regexPane.highlighter.removeAllHighlights()
        swing.targetPane.highlighter.removeAllHighlights()
        swing.regexStatus.text = ' '
        swing.targetStatus.text = ' '
    }

    // the main regex logic
    private doHighlights() {
        try {
            resetView()
            // note: get the text from the underlying document,
            // otherwise carriage return/line feeds different when using the JTextPane text
            def regex = swing.regexPane.document.getText(0, swing.regexPane.document.length)
            def target = swing.targetPane.document.getText(0, swing.targetPane.document.length)

            def matcher = (target =~ regex)

            // scan past the matches before the match we want
            int scan = 0
            while (scan < scanIndex) {
                matcher.find()
                scan++
            }
            if (matcher.find()) {
                // highlight any captured groups
                int i = 0
                while (i++ < matcher.groupCount()) {
                    swing.targetPane.highlighter.addHighlight(matcher.start(i), matcher.end(i), orange)
                }
                // highlight whole match
                swing.targetPane.highlighter.addHighlight(matcher.start(), matcher.end(), yellow)
                if (regex.length() != 0) {
                    swing.targetStatus.text = "Match #${scanIndex + 1} from ${matcher.start()} to ${matcher.end()}."
                }
            } else {// not found
                scanIndex = Math.max(scan - 1, 0)
                if (scanIndex > 0) {doHighlights()}
                swing.targetStatus.text = "No match."
            }
        } catch (PatternSyntaxException e) {
            swing.regexPane.highlighter.addHighlight(e.index, e.index + 2, red)
            swing.regexStatus.text = e.description
        }
    }
}
