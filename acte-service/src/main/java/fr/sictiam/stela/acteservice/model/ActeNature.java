package fr.sictiam.stela.acteservice.model;

public enum ActeNature {
    DELIBERATIONS ("1", "DE"),
    ARRETES_REGLEMENTAIRES ("2", "AR"),
    ARRETES_INDIVIDUELS ("3", "AI"),
    CONTRATS_ET_CONVENTIONS("4", "CC"),
    DOCUMENTS_BUDGETAIRES_ET_FINANCIERS("5", "BF"),
    AUTRES("6", "AU");

    private String code;
    private String abbreviation;

    ActeNature(String code, String abbreviation){
        this.code = code;
        this.abbreviation = abbreviation;
    }

    public String getCode(){
        return this.code;
    }

	public String getAbbreviation() {
		return this.abbreviation;
	}
}
