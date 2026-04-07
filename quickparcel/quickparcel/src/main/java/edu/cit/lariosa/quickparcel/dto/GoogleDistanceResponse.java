package edu.cit.lariosa.quickparcel.dto;

import java.util.List;

public class GoogleDistanceResponse {
    private String status;
    private List<Row> rows;

    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Row> getRows() { return rows; }
    public void setRows(List<Row> rows) { this.rows = rows; }

    public static class Row {
        private List<Element> elements;
        public List<Element> getElements() { return elements; }
        public void setElements(List<Element> elements) { this.elements = elements; }
    }

    public static class Element {
        private Distance distance;
        private Duration duration;
        private String status;
        public Distance getDistance() { return distance; }
        public void setDistance(Distance distance) { this.distance = distance; }
        public Duration getDuration() { return duration; }
        public void setDuration(Duration duration) { this.duration = duration; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class Distance {
        private String text;
        private long value;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public long getValue() { return value; }
        public void setValue(long value) { this.value = value; }
    }

    public static class Duration {
        private String text;
        private long value;
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public long getValue() { return value; }
        public void setValue(long value) { this.value = value; }
    }
}