/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.json

import groovy.test.GroovyTestCase

class RealJsonPayloadsTest extends GroovyTestCase {

    def parser = new JsonSlurper()

    void testTheGuardianPayload() {
        def content = '''
            {
              "response": {
                "status": "ok",
                "userTier": "free",
                "total": 1234,
                "startIndex": 1,
                "pageSize": 10,
                "currentPage": 1,
                "pages": 124,
                "orderBy": "newest",
                "results": [
                  {
                    "id": "commentisfree/2011/jan/25/best-burns-night-ever-supper",
                    "sectionId": "commentisfree",
                    "sectionName": "Comment is free",
                    "webPublicationDate": "2011-01-25T18:00:04Z",
                    "webTitle": "How to have the Best Burns Night Ever, eh no? | Shelagh McKinlay",
                    "webUrl": "http://www.guardian.co.uk/commentisfree/2011/jan/25/best-burns-night-ever-supper",
                    "apiUrl": "http://content.guardianapis.com/commentisfree/2011/jan/25/best-burns-night-ever-supper"
                  },
                  {
                    "id": "world/2011/jan/23/greens-quit-irish-government",
                    "sectionId": "world",
                    "sectionName": "World news",
                    "webPublicationDate": "2011-01-23T17:04:00Z",
                    "webTitle": "Greens pull out of Irish government after Brian Cowen resignation",
                    "webUrl": "http://www.guardian.co.uk/world/2011/jan/23/greens-quit-irish-government",
                    "apiUrl": "http://content.guardianapis.com/world/2011/jan/23/greens-quit-irish-government"
                  },
                  {
                    "id": "music/2011/jan/23/julie-fowlis-live-perthshire-amber-review",
                    "sectionId": "music",
                    "sectionName": "Music",
                    "webPublicationDate": "2011-01-23T00:05:34Z",
                    "webTitle": "Julie Fowlis: Live at Perthshire Amber – review",
                    "webUrl": "http://www.guardian.co.uk/music/2011/jan/23/julie-fowlis-live-perthshire-amber-review",
                    "apiUrl": "http://content.guardianapis.com/music/2011/jan/23/julie-fowlis-live-perthshire-amber-review"
                  },
                  {
                    "id": "theguardian/2011/jan/26/archive-sinn-feins-declaration-of-independence-1919",
                    "sectionId": "theguardian",
                    "sectionName": "From the Guardian",
                    "webPublicationDate": "2011-01-22T15:41:00Z",
                    "webTitle": "From the archive, 22 January 1919: Sinn Fein's \\"declaration of independence\\"",
                    "webUrl": "http://www.guardian.co.uk/theguardian/2011/jan/26/archive-sinn-feins-declaration-of-independence-1919",
                    "apiUrl": "http://content.guardianapis.com/theguardian/2011/jan/26/archive-sinn-feins-declaration-of-independence-1919"
                  },
                  {
                    "id": "music/2011/jan/20/emily-smith-traivellers-joy-review",
                    "sectionId": "music",
                    "sectionName": "Music",
                    "webPublicationDate": "2011-01-20T22:31:01Z",
                    "webTitle": "Emily Smith: Traiveller's Joy – review ",
                    "webUrl": "http://www.guardian.co.uk/music/2011/jan/20/emily-smith-traivellers-joy-review",
                    "apiUrl": "http://content.guardianapis.com/music/2011/jan/20/emily-smith-traivellers-joy-review"
                  },
                  {
                    "id": "world/2011/jan/20/ireland-exodus-workers-thinktank",
                    "sectionId": "world",
                    "sectionName": "World news",
                    "webPublicationDate": "2011-01-20T19:29:43Z",
                    "webTitle": "Ireland expects another exodus of workers",
                    "webUrl": "http://www.guardian.co.uk/world/2011/jan/20/ireland-exodus-workers-thinktank",
                    "apiUrl": "http://content.guardianapis.com/world/2011/jan/20/ireland-exodus-workers-thinktank"
                  },
                  {
                    "id": "business/ireland-business-blog-with-lisa-ocarroll/2011/jan/20/ireland-emigration-australia",
                    "sectionId": "business",
                    "sectionName": "Business",
                    "webPublicationDate": "2011-01-20T11:05:16Z",
                    "webTitle": "Irish emigration worse than 1980s",
                    "webUrl": "http://www.guardian.co.uk/business/ireland-business-blog-with-lisa-ocarroll/2011/jan/20/ireland-emigration-australia",
                    "apiUrl": "http://content.guardianapis.com/business/ireland-business-blog-with-lisa-ocarroll/2011/jan/20/ireland-emigration-australia"
                  },
                  {
                    "id": "world/2011/jan/19/hotel-security-officer-court-michaela-mcareavey",
                    "sectionId": "world",
                    "sectionName": "World news",
                    "webPublicationDate": "2011-01-19T15:29:10Z",
                    "webTitle": "Hotel security officer in court over Michaela McAreavey murder",
                    "webUrl": "http://www.guardian.co.uk/world/2011/jan/19/hotel-security-officer-court-michaela-mcareavey",
                    "apiUrl": "http://content.guardianapis.com/world/2011/jan/19/hotel-security-officer-court-michaela-mcareavey"
                  },
                  {
                    "id": "world/2011/jan/19/michaela-mcareavey-murder-suspects-mauritius",
                    "sectionId": "world",
                    "sectionName": "World news",
                    "webPublicationDate": "2011-01-19T14:47:00Z",
                    "webTitle": "Michaela McAreavey murder suspects appear in court",
                    "webUrl": "http://www.guardian.co.uk/world/2011/jan/19/michaela-mcareavey-murder-suspects-mauritius",
                    "apiUrl": "http://content.guardianapis.com/world/2011/jan/19/michaela-mcareavey-murder-suspects-mauritius"
                  },
                  {
                    "id": "world/2011/jan/18/michaela-mcareavey-murder-fourth-arrest",
                    "sectionId": "world",
                    "sectionName": "World news",
                    "webPublicationDate": "2011-01-18T16:41:14Z",
                    "webTitle": "Fourth arrest in Michaela McAreavey murder investigation",
                    "webUrl": "http://www.guardian.co.uk/world/2011/jan/18/michaela-mcareavey-murder-fourth-arrest",
                    "apiUrl": "http://content.guardianapis.com/world/2011/jan/18/michaela-mcareavey-murder-fourth-arrest"
                  }
                ]
              }
            }
        '''

        def result = parser.parseText(content)

        assert result.response.status == "ok"
        assert result.response.total == 1234
        assert result.response.results.size() == 10
        assert result.response.results[9].sectionId == "world"
        assert result.response.results[9].webTitle == "Fourth arrest in Michaela McAreavey murder investigation"
    }

    void testGoogleShortener() {
        def content = '''
            {
             "kind": "urlshortener#url",
             "id": "http://goo.gl/fbsS",
             "longUrl": "http://www.google.com/",
             "status": "OK",
             "created": "2009-12-13T07:22:55.000+00:00",
             "analytics": {
              "allTime": {
               "shortUrlClicks": "3227",
               "longUrlClicks": "9358",
               "referrers": [ { "count": "2160", "id": "Unknown/empty" } ],
               "countries": [ { "count": "1022", "id": "US" } ],
               "browsers": [ { "count": "1025", "id": "Firefox" } ],
               "platforms": [ { "count": "2278", "id": "Windows" } ]
              },
              "month": { },
              "week": { },
              "day": { },
              "twoHours": { }
             }
            }
        '''

        def result = parser.parseText(content)

        assert result.analytics.allTime.referrers[0].count == "2160"
    }

    void testGoogleMaps() {
        def content = '''
            {
              "name": "1600 Amphitheatre Parkway, Mountain View, CA, USA",
              "Status": {
                "code": 200,
                "request": "geocode"
              },
              "Placemark": [
                {
                  "address": "1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA",
                  "AddressDetails": {
                    "Country": {
                      "CountryNameCode": "US",
                      "AdministrativeArea": {
                        "AdministrativeAreaName": "CA",
                        "SubAdministrativeArea": {
                          "SubAdministrativeAreaName": "Santa Clara",
                          "Locality": {
                            "LocalityName": "Mountain View",
                            "Thoroughfare": {
                              "ThoroughfareName": "1600 Amphitheatre Pkwy"
                            },
                            "PostalCode": {
                              "PostalCodeNumber": "94043"
                            }
                          }
                        }
                      }
                    },
                    "Accuracy": 8
                  },
                  "Point": {
                    "coordinates": [-122.083739, 37.423021, 0]
                  }
                }
              ]
            }
        '''

        def result = parser.parseText(content)

        assert result.Status.code == 200
        assert result.Placemark[0].address == "1600 Amphitheatre Pkwy, Mountain View, CA 94043, USA"
        assert result.Placemark[0].AddressDetails.Country.AdministrativeArea.SubAdministrativeArea.Locality.PostalCode.PostalCodeNumber == "94043"
    }
}
