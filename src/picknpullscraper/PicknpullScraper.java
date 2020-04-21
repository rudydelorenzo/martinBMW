//Copyright © 2020 - Rodrigo de Lorenzo

package picknpullscraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PicknpullScraper {
    
    private static final String VERSION = "1.0.7";
    
    public static ArrayList<Car> cars = new ArrayList<>();
    public static ArrayList<Integer> oldIds = new ArrayList<>();
    public static ArrayList<String> partNumbers = new ArrayList<>();
    public static ArrayList<Part> parts = new ArrayList<>();
    public static LinkedHashMap<String, Float> distances = new LinkedHashMap<>(); //distances by postal code
    
    //email variables
    //email stuff
    public static String from = "picknpullnotify@gmail.com";
    public static final String username = "picknpullnotify@gmail.com";
    public static final String password = "picknpullrocks";
    public static Session session;
    
    public static void main(String[] args) throws IOException {
        //print out program info
        System.out.printf("Martin %s (BMW Edition) | Copyright 2020, Rodrigo de Lorenzo%n%n", VERSION);
        //initialize email
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        
        println("--------------------------------------PARTS-------------------------------------");
        System.out.print("Gathering information...");
        //work out part information and add parts to parts list
        
        getPartNumbers("https://gist.githubusercontent.com/rudydelorenzo/33e8db417e81232e7f12c4ed5e639b83/raw");
        
        for (String pn : partNumbers) parts.add(new Part(pn));
        
        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        for (Part p : parts) System.out.printf("Part Number: %s \t%-36s\t$%7.2f%n", p.partNumber, p.partName, p.price);
        
        //begin pick n pull data collection
        String zip = "T5K2K3";
        int distance = 500;
        String url = String.format("https://www.picknpull.com/check_inventory.aspx?Zip=%s&Make=90&Model=&Year=&Distance=%d", zip, distance);
        println("\n------------------------------------SEARCH--------------------------------------");
        
        int delay = 0;   // delay for 0 sec.
        int period = 600;  // repeat every 10 min (600sec).
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //code that gets executed repeatedly goes here
                
                //create list of old IDs to compare and see if new cars appeared
                oldIds = new ArrayList<>();
                for (Car c : cars) {
                    oldIds.add(c.id);
                }

                cars = scrape(url);

                //cars list is made, now everything is done locally (except for deepdives)
                ArrayList<Car> newCars = new ArrayList<>();
                System.out.printf("(%tH:%<tM:%<tS) STARTING IN-DEPTH SCAN%n", new Date());
                String progress = "Working ";
                System.out.print(progress);
                for (Car c : cars) {
                    //System.out.printf("%s: %d %s %s, VIN:%s \tAdded on: %tB %<te, %<tY%n", c.generation, c.year, c.make, c.model, c.vin, c.date);
                    for (int i =0 ; i < progress.length(); i++) System.out.print("\b");
                    progress += ".";
                    System.out.print(progress);
                    if (!oldIds.contains(c.id)) {
                        //car is new
                        
                        c.getRelevance();
                        newCars.add(c);
                    }
                }
                println("");
                if (!newCars.isEmpty()) {
                    //there are new cars! time to sort by relevance and email
                    System.out.printf("(%tH:%<tM:%<tS) THERE ARE %d NEW CARS!%n%n", new Date(), newCars.size());
                    newCars.sort(new PartsPresentComparator());
                    newCars.sort(new DistanceComparator());
                    newCars.sort(new RelevanceComparator());
                    //cars are now sorted (Relevance, distance, parts in common)
                    
                    //email time
                    sendEmail(newCars, "rdelorenzo5@gmail.com");
                    
                } else {
                    System.out.printf("(%tH:%<tM:%<tS) THERE ARE NO NEW CARS!%n%n", new Date());
                }
            }
        }, delay*1000, period*1000);
    }
    
    public static ArrayList<Car> scrape(String url) {
        ArrayList<Car> toReturn = new ArrayList<>();
        try{
            Document page = Jsoup.connect(url).get();

            Element pageContent = page.getElementById("page-content-wrapper");
            Element resultsDiv = pageContent.getElementById("ctl00_ctl00_MasterHolder_MainContent_resultsDiv");

            //grab location info
            Elements locations = resultsDiv.select("div.location-distance");
            Elements postalCodes = resultsDiv.select("div.store-info");
            for (int i = 0; i<locations.size(); i++) {
                Element location = locations.get(i);
                String locdistString = location.select("span.approx-distance span").first().text();
                float locdistance = Float.parseFloat(locdistString.substring(0, locdistString.indexOf(" ")));
                
                Element postCodeDiv = postalCodes.get(i);
                String postCode = postCodeDiv.select("div.address-phone div.store-address span.street-address span.city-state-zip").first().text().split(", ")[1];
                postCode = postCode.substring(postCode.indexOf((char)8194)+1).replace(" ", "");
                //WARNING: Post code stored with no spaces!
                distances.put(postCode, locdistance);
            }
            
            //grab car info
            Elements scripts = resultsDiv.getElementsByTag("script");
            for (Element script: scripts) {
                String scriptText = script.toString();
                String locationData = scriptText.substring(scriptText.indexOf("[")+2, scriptText.indexOf("]")-1);
                String[] carData = locationData.split("\\},\\{");
                for (String data : carData) {
                    toReturn.add(new Car(data));
                }
            }
        } catch (IOException e) {
            println("ERROR: Could not establish connection to site, exiting...");
            System.exit(10);
        }
        
        return toReturn;
        
    }
    
    public static boolean sendEmail(ArrayList<Car> cars, String email) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "Martin"));
            message.addRecipient(
                    Message.RecipientType.TO,
                    InternetAddress.parse(email)[0]
                );
            message.setSubject("New Arrivals at Pick-n-Pull! (" + cars.size() + ")");
            
            //divide list into relevance categories
            LinkedHashMap<Integer, ArrayList<Car>> lhm = new LinkedHashMap<>();
            for (Car c : cars) {
                ArrayList<Car> relevanceList = lhm.get(c.getRelevance());
                if (relevanceList == null) {
                    lhm.put(c.getRelevance(), relevanceList = new ArrayList<>());
                }
                relevanceList.add(c);
            }
            //println(lhm);
            
            //creating email text
            String emailText = "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table, th, td {border-collapse: collapse; border: 1px solid black;}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body><h1>ARRIVALS</h1>\n";
            //make sections (headers + tables)
            for (Map.Entry<Integer, ArrayList<Car>> entry : lhm.entrySet()) {
                //make header
                int currentLevel = entry.getKey();
                String sectionHeader = "";
                if (currentLevel == Relevance.IDENTICAL) sectionHeader = "IDENTICAL";
                else if (currentLevel == Relevance.PARTIAL) sectionHeader = "PARTIAL MATCH";
                else if (currentLevel == Relevance.NONE) sectionHeader = "OTHER";
                sectionHeader = String.format("<h2>%s</h2>\n", sectionHeader);
                
                String table = "<table cellpadding=\"10\">\n";
                //make header row
                table += String.format("<tr>"
                        + "<th>Year</th>"
                        + "<th>Model</th>"
                        + "%s"
                        + "<th>VIN</th>"
                        + "%s"
                        + "<th>Location</th>"
                        + "<th>Row</th>"
                        + "<th>Date Added</th></tr>\n", (currentLevel != Relevance.NONE ? "<th>Trim</th>":""), 
                        (currentLevel == Relevance.PARTIAL ? "<th>Parts</th>":""));
                //make table
                ArrayList<Car> currentList = entry.getValue();
                for (Car c : currentList) {
                    table += String.format("<tr>"
                            + "<td>%d</td>"
                            + "<td>%s</td>"
                            + "%s"
                            + "<td>%s</td>"
                            + "%s"
                            + "<td>%s</td>"
                            + "<td>%d</td>"
                            + "<td>%tB %<te, %<tY%n</td>"
                            + "</tr>\n", c.year, (c.model + " (" + c.generation + ")"), 
                            (currentLevel != Relevance.NONE ? "<td>" + c.trim + "</td>":""), 
                            String.format("<a href=\"%s\">%s</a>", c.carURL, c.vin), (currentLevel == Relevance.PARTIAL ? ("<td>Car may contain:" + c.getPartsListHTML() + "</td>"):""), 
                            c.locationName, c.row, c.dateAdded);
                }
                table += "</table>\n</body>\n</html>";
                
                emailText += sectionHeader;
                emailText += table;
            }
            
            
            //done creating email content
            
            message.setContent(emailText, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (NoClassDefFoundError f) {
            println("NoClassDef while trying to send email, probably an error with activation.jar");
            return false;
        }
    }
    
    public static void getPartNumbers(String in) {
        try {
            URL url = new URL(in);
            BufferedReader read = new BufferedReader(
                new InputStreamReader(url.openStream()));
            String i;
            while ((i = read.readLine()) != null)
                partNumbers.add(i.split("\t")[0]);
            read.close();
        } catch (IOException e) {
            println("Error reading parts file... Exiting");
            System.exit(10);
        }
        
    }

    public static void println(Object toPrint) {
        System.out.println(toPrint);
    }
    
}
