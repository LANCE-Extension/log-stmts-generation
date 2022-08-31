import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public final class FileUtility {
  
  private FileUtility(){
    throw new UnsupportedOperationException("Utility class and cannot be instantiated");
  }


  public static List<String[]> readCsv(Path inputFilePath) throws IOException {
    CSVReader reader = new CSVReader(new FileReader(inputFilePath.toFile()));
    return reader.readAll();
  }

  public static void writeCsv(Path outputFilePath, String[] line) throws IOException{
    CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath.toFile(), true));
    writer.writeNext(line);
    writer.close();
  }


  public static ArrayList<String[]> readTsv(Path inputFilePath){
    ArrayList<String[]> data = new ArrayList<>();
    String line = null;
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath.toFile()))) {
      while((line = reader.readLine()) != null){
        String[] lineItems = line.split("\t");
        if(lineItems.length == 3 )
          data.add(lineItems);
      }
    }catch(IOException e){
      e.printStackTrace();
    }
    return data;
  }

  public static ArrayList<String> readTxt(Path inputFilePath){
    ArrayList<String> data = new ArrayList<>();
    String line = null;
    try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath.toFile()))) {
      while((line = reader.readLine()) != null){
        data.add(line);
      }
    }catch(IOException e){
      e.printStackTrace();
    }
    return data;
  }

  public static void writeTxt(Path outputFilePath, ArrayList<String> data){
    try{
      FileWriter fw = new FileWriter(outputFilePath.toString());
      data.forEach((line) -> {
        try {
          fw.write(line+"\n");
        } catch (IOException e) {
          System.err.println("Error writing line "+line);
          e.printStackTrace();
        }
      });
      fw.close();
    }catch(IOException e){
      System.err.println("Error writing file "+outputFilePath.toString());
    }
  }

  public static void writeTsv(ArrayList<String[]> data, Path outputFilePath){
    ArrayList<String> outputData = new ArrayList<>();
    data.forEach((line) -> outputData.add(line[0].concat("\t".concat(line[1]))));
    // System.out.println(outputData.get(0));
    try{
      FileWriter fw = new FileWriter(outputFilePath.toString());
      outputData.forEach((line) -> {
        try {
          fw.write(line+"\n");
        } catch (IOException e) {
          System.err.println("Error writing line "+line);
          e.printStackTrace();
        }
      });
      fw.close();
    }catch(IOException e){
      System.err.println("Error writing file "+outputFilePath.toString());
    }
  }
  
}
