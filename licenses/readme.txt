This directory contains generated LICENSE files and snippets used to generate those files.
See the gradle/licenses.gradle file (updateLicenses task) for details on how this is done.
Every generated file starts with LICENSE-BASE followed by its snippets.
Snippets have predefined suffix values in their name to determine which files they go into.
LICENSE (the one for source), LICENSE-DOC and LICENSE-JARJAR
get snippets containing SRC, DOC and JARJAR respectively.
LICENSE-BINZIP gets JARJAR and BINZIP snippets.
LICENSE-SDK is LICENSE-BINZIP plus a pointer to the embedded src and doc zips.
LICENSE-DOCSJAR is used as META-INF/LICENSE for the groovydoc classifier jars.
LICENSE-ALLSRCJAR is used for the aggregated sources jar, which carries the
third-party-derived source files but not the documentation assets the source
zip also ships; the core sources jar gets plain LICENSE-BASE for the same reason.
In addition, LICENSE files are generated for these subprojects:
groovy-groovydoc, groovy-groovysh, groovy-jsr223
Those three, LICENSE-DOCSJAR and LICENSE-ALLSRCJAR name their snippets explicitly
rather than by suffix.
The *-license.txt files are the full third-party license texts that the snippets
point at. They are not merged into the generated files; the distributions copy them
into a licenses/ directory instead, so the include lists in
subprojects/groovy-binary/build.gradle must stay in sync with the licenses/*.txt
files referenced from LICENSE-BINZIP and LICENSE-DOC.
