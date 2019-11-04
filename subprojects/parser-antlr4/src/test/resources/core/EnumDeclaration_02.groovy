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
enum E {
    A() {}, B(1) {{}},
    C(1, 2) {
        public void prt() {
            println "$x, $y"
        }

        void prt2() {
            println "$x, $y"
        }

        @Test2 prt3() {
            println "$x, $y"
        }

        private void prt4() {
            println "$x, $y"
        }

        private prt5() {
            println "$x, $y"
        }
    },
    D {
        void hello() {}
    }

    protected int x;
    protected int y;

    E() {}
    E(int x) {
        this.x = x;
    }
    E(int x, y) {
        this(x)
        this.y = y;
    }

    void prt() {
        println "123"
    }
}

enum F {
    @Test2
    A
}

enum G implements I<T> {

}

enum J {
    A,
    B,
}

enum K {
    A,
    B,
    ;
}

enum Outer {
    A, B
    enum Inner{X, Y}
}

class TestClass {
    enum OuterEnum {
        VALUE,
        enum InnerEnum {
            A
        }
    }
}
