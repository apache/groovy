package org.codehaus.groovy.classgen

class ReflectorLoaderTest extends GroovyTestCase {

    void testDuplication(){
      def program =  '''
		closureA = {}
		closureB = {closureA ( ) }
	  '''
	  def binding  = new Binding()
	  ( new GroovyShell ( binding ) ).evaluate ( program )
	  binding.closureB.call( )
    }
}
