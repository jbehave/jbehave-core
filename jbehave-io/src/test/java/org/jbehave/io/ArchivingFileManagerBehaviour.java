package org.jbehave.io;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArchivingFileManagerBehaviour {

    private FileManager manager;
    private File upload;
    private File dir1;
    private File file1;
    private File file2;
    private File zip;

    @BeforeEach
    public void setup() throws IOException {
        upload = createUploadDir();
        dir1 = createDir("dir1");
        file1 = create("file1");
        file2 = create("file2");
        zip = create("dir1.zip");
        archiveFiles(zip, asList(file1, file2));
        manager = new ArchivingFileManager(new ZipFileArchiver(), new SilentFileMonitor(), upload);
    }

    @AfterEach
    public void tearDown() throws IOException {
        file1.delete();
        file2.delete();
        dir1.delete();
        zip.delete();
    }

    @Test
    void canListFilesThatAreNotDirectories() throws IOException {
        assertThat(listFiles(), is(asList(zip, file1, file2)));
    }

    private List<File> listFiles() {
        List<File> list = manager.list();        
        Collections.sort(list, Comparator.comparing(File::getName));
        return list;
    }

    @Test
    void canDeleteFilesAndDirectories() throws IOException {
        assertThat(listFiles(), is(asList(zip, file1, file2)));
        manager.delete(asList(file1));
        assertThat(listFiles(), is(asList(zip, file2)));
        manager.delete(asList(zip));
        assertThat(listFiles(), is(asList(file2)));
    }

    @Test
    void canWriteFileItems() throws Exception {
        List<String> errors = new ArrayList<String>();
        FileItem file2FileItem = mock(FileItem.class, "file2");
        FileItem zipFileItem = mock(FileItem.class, "zip");
        when(zipFileItem.getName()).thenReturn(zip.getName());        
        doNothing().when(zipFileItem).write(zip);
        when(file2FileItem.getName()).thenReturn(file2.getName());      
        doNothing().when(file2FileItem).write(file2);
        // ensure files do not exists
        file2.delete();
        dir1.delete();
        manager.upload(asList(file2FileItem, zipFileItem), errors);
        assertThat(errors.size(), is(0));
    }

    @Test
    void cannotUnarchiveMissingFile() throws Exception {
        FileItem file2FileItem = mock(FileItem.class, "file2");
        FileItem zipFileItem = mock(FileItem.class, "zip");
        when(zipFileItem.getName()).thenReturn(zip.getName());      
        doNothing().when(zipFileItem).write(zip);
        when(file2FileItem.getName()).thenReturn(file2.getName());      
        doNothing().when(file2FileItem).write(file2);
        // ensure files do not exists
        file2.delete();
        dir1.delete();
        // remove zip
        zip.delete();
        List<String> errors = new ArrayList<>();
        List<File> files = manager.upload(asList(file2FileItem, zipFileItem), errors);
        manager.unarchiveFiles(files, errors);
        assertThat(errors.size(), is(2));
    }

    @Test
    void canIgnoreWritingFileItemsWithBlankNames() throws Exception {
        List<String> errors = new ArrayList<String>();
        FileItem file2FileItem = mock(FileItem.class, "file2");
        FileItem zipFileItem = mock(FileItem.class, "zip");
        when(zipFileItem.getName()).thenReturn("");      
        when(file2FileItem.getName()).thenReturn("");              
        manager.upload(asList(file2FileItem, zipFileItem), errors);
        assertThat(errors.size(), is(0));
    }

    @Test
    void cannotWriteFileItemsThatFail() throws Exception {
        List<String> errors = new ArrayList<String>();
        FileItem file2FileItem = mock(FileItem.class, "file2");
        FileItem zipFileItem = mock(FileItem.class, "zip");
        when(zipFileItem.getName()).thenReturn(zip.getName());      
        doThrow(new IOException("zip write failed")).when(zipFileItem).write(zip);
        when(file2FileItem.getName()).thenReturn(file2.getName());      
        doThrow(new IOException("file2 write failed")).when(file2FileItem).write(file2);        
        // ensure files do not exists
        file2.delete();
        zip.delete();
        manager.upload(asList(file2FileItem, zipFileItem), errors);
        assertThat(errors.size(), is(4));
    }

    private File create(String path) throws IOException {
        File file = new File(upload, path);
        file.createNewFile();
        return file;
    }

    private File createDir(String path) throws IOException {
        File dir = new File(upload, path);
        dir.mkdirs();
        File child = new File(dir, "child1");
        child.createNewFile();
        return dir;
    }

    private File createUploadDir() throws IOException {
        File dir = new File("target/upload");
        dir.mkdirs();
        return dir;
    }

    
    private void archiveFiles(File archive, List<File> files) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(archive);
        ZipOutputStream zipStream = new ZipOutputStream(fileStream);

        for (File file : files) {
            if (!file.exists() || file.isDirectory()) {
                // only interested in flat file archives for testing purposes
                continue;
            }
            ZipEntry entry = new ZipEntry(file.getName());
            zipStream.putNextEntry(entry);
            copy(file, zipStream);
        }

        zipStream.close();
        fileStream.close();
    }

    private void copy(File file, ZipOutputStream out)
            throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            IOUtils.copy(in, out);
        } finally {
            in.close();
        }
    }

}