package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.StringUtils;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PesHistoryError implements Serializable {

    private String title;

    private String message;

    private String source;

    public PesHistoryError(String title, String message, String source) {
        this.title = title;
        this.message = message;
        this.source = source;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getMessage () {
        return message;
    }

    public void setMessage (String message) {
        this.message = message;
    }

    public String getSource () { return source; }

    public void setSource (String source) { this.source = source; }

    @JsonIgnore
    public String errorText () {
        return (!StringUtils.isEmpty(title) ? title + " : " : "") + (!StringUtils.isEmpty(message) ? message : "") + (!StringUtils.isEmpty(source) ? " (" + source + ")" : "");
    }

    @Override
    public String toString () {
        return "PesHistoryError { title='" + title + "\', message='" + message + "\' , source='" + message + "\' }";
    }
}
