# This is a comment: @#$%^@@#$;@#$@#$4!!
package gls.ch03.s07
/**
 * GLS 3.7:
 * In addition, if the first non-space character of a Groovy program is the
 * ASCII sharp sign ('#'), the whole line is treated as a comment. In other
 * words, the program is treated exactly as if two slash characters ('//') were
 * inserted before the sharp sign. This unusual rule makes it easier to write
 * Groovy scripts on some systems.
 */
//# This is not a comment (1) //@fail:parse
class HashComment extends GroovyTestCase {

//# This is not a comment (2) //@fail:parse

    void testHashComment() {
        //# This is not a comment (3) //@fail:parse
        assert true 
    }

}
