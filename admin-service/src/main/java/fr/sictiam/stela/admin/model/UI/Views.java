package fr.sictiam.stela.admin.model.UI;

public class Views {
    public interface LocalAuthorityView extends LocalAuthorityViewPrivate, LocalAuthorityViewPublic, WorkGroupViewPublic, WorkGroupViewChain, ProfileViewPublic, AgentViewPublic {

    }
    
    public interface LocalAuthorityViewPublic {

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

    public interface ProfileView extends ProfileViewPublic, ProfileViewPrivate, LocalAuthorityViewPublic, WorkGroupViewPublic, AgentViewPublic {

    }

    public interface ProfileViewPrivate {

    }

    public interface ProfileViewPublic {

    }

    public interface AgentView extends AgentViewPublic, AgentViewPrivate {

    }

    public interface AgentViewPublic {

    }

    public interface AgentViewPrivate {

    }

}