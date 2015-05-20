package ru.ifmo.ctddev.zban.copy;

import ru.ifmo.ctddev.zban.test.CopyUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Created by izban on 19.05.15.
 */
public class UIFileCopy {
    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 150;
    private static final int MAX_WIDTH = 600;
    private static final int MAX_HEIGHT = 600;
    static private final int PROGRESSBAR_SIZE = 1_000_000_000;
    private static final String TITLE = "Copying...";
    private static final String LABEL_COMPLETED_FORMAT = "Completed %d%% of copying";
    private static final String LABEL_ELAPSED_FORMAT = "Elapsed %d seconds";
    private static final String LABEL_AVERAGESPEED_FORMAT = "Average speed is %.3f mb/s";
    private static final String LABEL_CURRENTSPEED_FORMAT = "Current speed is %.3f mb/s";
    private static final String LABEL_ESTIMATE_FORMAT = "Estimate time is %d seconds";


    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: UIFileCopy from to");
            return;
        }
        long startTime = System.currentTimeMillis();
        final long[] lastTime = {startTime};
        final long[] downloaded = {0};

        String from = args[0];
        String to = args[1];


        JFrame frame = new JFrame();
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        JLabel label3 = new JLabel();
        JLabel label4 = new JLabel();
        JLabel label5 = new JLabel();
        JProgressBar progressBar = new JProgressBar();
        JButton button = new JButton("Cancel");
        JPanel panel = new JPanel();

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        frame.setMaximumSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
        frame.setTitle(TITLE);

        label1.setText(String.format(LABEL_COMPLETED_FORMAT, 0));

        label2.setText(String.format(LABEL_ELAPSED_FORMAT, 0));

        label3.setText(String.format(LABEL_AVERAGESPEED_FORMAT, 0.0));

        label4.setText(String.format(LABEL_CURRENTSPEED_FORMAT, 0.0));

        label5.setText(String.format(LABEL_ESTIMATE_FORMAT, 0));

        progressBar.setMaximum(1);
        progressBar.setValue(0);

        Copyrator copyrator = new Copyrator((done, all) -> {
            progressBar.setValue((int) (done * 1.0 / all * PROGRESSBAR_SIZE));
            progressBar.setMaximum(PROGRESSBAR_SIZE);
            label1.setText(String.format(LABEL_COMPLETED_FORMAT, 100L * done / all));
            long curTime = System.currentTimeMillis();
            label2.setText(String.format(LABEL_ELAPSED_FORMAT, (curTime - startTime) / 1000));
            if ((curTime - lastTime[0]) > 100) {
                double averageSpeed = done / 1000.0 / (curTime - startTime);
                double currentSpeed = (done - downloaded[0]) / 1000.0 / (curTime - lastTime[0]);
                label3.setText(String.format(LABEL_AVERAGESPEED_FORMAT, averageSpeed));
                label4.setText(String.format(LABEL_CURRENTSPEED_FORMAT, currentSpeed));
                label5.setText(String.format(LABEL_ESTIMATE_FORMAT, (int)((all - done) / 1e6 / averageSpeed)));
                lastTime[0] = curTime;
                downloaded[0] = done;
                panel.updateUI();
            }
            return null;
        });

        button.addActionListener(e -> {
            frame.dispose();
            copyrator.running = false;
        });

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label1);
        panel.add(label2);
        panel.add(label3);
        panel.add(label4);
        panel.add(label5);
        panel.add(progressBar);
        panel.add(button);
        panel.setMaximumSize(new Dimension(300, 300));

        Box box = new Box(BoxLayout.Y_AXIS);
        box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        box.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        box.add(Box.createVerticalGlue());
        box.add(panel);
        box.add(Box.createVerticalGlue());
        frame.add(box);

        panel.setAlignmentX(0.5f);
        panel.setAlignmentY(0.5f);

        frame.pack();
        frame.setVisible(true);

        copyrator.copy(from, to);
        frame.dispose();
    }
}
