package com.isw2.control;

import com.isw2.entity.Commit;
import com.isw2.entity.JavaFile;
import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.util.CsvHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class MeasureController {
    private final Project project;
    private static final Logger myLogger = Logger.getLogger("logger");

    public MeasureController(Project project) {
        this.project = project;
    }

    //TODO Implementare prendere le bugfix nella release e le bugfix da start
    public void createWalkForwardDatasets() {
        List<List<JavaFile>> releaseFiles = new ArrayList<>();
        List<List<Commit>> commits = new ArrayList<>();
        List<Release> releases = this.project.getReleasesOfInterest();
        for (int i = 0; i < releases.size(); i++) {
            Release release = releases.get(i);
            List<Commit> relCommits = release.getCommits();
            List<JavaFile> relFiles = release.getFileTreeAtReleaseEnd();
            measureInReleaseFeatures(relFiles, relCommits);
            commits.add(relCommits);
            releaseFiles.add(relFiles);
            measureFromStartFeatures(releaseFiles, i + 1);
            adjustMeasure(releaseFiles);
            CsvHandler.writeDataLineByLine(releaseFiles, i + 1);
        }


    }

    private void adjustMeasure(List<List<JavaFile>> releaseFiles){
        for(List<JavaFile> files : releaseFiles){
            for(JavaFile file : files) {
                String nRevFromStart = file.getnRevFromStart();
                String nRevInRelease = file.getnRevInRelease();
                String locAtEndRelease = file.getLocAtEndRelease();
                String avgLocAddedInRelease = file.getAvgLocAddedInRelease();
                if ((Objects.equals(nRevFromStart, "1")) && (Objects.equals(nRevInRelease, "1")) && (!Objects.equals(locAtEndRelease, avgLocAddedInRelease))) {
                    file.setAvgLocAddedInRelease(locAtEndRelease);
                    file.setAvgChurnInRelease(locAtEndRelease);
                    file.setAvgChurnFromStart(locAtEndRelease);
                    file.setAvgLocAddedFromStart(locAtEndRelease);
                }
            }
        }
    }

    private void measureInReleaseFeatures(List<JavaFile> releaseFiles, List<Commit> commits) {
        for (JavaFile srcFile : releaseFiles) {

            int cnt = 0;
            int adds = 0;
            int dels = 0;
            int filteredAdds;
            int filteredDels;
            int realAdds = 0;
            int realDels = 0;
            List<String> authors = new ArrayList<>();

            String[] lines = srcFile.getContent().split("\r\n|\r|\n");
            int nLines = lines.length;
            int filteredLines = (int) Math.floor((double) (nLines * 10) / 100);

            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(srcFile.getName())) {
                        String author = commit.getAuthor();
                        if (!authors.contains(author)) {
                            authors.add(author);
                        }
                        cnt++;
                        adds = Integer.parseInt(file.getAdd());
                        filteredAdds = (int) Math.floor((double) (adds * 10) / 100); //ASSUNZIONE 9-10
                        realAdds += adds - filteredAdds;
                        dels = Integer.parseInt(file.getDel());
                        filteredDels = (int) Math.floor((double) (dels * 10) / 100); //ASSUNZIONE 9-10
                        realDels += dels - filteredDels;

                    }
                }
            }

            int nAuthors = authors.size();       //Per ogni commit nella release che ha toccato il file si guarda l'autore e si aggiunge ad una lista senza duplicati
            srcFile.setnAuthorInRelease(Integer.toString(nAuthors));
            int loc = nLines - filteredLines;     //Per ogni commit nella release che ha toccato il file si guarda loc e si fa la media
            srcFile.setLocAtEndRelease(Integer.toString(loc));
            srcFile.setnRevInRelease(Integer.toString(cnt));        //Per ogni file si contano le commit nella release che lo hanno toccato
            int avgAdds = (int) Math.floor((double) realAdds / cnt);     //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
            srcFile.setAvgLocAddedInRelease(Integer.toString(avgAdds));
            int churn = Math.abs((int) Math.floor((double) (realAdds - realDels) / cnt));      //Per ogni commit nella release che ha toccato il file si fa locAdded - locDeleted e si fa la media
            srcFile.setAvgChurnInRelease(Integer.toString(churn));
            srcFile.setRelDels(Integer.toString(realDels)); //Serve per le misure from start
            srcFile.setRelAdds(Integer.toString(realAdds)); //Serve per le misure from start

        }
    }

    private void measureFromStartFeatures(List<List<JavaFile>> releaseFiles, int numReleases) {
        //I valori dalla release 0 me li calcolo come somma/media dei singoli valori della release fino alla corrente
        List<JavaFile> lastRelFiles = releaseFiles.get(numReleases - 1);
        for (JavaFile file : lastRelFiles) {
            int totCnt = Integer.parseInt(file.getnRevInRelease());
            int totAdds = Integer.parseInt(file.getRelAdds());
            int totDels = Integer.parseInt(file.getRelDels());
            for (int i = numReleases - 2; i >= 0; i--) {
                List<JavaFile> relFiles = releaseFiles.get(i);
                for (JavaFile relFile : relFiles) {
                    if (relFile.getName().equals(file.getName())) {
                        totCnt += Integer.parseInt(relFile.getnRevInRelease());
                        totAdds += Integer.parseInt(relFile.getRelAdds()); //Nota qui non faccio ASSUNZIONE 9-10 perchè questi valori li setto in measureInReleaseFeatures e sono gia 'corretti'
                        totDels += Integer.parseInt(relFile.getRelDels());
                    }
                }
            }
            file.setnRevFromStart(Integer.toString(totCnt));        //Per ogni file si contano le commit nella release che lo hanno toccato
            int avgLocAdded = (int) Math.floor((double) totAdds / totCnt);       //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
            file.setAvgLocAddedFromStart(Integer.toString(avgLocAdded));
            int avgChurn = Math.abs((int) Math.floor((double) (totAdds - totDels) / totCnt));     //Per ogni commit che ha toccato il file si fa locAdded - locDeleted e si fa la media
            file.setAvgChurnFromStart(Integer.toString(avgChurn));
        }
    }


    //Per ogni commit nella release che ha toccato il file si conta quante sono afferenti ad un bugFix
    private String measureFixCommitsInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit che ha toccato il file si conta quante sono afferenti ad un bugFix
    private String measureFixCommitsFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

}
