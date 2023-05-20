package org.pdks.session;


import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.parsers.DOMParser;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Scope(APPLICATION)
@Name("PDFContentCaptureServletResponse")
public class ContentCaptureServletResponse extends HttpServletResponseWrapper {

    private ByteArrayOutputStream contentBuffer;
    private PrintWriter writer;

    public ContentCaptureServletResponse(HttpServletResponse originalResponse) {
        super(originalResponse);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            contentBuffer = new ByteArrayOutputStream();
            writer = new PrintWriter(contentBuffer);
        }
        return writer;
    }

    public String getContent() throws IOException, SAXException, TransformerException {
        getWriter().flush();
        String xhtmlContent = new String(contentBuffer.toByteArray());
        // thead and tbody seam to work in the current version of saucer ?!
        // xhtmlContent = xhtmlContent.replaceAll("<thead>|</thead>|"+"<tbody>|</tbody>","");
        DOMParser parser = new DOMParser();

        parser.setFeature("http://cyberneko.org/html/features/balance-tags",Boolean.TRUE);
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        parser.setFeature("http://cyberneko.org/html/features/override-namespaces",Boolean.TRUE);
        parser.setFeature("http://cyberneko.org/html/features/insert-namespaces",Boolean.TRUE);
        parser.setProperty("http://cyberneko.org/html/properties/namespaces-uri", "http://www.w3.org/1999/xhtml");
        

        parser.parse(new InputSource(new StringReader(xhtmlContent)));

        Document node = parser.getDocument();

        StringWriter sw = new StringWriter();
        Transformer t = null;
        t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));

        return sw.toString();
    }
}

