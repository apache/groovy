package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * Completor for the command.names
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class CommandNameCompleter extends SimpleCompletor {
    private final CommandRegistry registry

    CommandNameCompleter(final CommandRegistry registry) {
        assert registry

        this.registry = registry
    }

    SortedSet getCandidates() {
        def set = new TreeSet()

        for (Command command in registry.commands()) {
            if (command.hidden) {
                continue
            }

            set << command.name
            set << command.shortcut
        }

        return set
    }
}
