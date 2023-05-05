package com.isw2.entity;

import java.util.List;

public class Commit {
    private String commitSha;
    private String commitMessage;
    private String commitDate;
    private String commitUrl;
    private String ticketId;
    private String author;
    private List<File> files;

    public Commit(String commitSha, String commitMessage, String commitDate, String commitUrl, String author, List <File> files) {
        this.commitSha = commitSha;
        this.commitMessage = commitMessage;
        this.commitDate = commitDate;
        this.commitUrl = commitUrl;
        this.author = author;
        this.files=files;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
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

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}
