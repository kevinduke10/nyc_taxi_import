package domain

class GreenCab {
    //1= Creative Mobile Technologies, LLC; 2= VeriFone Inc.
    Integer vendorId
    Date tpepPickupDatetime
    Date tpepDropoffDatetime
    Boolean storeAndFwdFlag

    //    1= Standard rate
    //    2=JFK
    //    3=Newark
    //    4=Nassau or Westchester
    //    5=Negotiated fare
    //    6=Group ride
    Integer ratecodeId

    Integer puLocationId
    Integer doLocationId

    Integer passengerCount
    Double tripDistance
    Double fareAmount
    Double extra
    Double mtaTax
    Double tipAmount
    Double tollsAmount
    Double ehailFee
    Double improvementSurcharge
    Double totalAmount

    //    1= Credit card
    //    2= Cash
    //    3= No charge
    //    4= Dispute
    //    5= Unknown
    //    6= Voided trip
    Integer paymentType

    //1= Street-hail
    //2= Dispatch
    Integer tripType
}
