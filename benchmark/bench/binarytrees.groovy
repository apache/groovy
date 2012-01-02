/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Jochen Hinrichsen
 * modified by Marko Kocic
 */

final class TreeNode {
    private final left, right, item

    TreeNode(item) {
        this.item = item
    }

    private static bottomUpTree(item, depth) {
        if (depth > 0) {
            return new TreeNode(
                bottomUpTree(2*item-1, depth-1),
                bottomUpTree(2*item,   depth-1),
                item
            )
        } else {
            return new TreeNode(item)
        }
    }

    TreeNode(left, right, item) {
        this.left = left
        this.right = right
        this.item = item
    }

    private itemCheck() {
        // if necessary deallocate here
        if (left == null) return item
        else return item + left.itemCheck() - right.itemCheck()
    }
}

def n = (args.length == 0) ? 10 : args[0].toInteger()
def minDepth = 4
def maxDepth = [minDepth + 2, n].max()
def stretchDepth = maxDepth + 1

def check = (TreeNode.bottomUpTree(0, stretchDepth)).itemCheck()
println "stretch tree of depth ${stretchDepth}\t check: ${check}"

def longLivedTree = TreeNode.bottomUpTree(0, maxDepth)

def depth = minDepth
while (depth <= maxDepth) {
    def iterations = 1 << (maxDepth - depth + minDepth)
    check = 0
    for (i in 1..iterations) {
        check += (TreeNode.bottomUpTree(i, depth)).itemCheck()
        check += (TreeNode.bottomUpTree(-i,depth)).itemCheck()
    }

    println "${iterations*2}\t trees of depth ${depth}\t check: ${check}"
    depth += 2
}

println "long lived tree of depth ${maxDepth}\t check: ${longLivedTree.itemCheck()}"
