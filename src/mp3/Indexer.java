package mp3;

import java.util.*;
import java.io.*;

public class Indexer {
    private static char KEYWORD_DELIM = ' ';
    private static String KEY = "illinois";

    /*
     * Indexes a single message into a mapping of keywords to sets of messages,
     * given a string containing separators and a list of stopwords. Adds the
     * message ID to the set of any keywords found in the message.
     *
     * Parameters:
     * map: Map to add index data to.
     * messagePath: Message filepath.
     * messageID: Message ID.
     * separators: String of separators.
     * stopwords: List of stopwords.
     *
     * Returns:
     * Map with added index data.
     */


    static class IndexSingleMessageThread extends Thread {
        private Map<String, Set<String>> classMap;

        public Map<String, Set<String>> getOutputMap() {
            return outputMap;
        }

        private Map<String, Set<String>> outputMap;
        private String classMessagePath;
        private String classMessageId;
        private String classSeparators;
        private List<String> classStopwords;

        IndexSingleMessageThread(Map<String, Set<String>> map, String messagePath, String messageID, String separators, List<String> stopwords) {
            classMap = map;
            classMessagePath = messagePath;
            classMessageId = messageID;
            classSeparators = separators;
            classStopwords = stopwords;
        }

        public Map<String, Set<String>> indexMessage(Map<String, Set<String>> map, String messagePath, String messageID, String separators, List<String> stopwords) throws IOException {
            Set<String> keywords;

            FileInputStream fileStream;
            DataInputStream dataIn;
            BufferedReader buffRead;

            StringTokenizer tokenizer;
            String messageLine;
            String newWord;

            Set<String> messages;

            keywords = new HashSet<String>();

            // PARSE EACH WORD OF THE MESSAGE
            // IF THE WORD IS NOT A STOPWORD, ADD IT TO THE SET OF KEYWORDS
            try {
                fileStream = new FileInputStream(messagePath);
                dataIn = new DataInputStream(fileStream);
                buffRead = new BufferedReader(new InputStreamReader(dataIn));
                while ((messageLine = buffRead.readLine()) != null) {
                    tokenizer = new StringTokenizer(messageLine, separators);
                    while (tokenizer.hasMoreTokens()) {
                        newWord = tokenizer.nextToken();
                        if (!stopwords.contains(newWord)) {
                            keywords.add(newWord);
                        }
                    }
                }
                dataIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // FOR EACH KEYWORD, ADD THIS MESSAGE TO ITS SET OF MESSAGES
            if (map == null) {
                map = new HashMap<String, Set<String>>();
            }
            for (String keyword : keywords) {
                messages = map.get(keyword);
                if (messages == null) {
                    messages = new HashSet<String>();
                }
                messages.add(messageID);
                map.put(keyword, messages);
            }

            return map;
        }

        public void run() {
            try {
                outputMap = indexMessage(classMap, classMessagePath, classMessageId, classSeparators, classStopwords);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /*
     * Indexes a list of messages into a mapping of keywords to sets of messages,
     * given a file containing separators and stopwords.
     *
     * Parameters:
     * map: Map to add index data to (null to create a new map).
     * messagePaths: List of message filepaths (must be same length as messageIDs).
     * messageIDs: List of message IDs (must be same length as messagePaths).
     * separatorsPath: Filepath for separators.
     * stopwordsPath: Filepath for stopwords.
     *
     * Returns:
     * Map with added index data.
     */
    public static Map<String, Set<String>> indexMessages(Map<String, Set<String>> map, List<String> messagePaths, List<String> messageIDs, String separatorsPath, String stopwordsPath) throws IOException {
        /*FileReader fileRead;
        String currString;
        int nextChar;*/
        String separators;

        FileInputStream fileStream;
        DataInputStream dataIn;
        BufferedReader buffRead;
        String stopword;
        List<String> stopwords;

        if (map == null) {
            map = new HashMap<String, Set<String>>();
        }
        if (messagePaths.size() != messageIDs.size() || messagePaths.size() == 0) {
            return map;
        }

        // PARSE IN SEPARATORS
/*        fileRead = new FileReader(separatorsPath);
        separators = "";

        currString = "";
        nextChar = fileRead.read();
        while (nextChar != -1) {
            separators += (char) nextChar;
            nextChar = fileRead.read();
        }

        fileRead.close();*/

        separators = "\t\n\r\\x0b\\x0c ";

        // PARSE IN STOPWORDS
        stopwords = new ArrayList<String>();
        try {
            fileStream = new FileInputStream(stopwordsPath);
            dataIn = new DataInputStream(fileStream);
            buffRead = new BufferedReader(new InputStreamReader(dataIn));
            while ((stopword = buffRead.readLine()) != null) {
                stopwords.add(stopword);
            }
            dataIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // INDEX MESSAGES
        List<IndexSingleMessageThread> threads = new ArrayList<IndexSingleMessageThread>();
        for (int i = 0; i < messagePaths.size(); i++) {
            try {
                IndexSingleMessageThread msgThred = new IndexSingleMessageThread(map, messagePaths.get(i), messageIDs.get(i), separators, stopwords);
                threads.add(msgThred);
                checkAndRunThreads(threads);
            } catch (Exception e) {
                e.printStackTrace();
            }
            map = checkAndMergeThreads(threads);
        }
        while(threads.size() > 0) {
            checkAndRunThreads(threads);
            map = checkAndMergeThreads(threads);
        }
        return map;
    }

    private static void checkAndRunThreads(List<IndexSingleMessageThread> threads) {
        if (threads.size() < 20) {
            // Find threads that are new and try and start them
            for (int t = 0; t < threads.size(); t++) {
                IndexSingleMessageThread thread = threads.get(t);
                if (thread.getState() == Thread.State.NEW) {
                    thread.run();
                }
            }
        } else {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, Set<String>> checkAndMergeThreads(List<IndexSingleMessageThread> threads) {
        Map<String, Set<String>> outputMap = new HashMap<String, Set<String>>();
        // Iterate through the threads and get the output from the finished ones and then remove them from the list
        for (int t = 0; t < threads.size(); t++) {
            IndexSingleMessageThread thread = threads.get(t);
            if (thread.getState() == Thread.State.TERMINATED) {
                outputMap = thread.getOutputMap();
                threads.remove(t);
                outputMap = mergeMaps(outputMap, outputMap);
            }
        }
        return outputMap;
    }

    /*
     * Writes a mapping of keywords to message ID's to a file of the following
     * format:
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...
     *
     * Parameters:
     * map: Map to write to the file.
     * indexPath: The filepath for the index file.
     *
     * Returns:
     * Whether or not the operation was successful.
     */
    public static Boolean mapToIndexFile(Map<String, Set<String>> map, String indexPath) throws IOException {
        FileWriter fileWrite;
        String toWrite;

        if (map == null) {
            return false;
        }

        fileWrite = new FileWriter(indexPath);

        for (String keyword : map.keySet()) {
            toWrite = keyword + KEYWORD_DELIM;
            for (String messageID : map.get(keyword)) {
                toWrite += messageID + KEYWORD_DELIM;
            }
            toWrite = toWrite.substring(0, toWrite.length() - 1) + '\n';

            for (char nextChar : toWrite.toCharArray()) {
                fileWrite.write(nextChar);
            }
        }

        fileWrite.close();

        return true;
    }

    /*
     * Parses a mapping of keywords to message ID's from a file.
     *
     * Parameters:
     * indexPath: The filepath for the index file.
     *
     * Returns:
     * The parsed mapping. If there is a failure in execution, null is returned.
     */
    public static Map<String, Set<String>> indexFileToMap(String indexPath) throws IOException {
        Map<String, Set<String>> map;

        FileInputStream fileStream;
        DataInputStream dataIn;
        BufferedReader buffRead;

        String indexLine;
        String[] tokens;
        Set<String> currMessages;

        map = new HashMap<String, Set<String>>();

        try {
            fileStream = new FileInputStream(indexPath);
            dataIn = new DataInputStream(fileStream);
            buffRead = new BufferedReader(new InputStreamReader(dataIn));
            while ((indexLine = buffRead.readLine()) != null) {
                tokens = indexLine.split(" ");
                currMessages = new HashSet<String>();
                for (int i = 1; i < tokens.length; i++) {
                    currMessages.add(tokens[i]);
                }
                map.put(tokens[0], currMessages);
            }
            dataIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    /*
     * Writes a mapping of keywords to message ID's to files of the following
     * format, with the name of each file being SHA-256(keyword):
     * <keyword><KEYWORD_DELIM><message id><KEYWORD_DELIM><message id>...
     *
     * Parameters:
     * map: Map to write to the file.
     * indexPath: The base filepath for the index files.
     *
     * Returns:
     * Whether or not the operation was successful.
     */
    public static Boolean mapToIndexFiles(Map<String, Set<String>> map, String indexPath) throws IOException {
        FileWriter fileWrite;
        MP3Encryption encryption;
        String toWrite;
        String fileName = "";
        if (map == null) {
            return false;
        }

        encryption = new MP3Encryption(KEY);

        for (String keyword : map.keySet()) {

//        	if (Character.isLetter(keyword.charAt(0))) {
//        			fileName = Character.toLowerCase(keyword.charAt(0)) + "_keyword.txt";
//        	}
//        	else if (Character.isDigit(keyword.charAt(0))) {
//        		fileName = "num_keyword.txt";
//        	}
//        	else {
//        		fileName = "misc_keyword.txt";
//        	}
//            fileWrite = new FileWriter(indexPath + "\\" + fileName, true); //+ encryption.hash(keyword));
            fileWrite = new FileWriter(indexPath + "\\" + keyword, true); //+ encryption.hash(keyword));

            toWrite = keyword + KEYWORD_DELIM;
            for (String messageID : map.get(keyword)) {
                toWrite += messageID + KEYWORD_DELIM;
            }
            toWrite = toWrite.substring(0, toWrite.length() - 1) + '\n';

            //encryption.encrypt(toWrite);

            for (char nextChar : toWrite.toCharArray()) {
                fileWrite.write(nextChar);
            }

            fileWrite.close();
        }

        return true;
    }

    public static Map<String, Set<String>> mergeMaps(Map<String, Set<String>> map1, Map<String, Set<String>> map2) {
        Set<Map.Entry<String, Set<String>>> entries = map1.entrySet();
        for (Map.Entry<String, Set<String>> entry : entries) {
            Set<String> map2Value = map2.get(entry.getKey());
            if (map2Value == null) {
                map2.put(entry.getKey(), entry.getValue());
            } else {
                map2Value.addAll(entry.getValue());
            }
        }
        return map2;
    }

}