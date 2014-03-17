package groovy.json

import groovy.json.internal.CharBuf

/**
 * Created by Richard on 2/6/14.
 */
class CharBufTest extends GroovyTestCase {

    public void testUnicodeAndControl() {
        String str = CharBuf.create(0).addJsonEscapedString("\u0001").toString()
        assert str.equals( "\"\\u0001\"")
        
        str =  CharBuf.create(0).addJsonEscapedString("\u00ff").toString()
        assert str.equals( "\"\\u00ff\"")

        str =  CharBuf.create(0).addJsonEscapedString("\u0fff").toString()
        assert str.equals( "\"\\u0fff\"")

        str =  CharBuf.create(0).addJsonEscapedString("\uefef").toString()
        assert str.equals( "\"\\uefef\"")

        str =  CharBuf.create(0).addJsonEscapedString(" \b ").toString()
        assert str.equals( "\" \\b \"" )

        str =  CharBuf.create(0).addJsonEscapedString(" \r ").toString()
        assert str.equals( "\" \\r \"" )

        str =  CharBuf.create(0).addJsonEscapedString(" \n ").toString()
        assert str.equals( "\" \\n \"" )

        str =  CharBuf.create(0).addJsonEscapedString(" \n ").toString()
        assert str.equals( "\" \\n \"" )

        str =  CharBuf.create(0).addJsonEscapedString(" \f ").toString()
        assert str.equals( "\" \\f \"" )

        str =  CharBuf.create(0).addJsonEscapedString(" \" Hi mom \" ").toString()
        assert str.equals( "\" \\\" Hi mom \\\" \"" )

        str =  CharBuf.create(0).addJsonEscapedString(" \\ ").toString()
        assert str.equals( "\" \\\\ \"" )
    }
}
