package ai.abstraction;
import rts.units.Unit;

import java.util.Arrays;
import java.util.Objects;

public class Term {


    public final static int OWN = 1;
    public final static int NOTOWN = 14;
    public final static int HASENOUGHRESOURCESFOR = 6;
    public final static int IDLE = 7;
    public final static int DOTRAINWORKER = 8;
    public final static int DOBUILDBASE = 9;
    public final static int DOBUILDBARRACKS = 10;
    public final static int DOHARVEST = 11;
    public final static int DOTRAINLIGHT = 12;
    public final static int DOATTACK = 13;
    public final static int WORKER = 2;
    public final static int BASE = 3;
    public final static int BARRACKS = 4;
    public final static int LIGHT = 5;


    int functor;
    int[] parameters;

    public Term(int f, int p1 ){
        functor = f;
        parameters = new int[]{p1};

    }

    public void print_term(){
        System.out.println();
        System.out.println("functor");
        System.out.println(functor);
        System.out.println("Params");
        for(int param: parameters)
            System.out.println(param);

    }

    @Override
    public boolean equals(Object o) {


        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term term = (Term) o;
        return functor == term.functor &&
                Arrays.equals(parameters, term.parameters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(functor);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }


}

