package groovy

/**
 * @author Danno Ferrin
 * @version $Revision: ???? $
 */
class ClosureMissingMethodTest extends GroovyTestCase {

  void testInScript() {
      GroovyShell shell = new GroovyShell()
      shell.evaluate("""
          int count = 0

          foo = {
            println "inside foo"
            count++
            bar()
          }
          baz = {
            println "inside baz"
            foo()
          }

          try {
              baz()
              fail()
          } catch (org.codehaus.groovy.runtime.InvokerInvocationException iie) {
              assert iie.cause.method == 'bar'
              assert count == 1
          } catch (MissingMethodException mme) {
              assert mme.method == 'bar'
              assert count == 1
          }
      """);
  }

  void testInMethod() {
      int count = 0

      def foo = {
          println "inside foo"
          count++
          bar()
      }
      def baz = {
          println "inside baz"
          foo()
      }

      try {
          baz()
          fail()
      } catch (MissingMethodException mme) {
          assert mme.method == 'bar'
          assert count == 1
      }
  }

  void testWithMetaClassInScript() {
      GroovyShell shell = new GroovyShell()
      shell.evaluate("""
          int count = 0

          foo = {
            println "inside foo"
            count++
            bar()
          }
          baz = {
            println "inside baz"
            foo()
          }
          mc = new ExpandoMetaClass(baz.getClass())
          mc.initialize()
          baz.metaClass = mc

          try {
              baz()
              fail()
          } catch (org.codehaus.groovy.runtime.InvokerInvocationException iie) {
              assert iie.cause.method == 'bar'
              assert count == 1
          } catch (MissingMethodException mme) {
              assert mme.method == 'bar'
              assert count == 1
          }
      """);
  }

  void testWithMetaClassInMethod() {
      int count = 0

      def foo = {
          println "inside foo"
          count++
          bar()
      }
      def baz = {
          println "inside baz"
          foo()
      }
      MetaClass mc = new ExpandoMetaClass(baz.getClass())
      mc.initialize()
      baz.metaClass = mc

      try {
          baz()
          fail()
      } catch (MissingMethodException mme) {
          assert mme.method == 'bar'
          assert count == 1
      }
  }
}