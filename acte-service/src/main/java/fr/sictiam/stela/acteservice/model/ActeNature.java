package fr.sictiam.stela.acteservice.model;

public enum ActeNature {
    DELIBERATIONS ("01", "DE"),
    ARRETES_REGLEMENTAIRES ("02", "AR"),
    ARRETES_INDIVIDUELS ("03", "AI"),
    CONTRATS_ET_CONVENTIONS("04", "CC"),
    DOCUMENTS_BUDGETAIRES_ET_FINANCIERS("05", "BF"),
    AUTRES("06", "AU");

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
