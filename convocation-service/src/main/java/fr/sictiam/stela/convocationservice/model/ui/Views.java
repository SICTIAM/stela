package fr.sictiam.stela.convocationservice.model.ui;

public class Views {

	public interface ConvocationView extends ConvocationViewPublic, ConvocationViewPrivate {

	}

	public interface ConvocationViewPublic {

	}

	public interface ConvocationViewPrivate {

	}

	public interface AssemblyTypeView extends ExternalUserViewPublic, AssemblyTypeViewPublic, AssemblyTypeViewPrivate {

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

}