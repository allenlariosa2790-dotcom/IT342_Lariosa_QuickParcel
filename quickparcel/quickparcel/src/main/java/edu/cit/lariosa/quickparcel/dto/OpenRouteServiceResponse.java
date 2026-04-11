package edu.cit.lariosa.quickparcel.dto;

import lombok.Data;
import java.util.List;

@Data
public class OpenRouteServiceResponse {
    private String status;

    private List<List<Double>> distances;  // matrix of distances in meters
    private List<List<Double>> durations;  // matrix of durations in seconds
    private List<List<Double>> sources;    // source coordinates
    private List<List<Double>> destinations; // destination coordinates

    public String getStatus() { return status; }
    public List<List<Double>> getDistances() { return distances; }
    public List<List<Double>> getDurations() { return durations; }
    public List<List<Double>> getSources() { return sources; }
    public List<List<Double>> getDestinations() { return destinations; }

    public void setStatus(String status) { this.status = status; }
    public void setDistances(List<List<Double>> distances) { this.distances = distances; }
    public void setDurations(List<List<Double>> durations) { this.durations = durations; }
    public void setSources(List<List<Double>> sources) { this.sources = sources; }
    public void setDestinations(List<List<Double>> destinations) { this.destinations = destinations; }
}