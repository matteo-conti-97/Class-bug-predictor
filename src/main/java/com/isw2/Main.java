package com.isw2;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.Project;
import com.isw2.entity.Release;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("MainLogger");
        Project project=new Project("bookkeeper", "apache");
        GitDao gitDao=new GitDao(project.getName(), project.getAuthor());
        JiraDao jiraDao=new JiraDao(project.getName());
        project.setCreationDate(gitDao.getProjectCreationDate());
        project.setReleases(jiraDao.getReleaseUntil(""));
        project.setInterestReleases(jiraDao.getReleaseUntil("4.4.0"));
        logger.fine("Project: "+project.getName());
        logger.fine("Creation date: "+project.getCreationDate());
        logger.fine(project.getReleases().size()+" releases:");
        for(int i=0;i<project.getReleases().size();i++) {
            Release tmp = project.getReleases().get(i);
            logger.fine(tmp.getName()+" "+tmp.getNumber()+" "+tmp.getReleaseDate()+" "+tmp.getEndDate());
        }
        logger.fine(project.getInterestReleases().size()+" interest releases:");
        for(int i=0;i<project.getInterestReleases().size();i++) {
            Release tmp = project.getReleases().get(i);
            logger.fine(tmp.getName()+" "+tmp.getNumber()+" "+tmp.getReleaseDate()+" "+tmp.getEndDate());
        }
        //logger.fine(jiraDao.getFixedBugTickets(0));
        //gitDao.getAllCommits();
    }
}
