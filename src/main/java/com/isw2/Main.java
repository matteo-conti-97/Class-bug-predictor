package com.isw2;

import com.isw2.control.ScraperController;
import com.isw2.entity.Commit;
import com.isw2.entity.Release;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws SQLException, ParseException {
        ScraperController scraperController = new ScraperController("bookkeeper", "apache");

        scraperController.setProjectCreationDate();
        List<Release> allReleases = scraperController.getAllReleases();
        scraperController.setProjectReleases(allReleases);
        List<Release> releasesOfInterest = scraperController.getReleasesOfInterest("4.4.0");
        scraperController.setProjectReleasesOfInterest(releasesOfInterest);

        String lastInterestReleaseEndDate = scraperController.getLastReleaseEndDateOfInterest();
        System.out.println("Last interest release end date: " + lastInterestReleaseEndDate);

        System.out.println("Project: " + scraperController.getProjectName());
        System.out.println("Creation date: " + scraperController.getProjectCreationDate());
        System.out.println("Last interest release end date: " + lastInterestReleaseEndDate);
        //scraperController.createAllCommitsJsonUntilDb(lastInterestReleaseEndDate,"commitDb");
        List<Commit> commits = scraperController.getCommitsFromDb("commitDb");
        scraperController.setProjectCommits(commits);
        scraperController.linkCommitsToReleases();
        for (Release release : scraperController.getProjectReleasesOfInterest()) {
            System.out.println("Release: " + release.getName() + " number " + release.getNumber() + " has " + release.getCommits().size() + " commits and starts at " + release.getStartDate() + " and ends at " + release.getEndDate());
        }

        scraperController.createWalkForwardDatasets();


        /*System.out.println("\n" + scraperController.getProjectReleases().size() + " releases:");
        for (Release tmp : allReleases) {
            System.out.println(tmp.getName() + " " + tmp.getNumber() + " " + tmp.getReleaseDate() + " " + tmp.getEndDate());
        }

        System.out.println("\n" + releasesOfInterest.size() + " interest releases:");
        for (Release tmp : releasesOfInterest) {
            System.out.println(tmp.getName() + " " + tmp.getNumber() + " " + tmp.getReleaseDate() + " " + tmp.getEndDate());
        }

        List<Ticket> allTickets = scraperController.getAllTickets();
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
