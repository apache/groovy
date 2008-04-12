/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

import static groovy.util.ProxyGenerator.INSTANCE

/**
 * This test verifies the behaviour of ProxyGenerator#getSimpleName(Class) used in Map of closures coercion to classes.
 * The behaviour of ProxyGenerator#getSimpleName(Class) should be the same as Class#getSimpleName().
 *
 * TODO remove this class when we can use Class#getSimpleName() in ProxyGenerator#instantiateAggregate(Map map, List interfaces, Class clazz)
 */
class ClassGetSimpleNameTest extends GroovyTestCase {

    void testPrimitiveTypes() {
        assert "boolean" == INSTANCE.getSimpleName(boolean)
        assert "byte"    == INSTANCE.getSimpleName(byte)
        assert "char"    == INSTANCE.getSimpleName(char)
        assert "double"  == INSTANCE.getSimpleName(double)
        assert "float"   == INSTANCE.getSimpleName(float)
        assert "int"     == INSTANCE.getSimpleName(int)
        assert "long"    == INSTANCE.getSimpleName(long)
        assert "short"   == INSTANCE.getSimpleName(short)
        assert "void"    == INSTANCE.getSimpleName(void)
    }

    void testNormalClasses() {
        assert "java.lang.Class"             == INSTANCE.getSimpleName(java.lang.Class)
        assert "java.lang.String"            == INSTANCE.getSimpleName(java.lang.String)
        assert "java.lang.Runnable"          == INSTANCE.getSimpleName(java.lang.Runnable)
        assert "groovy.SimpleNameDummyClass" == INSTANCE.getSimpleName(groovy.SimpleNameDummyClass)
        assert "java.util.Map.Entry"         == INSTANCE.getSimpleName(Map.Entry)
    }

    void testArrayOfPrimitives() {
        assert "boolean[]"     == INSTANCE.getSimpleName(boolean[])
        assert "byte[][]"      == INSTANCE.getSimpleName(byte[][])
        assert "char[]"        == INSTANCE.getSimpleName(char[])
        assert "double[][][]"  == INSTANCE.getSimpleName(double[][][])
        assert "float[]"       == INSTANCE.getSimpleName(float[])
        assert "int[]"         == INSTANCE.getSimpleName(int[])
        assert "long[][]"      == INSTANCE.getSimpleName(long[][])
        assert "short[][]"     == INSTANCE.getSimpleName(short[][])
    }

    void testArrayOfNormalClasses() {
        assert "java.lang.String[]"                == INSTANCE.getSimpleName(String[])
        assert "java.lang.Boolean[][]"             == INSTANCE.getSimpleName(Boolean[][])
        assert "groovy.SimpleNameDummyClass[][][]" == INSTANCE.getSimpleName(groovy.SimpleNameDummyClass[][][])
        assert "java.lang.Integer[][]"             == INSTANCE.getSimpleName(java.lang.Integer[][])
    }
}

class SimpleNameDummyClass {}