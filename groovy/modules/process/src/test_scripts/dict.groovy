gsh = new com.baulsupp.groovy.groosh.Groosh();

gsh.cat('/usr/share/dict/words').pipeTo(gsh.grep('lexia')).toStdOut();


