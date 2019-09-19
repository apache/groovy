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

import groovy.test.NotYetImplemented
import groovy.transform.stc.StaticTypeCheckingTestCase

class Groovy6786Bug extends StaticTypeCheckingTestCase {
    
    void testGenericAddAll() {
        assertScript '''

            public class Class1<ENTITY> {
            
                Container<ENTITY> container;
            
                void refresh() {
                    def items = findAllItems();
                    
                    container.addAll(items);
                }
            
                Collection<ENTITY> findAllItems() {
                    return null;
                }
            }
            interface Container<ENTITY> {
                void addAll(Collection<? extends ENTITY> collection);
            }
            new Class1()

        '''
    }

    void testGuavaCacheBuilderLikeGenerics() {
        assertScript '''
            class Class1 {
            
                protected LoadingCache<String, Integer> cache;
            
                Class1(CacheLoader<String, Integer> cacheLoader) {
                    this.cache = CacheBuilder.newBuilder().build(cacheLoader);
                }
            }
            
            class CacheLoader<K, V> {}
            
            class LoadingCache<K, V> {}
            
            class CacheBuilder<K, V> {
                public static CacheBuilder<Object, Object> newBuilder() {
                    return new CacheBuilder<Object, Object>();
                }
            
                public <K1 extends K, V1 extends V> LoadingCache<K1, V1> build(CacheLoader<? super K1, V1> loader) {
                    return new LoadingCache<K1, V1>();  
                }
            }
            new Class1(null)
        '''
    }

    void testOverrideGenericMethod() {
        assertScript '''
            abstract class AbstractManager <T> {
                protected boolean update(T item) {
                    return false;
                }
            }
            
            abstract class AbstractExtendedManager<T> extends AbstractManager<T> {}
                        
            class ConcreteManager extends AbstractExtendedManager<String> {
                @Override
                 protected boolean update(String item){
                    return super.update(item);
                }
            }
            new ConcreteManager();
        '''
    }

    void testIfWithInstanceOfAndAnotherConditionAfter() {
        assertScript '''
            class Class1 {
            
            
                Class1() {
                    Object obj = null;
                    
                    if(obj instanceof String && someCondition() && testMethod(obj)) {
                        println "Hello!"
                    }
                }
                
                boolean someCondition() {
                    return true;
                }
                
                boolean testMethod(String arg) {
                    return true;
                }
            }
            new Class1();
        '''
    }
    
}
