package com.tectonix.taxiimport.domain

class YellowCab {
    //1= Creative Mobile Technologies, LLC; 2= VeriFone Inc.
    Integer vendorId
    long tpepPickupDatetime
    long tpepDropoffDatetime
    Integer passengerCount
    Double tripDistance
    String rateType
    Boolean storeAndFwdFlag
    Integer puLocationId
    Integer doLocationId
    String paymentType
    Double fareAmount
    Double extra
    Double mtaTax
    Double tipAmount
    Double tollsAmount
    Double improvementSurcharge
    Double totalAmount
    Double[] pickupLocation
    Double[] dropoffLocation
}
