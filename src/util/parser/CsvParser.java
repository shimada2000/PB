package util.parser;

import java.util.Arrays;

public class CsvParser extends AbstractParser {

    private final String regex = "\s*,\s*";
    protected String[][] csv;
    protected int height, width;

    public CsvParser(String filePath, String commentPrefix){
        super(filePath, commentPrefix);
        height = lines.size();
        csv = getMatrix();
        width = (csv[0]).length;
    }

    public String[][] getMatrix(){
        String[][] data = new String[height][];
        for(int row = 0; row < height; row++){
            data[row] = (lines.get(row)).split(regex);
        }
        return data;
    }

    public int to1D(int row, int column){
        return row * width + column;
    }

    public void printCSV(){
        for(String[] row: csv){
            System.err.println(Arrays.toString(row));
        }
    }
}
