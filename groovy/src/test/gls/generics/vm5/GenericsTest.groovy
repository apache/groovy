package gls.generics.vm5

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class GenericsTest extends GenericsTestBase {

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
	
	public void testInterfaceWithParameter() {
	    createClassInfo """
	    	interface B<T> {}
	    """
	    assert signatures == ["class"    : "<T:Ljava/lang/Object;>Ljava/lang/Object;"]
	}	
	
	
	public void testInvalidParameterUsage() {
	    shouldNotCompile """
	    	abstract class B<T> implements Map<T>{}
	    """
	    shouldNotCompile """
	        class A<T,V> extends ArrayList<T,V>{}
        """ 
        shouldNotCompile """
	        class A<T extends Number> {}
	        class B<T> extends A<T>{}
        """
	    shouldNotCompile """
	        class B<T> extends ArrayList<?>{}
        """ 
	}
	
	void testCovariantReturn() {
        shouldNotCompile """
          class A<T> {
              T foo(T t) {1}
           }

          class B extends A<Long>{
              String foo(Long l){"2"}
          }
        """

        assertScript """
          class A<T> {
              T foo(T t) {1}
           }

          class B extends A<Long>{
              Long foo(Long l){2}
          }
          def b = new B();
          try {
            b.foo(new Object())
            assert false
          } catch (ClassCastException cce) {
            assert true
          }
          assert b.foo((Long) 1) == 2
        """
	}	
	
	void testCovariantReturnWithInterface() {
	  assertScript """
	    import java.util.concurrent.*

        class CallableTask implements Callable<String> {
          public String call() { "x" }
        } 
        
        def task = new CallableTask()
        assert task.call() == "x"
      """  
	}
	
	void testCovariantReturnWithEmptyAbstractClassesInBetween() {
	  assertScript """
	    import java.util.concurrent.*;

        abstract class AbstractCallableTask<T> implements Callable<T> { }
        abstract class SubclassCallableTask<T> extends AbstractCallableTask<T> { }
        class CallableTask extends SubclassCallableTask<String> {
          public String call() { return "x"; }
        }
        assert "x" == new CallableTask().call();
	  """
	}

	void testGenericsInAsType() {
	  // this is to ensure no regression to GROOVY-2725 will happen
      // "as ThreadLocal<Integer>\n" did not compile because the nls
      // was swallowed and could not be used to end the expression
	   assertScript """
import java.util.concurrent.atomic.AtomicInteger

 public class ThreadId
 {
  private static final AtomicInteger nextId = new AtomicInteger(0)
  private static final ThreadLocal<Integer> threadId = [
                  initialValue: { return nextId.getAndIncrement() }
                  ] as ThreadLocal<Integer>

  public static int get()
  {
      System.out.println( "Thread ID: " + threadId.get());
      return threadId.get();
  }

 }
 // we do not actually want to execute something, just
 // ensure this compiles, so we do a dummy command here
 assert ThreadId!=null
	   """
	}

    void testCompilationWithMissingClosingBracketsInGenerics() {
        assertScriptAndVerifyCompilationError """
            def list1 = new ArrayList<Integer()
        """

        assertScriptAndVerifyCompilationError """
            List<Integer list2 = new ArrayList<Integer>()
        """

        assertScriptAndVerifyCompilationError """
            def c = []
            for (Iterator<String i = c.iterator(); i.hasNext(); ) { }
        """
        
        assertScriptAndVerifyCompilationError """
            def m(Class<Integer someParam) {}
        """
        
        assertScriptAndVerifyCompilationError """
            abstract class ArrayList1<E extends AbstractList<E> implements List<E> {}
        """

        assertScriptAndVerifyCompilationError """
            abstract class ArrayList2<E> extends AbstractList<E implements List<E> {}
        """

        assertScriptAndVerifyCompilationError """
            abstract class ArrayList3<E> extends AbstractList<E> implements List<E {}
        """
        
        assertScriptAndVerifyCompilationError """
            def List<List<Integer> history = new ArrayList<List<Integer>>()
        """

        assertScriptAndVerifyCompilationError """
            def List<List<Integer>> history = new ArrayList<List<Integer>()
        """
    }
    
    private void assertScriptAndVerifyCompilationError(scriptText) {
		try {
		    assertScript scriptText
		    fail("The script compilation should have failed as it contains mis-matching generic brackets")
		} catch(MultipleCompilationErrorsException mcee) {
		    def text = mcee.toString();
		    assert text.contains("Missing closing bracket '>' for generics types")
		}
    }
}
