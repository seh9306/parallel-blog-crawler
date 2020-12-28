package team.dev.blog.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DataConsumer implements ApplicationRunner {
    private final ConcurrentLinkedQueue<String> queue;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (true) {
            if (queue.size() > 0) {
                BufferedWriter bufferedWriter;
                try {
                    if (Files.exists(Path.of("output.csv"))) {
                        bufferedWriter = new BufferedWriter(new FileWriter("output.csv", Charset.forName("EUC-KR"), true));
                    } else {
                        bufferedWriter = new BufferedWriter(new FileWriter("output.csv", Charset.forName("EUC-KR")));
                        bufferedWriter.write("아이디,전일 방문자,블로그 생성일,지수,링크,수집날짜");
                        bufferedWriter.newLine();
                    }
                } catch (FileNotFoundException e) {
                    continue;
                }
                BufferedWriter finalBufferedWriter = bufferedWriter;
                Stream.generate(queue::poll)
                        .limit(100)
                        .filter(Objects::nonNull)
                        .forEach(data -> {
                            try {
                                finalBufferedWriter.write(data);
                                finalBufferedWriter.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                bufferedWriter.close();
            }
            Thread.sleep(1000);
        }
    }
}
