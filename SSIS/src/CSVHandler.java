import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVHandler {
    private final String file;
    private final String[] headers;

    public CSVHandler(String file){
        this.file = file;
        this.headers = readHeaders();
    }

    public String[] readHeaders(){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            if (firstLine != null) {
                return firstLine.split(",");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new String[]{};
    }

    public String[] getHeaders() {
        return headers;
    }

    public List<String[]> readCSV(){
        List<String[]> data = new ArrayList<>();
        try{
            BufferedReader read = new BufferedReader(new FileReader(file));
            String line;
            boolean header = true;
            while ((line = read.readLine()) != null) {
                String[] val = line.split(",");
                if (header){
                    header = false;
                } else {
                    data.add(val);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
    public void writeCSV(List<String[]> data){
        try{
            BufferedWriter write = new BufferedWriter(new FileWriter(file));
            write.write(String.join(",", headers));
            write.newLine();
            for (String[] row : data){
                write.write(String.join(",", row));
                write.newLine();
                write.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public  void addtoCSV(String[] newData){
        try {
            BufferedWriter add = new BufferedWriter(new FileWriter(file, true));
            add.write(String.join(",", newData));
            add.newLine();
            add.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateCSV(int rowIndex, String[] updatedData){
        List<String[]> data = readCSV();
        if (rowIndex >= 0 && rowIndex < data.size()){
            data.set(rowIndex, updatedData);
            writeCSV(data);
        }
    }
    public void deletefromCSV(int rowIndex){
        List<String[]> data = readCSV();
        if (rowIndex >= 0 && rowIndex < data.size()) {
            data.remove(rowIndex);
            writeCSV(data);

        }
    }
}
