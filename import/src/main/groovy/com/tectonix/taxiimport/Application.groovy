package com.tectonix.taxiimport

class Application {

    public static void main(String[] args) {
        try {
            TaxiImport taxiImport = new TaxiImport()
            taxiImport.deleteTaxiIndex()
            boolean taxiIndexExists = taxiImport.checkTaxiIndexExists()
            if(!taxiIndexExists){
                taxiImport.createTaxiIndexMapping()
                taxiImport.indexYellowTaxisFromFile()
            }

        } catch (Exception e) {
            e.printStackTrace()
        }
    }
}
