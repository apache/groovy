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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy7204Bug extends GroovyTestCase {
    void testTypeChecked1() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            void delete(T arg);
            void delete(S arg);
        }
        
        @TypeChecked
        interface MyRepository extends CrudRepository<String, Long> {
        }
        
        @TypeChecked
        class MyRepositoryImpl implements MyRepository {
            @Override
            public void delete(String arg) {
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testTypeChecked2() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            abstract void delete(T arg);
            abstract void delete(S arg);
        }
        
        @TypeChecked
        abstract class MyRepository extends CrudRepository<String, Long> {
        }
        
        @TypeChecked
        class MyRepositoryImpl extends MyRepository {
            @Override
            public void delete(String arg) {
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testTypeChecked3() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            void delete(T arg);
            void delete(S arg);
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
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testTypeChecked4() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            abstract void delete(T arg);
            abstract void delete(S arg);
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
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testTypeChecked5() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            void delete(T arg);
            void delete(S arg);
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
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }


    void testCompileStatic1() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            void delete(T arg);
            void delete(S arg);
        }
        
        @CompileStatic
        interface MyRepository extends CrudRepository<String, Long> {
        }
        
        @CompileStatic
        class MyRepositoryImpl implements MyRepository {
            @Override
            public void delete(String arg) {
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testCompileStatic2() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            abstract void delete(T arg);
            abstract void delete(S arg);
        }
        
        @CompileStatic
        abstract class MyRepository extends CrudRepository<String, Long> {
        }
        
        @CompileStatic
        class MyRepositoryImpl extends MyRepository {
            @Override
            public void delete(String arg) {
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testCompileStatic3() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            void delete(T arg);
            void delete(S arg);
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
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testCompileStatic4() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            abstract void delete(T arg);
            abstract void delete(S arg);
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
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testCompileStatic5() {
        assertScript '''
        import java.io.Serializable;
        
        import groovy.transform.CompileStatic;
        import groovy.transform.TypeChecked;
        
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
            void delete(T arg);
            void delete(S arg);
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
                System.out.println("String");
                assert true
            }
            
            @Override
            public void delete(Long arg) {
                System.out.println("Long");
                assert false: 'wrong method invoked'
            }
        }
        '''
    }

    void testCompileStatic6() {
        assertScript '''
        import java.io.Serializable;
        import groovy.transform.CompileStatic;

        @CompileStatic
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

    void testCompileStatic7() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Trie<T> {}
        
        @groovy.transform.CompileStatic
        class Base<T> {
            protected List<Trie<T>> list
            
            Base() {
                list = new ArrayList<Trie<T>>()
                list.add(new Trie<String>())
            }
        }
        
        @groovy.transform.CompileStatic
        class Derived extends Base<String> {
            Trie<String> getFirstElement() {
                list.get(0)
            }
        }
        
        assert new Derived().getFirstElement() instanceof Trie
        '''
    }

    void testCompileStatic8() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Trie<T> {}
        
        @groovy.transform.CompileStatic
        class Base<T> extends ArrayList<Trie<T>> {
            
            Base() {
                this.add(new Trie<String>())
            }
        }
        
        @groovy.transform.CompileStatic
        class Derived extends Base<String> {
            Trie<String> getFirstElement() {
                this.get(0)
            }
        }
        
        assert new Derived().getFirstElement() instanceof Trie
        '''
    }
}
