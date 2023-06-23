package com.isw2.entity;

import java.util.List;

public class Project {
    private String name;
    private String author;
    private String creationDate;
    private List<Commit> commits;
    private List<Ticket> fixedBugTickets;
    private List<Release> releases;
    private List<Release> releasesOfInterest;
    private List<Ticket> fixedBugTicketsOfInterest;


    public Project(String name, String author, String creationDate){
        this.name = name;
        this.author = author;
        this.creationDate = creationDate;
    }

    public void removeRelease(Release release) {
        releases.remove(release);
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public List<Ticket> getFixedBugTickets() {
        return fixedBugTickets;
    }

    public void setFixedBugTickets(List<Ticket> fixedBugTickets) {
        this.fixedBugTickets = fixedBugTickets;
    }

    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(List<Release> releases) {
        this.releases = releases;
    }

    public List<Release> getReleasesOfInterest() {
        return releasesOfInterest;
    }

    public void setReleasesOfInterest(List<Release> releasesOfInterest) {
        this.releasesOfInterest = releasesOfInterest;
    }

    public List<Ticket> getFixedBugTicketsOfInterest() {
        return fixedBugTicketsOfInterest;
    }

    public void setFixedBugTicketsOfInterest(List<Ticket> fixedBugTicketsOfInterest) {
        this.fixedBugTicketsOfInterest = fixedBugTicketsOfInterest;
    }
}
