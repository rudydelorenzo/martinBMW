//Copyright © 2020 - Rudy de Lorenzo

package martinBMW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Part {
    
    public String partNumber;
    public String partName;
    public float price;
    public Set<String> presentIn = new HashSet<>();
    
    public Part(String pn) {
        pn = pn.replace("-", "");
        pn = pn.replace(" ", "");
        partNumber = pn;
        String url = "https://www.realoem.com/bmw/enUS/partxref?q=" + partNumber;
        try {
            Document page = Jsoup.connect(url).get();
            Element content = page.select("div.content").first();
            
            partName = content.getElementsByTag("h2").first().text();
            try {
                price = Float.parseFloat(content.select(">dl dd").last().text().replace("$", ""));
            } catch (NumberFormatException e) {
                //TODO: Couldn't get price for part. Maybe set a flag and print an asterisk next to price?
            }
            
            Elements results = content.select("div.partSearchResults ul li a");
            for (Element result : results) {
                String href = result.attr("href");
                int generationBeginIndex = href.indexOf("series=")+7;
                try {
                    presentIn.add(href.substring(generationBeginIndex, generationBeginIndex+3));
                } catch (StringIndexOutOfBoundsException e) {
                    //car chassis number only has 2 digits (i.e. Z4)
                    presentIn.add(href.substring(generationBeginIndex, generationBeginIndex+2));
                }
            }
            
        } catch (IOException e) {
            System.out.printf("Couldn't connect to RealOEM to gather information"
                    + " on part PN:%s%n", partNumber);
        }
        
    }
    
}
