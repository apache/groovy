package groovy

/** 
 * VarargsMethodTest.groovy
 *
 *   1) Test to fix the Jira issues GROOVY-1023 and GROOVY-1026.
 *   2) Test the feature that the length of arguments can be variable
 *      when invoking methods with or without parameters.
 *
 * @author Dierk Koenig
 * @author Pilho Kim
 * @author Hein Meling
 * @version $Revision$
 */

class VarargsMethodTest extends GroovyTestCase {  

    void testVarargsOnly() {  
        assertEquals 1, varargsOnlyMethod('')  
        assertEquals 1, varargsOnlyMethod(1)  
        assertEquals 2, varargsOnlyMethod('','')  
        assertEquals 1, varargsOnlyMethod( ['',''] )  
        assertEquals 2, varargsOnlyMethod( ['',''] as Object[])  
        assertEquals 2, varargsOnlyMethod( *['',''] )  

        // todo: GROOVY-1023
        assertEquals 0, varargsOnlyMethod()

        // todo: GROOVY-1026
        assertEquals(-1, varargsOnlyMethod(null))
        assertEquals(2, varargsOnlyMethod(null, null))
     }  

     Integer varargsOnlyMethod(Object[] args) {  
         println("args = " + args)
         // (1) todo: GROOVY-1023 (Java 5 feature)
         //     If this method having varargs is invoked with no parameter,
         //     then args is not null, but an array of length 0.
         // (2) todo: GROOVY-1026 (Java 5 feature)
         //     If this method having varargs is invoked with one parameter
         //     null, then args is null, and so -1 is returned here.
         if (args == null)
               return -1
         return args.size()  
     }  
  
     void testVarargsLast() {  
         assertEquals 0, varargsLastMethod('')  
         assertEquals 0, varargsLastMethod(1)  
         assertEquals 1, varargsLastMethod('','')  
         assertEquals 2, varargsLastMethod('','','')  
         assertEquals 1, varargsLastMethod('', ['',''] )  
         assertEquals 2, varargsLastMethod('', ['',''] as Object[])  
         assertEquals 2, varargsLastMethod('', *['',''] )  

         // todo: GROOVY-1026
         assertEquals(-1, varargsLastMethod('',null))
         assertEquals(2, varargsLastMethod('',null, null))
     }  
  
     Integer varargsLastMethod(Object first, Object[] args) {  
         // (1) todo: GROOVY-1026 (Java 5 feature)
         //     If this method having varargs is invoked with two parameters
         //     1 and null, then args is null, and so -1 is returned here.
         if (args == null)
               return -1
         return args.size()  
     }  
}  
