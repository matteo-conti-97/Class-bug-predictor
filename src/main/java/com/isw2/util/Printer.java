package com.isw2.util;

import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Printer {
    private final static Logger LOGGER = LoggerFactory.getLogger(Printer.class);
    private Printer(){}

    public static void printTicketsDetailed(List<Ticket> tickets) {
        int ticketSize = tickets.size();
        LOGGER.debug("\n{} tickets:", ticketSize);
        for (Ticket tmp : tickets) {
            String stringa = tmp.getKey() + " Resolution Date: " + tmp.getResolutionDate() + " Fix Version: " + tmp.getFv().getName() + " Num: " + tmp.getFv().getNumberStr() + " Creation Date: " + tmp.getCreationDate() + " Opening Version:" + tmp.getOv().getName() + " Num: " + tmp.getOv().getNumberStr();
            LOGGER.info(stringa);
            LOGGER.info("\tAffected Versions: ");
            for (Release av : tmp.getJiraAv()) {
                String avName = av.getName();
                LOGGER.debug("\t\tVersion: {}", avName);
            }
        }
    }

    public static void printTicketsBasic(List<Ticket> tickets){
        LOGGER.debug("\n" + tickets.size() + " tickets:");
        for (Ticket ticket : tickets) {
            String stringa=ticket.getKey() + " Resolution Date: " + ticket.getResolutionDate()+ " Creation Date: " + ticket.getCreationDate();
            LOGGER.info(stringa);
        }
    }

    public static void printProjectInfo(Project project) {
        String projectName = project.getName();
        String projectCreationDate = project.getCreationDate();
        LOGGER.debug("Project: {}", projectName);
        LOGGER.debug("Creation date: {}", projectCreationDate);
    }

    public static void printReleasesDetailed(List<Release> releases){
        LOGGER.info("Releases of interest: ");
        for (Release release : releases) {
            String releaseName = release.getName();
            String releaseNumber = release.getNumberStr();
            String releaseStartDate = release.getStartDate();
            String releaseEndDate = release.getEndDate();
            int releaseNumCommits = release.getCommits().size();
            int releaseNumFiles = release.getFileTreeAtReleaseEnd().size();
            LOGGER.debug("Release: {} number {} has {} commits and {} non test java files, starts at {} and ends at {}", releaseName, releaseNumber, releaseNumCommits, releaseNumFiles, releaseStartDate, releaseEndDate);
        }
    }

    public static void printReleasesBasic(List<Release> releases){
        LOGGER.info("Releases of interest: ");
        for (Release release : releases) {
            String releaseName = release.getName();
            String releaseNumber = release.getNumberStr();
            String releaseStartDate = release.getStartDate();
            String releaseEndDate = release.getEndDate();
            LOGGER.debug("Release: {} number {} starts at {} and ends at {}", releaseName, releaseNumber, releaseStartDate, releaseEndDate);
        }
    }
}
