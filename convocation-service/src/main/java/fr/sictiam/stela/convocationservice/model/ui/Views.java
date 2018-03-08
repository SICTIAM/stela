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
            ConvocationResponseViewPrivate, ExternalUserView, QuestionResponseViewPublic, QuestionView {

    }

    public interface ConvocationResponseViewPublic {

    }

    public interface ConvocationResponseViewPrivate {

    }

    public interface AssemblyTypeView
            extends ExternalUserViewPublic, AssemblyTypeViewPublic, AssemblyTypeViewPrivate, ConvocationViewPublic {

    }

    public interface AssemblyTypeViewPublic {

    }

    public interface AssemblyTypeViewPrivate {

    }

    public interface LocalAuthorityView {

    }

    public interface ExternalUserView extends ExternalUserViewPrivate, ExternalUserViewPublic {

    }

    public interface ExternalUserViewPublic {

    }

    public interface ExternalUserViewPrivate {

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