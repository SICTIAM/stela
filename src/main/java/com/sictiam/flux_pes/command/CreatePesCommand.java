package com.sictiam.flux_pes.command;

public class CreatePesCommand {

    private String titre;

    public CreatePesCommand(String titre) {
        this.titre = titre;
    }

    public String getTitre() {
        return titre;
    }
}
