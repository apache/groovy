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
      
      def actual = benchmarkInterceptor.statistic()
      def expected = [['b', 2, 0],['ctor', 1, 0],['a', 1, 0]]
      assert expected == actual
   }
}

class A{
   void a(){ b() }
   void b(){}
}
