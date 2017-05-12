package fr.sictiam.stela.apigateway.dto;

public class PesDto {

    private String pesId;

    public PesDto() {
    }

    public PesDto(String pesId) {
        this.pesId = pesId;
    }

    public String getPesId() {
        return pesId;
    }
}
