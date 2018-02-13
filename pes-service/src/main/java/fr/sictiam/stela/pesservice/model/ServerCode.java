package fr.sictiam.stela.pesservice.model;

public enum ServerCode {
    VHPCE11("VHPCE11"), VHPCE31("VHPCE31"), VHPCE51("VHPCE51"), MHPCE11("MHPCE11"), MHPCE21("MHPCE21"), MHPCE31("MHPCE31"), MHPCE41("MHPCE41"), MHPCE51("MHPCE51"), VHICE21("VHICE21");

    final String name;

    ServerCode(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}