/* The Computer Language Benchmarks Game
   http://shootout.alioth.debian.org/
   contributed by Jochen Hinrichsen.
   Modified by Alex Tkachman
*/

class TreeNode {
   private left, right
   private item

   TreeNode(item){
      this.item = item
   }

   private static def bottomUpTree(item, depth) {
      if (depth>0) {
      depth = depth-1
      def item2 = item*2
      return new TreeNode(
           bottomUpTree(item2-1, depth)
         , bottomUpTree(item2, depth)
         , item
         )
      } else {
      return new TreeNode(item)
      }
   }

   TreeNode(left, right, item){
      this.left = left
      this.right = right
      this.item = item
   }

   public def itemCheck(){
      // if necessary deallocate here
      if (left==null) return item
      else return item + left.itemCheck() - right.itemCheck()
   }
}

long start = System.currentTimeMillis ()
def n = (args.length == 0) ? 10 : args[0].toInteger()
def minDepth = 4
def maxDepth = [ minDepth + 2, n].max()
def stretchDepth = maxDepth + 1

def check = (TreeNode.bottomUpTree(0,stretchDepth)).itemCheck()
println "stretch tree of depth ${stretchDepth}\t check: ${check}"

def longLivedTree = TreeNode.bottomUpTree(0,maxDepth)

def depth=minDepth
while (depth<=maxDepth) {
  def iterations = 1 << (maxDepth - depth + minDepth)
  check = 0
  for (i in 1..iterations) {
     check += (TreeNode.bottomUpTree(i,depth)).itemCheck()
     check += (TreeNode.bottomUpTree(-i,depth)).itemCheck()
  }

  println "${iterations*2}\t trees of depth ${depth}\t check: ${check}"
  depth+=2
}

println "long lived tree of depth ${maxDepth}\t check: ${longLivedTree.itemCheck()}"
println "${System.currentTimeMillis () - start}ms"
