#include <errno.h>
#include <sys/syslimits.h>

#ifdef __APPLE__
#ifdef __MACH__
#include <mach-o/dyld.h>
#endif
#endif

int main(int argc, char* argv[]) {

  // Get the location of the executable -- platform specific
  char jarexe[PATH_MAX];

#ifdef __APPLE__
#ifdef __MACH__
  unsigned long length;
  _NSGetExecutablePath(jarexe, &length);
#endif
#endif

  // Setup the command line.
  // TODO: Add -J support for passing Java options
  char* argv2[argc+2];
  argv2[0] = "java";
  argv2[1] = "groovy.lang.GroovyShell";

  // Setup the CLASSPATH environment
  char *CLASSPATH = (char*) getenv("CLASSPATH");
  if (!CLASSPATH) CLASSPATH="";
  char *NEWCLASSPATH = (char*) calloc(strlen(CLASSPATH)+1+strlen(jarexe)+1); // +1 for :, +1 for null terminator
  sprintf(NEWCLASSPATH, "%s:%s", jarexe, CLASSPATH);
  setenv("CLASSPATH", NEWCLASSPATH, 1);

  // Setup the rest of the command line that was passed in.
  // TODO: This will also be affected by -J options
  int i;
  for (i = 1; i < argc; i++) {
    argv2[i+1] = argv[i];
  }
  argv2[argc+1] = 0;

  // Execute java
  execvp("java", argv2);

  // Report if the exec fails
  printf("Cannot execute '");
  for (i = 0; i < argc+1; i ++) { printf("%s ", argv2[i]); }
  printf("', caused by error: %d\n", errno);

}
