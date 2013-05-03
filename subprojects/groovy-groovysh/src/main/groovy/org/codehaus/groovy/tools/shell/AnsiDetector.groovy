package org.codehaus.groovy.tools.shell


import jline.Terminal

import java.util.concurrent.Callable

class AnsiDetector
implements Callable<Boolean>
{
    public Boolean call() throws Exception {
        return Terminal.getTerminal().isANSISupported()
    }
}