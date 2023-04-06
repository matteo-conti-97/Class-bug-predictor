package com.isw2;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        String REPO_PATH = "C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement";
        String FILTER = "#";
        GitFilter gitFilter = new GitFilter(REPO_PATH);
        System.out.println(gitFilter.filterLog(FILTER));
    }
}
