package net.goldenpi.parcels;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

final class Tools {

    private static final int READ_BUFFER_SIZE = 8192 * 10 * 10;

    static void readFile(String file, ParcelsFinder.LineProcessor lineProcessor) throws IOException {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(file), READ_BUFFER_SIZE)) {
            while ((line = br.readLine()) != null) {
                lineProcessor.processLine(line);
            }
        }
    }
}
