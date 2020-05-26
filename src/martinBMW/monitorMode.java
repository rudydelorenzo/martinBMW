//Copyright © 2020 - Rudy de Lorenzo
package martinBMW;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class monitorMode {
    
    public void monitor() {
        //initialize email
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        martinBMW m = new martinBMW();
        m.session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(m.USERNAME, m.PASSWORD);
                    }
                });
        
        ArrayList<Part> parts = m.getParts(m.getTextFromURL("https://gist.githubusercontent.com/rudydelorenzo/33e8db417e81232e7f12c4ed5e639b83/raw"));
        
        //begin pick n pull data collection
        String zip = "T5K2K3";
        int distance = 500;
        m.URL = String.format("https://www.picknpull.com/check_inventory.aspx?Zip=%s&Make=90&Model=&Year=&Distance=%d", zip, distance);
        m.println("\n------------------------------------SEARCH--------------------------------------");
        
        if (true);
        int delay = 0;   // delay for 0 sec.
        int period = 600;  // repeat every 10 min (600sec).
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    ArrayList<Car> newCars = m.getNewCars(m.URL);
                    m.sendEmail(newCars, parts, "E39", "rdelorenzo5@gmail.com");
                    System.out.println(newCars.get(0).imageURL);
                } catch (CouldNotConnectException e) {
                    System.out.println("ERROR: Could not establish connection to Pick n' Pull,\n"
                            + "\tNo results will be provided for this search cycle...");
                }
            }
        }, delay*1000, period*1000);
    }
    
}
