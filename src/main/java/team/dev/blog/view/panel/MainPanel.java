package team.dev.blog.view.panel;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.swing.*;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Slf4j
public class MainPanel extends JPanel {
    private final ConcurrentLinkedQueue<String> queue;
    private final ConcurrentLinkedQueue<String> idQueue = new ConcurrentLinkedQueue<>();

    public MainPanel(ConcurrentLinkedQueue<String> queue) {
        this.queue = queue;
        setLayout(null);
        JButton startButton = new JButton("크롤링");
        startButton.setBounds(50, 50, 100, 100);

        startButton.addActionListener(e -> {
            new Thread(() -> {
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(new FileReader("account.txt"));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        idQueue.add(line);
                    }
                } catch (IOException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                } finally {
                    try {
                        if (bufferedReader != null)
                            bufferedReader.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

                for (int i = 0; i < 16; i++) {
                    new Thread(() -> {
                        String phpsessid = null;

                        WebClient webClient = WebClient.builder()
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

                        // 세션을 받기 위한 요청
                        ResponseEntity<Void> responseEntity = webClient.get()
                                .uri("/notice.php")
                                .retrieve()
                                .toBodilessEntity()
                                .block();
                        if (responseEntity == null) {
                            log.error("세션 획득 실패로 워커 종료");
                            return;
                        }
                        String setCookie = Objects.requireNonNull(responseEntity.getHeaders().get("Set-Cookie")).toString();
                        phpsessid = setCookie.split(";")[0].split("=")[1];

                        // 로그인 요청
                        responseEntity = webClient.post()
                                .uri("/login_proc.php")
                                .header("Referer", "http://ndev.co.kr/login.php")
                                .header("Origin", "http://ndev.co.kr")
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .header("Cookie", "PHPSESSID=" + phpsessid + "; userid=damdi3")
                                .body(
                                        BodyInserters.fromFormData("userid", "damdi3")
                                                .with("password", "qhdekf22"))
                                .retrieve()
                                .toBodilessEntity()
                                .block();
                        if (responseEntity == null || !responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                            log.info("로그인 실패로 워커 종료");
                            return;
                        }

                        while (idQueue.size() != 0) {
                            String finalPhpsessid = phpsessid;
                            Stream.generate(idQueue::poll)
                                    .limit(2)
                                    .filter(Objects::nonNull)
                                    .forEach(id -> {
                                        String body = webClient.post()
                                                .uri("/r/rproc.php")
                                                .body(BodyInserters.fromFormData("userid", "doorihana79"))
                                                .header("Referer", "http://ndev.co.kr/r/")
                                                .header("Cookie", "PHPSESSID=" + finalPhpsessid + "; userid=damdi3")
                                                .retrieve()
                                                .bodyToMono(String.class)
                                                .block();
                                        if (body == null || body.isEmpty()) {
                                            queue.add(id);
                                            return;
                                        }
                                        log.info(id);
                                        Document document = Jsoup.parse(body);
                                        Elements blogCreationDate = document.select("div:nth-child(1) > div:nth-child(2) > div > div.panel-body");
                                        Elements indices = document.select("div:nth-child(3) > div:nth-child(4) > div > div.panel-body > b > font");
                                        queue.add(id + "," + blogCreationDate.text() + "," + indices.text());
                                    });
                        }
                        log.info("종료");
                    }).start();
                }
            }).start();
        });
        add(startButton);
    }
}
