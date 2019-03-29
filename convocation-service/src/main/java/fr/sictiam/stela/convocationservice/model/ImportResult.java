package fr.sictiam.stela.convocationservice.model;

import java.util.List;

public class ImportResult {

    private int processedCount;

    private int errorsCount;

    private List<Error> errors;

    public ImportResult(int processedCount, int errorsCount, List<Error> errors) {
        this.processedCount = processedCount;
        this.errorsCount = errorsCount;
        this.errors = errors;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getErrorsCount() {
        return errorsCount;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public static class Error {

        private String field;
        private String message;
        private int line;

        public Error(int line, String field, String message) {
            this.line = line;
            this.field = field;
            this.message = message;
        }

        public int getLine() {
            return line;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
