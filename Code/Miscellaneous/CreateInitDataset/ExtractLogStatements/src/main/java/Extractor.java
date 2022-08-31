package logstatement.extractor;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.opencsv.CSVParser;
import com.opencsv.CSVWriter;
import javax.annotation.Nonnull;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extractor {
    private static int counter = 0;
    private static Set<String> nameExprScope = new HashSet<>();
    private static Set<String> methodCallScope = new HashSet<>();

    final private static Set<String> methodCallCandidates = new HashSet<>();

    /**
     * Given a the csv path of the init DS parse all the methods, 
     * extracts the methods' log statements, 
     * writes a new csv with containing the list of statements and levels
     * @param args [Input csv file path, Output csv file path]
     */
    public static void main(String[] args){
        final Path inputFile = Paths.get(args[0]);
        final Path outputFile = Paths.get(args[1]);
        try(final CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFile.toFile()))){
            final long numEntries = Files.lines(inputFile).count();
            writeHeader(inputFile, csvWriter);
            Files.lines(inputFile)
                    .skip(1) // skip header line
                    .map(Extractor::getCsvLineStringArray)
                    .filter(array -> array.length==4)
                    .forEach(method -> {
                        counter+=1;
                        System.out.printf("Progress : %d / %d\r", counter, numEntries);
                        writeMethodAstBasedRepresentation(csvWriter, method);
                    });
        }catch(IOException ignored){}
    }

    private static String[] getCsvLineStringArray(String line) {
        try{
            return new CSVParser().parseLine(line);
        }catch (IOException ignored){
            return new String[]{};
        }
    }

    private static void writeMethodAstBasedRepresentation(CSVWriter csvWriter, String[] method) {
        try{
            final MethodDeclaration methodDecl = StaticJavaParser.parseMethodDeclaration(method[3]);
            final String[] logsAndLevels = getLogStatementsAndLevels(methodDecl);
            final List<String> output = new ArrayList<>(Arrays.asList(method));
            output.add(logsAndLevels[0]);
            output.add(logsAndLevels[1]);
            csvWriter.writeNext(output.toArray(new String[0]));
        }catch (ParseProblemException ignored){}
    }

    private static void writeHeader(Path inputFile, CSVWriter csvWriter) throws IOException {
        final String[] header = Files.lines(inputFile)
                .findFirst()
                .map(entry -> entry.split(","))
                .orElse(null);
        assert header != null;
        final List<String> headerList = new ArrayList<>(Arrays.asList(header));
        headerList.add("log_statements");
        headerList.add("log_levels");
        csvWriter.writeNext(headerList.toArray(new String[0]));
    }

    private static String[] getLogStatementsAndLevels(@Nonnull final MethodDeclaration methodDecl){
        final StringJoiner logsJoiner = new StringJoiner(",", "[", "]");
        final StringJoiner levelsJoiner = new StringJoiner(",", "[", "]");
        final List<String> validLevels = new ArrayList<>(Arrays.asList("debug", "error", "fatal", "log", "warning",
                "trace", "info", "severe", "fine", "finer", "finest", "warn"));
        methodDecl.findAll(MethodCallExpr.class).forEach((expr) -> {
            if(expr.getScope().isPresent()
                    && expr.getScope().get().toString().toLowerCase().contains("log")
                    && !expr.getScope().get().isObjectCreationExpr()
                    && !expr.getNameAsString().equals("forEach")) {
                Expression scope = expr.getScope().get();
                String expressionString = "'"+ expr.toString()
                        .replaceAll("[\r\n]+", " ")
                        .replace("\"", "\\\"")
                        .replace("'", "\\ \"") +"'";
                if(((scope.isMethodCallExpr() && scope.toString().toLowerCase().contains("getlog"))
                        || scope.isNameExpr())
                        && validLevels.contains(expr.getNameAsString())) {
                    logsJoiner.add(expressionString);
                    levelsJoiner.add("'"+expr.getNameAsString()+"'");
                }else {
                    Optional<String> optLevel = getLogLevel(expr);
                    if (optLevel.isPresent()){
                        logsJoiner.add(expressionString);
                        levelsJoiner.add("'"+optLevel.get()+"'");
                    }
                }
            }
        });
        return new String[]{logsJoiner.toString(), levelsJoiner.toString()};
    }

    private static Optional<String> getLogLevel(@Nonnull final MethodCallExpr logExpr){
        List<String> candidateScopes = new ArrayList<>(Arrays.asList("log", "logger"));
        if(logExpr.getScope().isPresent() && candidateScopes.contains(logExpr.getScope().get().toString().toLowerCase())) {
            final String regex = "\\b(?i:log|logger)\\.((?!(?:set|get|is|add|for).*)\\w*)\\([^\\)]";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(logExpr.toString());
            if (matcher.find())
                return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }


}
