package org.kurator.actors.io;

import akka.actor.PathUtils;
import akka.actor.UntypedActor;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.mime.MimeTypesFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * Created by lowery on 7/21/16.
 */
public class FileReader extends UntypedActor {
    private Tika tika = new Tika();
    private String type;

    public void onReceive(Object message) throws Throwable {
        if (message instanceof File) {
            File file = (File) message;

            if (file.isDirectory()) {

            } else if (file.isFile()) {
                type = tika.detect(file);
            }
        } else if (message instanceof URL) {
            type = tika.detect((URL) message);
        } else if (message instanceof InputStream) {
            type = tika.detect((InputStream) message);
        } else if (message instanceof String) {
            String str = (String) message;
            if (isValidPath(str)) {
                File file = new File(str);

                if (file.isFile() || file.isDirectory())
                    self().tell(file, self()); // make sure it is a file or directory (urls are also valid paths)

            }

            if (isValidUrl(str)) {
                self().tell(new URL(str), self());
            }

            // Unrecognized input
        }

        if (type != null) {
            System.out.println(type);
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {

            return false;
        }

        return true;
    }

    private boolean isValidPath(String path) {
        try {
                Paths.get(path);
            } catch (InvalidPathException |  NullPointerException ex) {
                return false;
            }
            return true;
        }
}
