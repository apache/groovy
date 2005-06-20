package groovy.text;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.util.IndentPrinter;
import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.QName;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    private static class XmlPrinter {

        protected final IndentPrinter out;

        public XmlPrinter() {
            this(new IndentPrinter(new PrintWriter(new OutputStreamWriter(System.out))));
        }

        public XmlPrinter(PrintWriter out, String indent) {
            this(new IndentPrinter(out, indent));
        }

        public XmlPrinter(IndentPrinter out) {
            if (out == null) {
                throw new IllegalArgumentException("Argument 'IndentPrinter out' must not be null!");
            }
            this.out = out;
        }

        public String getNameOfNode(Node node) {
            if (node == null) {
                throw new IllegalArgumentException("Node must not be null!");
            }
            Object name = node.name();
            if (name instanceof QName) {
                QName qname = (QName) name;
                return /*qname.getPrefix() + ":" +*/qname.getLocalPart();
            }
            return name.toString();
        }

        public boolean isEmptyElement(Node node) {
            if (node == null) {
                throw new IllegalArgumentException("Node must not be null!");
            }
            if (!node.children().isEmpty()) {
                return false;
            }
            String text = node.text();
            if (text.length() > 0) {
                return false;
            }
            return true;
        }

        public void print(Node node) {
            /*
             * Handle empty elements like '<br/>', '<img/> or '<hr noshade="noshade"/>.
             */
            if (isEmptyElement(node)) {
                printLineBegin();
                out.print("<");
                out.print(getNameOfNode(node));
                printNameAttributes(node.attributes());
                out.print("/>");
                printLineEnd("empty element"); // "node named '" + node.name() + "'"
                return;
            }

            /*
             * Handle GSP tag element!
             */
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
                    return;
                }
            }

            /*
             * Handle normal element like <html> ... </html>.
             */
            Object value = node.value();
            if (value instanceof List) {
                printName(node, true);
                printList((List) value);
                printName(node, false);
                return;
            }

            /*
             * Still here?!
             */
            throw new RuntimeException("Unsupported node value: " + node.value());
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

        protected void printLineEnd() {
            printLineEnd(null);
        }

        protected void printLineEnd(String comment) {
            out.print("\\n\");");
            if (comment != null) {
                out.print(" // ");
                out.print(comment);
            }
            out.print("\n");
        }

        protected void printList(List list) {
            out.incrementIndent();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object value = iter.next();
                /*
                 * If the current value is a node, recurse into that node.
                 */
                if (value instanceof Node) {
                    print((Node) value);
                    continue;
                }
                /*
                 * Print out "simple" text nodes.
                 */
                printLineBegin();
                out.print(InvokerHelper.toString(value));
                printLineEnd();
            }
            out.decrementIndent();
        }

        protected void printName(Node node, boolean begin) {
            if (node == null) {
                throw new NullPointerException("Node must not be null.");
            }
            Object name = node.name();
            if (name == null) {
                throw new NullPointerException("Name must not be null.");
            }
            printLineBegin();
            out.print("<");
            if (!begin) {
                out.print("/");
            }
            out.print(getNameOfNode(node));
            if (begin) {
                printNameAttributes(node.attributes());
            }
            out.print(">");
            printLineEnd();
        }

        protected void printNameAttributes(Map attributes) {
            if (attributes == null || attributes.isEmpty()) {
                return;
            }
            out.print(" ");
            boolean first = true;
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if (first) {
                    first = false;
                } else {
                    out.print(" ");
                }
                out.print(entry.getKey().toString());
                out.print("=");
                Object value = entry.getValue();
                if (value instanceof String) {
                    out.print("\\\"");
                    out.print((String) value);
                    out.print("\\\"");
                    continue;
                }
                out.print(InvokerHelper.toString(value));
            }
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

    public XmlTemplateEngine() throws SAXException, ParserConfigurationException {
        this(new GroovyShell(), new XmlParser(false, true));
    }

    public XmlTemplateEngine(GroovyShell groovyShell, XmlParser xmlParser) {
        this.groovyShell = groovyShell;
        this.xmlParser = xmlParser;
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

        StringWriter writer = new StringWriter(1024);
        writer.write("/* Generated by XmlTemplateEngine */\n");
        new XmlPrinter(new PrintWriter(writer), DEFAULT_INDENTION).print(root);
        String scriptText = writer.toString();

        // System.err.println("\n-\n" + scriptText + "\n-\n");

        Script script = groovyShell.parse(scriptText);
        Template template = new XmlTemplate(script);
        return template;
    }

    public String toString() {
        return "XmlTemplateEngine";
    }

}
