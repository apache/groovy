import java.io.*;
import groovy.xml.*;
import org.codehaus.groovy.sandbox.markup.*;
import org.codehaus.groovy.tools.xml.*;

class DOMTest extends GroovyTestCase {

  void testDOMParser() {
    xml = new StringReader("<html><head><title class='mytitle'>Test</title></head><body><p class='mystyle'>This is a test.</p></body></html>");
    doc = DOMBuilder.parse(xml);
    html = doc.documentElement;
    
    assertCorrect html;
  }

  void testDOMBuilder() {
    html = DOMBuilder.newInstance().
        
    html {
      head {
		title (class:"mytitle", "Test")
      }
      body {
		p (class:"mystyle", "This is a test.")
      }
    }

    assertCorrect html;
  }

  void testStreamingDOMBuilder() {

    doc = new StreamingDOMBuilder().bind {
      html {
        head {
		  title (class:"mytitle") {
		    mkp.yield "Test";
		  }
        }
        body {
		  p (class:"mystyle") {
		    mkp.yield "This is a test.";
		  }
        }
      }
	}

    assertCorrect doc().documentElement;
  }

  assertCorrect(html) {
    use groovy.xml.dom.DOMCategory;

    assert html.head.title.textContent == "Test";
    assert html.body.p.textContent == "This is a test.";
    assert html.find { it.tagName == "body" }.tagName == "body";
    assert html.getElementsByTagName("*").findAll { if (it["@class"] != "") { return it } }.size() == 2;
  }
  
  static void main(args) {
  	// Relative results:
  	// Test       04/27/2004
  	// Parser:    1.0
  	// Builder:   1.28
  	// Streaming: 0.69
  	
  	test = new DOMTest();
  	standard = 0;
  	[test.testDOMParser, test.testDOMBuilder, test.testStreamingDOMBuilder].each {
  		// Run the method once to fill any caches and to load classes
  		it();
	  	start = System.currentTimeMillis();
  		for (i in 1..2000) {
  			it();
	  	}
  		elapsed = System.currentTimeMillis() - start;
  		result = i * 1000 / elapsed;

		standard = (standard == 0 ? result : standard);  		
		factor = result/standard;
  		
	  	println "${it.method}: ${factor}x (${result} trees/s)";
	}
  }
}
