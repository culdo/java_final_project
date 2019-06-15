package line;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LINEAuto {

    private static WebDriver browser;
    private WebDriverWait wait;
    private String data_local_id;
    private WebElement input_area;
    private WebElement search_input;

    public LINEAuto() {
        String ext_path = System.getenv("LOCALAPPDATA") + "/Google/Chrome/User Data"+"/Default/Extensions/ophjlpahpchlmihnnnihgmmeilfjmjjc/2.2.2_0";

        ChromeOptions chrome_option = new ChromeOptions();
        chrome_option.addArguments("--load-extension=" + ext_path);
        browser = new ChromeDriver(chrome_option);
        wait = new WebDriverWait(browser, 99);
        browser.get("chrome-extension://ophjlpahpchlmihnnnihgmmeilfjmjjc/index.html");
    }

    public void login(String username, String pwd) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#line_login_email")));

        element.sendKeys(username + Keys.TAB + pwd + Keys.ENTER);
//        element = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("mdCMN01Code")));

        search_input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("_search_input")));
    }

    public void waitLogin() {
        search_input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("_search_input")));
    }

    public String[] readMembers(String room) {
        checkRoom(room);

        WebElement room_info = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("mdRGT04Txt")));
        room_info.click();
        List<WebElement> member_array = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.className("mdRGT13Ttl")));
        // size-1 to exclude user self
        String[] member_names = new String[member_array.size()-1];
        for (int i=1;i<member_names.length;i++) {
            member_names[i]= member_array.get(i).getText();
        }
        return member_names;
    }

    public String[] readRooms() {
        List<WebElement> room_array = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("#_chat_list_body>li"),3));
        String[] room_names = new String[room_array.size()];
        for (int i=0;i<room_names.length;i++) {
            room_names[i]= room_array.get(i).getAttribute("title");
            System.out.println(room_names[i]);
        }
        return room_names;
    }

    public void chooseRoom(String room) {

        search_input.sendKeys(room);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='_chat_list_body']/li[@title='"+room+"']")));
        element.click();
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        }catch (InterruptedException e) {
            String why;
            Throwable cause = e.getCause();
            if (cause != null) {
                why = cause.getMessage();
            } else {
                why = e.getMessage();
            }
            System.err.println("InterruptedException: " + why);
        }
        element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#_search > div > .MdBtn01Delete01")));
        element.click();
        element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.MdRGT07Cont:last-child")));
        data_local_id = element.getAttribute("data-local-id");
        input_area = browser.findElement(By.id("_chat_room_input"));
    }

    /*
    setwho param: Own, Other
     */
    public String[] checkNewMsg(String room, String setwho) {
        WebElement latest_msg;
        List<WebElement> msg_array;
        String data_local_id_now;
        String new_text = null;

        checkRoom(room);

        if(setwho == null) {
            msg_array = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.className("MdRGT07Cont")));
        } else {
            msg_array = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector(".MdRGT07Cont.mdRGT07" + setwho)));
        }
        latest_msg = msg_array.get(msg_array.size()-1);
        data_local_id_now = latest_msg.getAttribute("data-local-id");

        String sender = "自己";
        if(setwho!=null) {
            sender = latest_msg.findElement(By.className("mdRGT07Ttl")).getText();
        }

        if(!data_local_id.equals(data_local_id_now)) {
            data_local_id = data_local_id_now;
            boolean loop = true;
            while(loop) {
                try {
                    new_text = latest_msg.findElement(By.className("mdRGT07MsgTextInner")).getText();
                    loop = false;
                }catch (StaleElementReferenceException e) {
                    loop = true;
                }
            }
        }

        return new String[]{sender, new_text};
    }

    public String readMsg() {
        List<WebElement> msg_array = browser.findElements(By.cssSelector("div.mdRGT07Own .mdRGT07MsgTextInner"));
        return msg_array.get(msg_array.size()-1).getText();
    }

    public void checkRoom(String room) {
        boolean isInRoom = false;
        if(browser.findElements(By.className("mdRGT04Ttl")).size()>0) {
            isInRoom = browser.findElement(By.className("mdRGT04Ttl")).getText().equals(room);
        }
        if(!isInRoom) {
            chooseRoom(room);
            System.out.println("NotInRoom");
        }
    }

    public void sendMsg(String room, String msg) {
        checkRoom(room);
        String jquery = "$('#_chat_room_input').text(arguments[0])";
        ((JavascriptExecutor)browser).executeScript(jquery, msg);

        boolean loop = true;
        while (loop) {
            try {
                input_area.sendKeys(Keys.ENTER);
                loop = false;
            } catch (WebDriverException e) {
                input_area = browser.findElement(By.id("_chat_room_input"));
                input_area.click();
            }
        }
    }

    public static void main(String[] args) {
        LINEAuto test_Line = new LINEAuto();
        test_Line.login("george0228489372@yahoo.com.tw", "wuorsut");
//        test_Line.sendMsg("Alo Smo", "Test successfully!!!");
        while(true) {
            String[] result = test_Line.checkNewMsg("java期末測試群","Other");
            String msg = String.format("%s說了%s\n", result[0], result[1]);
            if(result[1]!=null) {
                System.out.println(msg);
                test_Line.sendMsg("java期末測試群", msg);
            }
        }

        //Close the browser
//        browser.quit();
    }
}