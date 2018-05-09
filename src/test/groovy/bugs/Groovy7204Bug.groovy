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

class Groovy7204Bug extends GroovyTestCase {
    void test1() {
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

    void test2() {
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

}
