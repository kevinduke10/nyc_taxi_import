package com.tectonix.taxiimport

import com.sun.deploy.net.HttpRequest
import com.tectonix.taxiimport.domain.GreenCab
import com.tectonix.taxiimport.domain.TaxiTools
import com.tectonix.taxiimport.domain.YellowCab
import groovy.json.JsonOutput
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import java.text.DateFormat
import java.text.SimpleDateFormat

class TaxiImport {
    DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')

    public void deleteTaxiIndex(){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete deleteTaxis = new HttpDelete('http://www.pangea-demo.com/es/taxi')
        CloseableHttpResponse response = client.execute(deleteTaxis)
        client.close()
    }

    public boolean checkTaxiIndexExists(){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet catIndicies = new HttpGet('http://www.pangea-demo.com/es/_cat/indices')
        CloseableHttpResponse response = client.execute(catIndicies)
        return EntityUtils.toString(response.getEntity()).contains("taxi")
    }


    public void indexYellowTaxisFromFile(String fileName){
        File sampleFile = new File("/Users/Kev/Downloads/yellow_tripdata_2016-01.csv")
        FileInputStream inputStream = new FileInputStream(sampleFile)
        List<YellowCab> yellowCabs = new ArrayList<>()

        inputStream.eachLine { line ->
            if(yellowCabs.size() > 900){
                boolean bulkSuccess = bulkIndexTaxis(yellowCabs)
                if(bulkSuccess){
                    "success"
                }else{
                    "failed"
                }
                yellowCabs.clear()
            }
            String[]splitLine = line.split(",")
            if (splitLine.length > 0 && !splitLine[0].equalsIgnoreCase("vendorId")) {
                yellowCabs.add(
                        new YellowCab(
                            vendorId:splitLine[0].toInteger(),
                            tpepPickupDatetime:dateFormat.parse(splitLine[1]),
                            tpepDropoffDatetime:dateFormat.parse(splitLine[2]),
                            passengerCount:splitLine[3].toInteger(),
                            tripDistance:splitLine[4].toDouble(),
                            pickupLocation: [splitLine[5].toDouble(),splitLine[6].toDouble()],
                            rateType: TaxiTools.getRateTypeById(splitLine[7].toInteger()),
                            storeAndFwdFlag:splitLine[8].toBoolean(),
                            dropoffLocation: [splitLine[9].toDouble(),splitLine[10].toDouble()],
                            paymentType: TaxiTools.getPaymentTypeById(splitLine[11].toInteger()),
                            fareAmount:splitLine[12].toDouble(),
                            extra:splitLine[13].toDouble(),
                            mtaTax:splitLine[14].toDouble(),
                            tipAmount:splitLine[15].toDouble(),
                            tollsAmount:splitLine[16].toDouble(),
                            improvementSurcharge:splitLine[17].toDouble(),
                            totalAmount:splitLine[18].toDouble()
                    )
                )
            }
        }
        //Index whatever is left in cab buffer
        if(yellowCabs.size() > 0){
            bulkIndexTaxis(yellowCabs)
        }
    }

    public List<GreenCab> readGreenTaxisFromFile(String fileName){
        File greenSampleFile = new File("/Users/Kev/Development/nyc_taxi_import/import/src/main/resources/sample_green_taxi.csv")
        FileInputStream greenInputStream = new FileInputStream(greenSampleFile)
        List<GreenCab> greenCabs = greenInputStream.eachLine {
            line ->
                String[]splitLine = line.split(",")
                if (splitLine.length > 0 && !splitLine[0].equalsIgnoreCase("vendorId")) {
                    GreenCab greenCab = new GreenCab(
                            vendorId:splitLine[0].toInteger(),
                            tpepPickupDatetime:dateFormat.parse(splitLine[1]),
                            tpepDropoffDatetime:dateFormat.parse(splitLine[2]),
                            storeAndFwdFlag:splitLine[3].toBoolean(),
                            rateType: TaxiTools.getRateTypeById(splitLine[4].toInteger()),
                            puLocationId:splitLine[5].toInteger(),
                            doLocationId:splitLine[6].toInteger(),

                            passengerCount:splitLine[7].toInteger(),
                            tripDistance:splitLine[8].toDouble(),
                            fareAmount:splitLine[9].toDouble(),
                            extra:splitLine[10].toDouble(),
                            mtaTax:splitLine[11].toDouble(),
                            tipAmount:splitLine[12].toDouble(),
                            tollsAmount:splitLine[13].toDouble(),
                            //ehailFee:splitLine[14].toDouble(),
                            improvementSurcharge:splitLine[15].toDouble(),
                            totalAmount:splitLine[16].toDouble(),
                            paymentType: TaxiTools.getPaymentTypeById(splitLine[17].toInteger()),
                            tripType: TaxiTools.getTripTypeById(splitLine[18].toInteger())
                    )
                }
        }.collect().asList()
        return greenCabs
    }

    public void createTaxiIndexMapping(){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut createMapping = new HttpPut("http://www.pangea-demo.com/es/taxi")
        createMapping.setHeader("Content-type", "application/json");
        StringEntity entity = new StringEntity("{\n" +
                "  \"mappings\": {\n" +
                "    \"taxi\": {\n" +
                "      \"_all\": {\n" +
                "        \"enabled\": false\n" +
                "      },\n" +
                "      \"dynamic\": \"false\",\n" +
                "      \"properties\": {\n" +
                "      \t\n" +
                "        \"location\": {\n" +
                "          \"type\": \"geo_point\",\n" +
                "          \"fields\": {\n" +
                "            \"pickupLocation\": {\n" +
                "              \"type\": \"pangea\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"settings\": {\n" +
                "    \"index\": {\n" +
                "      \"number_of_shards\": 4,\n" +
                "      \"number_of_replicas\": 0\n" +
                "    }\n" +
                "  }\n" +
                "}");
        createMapping.setEntity(entity)
        CloseableHttpResponse response = client.execute(createMapping);
        String responseString = EntityUtils.toString(response.getEntity());
        client.close();
    }

    public boolean bulkIndexTaxis(List<YellowCab> yellowCabs){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost bulkPost = new HttpPost("http://www.pangea-demo.com/es/_bulk")
        bulkPost.setHeader("Content-type", "application/json");
        String bulkContent = ""
        yellowCabs.each{cab ->
            String bulkIndexHeader = "{\"index\":{ \"_index\" : \"taxi\",\"_type\" : \"taxi\", \"_id\" : \"${UUID.randomUUID()}\" }}\n"
            bulkContent += bulkIndexHeader + JsonOutput.toJson(cab) + "\n"
        }
        StringEntity entity = new StringEntity(bulkContent + "\n")
        bulkPost.setEntity(entity)
        CloseableHttpResponse response = client.execute(bulkPost);
        String bulkResponse = EntityUtils.toString(response.getEntity());
        client.close();
        return bulkResponse.contains("errors\":false")
    }
}
