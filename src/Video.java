import java.util.List;

public final class Video {
    private final String id;
    private String name;
    private Boolean succeeded;
    private String succeededUrl;
    private Integer exitCode;
    private final Boolean ignore;

    private static final String ARCHIVE_URL_1 = "https://web.archive.org/web/2oe_/http://wayback-fakeurl.archive.org/yt/";
    private static final String ARCHIVE_URL_2 = "http://web.archive.org/web/20260110000000/";
    private static final String ARCHIVE_URL_3 = "https://www.webcitation.org/query?url=";
    private static final String ARCHIVE_URL_4 = "https://megalodon.jp/?url=";

    public Video(String id, String name, Boolean succeeded, String succeededUrl, Boolean ignore) {
        this.id = id;
        this.name = name;
        this.succeeded = succeeded;
        this.succeededUrl = succeededUrl;
        this.exitCode = 1;
        this.ignore = ignore;
    }

    public String getId() {
        return id;
    }

    public Boolean getSucceeded() {
        return succeeded;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public Boolean getIgnore() {
        return ignore;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSucceeded(Boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void setSucceededUrl(String succeededUrl) {
        this.succeededUrl = succeededUrl;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public List<String> getWaybackUrls() {
        return List.of(ARCHIVE_URL_1 + id, ARCHIVE_URL_2 + id, ARCHIVE_URL_3 + id, ARCHIVE_URL_4 + id);
    }

    @Override
    public String toString() {
        return id + "," + name + "," + succeeded + "," + succeededUrl + "," + ignore;
    }
}
