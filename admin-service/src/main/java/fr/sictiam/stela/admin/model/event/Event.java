package fr.sictiam.stela.admin.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = LocalAuthorityEvent.class, name = "LocalAutorityCreation") })
public abstract class Event {

    private String type;

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String origin;

    public Event(String type) {
        this.type = type;
        this.origin = "admin-service";
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
