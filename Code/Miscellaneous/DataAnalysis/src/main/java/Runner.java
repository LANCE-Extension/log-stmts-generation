import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Registry;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class Runner {

    public static String getLogMessage(String logStmt) {
        final String regex = "\\.\\s(?:warn|debug|error|fatal|info|trace)\\s";
        final Pattern pattern = Pattern.compile(regex);
        final java.util.regex.Matcher matcher = pattern.matcher(logStmt);
        if (matcher.find()) {
            return logStmt.substring(matcher.end(), logStmt.length()-2);
        }
        else {
            String items[] = logStmt.split(" ");

            int counter_start = 0;

            for (String token : items) {
                if (!token.equals("(")) {
                    counter_start += 1;
                } else {
                    break;
                }
            }

            int counter_end = items.length - 1;
            for (int i = items.length - 1; i >= 0; i--) {
                if (!items[i].equals(")")) {
                    counter_end -= 1;
                } else {
                    break;
                }
            }

            String result = "";
            for (int i = counter_start; i <= counter_end; i++) {
                result += items[i] + " ";
            }
            return result;
        }
    }

    public static int getDifferenceLogLevel(String logStmtTarget, String logStmtPrediction) {
        //Trace < Debug < Info < Warn < Error < Fatal.

        int cardinalTarget = 0;

        switch (logStmtTarget) {

            case "trace":
                // code block
                cardinalTarget = 1;
                break;

            case "debug":
                cardinalTarget = 2;
                break;

            case "info":
                cardinalTarget = 3;
                break;

            case "warn":
                cardinalTarget = 4;
                break;

            case "error":
                cardinalTarget = 5;
                break;

            case "fatal":
                cardinalTarget = 6;
                break;

            default:
                break;
        }

        int cardinalPrediction = 0;
        switch (logStmtPrediction) {

            case "trace":
                // code block
                cardinalPrediction = 1;
                break;

            case "debug":
                cardinalPrediction = 2;
                break;

            case "info":
                cardinalPrediction = 3;
                break;

            case "warn":
                cardinalPrediction = 4;
                break;

            case "error":
                cardinalPrediction = 5;
                break;

            case "fatal":
                cardinalPrediction = 6;
                break;

            default:
                break;
        }


        return Math.abs(cardinalTarget - cardinalPrediction);
    }


    public static String getLevelLog(String logStmt){
        final String regex = "\\.\\s(warn|debug|error|fatal|info|trace)\\s\\(";
        final Pattern pattern = Pattern.compile(regex);
        final java.util.regex.Matcher matcher = pattern.matcher(logStmt);
        if (matcher.find()) {
            return matcher.group(1);
        }else{
            return "";
        }
    }

    public static void main(String[] args) throws IOException {
        // Create a diff comparator with two inputs strings.

        Run.initGenerators(); // registers the available parsers

//        TODO: change file paths
        List<String> input_list = Files.readAllLines(Paths.get("/Users/ferrava/Desktop/logdiff/src/main/resources/antonio-finetuning-enhanced-single-top-5-Best-Performing-Model-validation_eval-wrong_log_injection_inputs"));
        List<String> log_stmt_list = Files.readAllLines(Paths.get("/Users/ferrava/Desktop/logdiff/src/main/resources/antonio-finetuning-enhanced-single-top-5-Best-Performing-Model-validation_eval-wrong_log_injection_logs")); // log target
        List<String> target_list = Files.readAllLines(Paths.get("/Users/ferrava/Desktop/logdiff/src/main/resources/antonio-finetuning-enhanced-single-top-5-Best-Performing-Model-validation_eval-wrong_log_injection_targets"));
        List<String> prediction_list = Files.readAllLines(Paths.get("/Users/ferrava/Desktop/logdiff/src/main/resources/antonio-finetuning-enhanced-single-top-5-Best-Performing-Model-validation_eval-wrong_log_injection_690000_predictions"));


        int matchingLevel = 0;
        int matchingPosition = 0;
        int matchingMessage = 0;
        int matchingLogVariableIdentifier = 0;
        int unparsable_counter = 0;
        int unparsableInputCounter = 0;
        int outRangeError = 0;
        int unparsableLogStmt = 0;

        // Res files
        // create FileWriter object with file as parameter

        FileWriter correctLogLevelWriter = new FileWriter("correctLogLevel.csv");
        CSVWriter writerCorrectLevel = new CSVWriter(correctLogLevelWriter);
        String[] headerX = {"Instance Number", "Prediction", "Target"};
        writerCorrectLevel.writeNext(headerX);

        FileWriter correctLogPositionWriter = new FileWriter("correctLogPosition.csv");
        CSVWriter writerCorrectPosition = new CSVWriter(correctLogPositionWriter);
        String[] headerX1 = {"Instance Number", "Prediction", "Target", "Pos1", "Pos2"};
        writerCorrectPosition.writeNext(headerX1);

        FileWriter correctLogVariableIdentifierWriter = new FileWriter("correctLogVariableIdentifier.csv");
        CSVWriter writerCorrectLogVariableIdentifier = new CSVWriter(correctLogVariableIdentifierWriter);
        writerCorrectLogVariableIdentifier.writeNext(headerX);

        FileWriter incorrectLogVariableIdentifierWriter = new FileWriter("incorrectLogVariableIdentifier.csv");
        CSVWriter writerIncorrectLogVariableIdentifier = new CSVWriter(incorrectLogVariableIdentifierWriter);
        writerIncorrectLogVariableIdentifier.writeNext(headerX);

        // create CSVWriter object filewriter object as parameter
        FileWriter predictedLog = new FileWriter("predictedLogsOnly.csv");
        CSVWriter writerLogPrediction = new CSVWriter(predictedLog);
        String[] header0 = {"Instance Number", "Prediction", "Target"};
        writerLogPrediction.writeNext(header0);

        FileWriter logLevelWriter = new FileWriter("logLevel.csv");
        CSVWriter writerLogLevel = new CSVWriter(logLevelWriter);
        String[] header1 = {"Instance Number", "Prediction", "Target"};
        writerLogLevel.writeNext(header1);

        FileWriter logPositionWriter = new FileWriter("logPosition.csv");
        CSVWriter writerLogPosition = new CSVWriter(logPositionWriter);
        String[] header2 = {"Instance Number", "Prediction", "Target", "Prediction log pos", "Target log pos"};
        writerLogPosition.writeNext(header2);

        FileWriter correctLogMessageWriter = new FileWriter("correctLogMessage.csv");
        CSVWriter writerLogMessage = new CSVWriter(correctLogMessageWriter);
        String[] header3 = {"Instance Number", "Prediction", "Target"};
        writerLogMessage.writeNext(header3);

        FileWriter outputFileDistance = new FileWriter("logLevelDistance.csv");
        CSVWriter writerDistance = new CSVWriter(outputFileDistance);
        String[] header6 = {"Instance Number", "Prediction", "Target", "Distance"};
        writerDistance.writeNext(header6);

        FileWriter bleuLogMsgReference = new FileWriter("bleuLogMsgReference.txt");
        FileWriter bleuLogMsgHypothesis = new FileWriter("bleuLogMsgHypothesis.txt");


        ////////////////////


        List<String> logLevels = new ArrayList<>();
        logLevels.add("trace");
        logLevels.add("debug");
        logLevels.add("info");
        logLevels.add("warn");
        logLevels.add("error");
        logLevels.add("fatal");

        int itemsSize = prediction_list.size();
        int progress = 0;

        for (int j = 0; j < itemsSize; j++) {
            progress += 1;

            String inputItem = input_list.get(j);
            String targetItem = target_list.get(j);
            String predItem = prediction_list.get(j);
            String logStmt = log_stmt_list.get(j);

            String flattenedLogStmt = String.join("", logStmt.split(" "));
//            System.out.println(flattenedLogStmt);

            //Get logMessage for the target
            String spacedTargetLogMessage = Runner.getLogMessage(logStmt);
            String targetLogMessage = String.join("", spacedTargetLogMessage.split(" "));
//            System.out.println(targetLogMessage);


            //Get logLevel for the target
            String logLevelTarget = String.join("", Runner.getLevelLog(logStmt).split(" "));

            //Get the logging variable identifier target
            String lvlSplit = "."+logLevelTarget;
            String logVariableIdentifierTarget = StringUtils.substringBefore(flattenedLogStmt, lvlSplit);

            //Create folder for each input, target and prediction file
//            TODO: change when necessary
            String basePath = "/Users/ferrava/Desktop/logdiff/target/out/" + j;
            //String basePath = "/Users/antonio/Desktop/NoPretraining-Files";

            File f1 = new File(basePath);
            boolean bool = f1.mkdir();

            // Create Java classes containing the current method instance
            
            String inputFile = basePath + "/input.java";
            String targetFile = basePath + "/target.java";
            String predFile = basePath + "/prediction.java";

            String classInputToWrite = "public class A { " + inputItem + " }";
            FileWriter myWriter = new FileWriter(inputFile, false);
            myWriter.write(classInputToWrite);
            myWriter.close();

            String classTargetToWrite = "public class A { " + targetItem + " }";
            myWriter = new FileWriter(targetFile, false);
            myWriter.write(classTargetToWrite);
            myWriter.close();

            String classPredToWrite = "public class A { " + predItem + " }";
            myWriter = new FileWriter(predFile, false);
            myWriter.write(classPredToWrite);
            myWriter.close();

            // Create a tree representation of Input method, Target method, and Prediction method
            // (take prev created files)
            Tree input;
            try {
                input = TreeGenerators.getInstance().getTree(inputFile).getRoot();
            } catch (Exception e) {
                unparsableInputCounter += 1;
                continue;
            }
            Tree target;
            try {
                target = TreeGenerators.getInstance().getTree(targetFile).getRoot();
            } catch (Exception e) {
                unparsableInputCounter += 1;
                continue;
            }

            Tree prediction;
            try {
                prediction = TreeGenerators.getInstance().getTree(predFile).getRoot();
            } catch (Exception e) {
                //Cannot construct the tree for the prediction, therefore we analyze this one with the CodeBleu
                unparsable_counter += 1;
                continue;

            }

            Matcher defaultMatcher = Matchers.getInstance().getMatcher(); // retrieves the default matcher
            
            // Get edit differences between Input method and Target method
            MappingStore mappingsToTarget = defaultMatcher.match(input, target); // computes the mappings between the trees
            EditScriptGenerator editScriptGeneratorTarget = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
            EditScript actionsTarget = editScriptGeneratorTarget.computeActions(mappingsToTarget); // computes the edit script

            // Get edit differences between Input method and Target method 
            MappingStore mappingsToPrediction = defaultMatcher.match(input, prediction); // computes the mappings between the trees
            EditScriptGenerator editScriptGeneratorPrediction = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
            EditScript actionsPrediction = editScriptGeneratorPrediction.computeActions(mappingsToPrediction); // computes the edit scrip

            // if no edit actions found between Input and Prediction
            // write log stmt in 'predictedLogsOnly'
            if (actionsPrediction.size() == 0) {
                String[] data0 = {String.valueOf(j), "", logStmt};
                writerLogPrediction.writeNext(data0);
                unparsableLogStmt += 1;
                continue;
            }


            int difference = Integer.MAX_VALUE;
            int targetEditAction = 0;

            int startPosTarget = -1;
            int endPosTarget = -1;


            // get start end pos of target log stmt
            for (int i = 0; i < actionsTarget.size(); i++) {

                int posStart1 = actionsTarget.get(i).getNode().getPos();
                int posEnd1 = actionsTarget.get(i).getNode().getEndPos();
                String sub = String.join("", classTargetToWrite.substring(posStart1, posEnd1).split(" "));
                if (sub.equals(flattenedLogStmt)) {
                    startPosTarget = posStart1;
                    endPosTarget = posEnd1;
                    break;
                }
            }

            // picking the nearest edit action to the target one
            // get edit action (between input-prediction) w/ position closest to target log stmt pos
            for (int i = 0; i < actionsPrediction.size(); i++) {

                int newRelativePosition = Math.abs(startPosTarget-actionsPrediction.get(i).getNode().getPos());
                if (newRelativePosition<difference)
                    difference = newRelativePosition;
                targetEditAction = i;
            }

            // get stat and end position of edit action
            int startPosPrediction = -1;
            int endPosPrediction = -1;

            startPosPrediction = actionsPrediction.get(targetEditAction).getNode().getPos();
            endPosPrediction = actionsPrediction.get(targetEditAction).getNode().getEndPos();


            String wrappedPred = "";
            String finalString = "";
            String[] itemsString = null;
            try {
                wrappedPred = "public class A { " + predItem + " }";
                finalString = wrappedPred.substring(startPosPrediction, endPosPrediction);
                itemsString = finalString.split(" ");
            }catch (Exception e){
                String[] data0 = {String.valueOf(j), finalString, logStmt};
                writerLogPrediction.writeNext(data0);
                unparsableLogStmt += 1; // this log stmt can not be parsed correctly
                continue;
            }

            String item0="";
            String item1="";
            String item2="";
            String itemLast="";

//             split "prediction item" into 4 elements
            try {
                item0 = itemsString[0].toLowerCase();
                item1 = itemsString[1].toLowerCase();
                item2 = itemsString[2].toLowerCase();
                itemLast = itemsString[itemsString.length - 1];
            }catch(Exception e){
                unparsableLogStmt += 1; // this log stmt can not be parsed correctly
                continue;
            }


            //writing The predicted log on txt File
            String[] data0 = {String.valueOf(j), finalString, logStmt};
            writerLogPrediction.writeNext(data0);


            // if prediction edit action in target log stmt position is a log
            if (((item2.contains("log") && item1.contains(".")) || (item0.contains("log") || item1.contains("log"))) && itemLast.contains(";")) {

                String logLevelPrediction = String.join("", Runner.getLevelLog(finalString).split(" "));
                String spacedPredLogMessage = Runner.getLogMessage(finalString);
                String predLogMessage = String.join("", spacedPredLogMessage.split(" "));

                bleuLogMsgHypothesis.write(spacedPredLogMessage+'\n');
                bleuLogMsgReference.write(spacedTargetLogMessage+'\n');

                // if getLevelLog did not got the log level
                if(logLevelPrediction.equals("")) {
                    int startFrom = 0;
                    // check if log level is `log` or `warning`
                    for (String token : finalString.split(" ")) {
                        if (token.equals("log") || token.equals("warning")) {
                            break;
                        }
                        startFrom += 1;
                    }
                    // try to get level
                    String [] subStr = finalString.substring(startFrom).split(" ");
                    String joinedString = "";

                    for(int k=startFrom+1; k<subStr.length-1; k++){
                        joinedString = joinedString + " " + subStr[k];
                    }

                    // check if level is a valid level and if found
                    int diff=-1;
                    String[] subArray = finalString.split(" ");
                    try {
                        for (String token : Arrays.copyOfRange(subArray, startFrom + 1, subArray.length - 1)) {
                            if (logLevels.contains(token)) {
                                diff = getDifferenceLogLevel(logLevelTarget, token);
                                logLevelPrediction = token;
                            }
                        }
                    }catch(Exception e){
                        logLevelPrediction="";
                    }
                }

                //Retrieve correct cardinal difference only for those log for which there is a Log4J/SLF4j mapping
                if(!logLevelTarget.equals("log") && !logLevelTarget.equals("warning") && !logLevelPrediction.equals("")) {

                    int distance = getDifferenceLogLevel(logLevelTarget, logLevelPrediction);
                    String[] data1 = {String.valueOf(j), logLevelPrediction, logLevelTarget, String.valueOf(distance)};
                    writerDistance.writeNext(data1);

                    // Keep track of log position
                    if (startPosPrediction == startPosTarget) {
                        String [] data2 = {String.valueOf(j), predItem, targetItem, Integer.toString(startPosPrediction), Integer.toString(startPosTarget)};
                        writerLogPosition.writeNext(data2);
                        writerCorrectPosition.writeNext(data2);
                        matchingPosition += 1;
                    }
                    else{
                        try {
                            String offsetTarget = classTargetToWrite.substring(17, startPosTarget);
                            String offsetPrediction = "";
                            if (startPosPrediction == 0) {
                                offsetPrediction = wrappedPred.substring(17);
                                System.out.println(j);
                            } else {
                                offsetPrediction = wrappedPred.substring(17, startPosPrediction);
                            }
                            String[] data3 = {String.valueOf(j), predItem, targetItem, offsetPrediction, offsetTarget};
                            writerLogPosition.writeNext(data3);
                        }catch (Exception e){
                            outRangeError += 1;
                        }
                    }

                    // Keep track of matching log levels
                    if (logLevelPrediction.equals(logLevelTarget)) {
                        matchingLevel += 1;
                        String[] data = {String.valueOf(j), finalString, logLevelTarget};
                        writerLogLevel.writeNext(data);
                        writerCorrectLevel.writeNext(data);
                    }

                    // Keep track of matching msgs
                    if (predLogMessage.equals(targetLogMessage)) {
                        matchingMessage += 1;
                        String[] data = {String.valueOf(j), finalString, targetLogMessage};
                        writerLogMessage.writeNext(data);
                    }

                    // Keep track of matching log variable identifiers
                    String flattenedFinalString =  String.join("", finalString.split(" "));
                    String logVariableIdentifierPrediction = StringUtils.substringBefore(flattenedFinalString, "."+logLevelPrediction);

                    if(logVariableIdentifierPrediction.equals(logVariableIdentifierTarget)){
                        matchingLogVariableIdentifier += 1;
                        String[] data = {String.valueOf(j), finalString, logVariableIdentifierTarget};
                        writerCorrectLogVariableIdentifier.writeNext(data);
                    }else{
                        String[] data = {String.valueOf(j), finalString, logVariableIdentifierTarget};
                        writerIncorrectLogVariableIdentifier.writeNext(data);
                    }
                }else{
                    unparsableLogStmt += 1; // this log stmt can not be parsed correctly
                }
                // We have a matching level. Keep track of those one
//                if (logLevelPrediction.equals(logLevelTarget)) {
//                    matchingLevel += 1;
//                    String[] data1 = {String.valueOf(j), finalString, logLevelTarget};
//                    writerLogLevel.writeNext(data1);
//                }
//
//                if (predLogMessage.equals(targetLogMessage)) {
//                    matchingMessage += 1;
//                    String[] data1 = {String.valueOf(j), finalString, targetLogMessage};
//                    writerLogMessage.writeNext(data1);
//                }
            }else{
                unparsableLogStmt += 1; // this log stmt can not be parsed correctly
            }
            System.out.printf("Progress: %d/%d\r", progress, itemsSize);
        }

        logLevelWriter.close();
        logPositionWriter.close();
        writerDistance.close();
        writerLogPrediction.close();
        writerCorrectLevel.close();
        writerCorrectPosition.close();
        correctLogMessageWriter.close();
        correctLogVariableIdentifierWriter.close();
        incorrectLogVariableIdentifierWriter.close();
        bleuLogMsgHypothesis.close();
        bleuLogMsgReference.close();

        int perfectPredictionNums = 7685; // single-log (tokens)
//        int perfectPredictionNums = 7109; // single-log (ast)
//        int perfectPredictionNums = 7710; // top-1
//        int perfectPredictionNums = 7810; // top-3
//        int perfectPredictionNums = 7824; // top-5
        int instances = 28698;
        System.out.println(instances + " " + unparsableInputCounter + " " + outRangeError);
        instances = instances - unparsableInputCounter - outRangeError;
        double accuracy = ((double) perfectPredictionNums / instances) * 100;
        double percentagePositionCorrect = ( (perfectPredictionNums + (double) matchingPosition) / instances) * 100;
        double percentageLevelCorrect = ( (perfectPredictionNums + (double )matchingLevel) / instances) * 100;
        double percentageMessageCorrect = ( (perfectPredictionNums + (double )matchingMessage) / instances) * 100;
        double percentageWrongSyntax =  ( ( (double) unparsable_counter) /instances) * 100;
        double percentageUnparsableLogStmts = ((double) unparsableLogStmt / instances) *100;
        double percentageLogVariableIdentifierCorrect = (( perfectPredictionNums + (double) matchingLogVariableIdentifier) / instances) *100;


        System.out.println("outRangeError: " + outRangeError);
        System.out.println("wrong input: " + unparsableInputCounter);
        System.out.printf("√ √ √ √\t%d\t%s\t(all)\n", perfectPredictionNums, accuracy);
        System.out.printf("- - - -\t%d\t%s\t(wrong syntax)\n", unparsable_counter, percentageWrongSyntax);
        System.out.printf("- - - -\t%d\t%s\t(non-parsable logs)\n", unparsableLogStmt, percentageUnparsableLogStmts);
        System.out.printf("√ - - -\t%d\t%s\n", (perfectPredictionNums+matchingLevel), percentageLevelCorrect);
        System.out.printf("- √ - -\t%d\t%s\n", (perfectPredictionNums+matchingPosition), percentagePositionCorrect);
        System.out.printf("- - √ -\t%d\t%s\n", (perfectPredictionNums + matchingMessage), percentageMessageCorrect);
        System.out.printf("- - - √\t%d\t%s\n", (perfectPredictionNums+matchingLogVariableIdentifier), percentageLogVariableIdentifierCorrect);

    }
}