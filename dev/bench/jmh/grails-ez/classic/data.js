window.BENCHMARK_DATA = {
  "lastUpdate": 1781857605872,
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
        "date": 1778455718339,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 286.92993405535714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 255.8731258625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 237.15156637777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 98.40864072380951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 30.18650499281001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 30.638221702002927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 60.43008390623886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 75.92200199501426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 169.28509496410257,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 167.97466739102566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 29.13961626854269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 183.08742653863638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 14.729105516479297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04176618093288021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 5.610427638396995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 33.31841651639344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 13.108369346478511,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 248.78888553055558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 192.2037993181818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 19.167564050477324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.04814896701179665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 6.891394616096899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 33.525516730300545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 14.10530654461152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 235.6621393075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 3.654396144024046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.01696914371381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.837142407246373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 13.76249468181553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.4114982137073073,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 11.96654182092244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 9.440093605679792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 11.54768304635181,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 4.052561113845115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 28.721692122044278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 22.998343205183016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 8.635026740163722,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 6.698920082982928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 5.0731353463831725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 4.500366600675575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 11.385712167041495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 42.81668361220053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 17.54392098295552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 34.88957144310345,
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
        "date": 1778458674804,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 377.3340284833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 322.4793894714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 314.55257052857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 135.39949431958334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.77888731142558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.91294656619501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.82853640769233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.93176254913422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 218.53030019,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 218.45693434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.55237083898305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 242.06277809999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.63394150841987,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05072091060636108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.387972267542371,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.67105116165588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.647838150822157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 295.76852531428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 234.2798853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.773990008165583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.059193306372933685,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.369115516668757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.79701614028986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.89233419803481,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 329.37572374523813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.07467221024204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.819726623788162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0509744190502375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.00082048909415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.873302075285142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.380698071083145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.172618999301653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.443065949364732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.079926812483427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.88910406453373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.90633364728772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.568253871003453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.050993159864362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.186487645044275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.258471418423472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.903818058688888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 50.9504330425641,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.197432634666047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 43.14471670344203,
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
        "date": 1778485659135,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 368.21552505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 337.04446372142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 308.7168048142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 126.44462528272058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.7985949685869,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 39.895945532588236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 75.79376455555554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.38479093658007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 219.27349389222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 213.38132943,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.520686759507655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 234.86505141111115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.233622770544674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.052195276780247504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.239933210807138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.09588513958333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.977589620412452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 277.59337118095243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 233.1150796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.66339650371876,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05976490113866838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 8.94291299763394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.32976919163043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.672663228737772,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 285.2309839380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.424818348521099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.943195287369675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0793518393255697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 18.226446916771856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9952630913226066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.456116891084864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.757354073156897,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.016657192328088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.211411853852284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.48349504809113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.883979434171113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.205842609692647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.604857054922656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.679476233510329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.611860944071563,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.639089516751032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 54.3121554534638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.154866069621487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.49262242242423,
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
        "date": 1778571157588,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 407.45698638333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 321.51624857142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 314.4348004428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 129.750314275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.2342814714244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.18351261479737,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 79.01473829215384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.58316294458874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 222.63775628777776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 220.3476315922222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.28939771698811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 234.92003163333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.03435919688737,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0508200666198784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.290563095994851,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.0435919740518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.56351896878873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 298.1109748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 235.84342025555557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.68694385484279,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.06067845516490224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.459755510425278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 45.21248478950593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.947478977667778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 303.85021131428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.061454202319016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.760922271993018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0680675948171863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.92651044103862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9024450633770624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.316257138635597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.147598299890282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.499001215641552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.039982542426364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.96024622608818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 26.490219752484848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.61578953179361,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.078281577291003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.184293304635437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.533255587346531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.941033816176688,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 50.95159876025641,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.468721231757932,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 43.743268930877655,
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
        "date": 1778658216550,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 333.4599349071429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 305.2932484142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 280.51906596785716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 124.55127919264706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 35.21256614949978,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 36.69587843818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 71.80187801120691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 91.06036639249012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 211.76653365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 211.97589746999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.55398264939077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 248.7709135652778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.37207927697373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05285126078944521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.656848282711245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 40.567176706100845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.382811002074817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 298.44664104285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 249.89815696666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.538178128190342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.059471669280709925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 8.392380548634993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 40.66030725036485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.390746482714718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 265.4547948650794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.372569369222877,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.835316043686846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.1237533500193941,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.645388417945288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.7994671597985339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.459722971077053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.113749737806758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.151231399448625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.520868028788164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.53009313677083,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.579127810858484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.299313532342271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.115906295126809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.614922017681662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.20937171037195,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.53186788276532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 48.674305039044235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 18.49527335674455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.075069045712624,
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
        "date": 1778744053487,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 291.7442750714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 257.09552796250006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 240.84397451111113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 95.27163909220779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 29.82201841617647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 30.478196324682973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 59.582786203529416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 75.2235069994709,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 172.71950366666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 170.46047554096737,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 28.33701695601051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 179.6621236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 13.654766286627464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04141686700121051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 5.683444817172504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 34.363659488818435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 13.8371628956725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 252.1571459875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 181.37836964166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 18.831196905254803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.047467911318695925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 7.221255408310843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 34.583266969636085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 14.306477320743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 222.6085795806818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 3.6212429311133776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.025330428037547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.8366255994796962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 13.579451790849106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.8051852689865182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 12.301487029355718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 9.500682713313251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 12.326772800464951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 4.015716771140886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 27.95858328810642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 22.3808919356642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 8.705825414966096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 6.677155196598038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 4.936581463889562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 4.468511865400133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 11.379957726612874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 43.78979760299619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 17.393085069963185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 36.271338349772726,
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
        "date": 1778830856421,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 278.956765875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 255.56263435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 242.95836748888888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 98.48177101829005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 29.32885759784252,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 30.169062856973824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 60.567231536274505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 74.92909268148148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 175.726606775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 170.61902147243592,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 28.64341208582014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 184.09645524545454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 13.491402955166913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.041844346858923444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 5.8446834744021325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 34.99950940939683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 13.656700925663927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 218.5568843938131,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 193.25070722727276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 18.712485216224746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.04806219626696439,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 7.06153509629908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 34.073422412524245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 14.280834890630896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 257.2759709875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 3.61128416774888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 2.9729372531850315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.8407487640713818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 13.455568474219984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.479746436609687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 12.274381713068045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 9.578125256955682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 11.904363583542096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 4.014863828656826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 26.867234583837835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 22.446594518521447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 8.561251100450153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 6.704256276001155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 5.150838143341508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 4.452649338005697,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 11.372634072738418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 41.96495428313043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 17.11190569165693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 35.114356847943135,
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
        "date": 1778843636263,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 292.30285077142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 254.267115625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 236.5924635888889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 95.75779415714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 27.74175803944064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 30.16167071591022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 59.34582615882353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 77.15342407350427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 168.3315651897436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 165.50205654999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 28.88911791807749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 189.05723697272725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 13.801273571826124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04175134953632858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 5.57587976624651,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 33.83033301220339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 13.220308109823913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 226.69766544111113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 187.27040207045454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 18.86026668325787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.04792321939855408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 7.02725740379543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 34.2409398914462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 14.234776046668026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 256.834450075,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 3.6271478655715628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.028408649592497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.8408556711500113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 13.526910316005466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.4275234583732064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 12.242163788923389,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 9.449538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 12.191321822491545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 3.9783206510491054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 28.44075165380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 22.557519514606742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 8.576379061478331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 6.838322943124747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 5.009215637784071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 4.669922246108185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 11.353331516151801,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 39.90692110270589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 17.07782353332534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 35.411008382456146,
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
        "date": 1778915952079,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 377.95727631666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 327.7359051428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 308.1967731571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 124.3107476102941,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.004321145891986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 40.43190296427032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.10660494729345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.53158542380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 220.49938361,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 214.58265784000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.62919533740093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 222.22515754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.17749428554765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05156846853417988,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.4332073816630855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.68282388406375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.65386911570497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 322.74165537142864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 227.56868920000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.969188266790525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05917269866401179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.267538363701101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.31319576107578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.93229813965906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 287.57687156428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.3789482753104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9109312971912304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.080076722263842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.35304618187672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.1334973706914564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 16.48421645267541,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.352458367759372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.243680235571077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.294620159388078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.97025197055138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.684516042402624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.149550654553245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.619482299976628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.5115071081127756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.787245633389609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.65166630916967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.75862239069069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.02357264352604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 46.7217631523583,
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
        "date": 1779003008358,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 387.0169984166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 332.809854952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 314.1698533142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 124.22077873970586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 36.834970216363644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 40.871806261061224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.41707770769231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.78894049155845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 220.79592228444443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 209.18403694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.915423567327096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 223.09367996444448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.93945599974768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0520858470208166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.315925378267049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 41.72067321909013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.53423470513136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 333.75385938333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 234.4130157333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.421325837188242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05965037330224342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.162081170683182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.61061932229418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.056228158598923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 292.0831398694445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.377884206447602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.943852960154346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.2234151161314573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.741737033838074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.182097991634596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.454812995337004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.453474667721226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.420540190130595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.237704840867499,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.638900548275856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.98965903025163,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.23893811155113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.575862889712408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.634796590450198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.743840398140544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.633377948640486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 53.247989718829864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.176418951929183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.823407052626266,
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
        "date": 1779092081329,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 389.26486734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 331.5336455023809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 304.1187825714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 130.259170275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.22333373643364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.649108810622444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 81.77359639250001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.5861381095238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 223.43037022777781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 213.51146056000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.756460820964776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 239.83132680000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.287523414066488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.050666019459266895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.128013274869406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.12412644893617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.686553554158472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 309.51780074285716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 242.66128010694442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 26.15046430506619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058590130307432854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.334259096701562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 45.61401523240647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.633196225274837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 225.2549167888889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.09636759269245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7578844827157214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0527993588332185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.784322542808809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.0208062638295674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.26142604998569,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.204349084059245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.749414078644469,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.142677005033119,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.135346014823355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.392004748004346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.860354294828959,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.052265087302285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.148451370738671,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.355539970967889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.055799265535267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 54.333310898043194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.195498148677395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 43.77115682352864,
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
        "date": 1779177677492,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 388.65859913333327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 326.1824605571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 301.1058202285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 125.81011279375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.03347552957912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.94697033166243,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 79.36429818553844,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.23188983333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 216.5297099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 215.77264287000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.62952638050731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 227.5684730877778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.34616401861779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05048678832593166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.413684608520832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.49231615975178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.544496606515047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 259.7457340928571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 229.52009173333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.222817166250003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058437291893673124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.296767432707274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.74503671292631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.668291157964603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 279.85631045444444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.061860654256784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.745671433506743,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0541998983267864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.919214803832524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.8569217488700196,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.500823598234868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.139947627060437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.632488784600833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.112265078065012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.2737175234511,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.051160777129716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.567116735119255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.084543060187176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.340579380615926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.2669379012971325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.910049332426174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 54.917230421621625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.31211867354236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.65066167974291,
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
        "date": 1779263652085,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 363.7647255166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 332.1141549238095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 311.0003144571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 130.097126325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.94121040718583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 39.97957476486274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 76.6237580897436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.48068616558442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 222.51369621333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 213.34745172999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.36191446994555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 230.60043177777774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.56411325522173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.051986103349710544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.553782395380222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 45.120585509090915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.719529551847096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 328.5673279190476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 233.50682539999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.78394694152329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05987465277793556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.208012733868241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.93499822659784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.89517860061462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 317.497284525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.377777449000543,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9115493831454984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0930845055447422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 18.384952212744498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9258044921862478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.393954424650172,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.40545197813377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.119128782244712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.226906697011572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.662515583722964,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.014016851503424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.185378383929034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.625616505244398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.597454296347074,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.838947699228411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.670730707838995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.47863311914415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.04634289077878,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 46.071421788372085,
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
        "date": 1779349899226,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 400.46029508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 333.77954009999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 309.7836217857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 129.73745639375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.69028427393665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.384438384756294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 81.20463069484614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 99.59437291547619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 221.10875994333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 227.77338183777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.7017795872584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 237.78205781111114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.80251612390536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.050051752924267535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.22525284841209,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 44.57918877103589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.613621938349894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 253.75071723285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 238.35732092222224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 26.230093913424025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058345996632607346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.487609983909213,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 45.13098559513615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.89205988413027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 321.861765002381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.059283448852381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7497124055830513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.066011377754725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.908096722902588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.7247214543748044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.237198262449592,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.158693830721845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.964643964364603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.112193078987992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.672974922287956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.706279541668227,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.68643020657404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.046026151782002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.573831694784792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.531617509368935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.003552829871285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 52.80523857398649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.71524522497315,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.63410575815034,
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
        "date": 1779436170629,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 354.7776689833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 329.80644569285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 309.27471055714284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 126.88250380237744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.11998414984522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 38.536917646226414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.32308381566952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.24105599177489,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 225.98327664444446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 221.20953486888888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.06856758011494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 224.4942276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.978458107908185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05204724202250831,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.481958080698004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.92811448517576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.82156076080532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 276.58599337460316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 227.1837768777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.060263119592655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05886962237379466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.316818433152116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.819115551285854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.990743988661727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 300.7226410222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.381049308040462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9279688844401996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.167553889525236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.430664418480568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.003133436074247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.557736505622575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.13138037395452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.507733034641248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.261571119448162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.45370068693368,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.070419801367542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.181621994312344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.672545978851975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.518237136683771,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.821966954506342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.736031282886955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 56.81742542365079,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.312238252647568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.05965225999999,
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
        "date": 1779521293705,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 390.6721303166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 326.7475868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 305.76716104285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 130.67242741249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.94451647709952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.437190315248465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.46935374230769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.35791097900434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 219.64249917888887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 222.16277491555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.740729257832285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 229.11577416666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.02068861440969,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05005643058157081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.415228037162717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.05130310059937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.628354022185604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 260.3548278436508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 229.65476974444445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.592496424695966,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058922552426600716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.314991590490639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 45.321972923226454,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.016685936105226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 331.22652385714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.097387871827555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.765911248030249,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0604834158194651,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.03627674799867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.024326481995059,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.916291616623559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.109557100460169,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.662085033557148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.2207062549355046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.24312924840619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.928212171422302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.551158290096996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.0688701345198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.151411099632298,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.473076081971273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.03862107947866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 53.40277530843273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.372709643983146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 43.6504994989765,
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
        "date": 1779608346825,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 390.1886689833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 324.01123648571433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 306.1927522285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 129.18368654625002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.94712127735478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.908533438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.43996693076922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.02390736666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 224.56088533333337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 217.69102861555552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.91020106584997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 233.25457451111114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.633672925964014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05107629008173886,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.390729535878334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.19147314037542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.439961609816322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 316.21536221428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 237.68458297777775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.343230004800887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.06018660530025947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.3514792279924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.48284712859903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.602068437957985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 322.66629107142853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.263621098565309,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.839471114231867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.068408633124935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.25246630723698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.8415623064476967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.634165459025905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.136533203457457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.743325854089377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.127849288578153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.455305424219155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.50775894500893,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.802964962888526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.070614650656363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.2500505145209555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.253051356323448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.823344274716268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 51.86292996323492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.350144883260356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 44.49203760643133,
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
        "date": 1779696784171,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 393.30969041,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 331.9890438166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 317.6242622285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 127.35525438933823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 40.17823973939283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.77735095659014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.11761178461538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.4148709695238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 217.52207607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 214.79645217999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.3007190801613,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 231.9204628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.32822548785401,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0502004184367977,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.4475237730095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.300345400156104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.761658282918663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 255.99223987857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 247.11831522499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.495420324438804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.0585248270945372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.379898372805467,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.58453171771507,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.902522780131797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 272.98636018730156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.080699509878833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7690174125476092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0553139372169063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.3001748527016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9504510338924699,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.649841508032022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.127807200895237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.997228637067115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.155789446363646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.70982855429304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.841592475965758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.564312700648292,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.08137002834777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.140743162586754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.392630166085141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.886250553641554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 50.3760426397561,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.230202524162202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 43.738178923830816,
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
        "date": 1779782085332,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 400.85072428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 328.46407974285717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 303.0447516571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 127.13226396249999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.857500699922994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.65545770429579,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.6665636383077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 99.44865471619048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 213.73322876000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 215.53907921,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.31099150163934,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 238.22195946666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.772298377818856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05025875988083763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.195134689304497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.16938332882382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.524954489231696,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 314.08760037142855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 241.56215192222226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.895395903738297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05863631171135344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.349045852975665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.490311019241446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.744877669197052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 263.3686968433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.086525331337201,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7719038945836827,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0492474861052352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.639515680431092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.8299557700406137,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.321111033332125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.392638965596502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.663401465310756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.282930600600914,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.19185937966101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.515821898875192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.517980399994574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.066279164370325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.20750745256605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.239480431213909,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.258263006350376,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.84615200202702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.58137086734717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.5323847248227,
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
        "date": 1779868864801,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 391.68732768999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 322.22816087142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 299.1466205571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 127.81672691875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.948199224943394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.19121646427381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.4716274494302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 94.92175722445889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 221.91755540111112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 219.28708704333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.56837886740258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 231.5564400222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.797602827849907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.054511151184436876,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.223908464560019,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.87829809864516,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.554487034717518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 319.78935502857144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 233.19374502222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.705323320957763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058587129052367536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.2423292545753,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.09208033358002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.606711520946114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 307.9051722142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.075170009876958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.768181830026228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0632080104506259,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.26987822353985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.080280171108853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.256394842634993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.345855003496903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.98112992650311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.152826902848263,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.872685579297546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.500630488354535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.590104534954936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.004440328473255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.115829087232575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.141973686156919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.843328588331167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.92853038460961,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.120583106030303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 43.33876752123034,
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
        "date": 1779954908117,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 379.94672403333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 326.4071407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 306.06287818571434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 131.452730755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.92817512527473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.53399955087192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.44833606153846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.32008651753247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 217.03784807000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 222.62893388666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.567615898502744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 235.4005551333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.03426305293996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05214582478941611,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.327991798141821,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.08335461882517,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.35191757393236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 263.61928229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 235.94557971527774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.308335038825582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05894438167442573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.2758499518422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.21571990845411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.502385414587472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 304.8016266428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.117483534840808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.8500780612131633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0476388252174664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.161208465971278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9860294679500523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.50248734164916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.272030350819053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.207656560392383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.146319229322478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.50871210568082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 27.01736087154264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.819812971421827,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.247434799316853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.261809726241509,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.418488425292331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.649583451808923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 52.70983810783785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.357095641743975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.747570718081256,
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
        "date": 1780041285669,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 385.99883759999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 330.6273002857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 308.7931211714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 129.14240654374996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.088249215703705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.62259450425531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.47178998846154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 99.3291765904762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 225.27637146666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 220.39767096999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.536623783016104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 224.67134629999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.15254771017113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05088576687495224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.2332040109030915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 44.017196400568956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.859485069130777,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 296.1684464428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 233.73533492222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.61457542926999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05831676617519547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.37226421450266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.30611465995169,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.177969999811392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 307.86826544285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.0791832595937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7558149519564785,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.065553459211303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.982752462125655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.8850640068227498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.394080774146895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.315485225788992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.20704603807054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.069327799573616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.236550188829874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.252097657348628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.786688227657718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.006293033817183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.238231768597407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.429402149734858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.13176084215733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 53.193990917494624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.27642588426304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 44.85797106789393,
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
        "date": 1780126414634,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 358.7914028833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 327.1457678714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 320.23202028571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 123.2833774305147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.55103831224264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 39.40215682368929,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 76.55414479216523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.70727661601732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 219.64783113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 218.53798443444444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.229870800072085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 225.6230619111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.014061126027805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05211267617682739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.61398595922541,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.77559342770583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.703877285361635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 355.9395185895238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 237.77985268888892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.815597457867444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058772777687230784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.19392876450701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.79354080657004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.189972719130424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 285.99459078095236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.382304084491959,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9060211599381622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0813847500715303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.720191477066116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9385902440731317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.649581269762937,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.221141430295093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.526258636616628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.238130493398321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.53890481509085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.68976574145334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.166739700042594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.63204879714606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.436152004564495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.815334727765055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.689823829799831,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 54.38424274623044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.157485175265172,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.74790290111112,
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
        "date": 1780213895901,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 387.48866913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 322.56820098571427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 307.8895670285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 125.97621459963234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.313280239215686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 42.898844278641626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 79.28297226707693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 99.35576426119049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 215.77872452000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 213.09523843000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.847587905675304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 233.69901664444447,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.030698932546205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.049521573452015986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.432834479525111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.165219232886216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.7152592990846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 262.8931613528572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 245.67700973055557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 26.368628254368645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05882653503682742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.309137990483766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.57468469976873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.652692554191894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 312.49828695714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.075675224332931,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7709758156658184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.091973265312728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.005895633269304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.1369602262399825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.23091957620746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.301990949688829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.282821737580637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.174975605352381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.331294434383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.288905772317435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.558256658902266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.144125906755594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.078022326070166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.234628493900521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.899309810632781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 54.58877485771298,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.22260935946238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 41.66216805565477,
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
        "date": 1780302110593,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 378.8748210833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 330.3583771285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 311.4206192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 123.77660526985296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.65076462246681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 37.954048948845625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.13915822193732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.50057106380953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 222.17581898666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 218.21104506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.91731756412578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 222.6541336255556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.32619828593232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05240087500123967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.532312295894862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.8937652245133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.95893647397708,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 323.53302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 234.76017295555553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.9758432556449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.06036471641916781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.785796723797272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.11018847688765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.213730128542984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 291.18811691111114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.4519471303582545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.929764539682796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0846981934632542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.42842774748528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.055012167070702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.431526888783875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.492105492147555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.3472062815446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.276324930349894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.77445312031488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.148388717510365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.265891794890665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.599513349197942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.674762397698325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.755414324236748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.797542462449314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 56.71098614674604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.129056489574662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.15273985292929,
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
        "date": 1780387970954,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 383.52029560333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 322.7809173142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 306.3648833285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 129.79860751875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.79790457049396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 40.21671949287395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.99196120384616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.83818641428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 219.27073159222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 218.32007720777773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.3213921813594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 245.30696786944446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.913833671451734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05047352649729773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.2932679380280305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.47019819628765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.61766027650008,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 255.84251346714282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 242.0156963333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.378341097465917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.06009653121048053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.53406986828918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.9806786688889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.531996194096628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 224.60545185555557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.159939187208164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7997406286100164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.2021921852853124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.52403569180627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9055934696118242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.495376362083288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.31147993601105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.765587264762456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.223429183920617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.024163000235184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.65688268573503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.781347871883536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.218994474828992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.661505941955488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.4066271261236825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 15.102921966850857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.35855708701202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.395043935485262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.16249985740248,
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
        "date": 1780475310797,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 317.51020395714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 298.50937244285717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 278.2318775517857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 124.69157986213236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 34.66302417304341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 36.66148933818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 73.95473377261905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 88.20454115652174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 207.50339365999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 213.76636791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.77839982170882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 234.93585414444448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.38467136648148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.051605269514697794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.621836095484683,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 38.93042202357495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.317061537821107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 293.1431658660714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 242.10191366666672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 22.721988782482125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05959772470750006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 8.360331583613302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 40.76332456152903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.371894859602698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 287.7574904785714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.297681545380622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.810460679869665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.1085105691269974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.483465548216625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.7691952676072564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.319833395253292,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 10.950900503277413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.132422493937764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.6734703966852855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.21350640777838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 26.00204131435232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.345660548882238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.05541107305901,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.491898905857127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.185582517287817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.62556515626047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 50.64811883170427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 18.75594951067974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.492538356386866,
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
        "date": 1780561120774,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 316.5967397571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 306.7868231571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 287.4460629053571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 123.1630008220588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 35.085768555811136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 36.211032905390745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 72.04695699162559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 90.74921139980238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 203.61059724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 208.19624241727274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 36.34365080920128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 226.86802856666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.361079208213063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05107254681527016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.647405734980528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 41.050567369673466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.308547222383623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 281.8339427053571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 237.94536509583332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.674473206806198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05839956458363175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 8.309058647526518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 40.376778518749994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.029042823716402,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 303.0186129160714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.381223439387918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.8109659946863355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.146231161369157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.643731028230302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.978047492073106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.414117463380549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 10.851235808749417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.073170666820271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.7629511826440645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.479603546490786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 27.040796274340913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.452874024668757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.041326190369347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.753556064284223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.4224900286501505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.662250251351775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 52.88015529028339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 18.911445543899287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 40.0570115529804,
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
        "date": 1780646616590,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 379.94214711666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 326.03069155714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 302.7696907571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 127.74367541874997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.76228648288888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 44.56684327331357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.39263203846153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.77357018435066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 217.88693912999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 215.12413209000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.77587194352115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 238.2257004666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.517875108620032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.049764787019551955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.325215769347603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 43.003774549441104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.33934324282969,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 319.89962501428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 227.88065617777775,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.576177400079917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058358429266657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.389759809077864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 44.39053237729367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.604975517652065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 264.7585160523809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.063553751087808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.860647836367961,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0506226630408042,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.745179162543062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.774946205651809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.516530276781344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.165718667083782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.841882208934047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.152146142007279,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.27477550095681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.430666111063353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.735413265913083,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.199180914936555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.955696662889655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.0671535051828815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.941274298456062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 51.20083715034084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.852587400601376,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 46.33203947431291,
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
        "date": 1780731413360,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 375.02733365,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 324.60938987142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 306.6268502857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 127.52605847499999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.97664050720629,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.707509775998226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.69370612763535,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.84603427348485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 216.01641204555557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 216.62869413000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.85899632114707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 240.75808392222226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.92646538785521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05059921265662405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.229260761163674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 41.96284558666595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.56847470274894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 304.22087281428566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 234.06025692222224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.43185267664962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.059073612520673544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.356623352493527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 43.77141795405733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.914464849718307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 275.17023275873015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.085813432863591,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7759593794942012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0602796573390747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.866605967004123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.0893855740946585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.244950312425473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.503397253185431,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.543284823244951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.112675266238554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.697206300392054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.238338810641864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.534090751432903,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.051703259812495,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.2227002607857305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.547683708321342,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.947684953232379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.12037095840841,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.593346102798314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 44.95331210519423,
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
        "date": 1780819025974,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 363.11266739999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 330.3823726857143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 311.94171929761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 128.3247366947549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.878705798514474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 39.56978096415385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.50938030886039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.10316127619046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 216.88164841000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 215.80144532999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.41863384658238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 230.41777640333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.1271839981666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05131079565676171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.477117114834888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.94672252061761,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.079749394144496,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 309.19162280833336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 225.37161156666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.885415599688162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05901337715858448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.166712587885835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 42.94159290373689,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.06557638070792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 330.2857362714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.372207718691689,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9136578255267858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0804604451933308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.688030147128096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.121969479554616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.446809324623214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.334937431096684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.907697224173006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.33073315938475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.44117736157636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.035177665539027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.15243248174619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.753381471938715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.590264738408247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.559740918176643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.815644479789412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 57.00601629293651,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.132475128681317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.43431136520202,
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
        "date": 1780906783250,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 355.6840341166666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 332.50921026428574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 314.6567036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 124.8940748235294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 35.88330791102757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 40.14422796369701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 76.20397046396012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.40232907359307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 228.09408539999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 213.73410879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.827914299736996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 226.42701031777779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.620526749392344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05233809286677117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.423598504294526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.256853118749994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 17.223939450920504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 324.87119424285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 226.88601102222225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.88399262319958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.059322153080131626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.12847993398187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 45.04225052479817,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.95295838340825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 286.24971972698415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.379734370133352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9062003881994536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0895474098297853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.468184341286236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.7428507901690953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.37902433414528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.261108554064311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.265955191839463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.283153419801698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.74119001379311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.226655876790282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.020934532598627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.683722554275265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.626898335667576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.769109029935304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.69911706869603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.93656904224581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.13606540578967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.46220807030303,
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
        "date": 1780991421384,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 190.12691922727274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 177.28974184999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 163.10990745064103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 71.27635066748769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 18.418428943811726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 18.47175809730147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 41.5154386781938,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 52.05452328760796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 121.61880282826799,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 120.61379279591503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 20.003617348429778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 125.91786636421568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 8.454831885508614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.028144019746932637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 3.641351984168041,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 22.977958557227534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 9.145544914936806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 226.8767144848485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 130.06376810014706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 11.8459236704782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.03311775869832438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 4.565380774147135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 24.124294357675822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 10.055086106869584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 120.4116356656863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 2.5527399948513354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 1.993712953985207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 0.555804307795103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 8.152727378401162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 0.9328345112075647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 8.404641271242589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 6.596518145290983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 8.484943427670101,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 3.0802931848893356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 19.922200868445923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 15.318969151668131,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 5.444652905225892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 4.245053839983406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 4.022115432517985,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 3.478805271810341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 7.362035835874247,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 29.886134304133925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 11.237056154481241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 23.982262186202835,
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
        "date": 1781079224523,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 397.96670614333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 326.6580860142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 310.1422164142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 125.36899590735293,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.925249717803915,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 40.57976175242577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 76.92985859444443,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.95320853852814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 216.08634511999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 218.8410434711111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.19706481326754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 224.68668295555554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.41399324494733,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04990419974676348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.492855863472114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 41.218841181345844,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.578610038174478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 295.1866313285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 236.45194882222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.5446209595099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.059038993101120665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.010561975482208,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 41.364776167942175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.783790414885637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 303.59212227142854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.070488613422678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7490721978383355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0677745990348668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.302009434318514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9412672517007235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.6115537048038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.22213302055608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.761901639860778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.116938415598356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.10706538792037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.694340341486413,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.535277877987582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.245139763599116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.059890096612795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.2155357645143585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.966307491621757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 56.427415560793655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.603662119681317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.968816455163044,
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
        "date": 1781165889941,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 353.04511025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 332.5959946952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 305.23804454285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 121.91770539411766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.02696161646719,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 38.742750483684794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.23497296452992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.49769194978356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 218.32810605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 218.0001121055556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.41701067999512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 223.29834862555555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.64183746034005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05285643696194747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.504861980973051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 40.19610124862744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.908280843137682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 307.8915094583333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 231.39367103333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.58207234058026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.06000759739790669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.352174047358371,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 41.90595053305597,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.892328818220605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 300.7640141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.391378100234103,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.954111706320918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0876066011528456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.447074321490636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9675657495986278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 16.077531186499193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.432620397697853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.53954018219132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.240403450336023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.67218869043999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.996547087692306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.055000790109577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.695886803591176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.79602127929505,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.6895267186446485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.685193710243402,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 53.98537729345662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.31785657574687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.762225454595956,
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
        "date": 1781251889295,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 374.4722454666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 328.8058921452381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 309.23741625714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 119.96441976503267,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.654849064160956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 39.109866596211475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 79.08348280676924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.9292296859524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 218.76783183333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 214.77489481222224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.77564293738431,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 225.1462313755556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.69733531589039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.051384687324147804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.4454621022923275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.20131103777209,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.661671216108815,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 251.5705028694444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 227.60201675555555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.6794308675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05922822369340688,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.118759297169639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 41.53041139409014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.14843056678484,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 286.0240887460318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.390126427013907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9053405806244768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.094823586478687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.625284057719533,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.3145219132221873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.332848017703963,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.169794598919134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.80821990880715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.315797491076838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.945926343224436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 30.115458376333862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.218856727004859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 10.64830196739645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.3392887555501485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.737188695995054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.68833793837947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.010587424047614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.53299501300278,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.54869765808081,
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
        "date": 1781337081560,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 376.33106283333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 330.4132639714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 310.98900804285717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 128.85764634264703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.539323516432624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 38.611606438837896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 76.93627035797721,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 97.22173882770561,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 226.6668122111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 215.0004409877778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.61734086775637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 221.42888812777778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.51769503113409,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.052110634393232644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.555001446362562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 40.725556618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.97920383343789,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 275.40246144603174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 226.57894976666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.364874012797536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.060317684737223244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.257323849685134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 40.71172397220407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.047373536534018,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 332.23552631666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.484828562021846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 4.037444241456727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0943174855897426,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 18.135969146543232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.2546290594575265,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.551378164174295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 13.459740115436238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.57833873237746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.244214707409111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 34.995308286025406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.185907800000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.125474155484055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.642392283161362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.524285497909025,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.665124246469465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.793659201261153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 56.80917754944445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.18174511871795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 46.46965560628964,
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
        "date": 1781424527039,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 389.34946675000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 330.5966601380952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 317.65836742857135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 126.38762396250002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 39.5422866748171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.168889658714285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 79.1556265023077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 101.42304887238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 224.33799346666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 216.9723645866667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 33.32559006161555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 237.55446444444442,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 18.642705776589352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05095007090914892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.274681553580825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 40.115597707698065,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.516826565106353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 297.90716695714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 255.89401052222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 26.664022229183832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05866182664847286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.55307171032046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 42.23355815789007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.06912501384945,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 304.18801227142853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.10232187607515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.7627410521975264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.1460252557882338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.25284218355701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.8645932891463133,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.634422123141658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.436610054875382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.46627284796983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.122182355037273,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.60825662800106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.213389921576457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.539333134600616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.065688555973358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.128884738824375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.364649317458043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.992581152727658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.34524648851351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.34970662609297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.46727878958333,
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
        "date": 1781512806896,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 336.01626792619044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 313.03941810000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 286.7457564571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 125.52961741102942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 34.91961202837868,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 36.42353471262987,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 73.40435448148148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 88.78154365652173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 206.47876293999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 216.31144900333328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 37.77830996825109,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 238.77828233055553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.313372250715524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05125066594079951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.586165745498635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 39.78852095431071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.81749666102691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 237.99964125555556,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 241.5440636125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.308234870289816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.058036827547237636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 8.307050138133011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 37.6427420090417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.41852692286712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 286.66530200357147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.366663588469271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.877569067094297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.1540274550954972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.920122849941643,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.08636943229165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.586644264962155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.344538686299028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.834580040919738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.7880523449700405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.02847489146828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 26.31700458157895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.352505709640742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.133946959786169,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.67034755193674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.30025195116541,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.481064362058849,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 52.46053074175675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 18.468955770351222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 40.85712902955102,
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
        "date": 1781600012951,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 353.73795451666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 333.1563724952381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 310.0183076571429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 119.57693707777776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 37.04306615546435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 38.56011502217764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 78.34063209484901,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 95.46103091818182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 218.44089440000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 210.89278851999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.15716061477878,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 231.7242072666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.728721261742262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05130291682153045,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.424773371107871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 39.901954029960784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.746082706979585,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 263.9664666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 232.73803424444446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 24.499624283433736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05897473478566134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.131261920265057,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 42.89988330163043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 18.33080190469022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 332.7138381714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.380778832403022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.9162307770356124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0799720501858268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.52646344915987,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.9284385857937765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.403964801971046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.320707907248776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.961267641501871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.273433290932907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.72698116413793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.486903104225355,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.038556453754314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.740138535146922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.451366199183009,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.700197473900644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 14.66700028503549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 56.589355014841274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.461491081118492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 45.89401777439393,
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
        "date": 1781684738874,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 374.9626633833334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 336.1386570166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 312.8694926142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 124.27047095330883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 35.62694746328321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 37.92851366529705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.88809924615384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 98.50058664761904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 217.74588021999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 211.34174428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 34.87838955994973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 226.63766401555554,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.902353983403636,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.05262507471553744,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.339218357403818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 42.275776962877224,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.46028854869237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 290.9579786142857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 228.16417496666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 23.911636542787114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.059792473399102465,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.179469309685487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 41.60672676186702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.93820975001687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 288.00914574126983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.39350177917105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.918893393507907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0850572054930012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 17.11842773037979,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.0952566789066,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 15.429388905782409,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 12.868547668172841,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 15.384631057784791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.266736479154583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 35.69273730703235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 29.84816190916417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 11.299961809783316,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.642272361958039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 6.782861612794221,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 5.7670575138544935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 15.085941402116536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 55.00847859229492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 22.34242224834584,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 46.30527292811839,
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
        "date": 1781770513955,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 346.88486769285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 314.97592701428573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 286.92730623035715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 122.63271585882353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 36.531956037578155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 38.02767293929686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 73.2111316165344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 88.92117755652174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 207.88482535999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 204.91838903363632,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.24119373284287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 244.69012635555555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 16.441950941379314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.0518584084716968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 6.684033283336009,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 37.584096453236135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.35818944936026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 292.46969233392855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 231.17649413333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 22.98631281235845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05857341124965213,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 8.326230090884982,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 38.68441127064373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.23476367277483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 289.3361167571428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.423094549200084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.8112875400924793,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.145565353677565,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 15.789444390968573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 2.006332118946456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.50135344940048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.160765249203028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 13.676857341068356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.650472907884941,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 32.40890094314254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 27.27692228558385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.611612808078416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.021393427950951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.563655140714422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.363716501217839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.385720650010132,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 47.529371415393136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 18.728837733054625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.071119855565215,
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
        "date": 1781857603786,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.configurationDsl",
            "value": 389.3391605666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionDuringMetaclassChurn",
            "value": 327.8192307428572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.controllerActionPattern",
            "value": 306.27668975714283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.domainValidationCycle",
            "value": 123.35734334301472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyByName",
            "value": 38.66060239704824,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.dynamicPropertyDuringMetaclassChurn",
            "value": 41.39774011997672,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleDuringMetaclassChurn",
            "value": 77.92537322649574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.fullRequestCycleSimulation",
            "value": 96.60929062380953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.markupBuilderPattern",
            "value": 218.14083996888888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainCreateAndList",
            "value": 214.29981109222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsLikePatternsBench.serviceChainWithCollections",
            "value": 35.16318231523637,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineCollectionClosureChain",
            "value": 229.94582742333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineDynamicPropertyByName",
            "value": 17.591670795447794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineFullAnalysis",
            "value": 0.04993824520129029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineGStringInterpolation",
            "value": 7.480472822987148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineNestedClosureDelegation",
            "value": 39.976368120263736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineProjectMetrics",
            "value": 16.50962430538795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.baselineSpreadOperator",
            "value": 310.4096638285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.collectionClosureChainWithInvalidation",
            "value": 229.7490344888889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.dynamicPropertyByNameWithInvalidation",
            "value": 25.50065307459185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.fullAnalysisWithInvalidation",
            "value": 0.05884134002710238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.gstringInterpolationWithInvalidation",
            "value": 9.21505721627872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.nestedClosureDelegationWithInvalidation",
            "value": 42.545161091045756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.projectMetricsWithInvalidation",
            "value": 17.859014309004397,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.GrailsWorkloadBench.spreadOperatorWithInvalidation",
            "value": 270.3407571936508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineClosureDispatchNoChanges",
            "value": 4.059280044501624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineMultiClassNoChanges",
            "value": 3.745185491971445,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselineNoMetaclassChanges",
            "value": 1.0616263072117076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.baselinePropertyAccessNoChanges",
            "value": 16.550659495809956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.burstThenSteadyState",
            "value": 1.861868401182407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.closureDispatchDuringMetaclassChurn",
            "value": 14.1273057273423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.expandoMethodAddition",
            "value": 11.322720312931303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.frequentExpandoChanges",
            "value": 14.569071230536082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.metaclassReplacement",
            "value": 5.078773665211364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.multiClassMetaclassChurn",
            "value": 33.10904206247898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassChangeBench.propertyAccessDuringMetaclassChurn",
            "value": 28.12681332634271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineMultiClassNoStartup",
            "value": 10.600271347182847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.baselineSharedMetaclass",
            "value": 8.033312874663912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.dynamicFinderCalls",
            "value": 7.235380585650761,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.mixedCompiledAndDynamicFinders",
            "value": 6.171190730878605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.multiClassStartupThenSteadyState",
            "value": 13.918220353331128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceInjectedMethodCalls",
            "value": 54.6888614365339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceMetaclass",
            "value": 20.48420709986409,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          },
          {
            "name": "org.apache.groovy.perf.grails.MetaclassVariationBench.perInstanceWithOngoingChurn",
            "value": 42.899206275050105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 2\nthreads: 1"
          }
        ]
      }
    ]
  }
}