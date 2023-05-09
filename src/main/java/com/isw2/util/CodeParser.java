package com.isw2.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import java.util.List;

public class CodeParser {
    static final String cmd="cmd /c start C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\cloc\\cloc.exe C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\cloc\\tmp\\inFile --lang-no-ext=Java --csv --out=C:\\Users\\matte\\Documents\\GitHub\\isw2-jira-git-measurement\\src\\main\\java\\resource\\cloc\\tmp\\out.csv";
    static final String inPath="./src/main/java/resource/cloc/tmp/inFile";


    public static List<String> getCodeStats(String fileContent) throws IOException {
        List<String> ret=new ArrayList<>();
        String decFileContent=base64Decode(fileContent);
        System.out.println(decFileContent);
        writeOnFile(decFileContent);
        Runtime.getRuntime().exec(cmd);
        List<String[]> csvRead=CsvHandler.readCsv("./src/main/java/resource/cloc/tmp/out.csv");
        ret.add(csvRead.get(0)[2]); //Blank line count
        ret.add(csvRead.get(0)[3]); //Comment line count
        ret.add(csvRead.get(0)[4]); //Code line count
        return ret;
    }

    private static String base64Decode(String encodedString) {
        return new String(Base64.decodeBase64(encodedString), StandardCharsets.UTF_8);
    }

    private static void writeOnFile(String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(inPath, false));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
