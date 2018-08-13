package com.tectonix.taxiimport.utils.CSVWithWayPoints

import com.tectonix.taxiimport.utils.MapboxDirectionsApi

class CSVWithWayPoints {

    public static void main(String... args){
        if(args.size() < 1){
            println "Need file location"
            return
        }
        File inputFile = new File(args[0])
        FileInputStream inputStream = new FileInputStream(inputFile)
        BigInteger inputFileLength = 0

        try {
            inputStream.eachLine { line ->
                inputFileLength++
            }

            inputStream.close()


            String newCsvFileName = inputFile.getName().substring(0, inputFile.getName().size() - 4)
            File outputFile = new File(inputFile.parentFile.toString() + "/" + newCsvFileName + "_with_waypoints.csv")

            BigInteger outputFileLength = 0
            if(outputFile.exists()){
                outputFile.eachLine {
                    outputFileLength++
                }
                continueAddingWayPoints(inputFile,outputFile,outputFileLength)
            }else{
                continueAddingWayPoints(inputFile,outputFile,1)
            }

            println "Got files"

        }catch(Exception e){
            e.printStackTrace()
        }

    }

    public static void continueAddingWayPoints(File inputFile, File outputFile, int startLine){
        FileInputStream inputStream = new FileInputStream(inputFile)

        try {
            inputStream.eachLine{ line, number ->
                if(number < startLine) return

                if(line.contains("VendorID")) {
                    outputFile.append(line + ",MapboxDirectionCoords")
                    return
                }

                List<List<BigDecimal>> mapboxDirections = getMapboxCoordsFromLine(line)
                if(mapboxDirections != null){
                    outputFile.append("\n" +  line + "," + getCoordStringFromDirections(mapboxDirections))
                }else{
                    outputFile.append("\n" + "FAILED")
                }
            }
        }catch(Exception e){
            inputStream.close()
        }
    }

    public static List<List<BigDecimal>> getMapboxCoordsFromLine(String line){
        String[] splitLine = line.split(",")
        try {
            double[] pickupLocation = [splitLine[5].toDouble(), splitLine[6].toDouble()]
            double[] dropoffLocation = [splitLine[9].toDouble(), splitLine[10].toDouble()]
            List<List<BigDecimal>> directionCoords = MapboxDirectionsApi.getMapboxDirections(pickupLocation, dropoffLocation)
            Double[] pickupCoord = [directionCoords.get(0)[0].toDouble(), directionCoords.get(0)[1].toDouble()]
            Double[] dropoffCoord = [
                    directionCoords.get(directionCoords.size() - 1)[0].toDouble(),
                    directionCoords.get(directionCoords.size() - 1)[1].toDouble()
            ]
            return directionCoords
        }catch(Exception e){
            e.printStackTrace()
        }
    }

    public static String getCoordStringFromDirections(List<List<BigDecimal>> directions){
        String csvString = ""
        directions.each { coord ->
            csvString = csvString + coord[0] + "&" + coord[1] + "|"
        }
        return csvString
    }
}
