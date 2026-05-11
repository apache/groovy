window.BENCHMARK_DATA = {
  "lastUpdate": 1778458364815,
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
      },
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
          "id": "160c9d57d19dd70b97fb7d3f286e285cac100496",
          "message": "tweak performance test which has stack overflow in some environments",
          "timestamp": "2026-05-10T23:27:04Z",
          "url": "https://github.com/apache/groovy/commit/160c9d57d19dd70b97fb7d3f286e285cac100496"
        },
        "date": 1778458364086,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.1479011105654344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.91656556412354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 16.81272668895341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0666417030802346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.6373709246378474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.08008708272171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.706381349589407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.478085231938554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.774911930253413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 62.84247747414772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.373600544946049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9117783594896391,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.553856193828511,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 195.5851297909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 85.69843273985508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 165.6898618397436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 197.55872034272727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 267.87456868749996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 399.9501159366667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 307.6041071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 90.29038068142293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 431.1963755399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7216488525668852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6936907651559796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.603787097920505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.660613500080277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.39974233607586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.2043924092159894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.06555047468093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.784443841248095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.313070454258284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.47441180220013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.314075920733774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 35.87610657396446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.927659007291666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.360827887928654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}