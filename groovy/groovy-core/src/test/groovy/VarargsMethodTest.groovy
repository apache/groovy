/** 
 * VarargsMethodTest.groovy
 *
 *     Test the feature that the length of arguments can be variable
 *     when invoking methods with or without parameters.
 *
 * @author Dierk Koenig
 * @author Pilho Kim
 * @version $Revision$
 */

class VarargsMethodTest extends GroovyTestCase {  

    void testVarargsOnly() {  
        assertEquals 0, varargsOnlyMethod() // todo: GROOVY-1023
        assertEquals 1, varargsOnlyMethod('')  
        assertEquals 1, varargsOnlyMethod(1)  
        assertEquals 2, varargsOnlyMethod('','')  
        assertEquals 1, varargsOnlyMethod( ['',''] )  
        assertEquals 2, varargsOnlyMethod( ['',''] as Object[])  
        assertEquals 2, varargsOnlyMethod( *['',''] )  
     }  

     Integer varargsOnlyMethod(Object[] args) {  
         println("args = " + args)
         // todo: GROOVY-1023
         //     If this method is invoked with no parameter,
         //     then args is not null, but an array of length 0.
         /* if (args == null)
               return 0
         */
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
     }  
  
     Integer varargsLastMethod(Object first, Object[] args) {  
         return args.size()  
     }  
}  
