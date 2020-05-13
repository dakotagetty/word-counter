import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * This class takes in an input file of words, counts and alphabetizes them to
 * be placed into a table in an html document. The class StringLT and the
 * methods generateElements and nextWordOrSeparator are methods used in
 * homeworks and labs in CSE 2221.
 *
 * @author Dakota Getty
 *
 */
public final class WordCounter {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private WordCounter() {
    }

    /**
     * Compare {@code String}s in lexicographic order.
     */
    private static class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param strSet
     *            the {@code Set} to be replaced
     * @replaces strSet
     * @ensures strSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> strSet) {
        assert str != null : "Violation of: str is not null";
        assert strSet != null : "Violation of: strSet is not null";

        for (int x = 0; x < str.length(); x++) {
            Character temp = str.charAt(x);
            if (!strSet.contains(temp)) {
                strSet.add(temp);
            }
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String next = "";
        Character temp = text.charAt(position);
        boolean loop = true;
        if (separators.contains(temp)) {
            while (separators.contains(temp) && loop) {
                next += temp;
                position++;
                if (position < text.length()) {
                    temp = text.charAt(position);
                } else {
                    loop = false;
                }
            }
        } else {
            while (!separators.contains(temp) && loop) {
                next += temp;
                position++;
                if (position < text.length()) {
                    temp = text.charAt(position);
                } else {
                    loop = false;
                }
            }
        }
        return next;
    }

    /**
     *
     * @param input
     *            The input file to be counted
     * @param folder
     *            The folder to store the data
     * @param inputName
     *            The name of the input file
     *
     */
    public static void createHTML(String inputName, SimpleReader input,
            String folder) {
        /*
         * The html file is created with a simple header and table. The words in
         * the alphabetized queue are added in the first column, and then the
         * storedMap Map is searched for that word, and the corresponding key is
         * added to the second column to match with the word.
         */
        String indexName = "wordCounter.html";
        SimpleWriter index = new SimpleWriter1L(folder + "/" + indexName);
        index.println("<html><head><title>WordCounter</title></head><body>");
        index.println("<h1>Words Counted in " + inputName + "</h1>");
        index.println("<hr>");
        index.println("<table border = \"1\">");
        index.println("<tr><th>Words</th><th>Counts</th><tr>");
        Map<String, Integer> words = enterIntoMap(input);
        Map<String, Integer> storedMap = new Map1L<>();
        Queue<String> sortedWords = sortWords(words, storedMap);
        while (sortedWords.length() > 0) {
            String word = sortedWords.dequeue();
            int number = storedMap.value(word);
            index.println(
                    "<tr><td>" + word + "</td><td>" + number + "</td></tr>");
        }
        index.println("</table>");
        index.println("</body>");
        index.println("</html>");
        index.close();
    }

    /**
     *
     * @param input
     *            The input file to be counted
     * @return The sequence of words
     */
    public static Map<String, Integer> enterIntoMap(SimpleReader input) {
        Map<String, Integer> words = new Map1L<>();
        /*
         * The set of separator strings are created, and then the input file is
         * scanned and is analyzed and separated into words and separators. Each
         * word that the file gives is checked by the Map words, and if the
         * current word already exists in the map then the value of the
         * corresponding key is increased by one, otherwise it is added as a new
         * element to the map.
         */
        final String separatorStr = " \t\n\r,-.!?[]';:/()";
        Set<Character> separatorSet = new Set1L<>();
        generateElements(separatorStr, separatorSet);
        while (!input.atEOS()) {
            int position = 0;
            String line = input.nextLine();
            while (position < line.length()) {
                String token = nextWordOrSeparator(line, position,
                        separatorSet);
                if (!(separatorSet.contains(token.charAt(0)))) {
                    if (words.hasKey(token)) {
                        int tempCount = words.value(token) + 1;
                        words.replaceValue(token, tempCount);
                    } else {
                        words.add(token, 1);
                    }
                }
                position += token.length();
            }
        }
        return words;
    }

    /**
     * @param words
     *            The map of words to be organized.
     * @param indexMap
     *            The transferred list of words and their respective
     *            occurrences.
     * @replaces indexMap
     * @clears words
     * @return The sorted queue of words.
     */
    public static Queue<String> sortWords(Map<String, Integer> words,
            Map<String, Integer> indexMap) {
        /*
         * The String values in the Map words are removed and placed into a new
         * queue in no order. A new map takes in each removed value to store the
         * removed values. The queue of words is then alphabetized and returned.
         */
        Queue<String> sortedWords = new Queue1L<>();
        while (words.size() > 0) {
            Map.Pair<String, Integer> temp = words.removeAny();
            sortedWords.enqueue(temp.key());
            indexMap.add(temp.key(), temp.value());
        }
        Comparator<String> alphabetize = new StringLT();
        sortedWords.sort(alphabetize);
        return sortedWords;
    }

    /**
     * Main method. Takes in a text file and the name of a previously existing
     * folder and calls createHTML.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        out.println("Enter an input file name");
        String inputName = in.nextLine();
        SimpleReader inputFile = new SimpleReader1L(inputName);
        out.println("Enter an output folder name");
        String outputFolder = in.nextLine();
        createHTML(inputName, inputFile, outputFolder);
        /*
         * Close input and output streams
         */
        in.close();
        out.close();
    }

}
