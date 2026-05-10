window.BENCHMARK_DATA = {
  "lastUpdate": 1778455929592,
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
          "id": "160c9d57d19dd70b97fb7d3f286e285cac100496",
          "message": "tweak performance test which has stack overflow in some environments",
          "timestamp": "2026-05-10T23:27:04Z",
          "url": "https://github.com/apache/groovy/commit/160c9d57d19dd70b97fb7d3f286e285cac100496"
        },
        "date": 1778455928389,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 363.6163640333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4827.9335123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 374.71172502999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 141.27915074380957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.896668106431164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1371.7025036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 658.5583449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.03945127189566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 248.7211193083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 265.8683239763889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.25306232423027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 276.02192954900795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.896503081609826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.047028782378545284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.906324745466529,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.98681722941176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.354399641281386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 342.45712331190475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1667.0903117999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 644.9350281750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3551067956641634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 123.58800371691174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 277.04236815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 174.4005580666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 365.24674916666675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 10.03815801884204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.34642044469118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.598946028585598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 27.617189114491822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.3455924592245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 978.2444924166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 990.4688760666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 562.288828125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 713.9515274333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2175.2435932,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 829.147003025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.11658111531763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.699764430961487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 81.39631390650872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 120.44542036229116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 344.18034740714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 825.6130784666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 530.198958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2119.6956880999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}