package org.codehaus.groovy.tools.shell.completion

import jline.console.completer.Completer
import jline.console.completer.StringsCompleter

import static jline.internal.Preconditions.checkNotNull

/**
 * Changes JLine 2.12 StringsCompleter behavior to either always or never add blanks.
 */
public class PatchedStringsCompleter
        extends StringsCompleter
{

    private boolean withBlank = true

    public PatchedStringsCompleter() {
    }

    public PatchedStringsCompleter(final Collection<String> strings) {
        super(strings)
    }

    public PatchedStringsCompleter(final String... strings) {
        this(Arrays.asList(strings));
    }

    void setWithBlank(boolean withBlank) {
        this.withBlank = withBlank
    }

    @Override
    public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
        // buffer could be null
        checkNotNull(candidates);

        if (buffer == null) {
            candidates.addAll(strings.collect({it -> withBlank ? it + ' ' : it}));
        }
        else {
            for (String match : strings.tailSet(buffer)) {
                if (!match.startsWith(buffer)) {
                    break;
                }

                candidates.add(withBlank ? match + ' ' : match);
            }
        }

        return candidates.isEmpty() ? -1 : 0;
    }
}
