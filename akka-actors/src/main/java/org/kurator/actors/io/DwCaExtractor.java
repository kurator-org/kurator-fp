package org.kurator.actors.io;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.gbif.dwc.record.StarRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;
import org.json.simple.JSONObject;
import org.kurator.actors.KafkaConsumerActor;
import org.kurator.actors.KafkaProducerActor;
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
public class DwCaExtractor extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private static final String TOPIC = "georeference";

    private ExecutionContext ec = getContext().system().dispatcher();
    private Iterator<StarRecord> iterator;

    private ActorRef producer;

    public DwCaExtractor() throws FileNotFoundException {
        this.producer = getContext().actorOf(Props.create(KafkaProducerActor.class, TOPIC), "producer");
    }

    public Future unzip(final File file) {
        Future<DwcArchiveExtracted> future = future(new Callable<DwcArchiveExtracted>() {
            public DwcArchiveExtracted call() throws Exception {
                Path outputDir = Files.createTempDirectory(file.getName().replace(".", "_") + "_content");

                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
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

                Archive archive = openArchive(outputDir.toFile());
                if (!isValidArchive(archive)) {
                    logger.error("Invalid dwca archive.");
                    // TODO: throw exception and send error message to supervisor
                }
                return new DwcArchiveExtracted(archive);
            }
        }, ec);

        return future;
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
                        logger.error("Unable to open archive directory " + e.getMessage());
                        logger.error("Unable to open directory contained within archive directory " + e1.getMessage());
                    }
                }
            }
            if (!foundContained) {
                logger.error("Unable to open archive directory " + e.getMessage());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.error("Unable to open archive directory " + e.getMessage());
        }
        return result;
    }

    protected boolean isValidArchive(Archive dwcArchive) {
        boolean result = false;
        if (dwcArchive==null) {
            return result;
        }
        if (dwcArchive.getCore() == null) {
            logger.error("Cannot locate the core datafile in " + dwcArchive.getLocation().getPath());
            return result;
        }
        logger.error("Core file found: " + dwcArchive.getCore().getLocations());
        logger.error("Core row type: " + dwcArchive.getCore().getRowType());
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
                    logger.error("Cannot find " + term + " in core of input dataset.");
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

            // Extract the dwc archive in a future
            File file = ((ExtractDwcArchive) message).file();
            Future<DwcArchiveExtracted> future = unzip(file);

            // pipe the result of the future back to self
            pipe(future, ec).to(self(), sender());

        } else if (message instanceof DwcArchiveExtracted) {

            // initialize the actor and start reading records
            Archive archive = ((DwcArchiveExtracted) message).archive();

            iterator = archive.iterator();

            if (iterator.hasNext()) {
                self().tell(new NextRecord(), sender());
            }

        } else if (message instanceof NextRecord) {

            StarRecord record = iterator.next();

            // serialize record as json
            JSONObject response = new JSONObject();
            for (Term term : record.core().terms()) {
                response.put(term.simpleName(), record.core().value(term));
            }

            // publish record via kafka producer actor
            producer.tell(response.toJSONString(), self());

        } else if (message instanceof KafkaProducerActor.SendSuccessful) {

            // if the message was published successfully send another record
            if (iterator.hasNext()) {
                self().tell(new NextRecord(), sender());
            }

        }
    }

    private class DwcArchiveExtracted {
        private Archive archive;

        public DwcArchiveExtracted(Archive archive) {
            this.archive = archive;
        }

        public Archive archive() {
            return archive;
        }
    }
}


