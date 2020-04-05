package picknpullscraper;

import java.util.Comparator;


public class PartsPresentComparator implements Comparator<Car>{

    @Override
    public int compare(Car o1, Car o2) {
        return ((Integer)o2.contains.size()).compareTo((Integer)o1.contains.size());
    }
    
}
