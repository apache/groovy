package org.codehaus.groovy.runtime

import org.codehaus.groovy.reflection.ClassInfo

class PerInstanceMetaClassTest extends GroovyTestCase{

    static Integer [] cache

    static {
        try {
            Integer.valueOf(0)
        }
        catch (MissingMethodException e) {
            cache = new Integer[-(-128) + 127 + 1]
            for(int i = 0; i < cache.length; i++)
              cache[i] = new Integer(i - 128);
        }
    }

    protected void setUp() {
        Integer.metaClass = null
        setValueOfMethod()
    }

    private def setValueOfMethod() {
        if (cache != null) {
            Integer.metaClass.static.valueOf = {
                Integer i ->
                final int offset = 128;
                if (i >= -128 && i <= 127) { // must cache
                    return cache[i + offset];
                }
                return new Integer(i);
            }
        }
    }

    void testEMC () {
        def x = Integer.valueOf(22)
        def FOUR = Integer.valueOf(4)
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
                    Integer.valueOf(11 * u)
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

        assertEquals 23, Integer.valueOf(10 + 12) + 6  // (10+12) == Integer.valueOf(22) !!!!!!

        x.metaClass = null
        Integer.metaClass = null
        setValueOfMethod()

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
        assertEquals 23, Integer.valueOf(10 + 12) + 6  // (10+12) == Integer.valueOf(22) !!!!!!

        x.metaClass = null

        assertEquals 28, 10 + 12 + 6
    }

    static def CONST = 2

    void testMetaClassMethod () {
        def x = Integer.valueOf(22)
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

        assertEquals 23, Integer.valueOf(10 + 12) + 6  // (10+12) == Integer.valueOf(22) !!!!!!

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
        def FOUR = Integer.valueOf(4)

        Integer.metaClass {
            'static' {
                fib { Number n ->
                    Integer.valueOf(n.fib ())
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
                  return fib(Integer.valueOf(n-1)) + fib(Integer.valueOf(n-2))
            }
        }

        assertEquals( 3, Integer.fib(3))

        FOUR.metaClass {
            fib { ->
                10
            }
        }

        assertEquals( 13, Integer.fib(5))

        FOUR.metaClass {
            fib { ->
                fib(2) + fib(3)
            }
        }

        assertEquals( 8, Integer.fib(5))
        FOUR.metaClass = null
    }

    void testCategory () {
        def FOUR = Integer.valueOf(4)

        Integer.metaClass {
            mixin Fib
        }

        assertEquals (3, 3.fib())

        Integer.metaClass = null

        shouldFail {
            3.fib()
        }

        FOUR.metaClass {
            mixin FibInst
        }
        assertEquals (15, FOUR.fib())
        FOUR.metaClass = null
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
