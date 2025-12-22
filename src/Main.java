import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String YTDLP_PATH = Path.of("yt-dlp.exe").toAbsolutePath().toString();
    private static final String ARCHIVE_URL = "https://web.archive.org/web/2oe_/http://wayback-fakeurl.archive.org/yt/";
    private static final Path IMPORTED_URLS_PATH = Path.of("imported_urls").toAbsolutePath();
    private static final Path EXPORTED_URLS_PATH = Path.of("exported_urls");
    private static final String EXPORTED_VIDEOS_PATH = "videos/%(title)s.%(ext)s";
    private static boolean IS_DRY_RUN = true;

    /**
     * Main class for VideoArchiver
     */
    public static void main(String[] args) throws Exception {
        String isDryRun = args[0];
        IS_DRY_RUN = Boolean.parseBoolean(isDryRun);

        downloadFromImportedUrls();
    }

    /**
     * Download all the videos from the imported_urls file
     *
     * @throws Exception if a download fails
     */
    private static void downloadFromImportedUrls() throws Exception {
        AtomicInteger count = new AtomicInteger();
        HashSet<String> videoUrls = new HashSet<>();
        ArrayList<String> succeededVideoUrls = new ArrayList<>();

        // Get the list of files to download
        Files.lines(IS_DRY_RUN ? IMPORTED_URLS_PATH : EXPORTED_URLS_PATH).forEach(videoUrls::add);

        // Sort the video urls
        ArrayList<String> uniqueVideoUrls = new ArrayList<>(videoUrls);
        Collections.sort(uniqueVideoUrls);

        for (String videoUrl : uniqueVideoUrls) {
            try {
                // Create wayback url
                String videoId = videoUrl.substring(videoUrl.indexOf("?v=") + 3);
                String waybackUrl = ARCHIVE_URL + videoId;

                // Download the video
                System.out.println("[" + count.get() + "] Downloading " + videoId + "...");
                int exitCode = downloadVideo(waybackUrl);
                System.out.println("Exit code: " + exitCode);
                count.getAndIncrement();

                // Write out the successful url
                if (exitCode == 0)
                    succeededVideoUrls.add(videoUrl);
            } catch (Exception ignored) {}
        }

        // Output the successful video urls
        Files.write(EXPORTED_URLS_PATH, succeededVideoUrls, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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