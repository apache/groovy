// Groovy Regex Coach - Copyright 2007 Jeremy Rayner
// inspired by http://weitz.de/regex-coach/
import java.awt.*
import java.awt.event.*
import java.util.regex.*
import javax.swing.*
import javax.swing.text.DefaultHighlighter
import groovy.swing.SwingBuilder

// define the view
def swing = new SwingBuilder()
def gui = swing.frame(title:'The Groovy Regex Coach', location:[20,40], size:[600,500], defaultCloseOperation:WindowConstants.EXIT_ON_CLOSE) {
  panel(layout:new BorderLayout()) {
    splitPane(orientation:JSplitPane.VERTICAL_SPLIT, dividerLocation:150) {
      panel(layout:new BorderLayout()) {
	label(constraints:BorderLayout.NORTH, text:'Regular expression:')
	scrollPane(constraints:BorderLayout.CENTER) {textPane(id:'regexPane')}
      }
      panel(layout:new BorderLayout()) {
	label(constraints:BorderLayout.NORTH, text:'Target string:')
	scrollPane(constraints:BorderLayout.CENTER) {textPane(id:'targetPane')}
	panel(constraints:BorderLayout.SOUTH, layout:new FlowLayout()) {
	  button('<<-', id:'scanLeft')
	  button('->>', id:'scanRight')
	}
      }
    }
  }
}
def highlighter = new RegexHighlighter(swing:swing)
swing.regexPane.addKeyListener(highlighter)
swing.targetPane.addKeyListener(highlighter)
swing.scanLeft.addActionListener(highlighter)
swing.scanRight.addActionListener(highlighter)
gui.show()

class RegexHighlighter extends KeyAdapter implements ActionListener {
  def swing // reference to the view
  int scanIndex // how many times to execute matcher.find()
  def orange = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE)
  def yellow = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW)
  def red = new DefaultHighlighter.DefaultHighlightPainter(Color.RED)

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

  // the main regex logic
  private void doHighlights() {
    try {
      swing.regexPane.highlighter.removeAllHighlights()
      swing.targetPane.highlighter.removeAllHighlights()
      def regex = swing.regexPane.text
      def target = swing.targetPane.text
      def matcher = (target =~ regex) 
      int scan = 0
      while (scan < scanIndex) {
	matcher.find()
	scan++
      }
      if (matcher.find()) {
      	int i = 0
	while (i++ < matcher.groupCount()) {
	  swing.targetPane.highlighter.addHighlight(matcher.start(i), matcher.end(i), orange)
	}
	swing.targetPane.highlighter.addHighlight(matcher.start(), matcher.end(), yellow)
      } else {
	scanIndex = Math.max(scan - 1, 0)
	if (scanIndex > 0) {doHighlights()}
      }
    } catch (PatternSyntaxException e) {
      swing.regexPane.highlighter.addHighlight(e.index, e.index + 2, red)
    }
  }
}
