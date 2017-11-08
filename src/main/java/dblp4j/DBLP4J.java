package dblp4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class DBLP4J {
    private DBLP4J() {}
    private static final Pattern patternPaperIndex = Pattern.compile("^#index");
    private static final Pattern patternCitedPaper = Pattern.compile("^#%");
    private static final Pattern patternConference = Pattern.compile("^#c");
    private static final Pattern patternAuthors = Pattern.compile("^#@");

    public static void main(String[] args) throws IOException {
        final String FILENAME = "src/main/resources/dblp.txt";

        BufferedReader br = new BufferedReader(new FileReader(FILENAME));
        String line;

        // temporal variable for each paper information
        String conferenceName = "";
        String paperIndex = "";
        int paperId = -1;
        List<Integer> citingPaperIds = new ArrayList<>();
        String[] authors = null;

        Map<String, Integer> paperIndex2PaperId = new HashMap<>();
        Map<String, Set<String>> authorBelongs2Conferences = new HashMap<>();
        Map<Integer, String[]> paperId2Authors = new HashMap<>();

        FileWriter titleWriter = new FileWriter("papers.tsv");
        FileWriter paperCitationWriter = new FileWriter("paper_citation_edge.tsv");
        FileWriter authorConferenceWriter = new FileWriter("author_conference_label.tsv");

        System.out.println("Create paper citation network");
        while ((line = br.readLine()) != null) {
            if (patternPaperIndex.matcher(line).find()) {
                paperIndex = line.substring(6);
                paperId = paperIndex2PaperId.getOrDefault(paperIndex, paperIndex2PaperId.size());
                if (paperId == paperIndex2PaperId.size()){
                    paperIndex2PaperId.put(paperIndex, paperId);
                }
            } else if (patternCitedPaper.matcher(line).find()) {
                String citingPaperIndex = line.substring(2);

                int citingPaperId = paperIndex2PaperId.getOrDefault(citingPaperIndex, paperIndex2PaperId.size());
                if (citingPaperId == paperIndex2PaperId.size()){
                    paperIndex2PaperId.put(citingPaperIndex, citingPaperId);
                }

                citingPaperIds.add(citingPaperId);
            } else if (patternConference.matcher(line).find()) {
                conferenceName = line.substring(2).toLowerCase().trim();
            } else if (patternAuthors.matcher(line).find()) {
                authors = line.substring(2).split(",");
                String[] trimmedAuthors = new String[authors.length];
                for (int i = 0; i < authors.length; i++){
                    trimmedAuthors[i] = authors[i].trim();
                }
                authors = trimmedAuthors;
            } else if (line.trim().isEmpty()) {
                if (paperId != -1){
                    titleWriter.write(paperIndex2PaperId.get(paperIndex) + "\t" + paperIndex + "\t" + conferenceName + "\n");

                    for (int citingPaperId : citingPaperIds) {
                        paperCitationWriter.write(paperId + "\t" + citingPaperId + "\n");
                    }
                    if (authors != null){
                        paperId2Authors.put(paperId, authors);
                    }
                }

                if (authors != null && !conferenceName.equals("")) {
                    for (String authorName : authors) {
                        Set<String> conferences = authorBelongs2Conferences.getOrDefault(authorName, new HashSet<>());
                        conferences.add(conferenceName);
                        authorBelongs2Conferences.put(authorName, conferences);
                    }
                }

                conferenceName = "";
                paperIndex = "";
                paperId = -1;
                citingPaperIds.clear();
                authors = null;
            }
        }
        titleWriter.close();
        paperCitationWriter.close();

        System.out.println("Create author labels");
        for (Map.Entry<String, Set<String>> e : authorBelongs2Conferences.entrySet()) {
            String author = e.getKey();

            authorConferenceWriter.write(author);
            for (String conference : e.getValue()) {
                authorConferenceWriter.write("\t" + conference);
            }
            authorConferenceWriter.write("\n");
        }

        authorConferenceWriter.close();
        paperIndex2PaperId.clear();
        authorBelongs2Conferences.clear();

        System.out.println("Create author citation network");
        // create author-citation network
        FileWriter authorCitationWriter = new FileWriter("author_citation_edge.tsv");
        Map<String, HashMap<String, Integer>> adjList = new HashMap<>();

        br = new BufferedReader(new FileReader("./paper_citation_edge.tsv"));
        while ((line = br.readLine()) != null) {
            String[] edge = line.split("\t");
            int fromPaper = Integer.parseInt(edge[0]);
            int toPaper = Integer.parseInt(edge[1]);

            // skip pair of paper which has no author information
            if (!paperId2Authors.containsKey(fromPaper)){
                continue;
            }
            if (!paperId2Authors.containsKey(toPaper)){
                continue;
            }

            for (String fromAuthor : paperId2Authors.get(fromPaper)) {
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
                int numCiting = counter.getValue();
                authorCitationWriter.write(fromAuthor + "\t" + toAuthor + "\t" + numCiting + "\n");
            }
        }
        authorCitationWriter.close();
    }
}
