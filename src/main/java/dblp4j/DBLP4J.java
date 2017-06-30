package dblp4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

public final class DBLP4J {
    private DBLP4J() {};
    private static final Pattern patternPaperTitle = Pattern.compile("^#\\*");
    private static final Pattern patternYear = Pattern.compile("^#t");
    private static final Pattern patternPaperIndex = Pattern.compile("^#index");
    private static final Pattern patterCitedPaper = Pattern.compile("^#%");

    public static void main(String[] args) throws IOException {
         final String FILENAME = "src/main/resources/dblp-v4.txt";

        BufferedReader br = new BufferedReader(new FileReader(FILENAME));
        String line;
        String paperTitle = "";
        int publishedYear = 0;
        int paperId = 0;

        FileWriter titleWriter = new FileWriter("titie.txt");
        FileWriter citationWriter = new FileWriter("citation.txt");

        while((line = br.readLine()) != null){
            if (patternPaperTitle.matcher(line).find()){
                paperTitle = line.substring(2);
            }else if (patternPaperIndex.matcher(line).find()){
                paperId = Integer.parseInt(line.substring(6));
                titleWriter.write(paperId + " " + paperTitle + "\n");
            }else if(patternYear.matcher(line).find()){
                publishedYear = Integer.parseInt(line.substring(2));
            }else if (patterCitedPaper.matcher(line).find()){
                citationWriter.write(paperId + " " + Integer.parseInt(line.substring(2)) + " " + publishedYear + "\n");
            }
        }

        titleWriter.close();
        citationWriter.close();
    }
}
