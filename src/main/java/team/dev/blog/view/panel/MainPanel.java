package team.dev.blog.view.panel;

import lombok.extern.slf4j.Slf4j;
import team.dev.blog.producer.DataProducer;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

@Slf4j
public class MainPanel extends JPanel {
    private final ConcurrentLinkedQueue<String> idQueue = new ConcurrentLinkedQueue<>();

    public MainPanel(ConcurrentLinkedQueue<String> queue, Properties properties) {
        int threadCount = Integer.parseInt(properties.getOrDefault("workers", "8").toString());
        setLayout(null);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(15, 20, 220, 30);
        progressBar.setString("0 / 0");
        progressBar.setMaximum(0);
        progressBar.setStringPainted(true);
        add(progressBar);

        JButton startButton = new JButton("크롤링");
        startButton.setBounds(15, 70, 220, 30);

        startButton.addActionListener(e -> new Thread(() -> {
            startButton.setEnabled(false);
            progressBar.setValue(0);
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader("account.txt"));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    idQueue.add(line);
                }
                progressBar.setMaximum(idQueue.size());
                progressBar.setString("0 / " + idQueue.size());
            } catch (IOException fileNotFoundException) {
                JOptionPane.showMessageDialog(this, "account.txt 파일을 찾을 수 없습니다.");
                startButton.setEnabled(true);
                return;
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> new DataProducer(queue, idQueue, properties).produce()).start();
            }

            new Thread(() -> {
                while (true) {
                    try {
                        progressBar.setValue(progressBar.getMaximum() - idQueue.size());
                        progressBar.setString(progressBar.getValue() + " / " + progressBar.getMaximum());
                        this.repaint();
                        if (progressBar.getValue() == progressBar.getMaximum()) {
                            startButton.setEnabled(true);
                            return;
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }).start();
        }).start());
        add(startButton);
    }
}
