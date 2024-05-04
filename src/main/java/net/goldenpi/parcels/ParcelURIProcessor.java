package net.goldenpi.parcels;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

final class ParcelURIProcessor implements UrlsProvider.URIProcessor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PARCELS_OUTPUT_FILE = "parcels-index.csv";

    private final HttpClient httpClient;

    private final Map<ParcelInfo, ParcelInfo> parcelInfoCacheHoldingReferencesToSaveMemory = new HashMap<>();

    private final Map<String, ParcelInfo> parcelInfoByParcelId = new HashMap<>();

    ParcelURIProcessor(HttpClient httpClient) throws IOException {
        this.httpClient = httpClient;
        Files.writeString(Paths.get(PARCELS_OUTPUT_FILE), "");
    }

    @Override
    public void newURI(String date, String department, URI uri) throws IOException, InterruptedException {
        System.out.printf("Downloading %s...\n", uri);
        String parcelJson = downloadFile(uri, httpClient);

        if ("".equals(parcelJson)) {
            System.out.printf("No parcel found for date %s and department %s%n", date, department);
            return;
        }

        ParcelResponseFeatures parcelResponseFeatures = objectMapper.readValue(parcelJson, new TypeReference<>() {
        });

        AtomicInteger countParcels = new AtomicInteger(0);

        parcelResponseFeatures.iterateOnParcelIds(parcelId -> {
            if (parcelInfoByParcelId.containsKey(parcelId)) {
                return;
            }

            ParcelInfo parcelInfo = new ParcelInfo(date, department);
            parcelInfoCacheHoldingReferencesToSaveMemory.putIfAbsent(parcelInfo, parcelInfo);

            parcelInfoByParcelId.put(parcelId, parcelInfoCacheHoldingReferencesToSaveMemory.get(parcelInfo));
            countParcels.incrementAndGet();
        });

        System.out.printf("done processing date %s and department %s - %d parcels.\n", date, department, countParcels.get());
        System.out.printf("Holding %d parcels\n", parcelInfoByParcelId.size());
    }

    @Override
    public void endDepartment(String department) throws IOException {
        String parcelsCsv = getParcelsCsv();

        Files.writeString(Paths.get(PARCELS_OUTPUT_FILE), parcelsCsv, StandardOpenOption.APPEND);

        parcelInfoByParcelId.clear();
        parcelInfoCacheHoldingReferencesToSaveMemory.clear();
    }

    private String getParcelsCsv() {
        final StringBuilder parcelIndexOutput = new StringBuilder();

        for (Map.Entry<String, ParcelInfo> stringParcelInfoEntry : parcelInfoByParcelId.entrySet()) {
            String parcelId = stringParcelInfoEntry.getKey();
            ParcelInfo parcelInfo = stringParcelInfoEntry.getValue();
            parcelIndexOutput
                    .append(parcelId).append(",")
                    .append(parcelInfo.date()).append(",")
                    .append(parcelInfo.department()).append("\n");
        }

        return parcelIndexOutput.toString();
    }

    private static String downloadFile(URI uri, HttpClient httpClient)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        int statusCode = response.statusCode();
        if (statusCode == 404) {
            return "";
        }

        if (statusCode != 200) {
            throw new IOException("URI " + uri.toString() + " returned status code " + statusCode);
        }

        return gunzip(response.body());
    }

    private static String gunzip(byte[] bytes) throws IOException {
        try (ByteArrayInputStream in0 = new ByteArrayInputStream(bytes);
             GZIPInputStream in = new GZIPInputStream(in0);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        }
    }

}
