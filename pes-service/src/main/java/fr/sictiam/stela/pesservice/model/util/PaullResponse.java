package fr.sictiam.stela.pesservice.model.util;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "status", "status_message", "data" })
public class PaullResponse {

    String status;
    String status_message;
    Object data;

    public PaullResponse() {

    }

    public PaullResponse(String status, String status_message, Object data) {
        this.status = status;
        this.status_message = status_message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus_message() {
        return status_message;
    }

    public void setStatus_message(String status_message) {
        this.status_message = status_message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PaullResponse{" +
                "status='" + status + '\'' +
                ", status_message='" + status_message + '\'' +
                ", data=" + data +
                '}';
    }
}