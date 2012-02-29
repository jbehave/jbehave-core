package com.learnthinkcode.example.jbehave;

import java.io.File;

import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;

public class ClassnameFilePathResolver implements FilePathResolver {

	private String className;

	public ClassnameFilePathResolver(String className) {
		this.className = className;
	}

	@Override
	public String resolveDirectory(StoryLocation storyLocation, String relativeDirectory) {
		File parent = new File(storyLocation.getCodeLocation().getFile()).getParentFile();
		return parent.getPath().replace('\\', '/') + "/" + relativeDirectory;
	}

	@Override
	public String resolveName(StoryLocation storyLocation, String extension) {
		return className + "." + extension;
	}

}
