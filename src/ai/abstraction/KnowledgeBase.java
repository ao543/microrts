package ai.abstraction;


import ai.abstraction.Term;
import java.util.List;

public class KnowledgeBase {

    List<Term> facts;

    void addTerm(Term t){
        facts.add(t);
    }

    void clear(){
        facts.clear();
    }
}
