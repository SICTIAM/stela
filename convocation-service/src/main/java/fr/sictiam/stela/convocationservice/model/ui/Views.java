package fr.sictiam.stela.convocationservice.model.ui;

public class Views {

    public interface ConvocationView
            extends ConvocationViewPublic, ConvocationViewPrivate, LocalAuthorityView, AssemblyTypeView, QuestionView {

    }

    public interface ConvocationViewPublic {

    }

    public interface ConvocationViewPrivate {

    }

    public interface ConvocationResponseView extends ConvocationViewPublic, ConvocationResponseViewPublic,
            ConvocationResponseViewPrivate, UserView, QuestionResponseViewPublic, QuestionView {

    }

    public interface ConvocationResponseViewPublic {

    }

    public interface ConvocationResponseViewPrivate {

    }

    public interface AssemblyTypeView
            extends UserViewPublic, AssemblyTypeViewPublic, AssemblyTypeViewPrivate, ConvocationViewPublic {

    }

    public interface AssemblyTypeViewPublic {

    }

    public interface AssemblyTypeViewPrivate {

    }

    public interface LocalAuthorityView {

    }

    public interface UserView extends UserViewPrivate, UserViewPublic {

    }

    public interface UserViewPublic {

    }

    public interface UserViewPrivate {

    }

    public interface UserLocalAuthorityView extends UserViewPublic, LocalAuthorityView {

    }

    public interface QuestionView {

    }

    public interface QuestionResponseView
            extends QuestionResponseViewPublic, QuestionResponseViewPrivate, QuestionView {

    }

    public interface QuestionResponseViewPublic {

    }

    public interface QuestionResponseViewPrivate {

    }

}