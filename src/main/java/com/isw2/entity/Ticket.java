package com.isw2.entity;

import java.util.List;

public class Ticket {

    private String ticketId;
    private String description;
    private String type;
    private String affectedVersion;
    private String fixDate;
    private String creationDate;
    private String versionUrl;
    private String ticketUrl;
    private List<Commit> gitCommits;

    public Ticket() {

    }

    public Ticket(String... args) {
        this.ticketId = args[0];
        this.description = args[1];
        this.type = args[2];
        this.affectedVersion = args[3];
        this.fixDate = args[4];
        this.creationDate = args[5];
        this.versionUrl = args[6];
        this.ticketUrl = args[7];
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAffectedVersion() {
        return affectedVersion;
    }

    public void setAffectedVersion(String affectedVersion) {
        this.affectedVersion = affectedVersion;
    }

    public String getFixDate() {
        return fixDate;
    }

    public void setFixDate(String fixDate) {
        this.fixDate = fixDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }

    public List<Commit> getGitCommits() { return gitCommits; }

    public void setGitCommits(List<Commit> gitCommits) { this.gitCommits = gitCommits; }
}
