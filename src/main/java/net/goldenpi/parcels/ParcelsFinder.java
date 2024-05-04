package net.goldenpi.parcels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

final class ParcelsFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParcelsFinder.class);

    private static final int READ_BUFFER_SIZE = 8192 * 10 * 10;

    interface LineProcessor {
        void processLine(String line) throws IOException;
    }

    static void findParcels(String parcelsToFindPath, String parcelsIndexFile) {
        try {
            ParcelIdsHolder parcelIdsHolder = readParcelIds(parcelsIndexFile);
            findParcelIds(parcelsToFindPath, parcelIdsHolder);
        } catch (IOException e) {
            LOGGER.error("Failed to find parcels: {}", e.getMessage(), e);
        }
    }

    private static void findParcelIds(String parcelsToFindFile, ParcelIdsHolder parcelIdsHolder) throws IOException {
        System.out.println("Finding parcels...");

        readFile(parcelsToFindFile, parcelId -> {
            ParcelInfo parcelInfo = parcelIdsHolder.getParcel(parcelId);
            if (parcelInfo == null) {
                return;
            }

            String parcelsCsv = parcelId + "," + parcelInfo.date() + "," + parcelInfo.department() + "\n";
            Files.writeString(Paths.get("parcels-matches.csv"), parcelsCsv, StandardOpenOption.APPEND);
        });

        System.out.println("Done finding parcels.");
    }

    private static ParcelIdsHolder readParcelIds(String parcelsIndexFile) throws IOException {
        ParcelIdsHolder parcelIdsHolder = new ParcelIdsHolder();

        readFile(parcelsIndexFile, line -> {
            String[] cells = line.split(",");
            if (cells.length != 3) {
                return;
            }
            parcelIdsHolder.holdParcelId(cells[0], cells[1], cells[2]);
        });

        System.out.println("Done reading parcels in memory");

        return parcelIdsHolder;
    }

    private static void readFile(String file, LineProcessor lineProcessor) throws IOException {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(file), READ_BUFFER_SIZE)) {
            while ((line = br.readLine()) != null) {
                lineProcessor.processLine(line);
            }
        }
    }

    static final class ParcelIdsHolder {

        private static final Map<String, ParcelInfo> parcelIdsMap = new HashMap<>();

        void holdParcelId(String parcelId, String date, String department) {
            parcelIdsMap.put(parcelId, new ParcelInfo(date, department));

            if (parcelIdsMap.size() % 5000000 == 0) {
                System.out.println("parcels in memory: " + parcelIdsMap.size());
            }
        }

        ParcelInfo getParcel(String parcelId) {
            return parcelIdsMap.get(parcelId);
        }
    }
}
