package ir.iranet.vms.util;

import java.util.Properties;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ehsan
 * Date: Jun 3, 2006
 * Time: 10:55:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class VMSProperties {


    private static final String VMS_PROPERTY_FILE = "vms.properties";

    public static void setProperty (String key, String value)
    {
        Properties props = new Properties();
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(VMS_PROPERTY_FILE);
            props.load (fis);
            props.setProperty(key, value);
            fis.close();

            fos = new FileOutputStream (VMS_PROPERTY_FILE);
            props.store(fos, "VMS Properties");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String getProperty (String key)
    {
        Properties props = new Properties();
        FileInputStream fis = null;
        String res=null;

        try {
            fis = new FileInputStream(VMS_PROPERTY_FILE);
            props.load (fis);
            res = props.getProperty(key);
            fis.close();
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

}
