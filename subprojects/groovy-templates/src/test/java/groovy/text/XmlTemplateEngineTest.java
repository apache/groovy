package groovy.text;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class XmlTemplateEngineTest extends TestCase {

    public void testBinding() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<!-- Just a comment. -->\n"
                + "<xml xmlns:gsp=\"http://groovy.codehaus.org/2005/gsp\">"
                + "  ${Christian}"
                + "  <gsp:expression>Christian</gsp:expression>"
                + "  <gsp:scriptlet>println Christian</gsp:scriptlet>"
                + "</xml>";
        String xmlResult = "<xml>\n"
                + "  Stein\n"
                + xmlTemplateEngine.getIndentation() + "Stein\n"
                + "Stein" + System.getProperty("line.separator")
                + "</xml>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testQuotes() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<!-- Just a comment. -->\n"
                + "<xml xmlns:mygsp=\"http://groovy.codehaus.org/2005/gsp\">"
                + "  ${Christian + \" \" + Christian}"
                + "  <mygsp:expression>Christian + \" \" + Christian</mygsp:expression>"
                + "  <mygsp:scriptlet>println Christian</mygsp:scriptlet>"
                + "</xml>";
        String xmlResult = "<xml>\n"
                + "  Stein Stein\n"
                + xmlTemplateEngine.getIndentation() + "Stein Stein\n"
                + "Stein" + System.getProperty("line.separator")
                + "</xml>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testNamespaces() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<foo:bar xmlns:foo='urn:baz' xmlns:mygsp=\"http://groovy.codehaus.org/2005/gsp\">"
                + "${Christian + \" \" + Christian}"
                + "<mygsp:expression>Christian + \" \" + Christian</mygsp:expression>"
                + "<nonamespace><mygsp:scriptlet>println Christian</mygsp:scriptlet></nonamespace>"
                + "</foo:bar>";
        String xmlResult = "<foo:bar xmlns:foo='urn:baz'>\n"
                + "  Stein Stein\n"
                + xmlTemplateEngine.getIndentation() + "Stein Stein\n"
                + xmlTemplateEngine.getIndentation() + "<nonamespace>\n"
                + "Stein" + System.getProperty("line.separator")
                + xmlTemplateEngine.getIndentation() + "</nonamespace>\n"
                + "</foo:bar>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testDoubleQuotesInAttributeValues() throws Exception{
        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<document a='quoted \"string\"'/>";
        String xmlResult = "<document a='quoted \"string\"'/>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make().toString());
    }
}
