package groovy

class ModuloTest extends GroovyTestCase {
  int modulo = 100

  void testModuloLesser() {
    for (i in 0..modulo-1) {
      assert (i%modulo)==i
    }
  }

  void testModuloEqual() {
    for (i in 0..modulo) {
      assert ((i*modulo) % modulo)==0
    }
  }

  void testModuloBigger() {
    for (i in 0..modulo-1) {
      assert ((i*modulo+i) % modulo)==i
    }
  }

}
