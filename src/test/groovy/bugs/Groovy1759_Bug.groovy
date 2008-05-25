class Groovy1759_Bug extends GroovyTestCase {
   void testInterception() {
      def benchmarkInterceptor = new BenchmarkInterceptor()
      def proxy = ProxyMetaClass.getInstance(A.class)
      proxy.setInterceptor(benchmarkInterceptor)
      proxy.use {
         def a = new A()
         a.a()
         a.b()
      }
      
      def actual = benchmarkInterceptor.statistic().collect{ [ it[0], it[1] ] }
      def expected = [['ctor', 1],['a', 1],['b', 2]]
      assert expected == actual
   }
}

class A{
   void a(){ b() }
   void b(){}
}
