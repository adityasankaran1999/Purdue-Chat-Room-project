import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatFilter {
    ArrayList<String> list  = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {
        try {
            File badWords = new File("words_to_filter.txt");
            Scanner s = new Scanner(badWords);
            while (s.hasNext()) {
                list.add(s.next());
            }
            s.close();

        }catch(IOException e){}
    }

    public String filter(String msg) {
        for(int i=0; i< list.size(); i++) {
        String badWord = msg.toUpperCase();
            if (badWord.contains(list.get(i))) {
                String newWord = list.get(i).replaceAll(".", "*");
                //String remainingWord = msg.replaceAll(list.get(i), "");
                msg = msg.replaceAll("(?i)"+list.get(i), newWord);
                //msg += remainingWord;
            }
        }
        return msg;
    }
}
