package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

public class DownloadMicrosoftStep implements GeneratorStep {

    private static final String URL = "https://fe3.delivery.mp.microsoft.com/ClientWebService/client.asmx/secured";
    private static final String DOWNLOAD_URL_XPATH = "//e:Body/cws:GetExtendedUpdateInfo2Response/cws:GetExtendedUpdateInfo2Result/cws:FileLocations/cws:FileLocation[starts-with(cws:Url, 'http://tlu.dl.delivery.mp.microsoft.com')]/cws:Url";

    private final String packageId;
    private final File output;

    public DownloadMicrosoftStep(final String packageId, final File output) {
        this.packageId = packageId;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Downloading package " + this.packageId + " from Microsoft...");
    }

    @Override
    public void run() throws Exception {
        final Instant createdAt = Instant.now();
        final Instant expiresAt = createdAt.plus(5, ChronoUnit.MINUTES);

        final String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">" +
                "   <s:Header>" +
                "      <a:Action s:mustUnderstand=\"1\">http://www.microsoft.com/SoftwareDistribution/Server/ClientWebService/GetExtendedUpdateInfo2</a:Action>" +
                "      <a:MessageID>urn:uuid:5754a03d-d8d5-489f-b24d-efc31b3fd32d</a:MessageID>" +
                "      <a:To s:mustUnderstand=\"1\">" + URL + "</a:To>" +
                "      <o:Security xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" s:mustUnderstand=\"1\">" +
                "         <Timestamp xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
                "            <Created>" + createdAt + "</Created>" +
                "            <Expires>" + expiresAt + "</Expires>" +
                "         </Timestamp>" +
                "         <wuws:WindowsUpdateTicketsToken xmlns:wuws=\"http://schemas.microsoft.com/msus/2014/10/WindowsUpdateAuthorization\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:id=\"ClientMSA\">" +
                "            <TicketType Name=\"AAD\" Version=\"1.0\" Policy=\"MBI_SSL\" />" +
                "         </wuws:WindowsUpdateTicketsToken>" +
                "      </o:Security>" +
                "   </s:Header>" +
                "   <s:Body>" +
                "      <GetExtendedUpdateInfo2 xmlns=\"http://www.microsoft.com/SoftwareDistribution/Server/ClientWebService\">" +
                "         <updateIDs>" +
                "            <UpdateIdentity>" +
                "               <UpdateID>" + this.packageId + "</UpdateID>" +
                "               <RevisionNumber>1</RevisionNumber>" +
                "            </UpdateIdentity>" +
                "         </updateIDs>" +
                "         <infoTypes>" +
                "            <XmlUpdateFragmentType>FileUrl</XmlUpdateFragmentType>" +
                "         </infoTypes>" +
                "      </GetExtendedUpdateInfo2>" +
                "   </s:Body>" +
                "</s:Envelope>";

        final byte[] response = NetUtils.postInsecure(URL, "application/soap+xml; charset=utf-8", payload.getBytes());

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new ByteArrayInputStream(response));

        final XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceResolver());
        final String downloadUrl = (String) xPath.compile(DOWNLOAD_URL_XPATH).evaluate(doc, XPathConstants.STRING);
        NetUtils.download(downloadUrl, this.output);
    }

    private static class NamespaceResolver implements NamespaceContext {

        @Override
        public String getNamespaceURI(final String prefix) {
            if ("e".equals(prefix)) {
                return "http://www.w3.org/2003/05/soap-envelope";
            } else if ("cws".equals(prefix)) {
                return "http://www.microsoft.com/SoftwareDistribution/Server/ClientWebService";
            }
            return null;
        }

        @Override
        public String getPrefix(final String namespaceURI) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<String> getPrefixes(final String namespaceURI) {
            throw new UnsupportedOperationException();
        }

    }

}
