- Getting started

================================================================
== installing on windows
================================================================
As the current swt libs are not on ibiblio you have to manually 
install them. 
I case you already have eclipse3.0 installed, all the libs can be found in 
the plugins directory. 

copy 
	eclipse/plugins/org.eclipse.swt.win32_3.0.0/ws/win32/swt.jar
to 
	maven/repository/swt/jars/swt-win32-3.0.jar


copy 
	eclipse/plugins/org.eclipse.jface_3.0.0/jface.jar
to 
	maven/repository/swt/jars/jface-3.0.jar
	

copy 
	eclipse/plugins/org.eclipse.ui.forms_3.0.0/forms.jar
to 
	maven/repository/swt/jars/forms-3.0.jar	

copy 
	eclipse/plugins/org.eclipse.core.runtime_3.0.0/runtime.jar
to 
	maven/repository/swt/jars/runtime-3.0.jar	


copy 
	eclipse/plugins/org.eclipse.osgi_3.0.0/runtime.jar
to 
	maven/repository/swt/jars/runtime-3.0.jar	
	

copy
	eclipse/plugins/org.eclipse.swt.win32_3.0.0/os/win32/x86/swt-awt-win32-3062.dll
	eclipse/plugins/org.eclipse.swt.win32_3.0.0/os/win32/x86/swt-win32-3062.dll 
to 
	a directory in your businessSystem environment variable $PATH
(Alternatively you can run the examples by using the -Djava.library.path vm argument)

All test cases will also fail when you do not have these libraries properly installed.
     
Alternatively you can download the RCP Runtime Binary from
the www.eclipse.org download pages. This download 
also contains most libraries but the forms.jar is missing.

================================================================
== running the examples
================================================================
The examples can be found in the src/examples subdirectory.
For every example there is a java main class you can run.


