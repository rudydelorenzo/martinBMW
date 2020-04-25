//Copyright © 2020 - Rudy de Lorenzo

package martinBMW;

import java.util.Comparator;

public class RelevanceComparator implements Comparator<Car>{
    
    @Override
    public int compare(Car o1, Car o2) {
        return ((Integer)o2.getRelevance()).compareTo((Integer)o1.getRelevance());
    }
    
}
