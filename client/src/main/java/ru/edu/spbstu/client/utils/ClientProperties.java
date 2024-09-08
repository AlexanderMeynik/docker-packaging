package ru.edu.spbstu.client.utils;

import java.io.*;
import java.util.Properties;

public class ClientProperties {
    public static void setProperties(String language) throws IOException {

            String Language = language;
            Properties props = new Properties();
            props.setProperty("Language", Language);
            saveProperties(props);

    }
    private static Properties getDefaultProperties()
    {
        String Language = "RU";
        Properties props = new Properties();
        props.setProperty("Language", Language);
        return props;
    }
    public static  void saveProperties(Properties props) throws IOException {
        File f = new File("properties.prop");
        OutputStream out = new FileOutputStream( f );
        props.store(out, "User properties");
        out.close();
    }
    public static Properties getProperties() throws IOException {
        Properties props = new Properties();
        File f = new File("properties.prop");
        try(InputStream in = new FileInputStream(f)) {
            props.load(in);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("No properties");
            Properties temp=getDefaultProperties();
            saveProperties(temp);
            return getDefaultProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}
