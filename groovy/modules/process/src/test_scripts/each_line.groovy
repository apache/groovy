gsh = new com.baulsupp.groovy.groosh.Groosh();

cat = gsh.cat('test_scripts/blah.txt');
lines = gsh.each_line { line,w | 
  w.write("*");
  w.write(line);
  w.write("\n");
};

cat.pipeTo(lines);
lines.toStdOut();


