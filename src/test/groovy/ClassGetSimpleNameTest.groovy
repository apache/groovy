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

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.*

/**
 * This test verifies the behaviour of DGM#getSimpleName(Class) used in Map of closures coercion to classes.
 * The behaviour of DGM#getSimpleName(Class) should be the same as Class#getSimpleName().
 *
 * TODO remove this class when we can use Class#getSimpleName() in DGM#makeSubClass(Map map, Class clazz)
 */
class ClassGetSimpleNameTest extends GroovyTestCase {

    void testPrimitiveTypes() {
        assert "boolean" == getSimpleName(boolean)
        assert "byte"    == getSimpleName(byte)
        assert "char"    == getSimpleName(char)
        assert "double"  == getSimpleName(double)
        assert "float"   == getSimpleName(float)
        assert "int"     == getSimpleName(int)
        assert "long"    == getSimpleName(long)
        assert "short"   == getSimpleName(short)
        assert "void"    == getSimpleName(void)
    }

    void testNormalClasses() {
        assert "java.lang.Class"             == getSimpleName(java.lang.Class)
        assert "java.lang.String"            == getSimpleName(java.lang.String)
        assert "java.lang.Runnable"          == getSimpleName(java.lang.Runnable)
        assert "groovy.SimpleNameDummyClass" == getSimpleName(groovy.SimpleNameDummyClass)
        assert "java.util.Map.Entry"         == getSimpleName(Map.Entry)
    }

    void testArrayOfPrimitives() {
        assert "boolean[]"     == getSimpleName(boolean[])
        assert "byte[][]"      == getSimpleName(byte[][])
        assert "char[]"        == getSimpleName(char[])
        assert "double[][][]"  == getSimpleName(double[][][])
        assert "float[]"       == getSimpleName(float[])
        assert "int[]"         == getSimpleName(int[])
        assert "long[][]"      == getSimpleName(long[][])
        assert "short[][]"     == getSimpleName(short[][])
    }

    void testArrayOfNormalClasses() {
        assert "java.lang.String[]"                == getSimpleName(String[])
        assert "java.lang.Boolean[][]"             == getSimpleName(Boolean[][])
        assert "groovy.SimpleNameDummyClass[][][]" == getSimpleName(groovy.SimpleNameDummyClass[][][])
        assert "java.lang.Integer[][]"             == getSimpleName(java.lang.Integer[][])
    }
}

class SimpleNameDummyClass {}