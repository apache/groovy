class A {
	private void testSlashy() {
		println(/Hello/)
		(/Hello/ + 1)
	}

	private void testDoubleQuoted() {
		println("${ variable }")
		println(" Text ${ variable } text")
		println(" Text ${ variable } text ${ variable } ${ variable + 1 } ${ "variable" }")
		println(" Text ${ "inner${ variable } ${ variable }" } text ${ variable } ${ variable + 1 } ${ "variable" }")
	}
}
