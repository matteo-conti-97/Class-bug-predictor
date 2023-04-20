package com.isw2.entity;

import java.util.List;

public class Release {
    private String name;
    private int number;
    private String releaseDate;
    private String endDate;
    private List<Commit> commits;
    private List<Ticket> tickets;

    public Release(String name, int number , String releaseDate) {
        this.name = name;
        this.number = number;
        this.releaseDate = releaseDate;
    }

    public Release(String name, int number , String releaseDate, String endDate){
        this.name = name;
        this.number = number;
        this.releaseDate = releaseDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return Integer.toString(number);
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
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
}
