import domain.GreenCab
import domain.YellowCab

import java.text.DateFormat
import java.text.FieldPosition
import java.text.ParsePosition
import java.text.SimpleDateFormat

class Application {

    public static void main(String[] args) {

            File sampleFile = new File("/Users/Kev/Development/nyc_taxi_import/nyc_taxi_import/yellow_cab/src/main/resources/sample_yellow_taxi.csv")
            FileInputStream inputStream = new FileInputStream(sampleFile)
            DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
            inputStream.eachLine {line ->
                String[] splitLine = line.split(",")
                if(splitLine.length > 0 && !splitLine[0].equalsIgnoreCase("vendorId")) {
                    YellowCab yellowCab = new YellowCab(
                            vendorId: splitLine[0].toInteger(),
                            tpepPickupDatetime: dateFormat.parse(splitLine[1]),
                            tpepDropoffDatetime: dateFormat.parse(splitLine[2]),
                            passengerCount: splitLine[3].toInteger(),
                            tripDistance: splitLine[4].toDouble(),
                            ratecodeId: splitLine[5].toInteger(),
                            storeAndFwdFlag: splitLine[6].toBoolean(),
                            puLocationId: splitLine[7].toInteger(),
                            doLocationId: splitLine[8].toInteger(),
                            paymentType: splitLine[9].toInteger(),
                            fareAmount: splitLine[10].toDouble(),
                            extra: splitLine[11].toDouble(),
                            mtaTax: splitLine[12].toDouble(),
                            tipAmount: splitLine[13].toDouble(),
                            tollsAmount: splitLine[14].toDouble(),
                            improvementSurcharge: splitLine[15].toDouble(),
                            totalAmount: splitLine[16].toDouble()
                    )
                }
            }

            File greenSampleFile = new File("/Users/Kev/Development/nyc_taxi_import/nyc_taxi_import/yellow_cab/src/main/resources/sample_green_taxi.csv")
            FileInputStream greenInputStream = new FileInputStream(greenSampleFile)
            greenInputStream.eachLine {line ->
                String[] splitLine = line.split(",")
                if(splitLine.length > 0 && !splitLine[0].equalsIgnoreCase("vendorId")) {
                    GreenCab greenCab = new GreenCab(
                            vendorId: splitLine[0].toInteger(),
                            tpepPickupDatetime: dateFormat.parse(splitLine[1]),
                            tpepDropoffDatetime: dateFormat.parse(splitLine[2]),
                            storeAndFwdFlag: splitLine[3].toBoolean(),
                            ratecodeId: splitLine[4].toInteger(),
                            puLocationId: splitLine[5].toInteger(),
                            doLocationId: splitLine[6].toInteger(),

                            passengerCount: splitLine[7].toInteger(),
                            tripDistance: splitLine[8].toDouble(),
                            fareAmount: splitLine[9].toDouble(),
                            extra: splitLine[10].toDouble(),
                            mtaTax: splitLine[11].toDouble(),
                            tipAmount: splitLine[12].toDouble(),
                            tollsAmount: splitLine[13].toDouble(),
                            ehailFee: splitLine[14].toDouble(),
                            improvementSurcharge: splitLine[15].toDouble(),
                            totalAmount: splitLine[16].toDouble(),
                            paymentType: splitLine[17].toInteger(),
                            tripType: splitLine[18].toInteger()
                    )
                    println greenCab
                }
            }
        }



}
