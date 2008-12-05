package groovy.ui.view

import groovy.ui.text.GroovyFilter
import java.awt.Color
import javax.swing.text.StyleConstants

build(Defaults)

// menu bar tweaks
System.setProperty("apple.laf.useScreenMenuBar", "true")
System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GroovyConsole")

// redo output styles
styles = [
    // output window styles
    regular: [
            (StyleConstants.FontFamily): "Monaco"
        ],
    prompt: [
            (StyleConstants.Foreground): Color.LIGHT_GRAY
        ],
    command: [
            (StyleConstants.Foreground): Color.GRAY
        ],
    stacktrace: [
            (StyleConstants.Foreground): Color.RED.darker()
        ],
    hyperlink: [
            (StyleConstants.Foreground): Color.BLUE,
            (StyleConstants.Underline): true
        ],
    output: [:],
    result: [
            (StyleConstants.Foreground): Color.WHITE,
            (StyleConstants.Background): Color.BLACK
        ],

    // syntax highlighting styles
    (GroovyFilter.COMMENT): [
            (StyleConstants.Foreground): Color.LIGHT_GRAY.darker().darker(),
            (StyleConstants.Italic) : true
        ],
    (GroovyFilter.QUOTES): [
            (StyleConstants.Foreground): Color.MAGENTA.darker().darker()
        ],
    (GroovyFilter.SINGLE_QUOTES): [
            (StyleConstants.Foreground): Color.GREEN.darker().darker()
        ],
    (GroovyFilter.SLASHY_QUOTES): [
            (StyleConstants.Foreground): Color.ORANGE.darker()
        ],
    (GroovyFilter.DIGIT): [
            (StyleConstants.Foreground): Color.RED.darker()
        ],
    (GroovyFilter.OPERATION): [
            (StyleConstants.Bold): true
        ],
    (GroovyFilter.IDENT): [:],
    (GroovyFilter.RESERVED_WORD): [
        (StyleConstants.Bold): true,
        (StyleConstants.Foreground): Color.BLUE.darker().darker()
    ]
]

menuBarClass = MacOSXMenuBar