package com.isw2.control;

import com.isw2.dao.GitDao;
import com.isw2.dao.JiraDao;
import com.isw2.entity.Commit;
import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.entity.Ticket;

import java.util.List;

public class ProjectController {
    private Project project;
    private JiraDao jiraDao;
    private GitDao gitDao;

    public ProjectController(String projectName, String projectAuthor) {
        this.project = new Project("bookkeeper", "apache");
        this.jiraDao = new JiraDao(project.getName());
        this.gitDao = new GitDao(project.getName(), project.getAuthor());
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getProjectAuthor() {
        return project.getAuthor();
    }



    public String getProjectCreationDate() {
        return project.getCreationDate();
    }

    public void setProjectCreationDate() {
        this.project.setCreationDate(gitDao.getProjectCreationDate());
    }
    public void setProjectCreationDate(String creationDate) {
        this.project.setCreationDate(creationDate);
    }

    public List<Commit> getProjectCommits() {
        return project.getCommits();
    }

    public void setProjectCommits(List<Commit> commits) {
        this.project.setCommits(commits);
    }

    public List<Ticket> getProjectTickets() {
        return project.getTickets();
    }

    public void setProjectTickets(List<Ticket> tickets) {
        this.project.setTickets(tickets);
    }

    public List<Release> getProjectReleases() {
        return project.getReleases();
    }

    public void setProjectReleases(List<Release> releases) {
        this.project.setReleases(releases);
    }

    public List<Release> getProjectInterestReleases() {
        return project.getInterestReleases();
    }

    public void setProjectInterestReleases(List<Release> interestReleases) {
        this.project.setInterestReleases(interestReleases);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
