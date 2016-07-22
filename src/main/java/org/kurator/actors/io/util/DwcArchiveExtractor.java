package org.kurator.actors.io.util;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import jdk.internal.util.xml.impl.Input;
import org.apache.avro.data.Json;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.noggit.JSONParser;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.gbif.dwc.record.StarRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.json.simple.JSONObject;
import org.kurator.messages.DwcArchiveExtracted;
import org.kurator.messages.ExtractDwcArchive;
import org.kurator.messages.NextRecord;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/21/16.
 */
public class DwcArchiveExtractor extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ExecutionContext ec = getContext().system().dispatcher();
    private final File zipFile;
    private Iterator<StarRecord> iterator;

    private ActorRef kafkaProducer;
    private long cValidRecords;

    public DwcArchiveExtractor(ActorRef producer, File file) throws FileNotFoundException {
        this.zipFile = file;
        this.kafkaProducer = producer;
    }

    public void unzip() {
        Future<DwcArchiveExtracted> future = future(new Callable<DwcArchiveExtracted>() {
            public DwcArchiveExtracted call() throws Exception {
                Path outputDir = Files.createTempDirectory(zipFile.getName().replace(".", "_") + "_content");
                System.out.println(outputDir);
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
                ZipEntry entry = zipInputStream.getNextEntry();

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
                }

                zipInputStream.closeEntry();
                zipInputStream.close();

                return new DwcArchiveExtracted(outputDir);
            }
        }, ec);

        pipe(future, ec).to(self(), sender());
    }

    protected Archive openArchive(File outputDirectory) {
        Archive result = null;
        try {
            result = ArchiveFactory.openArchive(outputDirectory);
        } catch (UnsupportedArchiveException e) {
            logger.error(e.getMessage());
            File[] containedFiles = outputDirectory.listFiles();
            boolean foundContained = false;
            for (int i = 0; i<containedFiles.length; i++) {
                if (containedFiles[i].isDirectory()) {
                    try {
                        // Try harder, some pathological archives contain a extra level of subdirectory
                        result = ArchiveFactory.openArchive(containedFiles[i]);
                        foundContained = true;
                    } catch (Exception e1) {
                        logger.error(e.getMessage());
                        System.out.println("Unable to open archive directory " + e.getMessage());
                        System.out.println("Unable to open directory contained within archive directory " + e1.getMessage());
                    }
                }
            }
            if (!foundContained) {
                System.out.println("Unable to open archive directory " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            System.out.println("Unable to open archive directory " + e.getMessage());
        }
        return result;
    }

    protected boolean checkArchive(Archive dwcArchive) {
        boolean result = false;
        if (dwcArchive==null) {
            return result;
        }
        if (dwcArchive.getCore() == null) {
            System.out.println("Cannot locate the core datafile in " + dwcArchive.getLocation().getPath());
            return result;
        }
        System.out.println("Core file found: " + dwcArchive.getCore().getLocations());
        System.out.println("Core row type: " + dwcArchive.getCore().getRowType());
        if (dwcArchive.getCore().getRowType().equals(DwcTerm.Occurrence) ) {

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

            for (DwcTerm term : expectedTerms) {
                if (!dwcArchive.getCore().hasTerm(term)) {
                    System.out.println("Cannot find " + term + " in core of input dataset.");
                }
            }

            result = true;
        } else {
            // currently can only process occurrence core
        }

        return result;
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof ExtractDwcArchive) {
            unzip();
        } else if (message instanceof DwcArchiveExtracted) {
            Archive archive = openArchive(((DwcArchiveExtracted) message).getOutputDir().toFile());
            checkArchive(archive);
            iterator = archive.iterator();

            if (iterator.hasNext()) {
                self().tell(new NextRecord(), sender());
            }
        } else if (message instanceof NextRecord) {
                StarRecord dwcrecord = iterator.next();

            JSONObject record = new JSONObject();
            for (Term term : dwcrecord.core().terms()) {
                record.put(term.simpleName(), dwcrecord.core().value(term));
            }
            //System.out.println(row);
                //SpecimenRecord record = new SpecimenRecord(dwcrecord);
                kafkaProducer.tell(record.toJSONString(), self());
                //Token<SpecimenRecord> t = new TokenWithProv<SpecimenRecord>(record,this.getClass().getSimpleName(),invoc);
                cValidRecords++;
            if (cValidRecords % 1000 == 0) {
                System.out.println("Produced " + cValidRecords + " messages.");
            }

            //self().tell(new NextRecord(), sender());
                //listener.tell(t,getSelf());
        } else if (message instanceof RecordMetadata) {
            if (iterator.hasNext()) {
                self().tell(new NextRecord(), sender());
            }
        }
    }
}
