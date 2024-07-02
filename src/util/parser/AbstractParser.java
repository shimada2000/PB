package util.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParser {

    private String filePath;
    private String commentPrefix;
    protected List<String> lines;

    public AbstractParser(String _filePath, String _commentPrefix){
        filePath = _filePath;
        commentPrefix = _commentPrefix;
        lines = parse();
        if(lines.isEmpty()){
            throw new IllegalArgumentException("given file is blank or comment only!!");
        }
    }

    private List<String> parse(){
        List<String> data = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            while(true){
                String line = br.readLine();
                if(line == null) break;
                if(line.equals("")) continue;
                if(line.startsWith(commentPrefix)) continue;
                data.add(line);
            }
            br.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return data;
    }

    public void printFile(){
        System.err.println("----------" + filePath + "----------");
    }

    public void commentedFile(){
        for(String line: lines){
            System.out.println(String.format("# %s", line));
        }
        System.out.println("");
    }
}
