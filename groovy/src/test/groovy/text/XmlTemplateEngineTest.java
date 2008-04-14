package groovy.text;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class XmlTemplateEngineTest extends TestCase {

    public void testBinding() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<!-- Just a comment. -->\n" //
                + "<xml xmlns:gsp=\"http://groovy.codehaus.org/2005/gsp\">" //
                + "  ${Christian}" //
                + "  <gsp:expression>Christian</gsp:expression>" //
                + "  <gsp:scriptlet>println Christian</gsp:scriptlet>" //
                + "</xml>";
        String xmlResult = "<xml>\n" //
                + "  Stein\n" //
                + xmlTemplateEngine.getIndentation() + "Stein\n" //
                + "Stein" + System.getProperty("line.separator") //
                + "</xml>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

    public void testQuotes() throws Exception {
        Map binding = new HashMap();
        binding.put("Christian", "Stein");

        XmlTemplateEngine xmlTemplateEngine = new XmlTemplateEngine();
        String xmlScript = "<!-- Just a comment. -->\n" //
                + "<xml xmlns:gsp=\"http://groovy.codehaus.org/2005/gsp\">" //
                + "  ${Christian + \" \" + Christian}" //
                + "  <gsp:expression>Christian + \" \" + Christian</gsp:expression>" //
                + "  <gsp:scriptlet>println Christian</gsp:scriptlet>" //
                + "</xml>";
        String xmlResult = "<xml>\n" //
                + "  Stein Stein\n" //
                + xmlTemplateEngine.getIndentation() + "Stein Stein\n" //
                + "Stein" + System.getProperty("line.separator") //
                + "</xml>\n";
        Template template = xmlTemplateEngine.createTemplate(xmlScript);
        assertEquals(xmlResult, template.make(binding).toString());
    }

}
