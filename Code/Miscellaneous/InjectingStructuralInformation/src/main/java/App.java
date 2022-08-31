import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import FileUtility;

public class App {
  private static int processFinetuningCounter = 0;
  private static int processFinetuningNumEntries = 0;
  private static int processPretrainingCounter = 0;
  private static int processedPretrainingEntries = 0;
  private static int tokenizerLimitCount = 0;

  public static void main(String[] args) {
      try{
        // TODO: Change files paths and use the appropriate method for the type of DS
          processFinetuningCsv(Paths.get("./files/in/classifier/test.csv"),
                  Paths.get("files/out/classifier/tokens/test.csv"),
                  Paths.get("files/out/classifier/ast/test.csv"));
          processFinetuningCsv(Paths.get("./files/in/classifier/eval.csv"),
                  Paths.get("files/out/classifier/tokens/eval.csv"),
                  Paths.get("files/out/classifier/ast/eval.csv"));
          processFinetuningCsv(Paths.get("./files/in/classifier/train.csv"),
                  Paths.get("files/out/classifier/tokens/train.csv"),
                  Paths.get("files/out/classifier/ast/train.csv"));
//          createTokenizerDS(Paths.get("./files/in/tokenizer/raw_tokenizer.txt"),
//                  Paths.get("./files/in/tokenizer/c4-train.00004-of-01024.txt"));
//          processPretrainingFile(Paths.get("./java_methods_wo_logs.csv"),
//                  Paths.get("./files/out/pretraining/pretraining.csv"));
      }catch(IOException ignored){}
  }

  private static void createTokenizerDS(final Path rawTokenizerFilepath, final Path c4Filepath) throws IOException{
      final Path astbasedOutputFilePath = Paths.get("./files/out/tokenizer/ast/tokenizer.txt");
      final Path tokensbasedOutputFilePath = Paths.get("./files/out/tokenizer/tokens/tokenizer.txt");
      Files.lines(rawTokenizerFilepath)
//              .limit(10)
              .forEach(method -> {
                  try{
                      MethodDeclaration inputMtd = getJavaMethodRepresentation(method);
                      String inputAST = MethodParser.parse(inputMtd, new StringJoiner(" ")).toString();
                      String trimmedInputAST = inputAST.trim().replaceAll("  +", " ");
                      if (tokenizerLimitCount == 1000000)
                          return;
                      if(tokenizerLimitCount++ < 500000){
                          writeUsingFiles(astbasedOutputFilePath, trimmedInputAST);
                      }else{
                          writeUsingFiles(astbasedOutputFilePath, method);
                      }
                      writeUsingFiles(tokensbasedOutputFilePath, method);
                  }catch(ParseProblemException ignored){}
              });
      Files.lines(c4Filepath)
//              .limit(10)
              .forEach(line -> {
                  if(line.startsWith("b'") || line.startsWith("b\"")){
                      line = StringUtils.substring(line, 2,line.length()-1);
                  }
                  writeUsingFiles(astbasedOutputFilePath, line);
                  writeUsingFiles(tokensbasedOutputFilePath, line);
              });
  }

  private static void processFinetuningCsv(Path inputFile, Path tokensOutputPath, Path astOutputPath) throws IOException {
      List<String[]> data = FileUtility.readCsv(inputFile);
      List<String> header = new ArrayList<>(Arrays.asList(data.get(0)));
      int strippedMethodIndex = header.indexOf("stripped_method_text");
      processFinetuningNumEntries = data.size();
      CSVWriter tokensWriter = new CSVWriter(new FileWriter(tokensOutputPath.toFile()));
      CSVWriter astWriter = new CSVWriter(new FileWriter(astOutputPath.toFile()));
      tokensWriter.writeNext(header.toArray(String[]::new));
      header.add("ast_method_text");
      astWriter.writeNext(header.toArray(String[]::new));
      data.stream()
              .skip(1) // skip header
                .forEach(line -> {
                  processFinetuningCounter += 1;
                  String input = line[strippedMethodIndex];
                  try{
                    MethodDeclaration inputMtd = getJavaMethodRepresentation(input);
                    String inputAST = MethodParser.parse(inputMtd, new StringJoiner(" ")).toString();
                    String trimmedInputAST = inputAST.trim().replaceAll("  +", " ");
                    tokensWriter.writeNext(line);
                    List<String> astLine = new ArrayList<>(Arrays.asList(line));
                    astLine.add(trimmedInputAST);
                    astWriter.writeNext(astLine.toArray(String[]::new));
                  }catch(ParseProblemException ignored){}
                  System.out.print("Progress : " + processFinetuningCounter + "/" + processFinetuningNumEntries +"\r");
                });
        tokensWriter.close();
        astWriter.close();
  }

  private static void printPreOrderWalk(MethodDeclaration mtd) {
    StringJoiner annotatedMethodText = new StringJoiner(" ");
    mtd.walk(Node.TreeTraversal.PREORDER, x -> {
      if (x.getChildNodes().isEmpty()) {
        annotatedMethodText.add(x.getMetaModel().toString());
        annotatedMethodText.add(x.toString());
      } else {
        annotatedMethodText.add(x.getMetaModel().toString());
      }
    });
    System.out.println(annotatedMethodText.toString());
    System.out.println();
  }

  private static MethodDeclaration getJavaMethodRepresentation(String javaMethod) throws ParseProblemException {
    return StaticJavaParser.parseMethodDeclaration(javaMethod);
  }

  private static void processPretrainingFile(Path inputFile, Path outputFilePath) throws IOException{
      try {
          long lineCount = Files.lines(inputFile, StandardCharsets.UTF_8).count();
          String headerString = Files.lines(inputFile, StandardCharsets.UTF_8)
                  .limit(1)
                  .collect(Collectors.joining());
          String[] headerArray = new CSVReader(new StringReader(headerString)).readNext();
          List<String> header = new ArrayList<>(Arrays.asList(headerArray));
          int tokenizedMethodTextIndex = header.indexOf("tokenized_method_text");
          CSVWriter writer = new CSVWriter(new FileWriter(outputFilePath.toFile()));
          header.add("ast_method_text");
          writer.writeNext(header.toArray(String[]::new));
          Files.lines(inputFile, StandardCharsets.UTF_8)
                  .skip(1) // skip csv header
                  .map(line -> {
                      try {
                          return new CSVReader(new StringReader(line)).readNext();
                      } catch (IOException e) {
                          throw new RuntimeException(e);
                      }
                  })
                  .forEach(line -> {
                      processPretrainingCounter +=1 ;
                    String method = line[tokenizedMethodTextIndex];
                    try{
                        MethodDeclaration inputMtd = getJavaMethodRepresentation(method);
                        String inputAST = MethodParser.parse(inputMtd, new StringJoiner(" ")).toString();
                        String trimmedInputAST = inputAST.trim().replaceAll("  +", " ");
                        List<String> newLine = new ArrayList<>(Arrays.asList(line));
                        newLine.add(trimmedInputAST);
                        writer.writeNext(newLine.toArray(String[]::new));
                        processedPretrainingEntries += 1;
                    }catch(ParseProblemException ignored){}
                    System.out.printf("Progress : %d/%d\tProcessed : %d/%d\r", processPretrainingCounter, lineCount, processedPretrainingEntries, lineCount);
                  });
          writer.close();
      }catch(IOException e){e.printStackTrace();}
  }

  private static void writeUsingFiles(Path filepath, String data) {
      data = data+"\n";
    try {
        Files.createDirectories(filepath.getParent());
        Files.write(filepath, data.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}