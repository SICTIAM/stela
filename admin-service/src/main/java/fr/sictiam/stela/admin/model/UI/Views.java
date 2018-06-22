package fr.sictiam.stela.admin.model.UI;

public class Views {
    public interface LocalAuthorityView extends LocalAuthorityViewPrivate, LocalAuthorityViewPublic,
            WorkGroupViewPublic, WorkGroupViewChain, ProfileViewPublic, ProfileViewChain, AgentViewPublic {

    }

    public interface LocalAuthorityViewBasic {

    }

    public interface LocalAuthorityViewPublic extends LocalAuthorityViewBasic {

    }

    public interface LocalAuthorityViewPrivate {

    }

    public interface WorkGroupView extends WorkGroupViewPublic, WorkGroupViewPrivate, WorkGroupViewChain {
    }

    public interface WorkGroupViewPublic {
    }

    public interface WorkGroupViewPrivate {
    }

    public interface WorkGroupViewChain {
    }

    public interface ProfileView extends ProfileViewPublic, ProfileViewPrivate, ProfileViewChain,
            LocalAuthorityViewPublic, WorkGroupViewPublic, AgentViewPublic, CertificateViewPublic {

    }

    public interface ProfileViewPrivate {

    }

    public interface ProfileViewPublic {

    }

    public interface ProfileViewChain {

    }

    public interface AgentView
            extends AgentViewPublic, AgentViewPrivate, LocalAuthorityViewPublic, ProfileViewPublic, ProfileViewPrivate,
            CertificateViewPublic {

    }

    public interface AgentViewPublic {

    }

    public interface AgentViewPrivate {

    }

    public interface CertificateViewPublic {

    }

    public interface GenericAccountView extends LocalAuthorityView {

    }

}