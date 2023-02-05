import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

class Occurence {
    int index;
    String token;
    String name;

    public Occurence(int index, String token, String name) {
        this.index = index;
        this.token = token;
        this.name = name;
    }
}

public class App {
    public static void main(String[] args) throws Exception {
        Scanner userInputScanner = new Scanner(System.in);  // Create a Scanner object

        System.out.println("Enter the file name to analyze: ");

        String fileName = userInputScanner.nextLine();  // Read user input
        userInputScanner.close();
        
        Scanner scanner = new Scanner(new File(fileName));

        int line = 0;
        while (scanner.hasNextLine()) {
            line++;

            String[] keywords = { "int", "double", "String" };
            String[] operators = { "=", "(", ")", "+", "-", "*", "/", ",", ";" };

            String testString = scanner.nextLine();

            List<Occurence> occurences = new ArrayList<>();

            // find all keywords
            for (String keyword : keywords) {
                occurences = findOccurences(occurences, testString, keyword, "keyword");
            }

            // find all operators
            for (String operator : operators) {
                occurences = findOccurences(occurences, testString, operator, "operator");
            }

            // replace all tokens with whitespace so that string splitting is easier
            for (Occurence occurence : occurences) {
                String regexTarget = occurence.name;

                String whitespace = " ";
                whitespace = whitespace.repeat(occurence.name.length());

                testString = testString.replace(regexTarget, whitespace);
            }

            String[] remainingStrings = testString.trim().split("\\s+");

            // create list to store remaining strings
            List<Occurence> unknownOccurences = new ArrayList<>();

            // add indices to all remaining strings
            for (String str : remainingStrings) {
                int index = testString.indexOf(str);

                Occurence unknownOccurence = new Occurence(index, "unknown", str);

                unknownOccurences.add(unknownOccurence);
            }

            for (Occurence unknownOccurence : unknownOccurences) {
                String name = unknownOccurence.name;

                // check if it contains a period (.) AND is not a floating point
                if (name.contains(".") && !(isDouble(name))) {
                    String before = "";
                    String after = "";
                    int afterIndex = 0;
                    boolean hitPeriod = false;
                    for (int i = 0; i < name.length(); i++) {
                        // once the period is reached
                        if (name.charAt(i) == '.') {
                            before = before + ".";
                            afterIndex = i + 1;
                            hitPeriod = true;
                        }
                        // before the period is reached
                        else if (!hitPeriod) {
                            before = before + name.charAt(i);
                        }

                        // after the period is reached
                        else {
                            after = after + name.charAt(i);
                        }
                    }
                    Occurence beforeOccurence = new Occurence(unknownOccurence.index, "unknown", before);
                    Occurence afterOccurence = new Occurence(unknownOccurence.index + afterIndex, "unknown", after);


                    // remove the original occurence
                    unknownOccurences.remove(unknownOccurence);

                    // add the two just discovered
                    unknownOccurences.add(beforeOccurence);
                    unknownOccurences.add(afterOccurence);

                    break;

                }
            }

            for (Occurence unknownOccurence : unknownOccurences) {
                String name = unknownOccurence.name;

                // check if it is an Identifier
                if (Character.isLetter(name.charAt(0)) && isAlphanumeric(name))
                    unknownOccurence.token = "identifier";

                // check if it is a quote
                else if (name.startsWith("\"") && name.endsWith("\""))
                    unknownOccurence.token = "String constant";

                else if (isDouble(name))
                    unknownOccurence.token = "double constant";

                // check if it contains a number
                else if (name.matches("-?\\d+"))
                    unknownOccurence.token = "int constant";

                else
                    unknownOccurence.token = "error";

                occurences.add(unknownOccurence);
            }

            // sort occurences by index
            Collections.sort(occurences, (Occurence a, Occurence b) -> a.index - b.index);

            // going through all occurences of anything
            for (Occurence occurence : occurences) {
                int realIndex = occurence.index + 1;
                System.out.println("Line" + line + ": " + realIndex + " " + occurence.token + ": " + occurence.name);
            }
        }

    }

    static List<Integer> findSubstringIndices(String str, String substring) {
        int index = 0;
        List<Integer> indices = new ArrayList<>();
        while ((index = str.indexOf(substring, index)) != -1) {
            indices.add(index);
            index++;
        }

        return indices;
    }

    // find occurences of keywords or operators
    static List<Occurence> findOccurences(List<Occurence> occurences, String searchString, String substring,
            String token) {
        List<Integer> indices = findSubstringIndices(searchString, substring);
        for (int index : indices) {
            Occurence intOccurence = new Occurence(index, token, substring);
            occurences.add(intOccurence);
        }

        return occurences;
    }

    static boolean isAlphanumeric(String s) {
        if (s == null) { // checks if the String is null
            return false;
        }
        int len = s.length();
        for (int i = 0; i < len; i++) {
            // checks whether the character is not a letter
            // if it is not a letter ,it will return false
            if ((Character.isLetterOrDigit(s.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    static boolean isDouble(String s) {
        return Pattern.matches("([0-9]*)\\.([0-9]*)", s);
    }

}
