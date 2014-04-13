/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tweet.dl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
    public static final String captureCount = "Capture_Count";
    
    public static final String RootElementName = "Twitter";
    public static final String StatusName = "Status";
    public static final String TextName="Text";
    public static final String UserScreenName="User";
    public static final String UserName="Name";
    public static final String ContributorsName="Contributor";
    public static final String CreatedAtName="CreatedAt";
    public static final String CountryName="Country";
    public static final String IdName="Id";
    public static final String LangName="Lang";
    public static final String PlaceName="Place";
    public static final String SourceName="Source";
        
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

            Document document = docBuilder.newDocument();
            Element rootElement = document.createElement(RootElementName);
            document.appendChild(rootElement);
            
            Element status = document.createElement(StatusName);
            status.setAttribute(captureCount, number+"");
            rootElement.appendChild(status);
            
            Element id = document.createElement(IdName);
            id.appendChild(document.createTextNode(newStatus.getId()+""));
            status.appendChild(id);
            
            Element text = document.createElement(TextName);
            text.appendChild(document.createTextNode(newStatus.getText()));
            status.appendChild(text);
            
            Element user = document.createElement(UserScreenName);
            user.appendChild(document.createTextNode(newStatus.getUser().getScreenName()));
            status.appendChild(user);
            
            Element name = document.createElement(UserName);
            name.appendChild(document.createTextNode(newStatus.getUser().getName()));
            status.appendChild(name);
            
            Element contributors = document.createElement(ContributorsName);
            contributors.appendChild(document.createTextNode(Arrays.toString(newStatus.getContributors())));
            status.appendChild(contributors);
            
            Element createdAt = document.createElement(CreatedAtName);
            createdAt.appendChild(document.createTextNode(newStatus.getCreatedAt().toString()));
            status.appendChild(createdAt);
                        
            Element lang = document.createElement(LangName);
            lang.appendChild(document.createTextNode(newStatus.getLang()));
            status.appendChild(lang);
                        
            Element statusSource = document.createElement(SourceName);
            statusSource.appendChild(document.createTextNode(newStatus.getSource()));
            status.appendChild(statusSource);
            
            if(newStatus.getPlace()!=null)
            {
                Element country = document.createElement(CountryName);
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
            
            Document document = documentBuilder.parse(outputName+fileNumber+outputFileExtention);
            Element root = document.getDocumentElement();
            
            Element status = document.createElement(StatusName);
            status.setAttribute(captureCount, number+"");
            root.appendChild(status);
            
            Element id = document.createElement(IdName);
            id.appendChild(document.createTextNode(newStatus.getId()+""));
            status.appendChild(id);
            
            Element text = document.createElement(TextName);
            text.appendChild(document.createTextNode(newStatus.getText()));
            status.appendChild(text);
            
            Element user = document.createElement(UserScreenName);
            user.appendChild(document.createTextNode(newStatus.getUser().getScreenName()));
            status.appendChild(user);
            
            Element name = document.createElement(UserName);
            name.appendChild(document.createTextNode(newStatus.getUser().getName()));
            status.appendChild(name);
            
            Element contributors = document.createElement(ContributorsName);
            contributors.appendChild(document.createTextNode(Arrays.toString(newStatus.getContributors())));
            status.appendChild(contributors);
            
            Element createdAt = document.createElement(CreatedAtName);
            createdAt.appendChild(document.createTextNode(newStatus.getCreatedAt().toString()));
            status.appendChild(createdAt);
            
            Element lang = document.createElement(LangName);
            lang.appendChild(document.createTextNode(newStatus.getLang()));
            status.appendChild(lang);
                        
            Element statusSource = document.createElement(SourceName);
            statusSource.appendChild(document.createTextNode(newStatus.getSource()));
            status.appendChild(statusSource);
            
            if(newStatus.getPlace()!=null)
            {
                Element country = document.createElement(CountryName);
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
}


    
