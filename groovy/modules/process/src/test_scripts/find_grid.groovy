// BUG?? java.util.Arrays.asList fails with util on null!
// changed java code to send List

gsh = new com.baulsupp.groovy.groosh.Groosh();

f = gsh.find('.', '-name', '*.java', '-ls');
total = 0;
lines = gsh.grid { values,w |
  x = values[2,4,6,10]; 
  s = x.join('	');
  w.println(s);
  total += Integer.parseInt(values[6]);
};

f.pipeTo(lines);
lines.toStdOut();

System.out.println("Total: " + total);

