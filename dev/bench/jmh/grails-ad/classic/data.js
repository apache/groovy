window.BENCHMARK_DATA = {
  "lastUpdate": 1779002695001,
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
      }
    ]
  }
}