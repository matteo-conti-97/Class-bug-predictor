package com.isw2.util;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvHandler {

    private CsvHandler(){}

    //takes a list of lists which containt all the filenames of a release for each release
    //TODO modificare la lista di liste di stringhe in lista di liste di file
    public static void writeDataLineByLine(List<List<String>> files, int numReleases) {
        // first create file object for file placed at location
        // specified by filepath
        String filePath= "src/main/java/resource/csv/dataset" + numReleases + ".csv";
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter

            FileWriter outputfile = new FileWriter(file, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "Release", "File", "Feature 1", "Feature 2", "Feature 3", "Feature 4", "Feature 5", "Feature 6", "Feature 7", "Feature 8", "Feature 9", "Feature 10", "Buggy"};
            writer.writeNext(header);
            for(int i=0; i<files.size();i++){
                for (int j=0; j<files.get(i).size();j++){
                    String filename = files.get(i).get(j);
                    String[] data = {Integer.toString(i+1), filename, "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"};
                    writer.writeNext(data);
                }
            }

            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
