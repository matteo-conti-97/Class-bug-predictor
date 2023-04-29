package com.isw2.control;

import com.isw2.dao.JiraDao;
import com.isw2.entity.Release;

import java.util.List;

public class ReleaseController {
    private JiraDao jiraDao;
    private List<Release> relOfInterest;

    public ReleaseController(JiraDao jiraDao) {
        this.jiraDao = jiraDao;
    }

    //Get all releases until the specified release, lastRel must be the name of the jira format e.g 4.4.0
    public void retrieveReleasesOfInterest(String lastRel) {
        relOfInterest = jiraDao.getReleaseUntil(lastRel);
    }

    public List<Release> getRelOfInterest() {
        return relOfInterest;
    }

    public void setRelOfInterest(List<Release> relOfInterest) {
        this.relOfInterest = relOfInterest;
    }

}
