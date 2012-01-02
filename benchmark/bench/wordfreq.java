/*
 * The Great Computer Language Shootout
 * http://shootout.alioth.debian.org/
 *
 * contributed by James McIlree
 */

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class wordfreq {
  static class Counter {
    int count = 1;
  }

  public static void main(String[] args) 
    throws IOException
  {
    HashMap map = new HashMap();
    Pattern charsOnly = Pattern.compile("\\p{Lower}+");

    BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
    String line;
    while ((line = r.readLine()) != null) {
      Matcher matcher = charsOnly.matcher(line.toLowerCase());
      while (matcher.find()) {
        String token = matcher.group();
        Counter c = (Counter)map.get(token);
        if (c != null)
          c.count++;
        else
          map.put(token, new Counter());
      }
    }
    
    ArrayList list = new ArrayList(map.entrySet());
    Collections.sort(list, new Comparator() {
        public int compare(Object o1, Object o2) {
          int c = ((Counter)((Map.Entry)o2).getValue()).count - ((Counter)((Map.Entry)o1).getValue()).count;
          if (c == 0) {
            c = ((String)((Map.Entry)o2).getKey()).compareTo((String)((Map.Entry)o1).getKey());
          }
          return c;
        }
      });
    
    String[] padding = { "error!", " ", "  ", "   ", "    ", "     ", "      ", "error!" };
    StringBuffer output = new StringBuffer();
    Iterator it = list.iterator();
    while (it.hasNext()) {
      Map.Entry entry = (Map.Entry)it.next();
      String word = (String)entry.getKey();
      String count = String.valueOf(((Counter)entry.getValue()).count);
      if (count.length() < 7)
        System.out.println(padding[7 - count.length()] + count + " " +word);
      else
        System.out.println(count + " " +word);
    }
  }
}

