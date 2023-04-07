package com.isw2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitFilter {
	private Git git;

	//Constructor in which is specified the path of the local repo
	GitFilter(String repoPath) {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			Repository repo = builder.setGitDir(new File(repoPath + "\\.git")).setMustExist(true).build();
			this.git = new Git(repo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//Retrieve all repo commits
	public List<String> getCommits() {
		Iterable<RevCommit> log = null;
		List<String> commitLog = new ArrayList<>();
		try {
			log = this.git.log().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
        for (RevCommit rev : log) {
          //Populate with full log
          commitLog.add(rev.getFullMessage());
        }
		return commitLog;
	}

	//Drop all commits until specified release TODO
	public List<String> dropUntilRel(List<String> commitLog, String rel){
		return commitLog;
	}

	//Drop all the commits not associated with specified ticket TODO
	public List<String> filterByTicket(List<String> commitLog, String lbTicket){
		return commitLog;
	}


	/*Bisogna prima di tutto capire quante versioni ci sono, poi bisogna associare i file java in git alla versione
	Successivamente vanno prelevate tutte le metriche necessarie, è utile sicuramente calcolare qualche indice
	come il linkage*/
}
