package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;

import java.sql.SQLException;
import java.text.ParseException;


public class Main {
    private static final String AUTHOR = "apache";
    private static final String ZOOKEEPER = "zookeeper";
    private static final String BOOKKEEPER = "bookkeeper";
    private static final String LAST_BOOKKEEPER_RELEASE = "4.5.0";
    private static final String LAST_BOOKKEEPER_RELEASE_END = "2017-06-16";
    private static final String LAST_ZOOKEEPER_RELEASE = "3.5.3";
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
        ScraperController scraperController = new ScraperController(ZOOKEEPER, AUTHOR);
        //scraperController.saveProjectDataOnDb(LAST_ZOOKEEPER_RELEASE,null);
        scraperController.getProjectDataFromDb();
        MeasureController measureController = new MeasureController(scraperController.getProject());
        for (String[] project : COLD_START_PROJECTS) {
            System.out.println("Processing cold start data of project: " + project[0] + " until release " + project[1]);
            ScraperController coldStartScraperController = new ScraperController(project[0], AUTHOR);
            //coldStartScraperController.saveColdStartDataOnDb(project[1]);
            coldStartScraperController.getColdStartDataFromDb();
            measureController.addColdStartProportionProject(coldStartScraperController.getProject());
        }
        double coldStartProportion = measureController.computeColdStartProportion();
        System.out.println("Cold start proportion: " + coldStartProportion);
        measureController.setColdStartProportion(coldStartProportion);
        measureController.createWalkForwardDatasets();
    }
}
