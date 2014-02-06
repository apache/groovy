package groovy.json

import groovy.json.internal.CharBuf

import static groovy.json.internal.Exceptions.die

/**
 * Created by Richard on 2/6/14.
 */
class CharBufTest extends GroovyTestCase {

    public void testUnicodeAndControl() {
        String str = CharBuf.create(0).addJsonEscapedString("\u0001").toString();

        boolean ok = str.equals( "\"\\u0001\"") || die(str);
        str =  CharBuf.create(0).addJsonEscapedString("\u00ff").toString();
        ok = str.equals( "\"\\u00ff\"") || die(str);

        str =  CharBuf.create(0).addJsonEscapedString("\u0fff").toString();
        ok = str.equals( "\"\\u0fff\"") || die(str);

        str =  CharBuf.create(0).addJsonEscapedString("\uefef").toString();
        ok = str.equals( "\"\\uefef\"") || die(str);

        str =  CharBuf.create(0).addJsonEscapedString(" \b ").toString();
        ok = str.equals( "\" \\b \"" ) || die(str);

        str =  CharBuf.create(0).addJsonEscapedString(" \r ").toString();
        ok = str.equals( "\" \\r \"" ) || die(str);

        str =  CharBuf.create(0).addJsonEscapedString(" \n ").toString();
        ok = str.equals( "\" \\n \"" ) || die(str);

        str =  CharBuf.create(0).addJsonEscapedString(" \n ").toString();
        ok = str.equals( "\" \\n \"" ) || die(str);


        str =  CharBuf.create(0).addJsonEscapedString(" \f ").toString();
        ok = str.equals( "\" \\f \"" ) || die(str);

        str =  CharBuf.create(0).addJsonEscapedString(" \" Hi mom \" ").toString();
        ok = str.equals( "\" \\\" Hi mom \\\" \"" ) || die(str);


        str =  CharBuf.create(0).addJsonEscapedString(" \\ ").toString();
        ok = str.equals( "\" \\\\ \"" ) || die(str);

    }




}
