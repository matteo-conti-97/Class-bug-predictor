package com.isw2;

public class Main {

    public static final String GITHUB_PATH = "C:\\Users\\matte\\Documents\\GitHub\\";
    public static final String FILTER = "Removed";

    public static void main(String[] args) {
        GitFilter gitFilter = new GitFilter(GITHUB_PATH+"isw2_bookkeeper");
        System.out.println(gitFilter.filterLog(FILTER));
    }
}
