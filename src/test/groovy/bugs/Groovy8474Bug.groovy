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
package groovy.bugs

class Groovy8474Bug extends GroovyTestCase {
    void testSettingSuperProperty() {
        assertScript '''
            class T {
              String group
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
            
            assert 'Hello' == new S().group
        '''
    }

    void testSettingSuperProperty2() {
        assertScript '''
            class T {
              String group
              String group2
              String group3
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
                super.group2 = 'Hello2'
                super.group3 = 'Hello3'
              }
            }
            
            assert 'Hello' == new S().group
            assert 'Hello2' == new S().group2
            assert 'Hello3' == new S().group3
        '''
    }

    void testSettingSuperProperty3() {
        assertScript '''
            class K {
                String group
            }
            class T extends K {
              
            }
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
            
            assert 'Hello' == new S().group
        '''
    }

    void testSettingSuperProtectedField() {
        assertScript '''
            class T {
              protected String group
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
            
            assert 'Hello' == new S().group
        '''
    }

    void testSettingSuperProtectedField2() {
        assertScript '''
            class T {
              protected String group
              protected String group2
              protected String group3
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
                super.group2 = 'Hello2'
                super.group3 = 'Hello3'
              }
            }
            
            assert 'Hello' == new S().group
            assert 'Hello2' == new S().group2
            assert 'Hello3' == new S().group3
        '''
    }

    void testSettingSuperProtectedField3() {
        assertScript '''
            class K {
                protected String group
            }
            class T extends K {
              
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
            
            assert 'Hello' == new S().group
        '''
    }

    void testSettingSuperPublicField() {
        assertScript '''
            class T {
              public String group
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
            
            assert 'Hello' == new S().group
        '''
    }

    void testSettingSuperPublicField2() {
        assertScript '''
            class T {
              public String group
              public String group2
              public String group3
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
                super.group2 = 'Hello2'
                super.group3 = 'Hello3'
              }
            }
            
            assert 'Hello' == new S().group
            assert 'Hello2' == new S().group2
            assert 'Hello3' == new S().group3
        '''
    }

    void testSettingSuperPublicField3() {
        assertScript '''
            class K {
                public String group
            }
            class T extends K {
              
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
            
            assert 'Hello' == new S().group
        '''
    }

    void testSettingSuperPrivateProperty() {
        def errMsg = shouldFail '''
            class T {
              private String group
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
        '''
        assert errMsg.contains('Cannot access private field')
    }

    void testSettingSuperFinalProperty() {
        shouldFail '''
            class T {
              protected final String group = 'Hi'
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
        '''
    }
}
