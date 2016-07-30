package org.kurator.messages;

import java.io.File;
import java.util.Objects;

public final class Curate {
    private File file;

    public Curate(File file) {
        this.file = file;
    }

    public File file() {
        return file;
    }
}
