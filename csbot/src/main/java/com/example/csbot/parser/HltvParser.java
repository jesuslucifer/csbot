package com.example.csbot.parser;

import com.example.csbot.model.Match;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class HltvParser {

    private static final String HLTV_BASE_URL = "https://www.hltv.org/";
    private static final String HLTV_MATCHES_URL = HLTV_BASE_URL + "/matches";

    public List<Match> getMatches() throws IOException {
        List<Match> upcomingMatches = new ArrayList<>();

        Document document = Jsoup.connect(HLTV_MATCHES_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .referrer("https://www.google.com")
                .timeout(10000)
                .followRedirects(true)
                .get();

        Elements liveBlocks = document.getElementsByClass(".match");

        for (Element liveBlock : liveBlocks) {
            Match match = new Match();

            match.setEvent(Objects.requireNonNull(
                    liveBlock.selectFirst("text-ellipsis"))
                    .text());

            Elements teams = liveBlocks.select(".match-team");

            match.setTeam1(teams
                    .get(0)
                    .selectFirst("match-teamname text-ellipsis")
                    .text());

            match.setTeam2(teams
                    .get(1)
                    .selectFirst("match-teamname text-ellipsis")
                    .text());

            match.setMeta(liveBlock.selectFirst("match-meta")
                    .text());

            match.setStatus("live");

        }

        return upcomingMatches;
    }

    public List<Match> getAllMatches() {
        List<Match> upcomingMatches = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        //options.addArguments("--headless=new");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-blink-features");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);

        try {
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            driver.get(HLTV_MATCHES_URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Проверяем, что загрузилась правильная страница
            if (driver.getTitle().contains("HLTV") || driver.getCurrentUrl().contains("hltv.org")) {
                System.out.println("Страница загружена: " + driver.getTitle());
            } else {
                System.out.println("Возможно CAPTCHA или редирект: " + driver.getCurrentUrl());
            }

            List<WebElement> matchBlocks = driver.findElements(By.cssSelector(".match"));

            for(WebElement block : matchBlocks) {
                Match match = new Match();

                match.setEvent(block.findElement((By.cssSelector("text-ellipsis"))).getText());

                List<WebElement> teams = block.findElements(By.cssSelector(".match-team"));

                if (teams.size() >= 2) {
                    match.setTeam1(teams.get(0).findElement(By.cssSelector("match-teamname text-ellipsis")).getText());
                    match.setTeam1(teams.get(1).findElement(By.cssSelector("match-teamname text-ellipsis")).getText());
                }

                match.setMeta(block.findElement((By.cssSelector("match-meta"))).getText());

                match.setStatus("live");

                upcomingMatches.add(match);
            }

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }


        return upcomingMatches;
    }

    public List<Match> getAMatches() {
        List<Match> upcomingMatches = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // можно убрать для debug
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(HLTV_MATCHES_URL);

            Thread.sleep(2500); // HLTV грузится не мгновенно

            // каждый матч — это блок .upcomingMatch
            List<WebElement> matchBlocks = driver.findElements(By.cssSelector(".upcomingMatch"));

            for (WebElement block : matchBlocks) {
                Match match = new Match();

                // event name
                WebElement eventElem = block.findElement(By.cssSelector(".matchEventName"));
                match.setEvent(eventElem.getText());

                // meta (время матча)
                WebElement metaElem = block.findElement(By.cssSelector(".matchTime"));
                match.setMeta(metaElem.getText());

                // команды
                List<WebElement> teams = block.findElements(By.cssSelector(".matchTeam .team"));
                if (teams.size() >= 2) {
                    match.setTeam1(teams.get(0).getText());
                    match.setTeam2(teams.get(1).getText());
                }

                // статус матча (здесь "upcoming")
                match.setStatus("upcoming");

                upcomingMatches.add(match);
            }

            return upcomingMatches;

        } catch (Exception e) {
            log.error("Error parsing HLTV matches", e);
            return upcomingMatches;

        } finally {
            driver.quit();
        }
    }
}
