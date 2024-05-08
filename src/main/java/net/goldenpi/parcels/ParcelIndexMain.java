package net.goldenpi.parcels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.Optional;

public class ParcelIndexMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParcelIndexMain.class);

    public static void main(String[] args) {
        if (args.length == 2) {
            if ("update".equals(args[0])) {
                buildParcelsIndex(Optional.of(args[1]));
                return;
            }
            String parcelsToFindFile = args[0];
            String parcelsIndexFile = args[1];
            System.out.printf("Looking for parcels to find from " + parcelsToFindFile + " using %s...\n", parcelsIndexFile);
            ParcelsFinder.findParcels(parcelsToFindFile, parcelsIndexFile);
        } else if (args.length == 0) {
            buildParcelsIndex(Optional.empty());
        } else {
            System.err.println("Invalid number of arguments: " + args.length);
        }
    }

    private static void buildParcelsIndex(Optional<String> optionalDateToUpdate) {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            downloadAndBuildIndex(httpClient, optionalDateToUpdate);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            LOGGER.error("Failed to build parcels index: {}", e.getMessage(), e);
        }
    }

    private static void downloadAndBuildIndex(HttpClient httpClient, Optional<String> optionalDateToUpdate)
            throws IOException, URISyntaxException, InterruptedException {
        var updateParcels = optionalDateToUpdate.isPresent();
        UrlsProvider.processURIs(new ParcelURIProcessor(httpClient, updateParcels), optionalDateToUpdate);
        System.out.println("Done");
    }

}
