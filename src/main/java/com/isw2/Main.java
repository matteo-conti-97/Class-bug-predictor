package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;

import java.text.ParseException;


public class Main {

    public static void main(String[] args) throws ParseException {
        ScraperController scraperController = new ScraperController("bookkeeper", "apache");
        scraperController.getProjectDataFromDb();
        //scraperController.saveProjectDataOnDb();


        MeasureController measureController = new MeasureController(scraperController.getProject());
        measureController.createWalkForwardDatasets();
    }
}
