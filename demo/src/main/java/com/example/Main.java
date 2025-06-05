package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.*;
import java.net.URL;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // dùng headless mới
        WebDriver driver = new ChromeDriver(options);

        try {
            String name = "VGE-D-BT01";
            driver.get("https://en.cf-vanguard.com/cardlist/cardsearch/?expansion=167");
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // Lấy số kết quả "281 Results"
            WebElement resultElement = driver.findElement(By.cssSelector(".number"));
            String resultText = resultElement.getText(); // ví dụ: "281 Results"
            int expectedCount = Integer.parseInt(resultText.replaceAll("[^0-9]", ""));
            System.out.println("Expecting " + expectedCount + " cards...");

            // Scroll cho đến khi đủ ảnh
            int lastCount = 0;
            while (true) {
                List<WebElement> cards = driver.findElements(By.cssSelector("#cardlist-container img"));
                int currentCount = cards.size();

                System.out.println("Currently loaded: " + currentCount);
                System.out.println("lastCount loaded: " + lastCount);

                if (currentCount >= expectedCount) break;

                if (currentCount == lastCount) {
                    // Nếu không tăng, có thể cần đợi thêm
                    // Thread.sleep(2000);
                    
                    
                }

                lastCount = currentCount;

                if (!cards.isEmpty()) {
                    js.executeScript("arguments[0].scrollIntoView(true);", cards.get(cards.size() - 1));
                }

                Thread.sleep(2000);
                
            }

            // Đợi thêm để chắc chắn ảnh cuối cùng đã load xong
            Thread.sleep(2000);

            // Lấy tất cả ảnh
            List<WebElement> cards = driver.findElements(By.cssSelector("#cardlist-container img"));
            System.out.println("Found " + cards.size() + " cards.");

            File folder = new File(name);
            if (!folder.exists()) folder.mkdir();

            int index = 1;
            for (WebElement img : cards) {
                try {
                    String imgUrl = img.getAttribute("src");
                    String title = img.getAttribute("alt");

                    if (imgUrl == null || imgUrl.isEmpty()) continue;
                    if (!imgUrl.startsWith("http")) {
                        imgUrl = "https://en.cf-vanguard.com" + imgUrl;
                    }

                    String fileName = "card_" + index + "_" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
                    saveImage(imgUrl, name + "/" + fileName);
                    System.out.println("Saved " + title);
                    index++;
                } catch (Exception e) {
                    System.err.println("Error with image: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream();
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }
}
