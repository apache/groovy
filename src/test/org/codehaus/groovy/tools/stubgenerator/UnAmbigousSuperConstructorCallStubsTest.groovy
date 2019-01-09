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
package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-4400 - call to super class Exception constructor should be rendered as 
 * "super ((java.lang.Throwable)null);" and not as "super (null);"
 */
class UnAmbigousSuperConstructorCallStubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'UserAuthException.groovy': '''
                public class UserAuthException extends java.lang.Exception {
                  private static String message = "User Authentication Failed";
                
                  public UserAuthException() {
                    super(message);
                  }
                
                  public String getError() {
                    return message;
                  }
                
                }
            ''',
            'Dummy.java': 'interface Dummy{}'
        ]
    }

    void verifyStubs() {
        assert classes['UserAuthException'] != null
        String stubSource = stubJavaSourceFor('UserAuthException')
        assert !stubSource.contains('super (null)')
    }
}
