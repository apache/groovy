package org.codehaus.groovy.classgen

class MyBean {

	property name
	property foo

/** @todo parser error 
	property name = "James"
	property foo = 123
*/
}
