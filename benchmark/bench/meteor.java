/* The Computer Language Shootout
   http://shootout.alioth.debian.org/

   contributed by Tony Seebregts
   modified by 
*/

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/** First hack at a Java solver for the meteor puzzle - just the IBM 
  * developerWorks article algorithm optimized with precalculated shapes 
  * and bitmasks. Should be possible to optimize it some more to take 
  * advantage of reflections but its turning out to be less obvious 
  * than expected :-).
  * <p>
  * Notes:
  * <ul>
  * <li>Seems to run faster without the -server switch.
  * <li>Testing for islands seems to be slower than just fitting pieces.
  * </ul>
  * 
  * @author Tony Seebregts
  *
  */
  
public class meteor
   { // CONSTANTS
       
     private static final int[]    SHIFT = { 0,6,11,17,22,28,33,39,44,50 };
     private static final long[][] MASK  = { { 0x01L,      0x02L,      0x04L,      0x08L,      0x10L   },
                     { 0x01L << 6, 0x02L << 6, 0x04L << 6, 0x08L <<  6,0x10L << 6  },
                     { 0x01L << 11,0x02L << 11,0x04L << 11,0x08L << 11,0x10L << 11 },
                     { 0x01L << 17,0x02L << 17,0x04L << 17,0x08L << 17,0x10L << 17 },
                     { 0x01L << 22,0x02L << 22,0x04L << 22,0x08L << 22,0x10L << 22 },
                     { 0x01L << 28,0x02L << 28,0x04L << 28,0x08L << 28,0x10L << 28 },
                     { 0x01L << 33,0x02L << 33,0x04L << 33,0x08L << 33,0x10L << 33 },
                     { 0x01L << 39,0x02L << 39,0x04L << 39,0x08L << 39,0x10L << 39 },
                     { 0x01L << 44,0x02L << 44,0x04L << 44,0x08L << 44,0x10L << 44 },
                     { 0x01L << 50,0x02L << 50,0x04L << 50,0x08L << 50,0x10L << 50 }
                       };
     
     private static final boolean DEBUG = false;

     // CLASS VARIABLES
     
     // INSTANCE VARIABLES
     
     private SortedSet<String> solutions = new TreeSet<String>();
     private Entry[]       solution  = new Entry[10];
     private int       depth     = 0;
     private Piece[]       pieces    = { new Piece(PIECE0),
                     new Piece(PIECE1),
                     new Piece(PIECE2),
                     new Piece(PIECE3),
                     new Piece(PIECE4),
                     new Piece(PIECE5),
                     new Piece(PIECE6),
                     new Piece(PIECE7),
                     new Piece(PIECE8),
                     new Piece(PIECE9)
                       };
       
     // CLASS METHODS

     /** Application entry point.
       * 
       * @param args  Command line arguments:
       *      <ul>
       *      <li> solution limit
       *      </ul>
       */
     
     public static void main(String[] args)
        { int N = 2098;
        
          // ... parse command line arguments
        
          if (args.length > 0)
         if (args[0].matches("\\d+"))
            N = Integer.parseInt(args[0]);
            
          // ... solve puzzle
          
          meteor        puzzle = new meteor ();
          Date      start;
          Date      end;
          long      time;
          SortedSet<String> solutions;
          
          start     = new Date();
          solutions = puzzle.solve();
          end   = new Date();
          time      = end.getTime() - start.getTime();      
          
          // ... print result
            
          if (solutions.size() > N)
         System.out.println("ERROR");
         else if (solutions.size() < N)
         System.out.println("TIMEOUT");
         else
         { if (DEBUG)
              { System.out.println("START    : " + start);
            System.out.println("END      : " + end);
            System.out.println("TIME     : " + time);
            System.out.println("SOLUTIONS: " + solutions.size ());
            System.out.println("FIRST    : " + solutions.first());
            System.out.println("LAST     : " + solutions.last ());
            System.out.println();
              }
           
           System.out.print(solutions.size () + " solutions found\n\n");
           print(solutions.first());
           System.out.print("\n");
           print(solutions.last ());
           System.out.print("\n");
         }
        }

     /** Prints out the puzzle.
       * 
       * 
       */
    
     private static void print (String solution)
         { System.out.print(solution.replaceAll("(\\d{5})(\\d{5})","$1 $2")
                    .replaceAll("(\\d{5})","$1\n")
                    .replaceAll("(\\d)","$1 "));
         }

     // CONSTRUCTORS
     
     /** Initialises the puzzle.
       * 
       */

     public meteor ()
        { for (int i=0; i<10; i++)
          solution[i] = new Entry();
        }
     
     // INSTANCE METHODS
     
     /** Initialises the puzzle and solution set at [0,0]
       *
       * @return Sorted list of solution strings.
       */ 
     
     private SortedSet<String> solve()
         { solve(0x0002004008010020L,0,0);
         
           return solutions;
         }
     
     /** Recursively solves the puzzle by fitting pieces into the 
       * next available hexagon.
       * 
       * @param puzzle  Current puzzle bitmask.
       * @param row     Row of next available hexagon. 
       * @param col     Column next available hexagon. 
       * 
       */
      
     private void solve (long puzzle,int row,int col)
         { for (int ix=0; ix<pieces.length; ix++)
           { Piece   piece;
             Shape[] list;
 
             // ... find shapes that fit
             
             if ((piece = pieces[ix]) == null)
            continue;
            else
            list  = pieces[ix].shapes(row,col);
               
             for (Shape shape: list)
             { // ... fits badly ?
          
               if ((shape.bitmap & puzzle) != 0)
                  continue;
               
               // ... try piece in puzzle
 
               long clone = puzzle | shape.bitmap;
 
               // ... find next position
                
               int irow = row;
               int icol = col/2 + 1;
                
               next:
               while (irow < 10)
                 { while (icol < 5)
                     { if ((clone & MASK[irow][icol]) == 0)
                      break next;
                              
                       icol++;
                     }
                         
                   irow++;
                   icol = 0;
                 }
                 
               // ... solve next
               
               Entry entry;
                 
               pieces[ix]  = null;
               entry   = solution[depth++];
               entry.row   = row;
               entry.col   = col;
               entry.shape = shape;
 
               if (depth == 10)
                  solutions.add(serialize(solution));
                  else
                  solve (clone,irow,2*icol + (irow % 2));
                
               depth--;
               pieces[ix] = piece;
             }
           }
         }
      
     /** Serializes the current solution to a string.
       * 
       */
      
     private String serialize (Entry[] solution)
         { char[] puzzle = new char[50];
           Shape   shape;
           int     row;
           int     col;
           
           for (Entry entry: solution)
           { shape = entry.shape;
             row   = entry.row;
             col   = entry.col;
             
             for (int[] xy: shape.vector)
             puzzle[5 * (row + xy[0]) + (col + xy[1])/2] = shape.symbol;
           }
      
           return new String(puzzle);
         }
    
     // INNER CLASSES
     
     /** Container class for a solution set entry.
       * 
       */
     
     private static class Entry
         { public int   row;
           public int   col;
           public Shape shape; 
         }
     
     /** Container class for the shapes for a single puzzle piece.
       * 
       * 
       */
     
     private static class Piece
         { private Shape[][][] shapes = new Shape[10][10][];
         
           @SuppressWarnings("unchecked")
           private Piece (Shape[] list)
               { // ... initialise
               
             ArrayList[][] array = new ArrayList[10][10];
             
             for (int i=0; i<10; i++)
                 for (int j=0; j<10; j++)
                 array[i][j] = new ArrayList<Shape>();
             
             // ... generate list
             
             for (Shape mutant: list)
                 for (int row=0; row<=mutant.maxRow; row++)
                 for (int col=mutant.minCol; col<=mutant.maxCol; col++)
                     { if (!mutant.islet)
                      array[row][col].add(new Shape(mutant,row,col));
                      else if ((row != 0) || (col != 0))
                      array[row][col].add(new Shape(mutant,row,col));
                     }
             
             for (int row=0; row<10; row++)
                 for (int col=0; col<10; col++)
                 shapes[row][col] = (Shape[]) array[row][col].toArray(new Shape[0]);
               }
           
           @SuppressWarnings("unchecked")
           private Shape[] shapes(int row,int col)
               { return shapes[row][col];
               }
         
         }

     /** Container class for the shape vector and bitmap single puzzle piece mutation.
       * 
       * 
       */
     
     private static class Shape
        { private char    symbol;
          private int[][] vector;
          private long    bitmap;
          private int     shift;
          
          private boolean islet;
          private int     maxRow;
          private int     minCol;
          private int     maxCol;
          
          private Shape (char    symbol,
                 int[][] vector,
                 long    bitmap,
                 int     shift,
                 boolean islet,
                 int     maxRow,
                 int     minCol,
                 int     maxCol)
              { this.symbol  = symbol;
            this.vector  = vector;
            this.bitmap  = bitmap;
            this.shift   = shift;
            
            this.islet   = islet;
            this.maxRow  = maxRow;
            this.minCol  = minCol;
            this.maxCol  = maxCol;
              }
          
          private Shape (Shape shape,
                 int   row,
                 int   col)
              { this.symbol  = shape.symbol;
            this.vector  = shape.vector;
            this.bitmap  = shape.bitmap << ((SHIFT[row] + (col - (row % 2))/2) - shape.shift);
            
            this.islet   = shape.islet;
            this.maxRow  = shape.maxRow;
            this.minCol  = shape.minCol;
            this.maxCol  = shape.maxCol;
              }
        }
     
     // PIECES

     private static final Shape[] PIECE0 = { new Shape ('0',new int[][] {{3, 5},{2, 4},{1, 3},{0, 2},{0, 0}},0x0000000000082083L,0,false,6,0,4),
                     new Shape ('0',new int[][] {{4,-2},{3,-1},{2, 0},{1, 1},{0, 0}},0x0000000000421082L,1,false,5,2,8),
                     new Shape ('0',new int[][] {{1,-7},{1,-5},{1,-3},{1,-1},{0, 0}},0x00000000000003D0L,4,false,8,7,9),
                     new Shape ('0',new int[][] {{0, 0},{1, 1},{2, 2},{3, 3},{3, 5}},0x00000000000C1041L,0,false,6,0,4),
                     new Shape ('0',new int[][] {{0, 0},{1,-1},{2,-2},{3,-3},{4,-2}},0x0000000000821084L,2,false,5,3,9),
                     new Shape ('0',new int[][] {{0, 6},{0, 4},{0, 2},{0, 0},{1,-1}},0x000000000000005EL,1,false,8,1,3),
                     new Shape ('0',new int[][] {{0, 0},{1, 1},{2, 2},{3, 3},{4, 2}},0x0000000000841041L,0,false,5,0,6),
                     new Shape ('0',new int[][] {{0, 0},{1,-1},{2,-2},{3,-3},{3,-5}},0x0000000000062108L,3,false,6,5,9),
                     new Shape ('0',new int[][] {{1, 7},{1, 5},{1, 3},{1, 1},{0, 0}},0x00000000000003C1L,0,false,8,0,2),
                     new Shape ('0',new int[][] {{4, 2},{3, 1},{2, 0},{1,-1},{0, 0}},0x0000000001041042L,1,false,5,1,7),
                     new Shape ('0',new int[][] {{3,-3},{2,-2},{1,-1},{0, 0},{0, 2}},0x000000000002108CL,2,false,6,3,7),
                     new Shape ('0',new int[][] {{0, 0},{0, 2},{0, 4},{0, 6},{1, 7}},0x000000000000020FL,0,false,8,0,2)
                       };

     private static final Shape[] PIECE1 = { new Shape ('1',new int[][] {{0, 2},{0, 0},{1,-1},{2, 0},{3,-1}},0x0000000000021046L,1,false,6,1,7),
                     new Shape ('1',new int[][] {{1, 3},{0, 2},{0, 0},{1,-1},{1,-3}},0x00000000000002CCL,2,false,8,3,6),
                     new Shape ('1',new int[][] {{3, 3},{2, 4},{1, 3},{1, 1},{0, 0}},0x00000000000420C1L,0,false,6,0,5),
                     new Shape ('1',new int[][] {{3,-3},{3,-1},{2, 0},{1,-1},{0, 0}},0x0000000000062084L,2,false,6,3,9),
                     new Shape ('1',new int[][] {{0, 0},{1, 1},{1, 3},{0, 4},{0, 6}},0x00000000000000CDL,0,true, 8,0,3),
                     new Shape ('1',new int[][] {{0, 0},{1,-1},{2, 0},{2, 2},{3, 3}},0x0000000000083042L,1,false,6,1,6),
                     new Shape ('1',new int[][] {{0, 6},{1, 5},{1, 3},{0, 2},{0, 0}},0x000000000000018BL,0,true, 8,0,3),
                     new Shape ('1',new int[][] {{3, 3},{3, 1},{2, 0},{1, 1},{0, 0}},0x0000000000060841L,0,false,6,0,6),
                     new Shape ('1',new int[][] {{3,-3},{2,-4},{1,-3},{1,-1},{0, 0}},0x00000000000208C4L,2,false,6,4,9),
                     new Shape ('1',new int[][] {{1,-1},{0, 0},{0, 2},{1, 3},{1, 5}},0x0000000000000346L,1,false,8,1,4),
                     new Shape ('1',new int[][] {{0, 0},{0, 2},{1, 3},{2, 2},{3, 3}},0x0000000000041083L,0,false,6,0,6),
                     new Shape ('1',new int[][] {{0, 0},{1, 1},{2, 0},{2,-2},{3,-3}},0x0000000000023104L,2,false,6,3,8)
                       };

     private static final Shape[] PIECE2 = { new Shape ('2',new int[][] {{1, 1},{0, 0},{2, 0},{2,-2},{2,-4}},0x0000000000003904L,2,false,7,4,8),
                     new Shape ('2',new int[][] {{2, 4},{1, 5},{2, 2},{1, 1},{0, 0}},0x0000000000003141L,0,false,7,0,4),
                     new Shape ('2',new int[][] {{3,-1},{3, 1},{2,-2},{1,-1},{0, 0}},0x0000000000060842L,1,false,6,2,8),
                     new Shape ('2',new int[][] {{1,-1},{2, 0},{0, 0},{0, 2},{0, 4}},0x000000000000104EL,1,false,7,1,5),
                     new Shape ('2',new int[][] {{0, 0},{1,-1},{0, 2},{1, 3},{2, 4}},0x0000000000004146L,1,false,7,1,5),
                     new Shape ('2',new int[][] {{0, 2},{0, 0},{1, 3},{2, 2},{3, 1}},0x0000000000021083L,0,true, 6,0,6),
                     new Shape ('2',new int[][] {{0, 2},{1, 3},{0, 0},{1,-1},{2,-2}},0x0000000000000946L,1,false,7,2,6),
                     new Shape ('2',new int[][] {{1, 5},{2, 4},{0, 4},{0, 2},{0, 0}},0x0000000000002107L,0,false,7,0,4),
                     new Shape ('2',new int[][] {{3, 1},{3,-1},{2, 2},{1, 1},{0, 0}},0x0000000000062082L,1,false,6,1,7),
                     new Shape ('2',new int[][] {{2,-4},{1,-5},{2,-2},{1,-1},{0, 0}},0x0000000000003148L,3,false,7,5,9),
                     new Shape ('2',new int[][] {{1,-1},{0, 0},{2, 0},{2, 2},{2, 4}},0x0000000000007042L,1,false,7,1,5),
                     new Shape ('2',new int[][] {{0, 0},{0, 2},{1,-1},{2, 0},{3, 1}},0x0000000000041046L,1,false,6,1,7)
                       };

     private static final Shape[] PIECE3 = { new Shape ('3',new int[][] {{0, 0},{2, 0},{1,-1},{2,-2},{2,-4}},0x0000000000003884L,2,false,7,4,9),
                     new Shape ('3',new int[][] {{1, 5},{2, 2},{1, 3},{1, 1},{0, 0}},0x00000000000011C1L,0,false,7,0,4),
                     new Shape ('3',new int[][] {{3, 1},{2,-2},{2, 0},{1,-1},{0, 0}},0x0000000000041842L,1,false,6,2,8),
                     new Shape ('3',new int[][] {{2, 0},{0, 0},{1, 1},{0, 2},{0, 4}},0x0000000000000847L,0,false,7,0,5),
                     new Shape ('3',new int[][] {{1,-3},{0, 0},{1,-1},{1, 1},{2, 2}},0x00000000000041C4L,2,false,7,3,7),
                     new Shape ('3',new int[][] {{0, 0},{1, 3},{1, 1},{2, 2},{3, 1}},0x00000000000210C1L,0,true, 6,0,6),
                     new Shape ('3',new int[][] {{1, 3},{0, 0},{1, 1},{1,-1},{2,-2}},0x00000000000009C2L,1,false,7,2,6),
                     new Shape ('3',new int[][] {{2, 4},{0, 4},{1, 3},{0, 2},{0, 0}},0x0000000000002087L,0,false,7,0,5),
                     new Shape ('3',new int[][] {{3,-1},{2, 2},{2, 0},{1, 1},{0, 0}},0x0000000000023082L,1,false,6,1,7),
                     new Shape ('3',new int[][] {{1,-5},{2,-2},{1,-3},{1,-1},{0, 0}},0x00000000000021C8L,3,false,7,5,9),
                     new Shape ('3',new int[][] {{0, 0},{2, 0},{1, 1},{2, 2},{2, 4}},0x0000000000003841L,0,false,7,0,5),
                     new Shape ('3',new int[][] {{0, 0},{1,-3},{1,-1},{2,-2},{3,-1}},0x00000000000410C4L,2,false,6,3,9)
                       };

     private static final Shape[] PIECE4 = { new Shape ('4',new int[][] {{1, 5},{2, 2},{1, 3},{0, 2},{0, 0}},0x0000000000001183L,0,false,7,0,4),
                     new Shape ('4',new int[][] {{3, 1},{2,-2},{2, 0},{1, 1},{0, 0}},0x0000000000041882L,1,false,6,2,8),
                     new Shape ('4',new int[][] {{2, 0},{0, 0},{1, 1},{1, 3},{0, 4}},0x00000000000008C5L,0,true, 7,0,5),
                     new Shape ('4',new int[][] {{1,-3},{0, 0},{1,-1},{2, 0},{2, 2}},0x00000000000060C4L,2,false,7,3,7),
                     new Shape ('4',new int[][] {{0, 0},{1, 3},{1, 1},{2, 0},{3, 1}},0x00000000000208C1L,0,false,6,0,6),
                     new Shape ('4',new int[][] {{0, 0},{2, 0},{1,-1},{1,-3},{2,-4}},0x00000000000028C4L,2,false,7,4,9),
                     new Shape ('4',new int[][] {{0, 0},{1,-3},{1,-1},{2, 0},{3,-1}},0x00000000000420C4L,2,false,6,3,9),
                     new Shape ('4',new int[][] {{1, 3},{0, 0},{1, 1},{2, 0},{2,-2}},0x0000000000001982L,1,false,7,2,6),
                     new Shape ('4',new int[][] {{2, 4},{0, 4},{1, 3},{1, 1},{0, 0}},0x00000000000020C5L,0,true, 7,0,5),
                     new Shape ('4',new int[][] {{3,-1},{2, 2},{2, 0},{1,-1},{0, 0}},0x0000000000023042L,1,false,6,1,7),
                     new Shape ('4',new int[][] {{1,-3},{2, 0},{1,-1},{0, 0},{0, 2}},0x00000000000020CCL,2,false,7,3,7),
                     new Shape ('4',new int[][] {{0, 0},{2, 0},{1, 1},{1, 3},{2, 4}},0x00000000000028C1L,0,false,7,0,5)
                       };

     private static final Shape[] PIECE5 = { new Shape ('5',new int[][] {{0, 2},{1, 1},{0, 0},{1,-1},{2,-2}},0x00000000000008C6L,1,false,7,2,7),
                     new Shape ('5',new int[][] {{1, 5},{1, 3},{0, 4},{0, 2},{0, 0}},0x0000000000000187L,0,false,8,0,4),
                     new Shape ('5',new int[][] {{3, 1},{2, 0},{2, 2},{1, 1},{0, 0}},0x0000000000021841L,0,false,6,0,7),
                     new Shape ('5',new int[][] {{2,-4},{1,-3},{2,-2},{1,-1},{0, 0}},0x00000000000018C4L,2,false,7,4,9),
                     new Shape ('5',new int[][] {{0, 0},{0, 2},{1, 1},{1, 3},{1, 5}},0x00000000000001C3L,0,false,8,0,4),
                     new Shape ('5',new int[][] {{0, 0},{1, 1},{1,-1},{2, 0},{3, 1}},0x00000000000410C2L,1,false,6,1,8),
                     new Shape ('5',new int[][] {{0, 2},{0, 0},{1, 1},{1,-1},{1,-3}},0x00000000000001CCL,2,false,8,3,7),
                     new Shape ('5',new int[][] {{2, 4},{1, 3},{2, 2},{1, 1},{0, 0}},0x00000000000030C1L,0,false,7,0,5),
                     new Shape ('5',new int[][] {{3,-1},{2, 0},{2,-2},{1,-1},{0, 0}},0x0000000000021842L,1,false,6,2,9),
                     new Shape ('5',new int[][] {{1,-1},{1, 1},{0, 0},{0, 2},{0, 4}},0x00000000000000CEL,1,false,8,1,5),
                     new Shape ('5',new int[][] {{0, 0},{1, 1},{0, 2},{1, 3},{2, 4}},0x00000000000020C3L,0,false,7,0,5),
                     new Shape ('5',new int[][] {{0, 0},{1,-1},{1, 1},{2, 0},{3,-1}},0x00000000000210C2L,1,false,6,1,8)
                       };

     private static final Shape[] PIECE6 = { new Shape ('6',new int[][] {{1, 1},{0, 0},{1,-1},{1,-3},{2,-4}},0x00000000000009C4L,2,false,7,4,8),
                     new Shape ('6',new int[][] {{2, 4},{1, 5},{1, 3},{0, 2},{0, 0}},0x0000000000002183L,0,false,7,0,4),
                     new Shape ('6',new int[][] {{3,-1},{3, 1},{2, 0},{1, 1},{0, 0}},0x0000000000061082L,1,false,6,1,8),
                     new Shape ('6',new int[][] {{1,-5},{2,-4},{1,-3},{1,-1},{0, 0}},0x00000000000011C8L,3,false,7,5,9),
                     new Shape ('6',new int[][] {{0, 0},{1,-1},{1, 1},{2, 2},{2, 4}},0x00000000000060C2L,1,false,7,1,5),
                     new Shape ('6',new int[][] {{0, 2},{0, 0},{1, 1},{2, 0},{3, 1}},0x0000000000020843L,0,false,6,0,7),
                     new Shape ('6',new int[][] {{0, 0},{1, 1},{1,-1},{2,-2},{2,-4}},0x0000000000001984L,2,false,7,4,8),
                     new Shape ('6',new int[][] {{1, 5},{2, 4},{1, 3},{1, 1},{0, 0}},0x00000000000021C1L,0,false,7,0,4),
                     new Shape ('6',new int[][] {{3, 1},{3,-1},{2, 0},{1,-1},{0, 0}},0x0000000000061042L,1,false,6,1,8),
                     new Shape ('6',new int[][] {{2,-2},{1,-3},{1,-1},{0, 0},{0, 2}},0x00000000000010CCL,2,false,7,3,7),
                     new Shape ('6',new int[][] {{1,-1},{0, 0},{1, 1},{1, 3},{2, 4}},0x00000000000041C2L,1,false,7,1,5),
                     new Shape ('6',new int[][] {{0, 0},{0, 2},{1, 1},{2, 2},{3, 1}},0x0000000000021043L,0,false,6,0,7)
                       };

     private static final Shape[] PIECE7 = { new Shape ('7',new int[][] {{0, 2},{1, 1},{0, 0},{2, 0},{2,-2}},0x0000000000001886L,1,false,7,2,7),
                     new Shape ('7',new int[][] {{1, 5},{1, 3},{0, 4},{1, 1},{0, 0}},0x00000000000001C5L,0,true, 8,0,4),
                     new Shape ('7',new int[][] {{3, 1},{2, 0},{2, 2},{1,-1},{0, 0}},0x0000000000043042L,1,false,6,1,7),
                     new Shape ('7',new int[][] {{2,-2},{1,-1},{2, 0},{0, 0},{0, 2}},0x0000000000001846L,1,false,7,2,7),
                     new Shape ('7',new int[][] {{0, 0},{0, 2},{1, 1},{0, 4},{1, 5}},0x0000000000000147L,0,false,8,0,4),
                     new Shape ('7',new int[][] {{0, 0},{1, 1},{1,-1},{2, 2},{3, 1}},0x00000000000420C2L,1,false,6,1,7),
                     new Shape ('7',new int[][] {{0, 4},{0, 2},{1, 3},{0, 0},{1,-1}},0x000000000000014EL,1,false,8,1,5),
                     new Shape ('7',new int[][] {{2, 4},{1, 3},{2, 2},{0, 2},{0, 0}},0x0000000000003083L,0,false,7,0,5),
                     new Shape ('7',new int[][] {{3,-1},{2, 0},{2,-2},{1, 1},{0, 0}},0x0000000000021882L,1,false,6,2,8),
                     new Shape ('7',new int[][] {{1,-1},{1, 1},{0, 0},{1, 3},{0, 4}},0x00000000000001CAL,1,false,8,1,5),
                     new Shape ('7',new int[][] {{0, 0},{1, 1},{0, 2},{2, 2},{2, 4}},0x0000000000003043L,0,false,7,0,5),
                     new Shape ('7',new int[][] {{0, 0},{1,-1},{1, 1},{2,-2},{3,-1}},0x00000000000208C2L,1,false,6,2,8)
                       };

     private static final Shape[] PIECE8 = { new Shape ('8',new int[][] {{4, 2},{3, 1},{2, 0},{1, 1},{0, 0}},0x0000000000820841L,0,false,5,0,7),
                     new Shape ('8',new int[][] {{3,-5},{2,-4},{1,-3},{1,-1},{0, 0}},0x0000000000021188L,3,false,6,5,9),
                     new Shape ('8',new int[][] {{0, 0},{0, 2},{0, 4},{1, 5},{1, 7}},0x0000000000000307L,0,false,8,0,2),
                     new Shape ('8',new int[][] {{0, 0},{1, 1},{2, 2},{3, 1},{4, 2}},0x0000000000821041L,0,true, 5,0,7),
                     new Shape ('8',new int[][] {{0, 0},{1,-1},{2,-2},{2,-4},{3,-5}},0x0000000000023108L,3,false,6,5,9),
                     new Shape ('8',new int[][] {{1, 7},{1, 5},{1, 3},{0, 2},{0, 0}},0x0000000000000383L,0,false,8,0,2),
                     new Shape ('8',new int[][] {{0, 0},{1, 1},{2, 2},{2, 4},{3, 5}},0x0000000000083041L,0,false,6,0,4),
                     new Shape ('8',new int[][] {{0, 0},{1,-1},{2,-2},{3,-1},{4,-2}},0x0000000000420842L,1,false,5,2,9),
                     new Shape ('8',new int[][] {{0, 4},{0, 2},{0, 0},{1,-1},{1,-3}},0x00000000000000DCL,2,false,8,3,5),
                     new Shape ('8',new int[][] {{3, 5},{2, 4},{1, 3},{1, 1},{0, 0}},0x00000000000820C1L,0,false,6,0,4),
                     new Shape ('8',new int[][] {{4,-2},{3,-1},{2, 0},{1,-1},{0, 0}},0x0000000000421042L,1,false,5,2,9),
                     new Shape ('8',new int[][] {{1,-5},{1,-3},{1,-1},{0, 0},{0, 2}},0x00000000000001D8L,3,false,8,5,7)
                       };

     private static final Shape[] PIECE9 = { new Shape ('9',new int[][] {{3, 3},{2, 2},{1, 1},{0, 0},{0, 2}},0x0000000000041043L,0,false,6,0,6),
                     new Shape ('9',new int[][] {{3,-3},{2,-2},{1,-1},{0, 0},{1, 1}},0x0000000000021184L,2,false,6,3,8),
                     new Shape ('9',new int[][] {{0, 0},{0, 2},{0, 4},{0, 6},{1, 5}},0x000000000000010FL,0,false,8,0,3),
                     new Shape ('9',new int[][] {{0, 0},{1, 1},{2, 2},{3, 3},{3, 1}},0x0000000000061041L,0,true, 6,0,6),
                     new Shape ('9',new int[][] {{0, 0},{1,-1},{2,-2},{3,-3},{2,-4}},0x0000000000021884L,2,false,6,4,9),
                     new Shape ('9',new int[][] {{1, 5},{1, 3},{1, 1},{1,-1},{0, 0}},0x00000000000003C2L,1,false,8,1,4),
                     new Shape ('9',new int[][] {{0, 0},{1, 1},{2, 2},{3, 3},{2, 4}},0x0000000000043041L,0,false,6,0,5),
                     new Shape ('9',new int[][] {{0, 0},{1,-1},{2,-2},{3,-3},{3,-1}},0x0000000000061084L,2,false,6,3,9),
                     new Shape ('9',new int[][] {{0, 6},{0, 4},{0, 2},{0, 0},{1, 1}},0x000000000000004FL,0,false,8,0,3),
                     new Shape ('9',new int[][] {{3, 3},{2, 2},{1, 1},{0, 0},{1,-1}},0x00000000000820C2L,1,false,6,1,6),
                     new Shape ('9',new int[][] {{3,-1},{2, 0},{1, 1},{0, 2},{0, 0}},0x0000000000021086L,1,false,6,1,7),
                     new Shape ('9',new int[][] {{1,-5},{1,-3},{1,-1},{1, 1},{0, 0}},0x00000000000003C8L,3,false,8,5,8)
                       };
                       
    }
