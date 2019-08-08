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
package groovy.ui

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.inspect.swingui.AstBrowser
import groovy.inspect.swingui.ObjectBrowser
import groovy.swing.SwingBuilder
import groovy.transform.CompileStatic
import groovy.transform.ThreadInterrupt
import groovy.ui.text.FindReplaceUtility
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.antlr.LexerFrame
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.messages.ExceptionMessage
import org.codehaus.groovy.control.messages.SimpleMessage
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.runtime.StackTraceUtils
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.codehaus.groovy.syntax.SyntaxException
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation

import javax.swing.*
import javax.swing.event.CaretEvent
import javax.swing.event.CaretListener
import javax.swing.event.DocumentListener
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.filechooser.FileFilter
import javax.swing.text.AttributeSet
import javax.swing.text.Document
import javax.swing.text.Element
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.Style
import javax.swing.text.StyleConstants
import javax.swing.text.html.HTML
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Font
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.util.prefs.Preferences

/**
 * Groovy Swing console.
 *
 * Allows user to interactively enter and execute Groovy.
 */
class Console implements CaretListener, HyperlinkListener, ComponentListener, FocusListener {

    static final String DEFAULT_SCRIPT_NAME_START = 'ConsoleScript'

    static private prefs = Preferences.userNodeForPackage(Console)

    // Whether or not std output should be captured to the console
    static boolean captureStdOut = prefs.getBoolean('captureStdOut', true)
    static boolean captureStdErr = prefs.getBoolean('captureStdErr', true)
    static consoleControllers = []

    boolean fullStackTraces = prefs.getBoolean('fullStackTraces',
        Boolean.valueOf(System.getProperty('groovy.full.stacktrace', 'false')))
    Action fullStackTracesAction

    boolean showScriptInOutput = prefs.getBoolean('showScriptInOutput', true)
    Action showScriptInOutputAction

    boolean visualizeScriptResults = prefs.getBoolean('visualizeScriptResults', false)
    Action visualizeScriptResultsAction

    boolean showToolbar = prefs.getBoolean('showToolbar', true)
    Component toolbar
    Action showToolbarAction

    boolean detachedOutput = prefs.getBoolean('detachedOutput', false)
    Action detachedOutputAction

    boolean orientationVertical = prefs.getBoolean('orientationVertical', true)
    Action orientationVerticalAction
    Action showOutputWindowAction
    Action hideOutputWindowAction1
    Action hideOutputWindowAction2
    Action hideOutputWindowAction3
    Action hideOutputWindowAction4
    int origDividerSize
    Component outputWindow
    Component copyFromComponent
    Component blank
    Component scrollArea

    boolean autoClearOutput = prefs.getBoolean('autoClearOutput', false)
    Action autoClearOutputAction

    // Safer thread interruption
    boolean threadInterrupt = prefs.getBoolean('threadInterrupt', false)
    Action threadInterruptAction

    boolean saveOnRun = prefs.getBoolean('saveOnRun', false)
    Action saveOnRunAction

    boolean indy = prefs.getBoolean('indy', false)
    Action indyAction

    //to allow loading classes dynamically when using @Grab (GROOVY-4877, GROOVY-5871)
    boolean useScriptClassLoaderForScriptExecution = false

    // Maximum size of history
    int maxHistory = 10

    // Maximum number of characters to show on console at any time
    int maxOutputChars = System.getProperty('groovy.console.output.limit','20000') as int

    // File to output stdout & stderr, in addition to console
    PrintWriter outputPrintWriter = null

    // UI
    SwingBuilder swing
    RootPaneContainer frame
    ConsoleTextEditor inputEditor
    JSplitPane splitPane
    JTextPane inputArea
    JTextPane outputArea
    JLabel statusLabel
    JLabel rowNumAndColNum

    // row info
    Element rootElement
    int cursorPos
    int rowNum
    int colNum

    // Styles for output area
    Style promptStyle
    Style commandStyle
    Style outputStyle
    Style stacktraceStyle
    Style hyperlinkStyle
    Style resultStyle

    // Internal history
    List history = []
    int historyIndex = 1 // valid values are 0..history.length()
    HistoryRecord pendingRecord = new HistoryRecord( allText: '', selectionStart: 0, selectionEnd: 0)
    Action prevHistoryAction
    Action nextHistoryAction

    // Current editor state
    boolean dirty
    Action saveAction
    int textSelectionStart  // keep track of selections in inputArea
    int textSelectionEnd
    def scriptFile
    File currentFileChooserDir = new File(Preferences.userNodeForPackage(Console).get('currentFileChooserDir', '.'))
    File currentClasspathJarDir = new File(Preferences.userNodeForPackage(Console).get('currentClasspathJarDir', '.'))
    File currentClasspathDir = new File(Preferences.userNodeForPackage(Console).get('currentClasspathDir', '.'))

    // Running scripts
    CompilerConfiguration baseConfig
    CompilerConfiguration config
    GroovyShell shell
    int scriptNameCounter = 0
    SystemOutputInterceptor systemOutInterceptor
    SystemOutputInterceptor systemErrorInterceptor
    Thread runThread = null
    Closure beforeExecution
    Closure afterExecution

    public static URL ICON_PATH = Console.class.classLoader.getResource('groovy/ui/ConsoleIcon.png') // used by ObjectBrowser and AST Viewer
    public static URL NODE_ICON_PATH = Console.class.classLoader.getResource('groovy/ui/icons/bullet_green.png') // used by AST Viewer

    static groovyFileFilter = new GroovyFileFilter()
    boolean scriptRunning = false
    boolean stackOverFlowError = false
    Action interruptAction

    Action selectWordAction
    Action selectPreviousWordAction

    ConsolePreferences consolePreferences

    static void main(args) {
        MessageSource messages = new MessageSource(Console)
        CliBuilder cli = new CliBuilder(usage: 'groovyConsole [options] [filename]', stopAtNonOption: false,
                header: messages['cli.option.header'])
        cli.with {
            _(names: ['-cp', '-classpath', '--classpath'], messages['cli.option.classpath.description'])
            h(longOpt: 'help', messages['cli.option.help.description'])
            V(longOpt: 'version', messages['cli.option.version.description'])
            pa(longOpt: 'parameters', messages['cli.option.parameters.description'])
            i(longOpt: 'indy', messages['cli.option.indy.description'])
            D(longOpt: 'define', type: Map, argName: 'name=value', messages['cli.option.define.description'])
            _(longOpt: 'configscript', args: 1, messages['cli.option.configscript.description'])
        }
        OptionAccessor options = cli.parse(args)

        if (options == null) {
            // CliBuilder prints error, but does not exit
            System.exit(22) // Invalid Args
        }

        if (options.h) {
            cli.usage()
            System.exit(0)
        }

        if (options.V) {
            System.out.println(messages.format('cli.info.version', GroovySystem.version))
            System.exit(0)
        }

        if (options.hasOption('D')) {
            options.Ds.each { k, v -> System.setProperty(k, v) }
        }

        // full stack trace should not be logged to the output window - GROOVY-4663
        java.util.logging.Logger.getLogger(StackTraceUtils.STACK_LOG_NAME).useParentHandlers = false

        //when starting via main set the look and feel to system
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        def baseConfig = new CompilerConfiguration(System.getProperties())
        String starterConfigScripts = System.getProperty("groovy.starter.configscripts", null)
        if (options.configscript || (starterConfigScripts != null && !starterConfigScripts.isEmpty())) {
            List<String> configScripts = new ArrayList<String>()
            if (options.configscript) {
                configScripts.add(options.configscript)
            }
            if (starterConfigScripts != null) {
                configScripts.addAll(StringGroovyMethods.tokenize((CharSequence) starterConfigScripts, ','))
            }
            GroovyMain.processConfigScripts(configScripts, baseConfig)
        }

        baseConfig.setParameters(options.hasOption("pa"))

        if (options.i) {
            enableIndy(baseConfig)
        }

        def console = new Console(Thread.currentThread().contextClassLoader, new Binding(), baseConfig)
        console.useScriptClassLoaderForScriptExecution = true
        console.run()
        def remaining = options.arguments()
        if (remaining && !remaining[-1].startsWith("-")) {
            console.loadScriptFile(remaining[-1] as File)
        }
    }

    int loadMaxOutputChars() {
        // For backwards compatibility 'maxOutputChars' remains defined in the Console class
        // and the System Property takes precedence as the default value.
        int max = prefs.getInt('maxOutputChars', ConsolePreferences.DEFAULT_MAX_OUTPUT_CHARS)
        return System.getProperty('groovy.console.output.limit', "${max}") as int
    }

    void preferences(EventObject evt = null) {
        if (!consolePreferences) {
            consolePreferences = new ConsolePreferences(this)
        }
        consolePreferences.show()
    }

    void setOutputPreferences(boolean useOutputFile, File outputFile) {
        prefs.remove('outputLogFileName')
        if (!useOutputFile) {
            closeOutputPrintWriter(outputFile)
        } else {
            if (outputFile != null) {
                closeOutputPrintWriter()
                createOutputPrintWriter(outputFile)
                prefs.put('outputLogFileName', outputFile.getAbsolutePath())
            }
        }
    }

    void createOutputPrintWriter(File outputFile) {
        outputPrintWriter = new PrintWriter(new FileOutputStream(
                outputFile,
                true))
    }

    void closeOutputPrintWriter() {
        if (outputPrintWriter != null) {
            outputPrintWriter.close()
            outputPrintWriter = null
        }
    }

    Console(Binding binding = new Binding()) {
        this(null, binding)
    }

    Console(ClassLoader parent, Binding binding = new Binding(), CompilerConfiguration baseConfig = new CompilerConfiguration(System.getProperties())) {
        this.baseConfig = baseConfig
        this.maxOutputChars = loadMaxOutputChars()
        indy = indy || isIndyEnabled(baseConfig)
        if (indy) {
            enableIndy(baseConfig)
        }

        // Set up output file for stdout/stderr, if any
        def outputLogFileName = prefs.get('outputLogFileName', null)
        if (outputLogFileName) {
            createOutputPrintWriter(new File(outputLogFileName))
        }

        newScript(parent, binding)
        try {
            System.setProperty('groovy.full.stacktrace', System.getProperty('groovy.full.stacktrace',
                    Boolean.toString(prefs.getBoolean('fullStackTraces', false))))

        } catch (SecurityException se) {
            fullStackTracesAction.enabled = false
        }
        consoleControllers += this

        // listen for Ivy events if Ivy is on the Classpath
        try {
            if (Class.forName('org.apache.ivy.core.event.IvyListener')) {
                def ivyPluginClass = Class.forName('groovy.ui.ConsoleIvyPlugin')
                ivyPluginClass.newInstance().addListener(this)
            }
        } catch(ClassNotFoundException ignore) { }

        binding.variables._outputTransforms = OutputTransforms.loadOutputTransforms()
    }

    void newScript(ClassLoader parent, Binding binding) {
        config = new CompilerConfiguration(baseConfig)
        config.addCompilationCustomizers(*baseConfig.compilationCustomizers)
        if (threadInterrupt) {
            config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt))
        }
        shell = new GroovyShell(parent, binding, config)
    }

    static frameConsoleDelegates = [
            rootContainerDelegate:{
                frame(
                    title: 'GroovyConsole',
                    //location: [100,100], // in groovy 2.0 use platform default location
                    iconImage: imageIcon('/groovy/ui/ConsoleIcon.png').image,
                    defaultCloseOperation: JFrame.DO_NOTHING_ON_CLOSE,
                ) {
                    try {
                        current.locationByPlatform = true
                    } catch (Exception e) {
                        current.location = [100, 100] // for 1.4 compatibility
                    }
                    containingWindows += current
                }
            },
            menuBarDelegate: {arg->
                current.JMenuBar = build(arg)}
        ]

    void run() {
        run(frameConsoleDelegates)
    }

    void run(JApplet applet) {
        run([
            rootContainerDelegate:{
                containingWindows += SwingUtilities.getRoot(applet.getParent())
                applet
            },
            menuBarDelegate: {arg->
                current.JMenuBar = build(arg)}
        ])
    }

    void run(Map defaults) {

        swing = new SwingBuilder()
        defaults.each{k, v -> swing[k] = v}

        // tweak what the stack traces filter out to be fairly broad
        System.setProperty('groovy.sanitized.stacktraces', '''org.codehaus.groovy.runtime.
                org.codehaus.groovy.
                groovy.lang.
                gjdk.groovy.lang.
                sun.
                java.lang.reflect.
                java.lang.Thread
                groovy.ui.Console''')


        // add controller to the swingBuilder bindings
        swing.controller = this

        // create the actions
        swing.build(ConsoleActions)

        // create the view
        swing.build(ConsoleView)

        bindResults()

        // stitch some actions together
        swing.bind(source:swing.inputEditor.undoAction, sourceProperty:'enabled', target:swing.undoAction, targetProperty:'enabled')
        swing.bind(source:swing.inputEditor.redoAction, sourceProperty:'enabled', target:swing.redoAction, targetProperty:'enabled')

        if (swing.consoleFrame instanceof Window) {
            nativeFullScreenForMac(swing.consoleFrame)
            swing.consoleFrame.pack()
            swing.consoleFrame.show()
        }
        installInterceptor()
        updateTitle() // Title changes based on indy setting
        swing.doLater inputArea.&requestFocus
    }

    /**
     * Make the console frames capable of native fullscreen
     * for Mac OS X Lion and beyond.
     *
     * @param frame the application window
     */
    private void nativeFullScreenForMac(Window frame) {
        if (System.getProperty('os.name').contains('Mac OS X')) {
            new GroovyShell(new Binding([frame: frame])).evaluate('''
                    try {
                        com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(frame, true)
                    } catch (Throwable t) {
                        // simply ignore as full screen capability is not available
                    }
                ''')
        }
    }

    void installInterceptor() {
        systemOutInterceptor = new SystemOutputInterceptor(this.&notifySystemOut, true)
        systemOutInterceptor.start()
        systemErrorInterceptor = new SystemOutputInterceptor(this.&notifySystemErr, false)
        systemErrorInterceptor.start()
    }

    void addToHistory(record) {
        history.add(record)
        // history.size here just retrieves method closure
        if (history.size() > maxHistory) {
            history.remove(0)
        }
        // history.size doesn't work here either
        historyIndex = history.size()
        updateHistoryActions()
    }

    // Ensure we don't have too much in console (takes too much memory)
    private ensureNoDocLengthOverflow(doc) {
        // if it is a case of stackOverFlowError, show the exception details from the front
        // as there is no point in showing the repeating details at the back
        int offset = stackOverFlowError ? maxOutputChars : 0
        if (doc.length > maxOutputChars) {
            doc.remove(offset, doc.length - maxOutputChars)
        }
    }

    // Append a string to the output area
    void appendOutput(String text, AttributeSet style){
        def doc = outputArea.styledDocument
        insertString(doc, doc.length, text, style)
        ensureNoDocLengthOverflow(doc)
    }

    void appendOutput(Window window, AttributeSet style) {
        appendOutput(window.toString(), style)
    }

    void appendOutput(Object object, AttributeSet style) {
        appendOutput(object.toString(), style)
    }

    void appendOutput(Component component, AttributeSet style) {
        SimpleAttributeSet sas = new SimpleAttributeSet()
        sas.addAttribute(StyleConstants.NameAttribute, 'component')
        StyleConstants.setComponent(sas, component)
        appendOutput(component.toString(), sas)
    }

    void appendOutput(Icon icon, AttributeSet style) {
        SimpleAttributeSet sas = new SimpleAttributeSet()
        sas.addAttribute(StyleConstants.NameAttribute, 'icon')
        StyleConstants.setIcon(sas, icon)
        appendOutput(icon.toString(), sas)
    }

    void appendStacktrace(text) {
        def doc = outputArea.styledDocument

        // split lines by new line separator
        def lines = text.split(/(\n|\r|\r\n|\u0085|\u2028|\u2029)/)

        // Java Identifier regex
        def ji = /([\p{Alnum}_\$][\p{Alnum}_\$]*)/

        // stacktrace line regex
        def stacktracePattern = /\tat $ji(\.$ji)+\((($ji(\.(java|groovy))?):(\d+))\)/

        lines.each { line ->
            int initialLength = doc.length

            def matcher = line =~ stacktracePattern
            def fileName =  matcher.matches() ? matcher[0][-5] : ''

            if (fileName == scriptFile?.name || fileName.startsWith(DEFAULT_SCRIPT_NAME_START)) {
                def fileNameAndLineNumber = matcher[0][-6]
                def length = fileNameAndLineNumber.length()
                def index = line.indexOf(fileNameAndLineNumber)

                def style = hyperlinkStyle
                def hrefAttr = new SimpleAttributeSet()
                // don't pass a GString as it won't be coerced to String as addAttribute takes an Object
                hrefAttr.addAttribute(HTML.Attribute.HREF, 'file://' + fileNameAndLineNumber)
                style.addAttribute(HTML.Tag.A, hrefAttr)

                insertString(doc, initialLength,                     line[0..<index],                    stacktraceStyle)
                insertString(doc, initialLength + index,             line[index..<(index + length)],     style)
                insertString(doc, initialLength + index + length,    line[(index + length)..-1] + '\n',  stacktraceStyle)
            } else {
                insertString(doc, initialLength, line + '\n', stacktraceStyle)
            }
        }

        ensureNoDocLengthOverflow(doc)
    }

    void insertString(Document doc, int offset, String text, AttributeSet attributeSet, boolean outputToFile = true) {
        doc.insertString(offset, text, attributeSet)

        // Output to file if activated
        if (outputToFile && outputPrintWriter != null) {
            outputPrintWriter.append(text)
            outputPrintWriter.flush()
        }
    }

    // Append a string to the output area on a new line
    void appendOutputNl(text, style) {
        def doc = outputArea.styledDocument
        def len = doc.length
        def alreadyNewLine = (len == 0 || doc.getText(len - 1, 1) == '\n')
        insertString(doc, doc.length, ' \n', style)
        if (alreadyNewLine) {
            doc.remove(len, 2) // windows hack to fix (improve?) line spacing
        }
        appendOutput(text, style)
    }

    void appendOutputLines(text, style) {
        appendOutput(text, style)
        def doc = outputArea.styledDocument
        def len = doc.length

        // Disable output to log file in this case ('\n' is removed from outputArea next line)
        insertString(doc, len, ' \n', style, false)
        doc.remove(len, 2) // windows hack to fix (improve?) line spacing
    }

    // Return false if use elected to cancel
    boolean askToSaveFile() {
        if (!dirty) {
            return true
        }
        switch (JOptionPane.showConfirmDialog(frame,
            'Save changes' + (scriptFile != null ? " to ${scriptFile.name}" : '') + '?',
            'GroovyConsole', JOptionPane.YES_NO_CANCEL_OPTION))
        {
            case JOptionPane.YES_OPTION:
                return fileSave()
            case JOptionPane.NO_OPTION:
                return true
            default:
                return false
        }
    }

    void beep() {
        Toolkit.defaultToolkit.beep()
    }

    // Binds the '_' and '__' variables in the shell
    void bindResults() {
        shell.setVariable('_', getLastResult()) // lastResult doesn't seem to work
        shell.setVariable('__', history.collect {it.result})
    }

    // Handles menu event
    static void captureStdOut(EventObject evt) {
        captureStdOut = evt.source.selected
        prefs.putBoolean('captureStdOut', captureStdOut)
    }

    static void captureStdErr(EventObject evt) {
        captureStdErr = evt.source.selected
        prefs.putBoolean('captureStdErr', captureStdErr)
    }

    void fullStackTraces(EventObject evt) {
        fullStackTraces = evt.source.selected
        System.setProperty('groovy.full.stacktrace',
            Boolean.toString(fullStackTraces))
        prefs.putBoolean('fullStackTraces', fullStackTraces)
    }

    void showScriptInOutput(EventObject evt) {
        showScriptInOutput = evt.source.selected
        prefs.putBoolean('showScriptInOutput', showScriptInOutput)
    }

    void visualizeScriptResults(EventObject evt) {
        visualizeScriptResults = evt.source.selected
        prefs.putBoolean('visualizeScriptResults', visualizeScriptResults)
    }

    void showToolbar(EventObject evt) {
        showToolbar = evt.source.selected
        prefs.putBoolean('showToolbar', showToolbar)
        toolbar.visible = showToolbar
    }

    void orientationVertical(EventObject evt) {
        def oldValue = orientationVertical
        orientationVertical = evt.source.selected
        prefs.putBoolean('orientationVertical', orientationVertical)
        if (oldValue != orientationVertical) {
            if (orientationVertical) {
                splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT)
            } else {
                splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT)
            }
            splitPane.resizeWeight = detachedOutput ? 1.0 : 0.5
            splitPane.resetToPreferredSizes()
        }
    }

    void detachedOutput(EventObject evt) {
        def oldDetachedOutput = detachedOutput
        detachedOutput = evt.source.selected
        prefs.putBoolean('detachedOutput', detachedOutput)
        if (oldDetachedOutput != detachedOutput) {
            if (detachedOutput) {
                splitPane.add(blank, JSplitPane.BOTTOM)
                origDividerSize = splitPane.dividerSize
                splitPane.dividerSize = 0
                splitPane.resizeWeight = 1.0
                outputWindow.add(scrollArea, BorderLayout.CENTER)
                prepareOutputWindow()
            } else {
                splitPane.add(scrollArea, JSplitPane.BOTTOM)
                splitPane.dividerSize = origDividerSize
                outputWindow.add(blank, BorderLayout.CENTER)
                outputWindow.visible = false
                splitPane.resizeWeight = 0.5
            }
        }
    }

    void autoClearOutput(EventObject evt) {
        autoClearOutput = evt.source.selected
        prefs.putBoolean('autoClearOutput', autoClearOutput)
    }

    void threadInterruption(EventObject evt) {
        threadInterrupt = evt.source.selected
        prefs.putBoolean('threadInterrupt', threadInterrupt)
        def customizers = config.compilationCustomizers.iterator()
        while (customizers.hasNext()) {
            def next = customizers.next()
            if (next instanceof ASTTransformationCustomizer) {
                ASTTransformationCustomizer astCustomizer = next
                if (astCustomizer.transformation instanceof ThreadInterruptibleASTTransformation) {
                    customizers.remove()
                }
            }
        }
        if (threadInterrupt) {
            config.addCompilationCustomizers(new ASTTransformationCustomizer(ThreadInterrupt))
        }
    }

    void caretUpdate(CaretEvent e){
        textSelectionStart = Math.min(e.dot,e.mark)
        textSelectionEnd = Math.max(e.dot,e.mark)
        setRowNumAndColNum()
    }

    void clearOutput(EventObject evt = null) {
        outputArea.text = ''
    }

    // If at exit time, a script is running, the user is given an option to interrupt it first
    def askToInterruptScript() {
        if(!scriptRunning) return true
        def rc = JOptionPane.showConfirmDialog(frame, "Script executing. Press 'OK' to attempt to interrupt it before exiting.",
            'GroovyConsole', JOptionPane.OK_CANCEL_OPTION)
        if (rc == JOptionPane.OK_OPTION) {
            doInterrupt()
            return true
        } else {
            return false
        }
    }

    void doInterrupt(EventObject evt = null) {
        runThread?.interrupt()
    }

    void exitDesktop(EventObject evt = null, quitResponse = null) {
        exit(evt)
        quitResponse.performQuit()
    }

    void exit(EventObject evt = null) {
        if (askToInterruptScript()) {
            if (askToSaveFile()) {
                if (frame instanceof Window) {
                    frame.hide()
                    frame.dispose()
                    outputWindow?.dispose()
                }
                FindReplaceUtility.dispose()
                consoleControllers.remove(this)
                if (!consoleControllers) {
                    systemOutInterceptor.stop()
                    systemErrorInterceptor.stop()
                }
            }
        }
    }

    void fileNewFile(EventObject evt = null) {
        if (askToSaveFile()) {
            scriptFile = null
            setDirty(false)
            inputArea.text = ''
        }
    }

    // Start a new window with a copy of current variables
    void fileNewWindow(EventObject evt = null) {
        Console consoleController = new Console(
            new Binding(
                new HashMap(shell.getContext().variables)))
        consoleController.systemOutInterceptor = systemOutInterceptor
        consoleController.systemErrorInterceptor = systemErrorInterceptor
        SwingBuilder swing = new SwingBuilder()
        consoleController.swing = swing
        frameConsoleDelegates.each {k, v -> swing[k] = v}
        swing.controller = consoleController
        swing.build(ConsoleActions)
        swing.build(ConsoleView)
        installInterceptor()
        nativeFullScreenForMac(swing.consoleFrame)
        swing.consoleFrame.pack()
        swing.consoleFrame.show()
        swing.doLater swing.inputArea.&requestFocus
    }

    void fileOpen(EventObject evt = null) {
        if (askToSaveFile()) {
            def scriptName = selectFilename()
            if (scriptName != null) {
                loadScriptFile(scriptName)
            }
        }
    }

    void loadScriptFile(File file) {
        swing.edt {
            inputArea.editable = false
        }
        swing.doOutside {
            try {
                consoleText = file.readLines().join('\n')
                scriptFile = file
                swing.edt {
                    def listeners = inputArea.document.getListeners(DocumentListener)
                    listeners.each { inputArea.document.removeDocumentListener(it) }
                    updateTitle()
                    inputArea.document.remove 0, inputArea.document.length
                    inputArea.document.insertString 0, consoleText, null
                    listeners.each { inputArea.document.addDocumentListener(it) }
                    setDirty(false)
                    inputArea.caretPosition = 0
                }
            } finally {
                swing.edt { inputArea.editable = true }
                // GROOVY-3684: focus away and then back to inputArea ensures caret blinks
                swing.doLater outputArea.&requestFocusInWindow
                swing.doLater inputArea.&requestFocusInWindow
            }
        }
    }

    // Save file - return false if user cancelled save
    boolean fileSave(EventObject evt = null) {
        if (scriptFile == null) {
            return fileSaveAs(evt)
        }

        scriptFile.write(inputArea.text)
        setDirty(false)
        return true
    }

    // Save file - return false if user cancelled save
    boolean fileSaveAs(EventObject evt = null) {
        scriptFile = selectFilename('Save')
        if (scriptFile != null) {
            scriptFile.write(inputArea.text)
            setDirty(false)
            return true
        } else {
            return false
        }
    }

    def finishException(Throwable t, boolean executing) {
        if(executing) {
            statusLabel.text = 'Execution terminated with exception.'
            history[-1].exception = t
        } else {
            statusLabel.text = 'Compilation failed.'
        }

        if (t instanceof MultipleCompilationErrorsException) {
            MultipleCompilationErrorsException mcee = t
            ErrorCollector collector = mcee.errorCollector
            int count = collector.errorCount
            appendOutputNl("${count} compilation error${count > 1 ? 's' : ''}:\n\n", commandStyle)

            collector.errors.each { error ->
                if (error instanceof SyntaxErrorMessage) {
                    SyntaxException se = error.cause
                    int errorLine = se.line
                    String message = se.originalMessage

                    String scriptFileName = scriptFile?.name ?: DEFAULT_SCRIPT_NAME_START

                    def doc = outputArea.styledDocument

                    def style = hyperlinkStyle
                    def hrefAttr = new SimpleAttributeSet()
                    // don't pass a GString as it won't be coerced to String as addAttribute takes an Object
                    hrefAttr.addAttribute(HTML.Attribute.HREF, 'file://' + scriptFileName + ':' + errorLine)
                    style.addAttribute(HTML.Tag.A, hrefAttr)

                    insertString(doc, doc.length, message + ' at ', stacktraceStyle)
                    insertString(doc, doc.length, "line: ${se.line}, column: ${se.startColumn}\n\n", style)
                } else if (error instanceof Throwable) {
                    reportException(error)
                } else if (error instanceof ExceptionMessage) {
                    reportException(error.cause)
                } else if (error instanceof SimpleMessage) {
                    def doc = outputArea.styledDocument
                    insertString(doc, doc.length, "${error.message}\n", new SimpleAttributeSet())
                }
            }
        } else {
            reportException(t)
        }

        if(!executing) {
            bindResults()
        }

        // GROOVY-4496: set the output window position to the top-left so the exception details are visible from the start
        outputArea.caretPosition = 0

        if (detachedOutput) {
            prepareOutputWindow()
            showOutputWindow()
        }
    }

    private calcPreferredSize(a, b, c) {
        [c, [a, b].min()].max()
    }

    private reportException(Throwable t) {
        appendOutputNl('Exception thrown\n', commandStyle)

        Writer sw = new StringBuilderWriter()
        new PrintWriter(sw).withWriter {pw -> StackTraceUtils.deepSanitize(t).printStackTrace(pw) }
        appendStacktrace("\n${sw.builder}\n")
    }

    def finishNormal(Object result) {
        // Take down the wait/cancel dialog
        history[-1].result = result
        if (result != null) {
            statusLabel.text = 'Execution complete.'
            appendOutputNl('Result: ', promptStyle)
            def obj = (visualizeScriptResults
                ? OutputTransforms.transformResult(result, shell.getContext()._outputTransforms)
                : result.toString())

            // multi-methods are magical!
            appendOutput(obj, resultStyle)
        } else {
            statusLabel.text = 'Execution complete. Result was null.'
        }
        bindResults()
        if (detachedOutput) {
            prepareOutputWindow()
            showOutputWindow()
        }
    }

    def compileFinishNormal() {
        statusLabel.text = 'Compilation complete.'
    }

    private def prepareOutputWindow() {
        outputArea.setPreferredSize(null)
        outputWindow.pack()
        outputArea.setPreferredSize([calcPreferredSize(outputWindow.getWidth(), inputEditor.getWidth(), 120),
                calcPreferredSize(outputWindow.getHeight(), inputEditor.getHeight(), 60)] as Dimension)
        outputWindow.pack()
    }

    // Gets the last, non-null result
    def getLastResult() {
        // runtime bugs in here history.reverse produces odd lookup
        // return history.reverse.find {it != null}
        if (!history) {
            return
        }
        for (i in (history.size() - 1)..0) {
            if (history[i].result != null) {
                return history[i].result
            }
        }
        return null
    }

    void historyNext(EventObject evt = null) {
        if (historyIndex < history.size()) {
            setInputTextFromHistory(historyIndex + 1)
        } else {
            statusLabel.text = "Can't go past end of history (time travel not allowed)"
            beep()
        }
    }

    void historyPrev(EventObject evt = null) {
        if (historyIndex > 0) {
            setInputTextFromHistory(historyIndex - 1)
        } else {
            statusLabel.text = "Can't go past start of history"
            beep()
        }
    }

    void inspectLast(EventObject evt = null){
        if (null == lastResult) {
            JOptionPane.showMessageDialog(frame, 'The last result is null.',
                'Cannot Inspect', JOptionPane.INFORMATION_MESSAGE)
            return
        }
        ObjectBrowser.inspect(lastResult)
    }

    void inspectVariables(EventObject evt = null) {
        ObjectBrowser.inspect(shell.getContext().variables)
    }

    void inspectAst(EventObject evt = null) {
        new AstBrowser(inputArea, rootElement, shell.getClassLoader(), config).run({ inputArea.getText() } )
    }

    void inspectTokens(EventObject evt = null) {
        def lf = LexerFrame.groovyScriptFactory(inputArea.getText())
        lf.visible = true
    }

    void largerFont(EventObject evt = null) {
        updateFontSize(inputArea.font.size + 2)
    }

    static boolean notifySystemOut(int consoleId, String str) {
        if (!captureStdOut) {
            // Output as normal
            return true
        }

        Closure doAppend = {
            Console console = findConsoleById(consoleId)
            if (console) {
                console.appendOutputLines(str, console.outputStyle)
            } else {
                consoleControllers.each {it.appendOutputLines(str, it.outputStyle)}
            }
        }

        // Put onto GUI
        if (EventQueue.isDispatchThread()) {
            doAppend.call()
        }
        else {
            SwingUtilities.invokeLater doAppend
        }
        return false
    }

    static boolean notifySystemErr(int consoleId, String str) {
        if (!captureStdErr) {
            // Output as normal
            return true
        }

        Closure doAppend = {
            Console console = findConsoleById(consoleId)
            if (console) {
                console.appendStacktrace(str)
            } else {
                consoleControllers.each {it.appendStacktrace(str)}
            }
        }

        // Put onto GUI
        if (EventQueue.isDispatchThread()) {
            doAppend.call()
        }
        else {
            SwingUtilities.invokeLater doAppend
        }
        return false
    }

    int getConsoleId() {
        return System.identityHashCode(this)
    }

    private static Console findConsoleById(int consoleId) {
        return consoleControllers.find { it.consoleId == consoleId }
    }

    // actually run the script

    void runScript(EventObject evt = null) {
        if (saveOnRun && scriptFile != null)  {
            if (fileSave(evt)) runScriptImpl(false)
        } else {
            runScriptImpl(false)
        }
    }

    void saveOnRun(EventObject evt = null)  {
        saveOnRun = evt.source.selected
        prefs.putBoolean('saveOnRun', saveOnRun)
    }

    void indy(EventObject evt = null)  {
        indy = evt.source.selected
        prefs.putBoolean('indy', indy)
        if (indy) {
            enableIndy(baseConfig)
        } else {
            disableIndy(baseConfig)
        }
        updateTitle()
        newScript(shell.classLoader, shell.context)
    }

    private static void enableIndy(CompilerConfiguration cc) {
        cc.getOptimizationOptions().put(CompilerConfiguration.INVOKEDYNAMIC, true)
    }

    private static void disableIndy(CompilerConfiguration cc) {
        cc.getOptimizationOptions().remove(CompilerConfiguration.INVOKEDYNAMIC)
    }

    private static boolean isIndyEnabled(CompilerConfiguration cc) {
        cc.getOptimizationOptions().get(CompilerConfiguration.INVOKEDYNAMIC)
    }

    void runSelectedScript(EventObject evt = null) {
        runScriptImpl(true)
    }

    void addClasspathJar(EventObject evt = null) {
        def fc = new JFileChooser(currentClasspathJarDir)
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        fc.multiSelectionEnabled = true
        fc.acceptAllFileFilterUsed = true
        if (fc.showDialog(frame, 'Add') == JFileChooser.APPROVE_OPTION) {
            currentClasspathJarDir = fc.currentDirectory
            Preferences.userNodeForPackage(Console).put('currentClasspathJarDir', currentClasspathJarDir.path)
            fc.selectedFiles?.each { file ->
                shell.getClassLoader().addURL(file.toURL())
            }
        }
    }

    void addClasspathDir(EventObject evt = null) {
        def fc = new JFileChooser(currentClasspathDir)
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        fc.acceptAllFileFilterUsed = true
        if (fc.showDialog(frame, 'Add') == JFileChooser.APPROVE_OPTION) {
            currentClasspathDir = fc.currentDirectory
            Preferences.userNodeForPackage(Console).put('currentClasspathDir', currentClasspathDir.path)
            shell.getClassLoader().addURL(fc.selectedFile.toURL())
        }
    }

    void listClasspath(EventObject evt = null) {
        List<URL> urls = []

        ClassLoader cl = shell.classLoader
        while(cl instanceof URLClassLoader) {
            cl.getURLs().each { url -> urls << url }
            cl = cl.parent
        }

        boolean isWin = isWindows()
        List data = urls.unique().collect { url -> [name: new File(url.toURI()).name, path: isWin ? url.path.substring(1).replace('/', '\\') : url.path] }
        data.sort { it.name.toLowerCase() }

        JScrollPane scrollPane = swing.scrollPane{
            table {
                tableModel(list : data) {
                    propertyColumn(header: 'Name', propertyName: 'name', editable: false)
                    propertyColumn(header:' Path', propertyName: 'path', editable: false)
                }
            }
        }

        def pane = swing.optionPane()
        pane.message = scrollPane
        def dialog = pane.createDialog(frame, 'Classpath')
        dialog.setSize(800, 600)
        dialog.resizable = true
        dialog.visible = true
    }

    void clearContext(EventObject evt = null) {
        def binding = new Binding()
        newScript(null, binding)
        // reload output transforms
        binding.variables._outputTransforms = OutputTransforms.loadOutputTransforms()
    }

    private void runScriptImpl(boolean selected) {
        if(scriptRunning) {
            statusLabel.text = 'Cannot run script now as a script is already running. Please wait or use "Interrupt Script" option.'
            return
        }
        scriptRunning = true
        interruptAction.enabled = true
        stackOverFlowError = false // reset this flag before running a script
        def endLine = System.getProperty('line.separator')
        def record = new HistoryRecord( allText: inputArea.getText().replaceAll(endLine, '\n'),
            selectionStart: textSelectionStart, selectionEnd: textSelectionEnd)
        addToHistory(record)
        pendingRecord = new HistoryRecord(allText:'', selectionStart:0, selectionEnd:0)

        if (prefs.getBoolean('autoClearOutput', false)) clearOutput()

        // Print the input text
        if (showScriptInOutput) {
            for (line in record.getTextToRun(selected).tokenize('\n')) {
                appendOutputNl('groovy> ', promptStyle)
                appendOutput(line, commandStyle)
            }
            appendOutputNl(' \n', promptStyle)
        }

        // Kick off a new thread to do the evaluation
        // Run in a thread outside of EDT, this method is usually called inside the EDT
        runThread = Thread.start {
            try {
                systemOutInterceptor.setConsoleId(this.getConsoleId())
                SwingUtilities.invokeLater { showExecutingMessage() }
                String name = scriptFile?.name ?: (DEFAULT_SCRIPT_NAME_START + scriptNameCounter++)
                if(beforeExecution) {
                    beforeExecution()
                }
                def result
                if(useScriptClassLoaderForScriptExecution) {
                    ClassLoader savedThreadContextClassLoader = Thread.currentThread().contextClassLoader
                    try {
                        Thread.currentThread().contextClassLoader = shell.classLoader
                        result = shell.run(record.getTextToRun(selected), name, [])
                    }
                    finally {
                        Thread.currentThread().contextClassLoader = savedThreadContextClassLoader
                    }
                }
                else {
                    result = shell.run(record.getTextToRun(selected), name, [])
                }
                if(afterExecution) {
                    afterExecution()
                }
                SwingUtilities.invokeLater { finishNormal(result) }
            } catch (Throwable t) {
                if(t instanceof StackOverflowError) {
                    // set the flag that will be used in printing exception details in output pane
                    stackOverFlowError = true
                    clearOutput()
                }
                SwingUtilities.invokeLater { finishException(t, true) }
            } finally {
                runThread = null
                scriptRunning = false
                interruptAction.enabled = false
                systemOutInterceptor.removeConsoleId()
            }
        }
    }

    void compileScript(EventObject evt = null) {
        if(scriptRunning) {
            statusLabel.text = 'Cannot compile script now as a script is already running. Please wait or use "Interrupt Script" option.'
            return
        }
        stackOverFlowError = false // reset this flag before running a script
        def endLine = System.getProperty('line.separator')
        def record = new HistoryRecord( allText: inputArea.getText().replaceAll(endLine, '\n'),
            selectionStart: textSelectionStart, selectionEnd: textSelectionEnd)

        if (prefs.getBoolean('autoClearOutput', false)) clearOutput()

        // Print the input text
        if (showScriptInOutput) {
            for (line in record.allText.tokenize('\n')) {
                appendOutputNl('groovy> ', promptStyle)
                appendOutput(line, commandStyle)
            }
            appendOutputNl(' \n', promptStyle)
        }

        // Kick off a new thread to do the compilation
        // Run in a thread outside of EDT, this method is usually called inside the EDT
        runThread = Thread.start {
            try {
                SwingUtilities.invokeLater { showCompilingMessage() }
                shell.getClassLoader().parseClass(record.allText)
                SwingUtilities.invokeLater { compileFinishNormal() }
            } catch (Throwable t) {
                SwingUtilities.invokeLater { finishException(t, false) }
            } finally {
                runThread = null
            }
        }
    }

    def selectFilename(name = 'Open') {
        def fc = new JFileChooser(currentFileChooserDir)
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        fc.acceptAllFileFilterUsed = true
        fc.fileFilter = groovyFileFilter
        if(name == 'Save') {
            fc.selectedFile = new File('*.groovy')
        }
        if (fc.showDialog(frame, name) == JFileChooser.APPROVE_OPTION) {
            currentFileChooserDir = fc.currentDirectory
            Preferences.userNodeForPackage(Console).put('currentFileChooserDir', currentFileChooserDir.path)
            return fc.selectedFile
        } else {
            return null
        }
    }

    void setDirty(boolean newDirty) {
        //TODO when @BoundProperty is live, this should be handled via listeners
        dirty = newDirty
        saveAction.enabled = newDirty
        updateTitle()
    }

    private void setInputTextFromHistory(newIndex) {
        def endLine = System.getProperty('line.separator')
        if (historyIndex >= history.size()) {
            pendingRecord = new HistoryRecord( allText: inputArea.getText().replaceAll(endLine, '\n'),
                selectionStart: textSelectionStart, selectionEnd: textSelectionEnd)
        }
        historyIndex = newIndex
        def record
        if (historyIndex < history.size()) {
            record = history[historyIndex]
            statusLabel.text = "command history ${history.size() - historyIndex}"
        } else {
            record = pendingRecord
            statusLabel.text = 'at end of history'
        }
        inputArea.text = record.allText
        inputArea.selectionStart = record.selectionStart
        inputArea.selectionEnd = record.selectionEnd
        setDirty(true) // Should calculate dirty flag properly (hash last saved/read text in each file)
        updateHistoryActions()
    }

    private void updateHistoryActions() {
        nextHistoryAction.enabled = historyIndex < history.size()
        prevHistoryAction.enabled = historyIndex > 0
    }

    // Adds a variable to the binding
    // Useful for adding variables before opening the console
    void setVariable(String name, Object value) {
        shell.getContext().setVariable(name, value)
    }

    void showAbout(EventObject evt = null) {
        def version = GroovySystem.getVersion()
        def pane = swing.optionPane()
         // work around GROOVY-1048
        pane.setMessage('Welcome to the Groovy Console for evaluating Groovy scripts\nVersion ' + version)
        def dialog = pane.createDialog(frame, 'About GroovyConsole')
        dialog.show()
    }

    void find(EventObject evt = null) {
        FindReplaceUtility.showDialog()
    }

    void findNext(EventObject evt = null) {
        FindReplaceUtility.FIND_ACTION.actionPerformed(evt)
    }

    void findPrevious(EventObject evt = null) {
        def reverseEvt = new ActionEvent(
            evt.getSource(), evt.getID(),
            evt.getActionCommand(), evt.getWhen(),
            ActionEvent.SHIFT_MASK) //reverse
        FindReplaceUtility.FIND_ACTION.actionPerformed(reverseEvt)
    }

    void replace(EventObject evt = null) {
        FindReplaceUtility.showDialog(true)
    }

    void comment(EventObject evt = null) {
        def rootElement = inputArea.document.defaultRootElement
        def cursorPos = inputArea.getCaretPosition()
        int startRow = rootElement.getElementIndex(cursorPos)
        int endRow = startRow

        if (inputArea.getSelectedText()) {
            def selectionStart = inputArea.getSelectionStart()
            startRow = rootElement.getElementIndex(selectionStart)
            def selectionEnd = inputArea.getSelectionEnd()
            endRow = rootElement.getElementIndex(selectionEnd)
        }

        // If multiple commented lines intermix with uncommented lines, consider them uncommented
        def allCommented = true
        startRow.upto(endRow) { rowIndex ->
            def rowElement = rootElement.getElement(rowIndex)
            int startOffset = rowElement.getStartOffset()
            int endOffset = rowElement.getEndOffset()
            String rowText = inputArea.document.getText(startOffset, endOffset - startOffset)
            if (rowText.trim().length() < 2 || !rowText.trim().substring(0, 2).equals("//")) {
                allCommented = false
            }
        }

        startRow.upto(endRow) { rowIndex ->
            def rowElement = rootElement.getElement(rowIndex)
            int startOffset = rowElement.getStartOffset()
            int endOffset = rowElement.getEndOffset()
            String rowText = inputArea.document.getText(startOffset, endOffset - startOffset)
            if (allCommented) {
            // Uncomment this line if it is already commented
            int slashOffset = rowText.indexOf("//")
            inputArea.document.remove(slashOffset + startOffset, 2)
            } else {
                // Add comment string in front of this line
                inputArea.document.insertString(startOffset, "//", new SimpleAttributeSet())
            }
        }
    }

    void selectBlock(EventObject evt = null) {
        final int startPos = inputArea.getSelectionStart()
        final int endPos = inputArea.getSelectionEnd()
        final int startRow = rootElement.getElementIndex(startPos)
        final int endRow = rootElement.getElementIndex(endPos)
        final Element rowElement = rootElement.getElement(startRow)
        final int startRowOffset = rowElement.getStartOffset()
        final int endRowOffset = rowElement.getEndOffset()

        // Empty line, nothing to do
        if (startRowOffset == endRowOffset - 1) {
            return
        }

        // Nothing is currently selected so select next chunk unless we are at the end of
        // the line then we select the previous
        if (startPos == endPos && selectWordAction != null && selectPreviousWordAction != null) {
            if (endPos == endRowOffset - 1) {
                selectPreviousWordAction.actionPerformed(evt)
            } else {
                selectWordAction.actionPerformed(evt)
            }
            return
        }

        // Partial selection on a single line but not the entire line or word
        // selection actions are not available so select the entire line
        if (startRow == endRow && (startPos != startRowOffset || (endPos != endRowOffset - 1))) {
            inputArea.setSelectionStart(startRowOffset)
            inputArea.setSelectionEnd(endRowOffset - 1)
            return
        }

        // At this point an entire line or multiple lines are selected so
        // look for a block/paragraph to select
        String rowText = inputArea.document.getText(startRowOffset, endRowOffset - startRowOffset)
        if (!rowText?.trim()) {
            // Selection is empty or all spaces so not part of any block
            return
        }

        // Look up for first empty row
        int startBlockPos = startRowOffset
        for (int i = startRow - 1; i >= 0; i--) {
            Element re = rootElement.getElement(i)
            rowText = inputArea.document.getText(re.getStartOffset(), re.getEndOffset() - re.getStartOffset())
            if (!rowText?.trim()) {
                break
            }
            startBlockPos = re.getStartOffset()
        }

        // Look down for first empty row
        int endBlockPos = endRowOffset
        int totalRows = rootElement.getElementCount()
        for (int i = startRow + 1; i < totalRows; i++) {
            Element re = rootElement.getElement(i)
            rowText = inputArea.document.getText(re.getStartOffset(), re.getEndOffset() - re.getStartOffset())
            if (!rowText?.trim()) {
                break
            }
            endBlockPos = re.getEndOffset()
        }

        inputArea.setSelectionStart(startBlockPos)
        inputArea.setSelectionEnd(endBlockPos)
    }

    void showMessage(String message) {
        statusLabel.text = message
    }

    void showExecutingMessage() {
        statusLabel.text = 'Script executing now. Please wait or use "Interrupt Script" option.'
    }

    void showCompilingMessage() {
        statusLabel.text = 'Script compiling now. Please wait.'
    }

    // Shows the detached 'outputArea' dialog
    void showOutputWindow(EventObject evt = null) {
        if (detachedOutput) {
            outputWindow.setLocationRelativeTo(frame)
            outputWindow.show()
        }
    }

    void hideOutputWindow(EventObject evt = null) {
        if (detachedOutput) {
            outputWindow.visible = false
        }
    }

    void hideAndClearOutputWindow(EventObject evt = null) {
        clearOutput()
        hideOutputWindow()
    }

    void smallerFont(EventObject evt = null){
        updateFontSize(inputArea.font.size - 2)
    }

    void updateTitle() {
        if (frame.title) {
            String title = 'GroovyConsole'
            if (indy) {
                title += ' (Indy)'
            }
            if (scriptFile != null) {
                frame.title = scriptFile.name + (dirty?' * ':'') + ' - ' + title
            } else {
                frame.title = title
            }
        }
    }

    private updateFontSize(newFontSize) {
        if (newFontSize > 40) {
            newFontSize = 40
        } else if (newFontSize < 4) {
            newFontSize = 4
        }

        prefs.putInt('fontSize', newFontSize)

        // don't worry, the fonts won't be changed to this family, the styles will only derive from this
        def newFont = new Font(inputEditor.defaultFamily, Font.PLAIN, newFontSize)
        inputArea.font = newFont
        outputArea.font = newFont
    }

    void invokeTextAction(evt, closure, area = inputArea) {
        def source = evt.getSource()
        if (source != null) {
            closure(area)
        }
    }

    void cut(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.cut() })
    }

    void copy(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.copy() }, copyFromComponent ?: inputArea)
    }

    void paste(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.paste() })
    }

    void selectAll(EventObject evt = null) {
        invokeTextAction(evt, { source -> source.selectAll() }, copyFromComponent ?: inputArea)
    }

    void setRowNumAndColNum() {
        cursorPos = inputArea.getCaretPosition()
        rowNum = rootElement.getElementIndex(cursorPos) + 1

        def rowElement = rootElement.getElement(rowNum - 1)
        colNum = cursorPos - rowElement.getStartOffset() + 1

        rowNumAndColNum.setText("$rowNum:$colNum")
    }

    void print(EventObject evt = null) {
        inputEditor.printAction.actionPerformed(evt)
    }

    void undo(EventObject evt = null) {
        inputEditor.undoAction.actionPerformed(evt)
    }

    void redo(EventObject evt = null) {
        inputEditor.redoAction.actionPerformed(evt)
    }

    void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            // URL of the form: file://myscript.groovy:32
            String url = e.getURL()
            int lineNumber = url[(url.lastIndexOf(':') + 1)..-1].toInteger()

            def editor = inputEditor.textEditor
            def text = editor.text

            int newlineBefore = 0
            int newlineAfter = 0
            int currentLineNumber = 1

            // let's find the previous and next newline surrounding the offending line
            int i = 0
            for (ch in text) {
                if (ch == '\n') {
                    currentLineNumber++
                }
                if (currentLineNumber == lineNumber) {
                    newlineBefore = i
                    def nextNewline = text.indexOf('\n', i + 1)
                    newlineAfter = nextNewline > -1 ? nextNewline : text.length()
                    break
                }
                i++
            }

            // highlight / select the whole line
            editor.setCaretPosition(newlineBefore)
            editor.moveCaretPosition(newlineAfter)
        }
    }

    void componentHidden(ComponentEvent e) { }

    void componentMoved(ComponentEvent e) { }

    void componentResized(ComponentEvent e) {
        def component = e.getComponent()
        if (component == outputArea || component == inputArea) {
            def rect = component.getVisibleRect()
            prefs.putInt("${component.name}Width", rect.getWidth().intValue())
            prefs.putInt("${component.name}Height", rect.getHeight().intValue())
        } else {
            prefs.putInt("${component.name}Width", component.width)
            prefs.putInt("${component.name}Height", component.height)
        }
    }

    void componentShown(ComponentEvent e) { }

    void focusGained(FocusEvent e) {
        // remember component with focus for text-copy functionality
        if (e.component == outputArea || e.component == inputArea) {
            copyFromComponent = e.component
        }
    }

    void focusLost(FocusEvent e) { }

    private static boolean isWindows() {
        return getOsName().startsWith("windows")
    }
    private static String getOsName() {
        return System.getProperty("os.name").toLowerCase()
    }
}

@CompileStatic
class GroovyFileFilter extends FileFilter {
    private static final List GROOVY_SOURCE_EXTENSIONS = ['*.groovy', '*.gvy', '*.gy', '*.gsh', '*.story', '*.gpp', '*.grunit']
    private static final GROOVY_SOURCE_EXT_DESC = GROOVY_SOURCE_EXTENSIONS.join(',')

    boolean accept(File f) {
        if (f.isDirectory()) {
            return true
        }
        GROOVY_SOURCE_EXTENSIONS.find {it == getExtension(f)} ? true : false
    }

    String getDescription() {
        "Groovy Source Files ($GROOVY_SOURCE_EXT_DESC)"
    }

    // for binary compatibility - don't use
    @Deprecated
    static String getExtension(f) {
        assert f instanceof File
        getExtension((File) f)
    }

    static String getExtension(File f) {
        def ext = null
        def s = f.getName()
        def i = s.lastIndexOf('.')
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i).toLowerCase()
        }
        "*$ext"
    }
}
