package net.goldenpi.parcels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ParcelResponseFeatures {

    public interface ParcelIdProcessor {
        void processParcel(String parcelId);
    }

    public Features features;

    void iterateOnParcelIds(ParcelIdProcessor parcelIdProcessor) {
        for (Feature feature : features) {
            parcelIdProcessor.processParcel(feature.id);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Features extends ArrayList<Feature> {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Feature {
        public String id;
    }
}
