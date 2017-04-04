package com.lordjoe.identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * com.lordjoe.identifier.ModeFinder
 * find the most common int in a set
 * User: Steve
 * Date: 3/25/2017
 */
public class ModeFinder {
    private final Map<Integer,Integer> counts = new HashMap<>();

    public   int addItem(int item)  {
        Integer count = counts.get(item);
        if(count == null)  {
            count = 0;
        }
        int ret = count + 1;
        counts.put(item,ret);
        return ret;

    }

    /**
     * get most common key return first with count
     * @return
     */
    public int getMode()
    {
        if( isEmpty())
            throw new IllegalStateException("problem"); // ToDo change
        int modeCount = 0;
        int mode  = -1;
        for (Integer key : counts.keySet()) {
            int thisCount = counts.get(key);
            if(thisCount > modeCount)  {
                mode = key;
                modeCount = thisCount;
            }
        }
        return mode;
    }

    /**
     * true if most common key or as common as
     * @param test
     * @return
     */
    public boolean isMode(int test)
    {
        if(!counts.containsKey(test))
            return false;
        int mode = getMode();
        return mode == test || counts.get(test)  == mode;

    }

    public boolean isEmpty()
    {
        return counts.isEmpty();
    }


}
