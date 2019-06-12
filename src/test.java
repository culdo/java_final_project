import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class test {

    public static void main(String[] args){
        String line = "我是(誰)加(時間)";
        String pattern = "\\((誰|時間)\\)";

        Pattern p = Pattern.compile(pattern);

        Matcher m = p.matcher(line);

        boolean found = false;
        while (m.find()) {
            System.out.printf("I found the text \"%s\" starting at index %d " +
                            "and ending at index %d.%n",
                    m.group(1),
                    m.start(),
                    m.end());
            found = true;
        }
        if(!found){
            System.out.printf("No match found.%n");
        }
    }
}