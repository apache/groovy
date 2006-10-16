package groovy.xml.dom

import groovy.xml.*
import org.codehaus.groovy.sandbox.markup.*
import org.codehaus.groovy.tools.xml.*

class DOMTest extends GroovyTestCase {
    def benchmark = false

    void testDOMParser() {
        def xml = new StringReader("<html><head><title class='mytitle'>Test</title></head><body><p class='mystyle'>This is a test.</p></body></html>")
        def doc = DOMBuilder.parse(xml)
        def html = doc.documentElement
        if (!benchmark) assertCorrect html
    }

    void testDOMBuilder() {
        def html = DOMBuilder.newInstance().
        html {
          head {
            title (class:"mytitle", "Test")
          }
          body {
            p (class:"mystyle", "This is a test.")
          }
        }
        if (!benchmark) assertCorrect html
    }

  void testStreamingDOMBuilder_FAILS() { if (notYetImplemented()) return

        def doc = new StreamingDOMBuilder().bind{
          html {
            head {
              title (class:"mytitle", "Test")
            }
            body {
              p (class:"mystyle", "This is a test.")
            }
          }
        }()
        if (!benchmark) { assertCorrect doc.documentElement }
    }

    private def assertCorrect(html) {
        use (DOMCategory) {
          assert html.head.title.text() == ['Test']
          assert html.head.title[0].textContent == 'Test'
          assert html.body.p.text() == ['This is a test.']
          assert html.find { it.tagName == 'body' }.tagName == 'body'
          assert html.getElementsByTagName('*').findAll{ it.'@class' != '' }.size() == 2
        }
        // should fail outside category
        shouldFail (MissingPropertyException) { html.head }
    }
  
    static void main(args) {
        // Relative results:
        // Test       05 May 2004  14 Oct 2006
        // Parser:    1.0          1.0
        // Builder:   1.05         2.90
        // Streaming: 0.77         0.20
        def x = args.size() == 0 ? 1000 : Integer.parseInt(args[0])
        def mydomtest = new DOMTest()
        def standard = 0
        mydomtest.benchmark = true
        [{ mydomtest.testDOMParser() },
         { mydomtest.testDOMBuilder() },
         { mydomtest.testStreamingDOMBuilder_FAILS() }].eachWithIndex { testMethod, index ->
            // Run the method once to fill any caches and to load classes
            testMethod()
            def start = System.currentTimeMillis()
            def lastIndex
            for (i in 1..x) {
                testMethod()
                lastIndex = i
            }
            def elapsed = System.currentTimeMillis() - start
            def result = lastIndex * 1000 / elapsed

            standard = (standard == 0 ? result : standard)
            def factor = result/standard
            def relative = 1/factor
            println "${index}: ${factor}x relative=${relative} (${result} trees/s)"
        }
    }
}
