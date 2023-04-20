package com.isw2;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.Project;
import com.isw2.entity.Release;

import java.text.MessageFormat;
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
        logger.fine(MessageFormat.format("Project: {}", project.getName()));
        logger.fine(MessageFormat.format("Creation date: {}",project.getCreationDate()));
        logger.fine(MessageFormat.format("There are {} releases:",project.getReleases().size()));
        for(int i=0;i<project.getReleases().size();i++) {
            Release tmp = project.getReleases().get(i);
            logger.fine(tmp.getName()+" "+tmp.getNumber()+" "+tmp.getReleaseDate()+" "+tmp.getEndDate());
        }
        logger.fine(MessageFormat.format("There are {} interest releases:", project.getInterestReleases().size()));
        for(int i=0;i<project.getInterestReleases().size();i++) {
            Release tmp = project.getReleases().get(i);
            logger.fine(MessageFormat.format("Release: {0} with ID: {1} released at: {2} and ended at: {3}", tmp.getName(), tmp.getNumber(), tmp.getReleaseDate(), tmp.getEndDate()));
        }
        //logger.fine(jiraDao.getFixedBugTickets(0));
        //gitDao.getAllCommits();
    }
}
