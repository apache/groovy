package groovy.security;

import java.io.File;

/**
 * Test case for running a single groovy script parsed from a .groovy file.
 */
public class RunOneGroovyScript extends SecurityTestSupport {

	public void testScript() {
		assertExecute(new File("src/test/groovy/bugs/ConstructorBug.groovy"), null);
	}
}
