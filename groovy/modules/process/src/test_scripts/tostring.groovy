gsh = new com.baulsupp.groovy.groosh.Groosh();

s = gsh.cat('test_scripts/blah.txt').pipeTo(gsh.grep('a')).toStringOut();

System.out.println('->' + s.toString() + '<-');

