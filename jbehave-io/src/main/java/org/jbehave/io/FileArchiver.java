package org.jbehave.io;

import java.io.File;
import java.util.List;

/**
 * Allows to archive and unarchive a file and list its content.
 */
public interface FileArchiver {

	boolean isArchive(File file);

	void archive(File archive, File directory);
	
	void unarchive(File archive, File directory);

	List<File> listContent(File directory);

	File directoryOf(File archive);	

	File relativeTo(File file, File directory);

}