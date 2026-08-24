import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String YTDLP_PATH = Path.of("yt-dlp.exe").toAbsolutePath().toString();
    private static final String IMPORTED_URLS_PATH = "imported/imported_urls_%s.csv";
    private static final String EXPORTED_VIDEOS_PATH = "videos/%(title)s.%(ext)s";
    private static boolean IS_DRY_RUN = true;

    /**
     * Main class for VideoArchiver
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // Check if this is a dry run
        IS_DRY_RUN = Boolean.parseBoolean(args[0]);

        // Test 100 videos at a time
        for (int i = 1; i < 12; i++) {
            // Get the list of videos to download
            Path urlsPath = Path.of(IMPORTED_URLS_PATH.formatted(i));
            List<Video> videos = getVideos(urlsPath.toAbsolutePath());

            // Download the videos
            downloadFromVideos(videos, urlsPath.toAbsolutePath(), i);
        }
    }

    /**
     * @return the list of videos
     */
    private static List<Video> getVideos(Path importedVideosPath) throws IOException {
        List<Video> videos = new ArrayList<>();

        // Get the list of files to download
        Files.lines(importedVideosPath).forEach(row -> {
            String[] split = row.split(",");

            // Read video details from the csv
            Video video = new Video(split[0],
                    split.length > 1 ? split[1] : "null",
                    split.length > 2 && Boolean.parseBoolean(split[2]),
                    split.length > 3 ? split[3] : "null",
                    split.length > 5 && Boolean.parseBoolean(split[5]));

            // Add video to the list
            videos.add(video);
        });

        return videos;
    }

    /**
     * Download all the videos
     */
    private static void downloadFromVideos(List<Video> videos, Path exportedVideosPath, int base) throws IOException, InterruptedException {
        AtomicInteger count = new AtomicInteger((base * 100) - 99);

        for (Video video : videos) {
            // Ignore downloading if it cannot be downloaded
            if (!IS_DRY_RUN && (!video.getSucceeded() || video.getIgnore())) {
                count.getAndIncrement();
                continue;
            }

            // Download the video
            System.out.println("[" + count.get() + " - " + LocalTime.now() + "] Downloading " + video.getId() + "...");

            for (String waybackUrl : video.getWaybackUrls()) {
                Video newVideo = downloadVideo(video, waybackUrl);
                video.setExitCode(newVideo.getExitCode());

                // Download succeeded
                if (newVideo.getExitCode() == 0) {
                    video.setSucceeded(true);
                    video.setSucceededUrl(waybackUrl);
                    break;
                }
            }

            System.out.println("Exit code: " + video.getExitCode());
            count.getAndIncrement();
        }

        // Output the video back to the file
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(exportedVideosPath)) {
            bufferedWriter.write("");

            for (Video video : videos) {
                bufferedWriter.append(video.toString()).append("\n");
            }

            bufferedWriter.flush();
        }
    }

    /**
     * Download a single video using yt-dlp
     */
    private static Video downloadVideo(Video video, String waybackUrl) throws IOException, InterruptedException {
        // Dry run condition
        //String dryRunFlag = IS_DRY_RUN ? "--simulate" : "";

        // Run yt-dlp
        Process process = new ProcessBuilder(YTDLP_PATH, "-o", EXPORTED_VIDEOS_PATH, "--simulate", "-f", "best", "--ignore-errors", "--force-download-archive", waybackUrl).start();

        // Read the output of the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(System.out::println);

        // Download is finished, return status code
        video.setExitCode(process.waitFor());

        // Get the video name
        process = new ProcessBuilder(YTDLP_PATH, "--simulate", "--print", "\"%(title)s\"", waybackUrl).start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(line -> {
            if (!line.contains("java.util.stream.ReferencePipeline")) {
                System.out.println("Found video to download: " + line);
                video.setName(line.replace(",", ""));
            }
        });

        // Return the video
        return video;
    }
}