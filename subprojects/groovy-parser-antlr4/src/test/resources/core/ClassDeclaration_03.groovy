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
import java.sql.SQLException

class AAA {
    private volatile XX xx;
    private transient YY yy;

    public AAA() {

    }

    public AAA(String name) {

    }

    @Test2
    public AAA(String name, int age) throws Exception {

    }

    AAA(String name, int age, String title) throws Exception {

    }

    private AAA(String name, int age, String title, double income) throws Exception {

    }

    @Test2
    public synchronized String sayHello(String name) {
        return "Hello, $name";
    }

    @Test2
    public <T> T sayHello2(T name) throws IOException, SQLException {
        return "Hello, $name";
    }

    public static privateStaticMethod(){}

    public void m(final int param) {}
    public void m2(def param) {}
    public void m3(final int param1, long param2, final String param3) {}

    def "hello world"(p1, p2) {
        println "$p1, $p2"
    }

    def run() {
        this."hello world"('ab', 'bc')
    }
}
