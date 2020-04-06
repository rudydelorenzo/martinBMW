//Copyright © 2020 - Rudy de Lorenzo

package picknpullscraper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static picknpullscraper.PicknpullScraper.println;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import static picknpullscraper.PicknpullScraper.parts;

public class Car {
    
    private LinkedHashMap<String, String> data = new LinkedHashMap<>();
    public int id;
    public int locationId;
    public String locationName;
    public int row;
    public String carURL;
    public String postCode;
    public Date dateAdded;
    public int year;
    public String make;
    public String model;
    public String trim;
    public String generation; //UNK means car is automatically irrelevant
    public String vin;
    public int relevance = -1;
    public ArrayList<Part> contains = new ArrayList<>();
    public String imageURL;
    
    public Car(String dataString) {
        //first make sure that string passed in makes sense
        if (!(dataString.contains("Id") && dataString.contains(":"))) {
            throw new IllegalArgumentException("Suspected erroneous string "
                    + "passed into new Car() function.");
        }
        
        //println(dataString);
        
        //decompose string and put into dictionary
        String[] pairs = dataString.split(",");
        for (String pair : pairs) {
            String[] values = pair.split(":");
            //WARNING: this removes quotation marks present on any field
            data.put(values[0].replace("\"", ""), values[1].replace("\"", ""));
        }
        
        id = Integer.parseInt(data.get("Id"));
        locationId = Integer.parseInt(data.get("LocationId"));
        locationName = data.get("LocationName");
        row = Integer.parseInt(data.get("Row"));
        postCode = data.get("Zip");
        try {
            dateAdded = new SimpleDateFormat("yyyy-MM-dd'T'HH").parse(data.get("DateAdded"));
        } catch (ParseException e) {
            System.out.printf("Couldn't parse date for %d %s %s, VIN %s.", year, make, model, vin);
        }
        year = Integer.parseInt(data.get("Year"));
        make = data.get("Make");
        model = data.get("Model");
        generation = solveGeneration();
        vin = data.get("VIN");
        carURL = "https://www.picknpull.com/vehicle_details.aspx?VIN=" + vin;
        imageURL = data.get("ThumbNail");
    }
    
    private String solveGeneration() {
        String gen = "UNK";
        
        if (model.equalsIgnoreCase("5-Series") || model.equalsIgnoreCase("M5")) {
            if (1988 < year && year < 1996) gen = "E34";
            else if (1995 < year && year < 2004) gen = "E39";
            else if (2003 < year && year < 2011) gen = "E60";
        } else if (model.equalsIgnoreCase("3-Series")) {
            if (1997 < year && year < 2006) gen = "E46";
        } else if (model.equalsIgnoreCase("7-Series")) {
            if (1993 < year && year < 2002) gen = "E38";
            else if (2001 < year && year < 2006) gen = "E65";
        } else if (model.equalsIgnoreCase("8-Series")) {
            if (1988 < year && year < 2000) gen = "E31";
        } else if (model.equalsIgnoreCase("X5")) {
            if (1999 <= year && year < 2006) gen = "E53";
        } else if (model.equalsIgnoreCase("X3")) {
            if (2003 < year && year < 2011) gen = "E83";
        } else if (model.equalsIgnoreCase("Z3")) {
            if (1995 < year && year < 2003) gen = "E36";
        } else if (model.equalsIgnoreCase("Z4")) {
            if (2001 < year && year < 2006) gen = "E85";
        } else if (model.equalsIgnoreCase("Z8")) {
            if (1997 < year && year < 2004) gen = "E52";
        }
        
        return gen;
    }
    
    public int getRelevance() {
        if (relevance == -1) {
            //if we enter here, relevance hasn't been calculated yet
            relevance = Relevance.NONE;
            if (!generation.equals("UNK")) {
                try {
                    deepDive();
                    if (generation.equals("E39")) {
                        relevance = Relevance.IDENTICAL;
                    } else {
                        for (Part p : parts) {
                            if (p.presentIn.contains(generation)) {
                                relevance = Relevance.PARTIAL;
                                contains.add(p);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.printf("Error deepDiving into car with VIN %s"
                            + ", relevance information may be incorrect!%n", vin);
                }
            }
        }
        return relevance;
    }
    
    private void deepDive() throws IOException {
        Document page = Jsoup.connect(carURL).get();
        
        Element dataTable = page.getElementById("ctl00_ctl00_MasterHolder_MainContent_infoTextBlock");
        Elements rows = dataTable.select("div.dataRow, div.altDataRow");
        
        //order of rows on website matters...
        if (data.get("Row").equals("null")) data.replace("Row", rows.get(0).select("span.body-text").text());
        if (data.get("Trim").equals("null")) data.replace("Trim", rows.get(2).select("span.body-text").text());
        if (data.get("Engine").equals("null")) data.replace("Engine", rows.get(3).select("span.body-text").text());
        if (data.get("Transmission").equals("null")) data.replace("Transmission", rows.get(4).select("span.body-text").text());
        if (data.get("Color").equals("null")) data.replace("Color", rows.get(5).select("span.body-text").text());
        if (data.get("BarCodeNumber").equals("null")) data.replace("BarCodeNumber", rows.get(7).select("span.body-text").text());
        
        trim = data.get("Trim");
    }
    
    public String getPartsListHTML() {
        String str = "<ul>\n";
        for (Part p : contains) {
            str += String.format("<li>%s (%s)</li>%n", p.partName, p.partNumber);
        }
        str += "</ul>";
        
        return str;
    }
    
    @Override
    public String toString() {
        return String.format("%d %s %s (%s)", year, make, model, generation);
    }
    
}
