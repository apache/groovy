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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy7722 {

    @Test
    void testGenericsStackOverflow1() {
        assertScript '''
            interface Action1<T> {
                void call(T t)
            }
            abstract class Subscriber<T> {
            }
            interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {
            }

            new OnSubscribe<List>() {
                @Override
                void call(Subscriber<? super List> o) {
                    println 'called'
                }
            }.call(null)
        '''
    }

    @Test // GROOVY-7864
    void testGenericsStackOverflow2() {
        assertScript '''
            // from RxJava 1.x
            class Observable<T> {
                interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {
                }
                static <T> Observable<T> create(OnSubscribe<T> f) {
                    return new Observable<T>(/*RxJavaHooks.onCreate(f)*/);
                }
            }
            abstract class Subscriber<T> implements Observer<T>, Subscription {
            }
            interface Action1<T> /*extends Action*/ {
                void call(T t)
            }
            interface Observer<T> {
                void onNext(T t)
                void onCompleted()
                void onError(Throwable t)
            }
            public interface Subscription {
                void unsubscribe()
                boolean isUnsubscribed()
            }

            Observable.create(new Observable.OnSubscribe() {
                @Override
                void call(Subscriber subscriber) {
                    //...
                }
            })
        '''
    }
}
