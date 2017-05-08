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
public class OuterA {
    class InnerB {}
}

class OuterClazz {
    enum InnerEnum implements SomeInterface {
        A, B
    }
}


class AA {
    class Inner {
        public def innerMethod() {}
    }
    private class PrivateInner {}
    protected final class ProtectedFinalInner {
        ProtectedFinalInner() {

        }

        ProtectedFinalInner(Integer a) {
            new A() {
                public int method() {
                    1
                }
            }
        }
    }

    public int method() {
        0
    }
}



interface A {
    static enum B {
        static interface C {
        }
    }
}

interface A2 {
    static class B2 {
        static enum C2 {
        }
    }
}

enum A4 {
    static interface B4 {
        static class C4 {
        }
    }
}


class A3 {
    static class B3 {
        static enum C3 {
        }
    }
}

class A5 {
    static class B5 {
        static class C5 {
        }
    }
}

interface A1 {
    static interface B1 {
        static enum C1 {
        }
    }
}




