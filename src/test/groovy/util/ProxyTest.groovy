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
package groovy.util

import groovy.test.GroovyTestCase

class ProxyTest extends GroovyTestCase {

    void testStringDecoration(){
        def original = 'decorated String'
        def proxy = new StringDecorator().wrap(original)
        // method, that is only known on proxy
        assertSame original, proxy.adaptee
        // method, that is only known on adaptee is relayed through the proxy
        assertEquals original.size(), proxy.size()
        // method, that is availabe in both objects should come from proxy
        assertEquals 0, proxy.length()
        // method, that is availabe in both objects
        // but should come from adaptee needs explicit relay
        assertEquals original, proxy.toString()
        // method from decorator, that is not in adaptee
        assertEquals 'new Method reached', proxy.someNewMethod()
    }

  /*
   *  Some test cases to probe perceived problems with each and collect on Proxy objects.
   *  cf. GROOVY-1461.  Jonathan Carlson <Jonathan.Carlson@katun.com> made a proposal for a test
   *  as a single method, Russel Winder <russel@winder.org.uk> split things up when entering
   *  them so that there is only a single assert per method to try and maximize the benefit of
   *  the tests.
   */

  void testProxyCollect ( ) {
    def collection = [ 1 , 2 , 3 ]
    def proxy = ( new Proxy ( ) ).wrap ( collection ) 
    assertEquals ( [ 2 , 3 , 4 ] , proxy.collect { it + 1 } )
  }

  void testProxyAny ( ) {
    def collection = [ 1 , 2 , 3 ]
    def proxy = ( new Proxy ( ) ).wrap ( collection ) 
    assertEquals ( true , proxy.any { it == 2 } )
  }

  void testProxyFind ( ) {
    def collection = [ 1 , 2 , 3 ]
    def proxy = ( new Proxy ( ) ).wrap ( collection ) 
    assertEquals ( 2 , proxy.find { it == 2 } )
  }

  void testProxyEach ( ) {
    def collection = [ 1 , 2 , 3 ]
    def proxy = ( new Proxy ( ) ).wrap ( collection ) 
    def testString = ''
    proxy.each { testString += it }
    assertEquals ( '123' , testString )
  }
  
  void testMultipleWrapping() {
    assertScript """
        import groovy.util.Proxy
        
        class Proxy1 extends Proxy {
            def foo() { "Foo" }
        }
        
        class Proxy2 extends Proxy {
            def bar() { "Bar" }
        }
        
        class Obj {
            def baz() { "Baz" }
        }
        
        def proxy1 = new Proxy1()
        def proxy2 = new Proxy2()
        proxy1.adaptee = proxy2
        proxy2.adaptee = new Obj()
        
        assert proxy1.foo() == "Foo"
        assert proxy1.bar() == "Bar"
    """
  }

}

class StringDecorator extends Proxy{
    int length()          { 0 }
    String toString()     { adaptee.toString()}
    String someNewMethod(){ 'new Method reached' }
}
