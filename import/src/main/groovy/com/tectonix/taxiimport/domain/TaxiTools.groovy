package com.tectonix.taxiimport.domain

class TaxiTools {

    private static Map<Integer,String> rateTypeById = [ 1:"Standard Rate",
                                                 2:"JFK",
                                                 3:"Newark",
                                                 4:"Nassau or Westchester",
                                                 5:"Negotiated Fare",
                                                 6:"Group Ride"
                                                ]

    private static Map<Integer,String> paymentTypeById = [ 1:"Credit Card",
                                                 2:"Cash",
                                                 3:"No Charge",
                                                 4:"Dispute",
                                                 5:"Unknown",
                                                 6:"Voided Trip"
                                                ]

    private static Map<Integer,String> tripTypeById = [1:"Street Hail",2:"Dispatch"]

    public static String getRateTypeById(Integer rateCodeId){
        return rateTypeById[rateCodeId]
    }

    public static String getPaymentTypeById(Integer rateCodeId){
        return paymentTypeById[rateCodeId]
    }

    public static String getTripTypeById(Integer tripType){
        return tripTypeById[tripType]
    }
}
