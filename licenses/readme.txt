This directory contains generated LICENSE files and snippets used to generate those files.
See the assemble.gradle file (updateLicenses task) for details on how this is done.
Snippets have predefined suffix values in their name to determine which files they go into.
LICENSE (the one for source), LICENSE-DOC and LICENSE-JARJAR
get snippets containing SRC, DOC and JARJAR respectively.
LICENSE-ALLJARJAR gets JARJAR and ALLJARJAR snippets.
LICENSE-BINZIP gets JARJAR, ALLJARJAR and BINZIP snippets.
In addition, LICENSE files are generated for these subprojects:
groovy-docgenerator, groovy-groovydoc, groovy-groovysh, groovy-jsr223