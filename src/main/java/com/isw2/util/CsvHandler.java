package com.isw2.util;

import com.isw2.entity.JavaFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvHandler {

    private static final String OUTPUT_PATH = "src/main/java/resource/csv/dataset_";

    private CsvHandler() {
    }

    //takes a list of lists which containt all the filenames of a release for each release
    public static void writeDataLineByLine(List<List<JavaFile>> files, int numReleases, String projectName) {
        // first create file object for file placed at location
        // specified by filepath
        String filePath = OUTPUT_PATH + projectName + "_"+numReleases + ".csv";
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter

            FileWriter outputfile = new FileWriter(file, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = {"Release", "File", "# Authors in Release", "LOC", "Avg Churn in Release", "Avg Churn from Start", "Avg LOC Added in Release", "Avg LOC Added from Start", "# Revision in Release", "# Revision from Start", "# Bug Fix in Release", "# Bug Fix from Start", "Buggy"};
            writer.writeNext(header);
            for (int i = 0; i < files.size(); i++) {
                for (int j = 0; j < files.get(i).size(); j++) {
                    String filename = files.get(i).get(j).getName();
                    String nAuthorsInRel = files.get(i).get(j).getnAuthorInRelease();
                    String locInRel = files.get(i).get(j).getLocAtEndRelease();
                    String avgChurnInRel = files.get(i).get(j).getAvgChurnInRelease();
                    String nRevInRel = files.get(i).get(j).getnRevInRelease();
                    String avgAddInRel = files.get(i).get(j).getAvgLocAddedInRelease();
                    String avgAddFromStart = files.get(i).get(j).getAvgLocAddedFromStart();
                    String avgChurnFromStart = files.get(i).get(j).getAvgChurnFromStart();
                    String nRevFromStart = files.get(i).get(j).getnRevFromStart();
                    String nBugFixInRel = files.get(i).get(j).getnFixCommitInRelease();
                    String nBugFixFromStart = files.get(i).get(j).getnFixCommitFromStart();
                    String buggy= files.get(i).get(j).getBuggy();
                    String[] data = {Integer.toString(i + 1), filename, nAuthorsInRel, locInRel, avgChurnInRel, avgChurnFromStart, avgAddInRel, avgAddFromStart, nRevInRel, nRevFromStart, nBugFixInRel, nBugFixFromStart, buggy};
                    writer.writeNext(data);
                }
            }
            // closing writer connection
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String[]> readCsv(String path) {
        List<String[]> ret = new ArrayList<>();
        try {
            // Create an object of file reader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader(path);

            // create csvReader object and skip first Line
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); //Skip title line
            ret = csvReader.readAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void convertDataset(int numReleases, String projectName){
        for(int i=1;i<=numReleases;i++){
            csvToArff(i,projectName);
        }
    }

    private static List<String> stripCsvLine(String[] line){
        List<String> ret=new ArrayList<>();
        for(int i=0;i<line.length;i++){
            if(i==1) continue;
            ret.add(line[i]);
        }
        return ret;
    }

    private static void csvToArff(int release, String projectName){
        String csvFilePath = OUTPUT_PATH +projectName+"_"+ release + ".csv";
        String arffFilePath = OUTPUT_PATH+projectName+"_"+ release +  ".arff";
        List<String[]> csvData = readCsv(csvFilePath);
        File file = new File(arffFilePath);

        FileWriter outputfile = null;
        try {
            outputfile = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(outputfile);
            writeHeader(bufferedWriter,"dataset"+release);
            for(String[] line:csvData){
                List<String> tmp=stripCsvLine(line);
                for(int i=0;i<tmp.size()-1;i++){
                    bufferedWriter.write(Integer.parseInt(tmp.get(i))+",");
                }
                bufferedWriter.write(tmp.get(tmp.size()-1));
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeader(BufferedWriter bufferedWriter, String title) throws IOException {
        String[] header = {"@relation "+title,"@attribute Release numeric", "@attribute #AuthorsInRelease numeric", "@attribute LOC numeric",
                "@attribute AvgChurnInRelease numeric", "@attribute AvgChurnFromStart numeric",
                "@attribute AvgLOCAddedInRelease numeric", "@attribute AvgLOCAddedFromStart numeric",
                "@attribute #RevisionInRelease numeric", "@attribute #RevisionFromStart numeric",
                "@attribute #BugFixInRelease numeric", "@attribute #BugFixFromStart numeric", "@attribute Buggy {1,0}", "@data"};
        for (String line : header) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
    }
}
