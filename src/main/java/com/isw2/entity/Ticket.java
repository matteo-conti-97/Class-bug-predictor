package com.isw2.entity;

public class Ticket {

    private String ticketId;
    private String description;
    private String type;
    private String affectedVersion;
    private String fixDate;
    private String creationDate;
    private String versionUrl;
    private String ticketUrl;

    public Ticket(String... args) {
        this.ticketId = args[0];
        this.description = args[0];
        this.type = args[0];
        this.affectedVersion = args[0];
        this.fixDate = args[0];
        this.creationDate = args[0];
        this.versionUrl = args[0];
        this.ticketUrl = args[0];
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

}
