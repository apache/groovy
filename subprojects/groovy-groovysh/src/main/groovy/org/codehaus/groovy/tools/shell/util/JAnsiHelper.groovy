package org.codehaus.groovy.tools.shell.util

import org.fusesource.jansi.AnsiOutputStream

class JAnsiHelper {

    /**
     * copied from jline2 ConsoleReader
     * @param str
     * @return
     */
    public static CharSequence stripAnsi(CharSequence str) {
        if (str == null) return "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AnsiOutputStream aos = new AnsiOutputStream(baos);
            aos.write(str.toString().getBytes());
            aos.flush();
            return baos.toString();
        } catch (IOException e) {
            return str;
        }
    }
}
