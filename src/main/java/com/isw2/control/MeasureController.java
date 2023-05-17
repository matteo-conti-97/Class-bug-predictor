package com.isw2.control;

import com.isw2.entity.Commit;
import com.isw2.entity.JavaFile;
import com.isw2.entity.Project;
import com.isw2.entity.Release;
import com.isw2.util.CsvHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MeasureController {
    private final Project project;
    private static final Logger myLogger = Logger.getLogger("logger");

    public MeasureController(Project project) {
        this.project = project;
    }

    //TODO MODIFICARLA PER ADATTARLA ALLA NUOVA IMPLEMENTAZIONE DEL RESTO
    public void createWalkForwardDatasets() {
        List<List<JavaFile>> releaseFiles = new ArrayList<>();
        List<List<Commit>> commits = new ArrayList<>();
        List<Release> releases = this.project.getReleasesOfInterest();
        for (int i = 0; i < releases.size(); i++) {
            Release release = releases.get(i);
            List<Commit> relCommits= release.getCommits();
            List<JavaFile> relFiles= release.getFileTreeAtReleaseEnd();
            commits.add(relCommits);
            releaseFiles.add(relFiles);
            measureAuthorsInRelease(relFiles, relCommits);
            measureLocAtEndRelease(relFiles);
            measureAvgChurnInRelease(relFiles, relCommits);
            measureRevisionNumInRelease(relFiles, relCommits);
            measureAvgLocAddedInRelease(relFiles, relCommits);
            CsvHandler.writeDataLineByLine(releaseFiles, i + 1);
        }

    }

    //Per ogni commit nella release che ha toccato il file si guarda l'autore e si aggiunge ad una lista senza duplicati
    private void measureAuthorsInRelease(List<JavaFile> releaseFiles, List<Commit> commits) {
        for (JavaFile srcFile : releaseFiles) {
            List<String> authors = new ArrayList<>();
            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(srcFile.getName())) {
                        String author = commit.getAuthor();
                        if (!authors.contains(author)) {
                            authors.add(author);
                        }
                    }
                }
            }
            srcFile.setnAuthorInRelease(Integer.toString(authors.size()));
        }
    }

    //Per ogni commit nella release che ha toccato il file si guarda loc e si fa la media
    private void measureLocAtEndRelease(List<JavaFile> releaseFiles) {
        String loc;
        for(JavaFile srcFile : releaseFiles){
            //Togliendo i newline ed i carriage return non ho blank lines
            String[] lines = srcFile.getContent().split("\r\n|\r|\n");
            //ASSUNZIONE i commenti sono il 10% delle righe di codice totale
            int nLines=lines.length;
            int filteredLines=(int) Math.floor((double)(nLines*10)/100);
            loc = String.valueOf(nLines-filteredLines);
            srcFile.setLocAtEndRelease(loc);
        }
    }

    //Per ogni commit nella release che ha toccato il file si fa locAdded - locDeleted e si fa la media
    private void measureAvgChurnInRelease(List<JavaFile> releaseFiles, List<Commit> commits) {
        for (JavaFile srcFile : releaseFiles) {
            int cnt=0;
            int adds=0;
            int dels=0;
            int filteredAdds;
            int filteredDels;
            int realAdds=0;
            int realDels=0;
            int churn=0;
            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(srcFile.getName())) {
                        cnt++;
                        adds=Integer.parseInt(file.getAdd());
                        filteredAdds=(int) Math.floor((double)(adds*10)/100);
                        realAdds+=adds-filteredAdds;
                        dels=Integer.parseInt(file.getDel());
                        filteredDels=(int) Math.floor((double)(dels*10)/100);
                        realDels+=dels-filteredDels;
                    }
                }
            }

            churn=Math.abs((int) Math.floor((double)(realAdds-realDels)/cnt));
            srcFile.setAvgChurnInRelease(Integer.toString(churn));
        }
    }

    //Per ogni commit che ha toccato il file si fa locAdded - locDeleted e si fa la media
    private String measureAvgChurnFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni file si contano le commit nella release che lo hanno toccato
    private void measureRevisionNumInRelease(List<JavaFile> releaseFiles, List<Commit> commits) {
        for (JavaFile srcFile : releaseFiles) {
            int cnt=0;
            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(srcFile.getName())) {
                        cnt++;
                    }
                }
            }
            srcFile.setnRevInRelease(Integer.toString(cnt));
        }
    }

    //Per ogni file si contano le commit nella release che lo hanno toccato
    private String measureRevisionNumFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }


    //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
    private void measureAvgLocAddedInRelease(List<JavaFile> releaseFiles, List<Commit> commits) {
        for (JavaFile srcFile : releaseFiles) {
            int cnt=0;
            int adds=0;;
            int filteredAdds;
            int realAdds=0;;
            int avgAdds=0;
            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(srcFile.getName())) {
                        cnt++;
                        adds=Integer.parseInt(file.getAdd());
                        filteredAdds=(int) Math.floor((double)(adds*10)/100);
                        realAdds+=adds-filteredAdds;
                    }
                }
            }

            avgAdds=(int) Math.floor((double)realAdds/cnt);
            srcFile.setAvgLocAddedInRelease(Integer.toString(avgAdds));
        }
    }


    //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
    private String measureAvgLocAddedFromStart(String filename, int release) {
        String ret = "";
        return ret;
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

    //
    private String measureJolly(String filename, int release) {
        String ret = "";
        return ret;
    }
}
