package groovy.ui.view

import groovy.ui.text.GroovyFilter
import java.awt.Color
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

menuBarClass     = groovy.ui.view.BasicMenuBar
contentPaneClass = groovy.ui.view.BasicContentPane
toolBarClass     = groovy.ui.view.BasicToolBar
statusBarClass   = groovy.ui.view.BasicStatusBar

styles = [
    // output window styles
    regular: [
            (StyleConstants.FontFamily): 'Monospaced',
        ],
    prompt: [
            (StyleConstants.Foreground): new Color(0, 128, 0),
        ],
    command: [
            (StyleConstants.Foreground): Color.BLUE,
        ],
    output: [:],
    result: [
            (StyleConstants.Foreground): Color.BLUE,
            (StyleConstants.Background): Color.YELLOW,
        ],

    // syntax highlighting styles
    (StyleContext.DEFAULT_STYLE) : [
            (StyleConstants.FontFamily): 'Monospaced',
        ],
    (GroovyFilter.COMMENT): [
            (StyleConstants.Foreground): Color.LIGHT_GRAY.darker().darker(),
            (StyleConstants.Italic) : true,
        ],
    (GroovyFilter.QUOTES): [
            (StyleConstants.Foreground): Color.MAGENTA.darker().darker(),
        ],
    (GroovyFilter.SINGLE_QUOTES): [
            (StyleConstants.Foreground): Color.GREEN.darker().darker(),
        ],
    (GroovyFilter.SLASHY_QUOTES): [
            (StyleConstants.Foreground): Color.ORANGE.darker(),
        ],
    (GroovyFilter.DIGIT): [
            (StyleConstants.Foreground): Color.RED.darker(),
        ],
    (GroovyFilter.OPERATION): [
            (StyleConstants.Bold): true,
        ],
    (GroovyFilter.IDENT): [:],
    (GroovyFilter.RESERVED_WORD): [
        (StyleConstants.Bold): true,
        (StyleConstants.Foreground): Color.BLUE.darker().darker(),
    ],
]
