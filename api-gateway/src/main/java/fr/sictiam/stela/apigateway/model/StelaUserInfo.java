package fr.sictiam.stela.apigateway.model;

import org.oasis_eu.spring.kernel.model.UserInfo;

public class StelaUserInfo extends UserInfo {
    
    private String currentProfile;

    public String getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(String currentProfile) {
        this.currentProfile = currentProfile;
    }
    
    public static StelaUserInfo from(UserInfo in) {
        StelaUserInfo out = new StelaUserInfo();
        out.setName(in.getName());
        out.setNickname(in.getNickname());
        out.setLocale(in.getLocale());
        out.setEmail(in.getEmail());
        out.setEmailVerified(in.isEmailVerified());
        out.setUserId(in.getUserId());
        out.setAddress(in.getAddress());
        out.setBirthdate(in.getBirthdate());
        out.setFamilyName(in.getFamilyName());
        out.setGivenName(in.getGivenName());
        out.setGender(in.getGender());
        out.setPhoneNumber(in.getPhoneNumber());
        out.setPhoneNumberVerified(in.isPhoneNumberVerified() != null ? in.isPhoneNumberVerified() : false);
        out.setPictureUrl(in.getPictureUrl());
        out.setUpdatedAt(in.getUpdatedAt());
        out.setZoneInfo(in.getZoneInfo());

        return out;
    }
}
