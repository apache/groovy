/* The Computer Language Shootout
   http://shootout.alioth.debian.org/

   benchmark implementation (not optimized)
   JRE 1.5 only
   contributed by Josh Goldfoot */

import java.io.*;
import java.lang.*;
import java.util.*;

public class magicsquares {
    
    static int n;
    static int mn;
    
    public static class FfmResult {
        public int x;
        public int y;
        public int[] moves;
        public FfmResult(int ix, int iy, int[] imoves) {
            x = ix;
            y = iy;
            moves = (int[]) imoves.clone();
        }
    }
    
    public static class PQNode implements Comparable {
        public int [] grid;
        boolean priorityCalculated;
        private int priority;
        private FfmResult ffm;
        
        public PQNode() {
            grid = new int [n * n];
            int i;
            for (i = 0; i < n * n; i++)
                grid[i] = 0;
            priorityCalculated = false;
            ffm = null;
        }
        public PQNode(PQNode other) {
            grid = (int[]) other.grid.clone();
            priorityCalculated = false;
        }

        public int[] gridRow(int y) {
            int[] r = new int[n];
            int x;
            for (x = 0; x < n; x++)
                r[x] = grid[x + y * n];
            return r;
        }

        public int[] gridCol(int x) {
            int[] r = new int[n];
            int y;
            for (y = 0; y < n; y++)
                r[y] = grid[x + y * n];
            return r;
        }

        public int[] diagonal(int q) {
            int[] r = new int[n];
            int i;
            for (i = 0; i < n; i++) {
                if (q == 1)
                    r[i] = grid[i + i * n];
                else if (q == 2) {
                    r[i] = grid[i + (n - 1 - i) * n];
                }
            }
            return r;
        }

        public int[] possibleMoves(int x, int y) {
            int zerocount, sum, highest, j, i;
            
            if (grid[x + y * n] != 0)
                return ( new int [] { });
            ArrayList<int[]> cellGroups = new ArrayList<int[]>();
            cellGroups.add(gridCol(x));
            cellGroups.add(gridRow(y));
            if (x == y)
                cellGroups.add(diagonal(1));
            if (x + y == n - 1)
                cellGroups.add(diagonal(2));
            HashSet<Integer> usedNumbers = new HashSet<Integer>();
            for (i = 0; i < grid.length; i++)
                usedNumbers.add(new Integer(grid[i]));
            HashSet<Integer> onePossible = new HashSet<Integer>();
            highest = n * n;
            for (int[] group: cellGroups) {
                zerocount = 0;
                sum = 0;
                for (int num: group) {
                    if (num == 0)
                        zerocount++;
                    sum += num;
                }
                if (zerocount == 1)
                    onePossible.add(new Integer(mn - sum));
                if (mn - sum < highest)
                    highest = mn - sum;
            }
            if (onePossible.size() == 1) {
                Integer onlyPossibleMove = (Integer) onePossible.iterator().next();
                int opmint = onlyPossibleMove.intValue();
                if (opmint >= 1 && 
                        opmint <= n * n && 
                        ! usedNumbers.contains(onlyPossibleMove))
                    return (new int[] { opmint });
                else
                    return ( new int [] { });
            } else if (onePossible.size() > 1)
                return ( new int [] { });
            ArrayList<Integer> moves = new ArrayList<Integer>();
            for (i = 1; i <= highest; i++) {
                Integer ii = new Integer(i);
                if (! usedNumbers.contains(ii))
                    moves.add(ii);
            }
            int[] r = new int[moves.size()];
            i = 0;
            for (Integer move: moves) {
                r[i++] = move.intValue();
            }
            return r;
        }

        public FfmResult findFewestMoves() {
            if (ffm != null)
                return ffm;
            int x, y, mx, my, ind;
            int[] minmoves = (new int[] { });
            int[] moves;
            mx = my = -1;
            for (y = 0; y < n; y++)
                for (x = 0; x < n; x++) {
                    ind = x + y * n;
                    if (grid[ind] == 0) {
                        moves = possibleMoves(x,y);
                        if (mx == -1 || moves.length < minmoves.length) {
                            mx = x;
                            my = y;
                            minmoves = (int[]) moves.clone();
                        }
                    }
                }
            ffm = new FfmResult(mx, my, minmoves);
            return ffm;
        }
        
        public void calculatePriority()
        {
            int i, zerocount;
            zerocount = 0;
            for (int cell: grid)
                if (cell == 0)
                    zerocount++;
            priority = zerocount + findFewestMoves().moves.length;
            priorityCalculated = true;
        }
        
        public int getPriority()
        {
            if (priorityCalculated)
                return priority;
            else {
                calculatePriority();
                return priority;
            }
        }
        
        public int compareTo(Object anotherMSquare) throws ClassCastException {
            if (!(anotherMSquare instanceof PQNode))
                throw new ClassCastException();
            PQNode other = (PQNode) anotherMSquare;
            int c = getPriority() - other.getPriority();
            if (c == 0) {
                int i = 0;
                while (c == 0 && i < n * n) {
                    c = grid[i] - other.grid[i];
                    i++;
                }
            }
            return c;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            int y, x;
            for (y = 0; y < n; y++) {
                for (x = 0; x < n; x++) {
                    sb.append(grid[x + y * n]);
                    if (x < n-1)
                        sb.append(' ');
                }
                if (y < n-1)
                    sb.append('\n');
            }
            return sb.toString();
        }

        
    }
    
    public magicsquares() { }
    
    public static void main(String[] args) {
        n = 3;
        if (args.length > 0) 
            n = Integer.parseInt(args[0]);
        mn = n * (1 + n * n) / 2;
        PriorityQueue<magicsquares.PQNode> pq = new PriorityQueue<magicsquares.PQNode>(); 
        pq.offer(new magicsquares.PQNode() );
        while (! pq.isEmpty()) {
            PQNode topSquare = pq.poll();
            if (topSquare.getPriority() == 0) {
                System.out.println(topSquare);
                break;
            }
            magicsquares.FfmResult result = topSquare.findFewestMoves();

            int ind = result.x + result.y * n;
            for (int move: result.moves) {
                magicsquares.PQNode newSquare = new magicsquares.PQNode(topSquare);
                newSquare.grid[ind] = move;
                pq.offer(newSquare);
            }
        }
                
    }
}
