//vi: binary:noeol
package gls.ch03.s05

// Note: this file must not have a trailing EOL. For this reason, 
// it should be stored in CVS as a binary file (-kb)

/**
 * GLS 3.5:
 * As a special concession for compatibility with certain operating
 * systems, the ASCII SUB character (\u001a, or control-Z) is ignored if
 * it is the last character in the escaped input stream.
 *
 * This file tests ASCII SUBs expressed as unicode escapes
 */
public class EndWithSUB2 extends GroovyTestCase {

    void testMe() {
        // Inline SUBs are not allowed
        // int a\u001a = 2//@fail:parse
    }
// Next line ends with ASCII SUB, expressed as a unicode escape
} \u001a
