package fr.sictiam.stela.admin.model.UI;

/**
 * Specific view of a profile that is used in the top bar to display the current user's profiles.
 */
public interface ProfileSummary {

    String getUuid();
    String getLocalAuthorityName();
}
