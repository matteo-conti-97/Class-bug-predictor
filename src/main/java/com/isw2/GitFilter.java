package com.isw2;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GitFilter {
	private ArrayList<String> logMessages;
	private ArrayList<String> filteredLogMessages;
	private FileRepositoryBuilder builder;
	private Repository repo;
	private Git git;

	GitFilter(String repoPath){
		logMessages=new ArrayList<>();
		filteredLogMessages=new ArrayList<>();
		builder = new FileRepositoryBuilder();
		try {
			repo = builder.setGitDir(new File(repoPath+"\\.git")).setMustExist(true).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
        git = new Git(repo);
	}

	public ArrayList<String> filterLog(String filter) {
		Iterable<RevCommit> log = null;
		try {
			log = this.git.log().call();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
        for (RevCommit rev : log) {
          //Populate with filtered log
          if (rev.getFullMessage().contains(filter)){
        	 filteredLogMessages.add(rev.getFullMessage());
          }
          //Populate with full log
          logMessages.add(rev.getFullMessage());
        }
		return filteredLogMessages;
	}
}
