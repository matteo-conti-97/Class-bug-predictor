package com.isw2;

public class Main {
    public static void main(String[] args) {
        GitFilter gitFilter = new GitFilter(Constants.REPO_PATH);
        System.out.println(gitFilter.filterLog(Constants.FILTER));
    }
}
