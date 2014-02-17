package com.github.pires.example.tests;

import org.osgi.framework.ServiceReference;

import java.util.*;

public class TestUtility
{

    private TestUtility()
    {
        super();
    }

    /**
     * Explodes the dictionary into a ,-delimited list of key=value pairs
     */
    public static String explode(Dictionary dictionary)
    {
        Enumeration keys = dictionary.keys();
        StringBuffer result = new StringBuffer();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            result.append(String.format("%s=%s", key, dictionary.get(key)));
            if (keys.hasMoreElements())
            {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /*
     * Provides an iterable collection of references, even if the original array
     * is null
     */
    public static Collection<ServiceReference> asCollection(ServiceReference[] references)
    {
        return references != null ? Arrays.asList(references) : Collections.<ServiceReference>emptyList();
    }


}
