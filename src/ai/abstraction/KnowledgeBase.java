package ai.abstraction;


import ai.abstraction.Term;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase {

    List<Term> facts;

    public KnowledgeBase(){
        facts = new ArrayList<Term>();
    }

    void addTerm(Term t){
        facts.add(t);
    }

    void clear(){
        facts.clear();
    }

    public void print_kb(){
        System.out.println("Printing kb");
        for(Term t1: facts)
            t1.print_term();
        System.out.println("End print of kb");
    }

}
