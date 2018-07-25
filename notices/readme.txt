This directory contains generated NOTICE files and snippets used to generate those files.
See the assemble.gradle file (updateNotices task) for details on how this is done.
Snippets have predefined suffix values in their name to determine which files they go into.
NOTICE (the one for source), NOTICE-DOC, NOTICE-GROOID and NOTICE-JARJAR
get snippets containing SRC, DOC, GROOID and JARJAR respectively.
NOTICE-GROOIDJARJAR gets JARJAR and GROOID snippets.
NOTICE-BINZIP gets GROOID, JARJAR and BINZIP snippets.
In addition, NOTICE files are generated for these subprojects:
groovy-console