import java.util.List;

public final class Video {

    private final String url;
    private final String name;
    private Integer exitCode;
    private Boolean succeeded;

    private static final String ARCHIVE_URL_1 = "https://web.archive.org/web/2oe_/http://wayback-fakeurl.archive.org/yt/";
    private static final String ARCHIVE_URL_2 = "http://web.archive.org/web/20260110000000/";
    private static final String ARCHIVE_URL_3 = "https://www.webcitation.org/query?url=";
    private static final String ARCHIVE_URL_4 = "https://megalodon.jp/?url=";

    public Video(String url, String name, Boolean succeeded) {
        this.url = url;
        this.name = name;
        this.succeeded = succeeded;
    }

    public String getUrl() {
        return url;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setSucceeded(Boolean succeeded) {
        this.succeeded = succeeded;
    }

    public List<String> getWaybackUrls() {
        String id = url.substring(url.indexOf("?v=") + 3);

        return List.of(ARCHIVE_URL_1 + id, ARCHIVE_URL_2 + id, ARCHIVE_URL_3 + id, ARCHIVE_URL_4 + id);
    }

    @Override
    public String toString() {
        return url + "," + name + "," + succeeded;
    }
}
