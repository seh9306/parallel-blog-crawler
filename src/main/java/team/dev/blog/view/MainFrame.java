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

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        setContentPane(new MainPanel(queue, properties()));

        setVisible(true);
    }

    public Properties properties() throws IOException {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(System.getProperty("user.dir") + "\\config.txt"));
            if (!properties.containsKey("id")) {
                JOptionPane.showMessageDialog(this, "id 값이 설정되어 있지 않습니다.");
                System.exit(0);
            }
            if (!properties.containsKey("pw")) {
                JOptionPane.showMessageDialog(this, "pw 값이 설정되어 있지 않습니다.");
                System.exit(0);
            }
            return properties;
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "config.txt 파일을 읽을 수 없습니다.");
            System.exit(0);
            return new Properties();
        }
    }
}
