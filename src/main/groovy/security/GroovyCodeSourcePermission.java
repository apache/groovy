package groovy.security;

import java.security.BasicPermission;

/**
 * Permission required to explicitly specify a codebase for a groovy script whose
 * codebase cannot be determined.  Typically this permission is only
 * required by clients that want to associate a code source with a script which
 * is a String or an InputStream.
 *
 * @author Steve Goetze
 */
public class GroovyCodeSourcePermission extends BasicPermission {

	public GroovyCodeSourcePermission(String name) {
		super(name);
	}

	public GroovyCodeSourcePermission(String name, String actions) {
		super(name, actions);
	}
}
