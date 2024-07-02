package util.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConfigParser extends AbstractParser{

    private final Pattern sectionPattern = Pattern.compile("\\[\s*([0-9A-Za-z]+)\s*\\]");
    private final Pattern configPattern = Pattern.compile("(\\S+)\s*=\s*(.*\\S)\s*$");
    private final Pattern listPattern = Pattern.compile("\\[\s*(.*\\S)\s*\\]");
    
    protected final Map<String, Map<String, String>> dictionary;

    public ConfigParser(String filePath, String commentPrefix){
        super(filePath, commentPrefix);
        dictionary = new HashMap<>();
        getDictionary();
        if(dictionary.isEmpty()){
            throw new IllegalArgumentException("given file doesn't have any section!!");
        }
    }

    private void getDictionary(){
        Iterator<String> iterator = super.lines.iterator();
        String str = iterator.next();
        while (iterator.hasNext()) {
            Matcher matcher = sectionPattern.matcher(str);
            if(matcher.find()){
                Map<String, String> map = new HashMap<>();
                str = section(iterator, map);
                dictionary.put(matcher.group(1).toLowerCase(), map);
            }else{
                str = iterator.next();
            }
            if(str == null) break;
        }
    }

    
    /** 
     * @param iterator Iterator for file lines after section line
     * @param map Map of configs corresponding to section
     * @return Next line matching sectionPattern
     */
    private String section(Iterator<String> iterator, Map<String, String> map){
        while(iterator.hasNext()){
            String line = iterator.next();
            Matcher matcher = configPattern.matcher(line);
            if(!matcher.find()){
                Matcher section = sectionPattern.matcher(line);
                if(section.find()) return line;
                continue;
            }
            map.put(matcher.group(1).toUpperCase(), matcher.group(2));
        }
        return null;
    }

    /**
     * returns config corresponding (section, key) pair.
     * @param section must be lower case
     * @param key must be UPPER_SNAKE_CASE
     * @return String config
     */

    public String getConfig(String section, String key){
        String config = dictionary.get(section).get(key);
        Matcher matcher = listPattern.matcher(config);
        if(matcher.find()) config = matcher.group(1);
        return config;
    }

    public Map<String, String> getSection(String section){
        return Collections.unmodifiableMap(dictionary.get(section));
    }

    @Override
    public void printFile() {
        super.printFile();
        System.err.println(dictionary);
    }
}
