package net.goldenpi.parcels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;

public class ParcelIndexMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParcelIndexMain.class);

    public static void main(String[] args) {
        if (args.length == 2) {
            String parcelsToFindFile = args[0];
            String parcelsIndexFile = args[1];
            System.out.printf("Looking for parcels to find from " + parcelsToFindFile + " using %s...\n", parcelsIndexFile);
            ParcelsFinder.findParcels(parcelsToFindFile, parcelsIndexFile);
        } else if (args.length == 0) {
            buildParcelsIndex();
        } else {
            System.err.println("Invalid number of arguments: " + args.length);
        }
    }

    private static void buildParcelsIndex() {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            downloadAndBuildIndex(httpClient);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            LOGGER.error("Failed to build parcels index: {}", e.getMessage(), e);
        }
    }

    private static void downloadAndBuildIndex(HttpClient httpClient)
            throws IOException, URISyntaxException, InterruptedException {
        UrlsProvider.processURIs(new ParcelURIProcessor(httpClient));
        System.out.println("Done");
    }

}
