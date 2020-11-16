package ai.abstraction;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import ai.abstraction.Term;
import java.util.List;

public class Rule {

    List<Term> pattern = new ArrayList<Term>();
    List<Term> effect = new ArrayList<Term>();
    List<Integer> effectType = new ArrayList<Integer>();

    //Term[] pattern;
    //Term[] effect;
    //int[] effectType;
    String action_token;
    String[] split_line;
    String[] split_line2;
    String  condition_string;
    String[] condition_tokens;
    String global_obj;
    Map<String, Integer> hashMap;


    public Rule(String line) throws FileNotFoundException {



        hashMap = new HashMap();


        hashMap.put("Base", 3);
        hashMap.put("Worker", 2);
        hashMap.put("Barracks", 4);
        hashMap.put("Light", 5);
        hashMap.put("idle", 7);
        hashMap.put("own", 1);
        hashMap.put("enoughResourcesFor", 6);
        hashMap.put("doTrainWorker", 8);
        hashMap.put("doBuildBase", 9);
        hashMap.put("doBuildBarracks", 10);
        hashMap.put("doHarvest", 11);
        hashMap.put("doTrainLight", 12);
        hashMap.put("doAttack", 13);
        hashMap.put("~own", 14);

        //hashMap.put();
        split_line2 = line.split(":-");
        action_token = split_line2[0];
        condition_string = split_line2[1];
        set_effect(action_token);

        //Effect type what you were looking for to distinguish the two
        set_effect_type(action_token);
        set_pattern(condition_string );

    }





    public void set_pattern(String condition_string){
        Term term;
        String functor;
        String arg;
        condition_tokens = condition_string.split(",");
        for(String token: condition_tokens){

            if(token.contains("(")){
                functor = token.split("\\(" )[0];
                arg = token.split("\\(" )[1];
                arg = arg.substring(1, arg.indexOf(")") - 1) ;
            }

            else{
                functor = token;
                arg = global_obj;
                if(functor.contains("."))
                    functor = functor.substring(0, functor.length() - 1);
            }

            functor = functor.trim();
            //Test
            //System.out.println(functor);
            //System.out.println(arg);
            //System.out.println(hashMap.get(functor));
            //System.out.println(hashMap.get(arg));


            term = new Term(hashMap.get(functor).intValue(), hashMap.get(arg));


            pattern.add(term);
        }
    }

    public void set_effect(String action_token){
        String action_str = action_token.split("\\(" )[0];
        int first_quote = action_token.indexOf("\"");
        int last_quote = action_token.lastIndexOf("\"");
        String object_string = action_token.substring(first_quote + 1, last_quote);
        global_obj = object_string;

        //Test
        //System.out.println("Test numb");
        //System.out.println(hashMap.get(action_str));

        Term functor = new Term(hashMap.get(action_str), hashMap.get(object_string));
        effect.add(functor);

    }

    public void set_effect_type(String action_token){
        effectType.add(1);

    }

    public void print_rule(){

        System.out.println();
        System.out.println("Pattern ");
        System.out.println();

        for(Term term: pattern)
            term.print_term();

        System.out.println();
        System.out.println("Effect ");
        System.out.println();

        for(Term term: effect)
           term.print_term();
    }

    /*
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println(System.getProperty("user.dir"));
        Rule rule = new Rule();
        //System.out.println(rule.pattern.size());
        //System.out.println("Test");
        rule.print_rule();
    }

     */



}
