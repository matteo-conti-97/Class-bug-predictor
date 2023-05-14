package com.isw2.control;

import com.isw2.entity.Commit;
import com.isw2.entity.JavaFile;
import com.isw2.entity.Project;
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
    /*public void createWalkForwardDatasets() {
        List<List<String>> releaseFiles = new ArrayList<>();
        List<List<String>> features = new ArrayList<>();
        List<List<Commit>> commits = new ArrayList<>();
        List<Release> releases = this.project.getReleasesOfInterest();
        Release release;
        for (int i = 0; i < releases.size(); i++) {
            release = releases.get(i);
            //ASSUNZIONE se la release non ha commit associati, la lista di file è vuota e la lista di commit anche, avrò due file dataset uguali, lo cancello manualmente
            if (release.getCommits().isEmpty()) {
                releaseFiles.add(new ArrayList<>());
                commits.add(new ArrayList<>());
                features.add(new ArrayList<>());
            } else {
                myLogger.info(new String("Release " + release.getName() + " has " + releaseFiles.get(i).size() + " non test java files based on commit " + lastCommit.getSha()));
                //TODO Ora ho la lista dei file per ogni release, per ogni lista dei file devo girarmela e calcolarmi tutte le feature per la release associata alla lista
                features.add(measureAuthorsInRelease(releaseFiles.get(i), commits.get(i)));
                //TODO Probabilmente mi conviene fare un array feature per ogni feature
                myLogger.info(new String(features));
            }
            CsvHandler.writeDataLineByLine(releaseFiles, features, i + 1);

        }
    }*/

    //Per ogni commit nella release che ha toccato il file si guarda l'autore e si aggiunge ad una lista senza duplicati
    private List<String> measureAuthorsInRelease(List<String> releaseFiles, List<Commit> commits) {
        List<String> ret = new ArrayList<>();
        for (String filename : releaseFiles) {
            List<String> authors = new ArrayList<>();
            for (Commit commit : commits) {
                List<JavaFile> touchedFiles = commit.getTouchedFiles();
                for (JavaFile file : touchedFiles) {
                    if (file.getName().equals(filename)) {
                        String author = commit.getAuthor();
                        if (!authors.contains(author)) {
                            authors.add(author);
                        }
                    }
                }
            }
            ret.add(Integer.toString(authors.size()));
        }
        myLogger.info(new String(Integer.toString(ret.size())));
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file si guarda loc e si fa la media
    private String measureLocAtEndRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file si fa locAdded - locDeleted e si fa la media
    private String measureAvgChurnInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit che ha toccato il file si fa locAdded - locDeleted e si fa la media
    private String measureAvgChurnFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni file si contano le commit nella release che lo hanno toccato
    private String measureRevisionNumInRelease(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni file si contano le commit nella release che lo hanno toccato
    private String measureRevisionNumFromStart(String filename, int release) {
        String ret = "";
        return ret;
    }

    //
    private String measureRevisionAge(String filename, int release) {
        String ret = "";
        return ret;
    }

    //Per ogni commit nella release che ha toccato il file prende fa locAdded e si fa la media
    private String measureAvgLocAddedInRelease(String filename, int release) {
        String ret = "";
        return ret;
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
}
