package com.notorious.ddl.game;

import lombok.extern.slf4j.Slf4j;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

import static com.notorious.ddl.game.Globals.showNothing;
import static org.apache.commons.math3.util.FastMath.floor;

@Slf4j
public class Population {
    List<Player> pop = new ArrayList<>();
    Player bestPlayer;//the best ever player
    int bestScore = 0;//the score of the best ever player
    int gen;
    List<ConnectionHistory> innovationHistory = new ArrayList<>();
    List<Player> genPlayers = new ArrayList<>();
    List<Species> species = new ArrayList<>();

    boolean massExtinctionEvent = false;
    boolean newStage = false;
    int populationLife = 0;


    //------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public Population(PApplet applet, int size) {

        for (int i = 0; i < size; i++) {
            pop.add(new Player(applet));
            pop.get(i).getBrain().generateNetwork();
            pop.get(i).getBrain().mutate(innovationHistory);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //update all the players which are alive
    public void updateAlive(PGraphics g) {
        populationLife++;
        for (int i = 0; i < pop.size(); i++) {
            if (!pop.get(i).isDead()) {
                pop.get(i).look();//get inputs for brain
                pop.get(i).think();//use outputs from neural network
                pop.get(i).update();//move the player according to the outputs from the neural network
                if (!showNothing) {
                    pop.get(i).show(g);
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //returns true if all the players are dead      sad
    public boolean done() {
        for (int i = 0; i < pop.size(); i++) {
            if (!pop.get(i).isDead()) {
                return false;
            }
        }
        return true;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //sets the best player globally and for this gen
    public void setBestPlayer() {
        Player tempBest = species.get(0).getPlayers().get(0);
        tempBest.setGen(gen);


        //if best this gen is better than the global best score then set the global best as the best this gen

        if (tempBest.getScore() > bestScore) {
            genPlayers.add(tempBest.cloneForReplay());
            log.debug("old best:", bestScore);
            log.debug("new best:", tempBest.getScore());
            bestScore = tempBest.getScore();
            bestPlayer = tempBest.cloneForReplay();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------
    //this function is called when all the players in the population are dead and a new generation needs to be made
    void naturalSelection() throws CloneNotSupportedException {
        speciate();//seperate the population into species
        calculateFitness();//calculate the fitness of each player
        sortSpecies();//sort the species to be ranked in fitness order, best first
        if (massExtinctionEvent) {
            massExtinction();
            massExtinctionEvent = false;
        }
        cullSpecies();//kill off the bottom half of each species
        setBestPlayer();//save the best player of this gen
        killStaleSpecies();//remove species which haven't improved in the last 15(ish) generations
        killBadSpecies();//kill species which are so bad that they cant reproduce

        log.debug("Generation: {} | Number of Mutations: {} | Species: {} <<<<<<<<<<<<<<<<<<<<<<<<",
                gen, innovationHistory.size(), species.size());


        float averageSum = getAvgFitnessSum();
        ArrayList<Player> children = new ArrayList<Player>();//the next generation
        log.debug("Species:");
        for (int j = 0; j < species.size(); j++) {//for each species

            log.debug("best unadjusted fitness: {}", species.get(j).getBestFitness());
            for (int i = 0; i < species.get(j).getPlayers().size(); i++) {
                log.debug("Player: {} | Fitness: {} | Score: {} <<<<<<<<<<<<<<<<<<<<<<<<",
                        i, species.get(j).getPlayers().get(i).getFitness(),
                        species.get(j).getPlayers().get(i).getScore());
            }
            children.add(species.get(j).getChamp().clone());//add champion without any mutation

            int NoOfChildren = (int)floor(species.get(j).getAverageFitness() / averageSum * pop.size()) - 1;//the number of children this species is allowed, note -1 is because the champ is already added
            for (int i = 0; i < NoOfChildren; i++) {//get the calculated amount of children from this species
                children.add(species.get(j).giveMeBaby(innovationHistory));
            }
        }

        while (children.size() < pop.size()) {//if not enough babies (due to flooring the number of children to get a whole int)
            children.add(species.get(0).giveMeBaby(innovationHistory));//get babies from the best species
        }
        pop.clear();
        pop = (ArrayList) children.clone(); //set the children as the current population
        gen += 1;
        for (int i = 0; i < pop.size(); i++) {//generate networks for each of the children
            pop.get(i).getBrain().generateNetwork();
        }

        populationLife = 0;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //seperate population into species based on how similar they are to the leaders of each species in the previous gen
    void speciate() {
        for (Species s : species) {//empty species
            s.getPlayers().clear();
        }
        for (int i = 0; i < pop.size(); i++) {//for each player
            boolean speciesFound = false;
            for (Species s : species) {//for each species
                if (s.sameSpecies(pop.get(i).getBrain())) {//if the player is similar enough to be considered in the same species
                    s.addToSpecies(pop.get(i));//add it to the species
                    speciesFound = true;
                    break;
                }
            }
            if (!speciesFound) {//if no species was similar enough then add a new species with this as its champion
                species.add(new Species(pop.get(i)));
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //calculates the fitness of all of the players
    void calculateFitness() {
        for (int i = 1; i < pop.size(); i++) {
            pop.get(i).calculateFitness();
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //sorts the players within a species and the species by their fitnesses
    void sortSpecies() {
        //sort the players within a species
        for (Species s : species) {
            s.sortSpecies();
        }

        //sort the species by the fitness of its best player
        //using selection sort like a loser
        ArrayList<Species> temp = new ArrayList<>();
        for (int i = 0; i < species.size(); i++) {
            float max = 0;
            int maxIndex = 0;
            for (int j = 0; j < species.size(); j++) {
                if (species.get(j).getBestFitness() > max) {
                    max = species.get(j).getBestFitness();
                    maxIndex = j;
                }
            }
            temp.add(species.get(maxIndex));
            species.remove(maxIndex);
            i--;
        }
        species = (ArrayList) temp.clone();
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //kills all species which haven't improved in 15 generations
    void killStaleSpecies() {
        for (int i = 2; i < species.size(); i++) {
            if (species.get(i).getStaleness() >= 15) {
                species.remove(i);
                i--;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //if a species sucks so much that it wont even be allocated 1 child for the next generation then kill it now
    void killBadSpecies() {
        float averageSum = getAvgFitnessSum();

        for (int i = 1; i < species.size(); i++) {
            if (species.get(i).getAverageFitness() / averageSum * pop.size() < 1) {//if wont be given a single child
                species.remove(i);//sad
                i--;
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //returns the sum of each species average fitness
    float getAvgFitnessSum() {
        float averageSum = 0;
        for (Species s : species) {
            averageSum += s.getAverageFitness();
        }
        return averageSum;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //kill the bottom half of each species
    void cullSpecies() {
        for (Species s : species) {
            s.cull(); //kill bottom half
            s.fitnessSharing();//also while we're at it lets do fitness sharing
            s.setAverage();//reset averages because they will have changed
        }
    }


    void massExtinction() {
        for (int i = 5; i < species.size(); i++) {
            species.remove(i);//sad
            i--;
        }
    }
}