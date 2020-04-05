package picknpullscraper;

import java.util.Comparator;
import static picknpullscraper.PicknpullScraper.distances;


public class DistanceComparator implements Comparator<Car>{

    @Override
    public int compare(Car o1, Car o2) {
        return distances.get(o1.postCode).compareTo(distances.get(o2.postCode));
    }
    
}
