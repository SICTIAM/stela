package fr.sictiam.signature.utils;

import fr.sictiam.signature.pes.verifier.UnExpectedException;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class DomUtils {
    public static final String NAMESPACE_SPEC_NS = "http://www.w3.org/2000/xmlns/";
    private static final String XML_DSIG_QUALIFIED_NAME = "xmlns:ds";
    private static final String PES_SPEC_NS = "http://www.minefi.gouv.fr/cp/helios/pes_v2/Rev0/aller";
    private static final String PES_QUALIFIED_NAME = "xmlns:n";
    public static final String ETSI_NS = "http://uri.etsi.org/01903/v1.1.1#";
    private static final String XML_ETSI_QUALIFIED_NAME = "xmlns:xad";
    public static final String XML_DSIG_SPEC_NS = "http://www.w3.org/2000/09/xmldsig#";

    public static Element getFCE(Element element, String elementName) {
        if ((element != null) && (elementName != null)) {
            NodeList nodeList = element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (((node instanceof Element)) && (((Element) node).getLocalName().equals(elementName))) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    public static String getFCEC(Element element, String elementName) {
        Element childElement = getFCE(element, elementName);
        return childElement != null ? childElement.getTextContent() : null;
    }

    public static String getFCEA(Element element, String elementName, String attribute) {
        Element childElement = getFCE(element, elementName);
        return childElement != null ? childElement.getAttribute(attribute) : null;
    }

    public static Node createXMLDSigAndPesNamespaceNode(Document document)
            throws ParserConfigurationException, SAXException, IOException {
        Element namespaceElement = document.createElementNS(null, "namespaceContext");
        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds",
                "http://www.w3.org/2000/09/xmldsig#");
        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:n",
                "http://www.minefi.gouv.fr/cp/helios/pes_v2/Rev0/aller");

        namespaceElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xad",
                "http://uri.etsi.org/01903/v1.1.1#");
        return namespaceElement;
    }

    public static String nodeToString(Element node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty("omit-xml-declaration", "yes");
            t.setOutputProperty("indent", "no");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
        }
        return sw.toString();
    }

    public static String recupXMLBlocWithId(Document doc, String id) {
        try {
            if ("".equals(id)) {
                return nodeToString(
                        (Element) XPathAPI.selectSingleNode(doc, "//*", createXMLDSigAndPesNamespaceNode(doc)));
            }
            return nodeToString((Element) XPathAPI.selectSingleNode(doc,
                    "//*[@Id='" + ((id != null) && (id.startsWith("#")) ? id.substring(1) : id) + "']",
                    createXMLDSigAndPesNamespaceNode(doc)));
        } catch (ParserConfigurationException ex) {
            throw new UnExpectedException(ex);
        } catch (SAXException ex) {
            throw new UnExpectedException(ex);
        } catch (IOException ex) {
            throw new UnExpectedException(ex);
        } catch (TransformerException ex) {
            throw new UnExpectedException(ex);
        }
    }

    public static Element createElement(Document doc, String name, String namespace,
            List<ElementAttribute1> attributeList, String value) {
        if (name != null) {
            Element propNode = doc.createElementNS(namespace, name);
            if (value != null) {
                propNode.setTextContent(value);
            }
            if (attributeList != null) {
                for (ElementAttribute1 attribute : attributeList) {
                    propNode.setAttribute(attribute.getName(), attribute.getValue());
                }
            }
            return propNode;
        }
        return null;
    }

    public static Element createElement(Document doc, String name, List<ElementAttribute1> attributeList,
            String value) {
        if (name != null) {
            Element propNode = doc.createElement(name);
            if (value != null) {
                propNode.setTextContent(value);
            }
            if (attributeList != null) {
                for (ElementAttribute1 attribute : attributeList) {
                    propNode.setAttribute(attribute.getName(), attribute.getValue());
                }
            }
            return propNode;
        }
        return null;
    }

    public static Element createElementNS(Document doc, String namespace, String name,
            List<ElementAttribute1> attributeList, String value) {
        if (name != null) {
            Element propNode = doc.createElementNS(namespace, name);
            if (value != null) {
                propNode.setTextContent(value);
            }
            if (attributeList != null) {
                for (ElementAttribute1 attribute : attributeList) {
                    propNode.setAttributeNS(namespace, attribute.getName(), attribute.getValue());
                }
            }
            return propNode;
        }
        return null;
    }

    public static String recupPrefix(Node pesAllerNode, String nameSpace) {
        if (pesAllerNode.getAttributes() != null) {
            int nb = pesAllerNode.getAttributes().getLength();
            for (int i = 0; i < nb; i++) {
                Node anode = pesAllerNode.getAttributes().item(i);
                if (nameSpace.equalsIgnoreCase(anode.getNodeValue())) {
                    return anode.getNodeName().substring(anode.getNodeName().indexOf("xmlns:"));
                }
            }
        }
        return null;
    }

    public static Element recupElement(Document doc, String elementName, Node nameSpaceNode)
            throws TransformerException {
        return (Element) XPathAPI.selectSingleNode(doc, elementName, nameSpaceNode);
    }

    public static boolean updateNodeAttribute(Document doc, String node, String attribute, String value, Node namespace)
            throws TransformerException {
        Element tmp = recupElement(doc, node, namespace);
        if (tmp != null) {
            tmp.setAttribute(attribute, value);
            return true;
        }
        return false;
    }

    public static class ElementAttribute1 {
        private String name;
        private String value;

        public ElementAttribute1(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static void outputDOM(Document document, File signedDocument, String encoding)
            throws IOException, TransformerConfigurationException, TransformerException {
        FileWriterWithEncoding fileWriterWithEncoding = new FileWriterWithEncoding(signedDocument, encoding);
        try {
            StreamResult streamResult = new StreamResult(fileWriterWithEncoding);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", encoding);
            transformer.transform(new DOMSource(document), streamResult);
        } finally {
            fileWriterWithEncoding.close();
        }
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.utils.DomUtils Java Class
 * Version: 6 (50.0) JD-Core Version: 0.7.1
 */