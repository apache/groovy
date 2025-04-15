/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package bugs

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy7204 {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports { star 'groovy.transform' }
    }

    @NotYetImplemented @Test
    void testTypeChecked1() {
        assertScript shell, '''
            @TypeChecked
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @TypeChecked
            interface CrudRepository<T, S extends Serializable> {
                void delete(T arg)
                void delete(S arg)
            }

            @TypeChecked
            interface MyRepository extends CrudRepository<String, Long> {
            }

            @TypeChecked
            class MyRepositoryImpl implements MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testTypeChecked2() {
        assertScript shell, '''
            @TypeChecked
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @TypeChecked
            abstract class CrudRepository<T, S extends Serializable> {
                abstract void delete(T arg)
                abstract void delete(S arg)
            }

            @TypeChecked
            abstract class MyRepository extends CrudRepository<String, Long> {
            }

            @TypeChecked
            class MyRepositoryImpl extends MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testTypeChecked3() {
        assertScript shell, '''
            @TypeChecked
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @TypeChecked
            interface CrudRepository<T, S extends Serializable> {
                void delete(T arg)
                void delete(S arg)
            }

            @TypeChecked
            interface MyRepository2 extends CrudRepository<String, Long> {
            }

            @TypeChecked
            interface MyRepository extends MyRepository2 {
            }

            @TypeChecked
            class MyRepositoryImpl implements MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testTypeChecked4() {
        assertScript shell, '''
            @TypeChecked
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @TypeChecked
            abstract class CrudRepository<T, S extends Serializable> {
                abstract void delete(T arg)
                abstract void delete(S arg)
            }

            @TypeChecked
            abstract class MyRepository2 extends CrudRepository<String, Long> {
            }

            @TypeChecked
            abstract class MyRepository extends MyRepository2 {
            }

            @TypeChecked
            class MyRepositoryImpl extends MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testTypeChecked5() {
        assertScript shell, '''
            @TypeChecked
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @TypeChecked
            interface CrudRepository<T, S extends Serializable> {
                void delete(T arg)
                void delete(S arg)
            }

            @TypeChecked
            abstract class MyRepository2 implements CrudRepository<String, Long> {
            }

            @TypeChecked
            abstract class MyRepository extends MyRepository2 {
            }

            @TypeChecked
            class MyRepositoryImpl extends MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testTypeChecked6() {
        assertScript shell, '''
            class Repository<T, S extends Serializable> {
                void delete(T arg) { assert true }
                void delete(S arg) { assert false: 'wrong method invoked' }
            }

            @TypeChecked
            def test() {
                Repository<String, Long> r = new Repository<String, Long>()
                r.delete('foo')
            }

            test()
        '''
    }

    //

    @NotYetImplemented @Test
    void testCompileStatic1() {
        assertScript shell, '''
            @CompileStatic
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @CompileStatic
            interface CrudRepository<T, S extends Serializable> {
                void delete(T arg)
                void delete(S arg)
            }

            @CompileStatic
            interface MyRepository extends CrudRepository<String, Long> {
            }

            @CompileStatic
            class MyRepositoryImpl implements MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testCompileStatic2() {
        assertScript shell, '''
            @CompileStatic
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @CompileStatic
            abstract class CrudRepository<T, S extends Serializable> {
                abstract void delete(T arg)
                abstract void delete(S arg)
            }

            @CompileStatic
            abstract class MyRepository extends CrudRepository<String, Long> {
            }

            @CompileStatic
            class MyRepositoryImpl extends MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testCompileStatic3() {
        assertScript shell, '''
            @CompileStatic
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @CompileStatic
            interface CrudRepository<T, S extends Serializable> {
                void delete(T arg)
                void delete(S arg)
            }

            @CompileStatic
            interface MyRepository2 extends CrudRepository<String, Long> {
            }

            @CompileStatic
            interface MyRepository extends MyRepository2 {
            }

            @CompileStatic
            class MyRepositoryImpl implements MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testCompileStatic4() {
        assertScript shell, '''
            @CompileStatic
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @CompileStatic
            abstract class CrudRepository<T, S extends Serializable> {
                abstract void delete(T arg)
                abstract void delete(S arg)
            }

            @CompileStatic
            abstract class MyRepository2 extends CrudRepository<String, Long> {
            }

            @CompileStatic
            abstract class MyRepository extends MyRepository2 {
            }

            @CompileStatic
            class MyRepositoryImpl extends MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testCompileStatic5() {
        assertScript shell, '''
            @CompileStatic
            public class MyClass {
                static MyRepository factory() {
                    return new MyRepositoryImpl()
                }

                static void main(String[] args) {
                    MyRepository r = factory()
                    r.delete('foo')
                }
            }

            @CompileStatic
            interface CrudRepository<T, S extends Serializable> {
                void delete(T arg)
                void delete(S arg)
            }

            @CompileStatic
            abstract class MyRepository2 implements CrudRepository<String, Long> {
            }

            @CompileStatic
            abstract class MyRepository extends MyRepository2 {
            }

            @CompileStatic
            class MyRepositoryImpl extends MyRepository {
                @Override
                public void delete(String arg) {
                    assert true
                }

                @Override
                public void delete(Long arg) {
                    assert false: 'wrong method invoked'
                }
            }
        '''
    }

    @NotYetImplemented @Test
    void testCompileStatic6() {
        assertScript shell, '''
            class Repository<T, S extends Serializable> {
                void delete(T arg) { assert true }
                void delete(S arg) { assert false: 'wrong method invoked' }
            }

            @CompileStatic
            def test() {
                Repository<String, Long> r = new Repository<String, Long>()
                r.delete('foo')
            }

            test()
        '''
    }

    @NotYetImplemented @Test // GROOVY-8059
    void testCompileStatic7() {
        assertScript shell, '''
            abstract class A<K extends Serializable, V> {
                void delete(K key) {}
                void delete(V val) {}
            }
            class C extends A<String, Integer> {
            }

            @CompileStatic
            class Test {
                Test() {
                    def obj = new C()
                    obj.delete(Integer.valueOf(1))
                }
            }

            new Test()
        '''
    }

    @Test
    void testCompileStatic8() {
        assertScript shell, '''
            class Trie<T> {
            }

            @CompileStatic
            class Base<T> {
                protected List<Trie<T>> list = []
                Base() {
                    list.add(new Trie<String>()) // should fail!!
                }
            }

            @CompileStatic
            class Test extends Base<String> {
                Trie<String> getFirstElement() {
                    list.get(0)
                }
            }

            assert new Test().firstElement instanceof Trie
        '''
    }

    @Test
    void testCompileStatic9() {
        assertScript shell, '''
            class Trie<T> {
            }

            class Base<T> extends ArrayList<Trie<T>> {
            }

            @CompileStatic
            class Test extends Base<String> {
                Test() {
                    add(new Trie<String>())
                }
                Trie<String> getFirstElement() {
                    get(0)
                }
            }

            assert new Test().firstElement instanceof Trie
        '''
    }
}
