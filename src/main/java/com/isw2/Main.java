package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;

import java.sql.SQLException;
import java.text.ParseException;


public class Main {
    private static final String AUTHOR = "apache";
    private static final String[][] COLD_START_PROJECTS = { //Prese la meta delle release su jira
            {"accumulo", "1.7.0"},
            {"tajo", "0.11.0"},
            {"pig", "0.13.0"},
            {"syncope", "2.0.4"},
            {"avro", "1.7.0"},
            {"hadoop", "2.6.4"},
            {"kafka", "2.6.2"}
    };

    public static void main(String[] args) throws ParseException, SQLException {
        ScraperController bookkeeperScraperController = new ScraperController("bookkeeper", AUTHOR);
        bookkeeperScraperController.getProjectDataFromDb();
        //bookkeeperScraperController.saveProjectDataOnDb("4.5.0", "2017-06-16");
        MeasureController bookkeeperMeasureController = new MeasureController(bookkeeperScraperController.getProject());
        for(String[] project: COLD_START_PROJECTS) {
            System.out.println("Processing cold start data of project: " + project[0] +" until release "+ project[1]);
            ScraperController scraperController = new ScraperController(project[0], AUTHOR);
            //scraperController.saveColdStartDataOnDb(project[1]);
            scraperController.getColdStartDataFromDb();
            bookkeeperMeasureController.addColdStartProportionProject(scraperController.getProject());
        }
        double coldStartProportion=bookkeeperMeasureController.computeColdStartProportion();
        System.out.println("Cold start proportion: " + coldStartProportion);
        bookkeeperMeasureController.setColdStartProportion(coldStartProportion);

        bookkeeperMeasureController.createWalkForwardDatasets();
    }
}
