package pievis.utils;

/**
 * Created by Pievis on 06/11/2016.
 */
public interface Logger {
    public final static String LEVEL_INFO = "info";
    public final static String LEVEL_WARNING = "warn";
    public final static String LEVEL_ERROR = "error";

    void log(String text, String level);
    void info(String text);
    void error(String text);
    void warning(String text);
}
