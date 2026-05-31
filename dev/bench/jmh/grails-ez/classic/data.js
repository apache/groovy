window.BENCHMARK_DATA = {
  "lastUpdate": 1780213897504,
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
      }
    ]
  }
}