package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

/**
 * Unit tests for static type checking : implicit and explicit returns.
 *
 * @author Cedric Champeau
 */
class ReturnsSTCTest extends StaticTypeCheckingTestCase {

    void testVoidReturn() {
        shouldFailWithMessages '''
            void method() {
            }

            int x = method()
        ''', 'Cannot assign value of type void to variable of type java.lang.Integer'
    }

    void testIncompatibleExplicitReturn() {
        shouldFailWithMessages '''
            String method() {
                return 'String'
            }

            int x = method()
        ''', 'Cannot assign value of type java.lang.String to variable of type java.lang.Integer'
    }

    void testIncompatibleExplicitReturn2() {
        shouldFailWithMessages '''
            int method() {
                return 'String'
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testIncompatibleImplicitReturn2() {
        shouldFailWithMessages '''
            int method() {
                'String'
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testIncompatibleImplicitReturn() {
        shouldFailWithMessages '''
            String method() {
                'String'
            }

            int x = method()
        ''', 'Cannot assign value of type java.lang.String to variable of type java.lang.Integer'
    }

    void testImplicitReturnFailureWithIfElse() {
        shouldFailWithMessages '''
            int method() {
                if (true) {
                    'String'
                } else {
                    2
                }
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testImplicitReturnFailureWithIfElse2() {
        shouldFailWithMessages '''
            int method() {
                if (true) {
                    2
                } else {
                    'String'
                }
            }
        ''', 'Cannot return value of type java.lang.String on method returning type int'
    }

    void testImplicitReturnFailureWithIfElse3() {
        shouldFailWithMessages '''
            int method() {
                if (true) {
                    'String'
                } else {
                    'String'
                }
            }
        ''',
            'Cannot return value of type java.lang.String on method returning type int', // first branch
            'Cannot return value of type java.lang.String on method returning type int' // second branch
    }

    void testImplicitReturnFailureWithSwitch() {
         shouldFailWithMessages '''
             int method(int x) {
                 switch (x) {
                    case 1:
                        2
                        break
                    case 2:
                        'String'
                        break
                    default:
                        3
                 }
             }
         ''', 'Cannot return value of type java.lang.String on method returning type int'
    }


   void testImplicitReturnFailureWithSwitch2() {
         assertScript '''
             int method(int x) {
                 switch (x) {
                    case 1:
                        2
                        break
                    case 2:
                        'String'
                    default:
                        3
                 }
             }
         ''' // should not fail because default case overrides case 2 return type
     }

    void testImplicitReturnToString() {
        assertScript '''
            // automatic toString works
            String greeting(String name) {
                def sb = new StringBuilder()
                sb << "Hi" << name
            }
        '''
    }

    void testWrongReturnType() {
        shouldFailWithMessages '''
            double greeting(String name) {
                new Object()
            }
        ''', 'Cannot return value of type java.lang.Object -> java.lang.Object on method returning type double'
    }

}

