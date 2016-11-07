package pievis.spsel;

import pievis.spsel.model.Database;
import pievis.utils.Logger;
import pievis.utils.SimpleLogger;

/**
 * Created by Pievis on 06/11/2016.
 * Holding the application general settings
 */
public class Config {
    private Database database;
    private Logger logger;

    static Config instance = null;

    public static Config get() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        this.logger = new SimpleLogger();
    }

    public void setDatabase(Database database) {
        if(this.database != null){
            logger.error("database has already been set");
        }else {
            this.database = database;
        }
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Database getDatabase() {
        return database;
    }

    public Logger getLogger() {
        return logger;
    }
}
