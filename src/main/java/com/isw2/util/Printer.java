package com.isw2.util;

import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.entity.Ticket;

import java.util.List;

public class Printer {

    private Printer(){}

    public static void printTicketsDetailed(List<Ticket> tickets){
        System.out.println("\n" + tickets.size() + " tickets:");
        for (Ticket tmp : tickets) {
            String stringa=tmp.getKey() + " Resolution Date: " + tmp.getResolutionDate()+ " Fix Version: " + tmp.getFv().getName() +" Num: " +tmp.getFv().getNumberStr()+ " Creation Date: " + tmp.getCreationDate() + " Opening Version:" + tmp.getOv().getName() +" Num: " +tmp.getOv().getNumberStr();
            System.out.println(stringa);
            System.out.println("\tAffected Versions: ");
            for(Release av: tmp.getJiraAv()){
                System.out.println("\t\tVersion: "+av.getName());
            }
        }
    }

    public static void printTicketsBasic(List<Ticket> tickets){
        System.out.println("\n" + tickets.size() + " tickets:");
        for (Ticket ticket : tickets) {
            String stringa=ticket.getKey() + " Resolution Date: " + ticket.getResolutionDate()+ " Creation Date: " + ticket.getCreationDate();
            System.out.println(stringa);
        }
    }

    public static void printProjectInfo(Project project){
        System.out.println("Project: " + project.getName());
        System.out.println("Creation date: " + project.getCreationDate());
    }

    public static void printReleasesDetailed(List<Release> releases){
        System.out.println("Releases of interest: ");
        for (Release release : releases) {
            System.out.println("Release: " + release.getName() + " number " + release.getNumberStr() + " has " + release.getCommits().size() + " commits and " + release.getFileTreeAtReleaseEnd().size() + " non test java files, starts at " + release.getStartDate() + " and ends at " + release.getEndDate());
        }
    }

    public static void printReleasesBasic(List<Release> releases){
        System.out.println("Releases of interest: ");
        for (Release release : releases) {
            System.out.println("Release: " + release.getName() + " number " + release.getNumberStr() + " starts at " + release.getStartDate() + " and ends at " + release.getEndDate());
        }
    }
}
