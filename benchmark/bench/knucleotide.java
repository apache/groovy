/* The Computer Language Benchmarks Game
 http://shootout.alioth.debian.org/

 contributed by James McIlree
 */

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class knucleotide {
    String sequence;
    int count = 1;

    knucleotide(String sequence) {
        this.sequence = sequence;
    }

    static ArrayList<Callable< Map<String, knucleotide> > > createFragmentTasks(final String sequence, int[] fragmentLengths) {
        ArrayList<Callable<Map<String, knucleotide>>> tasks = new ArrayList<Callable<Map<String, knucleotide>>>();
        for (int fragmentLength : fragmentLengths) {
            for (int index=0; index<fragmentLength; index++) {
                final int offset = index;
                final int finalFragmentLength = fragmentLength;
                tasks.add(new Callable<Map<String, knucleotide>>() {
                    public Map<String, knucleotide> call() {
                    return createFragmentMap(sequence, offset, finalFragmentLength);
                    }
                });
            }
        }
        return tasks;
    }

    static Map<String, knucleotide> createFragmentMap(String sequence, int offset, int fragmentLength) {
        HashMap<String, knucleotide> map = new HashMap<String, knucleotide>();
        int lastIndex = sequence.length() - fragmentLength + 1;
        for (int index=offset; index<lastIndex; index+=fragmentLength) {
            String temp = sequence.substring(index, index + fragmentLength);
            knucleotide fragment = (knucleotide)map.get(temp);
            if (fragment != null)
            fragment.count++;
            else
            map.put(temp, new knucleotide(temp));
        }

        return map;
    }

    // Destructive!
    static Map<String, knucleotide> sumTwoMaps(Map<String, knucleotide> map1, Map<String, knucleotide> map2) {
        for (Map.Entry<String, knucleotide> entry : map2.entrySet()) {
            knucleotide sum = (knucleotide)map1.get(entry.getKey());
            if (sum != null)
            sum.count += entry.getValue().count;
            else
            map1.put(entry.getKey(), entry.getValue());
        }
        return map1;
    }

    static String writeFrequencies(Map<String, knucleotide> frequencies) {
        ArrayList<knucleotide> list = new ArrayList<knucleotide>(frequencies.size());
        int sum = 0;
        for (knucleotide fragment : frequencies.values()) {
            list.add(fragment);
            sum += fragment.count;
        }

        Collections.sort(list, new Comparator<knucleotide>() {
            public int compare(knucleotide o1, knucleotide o2) {
                int c = o2.count - o1.count;
                if (c == 0) {
                    c = o1.sequence.compareTo(o2.sequence);
                }
                return c;
            }
        });

        StringBuilder sb = new StringBuilder();
        for (knucleotide k : list)
            sb.append(String.format("%s %.3f\n", k.sequence.toUpperCase(), (float)(k.count) * 100.0f / (double)sum));

        return sb.toString();
    }

    static String writeCount(List<Future<Map<String, knucleotide>>> futures, String nucleotideFragment) throws Exception {
        int count = 0;
        for (Future<Map<String, knucleotide>> future : futures) {
            knucleotide temp = future.get().get(nucleotideFragment);
            if (temp != null) count += temp.count;
        }

        return count + "\t" + nucleotideFragment.toUpperCase();
    }

    public static void main (String[] args) throws Exception {
        String line;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while ((line = in.readLine()) != null) {
            if (line.startsWith(">THREE")) break;
        }

        StringBuilder sbuilder = new StringBuilder();
        while ((line = in.readLine()) != null) {
            sbuilder.append(line);
        }

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int[] fragmentLengths = { 1, 2, 3, 4, 6, 12, 18 };
        List<Future<Map<String, knucleotide>>> futures = pool.invokeAll(createFragmentTasks(sbuilder.toString(), fragmentLengths));
        pool.shutdown();

        // Print the length 1 & 2 counts. We know the offsets of the tasks, so we can cheat.
        System.out.println(writeFrequencies(futures.get(0).get()));
        System.out.println(writeFrequencies(sumTwoMaps(futures.get(1).get(), futures.get(2).get())));

        System.out.println(writeCount(futures, "ggt"));
        System.out.println(writeCount(futures, "ggta"));
        System.out.println(writeCount(futures, "ggtatt"));
        System.out.println(writeCount(futures, "ggtattttaatt"));
        System.out.println(writeCount(futures, "ggtattttaatttatagt"));
    }

}
