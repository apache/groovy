package gls.generics

class GenericsTest extends GenericsTestBase {
    
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
	
	public void testImplementsWithParameter() {
	    createClassInfo """
	    	abstract class B<T> implements List<T> {}
	    """
	    assert signatures==["class":"<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"]
	}
	
	public void testMultipleImplementsWithParameter() {
	    createClassInfo """
	    	abstract class B<T> implements Runnable,List<T> {}
	    """
	    assert signatures == ["class":"<T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/lang/Runnable;Ljava/util/List<TT;>;Lgroovy/lang/GroovyObject;"]
	}
	
	public void testBoundInterface() {
	    createClassInfo """
	    	class B<T extends List> {}
	    """
	    assert signatures == ["class":"<T::Ljava/util/List;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"]
	}
	
	public void testNestedReuseOfParameter() {
	    createClassInfo """
	    	class B<Y,T extends Map<String,Map<Y,Integer>>> {}
	    """
	    assert signatures == ["class":"<Y:Ljava/lang/Object;T::Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<TY;Ljava/lang/Integer;>;>;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;"]
	}
	
	public void testFieldWithParameter() {
	    createClassInfo """
	    	class B { public Collection<Integer> books }
	    """
	    assert signatures == [books : "Ljava/util/Collection<Ljava/lang/Integer;>;"]
	}
	
	public void testFieldReusedParameter() {
	    createClassInfo """
	    	class B<T> { public Collection<T> collection }
	    """
	    assert signatures == ["class"    : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
	                          collection : "Ljava/util/Collection<TT;>;"]
	}
	
	public void testParameterAsReturnType() {
	    createClassInfo """
	    	class B {
	    		static <T> T foo() {return null}
			}
	    """
	    assert signatures == ["foo()Ljava/lang/Object;":"<T:Ljava/lang/Object;>()TT;"]
	}
	
	public void testParameterAsReturnTypeAndParameter() {
	    createClassInfo """
	    	class B {
	    		static <T> T foo(T t) {return null}
			}
	    """
	    assert signatures == ["foo(Ljava/lang/Object;)Ljava/lang/Object;":"<T:Ljava/lang/Object;>(TT;)TT;"]
	}
	
	public void testParameterAsMethodParameter() {
	    createClassInfo """
			class B<T> {
	    		void foo(T t){}
			}
	    """
	    assert signatures == ["class" : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;", 
	        				  "foo(Ljava/lang/Object;)V":"(TT;)V"]
	}
	
	public void testParameterAsNestedMethodParameter() {
	    createClassInfo """
			class B<T> {
	    		void foo(List<T> t){}
			}
	    """
	    assert signatures == ["class" : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;", 
	        				  "foo(Ljava/util/List;)V":"(Ljava/util/List<TT;>;)V"]
	}
	
	public void testParameterAsNestedMethodParameterReturningInterface() {
	    createClassInfo """
			class B<T> {
	    		Cloneable foo(List<T> t){}
			}
	    """
	    assert signatures == ["class" : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;", 
	        				  "foo(Ljava/util/List;)Ljava/lang/Cloneable;":"(Ljava/util/List<TT;>;)Ljava/lang/Cloneable;"]
	}
	
	public void testMultipleBounds() {
	    createClassInfo """
			class Pair<	A extends Comparable<A> & Cloneable , 
    					B extends Cloneable & Comparable<B> > 
			{
	    		A foo(){}
				B bar(){}
			}
	    """
	    assert signatures == 
	        ["class" : "<A::Ljava/lang/Comparable<TA;>;:Ljava/lang/Cloneable;B::Ljava/lang/Cloneable;:Ljava/lang/Comparable<TB;>;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
	    	 "foo()Ljava/lang/Comparable;" : "()TA;",
	    	 "bar()Ljava/lang/Cloneable;"  : "()TB;"]
	}

	public void testWildCard() {
	    createClassInfo """
			class B {
				private Collection<?> f1 
				private List<? extends Number> f2 
				private Comparator<? super String> f3 
				private Map<String,?> f4  
			}
	    """
	    assert signatures==[
	    	f1 : "Ljava/util/Collection<*>;",
	    	f2 : "Ljava/util/List<+Ljava/lang/Number;>;",
	    	f3 : "Ljava/util/Comparator<-Ljava/lang/String;>;",
			f4 : "Ljava/util/Map<Ljava/lang/String;*>;"   
	    	]
	}
	
	public void testParameterAsParameterForReturnTypeAndFieldClass() {
	    createClassInfo """
		   	public class B<T> {
   				private T owner;
   				public Class<T> getOwnerClass(){}
   
			} 
	    """
	    assert signatures==[
			"class" : "<T:Ljava/lang/Object;>Ljava/lang/Object;Lgroovy/lang/GroovyObject;",
			"owner" : "TT;",
			"getOwnerClass()Ljava/lang/Class;" : "()Ljava/lang/Class<TT;>;"
			]
	}
	
	public void testClassWithoutParameterExtendsClassWithFixedParameter() {
	    createClassInfo """
			class B extends ArrayList<Long> {} 
	    """
	    assert signatures==[
			"class" : "Ljava/util/ArrayList<Ljava/lang/Long;>;Lgroovy/lang/GroovyObject;",
			]
	}

	public void testInvalidParameterUsage_FAILS() {
	    if (notYetImplemented()) return
	    shouldNotCompile """
	    	abstract class B<T> implements Map<T>{}
	    """
	}
}