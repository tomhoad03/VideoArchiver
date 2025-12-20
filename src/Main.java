import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Files.walk(Paths.get("exports")).filter(Files::isRegularFile).forEach(file -> {
            try {
                Files.lines(file.toAbsolutePath()).forEach(videoUrl -> {
                    try {
                        // Create wayback url
                        String videoId = videoUrl.substring(videoUrl.indexOf("?v=") + 3);
                        String waybackUrl = "https://web.archive.org/web/2oe_/http://wayback-fakeurl.archive.org/yt/" + videoId;

                        // Download the video
                        int exitCode = downloadVideo(waybackUrl);

                        // Write the successful url
                        if (exitCode == 0) {
                            BufferedWriter writer = new BufferedWriter(new FileWriter("extracted_urls"));
                            writer.write(videoUrl);
                            writer.newLine();
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                });
            } catch (IOException e) {
                System.out.println(e);
            }
        });
    }

    private static int downloadVideo(String videoString) throws IOException, InterruptedException {
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
        return process.waitFor();
    }
}