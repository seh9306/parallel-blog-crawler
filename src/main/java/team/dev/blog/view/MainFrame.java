package team.dev.blog.view;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import team.dev.blog.view.panel.MainPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MainFrame extends JFrame {
    public static MainFrame thisFrame = null;

    public MainFrame(ConcurrentLinkedQueue<String> queue) throws IOException {
        super("NDEV 크롤러");
        thisFrame = this;
        setResizable(false);
        setSize(250, 150);

        ClassPathResource classPathResource = new ClassPathResource("ico/search_cat.png");
        setIconImage(ImageIO.read(classPathResource.getURL()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(200,200);
        setContentPane(new MainPanel(queue));
        setVisible(true);
    }
}
