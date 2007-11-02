package gls.generics.vm5

class GenericsTest extends gls.generics.GenericsTestBase {

    public void testClassWithoutParameterExtendsClassWithFixedParameter() {
        createClassInfo """
            class B extends ArrayList<Long> {}
        """
        assert signatures==[
            "class" : "Ljava/util/ArrayList<Ljava/lang/Long;>;Lgroovy/lang/GroovyObject;",
            ]
    }

    public void testMultipleImplementsWithParameter() {
        createClassInfo """
            abstract class B<T> implements Runnable,List<T> {}
        """
        assert signatures == ["class":"<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/Runnable;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"]
    }

    public void testImplementsWithParameter() {
        createClassInfo """
            abstract class B<T> implements List<T> {}
        """
        assert signatures==["class":"<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"]
    }

    public void testExtendsWithParameter() {
	    createClassInfo """
	    	class B<T> extends ArrayList<T> {}
	    """
	    assert signatures==["class":"<T:Ljava/lang/Object;>Ljava/util/ArrayList<TT;>;Lgroovy/lang/GroovyObject;"]
	}

	public void testNestedExtendsWithParameter() {
	    createClassInfo """
	    	class B<T> extends HashMap<T,List<T>> {}
	    """
	    assert signatures == ["class":"<T:Ljava/lang/Object;>Ljava/util/HashMap<TT;Ljava/util/List<TT;>;>;Lgroovy/lang/GroovyObject;"]
	}
}
