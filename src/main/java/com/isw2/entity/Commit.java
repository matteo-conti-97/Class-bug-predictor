package com.isw2.entity;

public class Commit {
    private String commitId;
    private String commitMessage;
    private String commitDate;
    private String commitUrl;
    private String ticketId;
    private String author;

    public Commit(String commitId, String commitMessage, String commitDate, String commitUrl, String ticketId, String author) {
        this.commitId = commitId;
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
        this.commitUrl = commitUrl;
        this.ticketId = ticketId;
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getCommitUrl() {
        return commitUrl;
    }

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
}
