H - Change encoding from ISO-8859-1.  ISO-8859-1 is a marker to search for, rather than using the implicit methods.

M - Various TODOs in the code.
M - Line based stdin, rather than buffer based.  More intuitive for users.
M - Handle process completion including sink?  Source can be ignored?
M - Groovy operator overloading.
M - Throw error if source/sink retrieved twice etc.  Or after start() called.

E - Pipeline parsing i.e |, `.
E - Tee Process for duplicating output
E - Concat for joining multiple inputs.  With ThreadLocal input name!
E - Sink/Source dealing with Reader/Writer?

General

bin/groovy test_scripts/dict_args.groovy 2>&1 | grep -v NativeMethod | grep -v Delegating | grep -v Launcher | grep -v Method.invoke | grep -v Invoker | grep -v MetaClass

Caught: java.lang.ArrayIndexOutOfBoundsException: 0
java.lang.ArrayIndexOutOfBoundsException: 0
        at org.codehaus.groovy.runtime.DefaultGroovyMethods.get(DefaultGroovyMethods.java:741)
        at dict_args.run(test_scripts/dict_args.groovy:7)
        at dict_args.invokeMethod(test_scripts/dict_args.groovy)
        at dict_args.main(test_scripts/dict_args.groovy)
        at groovy.lang.GroovyShell.run(GroovyShell.java:166)
        at groovy.lang.GroovyShell.main(GroovyShell.java:85)

