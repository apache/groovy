window.BENCHMARK_DATA = {
  "lastUpdate": 1783149961665,
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
          "id": "c18dd839c712cddcaab67e4631ebe96e09b407fc",
          "message": "test additional user agent workarounds",
          "timestamp": "2026-05-11T06:34:33Z",
          "url": "https://github.com/apache/groovy/commit/c18dd839c712cddcaab67e4631ebe96e09b407fc"
        },
        "date": 1778485372293,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.057716127905368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8755709205305642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.169701298034433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9705753975288873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.861896639169261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.178954960793343,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.457385548290988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.429579194122299,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.731858659187484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.09571491522178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.323586220075274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8417875052967385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.453753885330293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 180.18753452954547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 86.5166147951087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 157.1533469076923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 185.01798485454543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 256.3129685513889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 373.3263817666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 300.8677619142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 91.20797562905139,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 415.09078572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7072768098231947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6765747818991006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.569703690056211,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.568369979820508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.395427652036357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.09589774062766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.646996069771648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.86550771537198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 26.203605467446693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.439205204812865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.7185116026935034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.526211147554875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.64706730067814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.449486680740398,
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
          "id": "03ec3f0ea3ee4858566320d3ae561445c9e62cda",
          "message": "AI readiness: add draft meta skill about creating skills",
          "timestamp": "2026-05-12T06:50:20Z",
          "url": "https://github.com/apache/groovy/commit/03ec3f0ea3ee4858566320d3ae561445c9e62cda"
        },
        "date": 1778570852964,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0462149306750277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8724986235066142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.586161964364567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.6421123730331066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.9642177607661404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.127802264095958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.339906104403656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.1422473973427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.528957436741987,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.02886118243279,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.257699269324515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8404513366330837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.433870820838606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 176.22296383333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 86.31510573768115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 153.00202558901097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 178.0002814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 250.4046373875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 359.9779433499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 293.8044701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 91.20833632588933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 413.49044668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6669242351874953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.678505240494627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.553617574783007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.747808302448021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.149243276074724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.731377852042218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.856927448772083,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.573548445662972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.189888353088026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 18.235634469784195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.884534296841048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 41.565284889328225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.45289070729992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.368458061854847,
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
          "id": "cfa37afd58bb7fdaf5042e7ed47e2f9060688f51",
          "message": "bump dependency metadata",
          "timestamp": "2026-05-13T02:19:00Z",
          "url": "https://github.com/apache/groovy/commit/cfa37afd58bb7fdaf5042e7ed47e2f9060688f51"
        },
        "date": 1778657938705,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0541991448743633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8977242268564372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.424316201934428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0278802529477296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.8739124179403306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.932139448518097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.484031951237288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.258569675922939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.634984289748463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 62.68853735871212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.8505633356584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8487074937922818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.475216242785151,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 178.35807829166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 85.89058313115945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 154.0875687346154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 180.83848319318182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 245.66572068888885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 366.7863095666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 299.0907525142858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 91.80155301640316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 414.2882522600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6769240533243468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6800171738732064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.351286878020265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.57157416420013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.093955352797412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.95535218152948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.260042308549327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.56648171430583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.44552140517212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 18.60654128128829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.742090634028921,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.93039033637037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.353799332974166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.366484022075662,
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
          "id": "0a04376328dcec0a3442f3bb3b28723b52c1daf4",
          "message": "try to make JMX tests more resilient",
          "timestamp": "2026-05-13T13:20:24Z",
          "url": "https://github.com/apache/groovy/commit/0a04376328dcec0a3442f3bb3b28723b52c1daf4"
        },
        "date": 1778743774622,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 0.8351461729351664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.7131574447254421,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 14.347329781384706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.4084785957601333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.213604043492494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 10.862835020437451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 8.952906870523528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 8.92841811380986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 8.982601745279187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 55.25685721156157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 9.565836906615004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.7199276984016809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 3.568230057789794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 145.15283622857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 64.17854971239919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 121.52294237647061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 145.6753928357143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 204.41695854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 292.04536498571423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 237.81448103333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 67.00056522311829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 332.6643430357143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.3278012320364927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.3091591076746831,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 6.417194395708123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 7.09239556300512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 11.561813297996723,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.236092663102717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.649228719312996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.02686382019669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 18.627925466764275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.932500364961149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 4.599439468072815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.78745274392197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.236348321444446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 9.22247808727209,
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
          "id": "b6174a88c57fd3419d97bfeba5e4eb6938673ca5",
          "message": "GROOVY-12012: groovy-yaml: clarify when dates can retain rich types vs when handled as Strings",
          "timestamp": "2026-05-15T03:47:58Z",
          "url": "https://github.com/apache/groovy/commit/b6174a88c57fd3419d97bfeba5e4eb6938673ca5"
        },
        "date": 1778830614190,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.070894985932115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8735391906824379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.875365401226357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9492514084144283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.008562431854413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.384869806232441,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.624184875493574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.014300921837746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.440588416544802,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.31505636867973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.322618414669188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8460752281738463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.4816670638026315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 177.64694602499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 85.6835193591087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.23403952307694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 180.49165084469698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 244.37976476666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 360.47290883333324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 294.19843788571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 91.88077589011857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 415.29756725999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6993566072856872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6893025711159322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.380004314877728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.668201695073359,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.837792292011553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.892942794096872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.642908224724687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.797189882895744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 26.937368721297297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.617196070817066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.684435636360386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 40.83558625268826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.877856000953123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.361422166059304,
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
          "id": "704d8652b3c5016f4b0a54c76028f6dfdb21c798",
          "message": "minor refactor: jmh summary graph",
          "timestamp": "2026-05-15T10:45:10Z",
          "url": "https://github.com/apache/groovy/commit/704d8652b3c5016f4b0a54c76028f6dfdb21c798"
        },
        "date": 1778843382141,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.114919565377363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9103555959217937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 16.83810646781564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9492312172322577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.7470467863659307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.57595037208903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.272565532071107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.268087226176629,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.50037798564227,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.32080941612902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.222287188751668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9058447724575907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.5308684159522254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 192.4022561909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.48813755833335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 165.0083676621795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 187.37337633636366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 278.3695656125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 398.69653423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 309.9860592857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 87.9791286692029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 432.03389056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.690649990854554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7216730003206215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.745684835676682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.811365046947833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.177661283307378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.18389668668481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.981329735649917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.297509134937407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.69945411657798,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.945495585561883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.288312225592865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.94961455384615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.833114958870276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 9.729360294674844,
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
          "id": "e8aed4f0379ad063b027a56fab14119c54be03aa",
          "message": "GROOVY-12013: New optional type checking extension: CombinerChecker to verify associative combiners in injectParallel/sumParallel/Stream.reduce",
          "timestamp": "2026-05-16T06:19:01Z",
          "url": "https://github.com/apache/groovy/commit/e8aed4f0379ad063b027a56fab14119c54be03aa"
        },
        "date": 1778915655276,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.1027088266735494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9076622704981956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.247002539009536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.3026235925195193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.17752841555282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.712498000507452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.481027441757979,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.649970526730959,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.389987942181342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 70.49343015172414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.314261850790595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8951442629018194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.612920753593912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.33723646363634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 86.3542500960145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 156.09384703846155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 187.1899783181818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 258.68126235000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 377.56527515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 308.9666314857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 90.34102685177865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 432.78092936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7055376569913512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6957989910794464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.236431262586887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.229218977763654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.824043185539216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.327125864479552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.85938918807387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 30.527861300648613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.19878458810161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 18.895994501914082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.989440817192002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 40.31590139879232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 23.785861782478992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.33577010958088,
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
          "id": "7f4fb8afd4c72bb5da84bb74fc9181c607f4dce8",
          "message": "AI readiness: sandboxed reproducers",
          "timestamp": "2026-05-17T05:26:09Z",
          "url": "https://github.com/apache/groovy/commit/7f4fb8afd4c72bb5da84bb74fc9181c607f4dce8"
        },
        "date": 1779002694205,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 0.8383150960273099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.7208787854295363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 14.161049130886862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.3886578783511423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.4454447087320035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 10.963061094254018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 9.59288164447388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 8.775466489604119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 8.972864583818358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 53.380695545530145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 9.475456189341136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.697247496156818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 3.56780939878974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 140.25645774666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 65.21125973004033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 122.14554335294117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 144.60598270714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 202.57877180636362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 296.00561768571424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 239.0640031333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 67.12293377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 340.0247016666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.318751583110139,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.3058715502937432,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 6.433079443270648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 6.986293190172452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 11.923888888854188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.470168168606783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.02942146491666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 21.867743938536528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 17.333132571294595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.699004029516908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 4.569580611967035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.29259992478141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.90434276084314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 9.320371915979546,
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
          "id": "167034dab7ed20d43d763520a40d916cac3fe648",
          "message": "minor refactor: improved consistency with other recent method signatures",
          "timestamp": "2026-05-18T05:56:43Z",
          "url": "https://github.com/apache/groovy/commit/167034dab7ed20d43d763520a40d916cac3fe648"
        },
        "date": 1779091619087,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0885748987128716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9372161548754846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.620851230328377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.014243381436535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.817572911644132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.14747590090062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.548564426780217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.36830195172466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 12.2219945792233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 69.38536784229885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.255462917143351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8934299239070898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.603311479044659,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.1180472560606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 84.06824527183332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 157.30984631538462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 191.4701128454545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 282.35857507142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 378.62163360000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 308.4190523857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 89.05924578002306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 439.45927874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7096085486449248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6961320369390573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.146624709792627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.127315548684225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.018269062030754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.511071054962361,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.73426008419447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 29.921370158990577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.08390886496649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 18.88681065811636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.023543317561917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 40.83977489898639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 23.96829103468631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.473079250638751,
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
          "id": "6f24829c9038c9539426fc998420f481cde68ec9",
          "message": "minor refactor: mention spec vs mainline tests in skills file",
          "timestamp": "2026-05-19T05:31:24Z",
          "url": "https://github.com/apache/groovy/commit/6f24829c9038c9539426fc998420f481cde68ec9"
        },
        "date": 1779177511703,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0672662101557062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8731892469911593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.640656578738152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9475036967882384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.8142139499346728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.968186399858983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.198835318899523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.097363040083518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.557938474303393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.278245515625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.139179717028862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8457817783064933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.465216972926828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 174.85692425833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 84.58540734150002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 151.9400667489011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 177.11136509166664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 246.11498494444444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 357.79881803333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 292.54883468571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.71982670901562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 408.9736214800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.689837898481525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6849338998795882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.321564646741289,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.52086447319825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.12767420115841,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.808217991401319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.399049861415104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.56481223325934,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.6293502617332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.328343746793223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.648287721325034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 40.22374178524129,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.218821776479537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.42372970777323,
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
          "id": "6f24829c9038c9539426fc998420f481cde68ec9",
          "message": "minor refactor: mention spec vs mainline tests in skills file",
          "timestamp": "2026-05-19T05:31:24Z",
          "url": "https://github.com/apache/groovy/commit/6f24829c9038c9539426fc998420f481cde68ec9"
        },
        "date": 1779263366078,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.088331355950806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9140981066108413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.57476690504118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.089809708013507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.955984643689432,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.210819825088148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.738783631397075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.465229086438907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 12.8399192920567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 70.30844435862069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.555373604431747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8918503423963626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.5961375079051034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 182.15289178106062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 82.85552966616667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 153.5562373131868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.25935909999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 258.1299011875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 373.10184964999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 304.42235694285716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 86.80217374021738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 442.03949567999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7590542325445278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6953538317027619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.206217605053805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.190207386616896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.392759175320743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.314424920873767,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.480290673761354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.348106726582746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 23.94912778626658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.77395985472567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.265130827662135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.39996310128302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 23.518898247865938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.549319788535929,
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
          "id": "a2ce6f02fea7c439a9eb3feefe7d34f45e34282b",
          "message": "minor refactor: remove javadoc warning",
          "timestamp": "2026-05-20T15:04:10Z",
          "url": "https://github.com/apache/groovy/commit/a2ce6f02fea7c439a9eb3feefe7d34f45e34282b"
        },
        "date": 1779349597655,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0969361366409938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9109009845118721,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.348434436519405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0562069675119865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.320310363877093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.615067166357216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.776029425557315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.528254452284965,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 12.048249218830032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 71.02117996822662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.369568751983627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9015602241393325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.659843921223615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 188.6478515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 86.21553553025362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 158.97218559230768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 190.26510466909093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 268.8381694625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 378.8418082833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 310.1266517285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 90.02112448557313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 434.73313848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.717885231844208,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7112650351368845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.26916087717168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.247228891010248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.37180991189297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.370303468159461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.153351734683252,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 30.09750598916714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 23.829964864971988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 19.361828879141385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.1353068072860335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.58395403363096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 23.701014623809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.964250479355895,
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
          "id": "e9b9cf1ec53ceb86f6b1e3cd88d630ab3234f3ca",
          "message": "GROOVY-12027: Align CompilerConfiguration with JDK17 minimum (fix some tests and docs)",
          "timestamp": "2026-05-22T05:22:33Z",
          "url": "https://github.com/apache/groovy/commit/e9b9cf1ec53ceb86f6b1e3cd88d630ab3234f3ca"
        },
        "date": 1779435946820,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.050821718888228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9561783330544464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.550332487824306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9497994134274013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.729625098191749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.609660719111641,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.416190090824768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.043319657752093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.523834563417939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.57794403639113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.2573739055612,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8530327104302595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.4782589815054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 181.32036359848485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 85.575852775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 153.386007571978,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 181.32650580757576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 248.67166709305556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 361.1723565333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 299.68589901428567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 90.7961881160079,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 416.67578538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7016689782475811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6846629657840833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.55579154510675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.769551300697348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.62061140732457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.6814194440837245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.842264069948786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.91437883126776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.364809432914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.759000774893263,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.637768526397229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.31311222530415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.740021445184663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.589120005535216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "committer": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "id": "650ff80d2ef62d55ae100c4716176d76f2da25c2",
          "message": "Add missing javadoc",
          "timestamp": "2026-05-23T07:13:01Z",
          "url": "https://github.com/apache/groovy/commit/650ff80d2ef62d55ae100c4716176d76f2da25c2"
        },
        "date": 1779521003361,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.080973601483459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9065023719910229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.044509812484353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0414138334491256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.7895270604806965,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.097521381417542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.320858776811429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.257217697675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.450825922452662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 60.19158427852051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.078221791758416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9058143056536523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.566973766488137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 190.56536191818185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.73923744209486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 160.63758211538465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 196.36947136363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 262.1526154375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 402.3562549233333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 302.67915241428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 86.9021504490942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 437.95396584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.718546350729186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6627450903924104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.769893294707697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.637423721321344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.490981976450076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.350587395943318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.27113676907462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.112293721615806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.794968915734547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.145268077171647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.318915128501395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.32842999404934,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.87736082316366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.295597797841749,
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
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "realbluesun@hotmail.com"
          },
          "id": "121c8605f7994fcefb4f13f21b5a4a46b95ee84a",
          "message": "GROOVY-12030: Graduate PropertyHandler from incubating to stable",
          "timestamp": "2026-05-22T23:45:44Z",
          "url": "https://github.com/apache/groovy/commit/121c8605f7994fcefb4f13f21b5a4a46b95ee84a"
        },
        "date": 1779608038327,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.057825862279305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8767994599544335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.54873544649123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9336278914342113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.5712882237531938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.97265350491708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.569326071165007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.23656242055979,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.39763077500459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.90245326129033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.344550080200023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8511612695694671,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.450219829232709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 176.99145604166662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 86.53983378623188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 150.72669392857145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 182.0788960371212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 248.6842443166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 365.09730793333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 296.06295500000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.71607415454545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 411.4082991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6986550121179136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6790218663344842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.500108287227138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.660685388545213,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.322039819475965,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.90169566850871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.998463688001555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.53054141008048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.909982137918497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.252144316902996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.640177091319874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.44223019219696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.934340673076925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.717506871152324,
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
          "id": "42ce7e09611dd02d85d6d1e80e3c079a90100460",
          "message": "GROOVY-12038: Graduate groovy-contracts from incubating to stable",
          "timestamp": "2026-05-24T10:28:42Z",
          "url": "https://github.com/apache/groovy/commit/42ce7e09611dd02d85d6d1e80e3c079a90100460"
        },
        "date": 1779696478167,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0922520570520389,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9199259582470642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.62307823712883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.087442776775575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.325718273752388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.981733415763404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.57020717634153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.189520641606357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.683745388254268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 68.73852569275861,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.613670758186995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9058432880657176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.629312135290814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.36540565227273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 83.13893454133333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 154.78879820329672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 187.95302797272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 267.6497230625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 374.5049881666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 313.3471652571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 89.72800861383399,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 438.19960872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7145210990711668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6911621854909185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.351224272505789,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.388068226698843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.16901884181278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.347272799909117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.425247897544086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 30.13590494502488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.29723586277626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 19.622119855887657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.005036311038269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.29591759169692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 24.02216929344163,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.291118459739598,
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
          "id": "42ce7e09611dd02d85d6d1e80e3c079a90100460",
          "message": "GROOVY-12038: Graduate groovy-contracts from incubating to stable",
          "timestamp": "2026-05-24T10:28:42Z",
          "url": "https://github.com/apache/groovy/commit/42ce7e09611dd02d85d6d1e80e3c079a90100460"
        },
        "date": 1779781769620,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0840370948283558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9346035795879704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.37711688992112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0006254176122824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.184072867956667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.462885111412074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.581481027749273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.104432925025852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.692350121759826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 68.67461918885058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.122528600245777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8948782840536105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.616504259530794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 181.2637608719697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 83.21839338283333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.9140318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.02904954545454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 257.92199768750004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 380.8615255000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 307.7553496857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 88.15056990869566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 438.52386354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7163298769219697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6869310426821156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.424084103173087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.234873038956687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.98906732940927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.426402970188846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.147471693601664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 29.86940943402639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.31404704447717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.73633541947076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.007768343434359,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 40.41299318231292,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 23.145155226190475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.467986574321346,
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
          "id": "f1a4483fe4bab0f6cd9511fad0abb7016059e557",
          "message": "GROOVY-12040: restore @Builder retention to RUNTIME in 5.0.x",
          "timestamp": "2026-05-26T10:09:43Z",
          "url": "https://github.com/apache/groovy/commit/f1a4483fe4bab0f6cd9511fad0abb7016059e557"
        },
        "date": 1779868562931,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0615004367229264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8804315077259453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.3410999285273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9064091529766776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.947018926872903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.052260992379725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.572847068495632,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.41336612878813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.728975330876414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.96746120756049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.30456238829721,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8447575805369665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.443937866183404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 179.00883770000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 86.25683086847825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.98940004615386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 183.53853793712122,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 248.29387064722223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 359.8965696833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 292.92481335714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 93.37117297727272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 410.14710728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6746701769800763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6799804245394618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.426661992840967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.761653110335544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.557014830913294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.750924625746132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.83795626474419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.478102173058353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.485611192773202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.830569797621994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.782030538517334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.54527226989796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.126660533198493,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.575028016450021,
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
          "id": "f220aa2fe7a30fafb7ca02bb454577866174949b",
          "message": "re-enable jmh stats comparison",
          "timestamp": "2026-05-27T10:46:41Z",
          "url": "https://github.com/apache/groovy/commit/f220aa2fe7a30fafb7ca02bb454577866174949b"
        },
        "date": 1779954596420,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.086501868418503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9140742603161991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.419599179013407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0796706604137176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.123532420080312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.3828807308066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.669214145236944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.432159596871337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.510319221354672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 68.68086302149426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.33680521552826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9102103144060434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.621975988909047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 200.90903824464647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 84.88343290833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 156.9516530076923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 188.73466577272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 257.945478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 373.8587857333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 307.67682758571425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 88.99607012608696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 440.9190728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7255962171121824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6933169230113538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.12507665229171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.477512120253778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.842467844119437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.382284763069098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.553380757909345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.3618914553284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.253063258622014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 19.842964195837723,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.042903411975593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 37.51783248413793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.52409998183691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.263340142010595,
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
          "id": "f220aa2fe7a30fafb7ca02bb454577866174949b",
          "message": "re-enable jmh stats comparison",
          "timestamp": "2026-05-27T10:46:41Z",
          "url": "https://github.com/apache/groovy/commit/f220aa2fe7a30fafb7ca02bb454577866174949b"
        },
        "date": 1780040991067,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0899317956558179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8786189630224698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.517376507007505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.953630518840135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.158482322624171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.074815412755655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.576832907464123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.145611070033151,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.651680933810415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.81340231532258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.074959696412066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8421769537165325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.449702291466842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 174.22134806666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 85.29829557500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 149.84465872857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 180.74026090833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 245.66055053333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 360.2251573333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 295.2012396571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 89.60628331304346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 420.08038114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6717098031656739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.708113087662382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.399331843368775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.694978656496842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.554810341396536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.693826756858751,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.911358566502724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.97046575834566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 26.58372543707018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.705239722900906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.6765123867021146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.26848916054131,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.83646634531323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 11.088450787400546,
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
          "id": "f6e2248d1262e2d0cb0f9f835f1aee40f0d162b2",
          "message": "GROOVY-12036/GROOVY-12037: GDK: cache Collectors instances in StreamGroovyMethods and ParallelCollectionExtensions",
          "timestamp": "2026-05-24T00:57:15Z",
          "url": "https://github.com/apache/groovy/commit/f6e2248d1262e2d0cb0f9f835f1aee40f0d162b2"
        },
        "date": 1780126139191,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.05613935313704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8873743117083552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.553042514652937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9495656476831913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.014459204728403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.519191216452288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.465844517753334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.237180888819191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.410009100963554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.38876663175402,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.18748663044824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8522296780999807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.477419623467709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 174.8075092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 84.42452563750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 153.1121798098901,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 179.81420717500004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 244.86771607777777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 361.7296769833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 286.80146331428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 90.47877038972331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 409.17142365999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7647151205392742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6812587866752917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.436222624244344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.740162953553796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.378992189315827,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.736719983423791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.095696605558624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 29.005080686947117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.561046526427297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.456290296607065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.66862589495595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.484251080718956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.94534451772575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.941054609980117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "committer": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "id": "7bfdeea2361c3b02488d381b7b5dd867db0787bf",
          "message": "Trivial refactor: use pattern variable instead",
          "timestamp": "2026-05-31T06:53:43Z",
          "url": "https://github.com/apache/groovy/commit/7bfdeea2361c3b02488d381b7b5dd867db0787bf"
        },
        "date": 1780213570271,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 0.8377614036076648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.724042862332545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 14.139408488189355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.4924856269192306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.4633816100179473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 11.344820157792933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 8.784636302450284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 8.700833747565287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 8.8863397588643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 51.695863548717945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 9.74859851802507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.697688337696734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 3.5886403671056746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 139.63272233333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 65.56071127419355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 121.45652828823526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 145.97056459285713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 202.34412350999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 290.628772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 236.61574607777774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 68.66232298367817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 336.54217725476195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.3383231336274168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.3264821290405777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 6.5228909203224985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 6.887463617615028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 11.562249323030517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.492504280080586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.518304931671384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.036904187935487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 18.546939017911797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 13.578175303203034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 4.766078282863469,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.87952210633175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.246700864045284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 9.008527061008566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "netliomax25-code",
            "username": "netliomax25-code",
            "email": "netliomax25@gmail.com"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "1776f97521c3521723e181d466db72e007085a5c",
          "message": "use Locale.ROOT in DataSet table name folding",
          "timestamp": "2026-06-01T06:31:38Z",
          "url": "https://github.com/apache/groovy/commit/1776f97521c3521723e181d466db72e007085a5c"
        },
        "date": 1780301832009,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.054621550450438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8856967938576874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.657463808082024,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.962307168747725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.742986793218732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.003893994735952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.980582594948629,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.247101271558494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.162639401143585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.67017271935484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.238451696552156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8488456907012034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.468241376031412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 178.4916206666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 87.6434585414855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.9011542230769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 179.82477029166665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 248.32551664027778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 362.76816003333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 302.48163625714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.03563915533596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 413.3785091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6903941563941107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6806507497420813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.339246499150167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.76028803690506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.548267878255256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.9761269735426525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.194156072422743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.54773590850069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 26.17835611834393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 18.429337201301312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.693274612498838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.434075654113215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.969297591869083,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 11.073546180780472,
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
          "id": "653195f4057c7f77032b10b766562502db2705b4",
          "message": "Use log y-axis for JMH adhoc throughput charts\n\nLarger workload sizes span ~2 decades of throughput, so a linear axis\nflattened the size=10000 group into indistinguishable slivers despite a\nreal ~12x best-vs-worst spread within each size. Matches the allocation\ncharts, which were already log.\n\nCo-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-02T06:58:05Z",
          "url": "https://github.com/apache/groovy/commit/653195f4057c7f77032b10b766562502db2705b4"
        },
        "date": 1780387678314,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0521731317669627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8725879379703386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.581950689599907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9609342224305157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.018161414482178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.557391053264302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.410986439973547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.16025554421554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.37690081355466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 62.49271368674242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.208226935253322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8429437896965719,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.5186228504932515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 176.07691282500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 85.98583892173913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 151.52514324285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 181.24677903333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 242.12175405555553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 365.01891688333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 291.68397978571426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 88.53731210434782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 407.4471247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6705080769435618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6792278956295212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.41730395820233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.690251658555503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.468880278536975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.670863670798475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.549442447033366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 29.16508026496472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.341092164435004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.382173299536582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.646554860086864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.12437006513498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.95027153383899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.317512803733587,
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
          "id": "c71d51560dc2211aea298ad2360e8a7449d1e182",
          "message": "GROOVY-12060: groovy-contracts could support @Decreases at the method level (sibling loop test)",
          "timestamp": "2026-06-03T02:29:10Z",
          "url": "https://github.com/apache/groovy/commit/c71d51560dc2211aea298ad2360e8a7449d1e182"
        },
        "date": 1780475101860,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0607067518666167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8798366323225955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.683013444604875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.990820433717448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.9564250075426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.635993976856625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.813302423317959,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.3225252150989,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.656379236986409,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 67.3924958139785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.1531229196468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8477462934559418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.5064925522787975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 174.32234940833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 83.77404161166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 151.35698810329671,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 181.18834732499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 249.11897813055558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 359.07275866666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 299.4582186571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 90.34810782450593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 410.37126592000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.689955301215155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6918961923096236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.414658633879711,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.530282514255479,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.109396331517345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.8245587114994235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.19323611115286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.242786633466068,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 25.96325738181818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 19.086115494459253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.764601893876665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.454481092239504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.314293512195725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.436521270601634,
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
          "id": "13b009a3243716c000d3ceb7db681883d98bf76b",
          "message": "GROOVY-12059: groovy-contracts: test nested closures in postconditions\n\nThe fix already covered @Ensures (the method/constructor path in\nAnnotationClosureVisitor recomputes variable scopes for both pre- and\npostconditions), but the original commit only added nested-closure tests\nfor @Invariant and @Requires. This adds the matching @Ensures coverage:\nnested closures with an explicit parameter, the implicit 'it', and\nreferences to both the method parameter and 'result'.\n\nAssisted-by: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-03T22:09:51Z",
          "url": "https://github.com/apache/groovy/commit/13b009a3243716c000d3ceb7db681883d98bf76b"
        },
        "date": 1780560832638,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.139845733192211,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9240217054437101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 16.92080148648697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9816232700898704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.166807038652452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.568798205166278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.572975697467902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.136919644381873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.242928855463628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 61.843752411837116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.175419415921615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9491898634803659,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.569129948655428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 194.2690907545455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.21029377826086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 160.18701084615387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.86461242727276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 269.5546385125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 401.60732742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 305.47408531428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 89.26569388162055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 447.22997754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.683880006088269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6627260916843034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.71034735130488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.73114421816369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.36982725120183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.104021725962953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.23333703720986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.341537276624813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.99099169452555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.956654862858482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.302133391641339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 38.927608701923084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.341536517969043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 9.831880597541126,
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
          "id": "13b009a3243716c000d3ceb7db681883d98bf76b",
          "message": "GROOVY-12059: groovy-contracts: test nested closures in postconditions\n\nThe fix already covered @Ensures (the method/constructor path in\nAnnotationClosureVisitor recomputes variable scopes for both pre- and\npostconditions), but the original commit only added nested-closure tests\nfor @Invariant and @Requires. This adds the matching @Ensures coverage:\nnested closures with an explicit parameter, the implicit 'it', and\nreferences to both the method parameter and 'result'.\n\nAssisted-by: Claude Opus 4.8 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-06-03T22:09:51Z",
          "url": "https://github.com/apache/groovy/commit/13b009a3243716c000d3ceb7db681883d98bf76b"
        },
        "date": 1780646315346,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.290621080307685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8843170650835079,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.87608473928658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9658473546685302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.8848457583068496,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.002973155604735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.551310407820264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.004048071997342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.240279992817431,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.01325281956255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.353461087113036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8430841990362212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.455011094392081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 177.04843115833336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 87.7285276719697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.71005207692306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 183.12946960075757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 245.17777984444447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 365.0289552166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 297.7091923285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 89.94351348260871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 414.68830515999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6765623623312447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6869467239910356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.476294378716188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.689202102427885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.18090429999451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.000179572700047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.131064916677023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 27.394776191713923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 23.93272397502043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.30610017753904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.759799726957242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.22902376584906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.038043115251902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.361277419424672,
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
          "id": "5499e6c359c403bad892929c34f583170d930d50",
          "message": "GROOVY-12061: Develop a threat model for Groovy",
          "timestamp": "2026-06-03T06:46:04Z",
          "url": "https://github.com/apache/groovy/commit/5499e6c359c403bad892929c34f583170d930d50"
        },
        "date": 1780731128009,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.07538656367289,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8770286975760875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.74581026962428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.979041134357859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.9543504713751316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.311631113089401,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.319459757050925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.47545154279365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.567230988905187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.24917949677419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.337626656144067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8498838427788258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.503677073938223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 178.03125508333338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 87.28612056702899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 156.15120019670331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 181.554776425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 249.5289711388889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 368.61679616666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 299.82640072857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 91.90863352391304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 413.53991529999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6933254027631874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7240115099076199,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.409229715730978,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.763717145174185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.712566426242896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.686290100480297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.875828693272613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 28.41344735352113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 26.04890146249046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 19.088553885833782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.7220297840222685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 41.96495336780829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 21.75445868420991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.8858902473312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "dependabot[bot]",
            "username": "dependabot[bot]",
            "email": "49699333+dependabot[bot]@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "e53f643e0a88d7cc81afc91f4e3234f2c351e149",
          "message": "Bump actions/checkout from 6 to 6.0.2 (#2593)\n\nBumps [actions/checkout](https://github.com/actions/checkout) from 6 to 6.0.2.\n- [Release notes](https://github.com/actions/checkout/releases)\n- [Changelog](https://github.com/actions/checkout/blob/main/CHANGELOG.md)\n- [Commits](https://github.com/actions/checkout/compare/v6...v6.0.2)\n\n---\nupdated-dependencies:\n- dependency-name: actions/checkout\n  dependency-version: 6.0.2\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2026-06-07T01:55:18Z",
          "url": "https://github.com/apache/groovy/commit/e53f643e0a88d7cc81afc91f4e3234f2c351e149"
        },
        "date": 1780818724477,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0884398983585457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9121169885848344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.4047945134779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.20581877212103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.508896827213601,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.973690494123948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.78651866668957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.377025510951588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.606313656289554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 71.13257221305417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.21430251306513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8961568682046256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.649716387858518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.93649268939393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 83.60671108400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 154.27480139450552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 188.38829855454543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 262.5315499625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 381.2905189,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 308.00330482857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 86.60048451811593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 434.27744722,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7099361579635601,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6852077358635458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.285256742189134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.156830594336197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.334810546353925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.428687778240533,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.54252972991535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.661422542048363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 23.753802822661065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 18.867431519401418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.975844667206111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 39.47152897404551,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 22.682997263406538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.486646756090328,
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
          "id": "9f8ebfc8972504541da1c5ec85f80159d1489f8a",
          "message": "GROOVY-12067: IntRange.containsWithinBounds delegates to contains, breaking the continuous-bounds contract",
          "timestamp": "2026-06-08T04:07:38Z",
          "url": "https://github.com/apache/groovy/commit/9f8ebfc8972504541da1c5ec85f80159d1489f8a"
        },
        "date": 1780906491067,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0938453632112848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9119716020904057,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.440756028941866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.1054293834971594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.8509582691178545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.188476340364435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.782280240691858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.37687838414762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.393618247516379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 67.00130131290322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 13.538481502587553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8939308696565048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.65987570997482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 185.38359064227274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 84.63738010780435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 157.38009682307694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 185.4603492181818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 261.2512708625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 379.4885915333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 311.13982344285716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 88.41764579565218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 434.5612701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7113800997333743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6839852464714813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.117483053132087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.237790802668005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.12707922440164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.371915685642094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 10.166338533871329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 29.06786240352078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.935567856765278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.038465344702008,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.063756398293355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 41.538782385884346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 23.04241996543268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 11.464012748394389,
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
          "id": "61740ec67af12e1f9fa111aabfe538ebc11be20c",
          "message": "GROOVY-12071: groovy-contracts: ContractClosureWriter strips generics from closure parameters",
          "timestamp": "2026-06-09T07:20:26Z",
          "url": "https://github.com/apache/groovy/commit/61740ec67af12e1f9fa111aabfe538ebc11be20c"
        },
        "date": 1780991182133,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.156962523789805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9258758597138218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 16.8569266033678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9947858952158284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.913799441732757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.322261549057648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.795956905953162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.183919242937556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.402257213253174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 63.04979945624999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.327308328305879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9064268096525735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.551621341986154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 190.09015620909088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 91.20020548961979,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 165.87054789358973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 196.8892455818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 262.683552275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 405.23237043999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 317.8857230142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 93.1498643318182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 455.37910722,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6778877150419382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6739298990676137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.7277142300799255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.76690716031587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.427056958657772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.317783105590337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 11.23394505481441,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.831143953912857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.113773871359626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 17.340911304738828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.334560343977683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 34.34250864402502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.781333056690613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 10.31801530875126,
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
          "id": "4ce518dc181ce1d5d368af2b7af71f053578044e",
          "message": "some old missing @since versions",
          "timestamp": "2026-06-10T07:51:29Z",
          "url": "https://github.com/apache/groovy/commit/4ce518dc181ce1d5d368af2b7af71f053578044e"
        },
        "date": 1781078959527,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.3198126438196784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9099850742156061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.3242035242035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.077849084984643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.20851900063326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.903754165286893,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.864326378873631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.379326936069475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.845395642216843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 68.6189049343678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.432197051243948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9063525654903181,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.612918854015751,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 189.8707798909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 88.11046928260869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 163.21639125384615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 195.19445031818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 267.9002151125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 388.6669927566667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 316.42359145714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 93.51020660974025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 448.34950514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7091276878156325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.691095267664993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.323444080299911,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.235354603307552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.054042199657726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.400208129031381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.349370106130873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.97971992638387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 19.71887256609177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.022800628158727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.027073726768035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.82713592078238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.533726317188894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.546735868528697,
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
          "id": "5d84d5ff6f06cb735d0fb30b4fa52568e933c2f2",
          "message": "minor refactor: reduce javadoc warnings",
          "timestamp": "2026-06-11T05:24:44Z",
          "url": "https://github.com/apache/groovy/commit/5d84d5ff6f06cb735d0fb30b4fa52568e933c2f2"
        },
        "date": 1781165591565,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0602503697866534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8888378769824168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.967887180573662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9114594397680897,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.975079476487912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.08290258133969,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.300709347707707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.389803905656137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.616573636283963,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.35193268709676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.205669972629703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8454524240033185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.462000128341023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.99632005681815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 91.30885967411066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 162.3679706923077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 187.06392738181816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 251.1307761069444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 368.79988721666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 306.78369857142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.31425944761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 424.88162922000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7031660796389771,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.67989825960873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.491386840017951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.775686955390729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.256030468984111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.626563060399137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.966270019370038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.646134780308454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.783882279097934,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.178730135922752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.677039831146797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.40018646569712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.53593770044681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.859677077898574,
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
          "id": "f8f2f31effe9e0249efd93bf73b310275fced318",
          "message": "GROOVY-10985: additional test for doco purposes",
          "timestamp": "2026-06-12T06:37:05Z",
          "url": "https://github.com/apache/groovy/commit/f8f2f31effe9e0249efd93bf73b310275fced318"
        },
        "date": 1781251597026,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0918030190918668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9105710394817452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 19.485462958392304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.045280522364971,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.8821945091528973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.572029744898696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.626594647492476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.317945434137062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.752461538266207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 70.60359539655173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.23506824730676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9200937476348396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.609870952575422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 191.6178593545455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.7363015521739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 166.26076193525643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 194.36275585454547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 266.4365639125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 383.27718515000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 310.5093164857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 95.04403434458875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 447.11428624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7182427055413552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6832815847970803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.217253610555584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.503858011410694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.138335558796664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.397390975177768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.171807581327881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.58599268910526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 18.002309850639726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.15089214013405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.208646499012128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.397698870580186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.180860438511324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.580362517846562,
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
          "id": "7c3fe7067f8501a7fa368a5dea2273384c13be40",
          "message": "GROOVY-12085 follow-on, align vertical bars for terms containing emojis (best effort works well for modern terminals - may still be out for some typically older terminals)",
          "timestamp": "2026-06-13T02:13:17Z",
          "url": "https://github.com/apache/groovy/commit/7c3fe7067f8501a7fa368a5dea2273384c13be40"
        },
        "date": 1781336795714,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0631019972366913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8765160097311014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.594499726192375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9792904854444324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.066028811729018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.909524476953004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.189561831398395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.054473431696284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.308716379653713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 66.03624447419355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.805573942756395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.849035785215303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.453163460937916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.97751025833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 91.59565346086956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 157.2881609153846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.47975472727273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 253.77593213055553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 373.35749360000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 299.3249810285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.5026294952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 436.27926074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6884659085284413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6820896882507281,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.642551736748945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.68058582409693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.305462295560664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.650270770803004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.727672427745171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.580661457831145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.65051151132638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.6032715011491,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.745795658617647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 36.61103922476145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 17.928595114471484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.477838269018842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "committer": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "id": "44db89efb6e24e05cc5913569f05a4bc0db88ab4",
          "message": "Use `equals()` to compare `Integer`",
          "timestamp": "2026-06-13T15:41:50Z",
          "url": "https://github.com/apache/groovy/commit/44db89efb6e24e05cc5913569f05a4bc0db88ab4"
        },
        "date": 1781424238985,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.087803755110444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9108812233864109,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.92620896339605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0838879885111385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.140390429895927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.058705861551724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.701753906905392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.258551975515726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.856136580819314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 66.95975669333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.450202308060419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8925352166240976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.607489823739614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 189.79346364545455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.79319909664032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 163.31508775384614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 195.86252689999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 269.4123351875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 380.46485435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 316.90975907142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.88688926363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 451.3348560400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7137542010083535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6848536591214796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.254640817656481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.202030752444104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.872177623930892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.493376864322327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.280200571145272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.83650339554983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 21.29932657364592,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.703915805789581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.000660011298908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.805616423416645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.081508065837106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.7410831071880795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Jochen Theodorou",
            "username": "blackdrag",
            "email": "blackdrag@gmx.org"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "72002909d7079f9b60b12459b203eeac234541f2",
          "message": "Merge pull request #2597 from apache/feature/GROOVY-12068/short_paths\n\nGROOVY-12068: add fast paths on DTT for better inlining",
          "timestamp": "2026-06-14T22:47:19Z",
          "url": "https://github.com/apache/groovy/commit/72002909d7079f9b60b12459b203eeac234541f2"
        },
        "date": 1781512509360,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0864901965250144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9144662155229156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 19.317436832026655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.1478777138877128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.9727441488434083,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 15.088254817298509,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.32268328767659,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.086795833247923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.75876699363949,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 68.18384762747127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.341525622639177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8923496250696328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.67156722102164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 188.24035625454545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 88.41729914782607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 159.85565173846152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 190.5576591090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 265.7973670375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 379.90926675000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 314.9926764142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 94.11457382272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 453.34052484000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7188897849532452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6925223030795302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.381556428051978,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.211530876000293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.634936746585486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.390948152080105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.309036566127995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.311520938390178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 21.372658593948916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.50563696546947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.083304680861545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 34.44899461880638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.21573797286917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 8.340547844458937,
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
          "id": "141150926387f5f1739369638d614a74275f2821",
          "message": "GROOVY-12089: Groovy 5 ClassNode.getGetterMethod() can clone a getter with a null exceptions array when @Entity and @Sortable are combined",
          "timestamp": "2026-06-15T08:05:24Z",
          "url": "https://github.com/apache/groovy/commit/141150926387f5f1739369638d614a74275f2821"
        },
        "date": 1781599779421,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0960911199069057,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9123727869044558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.605554605064242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.00366585477522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.6433002236380836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.531286422852741,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.789569636719026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.30527776706407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.408581236933413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 70.6971165450739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.520909144637042,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8928829016808735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.649863399457724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 189.66934695454546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 88.98303095219038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 161.98962902371792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 194.6789605581818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 267.27565975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 380.51866574999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 316.1943458571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 95.40248483333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 454.0543643000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7206252026555287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6834094174149243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.295113420194843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.267657681815535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.548695705789703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.454218435034468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.361535845370105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.376431890362266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 19.52782146914146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 13.024089066393376,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.074257033430117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 30.760097766946387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.514836811591152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.761501602370819,
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
          "id": "72f671532a630f4b09b28e46d8dbcb517bcf5f4d",
          "message": "GROOVY-12092: Allow a \"groovy.indy.callsite.cache.cleaner.thread\" flag to turn off the PIC-Cleaner thread",
          "timestamp": "2026-06-16T13:20:08Z",
          "url": "https://github.com/apache/groovy/commit/72f671532a630f4b09b28e46d8dbcb517bcf5f4d"
        },
        "date": 1781684454469,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0544595858374157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8760561400008748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.7155050150954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9353078324814725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.7434442305716877,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.50338280220409,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.363359301364227,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.240084432258474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.64813314604432,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 66.68340800817205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.17262087202791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8460696953754239,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.51999205985221,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 185.24208515454546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 91.56971196126483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 159.4373153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.40632242727276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 256.95010917499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 373.3515408833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 305.46829602857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 95.63947488463204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 429.8126072,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6724447392730255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6808817983230562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.472464942099907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.751073597143707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.36047456273312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.7294455939050595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.59470233615021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.30276899957009,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 20.351989064374756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 15.772542723615913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.67683080762006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 34.29186458486086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.02882863167839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.56101656616991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Eric Milles",
            "username": "eric-milles",
            "email": "eric.milles@thomsonreuters.com"
          },
          "committer": {
            "name": "Eric Milles",
            "username": "eric-milles",
            "email": "eric.milles@thomsonreuters.com"
          },
          "id": "8f560e7046b39ce677e5eaa5f7613c1ed5c65b6b",
          "message": "GROOVY-12091: fix type of trait property placeholder in setter method",
          "timestamp": "2026-06-17T18:24:38Z",
          "url": "https://github.com/apache/groovy/commit/8f560e7046b39ce677e5eaa5f7613c1ed5c65b6b"
        },
        "date": 1781770231430,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.087261910618133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9081035771365105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.825231617321407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.101016703577179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.6611274355666517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.510569779216585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.50013788438194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.129625018243322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.775751890107715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 70.4677005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.166853461036395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8941468739324472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.638970074213989,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 187.4100088181818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 87.83589210869566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 165.1196594987179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 194.7051652090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 266.1845832125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 384.42739796666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 314.1157842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 93.43141086363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 450.23663486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.71175368888985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7063541845811243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.393569033862821,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.38371056355528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.210651760967759,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 9.156508540199498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.08616378982374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.758970687562556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 19.50289921062459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.146193156008607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.112887768106764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.43253162141362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.308711785281144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.668134937278788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "committer": {
            "name": "Daniel Sun",
            "username": "daniellansun",
            "email": "sunlan@apache.org"
          },
          "id": "1bb22751a4d4027eb53f42f524502e001c98561e",
          "message": "GROOVY-12094: Bump jline to 4.2.1",
          "timestamp": "2026-06-19T08:16:39Z",
          "url": "https://github.com/apache/groovy/commit/1bb22751a4d4027eb53f42f524502e001c98561e"
        },
        "date": 1781857300775,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0908986928522135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9370531258724677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.447462059293237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9903965246692157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.1519424868068215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.323052362982583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.729851894269379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.443625078997815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.689136390201472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 70.08087767241379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.606759434390334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.895938722142944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.610170064282146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 188.40940420909092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.32449348636362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 161.70001726923076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 197.94985388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 265.120602075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 383.53978333333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 316.11122935714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.59510632727273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 454.45648804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.715747260132062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6997438439561274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.344539626380861,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.226433611333125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.270495072184099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.41636884153903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.020167544278952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.820955013890995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 21.488952831287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.1584289379785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.00119157123155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.567569431445136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.026762391547297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 6.8699524573213875,
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
          "id": "9488b37ca09b89db266a82453f34e1e3f7b75a72",
          "message": "use basic cache-provider",
          "timestamp": "2026-06-21T04:04:22Z",
          "url": "https://github.com/apache/groovy/commit/9488b37ca09b89db266a82453f34e1e3f7b75a72"
        },
        "date": 1782029200891,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0863967760740945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9104545384454665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.827973713589852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0732060294109145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.691518557618004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.991483678851589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 12.357429198202817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.405992756870088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.432108844175879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 69.07080864896552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.28708769991957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9101187554299572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.597291610547119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 189.91330436363637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 88.12724549039855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 158.1075661230769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 193.1820643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 267.83077042499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 383.55378798333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 311.61911817142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 93.64595377077922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 453.2105681000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7165049711789604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6924305683593825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.40685823107306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.61478117611755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.638123293453305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.397657296734844,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.256436459880614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.84467764145071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 21.100621142719298,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.099242220141075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.9878322126767625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 31.453151301801945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 20.74949981597787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.777624898832052,
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
          "id": "9488b37ca09b89db266a82453f34e1e3f7b75a72",
          "message": "use basic cache-provider",
          "timestamp": "2026-06-21T04:04:22Z",
          "url": "https://github.com/apache/groovy/commit/9488b37ca09b89db266a82453f34e1e3f7b75a72"
        },
        "date": 1782117675011,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0621839272503124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8763615541650724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.980431341939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.881475341534271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.262618997055201,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.966120272646398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.278741981895973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.162228612156362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.404759309769734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.30815598004033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.183249460486124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8455898968361172,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.50345177108778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 178.49155004166664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.9517592347826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 154.93831752692307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 182.15280109015148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 253.17197795000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 369.7017478833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 300.9855031571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 93.01244334090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 433.86004358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6684553360181613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6789490360719572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.5661529465546895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.868899663914423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.457622479056962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.900392178003739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 7.8643572768693035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.95363898529831,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 21.672489072227307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 13.832937049789681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.662355923951182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.80028953309219,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.40740177125507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.875583530380046,
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
          "id": "4542f412672781a05bfc8742dbbdc925b4da5352",
          "message": "scrub failed resolution - seems to be getting cached",
          "timestamp": "2026-06-23T05:19:11Z",
          "url": "https://github.com/apache/groovy/commit/4542f412672781a05bfc8742dbbdc925b4da5352"
        },
        "date": 1782201273777,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0554862878132667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8790167450610346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.706013072530887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9696122660271254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.7836750227091387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.650102330784318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.139677837948705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.246454340050324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.282754457034738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.48346039153225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.024044291883353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8474773782206894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.46810189320029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 183.03253676893942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.50470761976285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 158.82918183076922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 187.2259068909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 259.56787792499995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 377.65128834999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 305.74463958571425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.2438582417749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 433.24989492000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.675956050450639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6860806476002512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.479292980985552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.871698315688317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.192333741427728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.852991857558537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.641399686977435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.140165864674806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 20.98473364578947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 15.612973720875624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.790279258157689,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 34.71647469101992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.70276114204075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.612372242078012,
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
          "id": "5252125f426e93c538b6724cfd086ba73c2cda4f",
          "message": "GROOVY-12100: provide Closure variants for AGM primitive each",
          "timestamp": "2026-06-23T11:17:47Z",
          "url": "https://github.com/apache/groovy/commit/5252125f426e93c538b6724cfd086ba73c2cda4f"
        },
        "date": 1782286804707,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0523101066120941,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8762994637552808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.449912472754676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9580808095195272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.357167585733972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 12.733134125598644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.188716383935542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.081023212946608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.375309347826288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.59721132580646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.17866954446913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.845996814834152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.453728332376567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 179.948266875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 92.28065010197628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 156.85522840769232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 185.6606975090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 253.8654119875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 372.6489979666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 301.5643776142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 95.63420553939395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 423.3538469,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7022864209977164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.680948776718219,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.4668607211857125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.827037158010455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.512649151131754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.875286085385165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.43329211590683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.140528496540686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 21.894248463990813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 15.310095756046351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.739688118527243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.118416518500794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.424429288834677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.498167254494119,
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
          "id": "49565020a078f78e514ad1fb0a3650af5761dd15",
          "message": "Traits: add two @NYI rows in the trait matrix test for discussion",
          "timestamp": "2026-06-25T04:34:40Z",
          "url": "https://github.com/apache/groovy/commit/49565020a078f78e514ad1fb0a3650af5761dd15"
        },
        "date": 1782373250374,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0896077010902014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9145850067481438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.502791007096985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.1857650346723587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.9342339502795625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.238791212818754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 11.886786875588234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.232990488553531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.760068570635484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 69.84178089571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.360899243087761,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9512386191123083,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.6223461160396075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 189.6283142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 87.33455685144928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 160.5026892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 193.7418735090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 272.61186392499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 387.79779548333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 315.3332433714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 92.89942590909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 451.95592302000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7122158379310453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7000058713304926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.414828387379423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.310331551431407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.258049299258555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.37606801751881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.068782717459763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.86274921511653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 19.698248456300423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 16.085952492535483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.165733834435833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.405348794826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.366524074206502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 8.556146153130767,
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
          "id": "e4a9bb8cc11052987427da7802f28abe7a818ade",
          "message": "test against jdk27",
          "timestamp": "2026-06-25T11:48:02Z",
          "url": "https://github.com/apache/groovy/commit/e4a9bb8cc11052987427da7802f28abe7a818ade"
        },
        "date": 1782459999829,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.155209933123625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9131487509058223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.215411432201556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.035134962451829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.219515842563783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.766829579532319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.727719137076207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.31454940935478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.873695592834899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 62.673293346394686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.38499340717675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9076916188945372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.569834793965604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 188.96275972727273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 96.64360941869565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 177.83177187500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 195.80063712727275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 275.49230788750003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 400.4484917066667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 316.35144539999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 101.1920660804762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 446.03464771999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7165428107231027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7014348145942424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.869532584146475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.75732547310853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.255108545517697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.461744162406097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.159451879788492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.31863373692968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.399970843665876,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.851773811591357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.300761988488383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.49387191929303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.43872477858461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.585283546263187,
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
          "id": "568bf980dfd617c3e98d62b51ce38d74e9ae3490",
          "message": "GROOVY-12108: Provide a more informative error message when GROOVY_HOME is not set correctly",
          "timestamp": "2026-06-27T05:13:08Z",
          "url": "https://github.com/apache/groovy/commit/568bf980dfd617c3e98d62b51ce38d74e9ae3490"
        },
        "date": 1782545564639,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.2910026749532277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8784811611795635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.771549241510023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9623802026882062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.7927258517028135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.143594902987271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.58536760067743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.075828907385016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.625673322424914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 71.26870733249702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.352467715792429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8464455633697849,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.470829906284405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 181.66623891363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 91.30273711660078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 159.86224803846153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 188.11861123636362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 254.09071991527782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 366.5915151666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 310.79732265714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 95.03090124393938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 425.87030098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6867961435645114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6793569133659676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.586377467404686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.69466156825149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.262065102022294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.08201799971189,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 7.814751481698508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.729019343406765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 20.752180523521762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 13.63986228218169,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.68465303643611,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 34.09692460087181,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.22728242816004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.539279564308939,
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
          "id": "304832bd265bd8fd91541b75554b236659eb44f8",
          "message": "GROOVY-12112: Reject qualified Trait.m() to a @Virtual trait static with a clear compile error",
          "timestamp": "2026-06-28T02:45:22Z",
          "url": "https://github.com/apache/groovy/commit/304832bd265bd8fd91541b75554b236659eb44f8"
        },
        "date": 1782632944204,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0526123145709205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8749474925382644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.682589224477603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.7842096526415743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.15412866660432,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.508368393074864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.416731391336148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.205089714005329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.563323077586784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.21089576853493,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.454361947368806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 1.165407362717421,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.54214363676498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 182.05961631893942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.66123377648222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.50903471208792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.47591851818183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 255.48085423749998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 380.67620623333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 309.26688137142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.16820099090907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 428.77471068000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6888800372412003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6990864435941764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.499730038102561,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.945525847897967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.410543180073054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.8623371175958585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.632397792047518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.685617603298244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 20.46800821597072,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 15.005267249930602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.829677895065404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.27547505065574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.440548514997676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.255767724695017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "dependabot[bot]",
            "username": "dependabot[bot]",
            "email": "49699333+dependabot[bot]@users.noreply.github.com"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "93f3d169375b5a8129496bdef416200210e44dba",
          "message": "Bump com.gradle.develocity from 4.4.2 to 4.4.3\n\nBumps com.gradle.develocity from 4.4.2 to 4.4.3.\n\n---\nupdated-dependencies:\n- dependency-name: com.gradle.develocity\n  dependency-version: 4.4.3\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-06-28T01:22:31Z",
          "url": "https://github.com/apache/groovy/commit/93f3d169375b5a8129496bdef416200210e44dba"
        },
        "date": 1782720952270,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0516065501946101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.873329174916147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.07012666151847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.933659981202869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.080165049873508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.29558124769087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.582549725456179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.210596125485093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.590324070164874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 67.39858821688172,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.183510502967456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8445008625416769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.451354050297727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 179.818509780303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.42649964150198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 154.63224703571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 185.4223408909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 254.772343825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 373.18570746666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 302.2385893857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.15013518679655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 434.49774290000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.681831952423946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.7783520736338319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.426429603068246,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.690909858543318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 13.74075052544806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.76051956882443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.38866596844056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 25.16399295612308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.12841895553503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.361930228242025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.705347245984649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 32.052262901739724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.894667887485618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 6.96075339088969,
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
          "id": "416652c34aad08c6b8b2cc1003183ce4d729acd5",
          "message": "traits: additional tests added",
          "timestamp": "2026-06-30T07:00:29Z",
          "url": "https://github.com/apache/groovy/commit/416652c34aad08c6b8b2cc1003183ce4d729acd5"
        },
        "date": 1782805602556,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0618337213815237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8890348061391263,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.516990389084423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.9312236189858067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.198545787109825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.092223371072881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.377619567199162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.184733383226177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.395174413326522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 66.59248539258064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.244023677965055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8496678205947242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.484236833158688,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 181.1519707340909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.34817915355731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 156.95053803076925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 186.1776378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 248.64990984861106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 366.4042857166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 301.6187310857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 94.76772060865801,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 427.39228649999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.7563378553754312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6855896349285788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.600635485186328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.865037797156962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.74318608264493,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 6.853508375748956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.245440481635926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.71113798898075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 24.026637762821373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 15.093385772913374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.705127453591765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.74608693034747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.50457956340469,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.711809898850847,
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
          "id": "cb58b93bad4bd451365a68fe8e86fd26de1a8539",
          "message": "minor refactor: documentation tweaks",
          "timestamp": "2026-07-01T06:28:54Z",
          "url": "https://github.com/apache/groovy/commit/cb58b93bad4bd451365a68fe8e86fd26de1a8539"
        },
        "date": 1782892907520,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0701900018993544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8735534712796665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.75908126235577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.957641441009345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.563228212711743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.211562417595118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.188899245237145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.107429305837375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.484171467727768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 65.10457871979168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.23506188205305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8435589860788768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.449958503662926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 180.2808708719697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 90.32254389802372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 162.6027725346154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 188.82280867272726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 253.63321086527776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 370.23543574999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 298.39356664285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.25178145238097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 429.42091994000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6788993006714272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6819380120371907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.893210991567173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.653195810526285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 14.330126921869368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.367452594353253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 9.18971984259484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 26.19318729182884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 22.013366267263258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 13.805626675733237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.697810034308574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 33.858163591610165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 18.795974698622338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.617152480799599,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Kartik Kenchi",
            "username": "netliomax25-code",
            "email": "netliomax25@gmail.com"
          },
          "committer": {
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "210ab01eb20784f3c07444caca3a3714de01f09c",
          "message": "use nio temp files for owner-only permissions in tooling",
          "timestamp": "2026-07-01T09:30:08Z",
          "url": "https://github.com/apache/groovy/commit/210ab01eb20784f3c07444caca3a3714de01f09c"
        },
        "date": 1782977722271,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0922685573170443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9184774833175856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 18.51784706833874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.0977801348351734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 4.225693794640651,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 14.068748269948774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 12.196678801049947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 11.732913441801141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 11.57438486272743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 68.4046286316092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 12.857288455009755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8918138888905144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.632233171664872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 186.25629465454543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 87.95670927826086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 161.39461836153845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 195.10514198727273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 267.7992420375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 382.79327505000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 318.60026944285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 95.01158609848484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 453.53137446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.711222235766586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6843689024691066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 8.368533122671687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 9.284809882096123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.216174971583635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 8.420844148133945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.278997479900337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 24.888038799050456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 18.29897431887553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.472409709517814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 6.012131230204226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 31.342880473687423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.44099230021471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.6758838757196175,
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
          "id": "80d37ed6cc39b365c4f326c028363db39aaa03eb",
          "message": "GROOVY-12125: deleteDir() should treat symbolic links as leaves (cleanup and hardening)",
          "timestamp": "2026-07-02T21:58:37Z",
          "url": "https://github.com/apache/groovy/commit/80d37ed6cc39b365c4f326c028363db39aaa03eb"
        },
        "date": 1783063948992,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0602816718524506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.8757154203835323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 17.68609377110002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 3.068399284129648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.8891286515971863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 13.117026860930016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 10.503477285431881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 10.023755413854198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 10.339190103002997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 64.31312329445565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 11.361707730991684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.8403182844758537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 4.444350864919594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 180.5433720537879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 89.21251117391304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 155.45244346153845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 185.60691204545455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 251.0775288625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 367.1537123833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 305.8166341142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 96.54293928095237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 435.32876408000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.6753762870816693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.6811538884919108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 7.574013372268597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 8.815929278080748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 15.263520421730458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 7.768246152805349,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 8.700115162712923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 23.98935670772184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 20.422418635178055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 14.075895690950324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 5.78493695190242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 31.65369531291857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 19.03360557973516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 7.46948534295573,
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
          "id": "80d37ed6cc39b365c4f326c028363db39aaa03eb",
          "message": "GROOVY-12125: deleteDir() should treat symbolic links as leaves (cleanup and hardening)",
          "timestamp": "2026-07-02T21:58:37Z",
          "url": "https://github.com/apache/groovy/commit/80d37ed6cc39b365c4f326c028363db39aaa03eb"
        },
        "date": 1783149959986,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineHotLoop",
            "value": 1.0051253442251293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineListSize",
            "value": 0.9552439829878022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineMultipleCallSites",
            "value": 10.541078142218138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.baselineSteadyStateNoBurst",
            "value": 2.8730413261765455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.burstThenSteadyState",
            "value": 3.91292914716428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery100",
            "value": 8.761530740201199,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery1000",
            "value": 7.412712552706121,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.crossTypeInvalidationEvery10000",
            "value": 6.685149043017574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.listSizeWithCrossTypeInvalidation",
            "value": 6.961479781144854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.multipleCallSitesWithInvalidation",
            "value": 40.170468232196086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CallSiteInvalidationBench.sameTypeInvalidationEvery1000",
            "value": 7.388314194421943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineDirectCalls",
            "value": 0.9412202745617119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.baselineEquivalentWithoutCategory",
            "value": 2.7201004750198456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryInLoop",
            "value": 103.709095215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryPerBatch",
            "value": 48.694381929907095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryShadowingExistingMethod",
            "value": 90.06984933913043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.categoryWithOutsideCalls",
            "value": 107.13056360666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.multipleCategoriesSimultaneous",
            "value": 151.62903729285713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategories",
            "value": 228.10149554444442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.nestedCategoryOuterWrapping",
            "value": 173.76672915833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.singleCategoryWrappingLoop",
            "value": 51.63798481025641,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.CategoryBench.threeCategoriesSimultaneous",
            "value": 265.29684325000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.baselinePlainMethodCalls",
            "value": 1.2069484929948728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedDispatch",
            "value": 1.2255209135518115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.defTypedPolymorphicDispatch",
            "value": 5.2554311125245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.expandoInjectedMethodCall",
            "value": 6.898002700228017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodAlternating",
            "value": 9.997561540608714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.invokeMethodInterception",
            "value": 5.2201570265705115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingMixedWithReal",
            "value": 4.605576248983735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingRotatingNames",
            "value": 13.72852552979207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSavePattern",
            "value": 10.621825504410255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.methodMissingSingleName",
            "value": 8.903200915083952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.mixedRealAndInjectedCalls",
            "value": 4.583089484720862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingReadWrite",
            "value": 19.598761244260423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingRotatingNames",
            "value": 12.367660309837005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.DynamicDispatchBench.propertyMissingSingleName",
            "value": 5.956252474045649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}