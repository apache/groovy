package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

/**
 * Unit tests for static type checking : assignments.
 *
 * @author Cedric Champeau
 */
class STCAssignmentTest extends StaticTypeCheckingTestCase {

    void testAssignmentFailure() {
        shouldFailWithMessages """
            int x = new Object()
        """, "Cannot assign value of type java.lang.Object to variable of type java.lang.Integer"
    }

    void testAssignmentFailure2() {
        shouldFailWithMessages """
            Set set = new Object()
        """, "Cannot assign value of type java.lang.Object to variable of type java.util.Set"
    }

    void testAssignmentFailure3() {
        shouldFailWithMessages """
            Set set = new Integer(2)
        """, "Cannot assign value of type java.lang.Integer to variable of type java.util.Set"
    }

    void testIndirectAssignment() {
        shouldFailWithMessages """
            def o = new Object()
            int x = o
        """, "Cannot assign value of type java.lang.Object to variable of type java.lang.Integer"
    }

    void testIndirectAssignment2() {
        shouldFailWithMessages """
            def o = new Object()
            Set set = o
        """, "Cannot assign value of type java.lang.Object to variable of type java.util.Set"
    }

    void testIndirectAssignment3() {
        shouldFailWithMessages """
            int x = 2
            Set set = x
        """, "Cannot assign value of type int to variable of type java.util.Set"
    }

    void testAssignmentToEnum() {
        assertScript """
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 'a' // string to enum is implicit
            e = "${'a'}" // gstring to enum is implicit too
        """
    }

    void testAssignmentToEnumFailure() {
        shouldFailWithMessages """
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 1
        """, "Cannot assign value of type int to variable of type MyEnum"
    }

    void testAssignmentToString() {
        assertScript """
            String str = new Object()
        """
    }

    void testAssignmentToBoolean() {
        assertScript """
            boolean test = new Object()
        """
    }

    void testAssignmentToBooleanClass() {
        assertScript """
            Boolean test = new Object()
        """
    }

    void testAssignmentToClass() {
        assertScript """
            Class test = 'java.lang.String'
        """
    }



}

