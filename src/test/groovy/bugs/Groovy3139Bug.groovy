package groovy.bugs

import groovy.mock.interceptor.StubFor

class Groovy3139Bug extends GroovyTestCase {

    void testStubbingIssueDueToCachingWhenUsing2Stubs() {
        def urlStub1 = new StubFor(URL)
        urlStub1.demand.openConnection {""}
        urlStub1.use {
           def get = new Get2(url: "http://localhost")
           def result = get.text            
        }

        def urlStub2 = new StubFor(URL)
        // the following stubbed call is on urlStub2 and its demand cound should be separate.
        // Currently due to caching of MockProxyMetaClass, it gets counted towards urlStub1 demands 
        // and throws "End of demands" exception
        urlStub2.demand.openConnection {""}
        urlStub2.use {
           def get = new Get2(url: "http://localhost")
           def result = get.text
        }
    }
}

class Get2{
    String url
    
    String getText() {
            def aUrl = new URL(toString())
            def conn = aUrl.openConnection()
            return "DUMMY"
    }
        
    String toString(){
        return url
    }
}