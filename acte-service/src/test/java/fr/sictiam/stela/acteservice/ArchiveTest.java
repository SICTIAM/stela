package fr.sictiam.stela.acteservice;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ArchiveTest {

    private final String PASTEL_LOGIN = "stela";
    private final String PASTELL_PASSWORD = "stela05";
    private final String PASTELL_SITE = "https://pastell.partenaires.libriciel.fr";


    @Test
    public void simpleTest() {
        assertThat(true, is(true));
    }
}
