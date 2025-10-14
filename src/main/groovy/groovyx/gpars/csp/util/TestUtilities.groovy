// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.csp.util

@SuppressWarnings("GroovyMultipleReturnPointsPerMethod")
abstract class TestUtilities {

    public static boolean listContains(firstList, secondList) {

        if (firstList.size != secondList.size) {
            return false
        }
        else {
            firstList.sort()
            secondList.sort()
            return (firstList == secondList)
        }
    } // end listContains

    public static boolean list1GEList2(firstList, secondList) {

        if (firstList.size != secondList.size) {
            return false
        }
        else {
            for (i in 0..<firstList.size) {
                if (firstList[i] < secondList[i]) {
                    return false
                }
            }
            return true
        }

    }


}
