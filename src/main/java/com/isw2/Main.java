package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());
    private static final String AUTHOR = "apache";
    private static final String ZOOKEEPER = "zookeeper";
    private static final String BOOKKEEPER = "bookkeeper";
    private static final String BOOKKEEPER_CREATION = "2011-03-30";
    private static final String ZOOKEEPER_CREATION = "2008-05-19";
    private static final String LAST_BOOKKEEPER_RELEASE = "4.3.0";
    private static final String LAST_ZOOKEEPER_RELEASE = "3.5.1";
    private static final String[][] COLD_START_PROJECTS = { //Prese la meta delle release su jira
            {"accumulo", "1.7.0", "2011-10-06"},
            {"tajo", "0.11.0", "2011-12-09"},
            {"pig", "0.13.0", "2007-10-29"},
            {"syncope", "2.0.4", "2010-04-29"},
            {"avro", "1.7.0", "2009-05-21"},
            {"kafka", "2.6.2", "2011-08-01"}
    };

    public static void main(String[] args) throws ParseException, SQLException {
        createDataset(BOOKKEEPER, BOOKKEEPER_CREATION, LAST_BOOKKEEPER_RELEASE);
        createDataset(ZOOKEEPER, ZOOKEEPER_CREATION, LAST_ZOOKEEPER_RELEASE);
        //scrapeDatasetData(BOOKKEEPER, BOOKKEEPER_CREATION);
        //scrapeDatasetData(ZOOKEEPER, ZOOKEEPER_CREATION);
    }

    public static void createDataset(String project, String projectCreationDate, String lastRelease) throws ParseException, SQLException {
        ScraperController scraperController = new ScraperController(project, AUTHOR, projectCreationDate);
        scraperController.getProjectDataFromDb(lastRelease);
        MeasureController measureController = new MeasureController(scraperController.getProject());
        for (String[] coldStartProject : COLD_START_PROJECTS) {
            LOGGER.info("Processing cold start data of project: {} until release {}", coldStartProject[0], coldStartProject[1]);
            ScraperController coldStartScraperController = new ScraperController(coldStartProject[0], AUTHOR, coldStartProject[2]);
            coldStartScraperController.getColdStartDataFromDb();
            measureController.addColdStartProportionProject(coldStartScraperController.getProject());
        }
        double coldStartProportion = measureController.computeColdStartProportion();
        LOGGER.info("Cold start proportion: {}", coldStartProportion);
        measureController.setColdStartProportion(coldStartProportion);
        measureController.createWalkForwardDatasets();
    }

    public static void scrapeDatasetData(String project, String projectCreationDate) throws ParseException {
        ScraperController scraperController1 = new ScraperController(project, AUTHOR, projectCreationDate);
        scraperController1.saveProjectDataOnDb();
       for (String[] coldStartProject : COLD_START_PROJECTS) {
            LOGGER.info("Scraping cold start data of project: {} until release {}", coldStartProject[0], coldStartProject[1]);
            ScraperController coldStartScraperController = new ScraperController(coldStartProject[0], AUTHOR, coldStartProject[2]);
            coldStartScraperController.saveColdStartDataOnDb(coldStartProject[1]);
        }
    }
}
