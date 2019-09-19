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
package groovy.util

import groovy.test.GroovyTestCase

/**
 * Tests for the ConfigSlurper class
 *
 * @since 0.6
 */
class ConfigSlurperTest extends GroovyTestCase {
    void testConsecutiveSlurperValues() {
        def config = new ConfigSlurper().parse('''
grails.views.default.codec="none"
grails {
    mail {
        host = "smtp.foo.com"
        port = 25
        username = "foo"
        password = "bar"
    }
}
''')


        assertEquals "none", config.grails.views.default.codec
        assertEquals "smtp.foo.com", config.grails.mail.host
    }

    void testConfigSlurperNestedValues() {
        def config = new ConfigSlurper().parse('''
foo {
    bar {
        password="value"
    }
    fruit="orange"
}
''')

        assertEquals "value", config.foo.bar.password
        assertEquals "orange", config.foo.fruit
        config = new ConfigSlurper().parse('''
            foo {
                bar.password="value"
                fruit="orange"
            }
            ''')

        assertEquals "value", config.foo.bar.password
        assertEquals "orange", config.foo.fruit

    }

    void testConfigBinding() {
        def slurper = new ConfigSlurper()
        slurper.binding = [foo: "bar"]
        def config = slurper.parse('''
test=foo + 1        
        ''')
        assertEquals "bar1", config.test

    }

    void testEnvironmentProperties2() {
        def config = new ConfigSlurper("production").parse('''
dataSource {
    pooling = false
    driverClassName = "org.hsqldb.jdbc.JDBCDriver"
    username = "sa"
    password = ""
}
environments {
    development {
        dataSource {
            dbCreate = "create-drop"
            url = "jdbc:hsqldb:mem:devDB"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:hsqldb:mem:testDB"
        }
    }
    production {
        dataSource {
            password = "secret"
            dbCreate = "update"
            url = "jdbc:hsqldb:file:prodDb;shutdown=true"
        }
    }
}''')

        assertEquals false, config.dataSource.pooling
        assertEquals "org.hsqldb.jdbc.JDBCDriver", config.dataSource.driverClassName
        assertEquals "sa", config.dataSource.username
        assertEquals "secret", config.dataSource.password
        assertEquals "update", config.dataSource.dbCreate
        assertEquals "jdbc:hsqldb:file:prodDb;shutdown=true", config.dataSource.url
    }

    void testParseProperties() {
        Properties props = new Properties()
        props['foo'] = 'bar'
        props['log4j.appender.NULL'] = 'org.apache.log4j.varia.NullAppender'
        props['log4j.rootLogger'] = 'error, NULL'
        props['log4j.logger.org.codehaus.groovy.grails.plugins'] = 'info,NULL'
        props['log4j.additivity.org.codehaus.groovy.grails.plugins'] = 'false'
        props['log4j.additivity.org.springframework'] = 'false'
        props['log4j.logger.grails.spring'] = 'info,NULL'
        props['log4j.appender.NULL.layout'] = 'org.apache.log4j.PatternLayout'

        def config = new ConfigSlurper().parse(props)

        assertEquals "org.apache.log4j.PatternLayout", config.log4j.appender.'NULL.layout' // tests overlapping properties
        assertEquals "org.apache.log4j.varia.NullAppender", config.log4j.appender.NULL // tests overlapping properties
        assertEquals 'error, NULL', config.log4j.rootLogger
        assertEquals 'info,NULL', config.log4j.logger.org.codehaus.groovy.grails.plugins
        assertEquals 'false', config.log4j.additivity.org.springframework
        assertEquals 'bar', config.foo
    }

    void testSimpleProperties() {
        def slurper = new ConfigSlurper()

        def config = slurper.parse('''
smtp.server.url = "localhost"
smtp.username = "fred"
smtp.dummy = null
''')

        assert config
        assertEquals "localhost", config.smtp.server.url
        assertEquals "fred", config.smtp.username
        assertNull config.smtp.dummy
    }

    void testScopedProperties() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
    smtp {
        mail.host = 'smtp.myisp.com'
        mail.auth.user = 'server'
    }
    resources.URL = "http://localhost:80/resources"
        ''')

        assert config
        assertEquals "smtp.myisp.com", config.smtp.mail.host
        assertEquals "server", config.smtp.mail.auth.user
        assertEquals "http://localhost:80/resources", config.resources.URL
    }

    void testScopedPropertiesWithNesting() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
    smtp {
        mail {
            host = 'smtp.myisp.com'
            auth.user = 'server'
        }
    }
    resources.URL = "http://localhost:80/resources"
        ''')

        assert config
        assert "smtp.myisp.com" == config.smtp.mail.host
        assertEquals "server", config.smtp.mail.auth.user
        assertEquals "http://localhost:80/resources", config.resources.URL
    }

    void testScopedVariableReusage() {
        def conf = '''
          a0 = "Goofy"
          a1 = "$a0"
          a2."$a0" = "Mickey Mouse and " + "$a0"
          group1 { a0 = "Donald Duck" }
          group2 { 
              a0 = a0
              a1 = "$group1.a0"
              group3 {
                  a0 = "inner$a0"
                  group4 {
                      a0 = "inner$a0"
                      a1 = a1
                      a3 = "Dagobert Duck"
                  }
              }
              a3 = a3
          }
          a3 = "$group1.a0"
        '''

        def config = new ConfigSlurper().parse(conf)
        assert config.group1.a0 == "Donald Duck"
        assert config.group2.a0 == "Goofy"
        assert config.group2.group3.a0 == "innerGoofy"
        assert config.group2.group3.group4.a0 == "innerinnerGoofy"
        assert config.group2.group3.group4.a1 == "Donald Duck"
        assert config.group2.group3.group4.a3 == "Dagobert Duck"
        assert config.group2.a3 == [:]
        assert config.a3 == "Donald Duck"
    }

    void testLog4jConfiguration() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
log4j {
    appender {
        stdout("org.apache.log4j.ConsoleAppender") {
            layout="org.apache.log4j.PatternLayout"
        }                
    }
    rootLogger="error,stdout"
    logger {
        org.codehaus.groovy.grails="info,stdout"
        org.springframework="info,stdout"
    }
    additivity {
        org.codehaus.groovy.grails=false
        org.springframework=false
    }
}
        ''')

        assert config

        //println config

        assertEquals "org.apache.log4j.ConsoleAppender", config.log4j.appender.stdout
        assertEquals "org.apache.log4j.PatternLayout", config.log4j.appender."stdout.layout"
        assertEquals "error,stdout", config.log4j.rootLogger
        assertEquals "info,stdout", config.log4j.logger.org.codehaus.groovy.grails
        assertEquals false, config.log4j.additivity.org.codehaus.groovy.grails
    }

    void testEnvironmentSpecificConfig() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
log4j {
    appender {
        stdout("org.apache.log4j.ConsoleAppender") {
            layout="org.apache.log4j.PatternLayout"
        }        
    }
    rootLogger="error,stdout"
    logger {
        org.codehaus.groovy.grails="info,stdout"
        org.springframework="info,stdout"
    }
    additivity {
        org.codehaus.groovy.grails=false
        org.springframework=false
    }
}
environments {
    development {
        log4j.logger.org.codehaus.groovy.grails="debug,stdout"
    }
}
        ''')

        assert config

        assertEquals "org.apache.log4j.ConsoleAppender", config.log4j.appender.stdout
        assertEquals "org.apache.log4j.PatternLayout", config.log4j.appender."stdout.layout"
        assertEquals "error,stdout", config.log4j.rootLogger
        assertEquals "info,stdout", config.log4j.logger.org.codehaus.groovy.grails
        assertEquals false, config.log4j.additivity.org.codehaus.groovy.grails

        slurper.setEnvironment("development")
        config = slurper.parse('''
log4j {
    appender {
        stdout("org.apache.log4j.ConsoleAppender") {        
            layout="org.apache.log4j.PatternLayout"
        }
    }
    rootLogger="error,stdout"
    logger {
        org.codehaus.groovy.grails="info,stdout"
        org.springframework="info,stdout"
    }
    additivity {
        org.codehaus.groovy.grails=false
        org.springframework=false
    }
}
environments {
    development {
        log4j.logger.org.codehaus.groovy.grails="debug,stdout"
        log4j.appender.layout="MyLayout"
    }
    production {
        log4j.appender.stdout="MyRobustFileAppender"
    }
}
        ''')

        assert config

        assertEquals "org.apache.log4j.ConsoleAppender", config.log4j.appender.stdout
        assertEquals "MyLayout", config.log4j.appender.layout
        assertEquals "error,stdout", config.log4j.rootLogger
        assertEquals "debug,stdout", config.log4j.logger.org.codehaus.groovy.grails
        assertEquals false, config.log4j.additivity.org.codehaus.groovy.grails
    }

    void testFlattenConfig() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
log4j {
    appender {
        stdout("org.apache.log4j.ConsoleAppender") {        
            layout="org.apache.log4j.PatternLayout"
        }
    }
    rootLogger="error,stdout"
    logger {
        org.codehaus.groovy.grails="info,stdout"
        org.springframework="info,stdout"
    }
    additivity {
        org.codehaus.groovy.grails=false
        org.springframework=false
    }
}
        ''')

        config = config.flatten()

        assertEquals "org.apache.log4j.ConsoleAppender", config."log4j.appender.stdout"
        assertEquals "org.apache.log4j.PatternLayout", config."log4j.appender.stdout.layout"
        assertEquals "error,stdout", config."log4j.rootLogger"
        assertEquals "info,stdout", config."log4j.logger.org.codehaus.groovy.grails"
        assertEquals false, config."log4j.additivity.org.codehaus.groovy.grails"
    }


    void testToProperties() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
smtp.dummy = null
log4j {
    appender {
        stdout("org.apache.log4j.ConsoleAppender") {
           layout="org.apache.log4j.PatternLayout"
        }
    }
    rootLogger="error,stdout"
    logger {
        org.codehaus.groovy.grails="info,stdout"
        org.springframework="info,stdout"
    }
    additivity {
        org.codehaus.groovy.grails=false
        org.springframework=false
    }
}
        ''')

        def props = config.toProperties()
        assert props

        assertEquals "org.apache.log4j.ConsoleAppender", props."log4j.appender.stdout"
        assertEquals "org.apache.log4j.PatternLayout", props."log4j.appender.stdout.layout"
        assertEquals "error,stdout", props."log4j.rootLogger"
        assertEquals "info,stdout", props."log4j.logger.org.codehaus.groovy.grails"
        assertEquals "false", props."log4j.additivity.org.codehaus.groovy.grails"
        assertNull props.'smtp.dummy'

        props = config.log4j.toProperties("log4j")
        assertEquals "org.apache.log4j.ConsoleAppender", props."log4j.appender.stdout"
        assertEquals "org.apache.log4j.PatternLayout", props."log4j.appender.stdout.layout"
        assertEquals "error,stdout", props."log4j.rootLogger"
        assertEquals "info,stdout", props."log4j.logger.org.codehaus.groovy.grails"
        assertEquals "false", props."log4j.additivity.org.codehaus.groovy.grails"
        assertFalse props.containsKey('smtp.dummy')
    }

    void testConfigTokensAsStrings() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
log4j {
    appender.stdout = "org.apache.log4j.ConsoleAppender"
    appender."stdout.layout"="org.apache.log4j.PatternLayout"
    rootLogger="error,stdout"    
}
        ''')

        assert config
        assertEquals "org.apache.log4j.ConsoleAppender", config.log4j.appender.stdout
        assertEquals "org.apache.log4j.PatternLayout", config.log4j.appender."stdout.layout"
        assertEquals "error,stdout", config.log4j.rootLogger
    }

    void testConfigInterReferencing() {
        def slurper = new ConfigSlurper()
        def config = slurper.parse('''
            var.one=5
            var.two=var.one*2
        ''')

        assertEquals 5, config.var.one
        assertEquals 10, config.var.two
    }

    void testSerializeConfig() {
        def text = '''
log4j {
    appender.stdout="org.apache.log4j.ConsoleAppender"
    appender.'stdout.layout'="org.apache.log4j.PatternLayout"        
    rootLogger="error,stdout"
    logger {
        org.codehaus.groovy.grails="info,stdout"
        org.springframework="info,stdout"
    }
     
    additivity.'default' = true
    additivity.org.codehaus.groovy.grails=false
    additivity.org.springframework=false
}'''
        def slurper = new ConfigSlurper()
        def config = slurper.parse(text)

        assert config

        def sw = new StringWriter()

        config.writeTo(sw)

        def newText = sw.toString()

        //println newText

        config = slurper.parse(newText)

        assertEquals "org.apache.log4j.ConsoleAppender", config.log4j.appender.stdout
        assertEquals "org.apache.log4j.PatternLayout", config.log4j.appender."stdout.layout"
        assertEquals "error,stdout", config.log4j.rootLogger
        assertEquals "info,stdout", config.log4j.logger.org.codehaus.groovy.grails
        assertEquals false, config.log4j.additivity.org.codehaus.groovy.grails
    }

    void testCloneConfig() {
        ConfigObject original = new ConfigSlurper().parse('foo { bar = "barValue" }')
        ConfigObject clone = original.clone()

        assert clone.foo.bar == "barValue"
    }

    void testNotProperlyNestedPropertiesArePreserved() throws IOException {
        Properties props = new Properties()
        props.load(ConfigSlurperTest.class.getResourceAsStream("system.properties"))
        assertEquals("false", props.get("catalog.prov"))
        assertEquals("sa", props.get("catalog.prov.db.user"))

        // now round-trip via ConfigSlurper
        ConfigSlurper configSlurper = new ConfigSlurper()
        ConfigObject newConfig = configSlurper.parse(props)
        props = newConfig.toProperties()
        assertEquals("false", props.get("catalog.prov"))
        assertEquals("sa", props.get("catalog.prov.db.user"))
    }

    void testSameElementNestingWithoutDuplication() {
        def cfg = """ 
            a { b { a { foo = 1 } } } 
            a.foo = 2
            a { b { a { bar = 3 } } }
        """
        ConfigObject c = new ConfigSlurper().parse(cfg)
        assert c.a.b.a.foo == 1
        assert c.a != c.a.b.a
        assert c.a.foo == 2
        assert c.a.b.a.bar == 3
    }

    /**
     * Test for GROOVY-3186:
     * ConfigSlurper only allows a single block for any given name
     */
    void testTwoSameBlocks() {
        def config = new ConfigSlurper().parse("""
            topNode {
                one = "1"
            }

            topNode {
                two = "2"
            }

            log4j {
                logger {
                    foo.bar = "debug"
                }
            }

            log4j {
                logger.extraLogger = "info"
            }
        """)

        assert config.topNode.one == "1"
        assert config.topNode.two == "2"

        assert config.log4j.logger.foo.bar == "debug"
        assert config.log4j.logger.extraLogger == "info"
    }

    /**
     * Test for GROOVY-5370: ConfigSlurper - multiple environment blocks broken
     */
    void testMultipleToplevelEnvironmentBlocksForSameEnvironment() {
        def config = new ConfigSlurper('development').parse("""
            environments {
                development {
                    a = 1
                }
            }
            environments {
                development {
                    b = 2
                }
            }
        """)

       assert config == [a:1, b:2]
    }

    /**
     * Test for GROOVY-5370: ConfigSlurper - multiple environment blocks broken
     */
    void testMultipleEnvironmentBlocksOnDifferentLevelsForSameEnvironment() {
        def config = new ConfigSlurper('development').parse("""
            environments {
                development {
                    a = 1
                }
            }
            blah {
                environments {
                    development {
                        c = 3
                    }
                }
            }
        """)

        assert config == [a:1, blah:[c: 3]]
    }

    void testVariableAssignments() {
        def conf = '''
          griffon.cli.verbose = true
          griffon.rt.verbose = true
          projects {
              custom {
                  this."griffon.cli.verbose" = false
                  griffon{rt{verbose = false}}
              }
          }
        '''

        def config = new ConfigSlurper().parse(conf)
        assert config.griffon.cli.verbose
        assert !config.projects.custom.griffon.cli.verbose
        assert config.griffon.rt.verbose
        assert !config.projects.custom.griffon.rt.verbose
    }

    void testCustomConditionalBlocks() {
        def conf = '''
          griffon.cli.verbose = true
          projects {
              custom {
                  //griffon{cli{verbose = false}}  // OK
                  //this."griffon.cli.verbose" = false  // fails
                  griffon {
                      cli.verbose = false
                  }
              }
          }
        '''

        ConfigSlurper slurper = new ConfigSlurper()
        slurper.registerConditionalBlock('projects', 'bogus')
        def config = slurper.parse(conf)
        assert config.griffon.cli.verbose
        slurper.registerConditionalBlock('projects', 'custom')
        config = slurper.parse(conf)
        assert !config.griffon.cli.verbose
    }

    void testNestedConditionaBlocks() {
        def conf = '''
          var = 1
          projects {
              custom {
                  var = 2
                  environments {
                      development {
                          var = 3
                      }
                      local {
                          var = 4
                      }
                  }
              }
              extension {
                  var = 7
              }
          }
          environments {
               development {
                   var = 5
               }
               production {
                   var = 6
               }
           }
        '''

        ConfigSlurper slurper = new ConfigSlurper()
        slurper.binding = [slurper:slurper]
        def config = slurper.parse(conf)
        assert config.var == 1

        slurper.registerConditionalBlock('environments', 'production')
        config = slurper.parse(conf)
        assert config.var == 6

        slurper.registerConditionalBlock('environments', 'test')
        config = slurper.parse(conf)
        assert config.var == 1

        slurper.registerConditionalBlock('projects', 'custom')
        config = slurper.parse(conf)
        assert config.var == 2

        slurper.registerConditionalBlock('environments', 'local')
        config = slurper.parse(conf)
        assert config.var == 4

        slurper.registerConditionalBlock('projects', 'bogus')
        config = slurper.parse(conf)
        assert config.var == 1
    }

    void testConditionalOverrides() {
        def conf = '''
          environments {
               development {
                   var1 = 1
                   var2 = 2
               }
               production {
                   var1 = 3
                   var2 = 4
               }
           }
           var1 = 5
           var2 = 6
        '''

        ConfigSlurper slurper = new ConfigSlurper()
        slurper.binding = [slurper:slurper]
        def config = slurper.parse(conf)
        assert config.var1 == 5
        assert config.var2 == 6

        slurper.registerConditionalBlock('environments', 'development')
        config = slurper.parse(conf)
        assert config.var1 == 1
        assert config.var2 == 2
    }
}
