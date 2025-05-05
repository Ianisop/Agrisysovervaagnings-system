package dk.agrisys.pigfeedingsystem;

import java.util.UUID;

public class Generator {
    public static String generate(int length){
        return UUID.randomUUID().toString().substring(0,length);
    }
}
