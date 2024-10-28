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

@Test2
enum E1 {
    A,
    B,C,
    D
    private void proc() {}
    def prop;
}

enum E2 {
    A(1), B(2)
    
    public static final String SOME_CONSTANT = '123';
    private final String name;
    private final int age = 2;
    final String title='title'
    public E2(int x) {}
}

enum E3 {
    A, B
    enum E4{X, Y}
}

class C {
    enum E5 {
        A
        enum E6 {
            B
        }
    }
}

enum E7 {
    A(1), B(2)
    E7(value) {
        this.value = value
    }
    private final int value
}

enum E8 {
    @Test2 A(1), B(2)
    E8(value) {
        this.value = value
    }
    private final int value
}

enum E9 {
    A(1), B(2)
    Object value // different parsing without leading keyword
    E9(value) {
        this.value = value
    }
}

// GROOVY-9301
enum E10 {
    X(1), Y(2), // trailing comma
    ;
    Object value
    E10(value) {
        this.value = value
    }
}

enum E11 {
    X, Y, Z
    def m1() { }
    public m2(args) { }
    int m3(String arg) { }
}

enum E12 {
    X, Y, Z
    def <T> T m() { }
}

enum E13 {
    X, Y, Z
    final <T> T m() { }
}

enum E14 {
    X, Y, Z
    public <T> T m() { }
}

enum E15 {
    X { double eval(int v) { return (double) v } },
    Y {
        double eval(int v) { return (double) v + 1 }
    }, Z
}

enum E16 {
    X, Y, Z
    class C { }
}

// GROOVY-8507
enum E17 {
    X, Y, Z, // trailing comma
    ;
    class C { }
}

enum E18 {
    X, Y, Z
    enum E2 { A, B, C }
}

// GROOVY-8507
enum E19 {
    X, Y, Z, // trailing comma
    ;
    enum Another { A, B, C }
}

enum E20 {
    X, Y, Z
    interface I { }
}

// GROOVY-8507
enum E21 {
    X, Y, Z, // trailing comma
    ;
    interface I { }
}

enum E22 {
    X, Y, Z
    @interface A { }
}

enum E23 {
    X, Y, Z, // trailing comma
    ;
    @interface A { }
}

enum E24 {
    X, Y, Z
    trait T { }
}

enum E25 {
    X, Y, Z;
    trait T { }
}

enum Color {
    RED, BLACK
}
enum Suit {
    CLUBS(Color.BLACK),
    DIAMONDS(Color.RED),
    HEARTS(Color.RED),
    SPADES(Color.BLACK), // trailing comma
    ;
    final Color color
    Suit(Color color) {
        this.color = color
    }
}

enum Orientation1 {
    LANDSCAPE, PORTRAIT
    
    @Override
    String toString() {
        name().toLowerCase().capitalize()
    }
}

// GROOVY-9306
enum Orientation2 {
    LANDSCAPE, PORTRAIT, // trailing comma
    ;
    @Override
    String toString() {
        name().toLowerCase().capitalize()
    }
}

// GROOVY-9306
enum Orientation3 {
    LANDSCAPE, PORTRAIT, // trailing comma
    ;
    @Deprecated <T> T whatever() { }
}
