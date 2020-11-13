package ai.abstraction;

import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ai.abstraction.Term;


public class RbsAI {

    KnowledgeBase kb;

    //Not sure if want to keep this here
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;



    public RbsAI(UnitTypeTable a_utt) {
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");

    }


    //Currently set for basic
    void perception(int player_numb,  PhysicalGameState pgs, Player p, GameState gs){
        for (Unit u : pgs.getUnits()){
            if(u.getPlayer() != player_numb)
                continue;
            if(u.getType() == baseType)
                kb.addTerm(new Term(1, 3));

            if(u.getType() == baseType && gs.getActionAssignment(u) == null)
                kb.addTerm(new Term(7, 3));

            if(u.getType() == workerType)
                kb.addTerm(new Term(1, 2));

            if(u.getType() == barracksType)
               kb.addTerm(new Term(1, 4));

            if(u.getType() == workerType && gs.getActionAssignment(u) == null)
                kb.addTerm(new Term(7, 2));

            if(u.getType() == lightType && gs.getActionAssignment(u) == null)
                kb.addTerm(new Term(7, 5));

            if(p.getResources() >= workerType.cost)
               kb.addTerm(new Term(6, 2));

            if(p.getResources() >= baseType.cost)
                kb.addTerm(new Term(6, 3));

            if(p.getResources() >= barracksType.cost)
                kb.addTerm(new Term(6, 4));

        }
    }

    //Think can just implement single unification algorithm for whole kb
    //What about that not stuff
    //May need to work in implementing equality operator
    public Map<Term, Term> unification(List<Term> pattern, KnowledgeBase kb){
        Map<Term, Term> bindings = new HashMap<Term, Term>();

        for(Term t1: pattern)
            for(Term t2: kb.facts) {
                if (!unification(t1, t2))
                    bindings.put(t1, t2);
                if (!bindings.isEmpty())
                    continue;
            }
            return bindings;


    }

    public boolean unification(Term t1, Term t2){

        if(t1.functor != t2.functor)
            return false;
        if(t1.parameters.length != t2.parameters.length)
            return false;

        for(int i = 0; i < t1.parameters.length; i++){
            if(t1.parameters[i] < 0)
                return true;
            else if(t1.parameters[i] != t2.parameters[i])
                return false;
        }
        return false;

    }

    public Rule instantiate(Rule r, Map<Term, Term> bindings){

    }

    void ruleBasedSystemIteration(Rule[] rules){

        ArrayList<Rule> fireRules = new ArrayList<Rule>();
        Map<Term, Term> bindings;

        for(Rule r: rules){

            bindings = unification(r.pattern, kb);
            if(!bindings.isEmpty())
                FiredRules.add(instantiate(r, bindings));
            RulesToExecute = arbitrate(FiredRules);
            for(Rules e: RulesToExecute)
                Execute(r.action)


        }


    }

    //Check if return should be void or back to Playeractions
    public void getAction(int player, GameState gs) throws FileNotFoundException {

        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);

        perception(player, pgs, p, gs);

        //This should be an array of rules need to pass in
        Rule rules = new Rule();
        ruleBasedSystemIteration(rules);

        //Possibly clear at end or start of each move so no accumulation??
        //Notes seem to indicate so

        //Not sure if need this but added cause was there for last one
        //return translateActions(player, gs);
        return;


    }


}
