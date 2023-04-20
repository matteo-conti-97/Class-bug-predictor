package com.isw2;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;

public class Main {

    public static void main(String[] args) {
        //JiraDao jiraDao = new JiraDao("BOOKKEEPER");
        //System.out.println(jiraDao.getFixedBugTickets(0));
        GitDao gitDao=new GitDao("bookkeeper","apache");
        gitDao.getAllCommits();
    }
}
