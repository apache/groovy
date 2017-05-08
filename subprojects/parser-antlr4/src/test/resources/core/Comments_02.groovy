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
/**
 * test class Comments
 */
public class Comments {
    /**
     * test Comments.SOME_VAR
     */
    public static final String SOME_VAR = 'SOME_VAR';
    /**
     * test Comments.SOME_VAR2
     */
    public static final String SOME_VAR2 = 'SOME_VAR2';

    public static final String SOME_VAR3 = 'SOME_VAR3';

    /**
     * test Comments.SOME_VAR4
     */
    // no groovydoc for SOME_VAR4
    public static final String SOME_VAR4 = 'SOME_VAR4';


    /**
     * test Comments.constructor1
     */
    public Comments() {

    }

    /**
     * test Comments.m1
     */
    def m1() {
        // executing m1
    }

    /*
     * test Comments.m2
     */
    private m2() {
        // executing m2
    }

    /**
     * test Comments.m3
     */
    public void m3() {
        // executing m3
    }

    /**
     * test class InnerClazz
     */
    static class InnerClazz {
        /**
         * test InnerClazz.SOME_VAR3
         */
        public static final String SOME_VAR3 = 'SOME_VAR3';
        /**
         * test InnerClazz.SOME_VAR4
         */
        public static final String SOME_VAR4 = 'SOME_VAR4';

        /**
         * test Comments.m4
         */
        public void m4() {
            // executing m4
        }

        /**
         * test Comments.m5
         */
        public void m5() {
            // executing m5
        }
    }

    /**
     * test class InnerEnum
     */
    static enum InnerEnum {
        /**
         * InnerEnum.NEW
         */
        NEW,

        /**
         * InnerEnum.OLD
         */
        OLD
    }

    static enum InnerEnum2 {}
}

/**
 * test class Comments2
 */
class Comments2 {
    /*
     * test Comments.SOME_VAR
     */
    public static final String SOME_VAR = 'SOME_VAR';
}

class Comments3 {}

/**
 * test someScriptMethod1
 */
void someScriptMethod1() {}

/*
 * test someScriptMethod2
 */
void someScriptMethod2() {}