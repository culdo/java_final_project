import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class test {

    public static void main(String[] args){
//        String[] placholder = {"誰", "時間"};
//        String[] replaceStrings = {"cat", "dog"};
//
//        String line = "我是(誰)加(時間)";
//        String pattern = String.format("\\((%s)\\)", String.join("|", placholder));
//
//        Pattern p = Pattern.compile(pattern);
//
//        Matcher m = p.matcher(line);
//        StringBuffer sb = new StringBuffer();
//
//        boolean found = false;
//        while (m.find()) {
//            System.out.printf("I found the text \"%s\" starting at index %d " +
//                            "and ending at index %d.%n",
//                    m.group(1),
//                    m.start(),
//                    m.end());
//            found = true;
//            String newString="";
//            for (int i = 0; i < placholder.length; i++) {
//                if (m.group(1).equals(placholder[i])) {
//                    newString = replaceStrings[i];
//                }
//            }
//            m.appendReplacement(sb, newString);
//        }
//        if(!found){
//            System.out.printf("No match found.%n");
//        }else{
//            m.appendTail(sb);
//            System.out.println(sb.toString());
//        }
    }
}