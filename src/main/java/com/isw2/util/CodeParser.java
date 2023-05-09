package com.isw2.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeParser {
    public static List<String> getCodeStats(String filePath) throws IOException {
        List<String> ret = new ArrayList<>();
        Process runtimeProcess = Runtime.getRuntime().exec("cmd /c start C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\com\\isw2\\util\\cloc.exe C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\com\\isw2\\util\\CsvHandler.java --csv --out=C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\tmp\\cloc.csv");
        return ret;
    }
}
