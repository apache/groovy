package groovy.bugs

import groovy.mock.interceptor.StubFor

class Groovy3403Bug extends GroovyTestCase {

    void testStubIssueForStaticMethodsDueToCallSiteCachingWhenUsing2Stubs() {
        def stub1 = new StubFor(Main3403)
        stub1.demand.test() {
            return "stubbed call made - 1"
        }

        def ot = new Helper3403()

        stub1.use {
            assert ot.doTest() == "stubbed call made - 1"
        }

        def stub2 = new StubFor(Main3403)
        stub2.demand.test() {
            return "stubbed call made - 2"
        }

        // the following stubbed call is on stub2 and its demand count should be separate.
        // Currently due to caching of MockProxyMetaClass, it gets counted towards stub1 demands 
        // and throws "End of demands" exception
        stub2.use {
            assert ot.doTest() == "stubbed call made - 2"
        }
    }
}

class Main3403 {
   static test(){
       println "original call made"
   }
}

class Helper3403 {
    def doTest() {
        Main3403.test()
    }
}