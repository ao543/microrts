package ai.abstraction;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import rts.PlayerAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;



import ai.abstraction.Term;


public class RbsAI extends AbstractionLayerAI{

    KnowledgeBase kb;

    //Not sure if want to keep this here
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;

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
    HashMap<Integer, UnitType> hash_map;


    public RbsAI(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
        kb = new KnowledgeBase();

        //hash_map = new HashMap<Integer, UnitType>();

    }

    public AI clone() {
        return new  RbsAI(utt, pf);
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

        for(Term t1: r.pattern) {
            if (bindings.containsKey(t1))
                r.pattern.set(r.pattern.indexOf(t1), bindings.get(t1));
        }
        return r;

    }


    //Implemented with Random Rule
    public ArrayList<Rule> arbitrate(ArrayList<Rule> FiredRules){
        Collections.shuffle(FiredRules);
        return FiredRules;
    }


    //May be unneecessary and need to delete
    public List<Unit> get_player_units(List<Unit> units, Player player){

        ArrayList<Unit> player_units = new ArrayList<Unit>();

        for(Unit u: units) {
            if (u.getPlayer() == player.getID())
                player_units.add(u);

        }
        return player_units;

    }




    public boolean conditons_met(List<Term> pattern){

        for(Term term: pattern)
            if(!kb.facts.contains(term))
                return false;

        return true;
    }



    public void execute(Rule r, PhysicalGameState pgs, Player player){

        //Write up conditions for u


        List<Unit> units =  pgs.getUnits();
        List<Unit> player_units = get_player_units(units, player);
        //

        if(!conditons_met(r.pattern))
            return;


        List<Integer> reservedPositions = new LinkedList<>();

        for(Term term: r.effect){
            switch(term.functor){
                case 8:
                    for(Unit u: player_units)
                        if(u.getType() == baseType)
                            train(u, workerType);
                    break;
                case 9:
                    for(Unit u: player_units)
                        if(u.getType() == workerType)
                            buildIfNotAlreadyBuilding(u,baseType,u.getX(),u.getY(),reservedPositions,player,pgs);
                    break;
                case 10:
                    for(Unit u: player_units)
                        if(u.getType() == workerType)
                            buildIfNotAlreadyBuilding(u,barracksType,u.getX(),u.getY(),reservedPositions,player,pgs);
                    break;
                case 11:
                    Unit closestBase = null;
                    Unit closestResource = null;
                    for(Unit u: player_units) {
                        if (u.getType() == workerType) {

                            int closestDistance = 0;
                            for (Unit u2 : pgs.getUnits()) {
                                if (u2.getType().isResource) {
                                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                                    if (closestResource == null || d < closestDistance) {
                                        closestResource = u2;
                                        closestDistance = d;
                                    }
                                }
                                if (u2.getType().isStockpile && u2.getPlayer()==player.getID()) {
                                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                                    if (closestBase == null || d < closestDistance) {
                                        closestBase = u2;
                                        closestDistance = d;
                                    }
                                }
                            }
                        }
                        harvest(u, closestResource, closestBase);
                    }

                    break;
                case 12:
                    for(Unit u: player_units)
                        if(u.getType() == barracksType)
                            train(u, lightType);
                    break;
                case 13:
                    for(Unit u: player_units)
                        if(u.getType() == lightType) {
                            Unit closestEnemy = null;
                            int closestDistance = 0;
                            for (Unit u2 : pgs.getUnits()) {
                                if (u2.getPlayer() >= 0 && u2.getPlayer() != player.getID()) {
                                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                                    if (closestEnemy == null || d < closestDistance) {
                                        closestEnemy = u2;
                                        closestDistance = d;
                                    }
                                }
                            }
                            attack(u, closestEnemy);
                        }
                    break;




            }

        }

    }

    void ruleBasedSystemIteration(ArrayList<Rule> rules, PhysicalGameState pgs, Player player ){

        ArrayList<Rule> firedRules = new ArrayList<Rule>();
        Map<Term, Term> bindings;
        ArrayList<Rule> rulesToExecute;

        for(Rule r: rules){

            bindings = unification(r.pattern, kb);
            if(!bindings.isEmpty())
                firedRules.add(instantiate(r, bindings));
            rulesToExecute = arbitrate(firedRules);
            for(Rule e: rulesToExecute)
                execute(e, pgs, player);


        }


    }

    public ArrayList<Rule> loadRules() throws FileNotFoundException {

        //Not sure if need all of these fields/which of them you need
        File file;
        Scanner input;
        String line;
        String action_token;
        String[] split_line;
        String[] split_line2;
        String condition_string;
        String[] condition_tokens;
        Map<String, Integer> hashMap = new HashMap();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        String global_obj;
        Rule rule;

        file = new File("src/ai/abstraction/rules-simple.txt");
        input = new Scanner(file);


        while (input.hasNextLine()) {
            line = input.nextLine();

            //Test
            //System.out.println(line);

            if (line.isEmpty())
                continue;

            split_line = line.split(" ");
            if (split_line[0].equals("#"))
                continue;

            rule = new Rule(line);
            rules.add(rule);


        }
        input.close();
        return rules;
    }
    //Check if return should be void or back to Playeractions
    public PlayerAction getAction(int player, GameState gs) throws FileNotFoundException {

        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);

        perception(player, pgs, p, gs);

        //This should be an array of rules need to pass in
        ArrayList<Rule> rules = loadRules();
        ruleBasedSystemIteration(rules, pgs, p);

        //Possibly clear at end or start of each move so no accumulation??
        //Notes seem to indicate so

        //Not sure if need this but added cause was there for last one
        //return translateActions(player, gs);
        kb.clear();
        return translateActions(player, gs);


    }

    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

    public static void main(String[] args){}

}
