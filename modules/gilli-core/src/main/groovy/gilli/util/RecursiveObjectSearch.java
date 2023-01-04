package gilli.util;

import gilli.internal.main.Gilli;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecursiveObjectSearch
{
    public SearchContext searchKey(Object o, String key)
    {
        SearchContext context = new SearchContext(key);
        searchKey(o, context, new ArrayList<>());

        return context;
    }

    private void searchKey(Object o, SearchContext context, List<String> path)
    {
        //System.out.println("path = " + String.join(".", path));
        if (GeneralUtil.isScalar(o))
            return;

        if (o instanceof Collection<?> l)
        {
            Iterator<?> iterator = l.iterator();
            int i = 0;
            while (iterator.hasNext())
            {
                Object child = iterator.next();
                String id = "[" + i + "]";
                List<String> newPath = GeneralUtil.copyAndAppend(path, id);
                searchKey(child, context, newPath);
                i++;
            }
        }
        else // Map or Object
        {
            Map<String, Object> properties = (o instanceof Map) ? (Map<String, Object>) o : DefaultGroovyMethods.getProperties(o);

            properties.remove("class");

            for (Map.Entry<String, Object> e : properties.entrySet())
            {
                List<String> childKey = GeneralUtil.copyAndAppend(path, e.getKey());

                if (e.getKey().equals(context.searchedKey))
                    context.addResult(childKey, properties.get(context.searchedKey));

                if (GeneralUtil.isScalar(e.getValue()))
                    continue;

                searchKey(e.getValue(), context, childKey);
            }
        }

    }

    public static class SearchContext
    {
        String searchedKey;
        Map<List<String>, Object> result = new HashMap<>();
        List<String> curPath = new ArrayList<>();

        public SearchContext(String searchedKey)
        {
            this.searchedKey = searchedKey;
        }

        void addResult(List<String> path, Object value)
        {
            ArrayList<String> newPath = new ArrayList<>(path);
            result.put(newPath, value);
        }

        public void printKeys()
        {
            for (Map.Entry<List<String>, Object> r : result.entrySet())
            {
                String k = printableKey(r.getKey());
                Gilli.stdoutWithoutTime.info(k);
            }
        }

        public void printPaths()
        {
            for (Map.Entry<List<String>, Object> r : result.entrySet())
            {
                String k = printableKey(r.getKey());

                Gilli.stdoutWithoutTime.info("{} = {}",
                        k,
                        r.getValue());
            }
        }

        private String printableKey(List<String> key)
        {
            String k = key.stream().map(
                    s -> s.contains("-") ? '"' + s + '"' : s
            ).collect(Collectors.joining("."));

            k = k.replace(".[", "[");
            return k;
        }
    }
}
