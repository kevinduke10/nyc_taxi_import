package com.tectonix.taxiimport.utils

import groovy.json.JsonSlurper
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class MapboxDirectionsApi {


    public static List<List<BigDecimal>> getMapboxDirections(double[] pickupPoint, double[] dropoffPoint){
        ThreadDeath.sleep(1001)
        String baseUrl = "https://api.mapbox.com/directions/v5/mapbox/driving/"
        String mapboxAccessToken = "pk.eyJ1Ijoia2V2aW5kdWtlMTAiLCJhIjoiY2l1am5seXNyMDBucDJvbnFtZjUyMmZ2aCJ9.wjc-TOzWktHgMggOZcrvyA"

        CloseableHttpClient client = HttpClients.createDefault();
        String encodedLatLons = URLEncoder.encode(pickupPoint[0].toString() + "," + pickupPoint[1].toString() + ";" +
                dropoffPoint[0].toString() + "," + dropoffPoint[1].toString(), "UTF-8")
        String requestString = baseUrl.concat(encodedLatLons).concat(".json?access_token=" + mapboxAccessToken + "&overview=full&geometries=geojson")
        List<List<BigDecimal>> coordList = new ArrayList<>()

        try {
            HttpGet catIndicies = new HttpGet(requestString)
            CloseableHttpResponse response = client.execute(catIndicies)
            if(response.getStatusLine().statusCode == 200){
                String responseString = EntityUtils.toString(response.getEntity())

                def jsonSlurper = new JsonSlurper()
                def objectResponse = jsonSlurper.parseText(responseString)
                def coordinates = objectResponse.getAt("routes")[0].getAt("geometry").getAt("coordinates")
                if(coordinates != null){
                    client.close()
                    return coordinates
                }
            }
            client.close()
            return null

            //return responseString.contains("elasticsearch")
        }catch(Exception e){
            println "Failure to get Mapbox Directions"
            client.close()
            return null
        }
    }
}
