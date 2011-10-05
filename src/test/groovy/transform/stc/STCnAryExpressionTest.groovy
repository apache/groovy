package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

/**
 * Unit tests for static type checking : unary and binary expressions.
 *
 * @author Cedric Champeau
 */
class STCnAryExpressionTest extends StaticTypeCheckingTestCase {

    void testBinaryStringPlus() {
        assertScript """
            String str = 'a'
            String str2 = 'b'
            str+str2
        """
    }

    void testBinaryStringPlusInt() {
        assertScript """
            String str = 'a'
            int str2 = 2
            str+str2
        """
    }

    void testBinaryObjectPlusInt() {
        shouldFailWithMessages """
            def str = new Object()
            int str2 = 2
            str+str2
        """, "Cannot find matching method plus(java.lang.Object, java.lang.Integer)",
                "tbd" // todo : remove when transformation will be fixed
    }

    void testBinaryIntPlusObject() {
        shouldFailWithMessages """
            def str = new Object()
            int str2 = 2
            str2+str
        """, "Cannot find matching method plus(java.lang.Integer, java.lang.Object)",
                "tbd" // todo : remove when transformation will be fixed
    }


}

