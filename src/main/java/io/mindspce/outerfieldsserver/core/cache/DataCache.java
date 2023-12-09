package io.mindspce.outerfieldsserver.core.cache;

import javax.xml.crypto.Data;


public class DataCache {
    private static DataCache INSTANCE = new DataCache();



    public static DataCache GET() {
        return INSTANCE;
    }

}
