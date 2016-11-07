package pievis.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pievis on 06/11/2016.
 */
public class SystemUtils {

    /**
     * Read the content of the file in a single line.
     * @param filePath
     * @return The content of the specified file
     * @throws IOException exception if the file doesn't exist or can't be opened.
     */
    public static String readFileContents(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return content;
    }

    /**
     * Writes the content to a file, overwriting its original content.
     * The file is created if it does not exist.
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void writeFileContents(String filePath, String content) throws IOException {
        Charset utf8 = StandardCharsets.UTF_8;
        Files.write(Paths.get(filePath), content.getBytes(utf8));
    }

    public static String formatDate(Date d, String pattern){
        if(d == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(d);
    }

    public static String formatDate(Date d){
        return formatDate(d, "dd/MM/yyyy");
    }


    public static Date parseDate(String str, String pattern){
        if(str == null)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        str = str.trim();
        if(str.equals(""))
            return null;
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date parseDate(String str){
        return parseDate(str, "dd/MM/yyyy");
    }
}
