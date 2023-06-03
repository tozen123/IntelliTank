package com.doublehammerstudios.intellitank;

import java.util.ArrayList;
import java.util.List;

public class GlobalConfiguration {

    private static GlobalConfiguration mInstance= null;
    protected GlobalConfiguration(){}
    public static List<String> device_ids = new ArrayList<String>() {{
        add("intellitank_01Bxc");
    }};

    public static synchronized GlobalConfiguration getInstance() {
        if(null == mInstance){
            mInstance = new GlobalConfiguration();
        }
        return mInstance;
    }

    public static Boolean confirmDevice(String id){
        if(device_ids.contains(id)){
            return true;
        } else{
            return false;
        }
    }

}

