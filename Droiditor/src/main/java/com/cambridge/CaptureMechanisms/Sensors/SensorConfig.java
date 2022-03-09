package com.cambridge.CaptureMechanisms.Sensors;

import android.util.Log;

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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SensorConfig extends CaptureConfigBase implements IConfig {

    public class SensorObject {
        public String name = "";
        public int id;
    }

    public ArrayList<SensorObject> sensors = new ArrayList<>();


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

                    if (captureMechanismName.equals(SensorCapture.class.getSimpleName().toLowerCase())) {

                        enabled = Boolean.parseBoolean(mechanismElement.getAttribute("enabled"));
                        offloadData = Boolean.parseBoolean(mechanismElement.getAttribute("offload_data"));
                        output_dir = ((Element) mechanismElement.getElementsByTagName("output_directory").item(0)).getTextContent();
                        filename_prefix = ((Element) mechanismElement.getElementsByTagName("filename_prefix").item(0)).getTextContent();

                        NodeList sensorlist = mechanismElement.getElementsByTagName("sensor");
                        for (int x = 0; x < sensorlist.getLength(); x++) {
                            Node sensorNode = sensorlist.item(x);
                            if (sensorNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element sensorElement = (Element) sensorNode;
                                SensorObject sensorObject = new SensorObject();
                                sensorObject.id = Integer.parseInt(((Element) sensorElement.getElementsByTagName("id").item(0)).getTextContent());
                                sensors.add(sensorObject);

                            }
                        }
                    }

                }

            }

            Log.e("SensorConfig", "added all sensors!");

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

}
