//Copyright © 2020 - Rudy de Lorenzo
package martinBMW;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import static martinBMW.martinBMW.*;

public class monitorMode {

    public static void monitor() {
        //initialize email
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS
        
        session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });
        
        
        parts();
        
        //begin pick n pull data collection
        String zip = "T5K2K3";
        int distance = 500;
        URL = String.format("https://www.picknpull.com/check_inventory.aspx?Zip=%s&Make=90&Model=&Year=&Distance=%d", zip, distance);
        println("\n------------------------------------SEARCH--------------------------------------");
        
        if (true);
        int delay = 0;   // delay for 0 sec.
        int period = 600;  // repeat every 10 min (600sec).
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                findNewCars(URL);
            }
        }, delay*1000, period*1000);
    }
    
}
