//Copyright © 2020 - Rudy de Lorenzo

package martinBMW;

import java.util.Comparator;
import java.util.LinkedHashMap;

public class DistanceComparator implements Comparator<Car>{
    
    public LinkedHashMap<String, Float> distances;
    
    public DistanceComparator(LinkedHashMap<String, Float> lhm) {
        distances = lhm;
    }
    
    @Override
    public int compare(Car o1, Car o2) {
        return distances.get(o1.postCode).compareTo(distances.get(o2.postCode));
    }
    
}
