gsh = new com.baulsupp.groovy.groosh.Groosh();

if (args.length == 0) {
  System.err.println("please provide a search pattern");
  System.err.println("usage: dict_args querystring");
} else {
  gsh.cat('/usr/share/dict/words').pipeTo(gsh.grep(args[0])).toStdOut();
}

