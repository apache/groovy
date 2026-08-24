This directory contains generated NOTICE files and snippets used to generate those files.
See the gradle/licenses.gradle file (updateNotices task) for details on how this is done.
Snippets have predefined suffix values in their name to determine which files they go into.
NOTICE (the one for source), NOTICE-GROOID and NOTICE-JARJAR
get snippets containing SRC, GROOID and JARJAR respectively.
The doc zip uses NOTICE-BASE as-is; there is no generated NOTICE-DOC.
NOTICE-GROOIDJARJAR gets JARJAR and GROOID snippets.
NOTICE-BINZIP gets GROOID, JARJAR and BINZIP snippets.
NOTICE-SDK is NOTICE-BINZIP plus a pointer to the embedded src and doc zips.
NOTICE-SRCJAR is used for the sources jars, which carry the third-party-derived
source files but not the documentation assets the source zip also ships; it names
its snippets explicitly rather than by suffix.
In addition, NOTICE files are generated for these subprojects:
groovy-console
