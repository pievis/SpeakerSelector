package pievis.spsel.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pievis.spsel.Config;
import pievis.utils.Logger;
import pievis.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Pievis on 06/11/2016.
 * Writes persistent information about the app on files.
 * This class can be used to access data persistently from files in a json format.
 */
public class FilePersistentDb implements Database {

    private final static String PEOPLE_FILE_NAME = "people.json";
    private final static String EVENTS_FILE_NAME = "events.json";

    //
    String peopleFilePath;
    String eventsFilePath;

    //
    String dirPath;
    Map<Integer, Person> peopleMap;
    AtomicInteger peopleAI;
    //TODO events

    /**
     * @param dirPath path to the directory that will hold the database
     * @throws IOException exception thrown if a problem occurs in creating the db files.
     */
    public FilePersistentDb(String dirPath) throws IOException {
        this.dirPath = dirPath;
        new File(dirPath).mkdirs();
        peopleFilePath = getFilePath(PEOPLE_FILE_NAME);
        eventsFilePath = getFilePath(EVENTS_FILE_NAME);
        new File(peopleFilePath).createNewFile();
        new File(eventsFilePath).createNewFile();
        //
        peopleMap = new HashMap<Integer, Person>();
        peopleAI = new AtomicInteger();
        loadPeople();
        //TODO load events
    }

    @Override
    public List<Person> getPeople() {
        if (peopleMap == null)
            return null;
        return new ArrayList<Person>(peopleMap.values());
    }

    @Override
    public Person getPerson(int id) {
        if (peopleMap == null)
            return null;
        return peopleMap.get(id);
    }

    @Override
    public int insertPerson(Person person) {
        if (peopleMap == null) {
            peopleMap = new HashMap<Integer, Person>();
        }
        int id = peopleAI.incrementAndGet();
        person.setId(id);

        peopleMap.put(person.getId(), person);
        savePeopleState();
        return id;
    }

    @Override
    public void updatePerson(Person person) {
        int id = person.getId();
        peopleMap.put(id, person);
        savePeopleState();
    }

    @Override
    public void deletePerson(Person person) {
        int id = person.getId();
        peopleMap.remove(id);
        savePeopleState();
    }
    //

    private String getFilePath(String fileName) {
        return dirPath + "/" + fileName;
    }

    /**
     * Loads people from specified file
     *
     * @return list of people.
     */
    private void loadPeople() {
        try {
            String jsonStr = SystemUtils.readFileContents(peopleFilePath);
            Type listType = new TypeToken<ArrayList<Person>>() {
            }.getType();
            List<Person> list = new Gson().fromJson(jsonStr, listType);
            mapPeopleFromList(list);
        } catch (Exception e) {
            e.printStackTrace();
            Config.get().getLogger().error("Error while reading " + peopleFilePath);
        }
    }

    private void mapPeopleFromList(List<Person> list) {
        peopleMap.clear();
        int maxId = 0;
        if (list != null) {
            for (Person p : list) {
                peopleMap.put(p.getId(), p);
                int idVal = p.getId();
                if (idVal > maxId) {
                    maxId = idVal;
                }
            }
        }
        peopleAI.set(maxId);
    }

    private void savePeopleState() {
        List<Person> list = new ArrayList<Person>(peopleMap.values());
        String json = new Gson().toJson(list);
        try {
            SystemUtils.writeFileContents(peopleFilePath, json);
        } catch (Exception e) {
            e.printStackTrace();
            Config.get().getLogger().error("Error while writing to file " + peopleFilePath);
        }
    }



    //Class tester function
    public static void main(String args[]) {
        Config conf = Config.get();
        Database db = null;
        try {
            db = new FilePersistentDb("db");
            conf.setDatabase(db);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (db == null)
            return;
/*
        Person p1 = new Person();
        p1.setFirstName("sergio");
        p1.setLastName("montagna");
        Person p2 = new Person();
        p2.setFirstName("sandra");
        p2.setLastName("calcagnile");

        db.insertPerson(p1);
        db.insertPerson(p2);
        */
        Logger logger = conf.getLogger();
        List<Person> list = db.getPeople();
        for (Person p : list) {
            logger.info(p.getFullName() + " " + p.getId());
        }

        logger.info(" ID 1 = " + db.getPerson(1).getFullName());

    }
}
