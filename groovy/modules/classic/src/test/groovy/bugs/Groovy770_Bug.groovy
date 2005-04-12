/**
 * @version $Revision$
 */

package groovy.bugs

class Groovy770_Bug extends GroovyTestCase {
     
    void testBug() {
        a = new Pair(sym:"x")
        b = new Pair(sym:"y")
        c = new Pair(sym:"y")

        l1 = [a, b]
        l2 = [c]
        println (l1)
        println (l2)
        println (l1 - l2)
        assert l1 - l2 == l1


        a = new CPair(sym:"x")
        b = new CPair(sym:"y")
        c = new CPair(sym:"y")
        l1 = [a, b]
        l2 = [c]
        println (l1)
        println (l2)
        println (l1 - l2)
        assert l1 - l2 == [a]
    }
}

import java.util.*

class Pair {
  String sym
}

class CPair implements Comparable {
  public String sym
  int compareTo(Object o) {
      return sym.compareTo(((CPair) o).sym);
  }
}


