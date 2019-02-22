package fr.sictiam.stela.acteservice.service.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class XmlUtils {

    public static <T> T unmarshall(StreamSource xml, Class<T> clazz) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return clazz.cast(unmarshaller.unmarshal(xml));
    }
}
