package fr.sictiam.stela.pesservice.model.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotificationAttachement {
    @JsonProperty
    private String title;

    @JsonProperty
    private String color;

    @JsonProperty
    private String fallback;

    @JsonProperty
    private List<Field> fields;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    static public class Field {
        @JsonProperty
        private Boolean _short = true;

        @JsonProperty
        private String title;

        @JsonProperty
        private String value;

        public Boolean get_short() {
            return _short;
        }

        public void set_short(Boolean _short) {
            this._short = _short;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
