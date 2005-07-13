package groovy.text;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.util.IndentPrinter;
import groovy.util.Node;
import groovy.util.XmlNodePrinter;
import groovy.util.XmlParser;
import groovy.xml.QName;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.xml.sax.SAXException;

/**
 * Template engine for xml data input.
 *
 * @author Christian Stein
 */
public class XmlTemplateEngine extends TemplateEngine {

    private static class GspPrinter extends XmlNodePrinter {

        public GspPrinter(PrintWriter out, String indent) {
            this(new IndentPrinter(out, indent));
        }

        public GspPrinter(IndentPrinter out) {
            super(out, "\\\"");
        }

        protected void printGroovyTag(String tag, String text) {
            if (tag.equals("scriptlet")) {
                out.print(text);
                out.print("\n");
                return;
            }
            if (tag.equals("expression")) {
                printLineBegin();
                out.print("${");
                out.print(text);
                out.print("}");
                printLineEnd();
                return;
            }
            throw new RuntimeException("Unsupported tag named \"" + tag + "\".");
        }

        protected void printLineBegin() {
            out.print("out.print(\"");
            out.printIndent();
        }

        protected void printLineEnd(String comment) {
            out.print("\\n\");");
            if (comment != null) {
                out.print(" // ");
                out.print(comment);
            }
            out.print("\n");
        }

        protected boolean printSpecialNode(Node node) {
            Object name = node.name();
            if (name != null && name instanceof QName) {
                /*
                 * FIXME Somethings wrong with the SAX- or XMLParser. Prefix should only contain 'gsp'?!
                 */
                String s = ((QName) name).getPrefix();
                if (s.startsWith("gsp:")) {
                    s = s.substring(4); // 4 = "gsp:".length()
                    if (s.length() == 0) {
                        throw new RuntimeException("No local part after 'gsp:' given in node " + node);
                    }
                    printGroovyTag(s, node.text());
                    return true;
                }
            }
            return false;
        }

    }

    private static class XmlTemplate implements Template {

        private final Script script;

        public XmlTemplate(Script script) {
            this.script = script;
        }

        public Writable make() {
            return make(new HashMap());
        }

        public Writable make(Map map) {
            if (map == null) {
                throw new IllegalArgumentException("map must not be null");
            }
            return new XmlWritable(script, new Binding(map));
        }

    }

    private static class XmlWritable implements Writable {

        private final Binding binding;
        private final Script script;
        private WeakReference result;

        public XmlWritable(Script script, Binding binding) {
            this.script = script;
            this.binding = binding;
            this.result = new WeakReference(null);
        }

        public Writer writeTo(Writer out) {
            Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
            PrintWriter pw = new PrintWriter(out);
            scriptObject.setProperty("out", pw);
            scriptObject.run();
            pw.flush();
            return out;
        }

        public String toString() {
            if (result.get() != null) {
                return result.get().toString();
            }
            String string = writeTo(new StringWriter(1024)).toString();
            result = new WeakReference(string);
            return string;
        }

    }

    public static final String DEFAULT_INDENTION = "  ";

    private final GroovyShell groovyShell;
    private final XmlParser xmlParser;
    private String indention;

    public XmlTemplateEngine() throws SAXException, ParserConfigurationException {
        this(DEFAULT_INDENTION, false);
    }

    public XmlTemplateEngine(String indention, boolean validating) throws SAXException, ParserConfigurationException {
        this(new XmlParser(validating, true), new GroovyShell(), indention);
    }

    public XmlTemplateEngine(XmlParser xmlParser, GroovyShell groovyShell, String indention) {
        this.groovyShell = groovyShell;
        this.xmlParser = xmlParser;
        this.indention = indention;
    }

    public Template createTemplate(Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        Node root = null;
        try {
            root = xmlParser.parse(reader);
        } catch (SAXException e) {
            throw new RuntimeException("Parsing XML source failed.", e);
        }

        if (root == null) {
            throw new IOException("Parsing XML source failed: root node is null.");
        }

        // new NodePrinter().print(root);
        // new XmlNodePrinter().print(root);

        StringWriter writer = new StringWriter(1024);
        writer.write("/* Generated by XmlTemplateEngine */\n");
        new GspPrinter(new PrintWriter(writer), DEFAULT_INDENTION).print(root);
        String scriptText = writer.toString();

        // System.err.println("\n-\n" + scriptText + "\n-\n");

        Script script = groovyShell.parse(scriptText);
        Template template = new XmlTemplate(script);
        return template;
    }

    public String getIndention() {
        return indention;
    }

    public void setIndention(String indention) {
        if (indention == null) {
            indention = DEFAULT_INDENTION;
        }
        this.indention = indention;
    }

    public String toString() {
        return "XmlTemplateEngine";
    }

}
