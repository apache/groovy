package org.codehaus.groovy.modules.pages;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 10, 2004
 * Token constants for lexer and parser.
 */
interface Tokens {
	static final int EOF = -1;
	static final int HTML = 0;
	static final int JEXPR = 1;   // <%= ... %>
	static final int JSCRIPT = 2; // <% .... %>
	static final int JDIRECT = 3; // <%@ ... %>
	static final int JDECLAR = 4; // <%! ... %>
	static final int GEXPR = 11;   // ${ ... }
	static final int GSCRIPT = 12; // %{ ... }%
	static final int GDIRECT = 13; // @{ ... }
	static final int GDECLAR = 14; // !{ ... }!
}
