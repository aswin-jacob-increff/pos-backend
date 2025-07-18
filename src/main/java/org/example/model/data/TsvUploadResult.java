package org.example.model.data;

import java.util.List;

public class TsvUploadResult {
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<String> errors;
    private List<String> warnings;
    private List<?> parsedForms; // Generic list to hold any type of parsed forms

    public TsvUploadResult() {
        this.errors = new java.util.ArrayList<>();
        this.warnings = new java.util.ArrayList<>();
    }

    public TsvUploadResult(int totalRows, int successfulRows, int failedRows, List<String> errors, List<String> warnings) {
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
        this.errors = errors != null ? errors : new java.util.ArrayList<>();
        this.warnings = warnings != null ? warnings : new java.util.ArrayList<>();
    }

    // Getters and Setters
    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessfulRows() {
        return successfulRows;
    }

    public void setSuccessfulRows(int successfulRows) {
        this.successfulRows = successfulRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getParsedForms() {
        return (List<T>) parsedForms;
    }

    public void setParsedForms(List<?> parsedForms) {
        this.parsedForms = parsedForms;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void incrementSuccessful() {
        this.successfulRows++;
    }

    public void incrementFailed() {
        this.failedRows++;
    }

    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Upload Summary: ");
        summary.append(successfulRows).append(" successful, ");
        summary.append(failedRows).append(" failed out of ");
        summary.append(totalRows).append(" total rows");
        
        if (!warnings.isEmpty()) {
            summary.append(" (").append(warnings.size()).append(" warnings)");
        }
        
        return summary.toString();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
} 