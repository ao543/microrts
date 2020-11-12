package ai.abstraction;

import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;


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
    void perception(int player_numb,  PhysicalGameState pgs, Player p){
        for (Unit u : pgs.getUnits()){
            if(u.getPlayer() != player_numb)
                continue;
            if(u.getType() == baseType)
                kb.addTerm(new Term(1, 3));

           if(u.getType() == workerType)
                kb.addTerm(new Term(1, 2));

           if(p.getResources() >= workerType.cost)
               kb.addTerm(new Term(6, 2));

        }
    }

    public PlayerAction getAction(int player, GameState gs) {

        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);

        perception(player, pgs, p);
        RuleBasedSystemIteration();

        //Possibly clear at end or start of each move so no accumulation??
        //Notes seem to indicate so

    }


}
