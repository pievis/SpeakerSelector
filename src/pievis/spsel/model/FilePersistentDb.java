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
    private final static String MEETINGS_FILE_NAME = "meetings.json";

    //
    String peopleFilePath;
    String meetingsFilePath;

    //
    String dirPath;
    Map<Integer, Person> peopleMap;
    AtomicInteger peopleAI;
    Map<Integer, Meeting> meetingsMap;
    AtomicInteger meetingsAI;

    /**
     * @param dirPath path to the directory that will hold the database
     * @throws IOException exception thrown if a problem occurs in creating the db files.
     */
    public FilePersistentDb(String dirPath) throws IOException {
        this.dirPath = dirPath;
        new File(dirPath).mkdirs();
        peopleFilePath = getFilePath(PEOPLE_FILE_NAME);
        meetingsFilePath = getFilePath(MEETINGS_FILE_NAME);
        new File(peopleFilePath).createNewFile();
        new File(meetingsFilePath).createNewFile();
        //load people in memory
        peopleMap = new HashMap<Integer, Person>();
        peopleAI = new AtomicInteger();
        loadPeople();
        //load meetings / events in memory
        meetingsAI = new AtomicInteger();
        meetingsMap = new HashMap<Integer, Meeting>();
        loadMeetings();
    }

    //Person

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

    @Override
    public List<Person> getPersonByIds(List<Integer> ids) {
        if(ids != null){
            List<Person> list = new ArrayList<>();
            for(Integer id : ids){
                Person p = peopleMap.get(id);
                if(p != null){
                    list.add(p);
                }
            }
            return list;
        }
        return null;
    }

    //Meeting

    @Override
    public List<Meeting> getMeetings() {
        if (meetingsMap == null)
            return null;
        return new ArrayList<Meeting>(meetingsMap.values());
    }

    @Override
    public Meeting getMeeting(int id) {
        if (meetingsMap == null)
            return null;
        return meetingsMap.get(id);
    }

    @Override
    public void deleteMeeting(int id) {
        meetingsMap.remove(id);
        saveMeetingsState();
    }

    @Override
    public void updateMeeting(Meeting meeting) {
        int id = meeting.getId();
        meetingsMap.put(id, meeting);
        saveMeetingsState();
    }

    @Override
    public int insertMeeting(Meeting meeting) {
        if (meetingsMap == null) {
            meetingsMap = new HashMap<Integer, Meeting>();
        }
        int id = meetingsAI.incrementAndGet();
        meeting.setId(id);

        meetingsMap.put(id, meeting);
        saveMeetingsState();
        return id;
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
        Type listType = new TypeToken<ArrayList<Person>>() {}.getType();
        List<Person> list = getJsonData(peopleFilePath, listType);
        mapPeopleFromList(list);
    }

    private void loadMeetings() {
        Type listType = new TypeToken<ArrayList<Meeting>>() {}.getType();
        List<Meeting> list = getJsonData(meetingsFilePath, listType);
        mapMeetingsFromList(list);
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

    private void mapMeetingsFromList(List<Meeting> list) {
        meetingsMap.clear();
        int maxId = 0;
        if (list != null) {
            for (Meeting m : list) {
                meetingsMap.put(m.getId(), m);
                int idVal = m.getId();
                if (idVal > maxId) {
                    maxId = idVal;
                }
            }
        }
        meetingsAI.set(maxId);
    }

    private void savePeopleState() {
        List<Person> list = new ArrayList<Person>(peopleMap.values());
        saveJsonData(list, peopleFilePath);
    }

    private void saveMeetingsState() {
        List<Meeting> list = new ArrayList<Meeting>(meetingsMap.values());
        saveJsonData(list, meetingsFilePath);
    }

    /**
     * Saves the list into a file using JSON standard
     *
     * @param list
     * @param filePath
     */
    private void saveJsonData(List list, String filePath) {
        String json = new Gson().toJson(list);
        try {
            SystemUtils.writeFileContents(filePath, json);
        } catch (Exception e) {
            e.printStackTrace();
            Config.instance().getLogger().error("Error while writing to file " + filePath);
        }
    }

    private <T> List<T> getJsonData(String filePath, Type listType) {
        try {
            String jsonStr = SystemUtils.readFileContents(filePath);
            List<T> list = new Gson().fromJson(jsonStr, listType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            Config.instance().getLogger().error("Error while reading " + filePath);
        }
        return null;
    }

    //Class tester function
    public static void main(String args[]) {
        Config conf = Config.instance();
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
        Meeting m = new Meeting();
        List<Person> list = db.getPeople();
        List<Integer> lp = new ArrayList<>();
        List<Integer> la = new ArrayList<>();
        for (Person p : list) {
            logger.info(p.getFullName() + " " + p.getId());
            lp.add(p.getId());
            if(p.getId() % 2 == 0){
                la.add(p.getId());
            }
        }
        m.setParticipants(lp);
        m.setAbsents(la);
        //logger.info(" ID 1 = " + db.getPerson(1).getFullName());
        m.setDate(new Date());
        m.setTitle("Meeting test");
        db.insertMeeting(m);

    }
}
