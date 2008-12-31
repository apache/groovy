package groovy

/**
 * @author Hallvard Tr�tteberg
 * @version $Revision$
 */
class ClosureCurryTest extends GroovyTestCase {

    void testCurry() {
        def clos1 = {s1, s2 -> s1 + s2}
        def clos2 = clos1.curry("hi")
        def value = clos2("there")
        assert value == "hithere"

        def clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        def clos4 = clos3.curry('a')
        def clos5 = clos4.curry('b')
        def clos6 = clos4.curry('x')
        def clos7 = clos4.curry('f', 'g')
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}.asWritable()
        clos4 = clos3.curry('a')
        clos5 = clos4.curry('b')
        clos6 = clos4.curry('x')
        clos7 = clos4.curry('f', 'g')
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        clos4 = clos3.curry('a').asWritable()
        clos5 = clos4.curry('b').asWritable()
        clos6 = clos4.curry('x').asWritable()
        clos7 = clos4.curry('f', 'g').asWritable()
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        clos4 = clos3.curry('a').clone()
        clos5 = clos4.curry('b').clone()
        clos6 = clos4.curry('x').clone()
        clos7 = clos4.curry('f', 'g').clone()
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        clos4 = clos3.curry('a').asWritable().clone()
        clos5 = clos4.curry('b').asWritable().clone()
        clos6 = clos4.curry('x').asWritable().clone()
        clos7 = clos4.curry('f', 'g').asWritable().clone()
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"
    }

    void testParameterTypes() {
        def cl1 = {String s1, int i -> return s1 + i }
        assert "foo5" == cl1("foo", 5)
        assert [String, int] == cl1.getParameterTypes().toList()

        def cl2 = cl1.curry("bla")
        assert "bla4" == cl2(4)
        assert null != cl2.getParameterTypes()
        assert [int] == cl2.getParameterTypes().toList()
    }

    void testDelegate() {
        def res = null
        def c = {a -> res = z}
        def cc = c.curry(1)

        cc.delegate = [z: "goodbye"]
        cc()
        assert res == cc.delegate.z
    }

    void testExpandoWithCurry() {
        def sz = 'java.util.Date'.size()
        def c = {arg -> arg + delegate.getClass().name.size() }
        def d = new Date()
        d.metaClass.foo = c
        assert d.foo(42) == 42 + sz
        d.metaClass.bar = c.curry('baz')
        assert d.bar() == 'baz' + sz
    }


}
