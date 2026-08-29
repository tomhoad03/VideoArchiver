import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String YTDLP_PATH = Path.of("yt-dlp.exe").toAbsolutePath().toString();
    private static final String IMPORTED_URLS_PATH = "targeted/targeted_urls_%s.csv";
    private static final String EXPORTED_VIDEOS_PATH = "videos/%(title)s.%(ext)s";

    /**
     * Main class for VideoArchiver
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i <= 0; i++) {
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
            Video video = new Video(split[0], split[1], split.length > 2 && Boolean.parseBoolean(split[2]));

            // Add video to the list
            videos.add(video);
        });

        return videos;
    }

    /**
     * Download all the videos
     */
    private static void downloadFromVideos(List<Video> videos, Path exportedVideosPath, int base) throws IOException, InterruptedException {
        AtomicInteger count = new AtomicInteger(Math.max(0, (base * 100) - 99));

        for (Video video : videos) {

            // Download the video
            System.out.println("[" + count.get() + " - " + LocalTime.now() + "] Downloading " + video.getUrl() + "...");

            for (String waybackUrl : video.getWaybackUrls()) {
                Video newVideo = downloadVideo(video, waybackUrl);
                video.setExitCode(newVideo.getExitCode());

                // Download succeeded
                if (newVideo.getExitCode() == 0) {
                    video.setSucceeded(true);
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
        // Run yt-dlp
        Process process = new ProcessBuilder(YTDLP_PATH, "-o", EXPORTED_VIDEOS_PATH, "-f", "best", "--ignore-errors", "--force-download-archive", waybackUrl).start();

        // Read the output of the process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(System.out::println);

        // Download is finished, set status code
        video.setExitCode(process.waitFor());

        // Return the video
        return video;
    }
}