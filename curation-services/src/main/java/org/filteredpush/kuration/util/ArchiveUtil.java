package org.filteredpush.kuration.util;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.filteredpush.kuration.exceptions.CurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lowery on 8/1/16.
 */
public class ArchiveUtil {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveUtil.class);
    private Archive archive;

    public static Archive openArchive(File archive) throws CurationException {
        File archiveDir = null;

        if (!archive.exists()) {
            throw new CurationException("Could not open archive file from: " + archive.getAbsolutePath());
        } if (archive.isFile()) {
            try {
                archiveDir = unzip(archive).toFile();
            } catch (IOException e) {
                throw new CurationException("Could not unzip archive", e);
            }
        } else if (archive.isDirectory()){
            archiveDir = archive;
        }

        Archive result = null;
        try {
            result = ArchiveFactory.openArchive(archiveDir);
        } catch (UnsupportedArchiveException e) {
            logger.warn("Unsupported archive, checking directory contents and trying again");
            File[] containedFiles = archiveDir.listFiles();
            boolean foundContained = false;
            for (int i = 0; i<containedFiles.length; i++) {
                if (containedFiles[i].isDirectory()) {
                    try {
                        // Try harder, some pathological archives contain a extra level of subdirectory
                        result = ArchiveFactory.openArchive(containedFiles[i]);
                        foundContained = true;
                    } catch (Exception e1) {
                        throw new CurationException("Unable to open directory contained within archive directory", e1);
                    }
                }
            }
            if (!foundContained) {
                throw e;
            }
        } catch (IOException e) {
            throw new CurationException("Unable to open archive directory ", e);
        }

        logger.debug("Deleting temporary archive directory: {}", archiveDir.getAbsolutePath());
        // TODO: clear temp dir

        // Check if this archive is valid
        if (archive==null) {
            throw new CurationException("Invalid archive");
        }
        if (result.getCore() == null) {
            throw new CurationException("Cannot locate the core datafile in " + result.getLocation().getPath());
        }

        logger.debug("Core file found: " + result.getCore().getLocations());
        logger.debug("Core row type: " + result.getCore().getRowType());

        if (result.getCore().getRowType().equals(DwcTerm.Occurrence) ) {

            // check expectations
            List<DwcTerm> expectedTerms = new ArrayList<DwcTerm>();
            expectedTerms.add(DwcTerm.scientificName);
            expectedTerms.add(DwcTerm.scientificNameAuthorship);
            expectedTerms.add(DwcTerm.eventDate);
            expectedTerms.add(DwcTerm.recordedBy);
            expectedTerms.add(DwcTerm.decimalLatitude);
            expectedTerms.add(DwcTerm.decimalLongitude);
            expectedTerms.add(DwcTerm.locality);
            expectedTerms.add(DwcTerm.basisOfRecord);

            List<DwcTerm> missingTerms = new ArrayList<>();

            for (DwcTerm term : expectedTerms) {
                if (!result.getCore().hasTerm(term)) {
                    missingTerms.add(term);
                }
            }

            if (!missingTerms.isEmpty())
                logger.error("Cannot find term(s)" + missingTerms + " in core of input dataset.");

        } else {
            // currently can only process occurrence core
        }

        return result;
    }

    private static Path unzip(File zipFile) throws IOException {
        Path outputDir = Files.createTempDirectory(zipFile.getName().replace(".", "_") + "_content");

        logger.debug("Unziping darwincore archive from {} to directory {}", zipFile, outputDir);

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry entry = zipInputStream.getNextEntry();

        int count = 0;

        while (entry != null) {
            File expandedFile = new File(outputDir + File.separator + entry.getName());
            OutputStream expandedfileOutputStream = new FileOutputStream(expandedFile);

            byte[] buffer = new byte[1024];

            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                expandedfileOutputStream.write(buffer, 0, len);
            }

            expandedfileOutputStream.close();
            entry = zipInputStream.getNextEntry();

            count++;
        }

        zipInputStream.closeEntry();
        zipInputStream.close();

        logger.debug("{} files unzipped to directory {}", count, outputDir);

        return outputDir;
    }
}
