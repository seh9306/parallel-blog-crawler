package team.dev.blog.producer;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Slf4j
public class DataProducer {
    private final WebClient webClient;
    private final ConcurrentLinkedQueue<String> queue;
    private final ConcurrentLinkedQueue<String> idQueue;
    private final Properties properties;

    private String sessionId;

    public DataProducer(ConcurrentLinkedQueue<String> queue, ConcurrentLinkedQueue<String> idQueue) {
        this.queue = queue;
        this.idQueue = idQueue;
        this.webClient = WebClient.builder()
                .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .defaultHeader("Accept-Encoding", "gzip, deflate")
                .defaultHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .defaultHeader("Cache-Control", "max-age=0")
                .defaultHeader("Connection", "keep-alive")
                .defaultHeader("Host", "ndev.co.kr")
                .defaultHeader("Upgrade-Insecure-Requests", "1")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .baseUrl("http://ndev.co.kr")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().compress(true)))
                .build();
        this.getSession();
        this.login();
    }

    public void produce() {
        while (idQueue.size() != 0) {
            Stream.generate(idQueue::poll)
                    .limit(3)
                    .filter(Objects::nonNull)
                    .forEach(id -> {
                        String body = webClient.post()
                                .uri("/r/rproc.php")
                                .body(BodyInserters.fromFormData("userid", id))
                                .header("Referer", "http://ndev.co.kr/r/")
                                .header("Cookie", "PHPSESSID=" + sessionId + "; userid=" + properties.get("id"))
                                .retrieve()
                                .bodyToMono(String.class)
                                .block();
                        if (body == null || body.isEmpty()) {
                            queue.add(id);
                            return;
                        }
                        Document document = Jsoup.parse(body);
                        Elements yesterdayVisitors = document.select("div:nth-child(1) > div:nth-child(8) > div > div.panel-body");
                        Elements blogCreationDate = document.select("div:nth-child(1) > div:nth-child(2) > div > div.panel-body");
                        Elements indices = document.select("div:nth-child(3) > div:nth-child(4) > div > div.panel-body > b > font");
                        ;
                        if (indices != null && indices.text().trim().isEmpty()) {
                            indices = document.select("div:nth-child(3) > div:nth-child(4) > div > div.panel-body > b");
                        }

                        queue.add(id
                                + "," + yesterdayVisitors.text().replaceAll(",", "")
                                + "," + blogCreationDate.text()
                                + "," + indices.text()
                                + ",https://blog.naver.com/" + id
                                + "," + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    });
        }
    }

    private void login() {
        var responseEntity = this.webClient.post()
                .uri("/login_proc.php")
                .header("Referer", "http://ndev.co.kr/login.php")
                .header("Origin", "http://ndev.co.kr")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "PHPSESSID=" + sessionId + "; userid=" + properties.get("id"))
                .body(
                        BodyInserters.fromFormData("userid", properties.get("id").toString())
                                .with("password", properties.get("pw").toString()))
                .retrieve()
                .toBodilessEntity()
                .block();
        if (responseEntity == null || !responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            log.info("로그인 실패로 워커 종료");
        }
    }

    private void getSession() {
        var responseEntity = this.webClient.get()
                .uri("/notice.php")
                .retrieve()
                .toBodilessEntity()
                .block();
        if (responseEntity == null) {
            log.error("세션 획득 실패로 워커 종료");
            return;
        }
        String setCookie = Objects.requireNonNull(responseEntity.getHeaders().get("Set-Cookie")).toString();
        sessionId = setCookie.split(";")[0].split("=")[1];
    }
}
