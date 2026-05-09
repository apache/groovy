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
package org.apache.groovy.groovysh.jline

import groovy.console.ui.Console
import groovy.console.ui.ObjectBrowser
import org.apache.groovy.groovysh.Main
import org.apache.groovy.groovysh.util.ClassUtils
import org.jline.builtins.Completers
import org.jline.builtins.Completers.OptDesc
import org.jline.builtins.Completers.OptionCompleter
import org.jline.builtins.SyntaxHighlighter
import org.jline.console.CmdDesc
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.CommandRegistry
import org.jline.console.Printer
import org.jline.console.ScriptEngine
import org.jline.console.impl.JlineCommandRegistry
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.reader.impl.completer.AggregateCompleter
import org.jline.reader.impl.completer.ArgumentCompleter
import org.jline.reader.impl.completer.NullCompleter
import org.jline.reader.impl.completer.StringsCompleter
import org.jline.terminal.Terminal
import org.jline.terminal.impl.TerminalGraphics
import org.jline.terminal.impl.TerminalGraphicsManager
import org.jline.utils.AttributedString

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import java.awt.Color
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

/**
 * Registers the Groovy-specific commands exposed by the groovysh JLine console.
 */
class GroovyCommands extends JlineCommandRegistry implements CommandRegistry {
    private static final String DEFAULT_NANORC_VALUE = 'gron'
    private final GroovyEngine engine
    private final Printer printer
    private final SyntaxHighlighter highlighter
    private final Map<String, Tuple4<Function, Function, Function, List<String>>> commands = [
        '/inspect'     : new Tuple4<>(this::inspect, this::inspectCompleter, this::inspectCmdDesc, ['display/browse object info on terminal/object browser']),
        '/console'     : new Tuple4<>(this::console, this::defCompleter, this::defCmdDesc, ['launch Groovy console']),
        '/doc'         : new Tuple4<>(this::doc, this::importsCompleter, this::defCmdDesc, ['display documentation']),
        '/grab'        : new Tuple4<>(this::grab, this::grabCompleter, this::grabCmdDesc, ['add maven repository dependencies to classpath']),
        '/img'         : new Tuple4<>(this::img, this::imgCompleter, this::imgCmdDesc, ['display image inline (Sixel/Kitty/iTerm2 terminals)']),
        '/classloader' : new Tuple4<>(this::classLoader, this::classloaderCompleter, this::classLoaderCmdDesc, ['display/manage Groovy classLoader data']),
        '/imports'     : new Tuple4<>(this::importsCommand, this::importsCompleter, this::nameDeleteCmdDesc, ['show/delete import statements']),
        '/vars'        : new Tuple4<>(this::varsCommand, this::varsCompleter, this::nameDeleteCmdDesc, ['show/delete variable declarations']),
        '/reset'       : new Tuple4<>(this::reset, this::defCompleter, this::defCmdDesc, ['clear the buffer']),
        '/load'        : new Tuple4<>(this::load, this::loadCompleter, this::loadCmdDesc, ['load state/a file into the buffer']),
        '/save'        : new Tuple4<>(this::save, this::saveCompleter, this::saveCmdDesc, ['save state/the buffer to a file']),
        '/slurp'       : new Tuple4<>(this::slurpcmd, this::slurpCompleter, this::slurpCmdDesc, ['slurp file or string variable context to object']),
        '/types'       : new Tuple4<>(this::typesCommand, this::typesCompleter, this::nameDeleteCmdDesc, ['show/delete types']),
        '/methods'     : new Tuple4<>(this::methodsCommand, this::methodsCompleter, this::nameDeleteCmdDesc, ['show/delete methods'])
    ]
    private boolean consoleUi
    private boolean ivy
    private Supplier<Path> workDir
    private static Map<String, String> slurpers = [
        'YAML'     : 'groovy.yaml.YamlSlurper',
        'XML'      : 'groovy.xml.XmlParser',
        'TOML'     : 'groovy.toml.TomlSlurper',
    ]

    /**
     * Creates the Groovy-specific command registry for groovysh.
     *
     * @param engine shell execution engine
     * @param workDir supplier for the current working directory
     * @param printer terminal printer
     * @param highlighter syntax highlighter used for previews
     */
    GroovyCommands(GroovyEngine engine, Supplier<Path> workDir, Printer printer, SyntaxHighlighter highlighter) {
        this.engine = engine
        this.printer = printer
        this.workDir = workDir
        this.highlighter = highlighter

        consoleUi = ClassUtils.lookFor('groovy.console.ui.ObjectBrowser')
        if (!consoleUi) {
            commands.remove('/console')
        }

        ivy = ClassUtils.lookFor('org.apache.ivy.util.Message')
        if (ivy) {
            System.setProperty('groovy.grape.report.downloads', 'false')
        } else {
            commands.remove('/grab')
        }

        // /img depends on java.desktop (BufferedImage, ImageIO, Swing fallback).
        // It's "static transitive" in JLine's module descriptor, so check at runtime.
        if (!ClassUtils.lookFor('javax.imageio.ImageIO')) {
            commands.remove('/img')
        }

        def available = commands.collectEntries { name, tuple ->
            [name, new CommandMethods((Function)tuple.v1, tuple.v2)]
        }
        registerCommands(available)
    }

    /**
     * Returns the short help text registered for the named command.
     *
     * @param command command name
     * @return command summary lines
     */
    @Override
    List<String> commandInfo(String command) {
        commands[command].v4
    }

    /**
     * Builds the detailed description for the selected command.
     *
     * @param args command arguments whose first element identifies the command
     * @return command description metadata
     */
    @Override
    CmdDesc commandDescription(List<String> args) {
        String command = args?[0] ?: ''
        commands[command].v3(command)
    }

    /**
     * Returns the display name for this command registry.
     *
     * @return registry name
     */
    String name() {
        'Groovy Commands'
    }

    /**
     * Lists cached grapes or grabs the supplied dependency coordinates.
     *
     * @param input parsed command input
     * @return always {@code null}
     */
    def grab(CommandInput input) {
        if (!input.xargs()) {
            return null
        }
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/grab')) return
        def origDownloads = System.getProperty('groovy.grape.report.downloads')
        try {
            String arg = input.args()[0]
            if (arg == '-l' || arg == '--list') {
                Object resp = engine.execute('groovy.grape.Grape.instance.enumerateGrapes()')
                printer.println([
                    (Printer.VALUE_STYLE)         : engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE),
                    (Printer.INDENTION)           : 4,
                    (Printer.MAX_DEPTH)           : 1,
                    (Printer.SKIP_DEFAULT_OPTIONS): true
                ], resp)
            } else {
                int artifactId = 0
                if (input.args().length == 2) {
                    if (input.args()[0] == '-v' || input.args()[0] == '--verbose') {
                        System.setProperty('groovy.grape.report.downloads', 'true')
                        artifactId = 1
                    } else if (input.args()[1] == '-v' || input.args()[1] == '--verbose') {
                        System.setProperty('groovy.grape.report.downloads', 'true')
                    } else {
                        throw new IllegalArgumentException('Unknown command parameters!')
                    }
                }
                Map<String, String> artifact = [:]
                Object xarg = input.xargs()[artifactId]
                if (xarg instanceof String) {
                    String[] vals = input.args()[artifactId].split(':')
                    if (vals.length != 3) {
                        throw new IllegalArgumentException('Invalid command parameter: ' + input.args()[artifactId])
                    }
                    artifact.put('group', vals[0])
                    artifact.put('module', vals[1])
                    artifact.put('version', vals[2])
                } else if (xarg instanceof Map) {
                    artifact = (Map<String, String>) xarg
                } else {
                    throw new IllegalArgumentException('Unknown command parameter: ' + xarg)
                }
                engine.put('_artifact', artifact)
                engine.execute('groovy.grape.Grape.grab(_artifact)')
            }
        } catch (Exception e) {
            saveException(e)
        } finally {
            System.setProperty('groovy.grape.report.downloads', origDownloads)
        }
        return null
    }

    /**
     * Displays an image inline using JLine's terminal-graphics support
     * (Sixel, Kitty, iTerm2). Falls back to a summary line when the
     * terminal doesn't speak any supported protocol; the {@code --gui}
     * flag opens a Swing window instead.
     *
     * @param input parsed command input
     * @return always {@code null}
     */
    def img(CommandInput input) {
        // No fixed arg-count cap: a fully-specified invocation
        //   /img --width 64 --height 32 --no-preserve-aspect-ratio --gui $img
        // is already 7 tokens. The parse loop below validates every flag and
        // treats the lone non-option token as the positional, which is the
        // useful constraint.
        if (maybePrintHelp(input, '/img')) return
        try {
            Integer width = null
            Integer height = null
            boolean preserveAspect = true
            boolean gui = false
            Object positional = null
            String positionalLabel = null
            for (int i = 0; i < input.args().length; i++) {
                String a = input.args()[i]
                if (a == null) {
                    // JLine puts null into args() when a $var reference resolves
                    // to null — usually because the variable isn't defined yet.
                    throw new IllegalArgumentException(
                            '/img: variable reference resolved to null ' +
                                    '(undefined or not yet assigned) — define it first, e.g. ' +
                                    "'panel = ScatterPlot.of(...).canvas().panel()'")
                }
                if (a == '-w' || a == '--width') {
                    width = Integer.parseInt(requireArgValue(input, ++i, a))
                } else if (a.startsWith('--width=')) {
                    width = Integer.parseInt(a.substring('--width='.length()))
                } else if (a == '--height') {
                    height = Integer.parseInt(requireArgValue(input, ++i, a))
                } else if (a.startsWith('--height=')) {
                    height = Integer.parseInt(a.substring('--height='.length()))
                } else if (a == '-p' || a == '--no-preserve-aspect-ratio') {
                    preserveAspect = false
                } else if (a == '-g' || a == '--gui') {
                    gui = true
                } else if (!a.startsWith('-')) {
                    // Use the resolved value from xargs — for "$myImage" this
                    // is the variable's value (e.g. a BufferedImage); for a
                    // plain string ("foo.png") it's the same string.
                    positional = input.xargs()[i]
                    positionalLabel = a
                }
            }
            if (positional == null) {
                throw new IllegalArgumentException('No image path, URL, or variable provided')
            }
            BufferedImage image = positional instanceof String
                    ? loadImage((String) positional)
                    : coerceToImage(positional, width, height)
            if (image == null) {
                throw new IllegalArgumentException("Not a recognised image: $positionalLabel")
            }
            // For raw-pixel inputs (file/URL/BufferedImage/RenderedImage), --width
            // and --height are terminal-display dimensions (cells). For inputs
            // that *generate* the image from those dims (createBufferedImage /
            // toBufferedImage / JComponent paint), the values are already
            // consumed as source pixels and must NOT also be passed to the
            // terminal opts — otherwise e.g. "--width=600" gets reinterpreted
            // as 600 character cells and the chart renders blank/clipped.
            boolean dimsConsumedByGeneration = !(positional instanceof String
                    || positional instanceof BufferedImage
                    || positional instanceof RenderedImage)
            Terminal terminal = input.terminal()
            if (gui) {
                showInSwing(image, positionalLabel)
            } else if (TerminalGraphicsManager.isGraphicsSupported(terminal)) {
                def opts = new TerminalGraphics.ImageOptions().preserveAspectRatio(preserveAspect)
                if (!dimsConsumedByGeneration) {
                    if (width != null) opts.width(width)
                    if (height != null) opts.height(height)
                }
                TerminalGraphicsManager.displayImage(terminal, image, opts)
                // Reset cursor to column 0 of the next line — Sixel/iTerm2/Kitty
                // protocols typically leave the cursor at the right edge of
                // the image, which would indent the next prompt.
                terminal.writer().println()
                terminal.writer().flush()
            } else {
                // Coerce to String — DefaultPrinter renders unknown Object
                // (including GString) as a field table; we want a plain line.
                String summary = "[image: ${image.width}x${image.height}, $positionalLabel] " +
                        "(this terminal doesn't support inline images; try Kitty/iTerm2/WezTerm, or use --gui)"
                printer.println(summary)
            }
        } catch (Exception e) {
            saveException(e)
        }
        return null
    }

    private static String requireArgValue(CommandInput input, int idx, String flag) {
        // Trailing-flag guard for `--width`/`--height` (and friends): without
        // this, `/img --width $img` reads past the end of args() and surfaces
        // an opaque ArrayIndexOutOfBoundsException via saveException.
        if (idx >= input.args().length) {
            throw new IllegalArgumentException("/img: missing value for $flag")
        }
        input.args()[idx]
    }

    private BufferedImage loadImage(String pathOrUrl) {
        if (pathOrUrl.startsWith('http://') || pathOrUrl.startsWith('https://')) {
            return ImageIO.read(URI.create(pathOrUrl).toURL())
        }
        Path path = workDir.get().resolve(pathOrUrl)
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: $pathOrUrl")
        }
        ImageIO.read(path.toFile())
    }

    /**
     * Converts an arbitrary value into a {@link BufferedImage} for /img.
     * Supports:
     * <ul>
     *   <li>{@code BufferedImage} — used as-is</li>
     *   <li>{@code RenderedImage} — drawn into a fresh {@code BufferedImage}</li>
     *   <li>anything with {@code createBufferedImage(int, int)} (e.g.
     *       {@code org.jfree.chart.JFreeChart}) — duck-typed so groovysh
     *       doesn't take a hard dependency on JFreeChart</li>
     *   <li>anything with {@code toBufferedImage(int, int)} (e.g.
     *       {@code smile.plot.swing.Figure}) — duck-typed for Smile's
     *       parallel naming convention</li>
     *   <li>{@code JComponent} — laid out and painted to a {@code BufferedImage}</li>
     * </ul>
     * Other types throw {@link IllegalArgumentException} with a clear message.
     */
    private BufferedImage coerceToImage(Object obj, Integer width, Integer height) {
        if (obj instanceof BufferedImage) {
            return (BufferedImage) obj
        }
        if (obj instanceof RenderedImage) {
            return renderedToBuffered((RenderedImage) obj)
        }
        // Duck-type: createBufferedImage(int, int) — JFreeChart's signature.
        try {
            return (BufferedImage) obj.createBufferedImage(width ?: 800, height ?: 600)
        } catch (MissingMethodException ignore) {
            // Not a JFreeChart-like — fall through.
        }
        // Duck-type: toBufferedImage(int, int) — Smile Figure's signature.
        try {
            return (BufferedImage) obj.toBufferedImage(width ?: 800, height ?: 600)
        } catch (MissingMethodException ignore) {
            // Not a Smile-Figure-like — fall through.
        }
        if (obj instanceof JComponent) {
            return renderJComponent((JComponent) obj, width, height)
        }
        throw new IllegalArgumentException(
                "/img: don't know how to render ${obj.class.name}; supports " +
                'BufferedImage, RenderedImage, anything with createBufferedImage(int,int) ' +
                'or toBufferedImage(int,int), or JComponent')
    }

    private static BufferedImage renderedToBuffered(RenderedImage src) {
        if (src instanceof BufferedImage) return (BufferedImage) src
        BufferedImage out = new BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_ARGB)
        Graphics2D g = out.createGraphics()
        try {
            g.drawRenderedImage(src, new java.awt.geom.AffineTransform())
        } finally {
            g.dispose()
        }
        out
    }

    private static BufferedImage renderJComponent(JComponent comp, Integer width, Integer height) {
        Dimension preferred = comp.preferredSize
        int w = width ?: (preferred.width > 0 ? preferred.width : 800)
        int h = height ?: (preferred.height > 0 ? preferred.height : 600)
        comp.size = new Dimension(w, h)
        comp.doLayout()
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        Graphics2D g = image.createGraphics()
        try {
            // Most plot panels assume a light background; default JComponent
            // paints transparent pixels which look broken when displayed.
            g.color = Color.WHITE
            g.fillRect(0, 0, w, h)
            comp.paint(g)
        } finally {
            g.dispose()
        }
        image
    }

    private static void showInSwing(BufferedImage image, String title) {
        SwingUtilities.invokeLater {
            JFrame frame = new JFrame(title)
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            frame.add(new JLabel(new ImageIcon(image)))
            frame.pack()
            frame.locationRelativeTo = null
            frame.visible = true
        }
    }

    /**
     * Clears the current buffer.
     *
     * @param input parsed command input
     */
    void reset(CommandInput input) {
        checkArgCount(input, [0, 1])
        if (maybePrintHelp(input, '/reset')) return
        engine.reset()
    }

    /**
     * Saves shared state or the current buffer to disk.
     *
     * @param input parsed command input
     */
    void save(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/save')) return
        if (input.args().length == 0) {
            def out = Main.userStateDirectory.resolve('groovysh.ser')
            out.text = engine.toJson(engine.sharedData)
            return
        }
        boolean overwrite = false
        String arg = input.args()[0]
        if (arg == '-o' || arg == '--overwrite') {
            overwrite = true
            arg = input.args()[1]
        }
        saveFile(engine, workDir.get().resolve(arg).toFile(), overwrite)
    }

    /**
     * Writes the current buffer to the target file.
     *
     * @param engine shell execution engine
     * @param file target file
     * @param overwrite whether existing files may be replaced
     */
    static void saveFile(GroovyEngine engine, File file, boolean overwrite = false) {
        if (!file && overwrite) {
            throw new IllegalArgumentException('File to overwrite not found: ' + file.path)
        }
        if (file && !overwrite) {
            throw new IllegalArgumentException("Can't overwrite existing file: " + file.path)
        }
        file.text = engine.buffer
        println "Saved: " + file.path
    }

    /**
     * Restores saved state or loads a file into the current buffer.
     *
     * @param input parsed command input
     */
    void load(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/load')) return
        if (input.args().length == 0) {
            def ser = Main.userStateDirectory.resolve('groovysh.ser')
            def map = engine.sharedData.variables
            map.clear()
            map.putAll(engine.deserialize(ser.text).variables)
            return
        }
        boolean merge = false
        String arg = input.args()[0]
        if (arg == '-m' || arg == '--merge') {
            merge = true
            arg = input.args()[1]
        }
        loadFile(engine, workDir.get().resolve(arg).toFile(), merge)
    }

    private static final String VAR_CONSOLE_OPTIONS = "CONSOLE_OPTIONS"

    private Map<String, Object> consoleOption(String key) {
        def opts = engine.hasVariable(VAR_CONSOLE_OPTIONS)
            ? (Map<String, Object>) engine.get(VAR_CONSOLE_OPTIONS)
            : new HashMap<>()
        opts[key]
    }

    /**
     * Opens configured documentation for the supplied object or type.
     *
     * @param input parsed command input
     * @return always {@code null}
     */
    def doc(CommandInput input) {
        def usage = new String[]{
            "/doc -  open document on browser",
            "Usage: /doc [OBJECT]",
            "  -? --help                       Displays command help"
        }
        try {
            parseOptions(usage, input.xargs());
            if (input.xargs().length == 0) {
                return null
            }
            if (!Desktop.isDesktopSupported()) {
                throw new IllegalStateException("Desktop is not supported!")
            }
            Map<String, Object> docs
            try {
                docs = consoleOption("docs")
            } catch (Exception e) {
                Exception exception = new IllegalStateException("Bad documents configuration!")
                exception.addSuppressed(e)
                throw exception
            }
            if (docs == null) {
                throw new IllegalStateException("No documents configuration!")
            }
            boolean done = false
            Object arg = input.xargs()[0]
            if (arg instanceof String) {
                def addresses = []
                addresses += docs.get(input.args()[0])
                addresses.each { address ->
                    if (address != null) {
                        done = true
                        if (urlExists(address)) {
                            Desktop.getDesktop().browse(new URI(address))
                        } else {
                            throw new IllegalArgumentException("Document not found: " + address)
                        }
                    }
                }
            }
            if (!done) {
                String name
                if (arg instanceof String && ((String) arg).matches("([a-z]+\\.)+[A-Z][a-zA-Z]+")) {
                    name = (String) arg
                } else {
                    name = arg.getClass().getCanonicalName()
                }
                name = name.replaceAll("\\.", "/") + ".html"
                Object doc = null
                for (Map.Entry<String, Object> entry : docs.entrySet()) {
                    if (name.matches(entry.getKey())) {
                        doc = entry.getValue()
                        break
                    }
                }
                if (doc == null) {
                    throw new IllegalArgumentException("No document configuration for " + name)
                }
                String url = name
                if (doc instanceof Collection) {
                    for (Object o : (Collection<?>) doc) {
                        url = o + name
                        if (urlExists(url)) {
                            Desktop.getDesktop().browse(new URI(url))
                            done = true
                        }
                    }
                } else {
                    url = doc + name
                    if (urlExists(url)) {
                        Desktop.getDesktop().browse(new URI(url))
                        done = true
                    }
                }
                if (!done) {
                    throw new IllegalArgumentException("Document not found: " + url)
                }
            }
        } catch (Exception e) {
            saveException(e)
        }
        return null
    }

    private boolean urlExists(String weburl) {
        try {
            URL url = URI.create(weburl).toURL()
            HttpURLConnection huc = (HttpURLConnection) url.openConnection()
            huc.setRequestMethod("HEAD")
            return huc.getResponseCode() == HttpURLConnection.HTTP_OK
        } catch (Exception ignore) {
            return false
        }
    }

    /**
     * Reads content into {@code _} from a file, URL, or literal value.
     *
     * @param input parsed command input
     * @return the parsed value
     */
    def slurpcmd(CommandInput input) {
        checkArgCount(input, [0, 1, 2, 3, 4])
        if (maybePrintHelp(input, '/slurp')) return
        Charset encoding = StandardCharsets.UTF_8
        String format = null
        def out = null
        def args = input.args()
        int index = 0
        int optionIndex = optionIdx(args, index)
        while (optionIndex > -1) {
            index++
            def option = args[optionIndex]
            def arg = null
            if (option.contains('=')) {
                arg = option.substring(option.indexOf('=') + 1)
                option = option.substring(0, option.indexOf('='))
            } else if (optionIndex + 1 < args.length) {
                arg = args[optionIndex + 1]
                index++
            }
            if (option in ['-e', '--encoding'] && arg) {
                encoding = Charset.forName(arg)
            }
            if (option in ['-f', '--format'] && arg) {
                format = arg.toUpperCase()
            }
            optionIndex = optionIdx(args, index)
        }
        Object arg = args[index]
        if (!(arg instanceof String)) {
            throw new IllegalArgumentException("Invalid parameter type: " + arg.getClass().simpleName)
        }
        try {
//            NamedInputStream nis = getSource(arg, input.xargs(), input.args().toList())
            Path path = null
            try {
                path = workDir.get().resolve(arg)
            } catch(Exception ignore) { }
            Reader source = null
            if (path && Files.exists(path)) {
                source = Files.newBufferedReader(path, encoding)
                if (!format) {
                    def ext = path.extension
                    if (ext.equalsIgnoreCase('json')) {
                        format = 'JSON'
                    } else if (ext.equalsIgnoreCase('yaml') || ext.equalsIgnoreCase('yml')) {
                        format = 'YAML'
                    } else if (ext.equalsIgnoreCase('xml') || ext.equalsIgnoreCase('rdf')) {
                        format = 'XML'
                    } else if (ext.equalsIgnoreCase('groovy')) {
                        format = 'GROOVY'
                    } else if (ext.equalsIgnoreCase('toml')) {
                        format = 'TOML'
                    } else if (ext.equalsIgnoreCase('properties')) {
                        format = 'PROPERTIES'
                    } else if (ext.equalsIgnoreCase('csv')) {
                        format = 'CSV'
                    } else if (ext.equalsIgnoreCase('txt') || ext.equalsIgnoreCase('text')) {
                        format = 'TEXT'
                    }
                }
            }
            if (!source && (arg.startsWith('http://') || arg.startsWith('https://'))) {
                URL url = new URL(arg)
                source = new InputStreamReader(url.openStream(), encoding)
            }
            if (source) {
                if (format == 'TEXT') {
                    out = source.readLines()
                } else if (format in engine.deserializationFormats) {
                    out = engine.deserialize(source.text, format)
                } else if (format in slurpers.keySet()) {
                    def parser = getParser(format, slurpers[format])
                    out = parser.parse(source)
                } else if (format == 'CSV') {
                    out = parseCsv(source)
                } else if (format == 'PROPERTIES') {
                    out = new Properties().tap {load(source) }
                } else {
                    out = engine.deserialize(source.text, 'AUTO')
                }
            } else {
                if (format == 'TEXT') {
                    out = arg.readLines()
                } else if (format in engine.deserializationFormats) {
                    out = engine.deserialize(arg, format)
                } else if (format in slurpers.keySet()) {
                    def parser = getParser(format, slurpers[format])
                    out = parser.parseText(arg)
                } else if (format == 'CSV') {
                    out = parseCsv(new StringReader(arg))
                } else {
                    out = engine.deserialize(arg, 'AUTO')
                }
            }
        } catch (Exception ignore) {
            throw ignore
        }
        engine.put("_", out)
        out
    }

    private static final String GROOVY_CSV_SLURPER = 'groovy.csv.CsvSlurper'
    private static final String COMMONS_CSV_FORMAT = 'org.apache.commons.csv.CSVFormat'

    private parseCsv(Reader source) {
        // Try groovy-csv (CsvSlurper) first, then fall back to Apache Commons CSV
        if (ClassUtils.lookFor(GROOVY_CSV_SLURPER)) {
            def parser = engine.execute("new ${GROOVY_CSV_SLURPER}()")
            return parser.parse(source)
        }
        if (ClassUtils.lookFor(engine, COMMONS_CSV_FORMAT)) {
            def parser = engine.execute("${COMMONS_CSV_FORMAT}.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build()")
            return parser.parse(source).toList()*.toMap()
        }
        throw new IllegalArgumentException("CSV format requires $GROOVY_CSV_SLURPER or $COMMONS_CSV_FORMAT to be available")
    }

    /**
     * Creates the parser implementation backing a named slurper format.
     *
     * @param format user-facing format name
     * @param parserName parser class name
     * @return parser instance
     */
    Object getParser(String format, String parserName) {
        if (!ClassUtils.lookFor(parserName)) {
            throw new IllegalArgumentException("$format format requires $parserName to be available")
        }
        engine.execute("new ${parserName}()")
    }

    /**
     * Replays file contents through the engine, optionally merging with existing state.
     *
     * @param engine shell execution engine
     * @param file source file to load
     * @param merge whether existing state should be preserved
     */
    static void loadFile(GroovyEngine engine, File file, boolean merge = false) {
        if (!file) {
            throw new IllegalArgumentException('File not found: ' + file.path)
        }
        if (!merge) {
            engine.reset()
        }
        def unprocessed = []
        file.readLines().each { line ->
            if (!line.trim()) return
            try {
                unprocessed << line
                engine.execute(unprocessed.join('\n'))
                unprocessed.clear()
            } catch (Exception ignore) {
            }
        }
        println "${merge ? 'Merged' : 'Loaded'}: " + file.path
    }

    private String highlight(Collection<String> lines) {
        highlighter.highlight(lines.join('\n\n')).toAnsi().readLines().collect{' \b' + it }.join('\n')
    }

    private boolean maybePrintHelp(CommandInput input, String name) {
        if (!input.args()) return false
        String arg = input.args()[0]
        if (arg == '-?' || arg == '--help') {
            printer.println(helpDesc(name))
            return true
        }
        return false
    }

    private static boolean maybeRemoveItem(CommandInput input, String name, Map<String, String> aggregate, Closure remove) {
        if (input.args().length == 2) {
            if (input.args()[0] == '-d' || input.args()[0] == '--delete') {
                def toRemove = []
                if (name == '*') {
                    toRemove.addAll(aggregate.keySet())
                } else if (name.endsWith('*')) {
                    String prefix = name[0..-2]
                    toRemove.addAll(aggregate.keySet().findAll { key -> key.startsWith(prefix) })
                } else if (name.startsWith('*')) {
                    String suffix = name[1..-1]
                    toRemove.addAll(aggregate.keySet().findAll { key -> key.endsWith(suffix) })
                } else {
                    toRemove.add(name)
                }
                toRemove.each{ remove(it) }
                return true
            }
        }
        return false
    }

    private static void checkArgCount(CommandInput input, Collection<Integer> nums) {
        if (input.args().length !in nums) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
    }

    /**
     * Lists or removes import statements tracked by the shell.
     *
     * @param input parsed command input
     */
    void importsCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/imports')) return
        if (!input.args()) printer.println(highlight(engine.imports.values()))  //getSnippets(EnumSet.of(SnippetType.IMPORT))))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.imports, engine::removeImport)) return
            String source = engine.imports.get(name)
            if (source) printer.println(highlight([source]))
        }
    }

    /**
     * Lists or removes tracked type declarations.
     *
     * @param input parsed command input
     */
    void typesCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/types')) return
        if (!input.args()) printer.println(highlight(engine.types.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.types, engine::removeType)) return
            String source = engine.types.get(name)
            if (source) printer.println(highlight([source]))
        }
    }

    /**
     * Lists or removes tracked variable declarations.
     *
     * @param input parsed command input
     */
    void varsCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/vars')) return
        if (!input.args()) printer.println(highlight(engine.variables.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.variables, engine::removeVariable)) return
            String source = engine.variables.get(name)
            if (source) printer.println(highlight([source]))
        }
    }

    /**
     * Lists or removes tracked method definitions.
     *
     * @param input parsed command input
     */
    void methodsCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/methods')) return
        if (!input.args()) printer.println(highlight(engine.methods.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.methods, engine::removeMethod)) return
            List<String> sources = engine.methods.entrySet().findAll{ e -> e.key.startsWith("$name(") }*.value
            if (sources) printer.println(highlight(sources))
        }
    }

    /**
     * Launches the Swing Groovy console seeded with the current shell state.
     *
     * @param input parsed command input
     */
    void console(CommandInput input) {
        checkArgCount(input, [0, 1])
        if (maybePrintHelp(input, '/console')) return
        Console c = new Console(engine.sharedData)
        def poller = new javax.swing.Timer(100, null)
        poller.addActionListener({ e ->
            if (c.inputArea != null) {
                poller.stop()
                c.inputArea.text = engine.buffer
            }
        } as ActionListener)
        poller.start()
        c.run()
    }

    /**
     * Displays object details in the terminal or object browser.
     *
     * @param input parsed command input
     * @return always {@code null}
     */
    def inspect(CommandInput input) {
        checkArgCount(input, [1, 2])
        if (maybePrintHelp(input, '/inspect')) return
        int idx = optionIdx(input.args())
        String option = idx < 0 ? '--info' : input.args()[idx]
        int id = 0
        if (idx >= 0) {
            id = idx == 0 ? 1 : 0
        }
        if (input.args().length < id + 1) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
        try {
            Object obj = input.xargs()[id]
            ObjectInspector inspector = new ObjectInspector(obj)
            Object out = null
            Map<String, Object> options = [:]
            if (option == '-m' || option == '--methods') {
                out = inspector.methods()
                options[Printer.COLUMNS] = ObjectInspector.METHOD_COLUMNS
            } else if (option == '-n' || option == '--metaMethods') {
                out = inspector.metaMethods()
                options[Printer.COLUMNS] = ObjectInspector.METHOD_COLUMNS
            } else if (option == '-i' || option == '--info') {
                out = inspector.properties()
                options[Printer.VALUE_STYLE] = engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE)
            } else if (consoleUi && (option == '-g' || option == '--gui')) {
                ObjectBrowser.inspect(obj)
            } else {
                throw new IllegalArgumentException('Unknown option: ' + option)
            }
            options[Printer.SKIP_DEFAULT_OPTIONS] = true
            options[Printer.MAX_DEPTH] = 1
            options[Printer.INDENTION] = 4
            printer.println(options, out)
        } catch (Exception e) {
            saveException(e)
        }
        return null
    }

    private classLoader(CommandInput input) {
        checkArgCount(input, [0, 1])
        String option = '--view'
        String arg = null
        if (input.args().length) {
            String[] args = input.args()
            int idx = optionIdx(args)
            option = idx > -1 ? args[idx] : '--view'
            if (option.contains('=')) {
                arg = option.substring(option.indexOf('=') + 1)
                option = option.substring(0, option.indexOf('='))
            } else if (input.args().length == 2 && idx > -1) {
                arg = idx == 0 ? args[1] : args[0]
            }
        }
        try {
            switch (option) {
                case '-?':
                case '--help':
                    printer.println(helpDesc('/classloader'))
                    break
                case '-v':
                case '--view':
                    printer.println([
                        (Printer.SKIP_DEFAULT_OPTIONS): true,
                        (Printer.VALUE_STYLE)         : engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE),
                        (Printer.INDENTION)           : 4,
                        (Printer.MAX_DEPTH)           : 1,
                        (Printer.COLUMNS)             : ['loadedClasses', 'definedPackages', 'classPath']
                    ], engine.classLoader)
                    break
                case '-d':
                case '--delete':
                    engine.purgeClassCache(arg != null ? arg.replace('*', '.*') : null)
                    break
                case '-a':
                case '--add':
                    File file = arg != null ? new File(arg) : null
                    if (!file) {
                        throw new IllegalArgumentException('Bad or missing argument!')
                    }
                    if (file.isDirectory()) {
                        String separator = FileSystems.default.separator
                        if (separator == '\\' && !arg.contains('\\') && arg.contains('/')) {
                            arg = arg.replace('/', '\\')
                        }
                        if (arg.endsWith(separator)) {
                            separator = ''
                        }
                        PathMatcher matcher = FileSystems.default
                            .getPathMatcher('regex:' + arg.replace('\\', '\\\\').replace('.', '\\.') + separator.replace('\\', '\\\\') + '.*\\.jar')
                        try (Stream<Path> pathStream = Files.walk(Paths.get(arg))) {
                            pathStream
                                .filter(matcher::matches)
                                .map(Path::toString)
                                .forEach(engine.classLoader::addClasspath)
                        }
                    } else {
                        engine.classLoader.addClasspath(arg)
                    }
                    break
            }
        } catch (Exception exp) {
            saveException(exp)
        }
        return null
    }

    private CmdDesc helpDesc(String command) {
        def tuple = commands[command]
        doHelpDesc(command, tuple.v4, tuple.v3(command))
    }

    private CmdDesc grabCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] <group>:<artifact>:<version>"),
            new AttributedString("$name --list")
        ], [], [
            '-? --help'     : doDescription('Displays command help'),
            '-l --list'     : doDescription('List the modules in the cache'),
            '-v --verbose'  : doDescription('Report downloads')
        ])
    }

    private CmdDesc slurpCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] file|variable")
        ], [], [
            '-? --help'               : doDescription('Displays command help'),
            '-e --encoding=ENCODING'  : doDescription('Encoding (default UTF-8)'),
            '-f --format=FORMAT'      : doDescription('Serialization format')
        ])
    }

    private CmdDesc defCmdDesc(String name) {
        new CmdDesc([
            new AttributedString(name),
        ], [], [
            '-? --help'     : doDescription('Displays command help')
        ])
    }

    private CmdDesc nameDeleteCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] [name]"),
        ], [], [
            '-? --help'      : doDescription('Displays command help'),
            '-d --delete'    : doDescription('Delete the named item'),
        ])
    }

    private CmdDesc loadCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] [filename]")
        ], [], [
            '-? --help'     : doDescription('Displays command help'),
            '-m --merge'    : doDescription('Merge into existing buffer')
        ])
    }

    private CmdDesc saveCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] [filename]")
        ], [], [
            '-? --help'       : doDescription('Displays command help'),
            '-o --overwrite'  : doDescription('Overwrite existing file')
        ])
    }

    private CmdDesc imgCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] (FILE | URL | \$VAR)"),
        ], [], [
            '-? --help'                       : doDescription('Displays command help'),
            '-w --width=N'                    : doDescription('Width: terminal cells for raw images, source pixels for charts'),
            '   --height=N'                   : doDescription('Height: terminal cells for raw images, source pixels for charts'),
            '-p --no-preserve-aspect-ratio'   : doDescription("Don't preserve aspect ratio"),
            '-g --gui'                        : doDescription('Open in a Swing window instead of inline')
        ])
    }

    private CmdDesc inspectCmdDesc(String name) {
        def optDescs = [
            '-? --help'        : doDescription('Displays command help'),
            '-i --info'        : doDescription('Object class info'),
            '-m --methods'     : doDescription('List object methods'),
            '-n --metaMethods' : doDescription('List object metaMethods')
        ]
        if (consoleUi) {
            optDescs['-g --gui'] = doDescription('Launch object browser')
        }
        new CmdDesc([
            new AttributedString("$name [OPTIONS] OBJECT"),
        ], [], optDescs)
    }

    private CmdDesc classLoaderCmdDesc(String name) {
        new CmdDesc([
            new AttributedString(name),
        ], [], [
            '-? --help'           : doDescription('Displays command help'),
            '-v --view'           : doDescription('View class loader info'),
            '-d --delete [REGEX]' : doDescription('Delete loaded classes'),
            '-a --add PATH'       : doDescription('Add classpath PATH - a jar file or a directory')
        ])
    }

    private List<AttributedString> doDescription(String description) {
        [new AttributedString(description)]
    }

    private static int optionIdx(String[] args, idx = 0) {
        args.findIndexOf(idx) { it.startsWith('-') }
    }

    private List<String> variables() {
        engine.find(null).keySet().collect{'$' + it }
    }

    private List<OptDesc> compileOptDescs(String command) {
        def tuple = commands[command]
        List<OptDesc> out = []
        for (Map.Entry<String, List<AttributedString>> entry : tuple.v3(command).optsDesc.entrySet() ) {
            String[] option = entry.key.split(/\s+/)
            String desc = entry.value[0].toString()
            if (option.length == 2) {
                out.add(new OptDesc(option[0], option[1], desc))
            } else if (option[0].charAt(1) == '-') {
                out.add(new OptDesc(null, option[0], desc))
            } else {
                out.add(new OptDesc(option[0], null, desc))
            }
        }
        return out
    }

    private List<Completer> classloaderCompleter(String command) {
        List<Completer> argsCompleters = Collections.singletonList(NullCompleter.INSTANCE)
        List<OptDesc> options = [
            new OptDesc('-?', '--help', NullCompleter.INSTANCE),
            new OptDesc('-a', '--add', new Completers.FilesCompleter(new File('.'), '*.jar')),
            new OptDesc('-d', '--delete', NullCompleter.INSTANCE),
            new OptDesc('-v', '--view', NullCompleter.INSTANCE)]
        [new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(argsCompleters, options, 1))]
    }

    private List<Completer> loadCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(new Completers.FilesCompleter(workDir), this::compileOptDescs, 1))]
    }

    private List<Completer> saveCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(new Completers.FilesCompleter(workDir), this::compileOptDescs, 1))]
    }

    private List<Completer> slurpCompleter(String command) {
        for (OptDesc o in compileOptDescs(command)) {
            if (o.shortOption()?.equals('-f')) {
                o.valueCompleter = new StringsCompleter('JSON', 'GROOVY', 'NONE', 'TEXT', 'YAML', 'TOML', 'XML')
                break
            }
        }
        [new ArgumentCompleter(
            NullCompleter.INSTANCE,
            new OptionCompleter(Arrays.asList(new AggregateCompleter(new Completers.FilesCompleter(workDir),
                new VariableReferenceCompleter(engine)), NullCompleter.INSTANCE), this::compileOptDescs, 1))]
    }

    private List<Completer> importsCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.imports.keySet()), NullCompleter.INSTANCE], this::compileOptDescs, 1))]
    }

    private List<Completer> imgCompleter(String command) {
        // Hint common image extensions; users can still tab-complete other files.
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter(new Completers.FilesCompleter(workDir),
                        this::compileOptDescs, 1))]
    }

    private List<Completer> inspectCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter([new StringsCompleter((Supplier) this::variables), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> grabCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter([new MavenCoordinateCompleter(), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> typesCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.types.keySet()), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> varsCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.variables.keySet()), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> methodsCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.methodNames), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> defCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter(NullCompleter.INSTANCE,
                        this::compileOptDescs, 1)).tap{strict = false }]
    }

    // Temporarily mimicking code from ConsoleEngineImpl (remove if a viable extension point to access it emerges)
    private static class VariableReferenceCompleter implements Completer {
        private final ScriptEngine engine

        VariableReferenceCompleter(ScriptEngine engine) {
            this.engine = engine
        }

        @Override
        @SuppressWarnings("unchecked")
        void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
            assert commandLine != null
            assert candidates != null
            String word = commandLine.word()
            try {
                if (!word.contains('.') && !word.contains('}')) {
                    for (String v : engine.find().keySet()) {
                        String c = '${' + v + '}'
                        candidates.add(new Candidate(AttributedString.stripAnsi(c), c, null, null, null, null, false))
                    }
                } else if (word.startsWith('${') && word.contains('}') && word.contains('.')) {
                    String var = word.substring(2, word.indexOf('}'))
                    if (engine.hasVariable(var)) {
                        String curBuf = word.substring(0, word.lastIndexOf('.'))
                        String objStatement = curBuf.replace('${', ' ').replace('}', '')
                        Object obj = curBuf.contains('.') ? engine.execute(objStatement) : engine.get(var)
                        Map<?, ?> map = obj instanceof Map ? (Map<?, ?>) obj : null
                        Set<String> identifiers = new HashSet<>()
                        if (map != null
                            && !map.isEmpty()
                            && map.keySet().iterator().next() instanceof String) {
                            identifiers = (Set<String>) map.keySet()
                        } else if (map == null && obj != null) {
                            identifiers = getClassMethodIdentifiers(obj.getClass())
                        }
                        for (String key : identifiers) {
                            candidates.add(new Candidate(AttributedString.stripAnsi(curBuf + "." + key), key, null, null, null, null, false))
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }

        private static Set<String> getClassMethodIdentifiers(Class<?> clazz) {
            Set<String> out = new HashSet<>()
            do {
                for (Method m : clazz.getMethods()) {
                    if (!m.isSynthetic() && m.getParameterCount() == 0) {
                        String name = m.getName()
                        if (name.matches("get[A-Z].*")) {
                            out.add(convertGetMethod2identifier(name))
                        }
                    }
                }
                clazz = clazz.getSuperclass()
            } while (clazz != null)
            return out
        }

        private static String convertGetMethod2identifier(String name) {
            char[] c = name.substring(3).toCharArray()
            c[0] = Character.toLowerCase(c[0])
            return new String(c)
        }
    }
}
