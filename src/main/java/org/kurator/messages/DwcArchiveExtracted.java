package org.kurator.messages;

import java.nio.file.Path;

/**
 * Created by lowery on 7/21/16.
 */
public class DwcArchiveExtracted {
    private Path outputDir;

    public DwcArchiveExtracted(Path outputDir) {
        this.outputDir = outputDir;
    }

    public Path getOutputDir() {
        return outputDir;
    }
}
