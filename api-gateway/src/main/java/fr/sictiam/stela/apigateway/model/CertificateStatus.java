package fr.sictiam.stela.apigateway.model;

import org.springframework.util.StringUtils;

import java.util.Arrays;

public enum CertificateStatus {
    VALID("valid"),
    EXPIRED("10"),
    REVOKED("23"),
    OTHER("other");

    final String value;

    CertificateStatus(String s) {
        value = s;
    }

    public static CertificateStatus getByValue(String value) {
        if (StringUtils.isEmpty(value)) return CertificateStatus.VALID;
        return Arrays.stream(CertificateStatus.values())
                .filter(certificateStatus -> certificateStatus.value.equals(value))
                .findFirst().orElse(CertificateStatus.OTHER);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
