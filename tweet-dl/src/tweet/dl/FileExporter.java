/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tweet.dl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import twitter4j.Status;

/**
 *
 * @author mespinozas
 */
public class FileExporter {
    
        
    public static final String outputName = "./output/Output-";
    public static final String outputFileExtention = ".xml";
    
    private static FileExporter instance;
    
    private FileExporter() {
        this.documentBuilderFactor = new ThreadLocal<DocumentBuilderFactory>(){
            @Override
            public DocumentBuilderFactory  initialValue(){
                return  DocumentBuilderFactory.newInstance();
            }
        };
        
    }
    
    public static FileExporter getInstance()
    {
        if(instance==null)
            instance = new FileExporter();
        return instance;
    }
    
    /**
     *
     * @param number
     * @param newStatus
     */
    public void createXML(int number, Status newStatus) {
        try 
        {
            int fileNumber = number/TweetDl.fileSize;
            DocumentBuilderFactory docFactory = documentBuilderFactor.get();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            ResourceBundle bundle = ResourceBundle.getBundle("properties/ExportFields");

            Document document = docBuilder.newDocument();
            Element rootElement = document.createElement(bundle.getString("RootElementNameKey"));
            document.appendChild(rootElement);
            
            Element status = document.createElement(bundle.getString("StatusNameKey"));
            status.setAttribute(bundle.getString("CaptureCountKey"), number+"");
            rootElement.appendChild(status);
            
            Element id = document.createElement(bundle.getString("IdNameKey"));
            id.appendChild(document.createTextNode(newStatus.getId()+""));
            status.appendChild(id);
            
            Element text = document.createElement(bundle.getString("TextNameKey"));
            text.appendChild(document.createTextNode(stripNonValidXMLCharacters(newStatus.getText())));
            status.appendChild(text);
            
            Element user = document.createElement(bundle.getString("UserScreenNameKey"));
            user.appendChild(document.createTextNode(newStatus.getUser().getScreenName()));
            status.appendChild(user);
            
            Element name = document.createElement(bundle.getString("UserNameKey"));
            name.appendChild(document.createTextNode(newStatus.getUser().getName()));
            status.appendChild(name);
            
            Element contributors = document.createElement(bundle.getString("ContributorsNameKey"));
            contributors.appendChild(document.createTextNode(Arrays.toString(newStatus.getContributors())));
            status.appendChild(contributors);
            
            Element createdAt = document.createElement(bundle.getString("CreatedAtNameKey"));
            createdAt.appendChild(document.createTextNode(newStatus.getCreatedAt().toString()));
            status.appendChild(createdAt);
                        
            Element lang = document.createElement(bundle.getString("LangNameKey"));
            lang.appendChild(document.createTextNode(newStatus.getLang()));
            status.appendChild(lang);
                        
            Element statusSource = document.createElement(bundle.getString("SourceNameKey"));
            statusSource.appendChild(document.createTextNode(newStatus.getSource()));
            status.appendChild(statusSource);
            
            if(newStatus.getPlace()!=null)
            {
                Element country = document.createElement(bundle.getString("CountryNameKey"));
                country.appendChild(document.createTextNode(newStatus.getPlace().getCountry()));
                status.appendChild(country);
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            
            StreamResult result = new StreamResult(new File(outputName+fileNumber+outputFileExtention));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);
            //System.out.println("File saved!");
        } 
        catch (ParserConfigurationException | TransformerException e) {
            System.out.print("Error en "+number);
              e.printStackTrace();
        }
    }
    
    /**
     *
     * @param filename
     */
    public void readXMLFile(String filename) {
        try {

            File fXmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName("staff");

            System.out.println("----------------------------");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    /*
                    System.out.println("Staff id : " + eElement.getAttribute("id"));
                    System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
                    System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
                    System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    final ThreadLocal<DocumentBuilderFactory> documentBuilderFactor;
    
    /**
     *
     * @param number
     * @param newStatus
     */
    public void appendStatus(int number,Status newStatus)
    {
        try {
            int fileNumber = number/TweetDl.fileSize;
            //synchronized (fw) {
            DocumentBuilderFactory documentBuilderFactory = documentBuilderFactor.get();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            ResourceBundle bundle = ResourceBundle.getBundle("properties/ExportFields");
            
            Document document = documentBuilder.parse(outputName+fileNumber+outputFileExtention);
            Element root = document.getDocumentElement();
            
            Element status = document.createElement(bundle.getString("StatusNameKey"));
            status.setAttribute(bundle.getString("CaptureCountKey"), number+"");
            root.appendChild(status);
            
            Element id = document.createElement(bundle.getString("IdNameKey"));
            id.appendChild(document.createTextNode(newStatus.getId()+""));
            status.appendChild(id);
            
            Element text = document.createElement(bundle.getString("TextNameKey"));
            text.appendChild(document.createTextNode(newStatus.getText()));
            status.appendChild(text);
            
            Element user = document.createElement(bundle.getString("UserScreenNameKey"));
            user.appendChild(document.createTextNode(newStatus.getUser().getScreenName()));
            status.appendChild(user);
            
            Element name = document.createElement(bundle.getString("UserNameKey"));
            name.appendChild(document.createTextNode(newStatus.getUser().getName()));
            status.appendChild(name);
            
            Element contributors = document.createElement(bundle.getString("ContributorsNameKey"));
            contributors.appendChild(document.createTextNode(Arrays.toString(newStatus.getContributors())));
            status.appendChild(contributors);
            
            Element createdAt = document.createElement(bundle.getString("CreatedAtNameKey"));
            createdAt.appendChild(document.createTextNode(newStatus.getCreatedAt().toString()));
            status.appendChild(createdAt);
            
            Element lang = document.createElement(bundle.getString("LangNameKey"));
            lang.appendChild(document.createTextNode(newStatus.getLang()));
            status.appendChild(lang);
                        
            Element statusSource = document.createElement(bundle.getString("SourceNameKey"));
            statusSource.appendChild(document.createTextNode(newStatus.getSource()));
            status.appendChild(statusSource);
            
            if(newStatus.getPlace()!=null)
            {
                Element country = document.createElement(bundle.getString("CountryNameKey"));
                country.appendChild(document.createTextNode(newStatus.getPlace().getCountry()));
                status.appendChild(country);
            }
            
            DOMSource source = new DOMSource(document);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamResult result = new StreamResult(outputName+fileNumber+outputFileExtention);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
            System.out.println("Error en "+number);
            Logger.getLogger(FileExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String stripNonValidXMLCharacters(String in) {
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) 
            return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }     
}


    
