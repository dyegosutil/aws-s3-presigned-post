package mendes.sutil.dyego.awspresignedpost.domain.tagging;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;

public class TaggingHelper {

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
            e.printStackTrace(); // TODO add log.error
            throw new IllegalStateException("Could not generate xml for tagging", e);
        }
    }
}
