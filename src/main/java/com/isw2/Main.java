package com.isw2;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.entity.Ticket;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Project project=new Project("bookkeeper", "apache");
        GitDao gitDao=new GitDao(project.getName(), project.getAuthor());
        JiraDao jiraDao=new JiraDao(project.getName());
        project.setCreationDate(gitDao.getProjectCreationDate());
        project.setReleases(jiraDao.getReleaseUntil(""));
        project.setInterestReleases(jiraDao.getReleaseUntil("4.4.0"));
        System.out.println("Project: "+project.getName());
        System.out.println("Creation date: "+project.getCreationDate());
        System.out.println(project.getReleases().size()+" releases:");
        for(int i=0;i<project.getReleases().size();i++) {
            Release tmp = project.getReleases().get(i);
            System.out.println(tmp.getName()+" "+tmp.getNumber()+" "+tmp.getReleaseDate()+" "+tmp.getEndDate());
        }
        System.out.println(project.getInterestReleases().size()+" interest releases:");
        for(int i=0;i<project.getInterestReleases().size();i++) {
            Release tmp = project.getReleases().get(i);
            System.out.println(tmp.getName()+" "+tmp.getNumber()+" "+tmp.getReleaseDate()+" "+tmp.getEndDate());
        }
        List<Ticket> tickets= jiraDao.getFixedBugTickets(0);
        System.out.println(tickets.size()+" tickets:");
        for (Ticket tmp : tickets) {
            System.out.println(tmp.getKey());
        }
        //logger.fine(jiraDao.getFixedBugTickets(0));
        //gitDao.getAllCommits();
    }
}
