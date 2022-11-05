package mendes.sutil.dyego.awspresignedpost.tagging;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

public class TaggingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaggingHelper.class);

    private static final JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance(Tagging.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static String marshal(Tagging tagging) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(tagging, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            LOGGER.error("Error while marshalling Tagging to xml", e);
            throw new IllegalStateException(e);
        }
    }
}
