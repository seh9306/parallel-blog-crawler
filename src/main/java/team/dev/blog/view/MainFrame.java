package team.dev.blog.view;

import org.springframework.stereotype.Component;
import team.dev.blog.view.panel.MainPanel;

import javax.swing.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MainFrame extends JFrame {
    public MainFrame(ConcurrentLinkedQueue<String> queue) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(200,200);
        setContentPane(new MainPanel(queue));
        setVisible(true);
    }
}
