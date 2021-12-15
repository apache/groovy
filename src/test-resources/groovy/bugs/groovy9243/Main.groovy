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

final class Groovy9243 extends Base {
    def accessX1() {
        String name = new X().name
        assert name =='staticClassX'
    }
    def accessX2() {
        String name = new Base.X().name
        assert name == 'staticClassX'
    }
    def accessX3() {
        String name = new groovy.bugs.groovy9243.Base.X().name
        assert name == 'staticClassX'
    }
    def accessY1() {
        String name = new Y().name
        assert name == 'classY'
    }
    def accessY2() {
        String name = this.new Y().name
        assert name == 'classY'
    }
    def accessY3() {
        def that = new Groovy9243()
        String name = that.new Y().name
        assert name == 'classY'
    }
    /*def accessY4() {
        String name = new Groovy9243().new Y().name
        assert name == 'classY'
    }*/
}

new Groovy9243().with {
    accessX1()
    accessX2()
    accessX3()
    accessY1()
    accessY2()
    accessY3()
}
