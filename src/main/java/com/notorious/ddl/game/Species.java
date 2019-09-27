package com.notorious.ddl.game;

import com.rits.cloning.Cloner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextFloat;
import static org.apache.commons.math3.util.FastMath.abs;

@Getter
@Slf4j
public class Species {

    private static final Cloner CLONER = new Cloner();

    private List<Player> players = new ArrayList<>();
    private float bestFitness = 0;
    private Player champ;
    private float averageFitness = 0;
    private int staleness = 0;//how many generations the species has gone without an improvement
    private Genome rep;

    //--------------------------------------------
    //coefficients for testing compatibility
    private float excessCoeff = 1;
    private float weightDiffCoeff = 0.5f;
    private float compatibilityThreshold = 3;
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //empty constructor

    public Species() {
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor which takes in the player which belongs to the species
    public Species(Player p) {
        players.add(p);
        //since it is the only one in the species it is by default the best
        bestFitness = p.fitness;
        rep = CLONER.deepClone(p.brain);
        champ = p.cloneForReplay();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns whether the parameter genome is in this species
    public boolean sameSpecies(Genome g) {
        float compatibility;
        float excessAndDisjoint = getExcessDisjoint(g, rep);//get the number of excess and disjoint genes between this player and the current species rep
        float averageWeightDiff = averageWeightDiff(g, rep);//get the average weight difference between matching genes


        float largeGenomeNormaliser = g.getGenes().size() - 20;
        if (largeGenomeNormaliser < 1) {
            largeGenomeNormaliser = 1;
        }

        compatibility = (excessCoeff * excessAndDisjoint / largeGenomeNormaliser) + (weightDiffCoeff * averageWeightDiff);//compatablilty formula
        return (compatibilityThreshold > compatibility);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //add a player to the species
    public void addToSpecies(Player p) {
        players.add(p);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns the number of excess and disjoint genes between the 2 input genomes
    //i.e. returns the number of genes which dont match
    public float getExcessDisjoint(Genome brain1, Genome brain2) {
        float matching = 0.0f;
        for (int i = 0; i < brain1.getGenes().size(); i++) {
            for (int j = 0; j < brain2.getGenes().size(); j++) {
                if (brain1.getGenes().get(i).getInnovationNo() == brain2.getGenes().get(j).getInnovationNo()) {
                    matching++;
                    break;
                }
            }
        }
        return (brain1.getGenes().size() + brain2.getGenes().size() - 2 * (matching));//return no of excess and disjoint genes
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns the avereage weight difference between matching genes in the input genomes
    public float averageWeightDiff(Genome brain1, Genome brain2) {
        if (brain1.getGenes().size() == 0 || brain2.getGenes().size() == 0) {
            return 0;
        }


        float matching = 0;
        float totalDiff = 0;
        for (int i = 0; i < brain1.getGenes().size(); i++) {
            for (int j = 0; j < brain2.getGenes().size(); j++) {
                if (brain1.getGenes().get(i).getInnovationNo() == brain2.getGenes().get(j).getInnovationNo()) {
                    matching++;
                    totalDiff += abs(brain1.getGenes().get(i).getWeight() - brain2.getGenes().get(j).getWeight());
                    break;
                }
            }
        }
        if (matching == 0) {//divide by 0 error
            return 100;
        }
        return totalDiff / matching;
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //sorts the species by fitness
    public void sortSpecies() {

        ArrayList<Player> temp = new ArrayList<Player>();

        //selection short
        for (int i = 0; i < players.size(); i++) {
            float max = 0;
            int maxIndex = 0;
            for (int j = 0; j < players.size(); j++) {
                if (players.get(j).fitness > max) {
                    max = players.get(j).fitness;
                    maxIndex = j;
                }
            }
            temp.add(players.get(maxIndex));
            players.remove(maxIndex);
            i--;
        }

        players = (ArrayList) temp.clone();
        if (players.size() == 0) {
            log.debug("fucking wot m8");
            staleness = 200;
            return;
        }
        //if new best player
        if (players.get(0).fitness > bestFitness) {
            staleness = 0;
            bestFitness = players.get(0).fitness;
            rep = CLONER.deepClone(players.get(0).brain);
            champ = players.get(0).cloneForReplay();
        } else {//if no new best player
            staleness++;
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //simple stuff
    public void setAverage() {

        float sum = 0;
        for (int i = 0; i < players.size(); i++) {
            sum += players.get(i).fitness;
        }
        averageFitness = sum / players.size();
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    //gets baby from the players in this species
    public Player giveMeBaby(List<ConnectionHistory> innovationHistory) {
        Player baby;
        if (nextFloat(0, 1f) < 0.25) {//25% of the time there is no crossover and the child is simply a clone of a random(ish) player
            baby = selectPlayer().clone();
        } else {//75% of the time do crossover

            //get 2 random(ish) parents
            Player parent1 = selectPlayer();
            Player parent2 = selectPlayer();

            //the crossover function expects the highest fitness parent to be the object and the lowest as the argument
            if (parent1.fitness < parent2.fitness) {
                baby = parent2.crossover(parent1);
            } else {
                baby = parent1.crossover(parent2);
            }
        }
        baby.brain.mutate(innovationHistory);//mutate that baby brain
        return baby;
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //selects a player based on it fitness
    public Player selectPlayer() {
        float fitnessSum = 0;
        for (Player player : players) {
            fitnessSum += player.fitness;
        }

        float rand = nextFloat(0, fitnessSum);
        float runningSum = 0;

        for (Player player : players) {
            runningSum += player.fitness;
            if (runningSum > rand) {
                return player;
            }
        }
        //unreachable code to make the parser happy
        return players.get(0);
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //kills off bottom half of the species
    public void cull() {
        if (players.size() > 2) {
            for (int i = players.size() / 2; i < players.size(); i++) {
                players.remove(i);
                i--;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //in order to protect unique players, the fitnesses of each player is divided by the number of players in the species that that player belongs to
    public void fitnessSharing() {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).fitness /= players.size();
        }
    }
}