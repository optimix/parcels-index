package net.goldenpi.parcels;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

final class UrlsProvider {

    private static final String BEFORE_2021_BASE_URL = "https://files.data.gouv.fr/cadastre/etalab-cadastre/";
    private static final String AFTER_2021_BASE_URL = "https://cadastre.data.gouv.fr/data/etalab-cadastre/";
    private static final List<String> BASE_URLS = Arrays.asList(BEFORE_2021_BASE_URL, AFTER_2021_BASE_URL);

    private static final int MIN_DEPARTMENT = 1;
    private static final int MAX_CONSECUTIVE_DEPARTMENT = 95;

    private static final String PARCEL_SUFFIX = "${date}/geojson/departements/${department}/cadastre-${department}-parcelles.json.gz";

    interface URIProcessor {
        void newURI(String date, String department, URI uri) throws IOException, InterruptedException;

        void endDepartment(String department) throws IOException;
    }

    public static void processURIs(URIProcessor uriProcessor)
            throws IOException, URISyntaxException, InterruptedException {
        final List<String> departments = getDepartments();

        final Map<String, List<String>> datesByBaseUrl = new HashMap<>();

        for (String department : departments) {
            HashMap<String, String> values = new HashMap<>();
            values.put("department", department);

            for (String baseUrl : BASE_URLS.reversed()) {
                if (!datesByBaseUrl.containsKey(baseUrl)) {
                    List<String> dates = getDates(baseUrl);
                    datesByBaseUrl.put(baseUrl, dates);
                }

                List<String> dates = datesByBaseUrl.get(baseUrl);
                for (String date : dates.reversed()) {
                    values.put("date", date);

                    String parcelSuffix = new StringSubstitutor(values).replace(PARCEL_SUFFIX);
                    URI uri = new URI(baseUrl + parcelSuffix);
                    uriProcessor.newURI(date, department, uri);
                }
            }

            uriProcessor.endDepartment(department);
        }
    }

    private static List<String> getDepartments() {
        final List<String> departments = new ArrayList<>();
        for (int d = MIN_DEPARTMENT; d <= MAX_CONSECUTIVE_DEPARTMENT; d++) {
            if (d == 20) { //20 = 2A & 2B
                continue;
            }

            String zeroPadded = StringUtils.leftPad(String.valueOf(d), 2, '0');
            departments.add(zeroPadded);
        }
        departments.addAll(Arrays.asList("2A", "2B", "971", "972", "973", "974", "976"));
        return departments;
    }

    private static List<String> getDates(String baseUrl) throws IOException {
        List<String> dates = new ArrayList<>();

        Document document = Jsoup.connect(baseUrl).get();

        for (Element aElement : document.select("a")) {
            String possibleDate = aElement.text().replaceAll("/$", "");
            if (possibleDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                dates.add(possibleDate);
            }
        }

        return dates;
    }

}
