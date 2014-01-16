package groovy;

import groovy.text.Template;
import groovy.text.GStringTemplateEngine;

/**
 * @author andy
 * @since Jan 11, 2006 1:05:23 PM
 */
public class SimpleGStringTemplateEngineTest extends GroovyTestCase
{
  void testRegressionCommentBug()
  {
    final Template template = new GStringTemplateEngine().createTemplate(
        "<% // This is a comment that will be filtered from output %>\n" +
        "Hello World!"
    );

    final StringWriter sw = new StringWriter();
    template.make().writeTo(sw);
    assertEquals("\nHello World!", sw.toString());
  }

    void testShouldNotShareBinding() {
        String text = "<% println delegate.books; books = books.split(\",\"); out << books %>";

        StringWriter sw = new StringWriter();
        GStringTemplateEngine engine = new GStringTemplateEngine();

        Template template = engine.createTemplate(text);

        Map data = [books: 'a,b,c,d']


        // round one sucess
        template.make(data).writeTo(sw);
        assert sw.toString() == '[a, b, c, d]'

        sw = new StringWriter();
        // round two fails
        data = [books: 'e,f,g,h']
        template.make(data).writeTo(sw);
        assert sw.toString() == '[e, f, g, h]'

    }
}
