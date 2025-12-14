import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Set the video to download
        String videoString = "https://web.archive.org/web/20250829030207/https://www.youtube.com/watch?v=Ziblv6nzyp8";

        // Download the video
        downloadVideo(videoString);
    }

    private static void downloadVideo(String videoString) throws IOException, InterruptedException {
        // Output directory
        String outputDir = "videos/%(title)s.%(ext)s";

        // Run yt-dlp
        File ytDlp = new File("src/yt-dlp.exe"); // current directory
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