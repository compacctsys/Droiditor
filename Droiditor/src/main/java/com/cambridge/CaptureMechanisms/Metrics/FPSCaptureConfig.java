package com.cambridge.CaptureMechanisms.Metrics;

import com.cambridge.Config.CaptureConfigBase;
import com.cambridge.Config.IConfig;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FPSCaptureConfig extends CaptureConfigBase implements IConfig {

    @Override
    public void parse(String config) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(config)));

            Element documentElement = doc.getDocumentElement();
            documentElement.normalize();

            NodeList mechanismNodes = doc.getElementsByTagName("capture-mechanism");

            for (int i = 0; i < mechanismNodes.getLength(); i++) {
                Node mechanismNode = mechanismNodes.item(i);
                if (mechanismNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element mechanismElement = (Element) mechanismNode;
                    String captureMechanismName = mechanismElement.getAttribute("name").toLowerCase();

                    if (captureMechanismName.equals(FPSCapture.class.getSimpleName().toLowerCase())) {
                        enabled = Boolean.parseBoolean(mechanismElement.getAttribute("enabled"));
                        offloadData = Boolean.parseBoolean(mechanismElement.getAttribute("offload_data"));
                        output_dir = ((Element) mechanismElement.getElementsByTagName("output_directory").item(0)).getTextContent();
                        filename_prefix = ((Element) mechanismElement.getElementsByTagName("filename_prefix").item(0)).getTextContent();
                    }

                }

            }


        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

}
