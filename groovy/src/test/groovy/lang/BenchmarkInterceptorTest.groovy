package groovy.lang

/**
* Test for the BenchmarkInterceptor
* @author Dierk Koenig
**/
class BenchmarkInterceptorTest extends GroovyTestCase{

    Interceptor benchmarkInterceptor
    def proxy

    void setUp() {
        benchmarkInterceptor = new BenchmarkInterceptor()
        proxy = ProxyMetaClass.getInstance(Date.class)
        proxy.setInterceptor(benchmarkInterceptor)
    }

    void testSimpleInterception() {
        proxy.use {
             def x = new Date(0)
             x++
        }
        def stats = benchmarkInterceptor.statistic()
        assertEquals 2, stats.size()
        assert stats.find{it[0] == 'ctor'}
        assert stats.find{it[0] == 'next'}
        assert stats.every{it[1] == 1}
        assert stats.every{it[2] < 200}
    }


}



