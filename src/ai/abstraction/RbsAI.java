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


    }

    public AI clone() {
        return new  RbsAI(utt, pf);
    }


    void perception(int player_numb,  PhysicalGameState pgs, Player p, GameState gs){

        boolean has_worker = false;
        boolean has_base = false;
        boolean has_barracks = false;

        for (Unit u : pgs.getUnits()){
            if(u.getPlayer() != player_numb)
                continue;
            if(u.getType() == baseType) {
                kb.addTerm(new Term(1, 3));
                has_base = true;
            }

            if(u.getType() == baseType && gs.getActionAssignment(u) == null)
                kb.addTerm(new Term(7, 3));

            if(u.getType() == workerType) {
                kb.addTerm(new Term(1, 2));
                has_worker = true;
            }

            if(u.getType() == barracksType) {
                kb.addTerm(new Term(1, 4));
                has_barracks = true;
            }

            if(u.getType() == barracksType && gs.getActionAssignment(u) == null) {
                kb.addTerm(new Term(7, 4));
            }

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

            if(p.getResources() >= lightType.cost)
                kb.addTerm(new Term(6, 5));

        }

        if(!has_barracks)
            kb.addTerm(new Term(14, 4));

        if(!has_worker)
            kb.addTerm(new Term(14, 2));
        if(!has_base)
            kb.addTerm(new Term(14, 3));
    }


    public Map<Integer, Integer> unification(List<Term> pattern, KnowledgeBase kb){
        Map<Integer, Integer> bindings = new HashMap<Integer, Integer>();
        Map<Integer, Integer> temp;


        for(Term t1: pattern){
            temp = unification(t1, kb);
            if(temp != null)
                bindings.putAll(temp);
        }



        return bindings;


    }

    public Map<Integer, Integer> unification(Term t1, KnowledgeBase kb){
        Map<Integer, Integer> bindings = new HashMap<Integer, Integer>();;
        for(Term s: kb.facts) {
            bindings = unification(t1, s);
            if(bindings != null)
                return bindings;
        }
        return null;


    }

    public Map<Integer, Integer> unification(Term t1, Term t2){


        t1.print_term();

        Map<Integer, Integer> bindings = new HashMap<Integer, Integer>();

        if(t1.functor != t2.functor)
            return null;
        

        if(t1.parameters.length != t2.parameters.length)
            return null;

        for(int i = 0; i < t1.parameters.length; i++){
            if(t1.parameters[i] < 0)
               bindings.put(t1.parameters[i], t2.parameters[i]);
            else if(t1.parameters[i] != t2.parameters[i])
                return null;
        }

        return bindings;

    }

    public ArrayList<Rule> arbitrate(ArrayList<Rule> FiredRules){
        Collections.shuffle(FiredRules);
        return FiredRules;
    }



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

        List<Unit> units =  pgs.getUnits();
        List<Unit> player_units = get_player_units(units, player);

        if(!conditons_met(r.pattern))
            return;

        List<Integer> reservedPositions = new LinkedList<>();

        for(Term term: r.effect){
            //Test
            System.out.println("functor");
            System.out.println(term.functor);
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

    public void substitute_term(Term term, HashMap<Integer, Integer> bindings){
        for(int i = 0; i < term.parameters.length; i++)
            if(bindings.get(term.parameters[i]) != null)
                term.parameters[i] = bindings.get(term.parameters[i]);


    }

    public Rule instantiate(Rule r, HashMap<Integer, Integer> bindings) throws FileNotFoundException {

        for(Term term: r.pattern ){
            substitute_term(term, bindings);
        }
        for(Term term: r.effect ){
            substitute_term(term, bindings);
        }

        return r;
    }

    void ruleBasedSystemIteration(ArrayList<Rule> rules, PhysicalGameState pgs, Player player ) throws FileNotFoundException {

        ArrayList<Rule> firedRules = new ArrayList<Rule>();
        HashMap<Integer, Integer> bindings = new HashMap<Integer, Integer>();

        ArrayList<Rule> rulesToExecute;


        for(Rule r: rules) {

           bindings = (HashMap<Integer, Integer>) unification(r.pattern, kb);

            System.out.println(bindings);
            if (!bindings.isEmpty())
                instantiate(r, bindings);

        }


            firedRules = rules;
            rulesToExecute = arbitrate(firedRules);


            for(Rule e: rulesToExecute){
                execute(e, pgs, player);
            }


            //Test
            //System.out.println("iteration");







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

        //file = new File("src/ai/abstraction/rules-simple.txt");
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
            //Test
            //rule.print_rule();
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

        //Test
        //kb.print_kb();

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

    public static void main(String[] args) throws Exception {

        UnitTypeTable utt = new UnitTypeTable();
        //"maps/8x8/basesWorkers8x8.xml"
        //PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
        //basesWorkersBarracks8x8
        //PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/bases8x8.xml", utt);
        PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkersBarracks8x8.xml", utt);
        GameState gs = new GameState(pgs, utt);
        RbsAI ai2 = new RbsAI(utt, new AStarPathFinding());

        //Term t1 = new Term(2, 3);
        //Term t2 = new Term(2, 3);

        //System.out.println("test unification");
        //System.out.println(ai2.unification(t1, t2));



        ai2.getAction(1, gs);
        //ai2.kb.print_kb();


    }

}
