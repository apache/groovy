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
package groovy.bugs.groovy9243

class Groovy9243 extends Base {
    def accessX() {
        assert 'staticClassX' == new X().name
    }
    def accessBaseX() {
        assert 'staticClassX' == new Base.X().name
    }
    def accessBaseX2() {
        assert 'staticClassX' == new groovy.bugs.groovy9243.Base.X().name
    }
    def accessY() {
        assert 'classY' == this.new Y().name
    }
    def accessY2() {
        def g = new Groovy9243()
        assert 'classY' == g.new Y().name
    }
    def accessY3() {
        assert 'classY' == new Groovy9243().new Y().name
    }
}

def g = new Groovy9243()
g.accessX()
g.accessBaseX()
g.accessBaseX2()
g.accessY()
g.accessY2()
g.accessY3()
