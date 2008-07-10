package org.codehaus.groovy.runtime

import org.codehaus.groovy.reflection.ClassInfo

class PerInstanceMetaClassTest extends GroovyTestCase{

    protected void setUp() {
        Integer.metaClass = null
    }

    void testEMC () {
        def x = 22
        def FOUR = 4
        ExpandoMetaClass emc = new ExpandoMetaClass(Integer, false, true).define {
            plus {Number arg ->
                Integer.metaClass.invokeMethod(delegate / 2, "plus", 2 * arg)
            }

            _100 = 100

            'static'{
                addTwice {Number u ->
                    delegate + 2 * u
                }

                mul = { u ->
                    11 * u
                }

                divFour << { u ->
                    u / FOUR
                }
            }
        }
        emc.initialize ()
        x.metaClass = emc

        assertEquals 123, x + 6 + x._100
        assertEquals 211, x.addTwice(50)
        assertEquals 127, 5 + x + x._100
        assertEquals 3, x.divFour (12)
        assertEquals 55, x.mul(5)

        assertEquals 23, 10 + 12 + 6  // (10+12) == Integer.valueOf(22) !!!!!!

        x.metaClass = null
        Integer.metaClass = null

        assertEquals 28, x + 6


        x.metaClass.static.addTwice = {Number u ->
                    delegate + 2 * u
                }

        x.metaClass.static.mul = { u ->
                    11 * u
                }

        x.metaClass.static.divFour << { u ->
            u / FOUR
        }

        x.metaClass.plus = {Number arg ->
                Integer.metaClass.invokeMethod(delegate / 2, "plus", 2 * arg)
            }

        x.metaClass._100 = 100

        assertEquals 123, x + 6 + x._100
        assertEquals 211, x.addTwice(50)
        assertEquals 127, 5 + x + x._100
        assertEquals 3, x.divFour (12)
        assertEquals 55, x.mul(5)
        assertEquals 23, 10 + 12 + 6  // (10+12) == Integer.valueOf(22) !!!!!!

        x.metaClass = null

        assertEquals 28, 10 + 12 + 6
    }

    static def CONST = 2

    void testMetaClassMethod () {
        def x = 22
        x.metaClass {
            // define method
            plus { Number arg ->
               Integer.metaClass.invokeMethod( delegate / CONST, "plus", 2*arg )
            }

             // define property
            _100 = 100

            'static' {
                addTwice { Number u ->
                    delegate + 2 * u
                }
            }
        }

        assertEquals 123, x + 6 + x._100
        assertEquals 211, x.addTwice(50)
        assertEquals 127, 5 + x + x._100

        assertEquals 23, 10 + 12 + 6  // (10+12) == Integer.valueOf(22) !!!!!!

        x.metaClass {
            // add method
            minus { Number arg ->
               delegate + arg
            }
        }

        assertEquals 23, x - 6

        x.metaClass = null

        assertEquals 28, x + 6
        assertEquals 16, x - 6
    }

    void testBean () {
        def bean = new PimctBean()

        assertEquals 24, bean.value

        bean.metaClass {
            getValue { ->
                25
            }
        }

        assertEquals 25, bean.value

        bean.metaClass {
            getValue { ->
                delegate.@value
            }

        }
        bean.metaClass.setValue = { v -> delegate.@value = 2*v }

        assertEquals 24, bean.value
        bean.value = 10
        assertEquals 20, bean.value

        bean.metaClass = null
        shouldFail(GroovyRuntimeException) {
            bean.metaClass {
                setValue << { v -> delegate.@value = 3*v }
            }
        }

        bean.metaClass = null
        bean.metaClass {
            prop = 12

            foo << { ->
                3*prop
            }
        }
        bean.prop = 7
        assertEquals 21, bean.foo ()
    }

    void testClass () {
        Integer.metaClass {
            'static' {
                fib { Number n ->
                    n.fib ()
                }
            }

            static.unused = { -> }

            fib { ->
                def n = delegate
                if (n == 0)
                  return 1;
                if (n == 1)
                  return 1
                else
                  return fib(n-1) + fib(n-2)
            }
        }

        assertEquals( 3, Integer.fib(3))

        4.metaClass {
            fib { ->
                10
            }
        }

        assertEquals( 13, Integer.fib(5))

        4.metaClass {
            fib { ->
                fib(2) + fib(3)
            }
        }

        assertEquals( 8, Integer.fib(5))
        4.metaClass = null
    }

    void testCategory () {
        Integer.metaClass {
            mixin Fib
        }

        assertEquals (3, 3.fib())

        Integer.metaClass = null

        shouldFail {
            3.fib()
        }

        4.metaClass {
            mixin FibInst
        }
        assertEquals (15, 4.fib())
        4.metaClass = null
    }
}

class PimctBean {
    def value = 24
}

class Fib {
    static int fib (Integer self) {
        if (self == 0 || self == 1)
          return 1
        else
          return (self-1).fib() + (self-2).fib ()
    }
}

class FibInst {
    static int fib (Integer self) {
        15
    }
}
