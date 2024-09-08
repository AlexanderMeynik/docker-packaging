package ru.edu.spbstu.client;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ru.edu.spbstu.client.utils.ClientProperties;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.nio.file.Path;

public class PropertiesTest extends ApplicationTest {
    static private String eng = "EN";
    static private String Rus = "RU";

    @Test
    public void testCreateProperties() throws IOException {
        String lang = eng;
        ClientProperties.setProperties(lang);
        var props = ClientProperties.getProperties();
        String gotten = props.getProperty("Language");
        if (!gotten.equals(lang)) {
            throw new InvalidClassException("Expected language " + lang + String.format("! Stored language %s!", gotten));
        }
    }

    @Test
    public void testDefaultProperties() throws IOException {

        File file = new File(String.valueOf(Path.of("properties.prop")));
        if (file.delete()) {
            System.out.println("File deleted successfully");
        } else {
            System.out.println("Failed to delete the file");
        }

        var props = ClientProperties.getProperties();
        String gotten = props.getProperty("Language");
        if (!gotten.equals(Rus)) {
            throw new InvalidClassException("Expected language " + Rus + String.format("! Stored language %s!", gotten));
        }
    }

}
