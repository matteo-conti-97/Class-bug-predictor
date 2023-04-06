package com.isw2;

public class Main {
    public static void main(String[] args) {
        String repoPath = "C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement";
        String filter = "#";
        GitFilter gitFilter = new GitFilter(repoPath);
        System.out.println(gitFilter.filterLog(filter));
    }
}
