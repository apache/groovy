package groovy.transform.stc

/**
 * Unit tests for static type checking : bug fixes.
 *
 * @author Cedric Champeau
 */
class BugsSTCTest extends StaticTypeCheckingTestCase {
    // GROOVY-5456
    void testShouldNotAllowDivOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it / 2 } }
        ''', 'Cannot find matching method java.lang.Object#div(int)'
    }
    void testShouldNotAllowDivBynUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { 2 / it } }
        ''', 'Cannot find matching method int#div(java.lang.Object)'
    }
    void testShouldNotAllowModOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it % 2 } }
        ''', 'Cannot find matching method java.lang.Object#mod(int)'
    }
    void testShouldNotAllowModBynUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { 2 % it } }
        ''', 'Cannot find matching method int#mod(java.lang.Object)'
    }
    void testShouldNotAllowMulOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it * 2 } }
        ''', 'Cannot find matching method java.lang.Object#multiply(int)'
    }
    void testShouldNotAllowMulBynUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { 2 * it } }
        ''', 'Cannot find matching method int#multiply(java.lang.Object)'
    }
    void testShouldNotAllowPlusOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it + 2 } }
        ''', 'Cannot find matching method java.lang.Object#plus(int)'
    }
    void testShouldNotAllowPlusWithUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { 2 + it } }
        ''', 'Cannot find matching method int#plus(java.lang.Object)'
    }
    void testShouldNotAllowMinusOnUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { it - 2 } }
        ''', 'Cannot find matching method java.lang.Object#minus(int)'
    }
    void testShouldNotAllowMinusBynUntypedVariable() {
        shouldFailWithMessages '''
            def foo(Closure cls) {}
            def bar() { foo { 2 - it } }
        ''', 'Cannot find matching method int#minus(java.lang.Object)'
    }

    void testGroovy5444() {
        assertScript '''
                def curr = { System.currentTimeMillis() }

                5.times {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                        assert node.getNodeMetaData(DECLARATION_INFERRED_TYPE) == long_TYPE
                    })
                    def t0 = curr()
                    100000.times {
                        "echo"
                    }
                    println (curr() - t0)
                }'''

    }

    void testGroovy5487ReturnNull() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == make(List)
        })
        List getList() {
            null
        }
        '''
    }

    void testGroovy5487ReturnNullWithExplicitReturn() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == make(List)
        })
        List getList() {
            return null
        }
        '''
    }

    void testGroovy5487ReturnNullWithEmptyBody() {
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == make(List)
        })
        List getList() {
        }
        '''
    }

    void testGroovy5482ListsAndFlowTyping() {
        assertScript '''
        class StaticGroovy2 {
            def bar() {

                def foo = [new Date(), 1, new C()]
                foo.add( 2 ) // Compiles
                foo.add( new Date() )
                foo.add( new C() )

                foo = [new Date(), 1]
                foo.add( 2 ) // Does not compile
            }
        }
        class C{
        }
        new StaticGroovy2()'''
    }

    void testClosureDelegateThisOwner() {
        assertScript '''
            class A {
                A that = this
                void m() {
                    def cl = {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                            assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                        })
                        def foo = this
                        assert this == that
                    }
                    cl()
                    cl = {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                            assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                        })
                        def foo = delegate
                        assert delegate == that
                    }
                    cl()
                    cl = {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                            assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                        })
                        def foo = owner
                        assert owner == that
                    }
                }
            }
            new A().m()
        '''
    }
    void testClosureDelegateThisOwnerUsingGetters() {
        assertScript '''
            class A {
                A that = this
                void m() {
                    def cl = {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                            assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                        })
                        def foo = getThisObject()
                        assert getThisObject() == that
                    }
                    cl()
                    cl = {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                            assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                        })
                        def foo = getDelegate()
                        assert getDelegate() == that
                    }
                    cl()
                    cl = {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                            assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                        })
                        def foo = getOwner()
                        assert getOwner() == that
                    }
                }
            }
            new A().m()
        '''
    }

    // GROOVY-5616
    void testAssignToGroovyObject() {
        assertScript '''
        class A {}
        GroovyObject obj = new A()
        '''
    }

    void testAssignJavaClassToGroovyObject() {
        shouldFailWithMessages '''
        GroovyObject obj = 'foo'
        ''', 'Cannot assign value of type java.lang.String to variable of type groovy.lang.GroovyObject'
    }

    void testCastToGroovyObject() {
        assertScript '''
        class A {}
        GroovyObject obj = new A()
        '''
    }

    void testAssignInnerClassToGroovyObject() {
        assertScript '''
        class A { static class B {} }
        GroovyObject obj = new A.B()
        '''
    }
    void testCastInnerClassToGroovyObject() {
        assertScript '''
        class A { static class B {} }
        GroovyObject obj = (GroovyObject)new A.B()
        '''
    }

    void testGroovyObjectInGenerics() {
        assertScript '''
        class A {}
        List<? extends GroovyObject> list = new LinkedList<? extends GroovyObject>()
        list.add(new A())
        '''
    }

    // GROOVY-5656
    void testShouldNotThrowAmbiguousMethodError() {
        assertScript '''import groovy.transform.*

        class Expr {}
        class VarExpr extends Expr {}

        class ArgList {
            ArgList(Expr e1) {  }
            ArgList(Expr[] es) {  }
        }

        class Bug4 {
            void test() {
                new ArgList(new VarExpr())
            }
        }

        new Bug4().test()
        '''
    }

    // GROOVY-5793
    void testByteAsParameter() {
        assertScript '''
        void testMethod(java.lang.Byte param){
            println(param)
        }

        void execute(){
            testMethod(java.lang.Byte.valueOf("123"))
        }

        execute()'''
    }

    // GROOVY-5874-part-1
    void testClosureSharedVariableInBinExp() {
        shouldFailWithMessages '''
            def sum = 0
            def cl1 = { sum = sum + 1 }
            def cl2 = { sum = new Date() }

        ''', 'A closure shared variable [sum] has been assigned with various types'
    }

    // GROOVY-5870
    void testShouldNotThrowErrorIfTryingToCastToInterface() {
        assertScript '''
            Set tmp = null
            List other = (List) tmp // should not complain because source and target are interfaces
        '''
    }

    // GROOVY-5889
    void testShouldNotGoIntoInfiniteLoop() {
        assertScript '''
        class Enclosing {
            static class FMessage {
                static enum LogLevel { finest, finer, fine, config, info, warning, severe }
                LogLevel logLevel
            }
        }
        new Enclosing()
        '''
    }

    // GROOVY-5959
    void testSwitchCaseShouldNotRemoveBreakStatements() {
        assertScript '''
        int test(Map<String, String> token) {
          switch(token.type) {
            case 'case one':
              1
              break
            case 'case two':
              2
              break
            default:
              3
              break
          }
        }
        assert test([type:'case one']) == 1
        assert test([type:'case two']) == 2
        assert test([type:'default']) == 3
        '''
    }

    void testShouldChooseFindMethodFromList() {
        assertScript '''
        class Mylist implements List<Object> {

            int size() { }
            boolean isEmpty() {}
            boolean contains(final Object o) {}
            Iterator iterator() {[].iterator()}
            Object[] toArray() {}
            Object[] toArray(final Object[] a) {}
            boolean add(final Object e) {}
            boolean remove(final Object o) {}
            boolean containsAll(final Collection<?> c) {}
            boolean addAll(final Collection c) {}
            boolean addAll(final int index, final Collection c) {}
            boolean removeAll(final Collection<?> c) {}
            boolean retainAll(final Collection<?> c) {}
            void clear() {}
            Object get(final int index) {}
            Object set(final int index, final Object element) {}
            void add(final int index, final Object element) {}
            Object remove(final int index) {}
            int indexOf(final Object o) {}
            int lastIndexOf(final Object o) {}
            ListIterator listIterator() {}
            ListIterator listIterator(final int index) {}
            List subList(final int fromIndex, final int toIndex) {}
        }

           def whatthe(Mylist a) {
               a.find { true }
           }
        whatthe(new Mylist())
        '''
    }

    // GROOVY-6050
    void testShouldAllowTypeConversion() {
        assertScript '''
            interface SomeInterface { void sayHello() }
            void foo(Writer writer) {
                if (writer instanceof SomeInterface) {
                    ((SomeInterface)writer).sayHello()
                }
            }
            foo(null)
        '''
    }

    // GROOVY-6099
    void testFlowTypingErrorWithIfElse() {
        assertScript '''
            def o = new Object()
            boolean b = true
            if (b) {
                o = 1
            } else {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                })
                def o2 = o
                println (o2.toString())
            }
        '''
    }
    void testFlowTypingErrorWithIfElseAndNoInitialInferredType() {
        assertScript '''
            def o
            boolean b = true
            if (b) {
                o = 1
            } else {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                })
                def o2 = o
                o2 = 'foo'
                println (o2.toString())
            }
        '''
    }

    // GROOVY-6104
    void testShouldResolveConstantFromInterfaceImplementedInSuperClass() {
        assertScript '''
            interface Foo {
                public static int MY_CONST = 85
            }
            class FooImpl implements Foo {}
            class Bar extends FooImpl {
                void bar() {
                    assert MY_CONST == 85
                }
            }
            new Bar().bar()
        '''
    }

    // GROOVY-6098
    void testUnresolvedPropertyReferencingIsBooleanMethod() {
        assertScript '''
            boolean isFoo() { true }
            assert foo
        '''
    }

    // GROOVY-6119
    void testShouldCallConstructorWithMap() {
        assertScript '''
            class Foo {
                String message
                Foo(Map map) {
                    message = map.msg
                }
            }
            def foo = new Foo(msg: 'bar')
            assert foo.message == 'bar'
        '''
    }

    // GROOVY-6119
    void testShouldCallConstructorWithHashMap() {
        assertScript '''
            class Foo {
                String message
                Foo(HashMap map) {
                    message = map.msg
                }
            }
            def foo = new Foo(msg: 'bar')
            assert foo.message == 'bar'
        '''
    }

    // GROOVY-6162
    void testShouldConsiderThisInStaticContext() {
        assertScript '''
            class Foo {
                static def staticMethod() {
                    @ASTTest(phase=INSTRUCTION_SELECTION,value={
                        def ift = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                        assert ift == CLASS_Type
                        assert ift.isUsingGenerics()
                        assert ift.genericsTypes[0].type.name == 'Foo'
                    })
                    def foo = this

                    this.classLoader
                }
            }
            assert Foo.staticMethod() instanceof ClassLoader
        '''
    }

}
