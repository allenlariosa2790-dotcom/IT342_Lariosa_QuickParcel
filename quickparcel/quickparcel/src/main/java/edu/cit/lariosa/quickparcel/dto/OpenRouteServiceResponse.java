package edu.cit.lariosa.quickparcel.dto;

import lombok.Data;
import java.util.List;

@Data
public class OpenRouteServiceResponse {
    private List<List<Double>> distances;  // matrix of distances in meters
    private List<List<Double>> durations;  // matrix of durations in seconds
    private List<List<Double>> sources;    // source coordinates
    private List<List<Double>> destinations; // destination coordinates
}