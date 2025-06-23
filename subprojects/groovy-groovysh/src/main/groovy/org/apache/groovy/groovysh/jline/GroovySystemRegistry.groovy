package org.apache.groovy.groovysh.jline

import org.jline.builtins.ConfigurationPath
import org.jline.console.impl.SystemRegistryImpl
import org.jline.reader.Parser
import org.jline.terminal.Terminal

import java.nio.file.Path
import java.util.function.Supplier

class GroovySystemRegistry extends SystemRegistryImpl {
    GroovySystemRegistry(Parser parser, Terminal terminal, Supplier<Path> workDir, ConfigurationPath configPath) {
        super(parser, terminal, workDir, configPath)
    }

    @Override
    boolean isCommandOrScript(String command) {
        return command.startsWith("/!") || super.isCommandOrScript(command)
    }
}
