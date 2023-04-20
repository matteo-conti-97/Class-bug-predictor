package com.isw2.entity;

import java.util.List;

public class Project {
    private String name;
    private String creationDate;
    private String lastInterestReleaseDate;
    private List<Commit> commits;
    private List<Ticket> tickets;
    private List<Release> releases;

    public String getName() {
        return name;
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

    public String getLastInterestReleaseDate() {
        return lastInterestReleaseDate;
    }

    public void setLastInterestReleaseDate(String lastInterestReleaseDate) {
        this.lastInterestReleaseDate = lastInterestReleaseDate;
    }

    public List<Commit> getCommits() {
        return commits;
    }

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(List<Release> releases) {
        this.releases = releases;
    }
}
