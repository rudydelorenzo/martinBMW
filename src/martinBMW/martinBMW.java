//Copyright © 2020 - Rodrigo de Lorenzo
package martinBMW;

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

public class martinBMW {
    
    private static final String VERSION = "1.2.1";
    
    public String URL;
    public static String vinDecoderURL = "http://bmwfans.info/vin/decoder?vin=";
    
    public ArrayList<Car> cars = new ArrayList<>();
    public ArrayList<Integer> oldIds = new ArrayList<>();
    public LinkedHashMap<String, Float> distances = new LinkedHashMap<>(); //distances by postal code
    
    //email variables
    public String from = "picknpullnotify@gmail.com";
    public final String USERNAME = "picknpullnotify@gmail.com";
    public final String PASSWORD = "picknpullrocks";
    public Session session;
    
    public static void main(String[] args) throws IOException {
        //print out program info
        System.out.printf("Martin %s (BMW Edition) | Copyright 2020, Rodrigo de Lorenzo%n%n", VERSION);
        
        String mode;
        try {
            mode = args[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            mode = "-h";
        }
        mode = "-m";
        String helpMessage = "Welcome to Martin for BMW!\n"
                + "Here are the supported flags:\n"
                + "\t\"-m\"\tlaunches martin in monitoring mode (perma-search).\n"
                + "\t\"-s\"\tlaunches marin in single-search mode.\n"
                + "\t\"-h\"\tshows supported flags.\n";
        
        //launch appropriate tool
        switch (mode) {
            case "-h":
                System.out.println(helpMessage);
                break;
            case "-m":
                monitorMode m = new monitorMode();
                m.monitor();
                System.out.println("");
                break;
            case "-s":
                //martinServer.startServer();
                break;
            default:
                System.out.printf("Flag %s not recognized. For a list of accepted"
                        + " flags, launch martin with the -h flag.%n", mode);
                break;
        }
        
    }
    
    public String getTextFromURL(String partsURL) {
        String text = "";
        try {
            URL url = new URL(partsURL);
            BufferedReader read = new BufferedReader(
                new InputStreamReader(url.openStream()));
            String i;
            while ((i = read.readLine()) != null)
                text += (i + "\n");
            read.close();
        } catch (IOException e) {
            println("Error reading parts file... Exiting");
            System.exit(10);
        }
        
        return text;
    }
    
    public ArrayList<Part> getParts(String partsText) {
        println("--------------------------------------PARTS-------------------------------------");
        System.out.print("Gathering information...");
        //work out part information and add parts to parts list
        
        ArrayList<String> partNumbers = getPartNumbers(partsText);
        
        ArrayList<Part> parts = new ArrayList<>();
        for (String pn : partNumbers) parts.add(new Part(pn));
        
        System.out.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        for (Part p : parts) System.out.printf("Part Number: %s \t%-36s\t$%7.2f%n", p.partNumber, p.partName, p.price);
        
        return parts;
    }
    
    
    public  ArrayList<String> getPartNumbers(String partsText) {
        ArrayList<String> pns = new ArrayList();
        String lines[] = partsText.split("\n");
        
        for (String line : lines) {
            String pn = line.split("\t")[0];
            if (!pn.equals("")) pns.add(pn);
        }
        
        return pns;
    }
    
    public ArrayList<Car> getNewCars(String url) throws CouldNotConnectException {
        //code that gets executed repeatedly goes here
                
        //create list of old IDs to compare and see if new cars appeared
        oldIds = new ArrayList<>();
        for (Car c : cars) {
            oldIds.add(c.id);
        }
        
        try {
            cars = scrape(url);
        } catch (CouldNotConnectException e) {
            throw e;
        }

        //cars list is made, now everything is done locally
        ArrayList<Car> newCars = new ArrayList<>();

        for (Car c : cars) {
            //System.out.printf("%s: %d %s %s, VIN:%s \tAdded on: %tB %<te, %<tY%n", c.generation, c.year, c.make, c.model, c.vin, c.date);
            if (!oldIds.contains(c.id)) {
                //car is new
                newCars.add(c);
            }
        }
        println("");
        return newCars;
    }
    
    public ArrayList<Car> scrape(String url) throws CouldNotConnectException {
        ArrayList<Car> toReturn = new ArrayList<>();
        try {
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
            throw new CouldNotConnectException();
        }
        
        return toReturn;
        
    }
    
    public ArrayList<Car> sortList(ArrayList<Car> list) {
        list.sort(new PartsPresentComparator());
        list.sort(new DistanceComparator(distances));
        list.sort(new RelevanceComparator());
        
        return list;
    }
    
    public String getEmailBody(ArrayList<Car> cars) {
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
            
            return emailText;
    }
    
    public boolean sendEmail(ArrayList<Car> cars, ArrayList<Part> parts, String desiredGeneration, String email) {
        if (!cars.isEmpty()) {
            System.out.printf("(%tH:%<tM:%<tS) STARTING IN-DEPTH SCAN%n", new Date());
            for (Car c : cars) c.calculateRelevance(parts, desiredGeneration);
            //there are new cars! time to sort by relevance and email
            System.out.printf("(%tH:%<tM:%<tS) THERE ARE %d NEW CARS!%n%n", new Date(), cars.size());
            
            //cars are now sorted (Relevance, distance, parts in common)
            cars = sortList(cars);
            //email time
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from, "Martin"));
                message.addRecipient(
                        Message.RecipientType.TO,
                        InternetAddress.parse(email)[0]
                    );
                message.setSubject("New Arrivals at Pick-n-Pull! (" + cars.size() + ")");

                message.setContent(getEmailBody(cars), "text/html; charset=utf-8");

                Transport.send(message);
                return true;
            } catch (MessagingException | UnsupportedEncodingException e) {
                e.printStackTrace();
                return false;
            } catch (NoClassDefFoundError f) {
                println("NoClassDef while trying to send email, probably an error with activation.jar");
                return false;
            }
        } else {
            System.out.printf("(%tH:%<tM:%<tS) THERE ARE NO NEW CARS!%n%n", new Date());
        }
        return true;
    }

    public void println(Object toPrint) {
        System.out.println(toPrint);
    }
    
}
