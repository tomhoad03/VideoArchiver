import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Main {
    private static final String YTDLP_PATH = Path.of("yt-dlp.exe").toAbsolutePath().toString();
    private static final String ARCHIVE_URL = "https://web.archive.org/web/2oe_/http://wayback-fakeurl.archive.org/yt/";
    private static final Path IMPORTED_URLS_PATH = Path.of("imported_urls").toAbsolutePath();
    private static final Path EXPORTED_URLS_PATH = Path.of("exported_urls");
    private static final String EXPORTED_VIDEOS_PATH = "videos/%(title)s.%(ext)s";
    private static final boolean IS_DRY_RUN = true;

    /**
     * Main class for VideoArchiver
     */
    public static void main(String[] args) throws Exception {
        downloadFromImportedUrls();
    }

    /**
     * Download all the videos from the imported_urls file
     *
     * @throws Exception if a download fails
     */
    private static void downloadFromImportedUrls() throws Exception {
        Files.lines(IMPORTED_URLS_PATH).forEach(videoUrl -> {
            try {
                // Create wayback url
                String videoId = videoUrl.substring(videoUrl.indexOf("?v=") + 3);
                String waybackUrl = ARCHIVE_URL + videoId;

                // Download the video
                int exitCode = downloadVideo(waybackUrl);

                // Write out the successful url
                if (exitCode == 0)
                    Files.write(EXPORTED_URLS_PATH, (videoUrl + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            } catch (Exception ignored) {}
        });
    }

    /**
     * Download a single video using yt-dlp
     *
     * @param videoString the string of the video to download
     * @return The exit code from downloading a video
     * @throws Exception if the download fails
     */
    private static int downloadVideo(String videoString) throws Exception {
        // Dry run condition
        String dryRunFlag = IS_DRY_RUN ? "--simulate" : "";

        // Run yt-dlp
        Process process = new ProcessBuilder(YTDLP_PATH, "-o", EXPORTED_VIDEOS_PATH, dryRunFlag, "-f", "best", videoString).start();

        // Read the output of the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(System.out::println);

        // Download is finished, return status code
        return process.waitFor();
    }
}