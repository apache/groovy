package groovy.io;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class PlatformLineWriterTest extends TestCase {

    public void testPlatformLineWriter() throws IOException, ClassNotFoundException {
        String LS = System.getProperty("line.separator");
        Map binding = new HashMap();
        binding.put("first", "Tom");
        binding.put("last", "Adams");
        Template template = new SimpleTemplateEngine().createTemplate("<%= first %>\n<% println last %>");
        StringWriter stringWriter = new StringWriter();
        Writer platformWriter = new PlatformLineWriter(stringWriter);
        template.make(binding).writeTo(platformWriter);
        assertEquals("Tom" + LS + "Adams" + LS, stringWriter.toString());
    }
}
