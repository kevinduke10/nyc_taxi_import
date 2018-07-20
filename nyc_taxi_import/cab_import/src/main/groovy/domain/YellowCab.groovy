package domain

class YellowCab {
    //1= Creative Mobile Technologies, LLC; 2= VeriFone Inc.
    Integer vendorId
    Date tpepPickupDatetime
    Date tpepDropoffDatetime
    Integer passengerCount
    Double tripDistance

    //    1= Standard rate
    //    2=JFK
    //    3=Newark
    //    4=Nassau or Westchester
    //    5=Negotiated fare
    //    6=Group ride
    Integer ratecodeId

    Boolean storeAndFwdFlag
    Integer puLocationId
    Integer doLocationId

    //    1= Credit card
    //    2= Cash
    //    3= No charge
    //    4= Dispute
    //    5= Unknown
    //    6= Voided trip
    Integer paymentType
    Double fareAmount
    Double extra
    Double mtaTax
    Double tipAmount
    Double tollsAmount
    Double improvementSurcharge
    Double totalAmount
}
