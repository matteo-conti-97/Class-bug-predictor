package com.isw2.util;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CodeParser {
    static final String CMD = "cmd /c start C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\cloc\\cloc.exe C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\cloc\\tmp\\inFile --lang-no-ext=Java --csv --out=C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\cloc\\tmp\\out.csv";
    static final String IN_PATH = "./src/main/java/resource/cloc/tmp/inFile";

    private CodeParser() {
    }

    public static String getCodeLineCount(String fileContent) throws IOException {
        String decFileContent = base64Decode(fileContent);
        writeOnFile(decFileContent);
        Runtime.getRuntime().exec(CMD);
        List<String[]> csvRead = CsvHandler.readCsv("./src/main/java/resource/cloc/tmp/out.csv");
        return csvRead.get(0)[4]; //Code line count
    }

    public static String getBlankLineCount(String fileContent) throws IOException {
        String decFileContent = base64Decode(fileContent);
        writeOnFile(decFileContent);
        Runtime.getRuntime().exec(CMD);
        List<String[]> csvRead = CsvHandler.readCsv("./src/main/java/resource/cloc/tmp/out.csv");
        return csvRead.get(0)[2]; //Code line count
    }

    public static String getCommentLineCount(String fileContent) throws IOException {
        String decFileContent = base64Decode(fileContent);
        writeOnFile(decFileContent);
        Runtime.getRuntime().exec(CMD);
        List<String[]> csvRead = CsvHandler.readCsv("./src/main/java/resource/cloc/tmp/out.csv");
        return csvRead.get(0)[3]; //Code line count
    }

    private static String base64Decode(String encodedString) {
        return new String(Base64.decodeBase64(encodedString), StandardCharsets.UTF_8);
    }

    private static void writeOnFile(String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(IN_PATH, false))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
