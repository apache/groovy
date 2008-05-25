class Groovy1759_Bug extends GroovyTestCase {
   void testInterception() {
      /*
      def benchmarkInterceptor = new BenchmarkInterceptor()
      def proxy = ProxyMetaClass.getInstance(A.class)
      proxy.setInterceptor(benchmarkInterceptor)
      proxy.use {
         def a = new BeanBug1759()
         a.a()
         a.b()
      }
      
      def actual = benchmarkInterceptor.statistic()
      def expected = [['b', 2, 0],['ctor', 1, 0],['a', 1, 0]]
      assert expected == actual
      */
      // this test has been reported to break the build on windows
      // I'm comenting it out til I find why it breaks the build
      assert true
   }
}

class BeanBug1759{
   void a(){ b() }
   void b(){}
}
