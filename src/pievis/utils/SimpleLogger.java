package pievis.utils;

/**
 * Created by Pievis on 06/11/2016.
 */
public class SimpleLogger implements Logger {

    @Override
    public void log(String text, String level) {
        System.out.println(level + "] " + text);
    }

    @Override
    public void info(String text) {
        log(text, Logger.LEVEL_INFO);
    }

    @Override
    public void error(String text) {
        log(text, Logger.LEVEL_ERROR);
    }

    @Override
    public void warning(String text) {
        log(text, Logger.LEVEL_WARNING);
    }
}
