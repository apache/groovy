window.BENCHMARK_DATA = {
  "lastUpdate": 1784099989976,
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
        "date": 1778458879267,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 401.37050066000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5901.3475826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 404.25767097999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 151.4295697076923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.6746727502693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1545.0348948500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 985.5390367166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 105.47254303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 261.09859961250004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 274.17297593571425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.85546810787639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 283.51671477071426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.383483805724094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.044567617918256634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.389499855712113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.56328108366013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.563765071423113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 300.61072607142853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1777.5220020000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 744.0013498166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3672792924825636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 159.20763402863633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 319.77837575714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 177.8289337037879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 341.26007673333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.810755128542166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.007177168190736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.472151155739429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.676307448300044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 93.63580780690513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1023.8812015000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1009.159536575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 745.9761553166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 963.4428638666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2541.0311104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1008.4529116416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.88784693162558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.021823682158562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 88.13062897557509,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 133.83480517041664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 372.9139099847619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 817.9369349333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 536.2212119000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2450.8269072999997,
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
        "date": 1778485885833,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 415.67427738000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5931.0207649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 401.5951228833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 152.06524637692308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.97169997660022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1593.3654492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 970.3316091166665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 106.10585273578947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 265.766397475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 261.1741184646825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.19138289218422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 293.2553531625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.972761792708653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04390949028831613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.567324334211412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.56949850980394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.78905051868001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 316.6632671142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1799.6183071500004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 915.0621271500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3935619381538887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 154.3323588093357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 323.48175667142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.67218018106058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 330.1464286095238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.84552356192722,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.978688673842687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.3707655203411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.718740561350398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.30467476579258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1096.1392384666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1120.66042045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 745.40701815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1055.7918079333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2354.14041185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 998.2995008999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.783894763760166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.157549210821756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.68220119879297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 129.85282370252452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 351.6331161190476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 818.0862150333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 530.8942783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2416.49813655,
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
        "date": 1778571385620,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 395.1787188133333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5597.8904452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 391.3281823266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 149.5028157736264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 47.091576190393134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1676.8595897999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 971.7142145499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 103.16451587864663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 252.36819189027773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 265.0045500827381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 31.9680118597403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 277.55750522380947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.276360455086547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04454903370366199,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.413051290129015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 117.23434856862745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.5552675565323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 297.50342302857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1770.9913613499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 846.5638606916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.37352065768354564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 137.58352566095238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 317.9403345142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 185.4373119489394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 338.1609427666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.840303858357672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.08425333681284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.344596257529115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.891053905115314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 91.68182611859353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1173.2714454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1145.8104884999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 718.9968635166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 965.3063063500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2510.5233565000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 905.1068456333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.66824428927061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.28863035961499,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 90.67616573536061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 143.8064738359661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 344.22732762857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 816.7728902333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 527.61779185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2437.6571710000003,
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
        "date": 1778658476298,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 367.00529498333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5802.178866200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 401.7740131466666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 151.03147165714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 43.97334180757575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1434.4117991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 935.4665213833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 108.57890809305816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 259.336458825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 270.7994396412698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.7636709339369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 281.72302134305556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.015681270638318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04542405065132586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.415287330685695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.7335908650327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.910279703473957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 300.78435211428575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1880.46097325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 886.1483071833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3999711154069786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 171.36545053339162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 322.10440336666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.006741675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 357.5153554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.861400355340953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.835312437095997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.416543792996828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.85217818055303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 99.28709228224197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1021.2073323833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 967.9144411833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 732.4106246083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 930.8216423500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2000.40340895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 941.1007691166669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 46.317783629083394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.466496539232377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.55545419029154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 150.18620328078174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 354.84832574999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 819.4120630666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 535.755696275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2509.7426157,
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
        "date": 1778744235291,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 346.88286722142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4147.0657591,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 362.16732389428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 139.75682387095236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 40.75357398714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1370.9639805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 661.5993877750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 91.59634507985601,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 248.81614696111114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 259.5623826904762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.29510546253118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 287.61445689841264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.112402438269406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.046395631606469055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.624405413341417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 115.11600145555553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.72875691403079,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 287.90469907678573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1656.6623422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 635.5031353166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.35183809826927465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 131.92731434707792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 280.0707825125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 170.46498431555943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 314.9651744428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.33468062192672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 28.116141754765852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.694540469300014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.610734313675625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 86.67608424718239,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 931.3533240333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 735.5721208000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 576.489399875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 757.7948166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2180.8111268999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 880.8024173083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 45.01365192607834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.441615435370966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 82.34613526752428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 119.57653618232777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 307.33607477142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 848.8911483666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 533.64663445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2175.4710172000005,
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
        "date": 1778831100480,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 404.77073033999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5786.2857153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 397.73017333333325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 149.54879709835163,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 44.04329068672629,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1373.2250232000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 965.55762675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 106.13053527263158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 260.90016778750004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 258.01381612976195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 38.2814558550291,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 284.09464907599204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.58810533511194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04454913047263885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.163177082671258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 115.76501169444445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.899172373063738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 298.4642458285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1772.5214566999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 862.2066256416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36837055757608916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 153.23207192959708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 315.2638570857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 175.42174575606063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 342.52526097619045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.883206650327347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.139347802663753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.305533033872404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.06959442825556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 95.7386183483715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1126.7582212916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1068.8290641333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 711.0067518916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 990.8396907333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2467.6589661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 965.8159710999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 46.727415288900275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.39836443835818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 97.9435293780449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 138.1570322864354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 339.97609634523803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 804.6670398666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 518.3776356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2396.9034125999997,
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
          "id": "7ecdd0a8e793af578a529ecb0e098fe711b3528b",
          "message": "add branch protection",
          "timestamp": "2026-05-15T11:16:14Z",
          "url": "https://github.com/apache/groovy/commit/7ecdd0a8e793af578a529ecb0e098fe711b3528b"
        },
        "date": 1778843918446,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 399.79699587666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5707.043699100001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 410.21646681000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 153.32808312967035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 45.32017364666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1535.2722961000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 997.7383467833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 107.38924182997076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 255.9198178125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 274.5928796498016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.43278341535228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 298.26650841428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.42366057490032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.044405582217468174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.331646091300932,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.67976919803921,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.924276736972235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 302.9203977285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1793.6160539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 772.6347251666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.39355321637942525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 157.22375971412586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 319.9033249285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 176.3612478280303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 331.5821786547619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.909889336212364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.307842630094903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.299588861572053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.669544158579846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 96.8078968039035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1082.7487241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1005.8971110333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 602.028911425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 841.8404107916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2173.87402625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 973.8913541166664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 46.77960214085972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 25.68435919746629,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 114.18775695390579,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 141.37151964001345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 364.38620439047617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 823.9949332333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 530.346609475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2424.5697322499996,
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
        "date": 1778916145889,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 383.82236817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4292.401794700001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 362.5493889880953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 140.5342697561905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.494753656028365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1380.1808933999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 648.222394025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.40840047348486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 253.81887287777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 276.62088821706345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.071724274123525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 275.69815645912695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.69802144355103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04815888394415106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.713026112015886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 121.1013697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.50214572287225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 331.9159974642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1725.6520281999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 725.1601978499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3477097112073304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 146.9224445797161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 276.510480775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 174.52873574423077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 375.0250450333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 10.226223163931033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.43971799615009,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.376701516389321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 27.395999573253448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.85216595109836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 916.7163759750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 873.0589474166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 788.2778951999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 824.029347425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1867.7744879499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 924.3435981666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.06908981307821,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.79698785203317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 82.32959204246924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 122.71692773671138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 346.98246904999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 831.7887232999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 532.5520424250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1946.5879336499997,
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
        "date": 1779003250677,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 387.33334132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5956.8157725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 406.7844302266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 147.7845127419414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 45.34247163336703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1693.5441904500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 989.3106589666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 106.02409628342104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 255.30751812500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 276.1535323049603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.68971483703619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 306.5061813857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.68756505715167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0439037007677517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.351398922206334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.24563138235297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.567120442348923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 312.0945645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1826.15836395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 893.4437113083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3876573975722262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 143.60894875809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 322.8963993761905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 178.1878746787879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 330.27643662857145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.847395737524034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.0394032571502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.392795876827533,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.10693665187356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 96.71295731383427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1152.0537179500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 998.8984689750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 601.453572875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1000.5500927833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1642.2761972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 903.2750617666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.329005318368864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.318684286783007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.11435069012346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 143.79695342923998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 335.7985075666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 831.5418700666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 537.9565956499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2449.2914828,
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
        "date": 1779092108828,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 350.11783439761905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4643.6016858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 358.61173953809526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 145.79026242857145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.15712535765165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1374.1043562999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 646.4431007333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 90.75859548389329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 240.54508026666662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 256.7752207422223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.62569336374869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 273.47690063611117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.18470908985769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04861752837952262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.244926061739756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 113.29924694444443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.98959337090535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 301.13939835714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1655.4838463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 643.2166627333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.34102505746423134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 129.38487531210436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 274.2694273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 165.5608093826923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 311.3281779285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.34721622677581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 27.796937427011414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.710358333591312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.535258230844725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 86.00950553259113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 863.21825885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 884.8912203666669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 569.5934488999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 867.5274241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1976.7736937000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 778.4805222333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 43.616865936153175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 21.84219045495646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 79.38829275268864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 120.82234089534131,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 296.5336819285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 848.0120880666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 548.540311025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1803.1347893500001,
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
        "date": 1779177954401,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 269.5701868625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3458.6104341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 283.12784831428576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 105.60714540157895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 32.40386162980874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 970.3720428833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 494.61911600499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 70.51369760427022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 187.73834722727275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 198.73053692015154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 30.9843784924104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 224.8518778611111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.705060787655675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.03771733969403537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.722606859931216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 90.07700016956522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 12.794027147585334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 258.4217362125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1367.7504470499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 617.0092778916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.27126167320076183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 114.63346492291453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 217.59132746666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 134.14878892708333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 276.01621255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 7.549792715417325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 24.894756428641973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.074798611586044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 20.378876972837972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 77.56430659439434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 796.9101989333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 729.7793835166665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 529.5230878816667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 667.9086658833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1609.0072781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 609.7898818983333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 39.6568557030327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 18.649339944148203,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 71.31352154785131,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 100.12495429325797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 279.8477814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 644.2590995833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 406.8652179266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1540.8431771,
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
        "date": 1779263846523,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 371.1477210033333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4668.9606011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 379.79373537000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 141.43239040761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 43.74697630624628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1419.779559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 635.8686138416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.5114457134632,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 246.59752998888888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 260.28884477500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.43552347136092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 272.497287181746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.956989125457717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04601445175028468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.723042504749701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 117.70087467941178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.614490257679183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 332.15295470714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1717.38517755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 678.8389092166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3574685085123287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 146.4064045390476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 275.658294025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 172.05232707564102,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 372.75119226666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.798557658695191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.20105373236801,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.383479056746117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 27.012936520849838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.82188395556767,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 862.4335871999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 944.07471945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 565.0384889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 821.4433025833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2115.88381535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 964.44712205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.050784487282584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.729226414912567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.15841624476707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 119.04197570401786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 339.54075144285713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 847.8204385000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 538.570015625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2204.32438035,
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
        "date": 1779350080538,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 364.1442264666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4287.223184500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 373.86507487999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 142.36461283095235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 43.6688026467298,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1251.054390816667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 635.6459544749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.80230712685464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 250.78231827916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 270.8509294494048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.717093065665146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 276.3288394801587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.041008508968357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04614230498472193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.392678108938414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 120.81720042279412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.003757336640327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 355.8191796333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1666.8191614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 689.1339825833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3454909215405994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 144.58928820761906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 277.48653273749994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 174.25418259469697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 378.03176200666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.733850365341649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 32.90445787834215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.42272021254142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.66888218477821,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 86.7866183792917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 995.1414230500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 884.8350092166665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 569.733820075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 813.5972377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1979.8588605000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 949.2652719333331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.14501026494587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.232818157440065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 83.41559861708886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 126.10596984768699,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 368.5255891714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 827.1701977666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 519.5176742000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1991.3879691999998,
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
        "date": 1779436370450,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 394.9871084666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4208.756260599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 397.1584363233333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 142.29347389333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.4383807502643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1369.58227735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 638.6124575083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.32509685535197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 249.8167832958333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 272.4252254178572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.90172558570926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 277.56837576964284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.59567890581749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04569541991279254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.375422884716732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.94289112352939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.35829135152008,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 350.12640011428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1692.8022914999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 652.9244101583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36029788631091036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 134.9881931570028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 280.3686061625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 173.30123161666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 370.33160436666657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.69140928887063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.05103487393619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.33000529152931,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.709378292594756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.32258148485633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 797.5390024916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 885.69032485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 677.6260285249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 818.0545265166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1755.1804226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 831.016726975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 51.28984654845202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.30877908631311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 98.12059769593947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 123.68190986269842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 360.13028897619046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 830.5115910666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 540.3890734500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2058.58791,
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
        "date": 1779521505671,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 405.88887646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5704.7369519,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 402.6468142466667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 151.10566191538462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 43.48136158708192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1514.8086867000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 897.7864944666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 104.75216309993735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 259.1062574305555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 264.26150316527776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.26439547364791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 286.2952187725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.45997588782018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04450782373370684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.553418764370567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.07007145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.6243077713202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 322.0741796476191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1774.0530605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 799.2229824916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36626907096292666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 165.29433906346154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 312.04606741428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 177.5312257704545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 337.70658921666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.841140264367574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.124885344819887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.332021145227934,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.408011638521764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 100.57754342395216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1023.6056745499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1070.2111943999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 712.9423661666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1034.3263055333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2177.72939065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 926.7716953166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.80895173867254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.69652596386694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 102.02905318440554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 150.89512853076388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 365.82778924999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 812.8553674333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 523.0301272250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2303.8147729,
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
        "date": 1779608518674,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 387.83850406666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4738.0395615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 379.45028185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 141.5931121647619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 43.12822962471655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1402.8035267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 617.5806107083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.73862960121328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 248.4504921638889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 264.3799096402778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.16734877355631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 265.60825663749995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.630645598197358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04588772257704601,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.436353916615476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.85510149999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.038102831648835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 351.36414988095237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1681.3206913499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 677.7220968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.34283656813915914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 129.99066835738796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 274.8813672125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 171.30165564009323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 359.4163955333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.783001407060398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 32.94989293189103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.338360246057409,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.5075935078872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.35234541635802,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 877.8330208666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 796.8355070916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 656.4886981166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 881.7464873333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2171.6150316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 804.9269694166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.75592400660401,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.426147737967217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 85.2675668019795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 123.81381396003519,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 350.93577266904765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 828.2581655333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 523.644200175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2110.47611585,
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
        "date": 1779696947917,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 383.65148483333337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4527.030905599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 390.68946553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 138.12329079988095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 40.84865724432653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1305.8819602333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 632.1872805333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.360755883527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 242.79275053333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 261.50687392500004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.9526859976026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 283.75183986607146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.215403312987526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04631519950387546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.518804229219366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.29943742875817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.97434327018182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 324.308895652381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1683.0380419499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 685.5402521333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.34453828837479017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 146.30008164257328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 279.4758856125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 173.81684923333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 370.9815817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.767247435302275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.00599097814344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.36636093606536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.488826550443168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 90.2748453437473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 810.9570845916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 931.0825677833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 562.4474335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 643.1271807083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2149.4618259999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 902.8242473500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 51.02331013951951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.901930378273317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 92.23312849047035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 121.43935498335418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 356.21162100238104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 822.8667066999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 557.6622023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2065.3481732,
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
        "date": 1779782243035,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 366.19347273333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5618.1733329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 408.0300890866667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 148.87334992362634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 45.74183419405685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1371.9652054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 893.4065746333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 103.97384468578949,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 252.94792643749997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 274.8779761265873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.49119931950822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 264.47683168908736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.62041137889245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.044577015018187456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.461362727810043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 117.72089246732025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.664262894846146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 304.9872538714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1759.96014265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 834.931333025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36677608955807883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 164.93612761327842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 307.6669188714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 176.20288694318182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 333.9496791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.880531304050583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.77303017734665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.308824621753947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.972693651403326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.9487780564074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1064.4430022000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 907.5054614333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 856.4324064666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 836.9762924916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2483.5196195999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1054.1199502499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.78443916460874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.429891211298326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.05563872663639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 161.79369649362636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 356.7767927242857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 813.6841883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 522.8642626999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2286.6648401499997,
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
        "date": 1779869061155,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 373.7817132333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4477.7761021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 372.1015472066666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 137.0762347733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.17347382327127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1427.9890507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 630.2868991166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 94.19104666919912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 246.78158172361114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 272.1515147208333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.406566064258186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 277.53215493333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.586104916950738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04640053509734148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.468428494234235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 120.59598695294119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.309660395214312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 334.999947547619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1678.0171685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 674.1017455666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3581979268697867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 146.24824876238094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 283.0760844035714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 170.45081277499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 366.92397965,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.86396124646333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 32.848342153045124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.353368653666923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.624023054491225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.78411538328649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 812.5448198333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 851.680677225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 783.9018334666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 810.1258558916668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1917.2631904999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 884.404578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 50.56764880461793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.599897757063466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 88.97068673045058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 112.57233268391812,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 354.75793428714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 834.1216607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 546.854820725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1954.3502538,
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
        "date": 1779955097415,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 358.65975178333326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4871.6497449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 364.04915698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 142.66277263380954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.39998757455443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1456.35514535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 674.5554677166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 91.85891472875258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 249.30423531805553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 259.87059935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.22469372592311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 279.4425762378968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.277897280845004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04759199963272639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.448947663392126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.1892632248366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.806131139407857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 266.4538732142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1671.9822913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 775.3928997999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3536787218323182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 130.68356112945727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 281.72204973035707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 174.86144661515152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 316.8521403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.361865997295661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 27.904662960470752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.565018713085776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.783478494559244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 90.39202989934907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1059.81749605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 764.6137346749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 829.7677893333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 912.1917091666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1941.8256707000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 849.0980327999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 44.43751327195045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.154590982322325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.86455377274378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 116.17453391145469,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 311.50315098095234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 857.2249014666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 534.5864832249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2136.7142053500006,
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
        "date": 1780041494754,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 393.4264115266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5468.3785786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 413.42253184333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 151.23274778571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.757871627815476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1589.3813996499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 978.84690385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 105.81514096657895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 263.77667080000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 277.30014009781746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.30416940371843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 260.43537650853176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.443253509818497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.044699695427378776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.296579142147179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.08077736111113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.659520419940726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 300.0181320142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1822.1790793499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 923.3416294666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.37178149229558016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 153.69563709816848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 320.0505549999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 177.4723436212121,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 330.7211894214285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.906026260660669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.51387093127221,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.304831622960645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.125254214970358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 93.19557787197441,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1020.6603776333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1067.81113825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 599.5971749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 998.0041856750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1886.4111656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1013.8380624499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.67758028529545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.749766923199683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 104.07329479639381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 147.69595193919372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 361.79695490714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 851.6919668333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 532.9463889000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2292.1564019499997,
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
        "date": 1780126620962,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 369.42051781666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4357.5365341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 375.39027658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 144.16246371619044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.699344672916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1279.5460059,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 615.0429682083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.03559468761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 247.62047229444443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 269.20275299900794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.735201207883776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 279.62411264642856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.422094187702307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04577624218211902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.297960732881403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.31455587058824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.852477926824523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 326.81998072857147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1683.1910027499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 663.8996368166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.35014737326360673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 141.17162244410716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 277.546599925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 172.30205061217947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 355.8510458333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.716233364222049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 32.95881950269522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.349172526915744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 27.800609234397285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 93.39644895424202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 927.7116345666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 985.6847004666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 794.3365906333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 916.3868802000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1871.6903155499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 952.5950523666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 50.53909151562635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.845565356103474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 85.46958634437941,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 119.40244986600146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 340.4228740023809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 845.2720808333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 539.157999425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2015.1321529499996,
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
        "date": 1780214107599,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 387.0724948166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5928.2954392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 418.25993394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 147.83112547142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 45.894112165788115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1309.0563878333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 945.5492642833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 105.13124407947369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 254.00851620000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 274.3193852880953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.93732374570608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 269.9827741453175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.445799425266873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04530564238286876,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.398826611947772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 119.30636381764707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.283776064261378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 319.92970806190476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1804.6461800000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 811.035994375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36279794454226216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 148.26394988021977,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 311.62548507142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 176.35902473333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 339.44734290714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.86029557444461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.49474173962572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.447793087708757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.924494416144835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 93.47183443110394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 934.5366094499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1047.33178265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 601.3156822999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1065.1138048666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2258.36671875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1116.5318439,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.07675342966327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.53920718707606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.36322636389187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 140.26409821351191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 333.501800097619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 829.0008193333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 541.988266125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2415.3540787499996,
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
        "date": 1780302310625,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 380.47256274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5020.4629681999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 379.32978497333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 139.39127530571426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.66176832338435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1472.71858755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 629.2610001083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.62738946967532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 249.52980313194445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 269.68246989126976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.41917311799476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 261.7561299203571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.758126526316428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04676443282764774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.281639070829439,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.26992192941175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.56928309684215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 334.2062630261904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1681.1136479499996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 630.70499545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3444933602725224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 143.15570387611723,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 281.2484086875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 172.04628298782055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 355.9515041,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.798836852575516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.06385764607777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.333353224822446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.759231076001424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 93.32609300556835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 981.8477684166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 912.6010351666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 555.916424625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 785.5975197416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1976.39854055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 740.2205643666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.19342830475516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.859819533430695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 85.23863414566154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 118.53998823767358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 367.7552069628572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 833.3214365666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 537.1894060750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1995.0070145000002,
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
        "date": 1780388165831,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 345.14060341904764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4425.3146915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 359.51776946666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 144.4809385485714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.252985767091836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1288.9269188499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 669.2270083833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 93.31263445789571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 244.11918178888885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 260.53935135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.41824958266956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 285.40171985416663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.322858573457715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04646112911350898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.59364823226488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 115.47556290490195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.035421034081715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 287.26579474107143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1667.4259331500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 698.3189699166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.35640162701613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 129.05680053020777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 278.1377242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 165.57672126346154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 326.98129880238093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.324238537948451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 27.648108718098648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.745028346731017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.53052136340344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 83.35844648674643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 860.9079356249998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 826.4996015166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 700.1298060749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 882.9278105666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1775.0172588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 678.4099646083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 44.752689213142666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.793801483135887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 82.81248285498212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 126.76813380034037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 309.4411567428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 853.6836166999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 526.001549825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2048.0176457000007,
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
        "date": 1780475618035,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 387.55981685333325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5319.2545992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 417.44432974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 148.9391991247253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.291974855695834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1789.7412873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 968.4739370666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 107.2675593955848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 254.7577364625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 271.20273563928566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.87901523144274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 291.90237623492067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.375374244998824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.045222935251442734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.450803702850674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 114.43313357777777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.691029761881598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 325.58658197380953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1766.7652080499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 975.2899491833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.37412262194895657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 146.94433179886445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 318.042234752381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 174.37457713257575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 346.1474206333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.83068261196244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.784072346653904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.27286485642102,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.07591271897012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 84.45595070064061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1084.8139807166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1110.6183850333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 716.4303730499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 939.19724995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2085.5861965999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1069.3982402666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 46.7246205698162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.627251865598165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 90.63322502969368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 134.31380273414567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 337.46309858809525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 832.4734644333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 519.11786435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2247.18240405,
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
        "date": 1780561273693,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 395.00913997000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5803.2487265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 398.3421992333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 152.82759281483516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 45.834215451587454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1692.3878103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 936.9834183666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 108.8043542773323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 255.936787925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 276.08919533928577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.13331130654454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 291.91610247857136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.321970388854126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04382967021142438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.428107319226452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 119.10361145980394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.76521292833752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 330.07922630476196,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1806.1170614500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 849.2556079,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.41397940943113537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 167.37939292843822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 323.9758227928572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.4741325651515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 337.94983087142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.02594767457077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.82284068480586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.329316817371017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.480538681652952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.1246791751123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1042.6772379166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 998.0171293666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 744.3556504083333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 950.4034439999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2498.7762351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1103.1629058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 50.612819486954734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.522850637944572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 97.02354091563402,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 136.3896612847619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 373.6465628366667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 825.7462306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 530.9780278500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2334.8450926,
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
        "date": 1780646842702,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 389.37323232333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5693.0933462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 414.96456082000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 153.7945449620879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.888396026580594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1335.34951925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 970.0299512666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 108.31430511464912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 262.87828567500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 285.5193395196428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.979933301745106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 281.17274126071425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.210820087582317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04503406265085232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.608641736357743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 117.57580290980393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.90090176528071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 310.9463148714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1815.83159395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 999.961790366667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3848392814753486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 163.87563742974527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 334.44365822142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 177.54457665303033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 340.60659320238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.078547228596783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.99071049096832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.453939559455481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.946696842648343,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 89.96174124206138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1222.9002047999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1075.177994383333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 729.17297935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 895.8383889583332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2008.9180442000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 950.5465705916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.99083851035567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 23.11491229629608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 89.73847088824027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 142.58869904649728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 377.63077362666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 821.4695857666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 543.8784056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2398.1693403,
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
        "date": 1780731607654,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 367.1009079666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4605.303022599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 393.03330635000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 141.0848490295238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.50041454525288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1210.9158498333331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 653.4096711583334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.98245690961038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 248.51923717083338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 260.07004267678565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.94286742927838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 277.2862988003968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.505429073289868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04749433589475108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 10.064754437131299,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.51698216830064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.911463133573644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 351.0745363166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1723.65921385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 797.5137071666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.35604297951517627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 148.18581530589742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 279.80143999642854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 174.5495844757576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 383.17897789999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 10.219710536194928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.129958861582715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.38621851119668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 28.36874769899133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.49245178292064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 909.6771394333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 874.8707728999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 655.72818095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 775.2513187666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1932.6138466500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 814.6597858416666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.07180263708724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 25.139245689132622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 80.62284128115377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 122.92891463917086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 348.83113809285703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 830.0018929333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 540.1137391,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2086.18268645,
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
        "date": 1780819257067,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 369.9051222833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5768.192170799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 418.99677176000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 149.93771728846156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 47.22743290416666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1663.6776495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 952.5905487999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 106.73543841271093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 259.2506221125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 273.82599522817463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.416877710732834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 271.08122223630954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.55278273563317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.045191275772746675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.328943128050497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.37618178692813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.368946913503827,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 316.43972943095235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1806.6293984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 903.5288163166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.37206813640212866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 143.68450194000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 324.76585054761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 178.77893001515153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 334.23251970238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.88838895205272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.9927509543679,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.326842191600853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.995112577571764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.04634105169048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1159.758549683333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1018.0810906666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 817.8985602666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1050.7671882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2107.01914135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1067.17199645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.80516182831713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.767562286108387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 101.20547344032482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 136.93964942159502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 368.74436011904766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 816.5349860333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 532.737359075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2358.2856017000004,
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
        "date": 1780907022399,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 379.44722683333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5913.7039369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 407.16396748666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 154.22599914780218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 44.8134052788274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1644.0429369499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 923.7497427166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 104.04158889675439,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 255.62124938749997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 270.602075221627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.54708162291223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 284.69825767321424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.37937091914237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.044359071664805017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.366045067369125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 115.38441321111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.769098944431217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 334.90420149761906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1770.36784915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 889.1223665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.4073937264359736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 142.25184374619047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 315.46282727142864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.57967162878785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 339.52589981666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.590393128408357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.019102773586177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.224825545272923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.682557109356498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 96.5201151718218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1008.0029029833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1002.1227880999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 708.7137961666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1024.8107017500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2215.1520843999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1098.8496427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.697739589358704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.672217841050653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 103.83462302146752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 145.59864817521517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 331.9284169119047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 824.2256743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 516.283088755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2311.1316825999997,
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
        "date": 1780991671795,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 381.14358875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4327.219175699999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 379.2781381233334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 145.9502865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.855143319323524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1380.2888219666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 627.1224933333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.62645051188107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 247.4486748777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 260.8403618636905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.440609758205106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 283.36259903035716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.892944479664926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04596212566762296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.402065325722393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 118.26595600588234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.083870952948423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 307.5197825083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1687.96400735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 692.3727132916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3540692074853151,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 125.01707169301471,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 281.15706880178567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 173.02281202214448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 375.50003278333327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.80688641176691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.23905062948087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.401615389668127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.64013782427362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.36748562612573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 912.2198433000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 911.0331886583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 569.406371975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 787.2210063333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2023.5896982499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 770.7905052583334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 51.4424819311197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.59609348216942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 96.25055477843775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 117.28409949887438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 333.0749074690476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 836.4361333333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 537.29619025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2016.69664615,
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
        "date": 1781079506638,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 366.9938353333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5006.5600187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 376.2182094533333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 137.52781095333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.51741806693262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1390.4580706000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 633.9577282500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 94.92157468572745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 246.75719259999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 271.90400601448414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.383592855607745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 285.7411349178572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.345282163609287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04721720026041447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.508056436955524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 116.4336255392157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.023596756152994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 341.60952518095235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1682.5116149499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 703.3506952916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36104023637109,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 124.02960461764704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 273.7222758749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 175.77468032500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 354.61286943333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 10.155892923341272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.44389227544753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.406855325201008,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.67255815119873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 97.12999974308256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 925.2994819916664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 876.2790552583334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 565.5807889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 910.8967329333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2209.3360109,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 879.2504743333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.23881536688556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 24.508242540891693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 84.62787207458035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 118.55134538727036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 362.0380624833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 829.0382257333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 532.58804735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2096.5701612000003,
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
        "date": 1781166127498,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 371.83626738333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5863.7334724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 408.37713620000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 149.24605627582417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 44.71577231769714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1684.2116591499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 1004.3439133333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 106.86070899397662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 257.31104285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 275.1394065920635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.34588944561566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 245.43115521412702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.64445615508319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04467663095345286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.416492327001274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 117.29501669803919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.69032243197414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 313.34670550000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1799.2747826999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 713.217446475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3790949574130112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 159.14332379058607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 316.9054443428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.51057826742425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 363.56095345714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.362171062574786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.31537276078124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.418361592748358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.90391984219397,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 100.57543098802584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1065.276344266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 994.9018795833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 603.4282462250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 1088.5358574999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2375.2323810499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1118.2402077666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.439530102020534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.806858371808154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 98.65936032118502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 140.1518228058898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 358.62772140000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 834.3448818999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 534.6166495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2314.320233,
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
        "date": 1781252110639,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 403.0434818433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 6046.8068028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 410.6541741733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 146.37847244285712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.329829374939095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1563.4985471999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 909.1994605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 104.87855313687132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 258.25664321249997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 251.00555532083337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.06019581073637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 291.19788182857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 23.71479964157313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04403778193637199,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.27586873681716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 113.46309226111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.929814992614943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 303.22844882857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1749.9401255500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 843.7535952666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.38640102276138577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 166.1353300948718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 313.7029560428571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.413360169697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 331.1185779523809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.15890267090995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.284578871028295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.314193268090992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.786349662187074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 91.9854421081116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1196.94831575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1013.4427641333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 686.6220468833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 952.2777220166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2028.69514025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1043.8393944333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.80909351021556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.75136967405525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.86587536653704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 136.9669960204991,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 364.4023854557143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 828.4650149666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 528.05562305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2310.1946756,
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
        "date": 1781337261131,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 362.97366388333324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4630.105714200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 349.19716966904764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 140.02550963142858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.774596240476185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1085.3624535666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 659.7327314333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 87.68053486341286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 244.0398084458333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 243.81115850888887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.18312122167182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 288.0529170339286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 20.96246156225481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.047734417969523255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.415807210550906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 108.60717795789473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.505798691570753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 275.7525882660714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1631.7515508500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 764.8849421333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.34544713457506704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 125.95121846693625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 271.1502209625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 163.42182225769233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 334.77690085476195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.433037236170339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 27.964925910847438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.590425527140756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.390312675583516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 86.83872613668518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 948.3425238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 919.9317197999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 675.5826122250002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 788.6540879666669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2016.8702170999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 856.861996475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 45.14291822750946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 21.967124999428357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 78.8134067522586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 117.6295063141029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 294.65807396785715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 841.9511924666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 541.329473525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1992.8166850000002,
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
        "date": 1781424763285,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 373.25308413333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5433.6869779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 410.7301659433334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 148.9463104142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 46.699573062156446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1698.9750571499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 924.8055241666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 104.41537922097328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 252.82059668750003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 270.76316454940473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.21724559432431,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 274.9603808894841,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.131199034669557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04578457978708757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.525041575386087,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 113.82109290438595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.871236314107954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 317.8670476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1857.43471905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 939.1886804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.402399208657373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 155.50253276403097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 312.39631820952377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 191.14076310606058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 362.94055131666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.951098284001233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.643629888631665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.355297564319597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.89589492367508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 95.17275967033648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1154.0914834333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 959.4284127333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 603.0351163,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 999.69710705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2513.8131735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1088.79204525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.0666048890226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.925118804688218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 90.51826767917488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 141.31923269634618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 389.8201745233333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 808.0913696000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 525.7252177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2525.19799125,
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
        "date": 1781513054540,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 394.25719717333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5624.906675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 394.4278432333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 140.38122131,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 42.324648478900706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1761.1287783499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 940.3954352999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 131.36907164869046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 246.60608649583338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 271.01506623392856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.447382657122596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 257.9648603285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.488396992953557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04418986600641032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.181739205263192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 93.24874715606062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.429491586208709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 300.3261795142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1778.7173816500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 865.2309868666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3786210533383868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 174.6321886107226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 275.37761148749996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 177.4547326909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 346.76771483333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.912111167877717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.83570212562065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.677203005092368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 25.70834363114124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.55485694905465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1192.9537028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 973.1397678833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 746.5047497833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 983.3783854166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2135.4587912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1061.52358065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 46.955855817036124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 20.634337039894724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 93.05889918883382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 125.34730928120098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 298.8560544416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 807.2513276333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 522.4340836499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2097.78446585,
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
        "date": 1781600219807,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 260.35926799444445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3734.8616521000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 271.86044252857147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 108.17529745590643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 31.439463859540602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1082.9128971500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 474.3102822299999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 78.35580372201404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 193.98454406545457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 187.2307145327273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.23200982493571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 236.6551346818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.57663470439839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.039357461322428414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.04799367008411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 72.99769553214284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 13.010959606459204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 206.57429068333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1373.75951495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 575.8512240616667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.2610216278721034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 109.56929297011425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 195.67876205636364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 146.25667915000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 262.7554234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 7.402982003179224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 25.011311804359526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 7.8524854453078685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 19.482823620726073,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 75.34718897844347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 777.7608117333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 665.9775917750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 420.2432473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 599.5375506766666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1594.8198659500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 749.3547201166665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 35.926845811119684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 18.64484639536866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 69.54385913868943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 83.54290614985388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 224.2562046197222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 652.219976625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 427.04053433999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1696.4109722,
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
        "date": 1781684988910,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 380.4501403866667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5729.2218309,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 377.9119983166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 141.78941640095238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 43.85118598451845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1794.7185925500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 895.7558561333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 130.57734732875002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 245.07567621111107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 269.71486314642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.593101915394136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 249.88052976896824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 24.366078418115666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.043489950517445156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.253362972247142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 92.22402796956521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.502257218077023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 306.1200819142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1750.545942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 759.5517987333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3640723940635787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 155.45542950980308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 276.6098429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 171.8661052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 328.7299151857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.949720835947389,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.05254928774827,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.305035433112662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.754291502991286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 97.11569542837175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1119.38599135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 951.2518284416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 674.3606573666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 911.7109737999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1779.2525489500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 924.6595088916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 44.16278344375904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 21.003584684919034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 95.64322789485816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 128.90174985975995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 304.1164329398809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 819.7995824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 523.7819612000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2336.6995086,
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
        "date": 1781770735888,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 371.60172575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4229.232206799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 345.0954980047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 133.17047441375001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.86416097704155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1298.2266212999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 629.7040241916666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 104.84193815429826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 238.09858762222225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 268.2713256875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.11907472792208,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 255.33169876746032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.87040751159467,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04604059881941319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.377191314282395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 95.70824963982685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.281556475686703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 344.53802588095243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1697.4477737499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 723.144910525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36535316621487446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 145.2036214549817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 249.6766230597222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 170.9256101615385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 352.89696309999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 10.220912551492647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.153258175310505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.334950251075698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.966986739711,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 92.84145994800198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1048.9456001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 935.88245625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 688.574403975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 821.9022056666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2031.26854165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 884.4833195666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 49.22582459290427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 23.35234771819723,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.50964169026551,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 111.89792821345891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 291.9781196803571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 811.2911068999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 535.446387675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1994.0610127,
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
        "date": 1781857778963,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 374.89593554999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4854.954467199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 359.0753762666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 134.79940449249997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 40.494902032816334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1211.0391382166665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 645.3027825083334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 102.63340962973685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 234.07991716666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 249.96562526388888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 38.06654535241573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 282.1154099811507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 19.910867454040716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04607188916241294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.270947106156012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 87.25368425652175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.956087731834122,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 288.0415019821429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1640.4977821500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 788.4562785333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3467534074802987,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 133.4656069170378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 241.92284908888888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 168.50097542628205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 336.0068317904762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.327995857878578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 28.064756061695427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.873065342194874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.761439362196306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 87.74865042052701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 1042.5626276833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 752.6635968583332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 556.6042852749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 771.0023452833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1948.8935467,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 739.3959093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 40.9334741283368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 20.77884038229746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 90.27926457107085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 108.43923561683008,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 272.45075573154764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 852.4979499333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 550.547863075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1965.5701271000003,
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
        "date": 1782029733964,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 407.3312026933334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5547.3710994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 396.0584810966667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 140.9086391009524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 45.792439543181814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1438.6401955999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 974.9154991166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 138.26213697118587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 250.47788956805556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 270.7652035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 32.42580601767161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 283.7549026619048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.32563487463034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0441787846795593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.128058525760778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 92.59809446363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.687289851970169,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 299.76281542857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1819.4944178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 807.6626130833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3741810222581107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 172.63015765949882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 277.9637866178572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.02700801363636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 362.14990233333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.136459464839556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 30.58190018144946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.426082406012915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.304855144573946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 95.94871035607822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 990.5687220833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 958.1066389583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 570.058918575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 876.1269079833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2558.4572146999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 1099.20793175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 44.50119896351896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 20.660560705432587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.27780574811592,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 134.6453586064803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 341.70368339999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 816.4221030333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 540.069454475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2160.80511715,
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
        "date": 1782118251926,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 378.1801955366667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4792.9681187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 356.72622213333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 134.8919120964881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.359615710257096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1373.0241163333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 623.2692151333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 103.14012948347117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 247.48632823333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 267.72326536785715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.608040757652894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 281.71540049305565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.273101659265766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04553709038057123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.11639529220881,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 93.44361377727273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.148922809801807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 331.30087309999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1687.1429936499997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 722.05887882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3437947850572072,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 139.08267510011905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 248.20405707638892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 166.10126553333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 377.14094071000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.73149254572097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.318896163146924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.414574307087914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 27.341378870971333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.59477328348737,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 923.2407367666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 848.8003941999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 553.410183075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 683.2303631666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1727.3035523499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 812.0876315916668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.46924469375183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 23.143656890005058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 98.75320045123925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 113.54288767596339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 348.08849305714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 845.4920362333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 522.2210413749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2052.09700635,
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
        "date": 1782201779059,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 363.75516295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4227.5646394000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 345.9551790357142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 136.46604853482143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.4189034244455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1391.6578397999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 587.7301848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 104.54159289500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 238.3144272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 265.52774289999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.09238408352369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 281.6225310785714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 21.52407738600076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04564322667192229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.454407074860384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 95.42696307056276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.823582413912797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 327.5704064571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1699.5477078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 711.3129639416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.34019559390362386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 147.4024461939286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 247.50749247777782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 172.20934847499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 360.2283527166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.783991868747197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 34.17430476931777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.472850829799977,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 27.363986138159703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.67794735759341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 903.7048710333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 868.6261367999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 821.7311441666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 804.466893725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1966.9131583000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 974.3025148333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 47.976704869919345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 23.61019089332882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.01857561508305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 113.75394478841244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 288.69512933392855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 818.9865513333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 529.4850217999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2108.1083399,
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
        "date": 1782287300122,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 368.91894423333326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5052.757936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 352.1724683166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 130.78926348958333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.902862511649666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1445.5337993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 622.9618525583334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 102.87092221842106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 240.18653311111106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 269.88215824107147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.07428473511747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 264.90659386666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 20.394828157544833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0455430902833001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.34191173253826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 94.90897809329005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.257893020246774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 344.1590884809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1677.7406065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 705.3608063500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3449478671467988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 130.16079493811276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 243.9950083875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 172.54833313368297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 356.39267085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 9.840163430378134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 33.283217771966285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 10.375828792274994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.88579504282503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 94.87111424250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 894.5349617833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 862.1180996666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 561.10872765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 779.6625992749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1962.4091812500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 870.7060726333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 48.18512795814417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 22.498117404170106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 87.9162987608621,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 114.60093940733918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 320.73728719821435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 818.8297765333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 537.66032165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1970.0083953999997,
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
        "date": 1782373739389,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 343.38509274523807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4532.131128800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 343.86245761666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 137.2210509655357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 41.298360725064455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1353.36310335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 656.3972330416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 100.14343960838346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 241.6289170333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 258.852551725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.11833178603284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 295.78821501746035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 20.464785364684733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0462263076133438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 8.331487321669114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 87.79064793913044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.327785100749214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 283.87403087678575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1662.0823464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 718.1163479166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.35841234070363875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 144.3841465171429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 246.00769238888893,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 164.1433724051282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 319.9240674857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.396894132648153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 27.996124416833766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 8.844154641119514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 24.895307509555316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 85.4066662142709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 881.6381953916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 997.5976939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 565.6000061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 694.0733935166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 1770.3546949,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 852.2201197916668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 43.068286518691394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 21.034138972919916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 83.87136500055915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 111.34207732864134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 301.4373104648809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 830.1044305666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 545.580941425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2082.51998825,
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
        "date": 1782460492709,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 390.9766794599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5276.4036416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 371.54930665333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 140.35944292523808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 44.09729602618357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 1601.11352015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 853.5575161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 131.52977433208332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 261.8806639875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 268.72331056785714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.89849957451455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 259.766920004127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 22.196953538676524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.043218450712108274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 9.152812911286638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 91.86369367272728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.732142452995765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 304.5810750142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1728.59624865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 897.1877804833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36463268404711613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 163.13139852867133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 271.153783375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 176.6860997398601,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 345.5696665166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 8.89604116386928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 29.9253045404524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 9.398489258737579,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 26.294157038082574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 88.8905244828142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 940.7371636916665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 1032.4339532666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 685.2856124916667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 789.854843975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 2084.0953677999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 940.56476735,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 43.656087482901505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 20.86501067616982,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 84.78595232202497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 134.28603119631939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 308.2849121404762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 794.0373340333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 518.266843275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 2368.9079179,
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
        "date": 1782546038961,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 368.37655243333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5363.785097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 372.62226476666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 142.96414483857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 33.23456333834247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 544.951721975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 877.6391120333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 133.13773108011904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 251.01536824583332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 263.36779067500004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.30136210439078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 289.1396577799603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.620374905062144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04321640864432158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.455401602051252,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 91.12705733221344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.357299394394715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 315.73643681428575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1800.6172301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 291.74079842142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3828155163939698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 50.80380148727329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 273.4868481053571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 173.85872374446387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 342.4570093166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.41284638577786703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7547596946109103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4410621925874404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.18591708510047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 51.66509024833325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 330.3569130952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 313.90251747142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 104.02972392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 281.45761521428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 648.1731742583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 290.079261025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.0755869839842367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3735503402363605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 98.48774981149299,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 120.46389043120105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 219.52463069805557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 774.9804484333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 479.63255566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1149.6829315,
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
        "date": 1782633430267,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 394.64276455000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5425.4440167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 396.8786949266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 144.12866008106226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 33.444465420895824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 590.70937345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 963.9072467166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 130.16327590992645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 247.65861730555554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 279.02403354107145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.531262408335685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 282.25354993591264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.821057262252552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04381955298726042,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.863393260690242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 92.72633751818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.09747281658742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 317.27689117142853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1873.66662955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 315.25553852857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3880008784803813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 51.19276279974359,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 286.46137314285716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 185.10368887954544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 348.3970962666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.4114154564365218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.8248008164010439,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4434977686828039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.075421306265316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 53.1035095146697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 349.9273249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 331.73643998333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 106.85420343157894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 305.3234914761905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 698.0785569416665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 305.0925397285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.079722328530651,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3719543626933794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 105.26812432229212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 125.04160309833776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 225.79054288762626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 814.2231260666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 476.7140695000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1193.8443556500001,
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
        "date": 1782721378120,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 366.20135805000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4356.179287500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 363.2699055833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 136.48489855333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 32.34393259360043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 527.8021550249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 616.6436446583334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 105.11656059780702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 240.58021483333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 268.03914347500006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.13987925560145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 278.1536922984127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.96449221337314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.045322080558523166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.630511310839135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 94.38443563419914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.665331231537674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 334.8870525404762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1651.7973126000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 269.6933855625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.34601114241992453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 51.52228966180162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 245.28600453611108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 169.01613973356646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 368.5296479,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.4422622781515935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.8124124303243931,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.42082110370761566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.979859038107321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 46.66351116693296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 322.0526457976191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 308.33807821428576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 102.15879048499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 278.24504055178573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 621.8642794833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 281.39771728750003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.167238934155513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.4034167415551149,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 83.25531573333055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 95.71059894420634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 214.54000107484848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 818.157463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 488.9626522750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1072.5429815,
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
        "date": 1782806086158,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 404.2710089666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5789.1358161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 412.62486767999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 145.46739335714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 34.41911428492864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 601.90472805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 899.6414585333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 134.27354084089743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 257.42978520138894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 278.944909875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.852505753967215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 285.4094525738095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.735442628643444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04534169443331951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.779955980879407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 95.57629072857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.019150387420876,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 300.3280051285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1793.4273383999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 285.43290725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3775443525620383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 51.17456549454124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 276.131882825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 178.26359073939392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 362.37815643333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.4094482403600441,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.756885540309423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.44199534262066054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.008644310822069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 54.24968977161552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 345.67797797380956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 332.27365576904765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 104.46541894552631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 293.52878063571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 659.2387480499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 290.10611488035715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.1121113903165654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3887098769249948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 104.043426021788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 115.09065087507936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 222.7862172255556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 799.7507202666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 491.55986265499996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1205.50839175,
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
        "date": 1782893374324,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 395.77181278333325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5674.3729883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 388.88015678000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 143.75228738761905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 33.10347762529143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 548.2122694249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 909.3782004833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 134.08203904624997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 262.6182850875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 269.1257346642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.63955721320525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 268.45823179325396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.58310719430137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04436282333884642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.421217503308661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 91.01932766620554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.1481261114269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 296.6423347714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1778.5510917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 296.0684780321429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.38518411075787773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 51.200009591089746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 274.95477222500006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.88577618712117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 347.0570165833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.41018652057504135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7399340888835404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.44301220395893076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 14.929233408100563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 53.040617366859536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 339.04559877619045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 321.5915412666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 106.27037378710527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 290.0454446964286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 651.5232303416667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 288.43767050357144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.151765104511183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3725109179399522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.81115925120797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 115.64332099393275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 225.72897213222217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 792.6434328666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 488.717625655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1141.5488164499998,
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
        "date": 1782978192800,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 407.21646302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5431.7903155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 375.4472164433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 142.80436193706961,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 35.516151148460594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 538.177757825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 867.3668588333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 134.82135383874999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 253.72075640416665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 258.21573956250006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.64165834550797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 298.58324035873017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.23448481479503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04468921215022261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.6344786961065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 91.61724670019763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.512410644231057,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 326.45041726904753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1767.0710610499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 283.90050586250004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3879559928173565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 50.20270762201219,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 274.61230035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 169.257680075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 350.3228672404763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.41060393233492787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7472160912322128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4403828953033055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.295852240125225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 50.08831797166502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 328.96180865952385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 315.93404705714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 106.69361926842107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 293.79673037976187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 670.2123546999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 286.7567482482143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.0771394894928807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3906593849052646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 98.16103729863258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 118.19388742945907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 226.480184904798,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 774.7086060666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 476.8349059200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1139.7548906,
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
        "date": 1783064425807,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 391.28278115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5654.206688300001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 386.58993023666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 143.81143836142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 32.86823644785714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 550.6498840749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 930.8957502166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 129.8054112576786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 249.9528163958333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 276.8454166053572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.84620571142155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 305.4608331428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.790967028247035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04506629794495272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.5412282112725935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 90.19466943913044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.776528555055773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 299.74631037142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1782.2120453000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 289.25519234642854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.37730142705560965,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 49.60937151384147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 284.6049712357143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.3951485439394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 362.2091895666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.4140278826067908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7558047033122162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4419779513367327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 14.996128140607393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 51.578046069108666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 337.870672497619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 319.26060905714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 103.09063752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 287.99084946964285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 662.3908138333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 292.43578153749996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.095781751221655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.370797588694732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 91.78873077518774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 122.6323117392682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 230.87115431944449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 789.7091711999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 461.84042905999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1126.4543380499997,
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
        "date": 1783150475278,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 397.15405437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5955.712352100001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 387.8744172366667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 141.39181875333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 33.169493881624966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 545.0671217249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 902.8818196,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 125.49514413767156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 248.80747280555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 263.4999045267857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.30857533486091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 284.44830058075405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.190560103853507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04365422209759086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.524711901872633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 91.52211423794468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.79614910625467,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 321.48505743571434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1755.9703058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 282.90634283571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.36289803511313734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 49.74754197030488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 266.05369074999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 172.9860504903846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 338.91591353333337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.40813229868721324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.74078035456543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.44018639716216557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 14.683791435345412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 48.50368690924101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 327.52365833571423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 320.7971237880953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 103.47739591315788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 281.6469825535714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 634.9771550833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 283.4196577625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.0509784752174065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3735452577840404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 96.31440588911119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 114.4898979057637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 236.30935867000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 777.8185816666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 489.8758537750001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1145.17085265,
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
          "id": "0534fdfbbc71dece45b6ad72b1eedd52801a7889",
          "message": "Bump testlens-app/setup-testlens from 1.9.2 to 1.9.3\n\nBumps [testlens-app/setup-testlens](https://github.com/testlens-app/setup-testlens) from 1.9.2 to 1.9.3.\n- [Release notes](https://github.com/testlens-app/setup-testlens/releases)\n- [Commits](https://github.com/testlens-app/setup-testlens/compare/d96a555133c275a00949d2cc77b70fe9a4242ebf...3f82d2dc6cd5c03f02ce5f7885a21195d85f8d14)\n\n---\nupdated-dependencies:\n- dependency-name: testlens-app/setup-testlens\n  dependency-version: 1.9.3\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-07-05T01:22:28Z",
          "url": "https://github.com/apache/groovy/commit/0534fdfbbc71dece45b6ad72b1eedd52801a7889"
        },
        "date": 1783237257741,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 397.29520137000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5778.4471328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 380.6731657266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 142.44887098714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 32.5665645356593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 562.7419125249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 955.1879614333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 129.85490419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 262.225907975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 275.9748700339286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.1772446826063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 298.8722692339286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.585379597034589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04371784659323374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.703638687580778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 89.9239911347826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.465380196593596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 326.7648898928571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1793.76169195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 290.7204033946429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.40094059744803234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 50.85630637679644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 272.22138110000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.7696517878788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 339.13985764999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 55.04183858515364,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 373.9614956546333,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 1020.606151308339,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.41085979266204325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7572739752586846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.44205347984379895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 14.85766706367205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 53.02298633344084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 330.93178218571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 318.5051997857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 107.12181226342105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 291.20316635773816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 660.6930883416666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 290.22508992857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.0517303370243227,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3792189109568578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 109.01589779895691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 110.65742917625217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 229.09710931313134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 799.0232322333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 493.60772053499994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1161.2573672,
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
          "id": "0534fdfbbc71dece45b6ad72b1eedd52801a7889",
          "message": "Bump testlens-app/setup-testlens from 1.9.2 to 1.9.3\n\nBumps [testlens-app/setup-testlens](https://github.com/testlens-app/setup-testlens) from 1.9.2 to 1.9.3.\n- [Release notes](https://github.com/testlens-app/setup-testlens/releases)\n- [Commits](https://github.com/testlens-app/setup-testlens/compare/d96a555133c275a00949d2cc77b70fe9a4242ebf...3f82d2dc6cd5c03f02ce5f7885a21195d85f8d14)\n\n---\nupdated-dependencies:\n- dependency-name: testlens-app/setup-testlens\n  dependency-version: 1.9.3\n  dependency-type: direct:production\n  update-type: version-update:semver-patch\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-07-05T01:22:28Z",
          "url": "https://github.com/apache/groovy/commit/0534fdfbbc71dece45b6ad72b1eedd52801a7889"
        },
        "date": 1783325480276,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 282.75515893928576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3636.7720166999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 272.80578167857146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 99.3983309052381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 24.708978118841095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 410.95186445999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 472.53686473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 72.32338060530013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 185.9817565090909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 201.98370007000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 30.802426332376438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 220.9213741511111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 12.437886656379277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.03662057282274193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 5.812033924615494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 72.885025055291,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 11.959209119784067,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 218.24070263383837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1318.0438449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 209.53368203999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.270138436755321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 39.407818415196076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 189.62533202727272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 130.68032195889705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 287.4414116267857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 45.12815780544131,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 327.7042310858584,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 436.38929154580325,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.3446260627494276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.3830217412680883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.3229200235300119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 12.369382888021246,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 33.53965976523603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 245.29570520416664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 237.38520841111108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 80.87178263429487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 218.76724991777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 485.455080805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 223.63326867777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 1.7238296826363613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.0566861608337728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 62.45647522730199,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 75.98140416960943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 170.29763128712122,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 607.3647201,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 370.1546937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 835.6133635,
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
          "id": "f3dde776d69ccf1c2e08682c043f34a26b8cf78f",
          "message": "Bump actions/setup-java from 5.3.0 to 5.4.0\n\nBumps [actions/setup-java](https://github.com/actions/setup-java) from 5.3.0 to 5.4.0.\n- [Release notes](https://github.com/actions/setup-java/releases)\n- [Commits](https://github.com/actions/setup-java/compare/v5.3.0...v5.4.0)\n\n---\nupdated-dependencies:\n- dependency-name: actions/setup-java\n  dependency-version: 5.4.0\n  dependency-type: direct:production\n  update-type: version-update:semver-minor\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-07-05T01:22:51Z",
          "url": "https://github.com/apache/groovy/commit/f3dde776d69ccf1c2e08682c043f34a26b8cf78f"
        },
        "date": 1783410435831,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 410.0456915066667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5687.384261199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 377.97177603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 144.8680109347619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 32.67117148131546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 552.744469425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 888.2486951333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 138.05586933577382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 256.2873179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 269.9378613946428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.50593440179926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 261.071039152381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.749384020697395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04445965788459595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.565152586117835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 92.80756900909091,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.196128341279936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 318.3398540571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1819.3356454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 290.57246368035715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.3784190742073992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 49.95365741219512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 279.95292669821424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 178.30902154318184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 347.1817748166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 55.074194345547745,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 374.0309828481082,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 1008.023271087508,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.41139550160097105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7450028141595944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4433257337782902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 14.849580835260207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 51.271950520185314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 329.8869904357143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 314.88732887142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 103.269917145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 292.89496675952375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 644.2774701166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 291.3673810303572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.079858646876877,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3977979697006926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 100.80676009724004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 121.90521896065255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 247.8160269422223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 795.3283787333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 495.2686999250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1183.16546385,
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
          "id": "23780cf25faf3fd44e7631736b7cd152849ea419",
          "message": "GROOVY-12140: normalize reflectively boxed primitive returns through valueOf\n\nMethod#invoke boxes primitive return values with fresh instances, unlike\nMethodHandle-based and generated-bytecode invocation, which box through\nthe valueOf caches. Dispatch paths that invoke reflectively therefore\nbroke reference identity (===) of primitive returns: classic-mode\nreflective call sites (e.g. varargs methods), and — in both compilation\nmodes — anything routed through MetaMethod#doMethodInvoke, notably\ndynamic-name calls such as obj.\"$name\"(), where a constant-name call\nto the same method returns the cached box.\n\nReflective results are now re-normalized through the valueOf caches at\nthe two chokepoints: CachedMethod#invoke (all doMethodInvoke routes) and\nPlainObjectMetaMethodSite#doInvoke (classic call sites holding a raw\nMethod). The shared helper lives in MetaClassHelper#normalizeBoxedReturn.\nfloat/double are left as-is since valueOf does not cache them on any\npath. Also reproducible on GROOVY_5_0_X, so a candidate for backport.",
          "timestamp": "2026-07-08T01:48:40Z",
          "url": "https://github.com/apache/groovy/commit/23780cf25faf3fd44e7631736b7cd152849ea419"
        },
        "date": 1783495476840,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 363.67302605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 4919.9575092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 370.35761407000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 137.51053173904762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 31.76010554273072,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 535.28308045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 616.312210875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 105.04257585794069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 239.78425523333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 264.16808497499994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.5885513939133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 282.69745988750003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.521759626266732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.045481214473813744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.909405713951108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 94.01983208593073,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.403755953116596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 335.1757318119047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1681.0197791500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 272.97685474285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.35547999635537425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 51.19960637365384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 247.4054487722222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 168.96270589603728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 385.18508685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 57.67423579280726,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 423.93505749343575,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 572.424019825564,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.44035572360927266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.839113231760253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.40300242879860015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.366159469104613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 48.84348494530011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 341.5234396642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 324.17805673095233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 102.43727958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 304.71706125000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 671.6003815333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 291.68143552678566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.203880833176268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.392634950981971,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 79.13665156631542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 98.12588412728267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 214.64667823747473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 826.6027813666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 499.28279137499993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1148.29233245,
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
          "id": "aa30a6906577f66a707a6522ca67d7a233117b80",
          "message": "Close per-test GroovyClassLoader in contracts BaseTestClass\n\nBaseTestClass created a GroovyClassLoader in @BeforeEach but never closed it,\nso every contracts test that extended it leaked a loader. Add an @AfterEach\nthat closes the loader, mirroring StubTestCase.",
          "timestamp": "2026-07-09T03:36:46Z",
          "url": "https://github.com/apache/groovy/commit/aa30a6906577f66a707a6522ca67d7a233117b80"
        },
        "date": 1783583259326,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 406.09956664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5724.0978457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 396.1344896433334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 144.1851430669231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 33.27785295291329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 543.4830264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 901.9775949000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 133.26836121198528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 243.7711864111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 283.8135446446429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.49064979754231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 292.13521026984125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.672125489808835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.044590601100132674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.437456812017913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 93.09170186125542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.350104605483583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 297.0706542714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1788.6674644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 288.8615874303572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.366603833846339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 50.35242709842714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 268.40910849999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 179.55959713939393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 333.37962025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 53.41197755658496,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 374.2271688893732,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 1004.1569015322499,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.428069630216579,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7831809414181277,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4091829228527518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 14.937210964464901,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 51.18847414049174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 333.76507149047626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 316.20454321904765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 105.4149730513158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 300.6018557065476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 650.943256625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 294.5700579446428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.056891906324029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3752330112271793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 90.18189345586363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 123.38156892117902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 221.66305606111115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 795.0300751666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 490.38105145500003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1124.1546397500001,
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
          "id": "aa30a6906577f66a707a6522ca67d7a233117b80",
          "message": "Close per-test GroovyClassLoader in contracts BaseTestClass\n\nBaseTestClass created a GroovyClassLoader in @BeforeEach but never closed it,\nso every contracts test that extended it leaked a loader. Add an @AfterEach\nthat closes the loader, mirroring StubTestCase.",
          "timestamp": "2026-07-09T03:36:46Z",
          "url": "https://github.com/apache/groovy/commit/aa30a6906577f66a707a6522ca67d7a233117b80"
        },
        "date": 1783669741066,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 391.34184733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 5704.6176411999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 377.7276226700001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 144.84573708857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 33.96445276915644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 548.146898175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 915.5758714500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 131.87973376886904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 258.614862525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 270.666787175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.516583052425446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 303.95369618095236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.837383967559834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04420194131525474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.240341190831893,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 91.94599520810276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.567565753365784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 317.67216665238095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1855.1363487000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 290.1004709071429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.38692236942723,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 50.06370717835366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 272.25606906249993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 177.37780114166668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 355.18429019999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 56.959603558784096,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 374.15896768833204,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 1010.2416342972415,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.42809680209362017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.7650760563486103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.41079834098629986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.019007475876393,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 52.05107205378623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 327.4251046833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 318.9740493285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 106.5092814163158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 291.0638953928571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 645.5765983333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 290.57654034107134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.0885859204128967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3743697483499926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 94.59120638140362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 119.48987005156519,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 217.4488551588889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 801.2263278333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 471.54545837999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 1168.9902686,
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
            "name": "Paul King",
            "username": "paulk-asert",
            "email": "paulk@asert.com.au"
          },
          "id": "9d01e177d88d57516ce821531624eccd359af8b4",
          "message": "GROOVY-12117: replace super trait search with more complete helper stub",
          "timestamp": "2026-07-08T15:42:17Z",
          "url": "https://github.com/apache/groovy/commit/9d01e177d88d57516ce821531624eccd359af8b4"
        },
        "date": 1783754318352,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 230.90407102111112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3303.7658389000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 230.4674043222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 96.70063364909092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 22.643913574569545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 352.9477542333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 421.18280114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 66.09364838831696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 179.97206146666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 175.65124571212124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 28.413274673184624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 205.10753938833332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 9.62296571925271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.035318600981019646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 4.520792085054391,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 66.67603673860215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 11.486401060295037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 214.63974049000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1228.6328456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 181.01354864621214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.23485194089769584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 34.623954862010514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 179.63052257878786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 130.20310889272062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 245.43265025555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 109.81751392701584,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 352.34194314990486,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 456.1569334750338,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.49015203742507396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.1911107835632126,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.34466631079064464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 10.31572175179683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 36.01876129821456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 208.70011098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 205.0447302618182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 70.9279619883005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 189.7996938169697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 438.15258306000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 175.15993127365968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 1.5704709498089215,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 0.9986198795154501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 55.08375213510417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 68.26626579530182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 124.92029484939891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 551.219838975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 356.76196608333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 790.5973034333332,
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
          "id": "60479488494fbbe20a552dc6b88755896e628cf2",
          "message": "Bump com.gradle.develocity from 4.4.3 to 4.5.0\n\nBumps com.gradle.develocity from 4.4.3 to 4.5.0.\n\n---\nupdated-dependencies:\n- dependency-name: com.gradle.develocity\n  dependency-version: 4.5.0\n  dependency-type: direct:production\n  update-type: version-update:semver-minor\n...\n\nSigned-off-by: dependabot[bot] <support@github.com>",
          "timestamp": "2026-07-12T03:06:05Z",
          "url": "https://github.com/apache/groovy/commit/60479488494fbbe20a552dc6b88755896e628cf2"
        },
        "date": 1783841299934,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 373.8128291633333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3657.8421394999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 339.4142056142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 135.71099400999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 31.652257187078373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 500.50731140999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 484.199189995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.60276750538304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 163.76081449230767,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 264.7234226714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.44306455603253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 281.3202838946429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.075221154108903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04534951905772276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.798239382916504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 105.03787214552631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.145592429722598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 324.8756343571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1597.4961237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 244.43640646805557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.33380254362736106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 49.62612037560975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 252.39600704444447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 165.43264807884617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 399.31231175999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 57.56932116024356,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 422.8474388604638,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 576.0379325012378,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.44084106857268157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.805363146393498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.4054959380609488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 0.8295902116359727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 43.110155200980195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 285.2659168642857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 284.48734837142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 89.1095375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 242.18065403472224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 470.87268444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 319.1385208142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.0970761157274356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.374839855508808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 85.80806404224413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 99.22305691115446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 211.15248518131312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 822.3188941666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 62.4799501288673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 555.0862063,
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
          "id": "86c6648d1e61436fe513afaa85d8d0c403de3cda",
          "message": "GROOVY-12154: box primitive lambda param when the target SAM param is a reference type\n\nA @CompileStatic lambda declaring a primitive parameter (e.g. (int a) -> ...)\nagainst a generic functional interface (e.g. Function<Integer,Integer>)\ncompiled cleanly but threw at runtime:\n\n  java.lang.invoke.LambdaConversionException:\n    int is not a subtype of class java.lang.Object\n\nAbstractFunctionalInterfaceWriter.convertParameterType always emitted a\nprimitive implementation-method parameter when the lambda declared a\nprimitive, ignoring the target (functional-interface) parameter type. But\nLambdaMetafactory links against the erased SAM signature (Object) and will\nnot unbox to a primitive, so the implementation method must accept the boxed\ntype when the SAM parameter is a reference type. It stays primitive only when\nthe SAM parameter is itself primitive (e.g. IntUnaryOperator), preserving\nGROOVY-9790.",
          "timestamp": "2026-07-12T13:11:53Z",
          "url": "https://github.com/apache/groovy/commit/86c6648d1e61436fe513afaa85d8d0c403de3cda"
        },
        "date": 1783928438513,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 176.5701949333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 1949.9664204499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 159.76273039230767,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 68.96439200942528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 16.027831180915857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 261.35370125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 234.4790484638889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 44.51077226580355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 86.2676959231884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 133.881682885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 17.905450083254845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 146.49483339789916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 7.989219507114525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.02425145369703408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 3.546692370794684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 54.315292181650065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 7.351949346044343,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 165.48267241410255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 854.4144574333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 127.13974794926469,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.16923583439400808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 24.380260844519544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 132.81080179291666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 77.29662287960684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 176.88896257202794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 57.22434034595224,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 266.2782662645598,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 341.9729412626577,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.2055450739020061,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 0.8326378817987852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.19522379999527167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 0.3428584538280105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 20.840108207564043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 141.90106118476191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 144.19122525663005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 45.020705483406154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 121.9300247216095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 253.2000295375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 162.27056562628204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 1.0351299931324587,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 0.6677432850743623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 42.35207851647234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 51.38295185158197,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 92.96818634070556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 434.2545444599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 36.90751985063177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 297.0146469571429,
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
          "id": "9b4176bc49be4361134d4ed274bcc6a56145488d",
          "message": "GROOVY-12160: @AnnotationCollector emits collected annotation members in hash order when the collector is precompiled",
          "timestamp": "2026-07-13T08:26:01Z",
          "url": "https://github.com/apache/groovy/commit/9b4176bc49be4361134d4ed274bcc6a56145488d"
        },
        "date": 1784013508418,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 402.48940380333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3628.5694888000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 340.66306819285717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 134.38622351708332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 32.5225510825474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 489.85115772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 496.58785095499996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 94.98780260913796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 157.72952833076926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 265.7575117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.566656767313646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 287.5071852428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 15.6634067292036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04683607819152017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.745893957206159,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 103.79285707842105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 15.077852296542954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 331.0573229880953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1589.42839485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 250.4457174625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.33540785304885096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 49.39860887456156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 259.64685380000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 163.10835549166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 371.1586285666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 55.88686435268838,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 422.57459499245243,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 572.8287941246405,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.43979028301734796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.8224404564454844,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.41074292934253265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 0.841081945035031,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 44.40319744155742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 287.64513178749996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 288.9394889625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 88.55502849999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 250.8352185938492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 481.094134605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 315.2134067142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.1218575053764988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3224273832333107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 86.95876128769325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 100.36500101169193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 207.00548917651517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 828.7502373333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 65.39239434096103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 563.883145775,
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
          "id": "00d067fc3bce66a53adf6f6f9c9e00e0f48a7cde",
          "message": "GROOVY-12165: note the call override cache's intended end-state (GEP-27)",
          "timestamp": "2026-07-15T03:22:11Z",
          "url": "https://github.com/apache/groovy/commit/00d067fc3bce66a53adf6f6f9c9e00e0f48a7cde"
        },
        "date": 1784099987490,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 362.42920655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 3541.750761099999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 318.5785870345238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 128.1895401709559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 31.733199162549607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 489.96547145999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 488.477620845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 92.60109338736119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 154.06735177087913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 255.56087566718253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 30.518426881449773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 256.0366424175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.0398242451,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.03707007577087794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.591757837726249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 105.11185055894737,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 13.049818778350893,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 360.5256637333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 1586.9305342500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 252.43969089027777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.32450408054867574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 48.88172819124564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 257.65024868750004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 166.48591336217947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 361.62755255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.allocationChurn",
            "value": 57.53303428739698,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.cpuIntegerOps",
            "value": 422.59186650457497,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.HostCalibrationBench.memoryPointerChase",
            "value": 573.2523829393141,
            "unit": "us/op",
            "extra": "iterations: 3\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 0.44179454289766606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.8234443211490117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.40880095419329854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 0.8331744424640919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 44.06636569676494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 290.33678884642853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 287.8091895089286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 88.25988012608695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 245.68208681805555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 478.2617615599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 315.46078105714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 2.1516418506055603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 1.3561368936088214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 84.03945872933367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 95.74132291304065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 211.07050694818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 807.1668266666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 63.05249574122679,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 577.9610076749999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}