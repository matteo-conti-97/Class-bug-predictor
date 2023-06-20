package com.isw2.entity;

import java.util.List;

public class Ticket {
    private String ticketId;
    private String key;
    private String ticketUrl;
    private String creationDate;
    private String resolutionDate;
    private int iv;
    private Release ov;
    private Release fv; //Not the jira fix version, but the git fix commit using SZZ approach
    private List<Release> jiraAv;
    private List<Release> proportionAv; //Not always jira is trustable
    private List<Commit> gitCommits;

    public Ticket(String key, String ticketId, String ticketUrl) {
        this.key = key;
        this.ticketId = ticketId;
        this.ticketUrl = ticketUrl;
        this.iv=-1;
    }


    public Ticket(String key) {
        this.key = key;
    }

    public int getIv(){return iv;}

    public void setIv(int iv){this.iv = iv;}

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTicketUrl() {
        return ticketUrl;
    }

    public void setTicketUrl(String ticketUrl) {
        this.ticketUrl = ticketUrl;
    }


    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(String resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public Release getOv() {
        return ov;
    }

    public void setOv(Release ov) {
        this.ov = ov;
    }

    public Release getFv() {
        return fv;
    }

    public void setFv(Release fv) {
        this.fv = fv;
    }

    public List<Release> getJiraAv() {
        return jiraAv;
    }

    public void setJiraAv(List<Release> jiraAv) {
        this.jiraAv = jiraAv;
    }

    public List<Release> getProportionAv() {
        return proportionAv;
    }

    public void setProportionAv(List<Release> proportionAv) {
        this.proportionAv = proportionAv;
    }

    public List<Commit> getGitCommits() {
        return gitCommits;
    }

    public void setGitCommits(List<Commit> gitCommits) {
        this.gitCommits = gitCommits;
    }
}

