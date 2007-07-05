/* Copyright 2006-2007 Graeme Rocher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util
/**
 * Tests for the ConfigSlurper class
 
 * @author Graeme Rocher
 * @since 0.6
  *
 * Created: Jun 19, 2007
 * Time: 6:29:33 PM
 * 
 */

class  ConfigSlurperTest extends GroovyTestCase {

    void testEnvironmentProperties2() {
        def config = new ConfigSlurper("production").parse('''
dataSource {
	pooling = false
	driverClassName = "org.hsqldb.jdbcDriver"
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
			url = "jdbc:hsqldb:mem:testDb"
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
        assertEquals "org.hsqldb.jdbcDriver", config.dataSource.driverClassName
        assertEquals "sa", config.dataSource.username
        assertEquals "secret", config.dataSource.password
        assertEquals "update", config.dataSource.dbCreate
        assertEquals "jdbc:hsqldb:file:prodDb;shutdown=true", config.dataSource.url
    }

    void testParseProperties() {
        Properties props = new Properties()
        props['foo'] = 'bar'
        props['log4j.appender.NULL']='org.apache.log4j.varia.NullAppender'
        props['log4j.rootLogger']='error, NULL'
        props['log4j.logger.org.codehaus.groovy.grails.plugins']='info,NULL'
        props['log4j.additivity.org.codehaus.groovy.grails.plugins']='false'
        props['log4j.additivity.org.springframework']='false'
        props['log4j.logger.grails.spring']='info,NULL'
        props['log4j.appender.NULL.layout']='org.apache.log4j.PatternLayout'

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
''')

        assert config
        println config
        println config.smtp.server
        assertEquals "localhost", config.smtp.server.url
        assertEquals "fred", config.smtp.username
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

        println config

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


        props = config.log4j.toProperties("log4j")
        assertEquals "org.apache.log4j.ConsoleAppender", props."log4j.appender.stdout"
        assertEquals "org.apache.log4j.PatternLayout", props."log4j.appender.stdout.layout"
        assertEquals "error,stdout", props."log4j.rootLogger"
        assertEquals "info,stdout", props."log4j.logger.org.codehaus.groovy.grails"
        assertEquals "false", props."log4j.additivity.org.codehaus.groovy.grails"

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

		println newText

		config = slurper.parse(newText)


        assertEquals "org.apache.log4j.ConsoleAppender", config.log4j.appender.stdout
        assertEquals "org.apache.log4j.PatternLayout", config.log4j.appender."stdout.layout"
        assertEquals "error,stdout", config.log4j.rootLogger
        assertEquals "info,stdout", config.log4j.logger.org.codehaus.groovy.grails
        assertEquals false, config.log4j.additivity.org.codehaus.groovy.grails

		
	}
}