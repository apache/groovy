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

public class Y {
    public class X {
        def name

        public X(String name) {
            this.name = name
        }
    }

    public static Y createY() {
        return new Y()
    }

    public static X createX(Y y) {
        return y.new X('Daniel')
    }

    public static X createX() {
        return createY().new X('Daniel')
    }

    public static String getXName() {
        return createY().new X('Daniel').name
    }

    public static String getXName2() {
        return createY().
                        new X('Daniel')
                                        .name
    }

    public static String getXName3() {
        return createY().
                new X('Daniel')
                                .getName()
    }
    public static String getXName4() {
        return createY()
                .new X('Daniel')
                .getName()
    }
}

assert 'Daniel' == Y.createX(new Y()).name
assert 'Daniel' == Y.createX().name
assert 'Daniel' == Y.getXName()
assert 'Daniel' == Y.getXName2()
assert 'Daniel' == Y.getXName3()
assert 'Daniel' == Y.getXName4()
