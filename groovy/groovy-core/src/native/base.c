#ifdef __APPLE__
#ifdef __MACH__
#define MACOSX
#define UNIX
#endif
#endif

#ifdef __linux__
#define UNIX
#define LINUX
#endif

#ifdef UNIX
#include <errno.h>
#endif

#ifdef LINUX
#include <unistd.h>
#include <linux/limits.h>
#endif

#ifdef MACOSX
#include <mach-o/dyld.h>
#include <sys/syslimits.h>
#endif

int main(int argc, char* argv[]) {

  // Get the location of the executable -- platform specific
#ifdef UNIX
  char jarexe[PATH_MAX];
#endif

#ifdef MACOSX
  unsigned long length;
  _NSGetExecutablePath(jarexe, &length);
#endif

#ifdef LINUX
  char temp[PATH_MAX];
  sprintf(temp, "/proc/%d/exe", getpid());
  realpath(temp, jarexe);
#endif

  // Setup the command line.
  // TODO: Add -J support for passing Java options
  char* argv2[argc+2];
  argv2[0] = "java";
  argv2[1] = JAVA_CLASS_NAME;

  // Setup the CLASSPATH environment
  char *CLASSPATH = (char*) getenv("CLASSPATH");
  if (!CLASSPATH) CLASSPATH="";
  char *NEWCLASSPATH = (char*) calloc(strlen(CLASSPATH)+1+strlen(jarexe)+1, sizeof(char)); // +1 for :, +1 for null terminator
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
#ifdef UNIX
  execvp("java", argv2);
#endif

  // Report if the exec fails
  printf("Cannot execute '");
  for (i = 0; i < argc+1; i ++) { printf("%s ", argv2[i]); }
#ifdef UNIX
  printf("', caused by error: %d\n.  In order to run %s you must have the Java VM you want to use in your PATH", errno, argv[0]);
#endif
}
