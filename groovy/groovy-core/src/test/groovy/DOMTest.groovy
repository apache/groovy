import java.io.*;
import groovy.xml.*;

class DOMTest extends GroovyTestCase {

  void testDOMParser() {
    use groovy.xml.dom.DOMCategory;

    xml = new StringReader("<html><head><title class='mytitle'>Test</title></head><body><p class='mystyle'>This is a test.</p></body></html>");
    doc = DOMBuilder.parse(xml);
    html = doc.documentElement;
    
    assertCorrect html;
  }

  void testDOMBuilder() {
    use groovy.xml.dom.DOMCategory;

    html = DOMBuilder.newInstance().
        
    html {
      head {
		title (class:"mytitle", 'Test');
      }
      body {
		p (class:"mystyle", 'This is a test.')
      }
    }

    assertCorrect html;
  }

  assertCorrect(html) {
    assert html.head.title.textContent == "Test";
    assert html.body.p.textContent == "This is a test.";
    assert html.find { it.tagName == "body" }.tagName == "body";
    assert html.getElementsByTagName("*").findAll { if (it["@class"] != "") { return it } }.size() == 2;
  }
  
  /*
  static void main(args) {
  	test = new DOMTest();
  	test.testDOMParser();
  	test.testDOMBuilder();
  }
  */
}
