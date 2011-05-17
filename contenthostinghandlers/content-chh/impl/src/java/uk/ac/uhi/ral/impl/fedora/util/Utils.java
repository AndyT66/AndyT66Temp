/* CVS Header
   $
   $
*/

package uk.ac.uhi.ral.impl.fedora.util;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.uhi.ral.DigitalItemInfo;

import java.io.*;
import java.util.HashMap;

import fedora.fedoraSystemDef.foxml.DatastreamType;
import fedora.fedoraSystemDef.foxml.StateType;
import fedora.fedoraSystemDef.foxml.DatastreamVersionType;
import fedora.fedoraSystemDef.foxml.XmlContentType;
import info.fedora.definitions.x1.x0.types.Datastream;

public class Utils {
  /**
   * Dumps an XML document to disk.
   *
   * @param doc The document to dump to disk
   * @param filePath The full path and name of the file
   */
  public static void dumpXML(XmlObject doc, String filePath) {
    HashMap<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
    namespaces.put("http://www.nsdl.org/ontologies/relationships#", "myns");
    XmlOptions xmlOptions = new XmlOptions();
    xmlOptions.setSavePrettyPrint();
    xmlOptions.setSavePrettyPrintIndent(2);
    //xmlOptions.setUseDefaultNamespace();
    //xmlOptions.setSaveAggressiveNamespaces();
    xmlOptions.setSaveSuggestedPrefixes(namespaces);
    //xmlOptions.setSaveNamespacesFirst();

    try {
      doc.save(new File(filePath), xmlOptions);
    }
    catch(Exception e) {
    }
  }

  /**
   * Returns an XMLBeans document as a String with full Fedora/RDF namespace support
   *
   * @param doc The document to parse to a String
   * @return String version of the document. This will contain prefixes for rdf and myns
   */
  public static String xmlToString(XmlObject doc) {
    HashMap<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
    namespaces.put("http://www.nsdl.org/ontologies/relationships#", "myns");
    XmlOptions xmlOptions = new XmlOptions();
    xmlOptions.setSaveSuggestedPrefixes(namespaces);
    xmlOptions.setSaveAggressiveNamespaces();
    StringWriter out = new StringWriter();
    try {
      doc.save(out, xmlOptions);
      return out.toString();
    }
    catch(Exception e) {
      return null;
    }
  }

  public static byte[] getContentBytes(InputStream is) {
    int bytesRead = 0;
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream bytes = new ByteArrayOutputStream(2048);
    try {
      while ((bytesRead = is.read(buffer)) != -1) {
        bytes.write(buffer, 0, bytesRead);
      }

      return bytes.toByteArray();
    }
    catch (IOException e) {
      return null;
    }
  }

}
