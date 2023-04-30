package com.isw2;

import com.isw2.control.ProjectController;
import com.isw2.control.ReleaseController;
import com.isw2.control.TicketController;
import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.entity.Ticket;

import java.text.ParseException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws ParseException {
        //Project project = new Project("bookkeeper", "apache");
        ProjectController projectController = new ProjectController("bookkeeper", "apache");
        ReleaseController releaseController = new ReleaseController(projectController.getProject());
        List<Release> allReleases=releaseController.getReleases();
        List<Release> releasesOfInterest=releaseController.getReleasesOfInterest("4.4.0");

        projectController.setProjectCreationDate();
        projectController.setProjectReleases(releaseController.getReleases());
        projectController.setProjectInterestReleases(releaseController.getReleasesOfInterest("4.4.0"));
        String lastInsterestReleaseEndDate=projectController.getProjectInterestReleases().get(projectController.getProjectInterestReleases().size()-1).getEndDate();
        System.out.println("Last interest release end date: " + lastInsterestReleaseEndDate);

        System.out.println("Project: " + projectController.getProjectName());
        System.out.println("Creation date: " + projectController.getProjectCreationDate());
        System.out.println("\n" + projectController.getProjectReleases().size() + " releases:");
        for (int i = 0; i < projectController.getProjectReleases().size(); i++) {
            Release tmp = projectController.getProjectReleases().get(i);
            System.out.println(tmp.getName() + " " + tmp.getNumber() + " " + tmp.getReleaseDate() + " " + tmp.getEndDate());
        }
        System.out.println("\n" + projectController.getProjectInterestReleases().size() + " interest releases:");
        for (int i = 0; i < projectController.getProjectInterestReleases().size(); i++) {
            Release tmp = projectController.getProjectInterestReleases().get(i);
            System.out.println(tmp.getName() + " " + tmp.getNumber() + " " + tmp.getReleaseDate() + " " + tmp.getEndDate());
        }

        TicketController ticketController = new TicketController(projectController.getProjectName());
        List<Ticket> ticketOfInteres=ticketController.getAllTicketsUntilRelEndDate(lastInsterestReleaseEndDate);
        System.out.println("\n" + ticketOfInteres.size() + " tickets of interest:");
        for(Ticket tmp:ticketOfInteres){
            System.out.println(tmp.getKey() + " ResDate: "  + tmp.getResolutionDate());
        }

        //logger.fine(jiraDao.getFixedBugTickets(0));
        //gitDao.getAllCommits();
    }
}
