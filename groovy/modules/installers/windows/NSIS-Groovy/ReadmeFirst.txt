NSIS Groovy
-----------

This directory contains the NSIS installer for Groovy as a project for Eclipse.

To use it, simply check it out into an Eclipse workspace. Additionally you might 
want to install the NSISEclipse-Plugin, it helps when developing NSIS stuff.

If you have worked with NSIS-scripts already there should be no big problem understanding
what is done.

The main script is the file setup.nsi, where you can find the installer program.
Three additional pages are defined, Variables, NativeLauncher and FileAssociation,
and for each you can find the respective .ini file containing the screen placement.
Simply open them in the design editor provided by the Eclipse-Plugin, and you can
see what they look like.

Two bmp-images contain the Groovy logo in different resolutions, one for the welcome
page and one as header image for the other pages. The name is the game ...

Fínally you have the launch scripts. These actually start the compiler with different
settings for the groovy version, the groovy location (on your hard disk) and the
location of the native launcher. The groovy version influences the final name of the
installer.

The launch scripts are setting the following variables:

SOURCE_VERSION defines the version of the release
SOURCE_DIR     is the full path to the groovy install directory
NATIVE_DIR     is the full path to the native launcher


Most probably you only have to set these variables and you are happy to go.

Have fun, Joachim

Contact: joachim.baumann@xinaris.de