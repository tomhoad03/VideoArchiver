import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String YTDLP_PATH = Path.of("yt-dlp.exe").toAbsolutePath().toString();
    private static final Path IMPORTED_URLS_PATH = Path.of("imported_urls.csv").toAbsolutePath();
    private static final String EXPORTED_VIDEOS_PATH = "videos/%(title)s.%(ext)s";
    private static boolean IS_DRY_RUN = true;

    /**
     * Main class for VideoArchiver
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // Check if this is a dry run
        String isDryRun = args[0];
        IS_DRY_RUN = Boolean.parseBoolean(isDryRun);

        // Get the list of videos to download
        List<Video> videos = getVideos();

        // If downloading, filter out files that cannot be downloaded
        List<Video> filteredVideos = videos.stream()
                .filter(video -> IS_DRY_RUN || (video.getSucceeded() == true && video.getIgnore() == false))
                .toList();

        // Download the videos
        downloadFromVideos(filteredVideos);
    }

    /**
     * @return the list of videos
     */
    private static List<Video> getVideos() throws IOException {
        List<Video> videos = new ArrayList<>();

        // Get the list of files to download
        Files.lines(IMPORTED_URLS_PATH).forEach(row -> {
            String[] split = row.split(",");

            // Read video details from the csv
            Video video = new Video(split[0],
                    split.length > 2 && Boolean.parseBoolean(split[2]),
                    split.length > 5 && Boolean.parseBoolean(split[5]));

            // Add video to the list
            videos.add(video);
        });

        return videos;
    }

    /**
     * Download all the videos
     */
    private static void downloadFromVideos(List<Video> videos) throws IOException, InterruptedException {
        AtomicInteger count = new AtomicInteger();
        List<Video> newVideos = new ArrayList<>();

        for (Video video : videos) {
            // Download the video
            System.out.println("[" + count.get() + "] Downloading " + video.getId() + "...");

            for (String waybackUrl : video.getWaybackUrls()) {
                Video newVideo = downloadVideo(video, waybackUrl);

                if (newVideo.getExitCode() == 0) {
                    video.setSucceeded(true);
                    video.setSucceededUrl(waybackUrl);
                    video.setExitCode(0);
                    break;
                }
            }

            System.out.println("Exit code: " + video.getExitCode());
            count.getAndIncrement();
            newVideos.add(video);
        }

        // Don't update list of files if actually downloading
        if (!IS_DRY_RUN) {
            return;
        }

        // Output the video back to the file
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(IMPORTED_URLS_PATH)) {
            bufferedWriter.write("");

            for (Video video : newVideos) {
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
        String dryRunFlag = IS_DRY_RUN ? "--simulate" : "";

        // Run yt-dlp
        Process process = new ProcessBuilder(YTDLP_PATH, "-o", EXPORTED_VIDEOS_PATH, dryRunFlag, "-f", "best", "--ignore-errors", "--force-download-archive", waybackUrl).start();

        // Read the output of the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(System.out::println);

        // Download is finished, return status code
        video.setExitCode(process.waitFor());

        // Get the video name
        process = new ProcessBuilder(YTDLP_PATH, "--simulate", "--print", "\"%(title)s\"", video.getUrl()).start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(line -> {
            if (!line.contains("java.util.stream.ReferencePipeline")) {
                video.setName(line);
            }
        });

        // Return the video
        return video;
    }
}