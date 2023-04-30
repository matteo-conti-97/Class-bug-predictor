package com.isw2.control;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.Project;
import com.isw2.entity.Release;

import java.util.ArrayList;
import java.util.List;

public class ReleaseController {
    private Project project;
    private JiraDao jiraDao;
    private GitDao gitDao;
    private List<Release> releases;

    public ReleaseController(Project project) {
        this.project = project;
        this.jiraDao = new JiraDao(project.getName());
        this.gitDao = new GitDao(project.getName(), project.getAuthor());
    }

    //Get all releases until the specified release, lastRel must be the name of the jira format e.g 4.4.0
    public List<Release> getReleasesOfInterest(String lastRel) {
        List<Release> ret = new ArrayList<>();
        for (Release release : releases) {
            ret.add(release);
            if (release.getName().equals(lastRel)) {
                break;
            }
        }
        return ret;
    }

    public List<Release> getReleases() {
        this.releases = jiraDao.getReleases();
        return releases;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public JiraDao getJiraDao() {
        return jiraDao;
    }

    public void setJiraDao(JiraDao jiraDao) {
        this.jiraDao = jiraDao;
    }

    public GitDao getGitDao() {
        return gitDao;
    }

    public void setGitDao(GitDao gitDao) {
        this.gitDao = gitDao;
    }

    public void setReleases(List<Release> releases) {
        this.releases = releases;
    }
}
