window.BENCHMARK_DATA = {
  "lastUpdate": 1778658218073,
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
      }
    ]
  }
}