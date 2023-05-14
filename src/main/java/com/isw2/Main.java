package com.isw2;

import com.isw2.control.MeasureController;
import com.isw2.control.ScraperController;
import java.text.ParseException;


public class Main {

    public static void main(String[] args) throws ParseException {
        ScraperController scraperController = new ScraperController("bookkeeper", "apache");
        scraperController.getProjectDataFromDb();

        MeasureController measureController = new MeasureController(scraperController.getProject());
        //measureController.createWalkForwardDatasets();


        /*List<Ticket> allTickets = scraperController.getAllTickets();
        scraperController.setProjectFixedBugTickets(allTickets);
        List<Ticket> ticketOfInterest = scraperController.getTicketsOfInterest(lastInterestReleaseEndDate);
        scraperController.setProjectFixedBugTicketsOfInterest(ticketOfInterest);

        System.out.println("\n" + ticketOfInterest.size() + " tickets of interest:");
        for (Ticket tmp : ticketOfInterest) {
            System.out.println(tmp.getKey() + " ResDate: " + tmp.getResolutionDate());
        }

        //logger.fine(jiraDao.getFixedBugTickets(0));
        //gitDao.getAllCommits();*/
    }
}
