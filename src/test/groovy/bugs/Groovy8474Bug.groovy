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

import groovy.test.GroovyTestCase

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

    void testSettingSuperProperty4() {
        assertScript '''
            class K {
              private String name
              public String getName() {
                name
              }
              public void setName(String name) {
                this.name = name
              }
            }
            class T extends K {
              String group
            }
            class S extends T {
              S() {
                super.group = 'Hello'
                super.name = 'World'
              }
              
              public String helloWorld() {
                "$group, $name"
              }
            }
            
            assert 'Hello, World' == new S().helloWorld()
        '''
    }

    void testSettingSuperProperty5() {
        assertScript '''
            class T {
              Integer group
            }
            
            class S extends T {
              S() {
                super.group = 1
              }
            }
            
            assert 1 == new S().group
        '''
    }

    void testSettingSuperProperty6() {
        assertScript '''
            class T {
              Long group
            }
            
            class S extends T {
              S() {
                super.group = 1
              }
            }
            
            assert 1 == new S().group
        '''
    }

    void testSettingSuperProperty7() {
        assertScript '''
            class T {
              Long group
            }
            
            class S extends T {
              S() {
                super.group = Long.MAX_VALUE
              }
            }
            
            assert Long.MAX_VALUE == new S().group
        '''
    }

    void testSettingSuperProperty8() {
        assertScript '''
            class T {
              int group
            }
            
            class S extends T {
              S() {
                super.group = Integer.MAX_VALUE
              }
            }
            
            assert Integer.MAX_VALUE == new S().group
        '''
    }

    void testSettingSuperProperty9() {
        assertScript '''
            class T {
              long group
            }
            
            class S extends T {
              S() {
                super.group = Long.MAX_VALUE
              }
            }
            
            assert Long.MAX_VALUE == new S().group
        '''
    }

    void testSettingSuperProperty10() {
        assertScript '''
            class T {
              int group
            }
            
            class S extends T {
              S() {
                super.group = 1
              }
            }
            
            assert 1 == new S().group
        '''
    }

    void testSettingSuperProperty11() {
        assertScript '''
            class T {
              long group
            }
            
            class S extends T {
              S() {
                super.group = 123456789123456789
              }
            }
            
            assert 123456789123456789 == new S().group
        '''
    }

    void testSettingSuperProperty12() {
        assertScript '''
            class T {
              boolean group
            }
            
            class S extends T {
              S() {
                super.group = true
              }
            }
            
            assert true == new S().group
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

    void testSettingSuperPrivateProperty2() {
        def errMsg = shouldFail '''
            class T {
              private String group
              
              public String getGroup() {
                return group
              }
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
        '''

        assert errMsg.contains('Cannot access private field')
    }

    void testSettingSuperPrivateProperty3() {
        def errMsg = shouldFail '''
            class T {
              private String group
              
              public void setGroup(String group) {
                this.group = group
              }
            }
            
            class S extends T {
              S() {
                super.group = 'Hello'
              }
            }
        '''

        assert errMsg.contains('Cannot access private field')
    }

    void testSettingSuperPrivateProperty4() {
        def errMsg = shouldFail '''
            class K {
              private String group
              
              public void setGroup(String group) {
                this.group = group
              }
            }
            class T extends K {
              public String getGroup() {
                return group
              }
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
