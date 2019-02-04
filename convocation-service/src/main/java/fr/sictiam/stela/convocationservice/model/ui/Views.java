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

    public interface AssemblyTypeViewPublic extends LocalAuthorityViewPublic, UserViewPublic {

    }

    public interface AssemblyTypeViewPrivate {

    }

    public interface LocalAuthorityViewPublic {

    }

    public interface LocalAuthorityView extends LocalAuthorityViewPublic {

    }

    public interface UserView extends UserViewPrivate, UserViewPublic {

    }

    public interface UserViewPublic {

    }

    public interface UserViewPrivate {

    }

    public interface UserLocalAuthorityAssemblyTypeView extends UserViewPublic, LocalAuthorityView, AssemblyTypeViewPublic {

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

    public interface Public {
    }

    public interface Search extends Public {
    }

    public interface SearchRecipient extends Search, RecipientInternal {
    }

    public interface SearchAssemblyType extends Search, AssemblyTypeInternal {
    }

    public interface SearchSentConvocation extends Search, Convocation {
    }

    public interface Recipient extends Public {
    }

    public interface RecipientInternal extends Recipient {
    }

    public interface RecipientPrivate extends RecipientInternal {
    }

    public interface AssemblyType extends Public {
    }

    public interface AssemblyTypeInternal extends AssemblyType {
    }

    public interface LocalAuthority extends Public {
    }

    public interface Convocation extends Recipient, LocalAuthority, QuestionView {
    }

    public interface ConvocationInternal extends Convocation {
    }


}