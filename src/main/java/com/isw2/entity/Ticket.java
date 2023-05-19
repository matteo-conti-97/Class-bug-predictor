package com.isw2.entity;

import java.util.List;

public class Ticket {
    private String ticketId;
    private String key;
    private String ticketUrl;
    private String creationDate;
    private String resolutionDate;
    private String ov;
    private String ovNum;
    private String fv; //Not the jira fix version, but the git fix commit using SZZ approach
    private String fvNum;
    private List<String> jiraAv;
    private List<String> proportionAv; //Not always jira is trustable
    private List<Commit> gitCommits;

    public Ticket(String key, String ticketId, String ticketUrl) {
        this.key = key;
        this.ticketId = ticketId;
        this.ticketUrl = ticketUrl;
    }

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

    public String getOv() {
        return ov;
    }

    public void setOv(String ov) {
        this.ov = ov;
    }

    public String getFv() {
        return fv;
    }

    public void setFv(String fv) {
        this.fv = fv;
    }

    public List<String> getJiraAv() {
        return jiraAv;
    }

    public void setJiraAv(List<String> jiraAv) {
        this.jiraAv = jiraAv;
    }

    public List<String> getProportionAv() {
        return proportionAv;
    }

    public void setProportionAv(List<String> proportionAv) {
        this.proportionAv = proportionAv;
    }

    public List<Commit> getGitCommits() {
        return gitCommits;
    }

    public void setGitCommits(List<Commit> gitCommits) {
        this.gitCommits = gitCommits;
    }

    public String getOvNum() {
        return ovNum;
    }

    public void setOvNum(String ovNum) {
        this.ovNum = ovNum;
    }

    public String getFvNum() {
        return fvNum;
    }

    public void setFvNum(String fvNum) {
        this.fvNum = fvNum;
    }
}
