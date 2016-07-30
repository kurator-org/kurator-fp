package org.kurator.messages;

import java.io.File;

/**
 * Created by lowery on 7/21/16.
 */
public class ExtractDwcArchive {
    private File file;

    public ExtractDwcArchive(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }
}