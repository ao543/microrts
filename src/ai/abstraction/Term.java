package ai.abstraction;
import rts.units.Unit;

public class Term {
    //Maybe switch to diff types
    public static int OWN = 1;
    public static int HASENOUGHRESOURCESFOR = 6;

    public static int WORKER = 2;
    public static int BASE = 3;
    public static int BARRACKS = 4;
    public static int LIGHT = 5;

    int functor;
    int[] parameters;

    public Term(int f, int p1 ){
        int functor = f;
        parameters = new int[]{p1};

    }


}
