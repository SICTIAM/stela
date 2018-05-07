package fr.sictiam.stela.pesservice.model.util;

import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum AuthorizationContextClasses {
    EIDAS_LOW("http://eidas.europa.eu/LoA/low"),
    EIDAS_SUBSTANTIAL("http://eidas.europa.eu/LoA/substantial");

    private final String value;

    AuthorizationContextClasses(String s) {
        value = s;
    }

    public String getValue() {
        return this.value;
    }

    static public AuthorizationContextClasses getByValue(String s) {
        if (StringUtils.isEmpty(s)) return null;
        Optional<AuthorizationContextClasses> opt = Arrays.stream(AuthorizationContextClasses.values())
                .filter(authorizationContextClasses -> authorizationContextClasses.getValue().equals(s))
                .findFirst();
        return opt.orElseGet(null);
    }
}
