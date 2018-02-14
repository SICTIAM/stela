package fr.sictiam.stela.pesservice.model.sesile;

public enum ClasseurStatus {
    REFUSED(0), IN_PROGRESS(1), FINALIZED(2), WITHDRAWN(3),;

    private final int value;

    ClasseurStatus(int value) {
        this.value = value;
    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString() {
        return Integer.toString(this.value);
    }
}
