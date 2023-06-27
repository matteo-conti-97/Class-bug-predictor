package com.isw2.util;

import com.isw2.model.JavaFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvHandler {

    private static final String CSV_OUTPUT_PATH = "src/main/java/resource/csv/dataset_";
    private static final String ARFF_OUTPUT_PATH = "src/main/java/resource/arff/dataset_";

    private CsvHandler() {
    }

    //takes a list of lists which containt all the filenames of a release for each release
    public static void writeDataLineByLine(List<List<JavaFile>> files, int numReleases, String projectName) {
        // first create file object for file placed at location
        // specified by filepath
        String trainFilePath = CSV_OUTPUT_PATH + projectName + "_"+numReleases + "Train.csv";
        String testFilePath = CSV_OUTPUT_PATH + projectName + "_"+numReleases + "Test.csv";
        File fileTrain = new File(trainFilePath);
        File fileTest = new File(testFilePath);
        try {
            // create FileWriter object with file as parameter

            FileWriter outputFileTrain = new FileWriter(fileTrain, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writerTrain = new CSVWriter(outputFileTrain);

            FileWriter outputFileTest = new FileWriter(fileTest, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writerTest = new CSVWriter(outputFileTest);

            // adding header to csv
            String[] header = {"Release", "File", "# Authors in Release", "LOC", "Avg Churn in Release", "Avg Churn from Start", "Avg LOC Added in Release", "Avg LOC Added from Start", "# Revision in Release", "# Revision from Start", "# Bug Fix in Release", "# Bug Fix from Start", "Buggy"};
            writerTrain.writeNext(header);
            writerTest.writeNext(header);
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
                    if(i==files.size()-1) writerTest.writeNext(data);
                    else writerTrain.writeNext(data);
                }
            }
            // closing writer connection
            writerTrain.close();
            writerTest.close();
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
        String csvTrainFilePath = CSV_OUTPUT_PATH +projectName+"_"+ release + "Train.csv";
        String csvTestFilePath = CSV_OUTPUT_PATH +projectName+"_"+ release + "Test.csv";
        String arffTrainFilePath = ARFF_OUTPUT_PATH+projectName+"_"+ release +  "Train.arff";
        String arffTestFilePath = ARFF_OUTPUT_PATH+projectName+"_"+ release +  "Test.arff";

        List<String[]> csvDataTrain = readCsv(csvTrainFilePath);
        List<String[]> csvDataTest = readCsv(csvTestFilePath);

        File fileTrain = new File(arffTrainFilePath);
        File fileTest = new File(arffTestFilePath);

        FileWriter outputfileTrain = null;
        FileWriter outputfileTest = null;
        try {
            outputfileTrain = new FileWriter(fileTrain, false);
            BufferedWriter bufferedWriterTrain = new BufferedWriter(outputfileTrain);
            writeHeader(bufferedWriterTrain,"dataset-"+release+"-training");
            writeCsvDataOnArff(bufferedWriterTrain, csvDataTrain);

            outputfileTest = new FileWriter(fileTest, false);
            BufferedWriter bufferedWriterTest = new BufferedWriter(outputfileTest);
            writeHeader(bufferedWriterTest,"dataset-"+release+"-testing");
            writeCsvDataOnArff(bufferedWriterTest, csvDataTest);

            bufferedWriterTrain.close();
            bufferedWriterTest.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCsvDataOnArff(BufferedWriter writer, List<String[]> csvData) throws IOException {
        for(String[] line:csvData){
            List<String> tmp=stripCsvLine(line);
            for(int i=0;i<tmp.size()-1;i++){
                writer.write(Integer.parseInt(tmp.get(i))+",");
            }
            writer.write(tmp.get(tmp.size()-1));
            writer.newLine();
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
