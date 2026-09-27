import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

/** Draws two graphs from results.csv using Java's built-in graphics library. */
public class PlotResults {
    private static final String[] NAMES = {"MergeSort", "QuickSort", "DeterministicSelect", "ClosestPair"};
    private static final String[] TYPES = {"random", "sorted", "reverse", "duplicates"};
    private static final Color[] COLORS = {new Color(40, 95, 190), new Color(210, 100, 30),
        new Color(20, 135, 90), new Color(140, 65, 170)};

    public static void main(String[] args) throws Exception {
        List<String> rows = Files.readAllLines(Path.of("results/results.csv"));
        Files.createDirectories(Path.of("docs/plots"));
        draw(rows, true);
        draw(rows, false);
        System.out.println("Graphs saved in docs/plots.");
    }

    private static void draw(List<String> rows, boolean time) throws Exception {
        BufferedImage image = new BufferedImage(1120, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1120, 820);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString(time ? "Time vs. input size" : "Recursion depth vs. input size", 35, 38);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("5 warmup runs, 7 measured runs. Data: results/results.csv", 35, 65);
        for (int i = 0; i < NAMES.length; i++) {
            g.setColor(COLORS[i]);
            g.fillRect(35 + i * 270, 85, 22, 4);
            g.drawString(NAMES[i], 64 + i * 270, 92);
        }
        for (int panel = 0; panel < TYPES.length; panel++) {
            int left = 85 + (panel % 2) * 545;
            int top = 160 + (panel / 2) * 325;
            int width = 420, height = 225;
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString(TYPES[panel], left, top - 32);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString(time ? "Median time in ms (log scale)" : "Maximum active calls", left, top - 12);
            int steps = time ? 6 : 4;
            for (int tick = 0; tick <= steps; tick++) {
                int y = top + height - tick * height / steps;
                g.setColor(new Color(225, 225, 225));
                g.drawLine(left, y, left + width, y);
                g.setColor(Color.BLACK);
                String label = time ? new String[]{"0.0001", "0.001", "0.01", "0.1", "1", "10", "100"}[tick]
                                    : Integer.toString(tick * 5);
                g.drawString(label, left - 55, y + 4);
            }
            String[] sizes = {"100", "1,000", "10,000", "100,000"};
            for (int i = 0; i < sizes.length; i++)
                g.drawString(sizes[i], left + i * width / 3 - 18, top + height + 20);
            g.drawString("Input size n (log scale)", left + 130, top + height + 42);
            for (int algorithm = 0; algorithm < NAMES.length; algorithm++) {
                int previousX = -1, previousY = -1;
                g.setColor(COLORS[algorithm]);
                g.setStroke(new BasicStroke(2));
                for (String row : rows.subList(1, rows.size())) {
                    String[] values = row.split(",");
                    if (!values[0].equals(NAMES[algorithm]) || !values[1].equals(TYPES[panel])) continue;
                    double n = Double.parseDouble(values[2]);
                    double value = time ? Double.parseDouble(values[3]) / 1e6 : Double.parseDouble(values[6]);
                    double fraction = time ? (Math.log10(value) + 4) / 6 : value / 20;
                    int x = left + (int) ((Math.log10(n) - 2) * width / 3);
                    int y = top + height - (int) (fraction * height);
                    if (previousX >= 0) g.drawLine(previousX, previousY, x, y);
                    g.fillOval(x - 3, y - 3, 6, 6);
                    previousX = x;
                    previousY = y;
                }
            }
        }
        g.dispose();
        String name = time ? "time-vs-n.png" : "depth-vs-n.png";
        ImageIO.write(image, "png", Path.of("docs/plots", name).toFile());
    }
}
