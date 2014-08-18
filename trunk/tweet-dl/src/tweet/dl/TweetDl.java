/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package tweet.dl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import tweet.dl.view.MainView;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Scopes;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.SymbolEntity;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.FilterQuery;
import twitter4j.User;


//import View.MainView;

/**
 *
 * @author Meny
 */
public class TweetDl {
    private static int fileSize=100;

    static TwitterStream twitterStream;
    static StatusListener listener;
    
    private static int statusCounter;

    /**
     * @return the fileSize
     */
    public static int getFileSize() {
        return fileSize;
    }

    /**
     * @param aFileSize the fileSize to set
     */
    public static void setFileSize(int aFileSize) {
        fileSize = aFileSize;
    }
    
    private TweetDl instance;
    
    private TweetDl()
    {
        //setFileSize(100);
    }
    
    public TweetDl getInstance()
    {
        if(instance==null)
            return new TweetDl();
        else
            return instance;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        MainView.getInstance().setVisible(true);                
    }
    
    
    public static void startCapture() throws TwitterException
    {
        ResourceBundle bundle = ResourceBundle.getBundle("properties/Autentication");
        ResourceBundle bundleSecret = ResourceBundle.getBundle("properties/private/AutenticationSecret");
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(bundle.getString("CONSUMER_KEY"));
        cb.setOAuthConsumerSecret(bundleSecret.getString("CONSUMER_SECRET"));
        cb.setOAuthAccessToken(bundle.getString("ACCESS_TOKEN"));
        cb.setOAuthAccessTokenSecret(bundleSecret.getString("ACCESS_SECRET"));
        
        statusCounter=1;
        
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        
        
        listener = new StatusListener(){
            @Override
            public void onStatus(Status status) {
                try {
                    User user           = status.getUser( );
                    String userName     = user.getScreenName( );
                    String userLocation = user.getLocation( );
                    String userLang     = user.getLang( );
                    String userTZ       = user.getTimeZone( );
                    int userUTC         = user.getUtcOffset( );
                    
                    System.out.println( userName );
                    System.out.println( userLocation );
                    System.out.println( userLang );
                    System.out.println( userTZ );
                    System.out.println( userUTC );
                    
                    String tLang     = status.getLang( );                    
                    String tText     = status.getText( );
                    if(status.getGeoLocation( )!=null)
                    {
                        String tLocation = status.getGeoLocation( ).toString( ); //if available
                        System.out.println( "Location: "+tLocation );                        
                    }
                    if(status.getPlace( )!=null)
                    {
                        String tPlace    = status.getPlace( ).getFullName( );
                        System.out.println( "Place: "+ tPlace );
                    }
                    System.out.println( tLang );
                    System.out.println( tText );
                    System.out.println( "----------------------------------------------------------" );
                    System.out.println(status.getUser().getName() + " : " + status.getText());
                    
                    //int fileSelect = statusCounter/fileSize;
                    if(statusCounter%getFileSize()==1)
                    {
                        FileExporter.getInstance().createXML(statusCounter,status);
                    }
                    else
                        FileExporter.getInstance().appendStatus(statusCounter, status);
                    statusCounter++;
                } catch (Exception ex) {
                    Logger.getLogger(TweetDl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
            @Override
            public void onException(Exception ex) {
                //ex.printStackTrace();
            }
            
            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    
        /*twitterStream.addListener(listener);        
        twitterStream.sample();*/        
        FilterQuery fq = new FilterQuery( );
        String keywords[] = { "transasia" };
        fq.track( keywords );
        twitterStream.addListener( listener );
        twitterStream.filter( fq );
        
    }
    
    public static void stopCapture()
    {
       twitterStream.shutdown();
       
    }
    
    public static void requestAuthorization() {
        StringBuilder buffer = new StringBuilder();
        
        try {
        /** 
         * get the time - note: value below zero 
         * the millisecond value is used for oauth_nonce later on
         */
            int millis = (int) System.currentTimeMillis() * -1;
            int time = (int) millis / 1000;

            /**
             * Listing of all parameters necessary to retrieve a token
             * (sorted lexicographically as demanded)
             */
            ResourceBundle bundleSecret = ResourceBundle.getBundle("properties/private/AutenticationSecret");
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("properties/Autentication");
             String[][] data = {
                {"oauth_callback", ""},
                {"oauth_consumer_key", bundle.getString("CONSUMER_KEY")},
                {"oauth_nonce",  String.valueOf(millis)},
                {"oauth_signature", ""},
                {"oauth_signature_method", "HMAC-SHA1"},
                {"oauth_timestamp", String.valueOf(time)},
                {"oauth_version", "1.0"}
            };

            /**
             * Generation of the signature base string
             */
            String signature_base_string = 
                "POST&"+URLEncoder.encode(bundle.getString("TwitterRequestURL"), "UTF-8")+"&";
            for(int i = 0; i < data.length; i++) {
                // ignore the empty oauth_signature field
                if(i != 3) {
                signature_base_string +=
                    URLEncoder.encode(data[i][0], "UTF-8") + "%3D" +
                    URLEncoder.encode(data[i][1], "UTF-8") + "%26";
                }
            }
            // cut the last appended %26 
            signature_base_string = signature_base_string.substring(0,
                signature_base_string.length()-3);

            /**
             * Sign the request
             */
            Mac m = Mac.getInstance("HmacSHA1");
            m.init(new SecretKeySpec(bundleSecret.getString("CONSUMER_SECRET").getBytes(), "HmacSHA1"));
            m.update(signature_base_string.getBytes());
            byte[] res = m.doFinal();
            String sig = String.valueOf(Base64Coder.encode(res));
            data[3][1] = sig;

           /**
            * Create the header for the request
            */
           String header = "OAuth ";
           for(String[] item : data) {
                header += item[0]+"=\""+item[1]+"\", ";
           }
           // cut off last appended comma
           header = header.substring(0, header.length()-2);

           System.out.println("Signature Base String: "+signature_base_string);
           System.out.println("Authorization Header: "+header);
           System.out.println("Signature: "+sig);

           String charset = "UTF-8";
           URLConnection connection = new URL(bundle.getString("TwitterRequestURL")).openConnection();
           connection.setDoInput(true);
           connection.setDoOutput(true);
           connection.setRequestProperty("Accept-Charset", charset);
           connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
           connection.setRequestProperty("Authorization", header);
           connection.setRequestProperty("User-Agent", "XXXX");
           OutputStream output = connection.getOutputStream();
           output.write(header.getBytes(charset));

           BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

           String read;
           while((read = reader.readLine()) != null) {
               buffer.append(read);
           }
        }
        catch(Exception e) {
            //e.printStackTrace();
        }
   }
    /*
    void verClases()
    {
        
        Status s;        
        s.getContributors();
        s.getCreatedAt();
        s.getCurrentUserRetweetId();
        s.getFavoriteCount();
        s.getGeoLocation();
        s.getHashtagEntities();
        s.getId();
        s.getInReplyToScreenName();
        s.getInReplyToStatusId();
        s.getInReplyToUserId();
        s.getLang();
        s.getMediaEntities();
        s.getPlace();
        s.getRateLimitStatus();
        s.getRetweetCount();
        s.getRetweetedStatus();
        
        
        
        
    }*/
}