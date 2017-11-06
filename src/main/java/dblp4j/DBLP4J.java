package dblp4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class DBLP4J {
    private DBLP4J() {}
    private static final Pattern patternPaperTitle = Pattern.compile("^#\\*");
    private static final Pattern patternYear = Pattern.compile("^#t");
    private static final Pattern patternPaperIndex = Pattern.compile("^#index");
    private static final Pattern patternCitedPaper = Pattern.compile("^#%");
    private static final Pattern patternConference = Pattern.compile("^#c");
    private static final Pattern patternAuthors = Pattern.compile("^#@");

    public static void main(String[] args) throws IOException {
        final String FILENAME = "src/main/resources/dblp-v4.txt";

        BufferedReader br = new BufferedReader(new FileReader(FILENAME));
        String line;

        // temporal variable for each paper information
        String paperTitle = "";
        String conferenceName = "";
        int paperId = 0;
        int publishedYear = 0;
        List<Integer> citationPapers = new ArrayList<>();
        String[] authors = null;

        Map<String, Set<String>> authorBelongs2Conferences = new HashMap<>();
        Map<Integer, String[]> paperId2Authors = new HashMap<>();

        FileWriter titleWriter = new FileWriter("title_year.tsv");
        FileWriter paperConferenceWriter = new FileWriter("paper_conference_label.tsv");
        FileWriter paperCitationWriter = new FileWriter("paper_citation_edge.tsv");
        FileWriter authorConferenceWriter = new FileWriter("author_conference_label.tsv");

        while ((line = br.readLine()) != null) {
            if (patternPaperTitle.matcher(line).find()) {
                paperTitle = line.substring(2);
            } else if (patternPaperIndex.matcher(line).find()) {
                paperId = Integer.parseInt(line.substring(6));
            } else if (patternYear.matcher(line).find()) {
                publishedYear = Integer.parseInt(line.substring(2));
            } else if (patternCitedPaper.matcher(line).find()) {
                citationPapers.add(Integer.parseInt(line.substring(2)));
            } else if (patternConference.matcher(line).find()) {
                conferenceName = line.substring(2).toLowerCase();
            } else if (patternAuthors.matcher(line).find()) {
                authors = line.substring(2).split(",");
            } else if (line.trim().isEmpty()) {
                titleWriter.write(paperId + "\t" + publishedYear + "\t" + paperTitle + "\n");
                paperConferenceWriter.write(paperId + "\t" + conferenceName + "\n");

                if (!citationPapers.isEmpty()){
                    for (int citePaper : citationPapers) {
                        paperCitationWriter.write(paperId + "\t" + citePaper + "\n");
                    }
                }

                if (authors != null) {
                    for (String authorName : authors) {
                        Set<String> conferences = authorBelongs2Conferences.getOrDefault(authorName, new HashSet<>());
                        conferences.add(conferenceName);
                        authorBelongs2Conferences.put(authorName, conferences);
                    }
                }

                paperId2Authors.put(paperId, authors);

                paperTitle = "";
                conferenceName = "";
                paperId = 0;
                publishedYear = 0;
                citationPapers.clear();
                authors = null;
            }
        }
        titleWriter.close();
        paperCitationWriter.close();

        System.out.println("Create authpr citation network");
        for (Map.Entry<String, Set<String>> e : authorBelongs2Conferences.entrySet()) {
            String author = e.getKey();
            authorConferenceWriter.write(author);
            for (String conference : e.getValue()) {
                authorConferenceWriter.write("\t" + conference);
            }
            authorConferenceWriter.write("\n");
        }

        authorConferenceWriter.close();

        // create author-citation network
        FileWriter authorCitationWriter = new FileWriter("author_citation_edge.tsv");
        Map<String, HashMap<String, Integer>> adjList = new HashMap<>();

        br = new BufferedReader(new FileReader("./paper_citation_edge.tsv"));
        while ((line = br.readLine()) != null) {
            String[] edge = line.split("\t");
            int fromPaper = Integer.parseInt(edge[0]);
            int toPaper = Integer.parseInt(edge[1]);

            for (String fromAuthor : paperId2Authors.getOrDefault(fromPaper, new String[0])) {
                HashMap<String, Integer> authorCounter = adjList.getOrDefault(fromAuthor, new HashMap<>());
                for (String toAuthor : paperId2Authors.get(toPaper)) {
                    int w = authorCounter.getOrDefault(toAuthor, 0) + 1;
                    authorCounter.put(toAuthor, w);
                }
                adjList.put(fromAuthor, authorCounter);
            }
        }

        for (Map.Entry<String, HashMap<String, Integer>> e : adjList.entrySet()) {
            String fromAuthor = e.getKey();
            for (Map.Entry<String, Integer> counter : e.getValue().entrySet()) {
                String toAuthor = counter.getKey();
                int w = counter.getValue();
                authorCitationWriter.write(fromAuthor + "\t" + toAuthor + "\t" + w + "\n");
            }
        }
        authorCitationWriter.close();
    }
}
