package groovy.io;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class PlatformLineWriterTest extends TestCase {

    public void testPlatformLineWriter() throws IOException, ClassNotFoundException {
        String LS = System.getProperty("line.separator");
        Binding binding = new Binding();
        binding.setVariable("first", "Tom");
        binding.setVariable("last", "Adams");
        StringWriter stringWriter = new StringWriter();
        Writer platformWriter = new PlatformLineWriter(stringWriter);
        GroovyShell shell = new GroovyShell(binding);
        platformWriter.write(shell.evaluate("\"$first\\n$last\\n\"").toString());
        assertEquals("Tom" + LS + "Adams" + LS, stringWriter.toString());
        stringWriter = new StringWriter();
        platformWriter = new PlatformLineWriter(stringWriter);
        platformWriter.write(shell.evaluate("\"$first\\r\\n$last\\r\\n\"").toString());
        assertEquals("Tom" + LS + "Adams" + LS, stringWriter.toString());
    }
}
