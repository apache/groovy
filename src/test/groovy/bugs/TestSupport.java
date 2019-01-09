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
package groovy.bugs;

import groovy.util.GroovyTestCase;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Base class for test cases
 */
@Ignore("base class for tests")
public abstract class TestSupport extends GroovyTestCase {

    public String[] getMockArguments() {
        return new String[]{"a", "b", "c"};
    }

    public static String mockStaticMethod() {
        return "cheese";
    }

    public static String getMockStaticProperty() {
        return "cheese";
    }

    public static int[] getIntArray() {
        return new int[]{1, 2, 3, 4, 5};
    }

    public Iterator iterator() {
        return Arrays.asList(getMockArguments()).iterator();
    }
}
