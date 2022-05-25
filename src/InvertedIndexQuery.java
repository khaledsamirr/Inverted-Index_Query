import java.io.*;
import java.util.*;

//=====================================================================
class DictEntry {

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
    public HashSet<Integer> postingList;

    DictEntry() {
        postingList = new HashSet<Integer>();
    }
}

class Index {

    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry> index; // THe inverted index

    Index() {
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, DictEntry>();
    }

    public void printDictionary() {
        Iterator it = index.entrySet().iterator();
        System.out.println("------------------------------------------------------");
        System.out.println("*****    Number of terms = " + index.size());
        System.out.println("------------------------------------------------------");
    }

    public void buildIndex(String[] files) {
        int i = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName);
                String ln;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        // check to see if the word is not in the dictionary
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry());
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i)) {
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                            index.get(word).postingList.add(i); // add the posting to the posting:ist
                        }
                        //set the term_fteq in the collection
                        index.get(word).term_freq += 1;
                    }
                }

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }
        printDictionary();
    }

    HashSet<Integer> intersect(HashSet<Integer> pL1, HashSet<Integer> pL2) {
        HashSet<Integer> answer = new HashSet<Integer>();
        Iterator<Integer> itP1 = pL1.iterator();
        Iterator<Integer> itP2 = pL2.iterator();
        int docId1 = 0, docId2 = 0;
        if (itP1.hasNext())
            docId1 = itP1.next();
        if (itP2.hasNext())
            docId2 = itP2.next();

        while (itP1.hasNext() && itP2.hasNext()) {

            if (docId1 == docId2) {
                answer.add(docId1);
                docId1 = itP1.next();
                docId2 = itP2.next();
            }
            else if (docId1 < docId2) {
                if (itP1.hasNext())
                    docId1 = itP1.next();
                else return answer;

            } else {
                if (itP2.hasNext())
                    docId2 = itP2.next();
                else return answer;

            }

        }
        if (docId1 == docId2) {
            answer.add(docId1);
        }
        return answer;
    }
    HashSet<Integer> OR(HashSet<Integer> p1, HashSet<Integer> p2) {
        HashSet<Integer> answer = new HashSet<Integer>();

        for (int id : p1) {
            answer.add(id);
        }
        for (int id : p2) {
            answer.add(id);
        }
        return answer;
    }

    HashSet<Integer> NOT(HashSet<Integer> p1) {
        HashSet<Integer> answer = new HashSet<Integer>();

        for (int i = 0; i < sources.size(); i++) {
            if (!p1.contains(i)) {
                answer.add(i);
            }
        }
        return answer;
    }

    //-----------------------------------------------------------------------
    public String Query(String phrase) { // any mumber of terms optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;
        //int [] freq = new int[len];
        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()).postingList);
        if(words[0].toLowerCase().equals("not")){
            res=new HashSet<Integer>(index.get(words[1].toLowerCase()).postingList);
            res = NOT(res);
        }
        int i = 1;
        while (i < len) {
            if (words[i].toLowerCase().equals("and")) {
                if (words[i + 1].toLowerCase().equals("not")) {
                    res = intersect(res, NOT(index.get(words[i + 2].toLowerCase()).postingList));
                    i++;
                } else {
                    res = intersect(res, index.get(words[i + 1].toLowerCase()).postingList);
                }
            } else if (words[i].toLowerCase().equals("or")) {
                if (words[i + 1].toLowerCase().equals("not")) {
                    res = OR(res, NOT(index.get(words[i + 2].toLowerCase()).postingList));
                    i++;
                } else {
                    res = OR(res, index.get(words[i + 1].toLowerCase()).postingList);
                }
            }
            i++;
        }
        for (int num : res) {
            result += "\n" + sources.get(num);
        }
        if(result==""){
            result="No matched results";
        }
        return result;
    }
}
//=====================================================================
public class InvertedIndexQuery {

    public static void main(String args[]) throws IOException {
        Index index = new Index();
        index.buildIndex(new String[]{
                "C:\\Users\\Khaled Samir\\OneDrive\\Desktop\\docs1\\100.txt",
                "C:\\Users\\Khaled Samir\\OneDrive\\Desktop\\docs1\\101.txt",
                "C:\\Users\\Khaled Samir\\OneDrive\\Desktop\\docs1\\102.txt"
        });
        String Query1="different and sherif";
        String Query2="agile or sherif";
        String Query3="different and not sherif";

        System.out.println("Query: "+Query1);
        System.out.println("====================== result ======================== " + index.Query(Query1) +"\n");


        System.out.println("Query: "+Query2);
        System.out.println("====================== result ======================== " + index.Query(Query2) +"\n");


        System.out.println("Query: "+Query3);
        System.out.println("====================== result ======================== " + index.Query(Query3)+"\n");
    }



}

