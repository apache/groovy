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
package core

import java.sql.SQLException

public interface AA1 {
        int a;
        long b;
        double c;
        char d;
        short e;
        byte f;
        float g;
        boolean h;
        String i;

        public static final NAME = "AA1"

        @Test3
        public static final NAME2 = "AA1"

        void sayHello();
        abstract void sayHello2();
        public void sayHello3();
        public abstract void sayHello4();
        @Test2
        public abstract void sayHello5();

        @Test2
        public abstract void sayHello6() throws IOException, SQLException;

        @Test2
        @Test3
        public abstract <T> T sayHello7() throws IOException, SQLException;

        @Test2
        @Test3
        public abstract <T extends A> T sayHello8() throws IOException, SQLException;

        @Test2
        @Test3
        public abstract <T extends A & B> T sayHello9() throws IOException, SQLException;
}
