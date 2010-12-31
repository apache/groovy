package groovy.bugs

class Groovy872 extends GroovyTestCase {
  void testScript ( ) {
    assertScript ( """
def cal = new GregorianCalendar ( )
cal.set ( Calendar.DAY_OF_MONTH , 1 )
println ( cal.get ( Calendar.DAY_OF_MONTH ) )
""")
  }
  void testCode ( ) {
    new MyCalendar ( ).tryit ( )
  }   
}

class MyCalendar {
  void tryit ( )  {
    def cal = new GregorianCalendar ( )
    cal.set ( Calendar.DAY_OF_MONTH , 1 )
    println ( cal.get ( Calendar.DAY_OF_MONTH ) )
  }
}
