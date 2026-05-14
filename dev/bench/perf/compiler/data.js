window.BENCHMARK_DATA = {
  "lastUpdate": 1778795379129,
  "repoUrl": "https://github.com/apache/groovy",
  "entries": {
    "Compiler Performance": [
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
          "id": "0fb707bea1d9460979380ea7a716b9e40a8a058c",
          "message": "add compiler performance to dashboard",
          "timestamp": "2026-05-14T21:27:23Z",
          "url": "https://github.com/apache/groovy/commit/0fb707bea1d9460979380ea7a716b9e40a8a058c"
        },
        "date": 1778795377781,
        "tool": "customSmallerIsBetter",
        "benches": [
          {
            "name": "compile@current",
            "value": 631.67,
            "range": "±46.25",
            "unit": "ms",
            "extra": "current"
          },
          {
            "name": "compile@groovy-3",
            "value": 728.5433333333334,
            "range": "±236.85",
            "unit": "ms",
            "extra": "3.0.25"
          },
          {
            "name": "compile@groovy-4",
            "value": 660.8766666666667,
            "range": "±168.32",
            "unit": "ms",
            "extra": "4.0.32"
          },
          {
            "name": "compile@groovy-5",
            "value": 559.27,
            "range": "±51.98",
            "unit": "ms",
            "extra": "5.0.6"
          }
        ]
      }
    ]
  }
}