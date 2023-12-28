package com.isw2.util;

import com.isw2.model.JavaFile;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvHandler {
    private static final String[] OUT_HEADER={"PROJECT", "#TRAIN_RELEASES", "ADDONS", "CLASSIFIER",
            "KAPPA", "PRECISION", "RECALL", "AUC", "TP", "FP", "TN", "FN" };
    private static final String CSV_OUTPUT_PATH = "src/main/java/resource/csv/dataset_";
    private static final String ARFF_OUTPUT_PATH = "src/main/java/resource/arff/dataset_";
    private static final String OUTPUT_PATH = "src/main/java/resource/out/";
    private static final String NONE = "None";
    private static final String FEATURE_SELECTION_STRING = "Best_First_Bidirectional";

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvHandler.class);

    private CsvHandler() {
    }

    private static int digitMatcher(String in){
        String pattern = "\\d+";
        StringBuilder ret= new StringBuilder();

        Pattern digitPattern = Pattern.compile(pattern);
        Matcher matcher = digitPattern.matcher(in);

        while (matcher.find()) {
            String digit = matcher.group();
            ret.append(digit);
        }
        return Integer.parseInt(ret.toString());
    }

    public static void writeOutData(String project, String datasetPath, ExperimentType type, List<Double> nb,
                                    List<Double> ibk, List<Double> rf){
        String numRel;
        if(datasetPath.equals("mean")) numRel=datasetPath;
        else numRel= String.valueOf(digitMatcher(datasetPath)-1);
        String outPath= OUTPUT_PATH + project + "_" + numRel + "_" + type + ".csv";
        String experimentTuple = getExperimentTypeTuple(type);
        File file = new File(outPath);
        try {
            FileWriter outputFile = new FileWriter(file, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);
            String[] nbData = {project, numRel, experimentTuple,
            "Naive Bayes", String.valueOf(nb.get(0)), String.valueOf(nb.get(1)), String.valueOf(nb.get(2)),
                    String.valueOf(nb.get(3)), String.valueOf(nb.get(4)), String.valueOf(nb.get(5)),
                    String.valueOf(nb.get(6)), String.valueOf(nb.get(7))};
            String[] ibkData = {project, numRel, experimentTuple,
                    "IBK", String.valueOf(ibk.get(0)), String.valueOf(ibk.get(1)), String.valueOf(ibk.get(2)),
                    String.valueOf(ibk.get(3)), String.valueOf(ibk.get(4)), String.valueOf(ibk.get(5)),
                    String.valueOf(ibk.get(6)), String.valueOf(ibk.get(7))};
            String[] rfData = {project, numRel, experimentTuple,
                    "Random Forest", String.valueOf(rf.get(0)), String.valueOf(rf.get(1)), String.valueOf(rf.get(2)),
                    String.valueOf(rf.get(3)), String.valueOf(rf.get(4)), String.valueOf(rf.get(5)),
                    String.valueOf(rf.get(6)), String.valueOf(rf.get(7))};
            writer.writeNext(OUT_HEADER);
            writer.writeNext(nbData);
            writer.writeNext(ibkData);
            writer.writeNext(rfData);
            // closing writer connection
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getExperimentTypeTuple(ExperimentType type){
        String ret = null;
        switch(type){
            case VANILLA:
                ret = NONE;
                break;
            case FEATURE_SELECTION:
                ret = FEATURE_SELECTION_STRING;
                break;
            case FEATURE_SELECTION_WITH_OVER_SAMPLING:
                ret = FEATURE_SELECTION_STRING+"Over_Sampling";
                break;
            case FEATURE_SELECTION_WITH_UNDER_SAMPLING:
                ret = FEATURE_SELECTION_STRING+"Under_Sampling";
                break;
            case FEATURE_SELECTION_WITH_COST_SENSITIVE_CLASSIFIER:
                ret = FEATURE_SELECTION_STRING+"Sensitive_Classifier_Threshold_CFP_1_CFN_10";
                break;
            case FEATURE_SELECTION_WITH_COST_SENSITIVE_LEARNING:
                ret = FEATURE_SELECTION_STRING+"Sensitive_Learning_CFP_1_CFN_10";
                break;
            default:
                break;
        }
        return ret;
    }

    private static void writeReleaseDataLineByLine(List<JavaFile> releaseFiles, CSVWriter writer, int release){
        for (JavaFile releaseFile : releaseFiles) {
            String filename = releaseFile.getName();
            String nAuthorsInRel = releaseFile.getnAuthorInRelease();
            String locInRel = releaseFile.getLocAtEndRelease();
            String avgChurnInRel = releaseFile.getAvgChurnInRelease();
            String nRevInRel = releaseFile.getnRevInRelease();
            String avgAddInRel = releaseFile.getAvgLocAddedInRelease();
            String avgAddFromStart = releaseFile.getAvgLocAddedFromStart();
            String avgChurnFromStart = releaseFile.getAvgChurnFromStart();
            String nRevFromStart = releaseFile.getnRevFromStart();
            String nBugFixInRel = releaseFile.getnFixCommitInRelease();
            String nBugFixFromStart = releaseFile.getnFixCommitFromStart();
            String buggy = releaseFile.getBuggy();
            String[] data = {Integer.toString(release), filename, nAuthorsInRel, locInRel, avgChurnInRel, avgChurnFromStart, avgAddInRel, avgAddFromStart, nRevInRel, nRevFromStart, nBugFixInRel, nBugFixFromStart, buggy};
            writer.writeNext(data);
        }
    }

    public static void createTestingSet(List<JavaFile> releaseFiles, int numReleases, String projectName){
        String filePath = CSV_OUTPUT_PATH + projectName + "_"+numReleases + "Test.csv";
        File file = new File(filePath);

        try {
            // create FileWriter object with file as parameter

            FileWriter outputFile = new FileWriter(file, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);

            // adding header to csv
            String[] header = {"Release", "File", "# Authors in Release", "LOC", "Avg Churn in Release", "Avg Churn from Start", "Avg LOC Added in Release", "Avg LOC Added from Start", "# Revision in Release", "# Revision from Start", "# Bug Fix in Release", "# Bug Fix from Start", "Buggy"};
            writer.writeNext(header);
            writeReleaseDataLineByLine(releaseFiles, writer, numReleases);
            // closing writer connection
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //takes a list of lists which containt all the filenames of a release for each release
    public static void createTrainingSet(List<List<JavaFile>> files, int numReleases, String projectName) {
        // first create file object for file placed at location
        // specified by filepath
        String filePath = CSV_OUTPUT_PATH + projectName + "_"+numReleases + "Train.csv";

        File file = new File(filePath);

        try {
            // create FileWriter object with file as parameter

            FileWriter outputFile = new FileWriter(file, false);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);

            // adding header to csv
            String[] header = {"Release", "File", "# Authors in Release", "LOC", "Avg Churn in Release", "Avg Churn from Start", "Avg LOC Added in Release", "Avg LOC Added from Start", "# Revision in Release", "# Revision from Start", "# Bug Fix in Release", "# Bug Fix from Start", "Buggy"};
            writer.writeNext(header);
            for (int i = 0; i < numReleases-1; i++) {
                writeReleaseDataLineByLine(files.get(i), writer, i+1);
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
        return new ArrayList<>(Arrays.asList(line).subList(2, line.length));
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
        String[] header = {"@relation "+title, "@attribute #AuthorsInRelease numeric", "@attribute LOC numeric",
                "@attribute AvgChurnInRelease numeric", "@attribute AvgChurnFromStart numeric",
                "@attribute AvgLOCAddedInRelease numeric", "@attribute AvgLOCAddedFromStart numeric",
                "@attribute #RevisionInRelease numeric", "@attribute #RevisionFromStart numeric",
                "@attribute #BugFixInRelease numeric", "@attribute #BugFixFromStart numeric", "@attribute Buggy {YES,NO}", "@data"};
        for (String line : header) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
    }

    public static void writeCsv(List<String[]> data, String path, String[] header) {
        File file = new File(path);
        boolean exists = file.exists();
        try {
            // create FileWriter object with file as parameter
            FileWriter outputFile = new FileWriter(file, true);
            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputFile);

            // adding header to csv
            if(!exists)
                writer.writeNext(header);
            for(String[] elem : data){
                writer.writeNext(elem);
            }
            // closing writer connection
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkIfOutDirIsEmpty(){
        String path = ARFF_OUTPUT_PATH.substring(0,25);
        File directory = new File(path);
        if (directory.isDirectory()) {
            String[] files = directory.list();

            return files != null && files.length == 0;
        } else {
            LOGGER.info("Not a valid directory path");
        }
        return false;
    }

    public static void  collectDataExperiment(String project, int datasetNum){
        String writePathTot = OUTPUT_PATH + project + ".csv";
        for(int i=0;i<datasetNum;i++){
            for (ExperimentType experimentType : ExperimentType.values()) {
                String readPath;
                String writePathPart;
                if(i==0) {
                    readPath = OUTPUT_PATH + project + "_mean_" + experimentType + ".csv";
                    writePathPart=OUTPUT_PATH + project + "_mean.csv";
                }
                else if((i==1)&&(Objects.equals(project, "zookeeper"))) continue; //ASSUNZIONE 22 Zookeeper jump release 2 cause of NaN values
                else {
                    readPath = OUTPUT_PATH + project + "_" + i + "_" + experimentType + ".csv";
                    writePathPart=OUTPUT_PATH + project + "_" + i + ".csv";
                }
                List<String[]> csvContent=readCsv(readPath);
                writeCsv(csvContent, writePathPart, OUT_HEADER);
                writeCsv(csvContent, writePathTot, OUT_HEADER);

            }
        }
    }
}
