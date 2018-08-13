package com.tectonix.taxiimport

import com.tectonix.taxiimport.domain.GreenCab
import com.tectonix.taxiimport.domain.TaxiTools
import com.tectonix.taxiimport.domain.YellowCab
import com.tectonix.taxiimport.utils.MapboxDirectionsApi
import groovy.json.JsonOutput
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Component
class TaxiImport {
    DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
    @Value('${data.location}')
    private String importDataLocation
    @Value('${bulk.index.size:10}')
    private Integer bulkIndexSize
    private Boolean bulkIndexingFailed = false

    @PostConstruct
    private void init(){
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                ensureIndexPopulated()
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(runnable,0,10,java.util.concurrent.TimeUnit.MINUTES)
    }

    public void ensureIndexPopulated(){
        try{
            boolean gotToTectonix = canGetToTectonix()
            boolean indexExists = checkTaxiIndexExists()

            if(gotToTectonix && !indexExists){
                println "Index doesn't exist creating"
                createTaxiIndexMapping()
                File fileDirectory = new File(importDataLocation)
                fileDirectory.listFiles().each{ file ->
                    println "Ingesting file = ${file.name}"
                    indexYellowTaxisFromFile(file.getAbsolutePath())
                }
            }
        }catch(Exception e){
            e.printStackTrace()
        }
    }

//    public void deleteTaxiIndex(){
//        HttpDelete deleteTaxis = new HttpDelete('http://www.pangea-demo.com/es/taxi')
//        CloseableHttpResponse response = client.execute(deleteTaxis)
//        //client.close()
//    }

    public boolean canGetToTectonix(){
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet catIndicies = new HttpGet('http://demo.tectonix.com/_cat/indices')
            CloseableHttpResponse response = client.execute(catIndicies)
            String responseString = EntityUtils.toString(response.getEntity())
            client.close()
            return responseString.contains("green")
        }catch(Exception e){
            "Can't get to Tectonix-Demo ES instance"
            return false
        }
    }

    public boolean checkTaxiIndexExists(){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet catIndicies = new HttpGet('http://demo.tectonix.com/_cat/indices')
        CloseableHttpResponse response = client.execute(catIndicies)
        String responseString = EntityUtils.toString(response.getEntity())
        client.close()
        return responseString.contains("taxi")
    }

    public void indexYellowTaxisFromFile(String pathName){
        File yellowFile = new File(pathName)
        FileInputStream inputStream = new FileInputStream(yellowFile)
        List<YellowCab> yellowCabs = new ArrayList<>()
        BigInteger bigInt = 0
        try {
            inputStream.eachLine { line ->
                if(bulkIndexingFailed){
                    throw new Exception("Failed indexing")
                }
                bigInt = bigInt + 1
//                if (yellowCabs.size() > bulkIndexSize) {
//                    bulkIndexTaxis(yellowCabs)
//                    yellowCabs.clear()
//                }
                yellowCabs.clear()
                String[] splitLine = line.split(",")
                if (splitLine.length > 0 && !splitLine[0].equalsIgnoreCase("vendorId")) {

                    try {
                        double[] pickupLocation = [splitLine[5].toDouble(), splitLine[6].toDouble()]
                        double[] dropoffLocation = [splitLine[9].toDouble(), splitLine[10].toDouble()]
                        List<List<BigDecimal>> directionCoords = MapboxDirectionsApi.getMapboxDirections(pickupLocation, dropoffLocation)
                        Double[] pickupCoord = [directionCoords.get(0)[0].toDouble(), directionCoords.get(0)[1].toDouble()]
                        Double[] dropoffCoord = [
                                directionCoords.get(directionCoords.size() - 1)[0].toDouble(),
                                directionCoords.get(directionCoords.size() - 1)[1].toDouble()
                        ]

                        long pickupTime = dateFormat.parse(splitLine[1]).getTime()
                        long dropOffTime = dateFormat.parse(splitLine[2]).getTime()

                        long totalTripTime = dropOffTime - pickupTime

                        long now = new Date().getTime()
                        long fakeStartTime = now - totalTripTime

                        if (totalTripTime > 0) {
                            long timeBetweenCoords = totalTripTime / directionCoords.size()
                            long currentTimeReading = fakeStartTime

                            directionCoords.each { coord ->
                                def location = [coord[0].toDouble(), coord[1].toDouble()]

                                yellowCabs.add(
                                        new YellowCab(
                                                vendorId: splitLine[0].toInteger(),
                                                tpepPickupDatetime: dateFormat.parse(splitLine[1]).getTime(),
                                                tpepDropoffDatetime: dateFormat.parse(splitLine[2]).getTime(),
                                                passengerCount: splitLine[3].toInteger(),
                                                tripDistance: splitLine[4].toDouble(),
                                                pickupLocation: pickupCoord,
                                                rateType: TaxiTools.getRateTypeById(splitLine[7].toInteger()),
                                                storeAndFwdFlag: splitLine[8].toBoolean(),
                                                dropoffLocation: dropoffCoord,
                                                paymentType: TaxiTools.getPaymentTypeById(splitLine[11].toInteger()),
                                                fareAmount: splitLine[12].toDouble(),
                                                extra: splitLine[13].toDouble(),
                                                mtaTax: splitLine[14].toDouble(),
                                                tipAmount: splitLine[15].toDouble(),
                                                tollsAmount: splitLine[16].toDouble(),
                                                improvementSurcharge: splitLine[17].toDouble(),
                                                totalAmount: splitLine[18].toDouble(),
                                                location: [location[0], location[1]],
                                                timestamp: currentTimeReading
                                        )
                                )

                                currentTimeReading = currentTimeReading + timeBetweenCoords

                            }
                        }

                        if (yellowCabs.size() > 0 && !bulkIndexingFailed) {
                            bulkIndexTaxis(yellowCabs)
                        }

                    }catch(Exception e){
                        e.printStackTrace()
                    }
                }
            }
            //Index whatever is left in cab buffer
            if (yellowCabs.size() > 0 && !bulkIndexingFailed) {
                bulkIndexTaxis(yellowCabs)
            }
        }catch(Exception e){
            "Error parsing yellow taxis"
            e.printStackTrace()
        }
    }

    public List<GreenCab> readGreenTaxisFromFile(String fileName){
        File greenSampleFile = new File(importDataLocation)
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
        HttpPut createMapping = new HttpPut("http://demo.tectonix.com/taxi")
        createMapping.setHeader("Content-type", "application/json");
        StringEntity entity = new StringEntity("{\n" +
                "  \"mappings\": {\n" +
                "    \"taxi\": {\n" +
                "      \"_all\": {\n" +
                "        \"enabled\": false\n" +
                "      },\n" +
                "      \"dynamic\": \"false\",\n" +
                "      \"properties\": {\n" +
                "      \t\"timestamp\": {\n" +
                "          \"type\": \"date\"\n" +
                "        },\n" +
                "        \"tpepDropoffDatetime\": {\n" +
                "          \"type\": \"date\"\n" +
                "        },\n" +

                "       \"paymentType\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +

                "       \"passengerCount\": {\n" +
                "          \"type\": \"integer\"\n" +
                "        },\n" +

                "       \"vendorId\": {\n" +
                "          \"type\": \"integer\"\n" +
                "        },\n" +

                "       \"fareAmount\": {\n" +
                "          \"type\": \"double\"\n" +
                "        },\n" +

                "       \"totalAmount\": {\n" +
                "          \"type\": \"double\"\n" +
                "        },\n" +


                "        \"location\": {\n" +
                "          \"type\": \"geo_point\",\n" +
                "          \"fields\": {\n" +
                "            \"tectonix\": {\n" +
                "              \"type\": \"tectonix\"\n" +
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

    CloseableHttpClient indexClient = HttpClients.createDefault();
    public void bulkIndexTaxis(List<YellowCab> yellowCabs){
        try {
            HttpPost bulkPost = new HttpPost("http://demo.tectonix.com/_bulk")
            bulkPost.setHeader("Content-type", "application/json");
            String bulkContent = ""
            yellowCabs.each { cab ->
                String bulkIndexHeader = "{\"index\":{ \"_index\" : \"taxi\",\"_type\" : \"taxi\", \"_id\" : \"${UUID.randomUUID()}\" }}\n"
                bulkContent += bulkIndexHeader + JsonOutput.toJson(cab) + "\n"
            }
            StringEntity entity = new StringEntity(bulkContent + "\n")
            bulkPost.setEntity(entity)
            CloseableHttpResponse response = indexClient.execute(bulkPost);
            String bulkResponse = EntityUtils.toString(response.getEntity());

            if(!bulkResponse.contains("errors\":false")){
                println "Error in bulk: ${bulkResponse}"
                bulkIndexingFailed = true
            }
        }catch(Exception e){
            println "Error bulk indexing"
            e.printStackTrace()
            indexClient.close()
            indexClient = HttpClients.createDefault()
            bulkIndexingFailed = true
        }
    }
}
