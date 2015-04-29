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

package org.codehaus.groovy.control.messages;

import junit.framework.TestCase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;

public class SyntaxErrorMessageTest extends TestCase {

    public void testSetsTheSourceLocatorOfItsSyntaxExceptionAsTheNameOfTheCorrespondingSourceUnitWhenInstantiated() {
        SyntaxException syntaxException = new SyntaxException(someString(), -1, -1);
        assertEquals("source locator", null, syntaxException.getSourceLocator());

        String sourceUnitName = someString();
        SourceUnit sourceUnit = SourceUnit.create(sourceUnitName, someString());

        new SyntaxErrorMessage(syntaxException, sourceUnit);
        assertEquals("source locator", sourceUnitName, syntaxException.getSourceLocator());
    }

    private String someString() {
        return String.valueOf(Math.random() * System.currentTimeMillis());
    }
}
