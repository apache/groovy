window.BENCHMARK_DATA = {
  "lastUpdate": 1778455445360,
  "repoUrl": "https://github.com/apache/groovy",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "3e02aeb12c3fbbf2ad5227f04fc315a3fda0133d",
          "message": "info added to comment",
          "timestamp": "2026-05-10T23:23:06Z",
          "url": "https://github.com/apache/groovy/commit/3e02aeb12c3fbbf2ad5227f04fc315a3fda0133d"
        },
        "date": 1778455444151,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0824094298167728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8753958251259879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.502364144355454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.987471265391142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.978455280181032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.957968821880433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.673558274699928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.31230204602695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.599466291438251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.85226307449597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.422754342162266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.863786562983883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.467860535327599,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 179.82463079999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.00563090869568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 158.64566088461538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 183.64588107272726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 247.62002272222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 371.0042106833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 298.3333106428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.00289257727272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 418.77603654000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.670040271005006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6774899159078742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.498734386664582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.5777146643887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.082110149930097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.039567028566504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.219520276420235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.785669073419513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.717702536700084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.832520314460716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.651618890113873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.3968603146546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.740242297253392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.47936896107384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}