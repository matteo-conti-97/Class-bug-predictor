package com.isw2;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import java.util.List;

public class Main {

    public static final String GITHUB_PATH = "C:\\Users\\matte\\Documents\\GitHub\\";
    public static final String FILTER = "Removed";

    public static void main(String[] args) {
        GitDao gitDao = new GitDao(GITHUB_PATH+"isw2_bookkeeper");
        List<String> commits = gitDao.getAllCommits();
        //System.out.println(gitDao.filterByTicket(commits, "#1105"));
        JiraDao jiraDao = new JiraDao("BOOKKEEPER");
        //jiraDao.getFixedBugTickets(0);
    }
}
