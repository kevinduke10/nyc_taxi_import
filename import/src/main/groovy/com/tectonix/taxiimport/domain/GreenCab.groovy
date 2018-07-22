package com.tectonix.taxiimport.domain

class GreenCab {
    //1= Creative Mobile Technologies, LLC; 2= VeriFone Inc.
    Integer vendorId
    Date tpepPickupDatetime
    Date tpepDropoffDatetime
    Boolean storeAndFwdFlag
    String rateType
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
    String paymentType
    String tripType
    Double[] location
}
