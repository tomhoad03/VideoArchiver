import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        Files.lines(Path.of("urls")).forEach(video -> {
            try {
                downloadVideo(video);
            } catch (Exception e) {
                System.out.println(e);
            }
        });
    }

    private static void downloadVideo(String videoString) throws IOException, InterruptedException {
        // Output directory
        String outputDir = "videos/%(title)s.%(ext)s";

        // Run yt-dlp
        File ytDlp = new File("src/yt-dlp.exe");
        Process process = new ProcessBuilder(ytDlp.getAbsolutePath(), "-o", outputDir, "-f", "best", videoString).start();

        // Read the output of the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        // Download is finished
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
    }
}