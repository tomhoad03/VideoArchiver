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
    private static final String ARCHIVE_URL_1 = "https://web.archive.org/web/2oe_/http://wayback-fakeurl.archive.org/yt/";
    private static final String ARCHIVE_URL_2 = "https://web.archive.org/web/20250000000000/";
    private static final Path IMPORTED_URLS_PATH = Path.of("imported_urls").toAbsolutePath();
    private static final Path EXPORTED_URLS_PATH = Path.of("exported_urls");
    private static final String EXPORTED_VIDEOS_PATH = "videos/%(title)s.%(ext)s";
    private static boolean IS_DRY_RUN = true;
    private static boolean IS_REMAINING_RUN = true;

    /**
     * Main class for VideoArchiver
     */
    public static void main(String[] args) throws Exception {
        // Check if this is a dry run
        String isDryRun = args[0];
        IS_DRY_RUN = Boolean.parseBoolean(isDryRun);

        // Check if this is a remaining run
        String isRemainingRun = args[1];
        IS_REMAINING_RUN = Boolean.parseBoolean(isRemainingRun);

        // Download the videos
        downloadFromUrls(getVideoUrls());
    }

    /**
     * @return the list of urls from the relevant file
     */
    private static ArrayList<String> getVideoUrls() throws IOException {
        ArrayList<String> importedVideoUrls = getUniqueVideoUrls(IMPORTED_URLS_PATH);
        ArrayList<String> exportedVideoUrls = getUniqueVideoUrls(EXPORTED_URLS_PATH);

        // Dry run download all videos in imported_urls
        if (IS_DRY_RUN)
            return importedVideoUrls;

        // Download all videos in exported_urls
        if (!IS_REMAINING_RUN)
            return exportedVideoUrls;

        // Try to download all videos from imported_urls that failed the dry run
        importedVideoUrls.removeAll(exportedVideoUrls);
        return importedVideoUrls;
    }

    /**
     * @return the list of unique urls from the relevant file
     */
    private static ArrayList<String> getUniqueVideoUrls(Path urls) throws IOException {
        HashSet<String> videoUrls = new HashSet<>();

        // Get the list of files to download
        Files.lines(urls).forEach(videoUrls::add);

        // Sort the video urls
        ArrayList<String> uniqueVideoUrls = new ArrayList<>(videoUrls);
        Collections.sort(uniqueVideoUrls);
        return uniqueVideoUrls;
    }

    /**
     * Download all the videos from the imported_urls file
     *
     * @throws Exception if a download fails
     */
    private static void downloadFromUrls(ArrayList<String> videoUrls) throws Exception {
        AtomicInteger count = new AtomicInteger();
        ArrayList<String> succeededVideoUrls = new ArrayList<>();

        for (String videoUrl : videoUrls) {
            try {
                // Create wayback url
                String videoId = videoUrl.substring(videoUrl.indexOf("?v=") + 3);
                String waybackUrl1 = ARCHIVE_URL_1 + videoId;

                // Download the video
                System.out.println("[" + count.get() + "] Downloading " + videoId + "...");
                int exitCode = downloadVideo(waybackUrl1);

                // Try alternative download
                if (exitCode != 0) {
                    String waybackUrl2 = ARCHIVE_URL_2 + videoUrl;
                    exitCode = downloadVideo(waybackUrl2);
                }

                System.out.println("Exit code: " + exitCode);
                count.getAndIncrement();

                // Write out the successful url
                if (exitCode == 0)
                    succeededVideoUrls.add(videoUrl);
            } catch (Exception ignored) {}
        }

        // Output the successful video urls
        if (IS_DRY_RUN && !IS_REMAINING_RUN)
            Files.write(EXPORTED_URLS_PATH, succeededVideoUrls, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        else Files.write(EXPORTED_URLS_PATH, succeededVideoUrls, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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