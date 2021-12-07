import java.io.*;
import java.util.*;

public class nGramTest {

    static class nGramThread extends Thread {

        private String[] chunk;

        private String previousLast;

        private HashMap<String, Integer> result = new HashMap<>();

        public nGramThread(String chunk, String previousLast) {
            this.chunk = chunk.split(" ");
            this.previousLast = previousLast;
        }

        @Override
        public void run() {
            String bigram;
            if (!previousLast.equals("")) {
                bigram = previousLast + " " + chunk[0];
                if (result.containsKey(bigram)) {
                    result.put(bigram, result.get(bigram) + 1);
                } else {
                    result.put(bigram, 1);
                }
            }
            String delimiter = " ";
            for (int i = 0; i < chunk.length - 1; i++) {
                bigram = String.join(delimiter, chunk[i], chunk[i + 1]);
                if (result.containsKey(bigram)) {
                    result.put(bigram, result.get(bigram) + 1);
                } else {
                    result.put(bigram, 1);
                }
            }
        }

        public HashMap<String, Integer> getResult() {
            return result;
        }
    }

    public static nGramThread[] chunkCorpus() throws IOException {
        long startRead = System.currentTimeMillis();
        FileInputStream inputStream = new FileInputStream("C:\\Users\\baris\\Desktop\\ParalelTest\\src\\news.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int lineCount = 0;
        while (br.readLine() != null) lineCount++;
        long readTime = System.currentTimeMillis() - startRead;
        System.out.println("Loading the file took " + readTime + "ms.");
        inputStream.getChannel().position(0);
        br = new BufferedReader(new InputStreamReader(inputStream));
        int numOfThreads = 64;
        nGramThread[] threads = new nGramThread[numOfThreads];
        int opsPerThread = lineCount / numOfThreads;
        List<String> corpusLines = new ArrayList<>();
        String line;
        String chunk;
        int counter;
        String previousLast = "";
        for (int i = 0; i < numOfThreads; i++) {
            counter = 0;
            if (i > 0) {
                if (corpusLines.size() != 0) {
                    previousLast = corpusLines.get(corpusLines.size() - 1).substring(corpusLines.get(corpusLines.size() - 1).lastIndexOf(" ") + 1);
                    corpusLines.clear();
                } else {
                    previousLast = "";
                }
            }
            while (counter < opsPerThread && (line = br.readLine()) != null){
                if (line.equals("")) counter--;
                else {
                    corpusLines.add(line);
                    counter++;
                }
            }
            chunk = String.join(" ", corpusLines);
            threads[i] = new nGramThread(chunk, previousLast);
            threads[i].start();
        }
        return threads;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        HashMap<String, Integer> result = new HashMap<>();

        long start = System.currentTimeMillis();
        nGramThread[] threads = chunkCorpus();

        for (nGramThread thread : threads) {
            thread.join();
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Execution took: " + duration + "ms.");

        for (nGramThread thread : threads) {
            thread.getResult().forEach((s, integer) -> result.merge(s, integer, Integer::sum));
        }

        File outputFile = new File("/home/monepicor/repos/yeap/output.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        try (bw) {
            result.forEach((key, value) -> {
                try {
                    bw.write(key + " >> " + value + System.lineSeparator());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }

    }
}
