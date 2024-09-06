import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MaintenanceSchedulerGA {

    // Power unit capacities
    static int[] powerUnits = {20, 15, 35, 40, 15, 15, 10};

    // System capacity and maximum loads per interval
    static int totalCapacity = 150;
    static int[] maxLoadIntervals = {80, 90, 65, 70};

    // Gene pool: Represents different maintenance schedules (1: under maintenance, 0: active)
    static int[][][] powerUnitsGenePool = {
        {{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 1, 1}}, // Power unit 1
        {{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 1, 1}}, // Power unit 2
        {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}, // Power unit 3
        {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}, // Power unit 4
        {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}, // Power unit 5
        {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}, // Power unit 6
        {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}}  // Power unit 7
    };

    // Random object
    static Random rand = new Random();

    // Generate a chromosome (random gene selection from the pool)
    public static List<int[]> generateChromosome() {
        List<int[]> chromosome = new ArrayList<>();
        for (int[][] genePool : powerUnitsGenePool) {
            chromosome.add(genePool[rand.nextInt(genePool.length)]);
        }
        return chromosome;
    }

    // Fitness function to evaluate a chromosome
    public static int fitness(List<int[]> chromosome) {
        List<Integer> netReserves = new ArrayList<>();

        // Loop through each interval (4 intervals total)
        for (int i = 0; i < 4; i++) {
            int maintenanceCapacity = 0;

            // Calculate the total maintenance capacity for this interval
            for (int j = 0; j < chromosome.size(); j++) {
                maintenanceCapacity += powerUnits[j] * chromosome.get(j)[i];
            }

            int availableCapacity = totalCapacity - maintenanceCapacity;
            int netReserve = availableCapacity - maxLoadIntervals[i];
            netReserves.add(netReserve);

            // If any net reserve is negative, it's an illegal schedule
            if (netReserve < 0) {
                return 0;
            }
        }

        // The fitness is the minimum net reserve (higher is better)
        return Collections.min(netReserves);
    }

    // Selection (tournament selection)
    public static List<int[]> selection(List<List<int[]>> population) {
        List<List<int[]>> tournament = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tournament.add(population.get(rand.nextInt(population.size())));
        }
        return Collections.max(tournament, Comparator.comparingInt(MaintenanceSchedulerGA::fitness));
    }

    // Crossover operation (gene-constrained crossover)
    public static List<int[]> crossover(List<int[]> parent1, List<int[]> parent2) {
        List<int[]> child = new ArrayList<>();
        for (int i = 0; i < parent1.size(); i++) {
            int[][] genePool = powerUnitsGenePool[i];
            int[] gene = (rand.nextBoolean() ? parent1.get(i) : parent2.get(i));
            if (!isValidGene(gene, genePool)) {
                gene = genePool[rand.nextInt(genePool.length)];
            }
            child.add(gene);
        }
        return child;
    }

    // Mutation operation (gene-constrained mutation)
    public static void mutate(List<int[]> chromosome) {
        int unit = rand.nextInt(chromosome.size());
        chromosome.set(unit, powerUnitsGenePool[unit][rand.nextInt(powerUnitsGenePool[unit].length)]);
    }

    // Check if a gene is valid by matching it against the gene pool
    public static boolean isValidGene(int[] gene, int[][] genePool) {
        for (int[] validGene : genePool) {
            if (java.util.Arrays.equals(gene, validGene)) {
                return true;
            }
        }
        return false;
    }

    // Genetic algorithm
    public static List<int[]> geneticAlgorithm(int popSize, int generations) {
        // Initialize population
        List<List<int[]>> population = new ArrayList<>();
        for (int i = 0; i < popSize; i++) {
            population.add(generateChromosome());
        }

        // Evaluate initial population fitness
        for (int i = 0; i < generations; i++) {
            List<List<int[]>> newPopulation = new ArrayList<>();

            // Generate new population through selection, crossover, and mutation
            while (newPopulation.size() < popSize) {
                List<int[]> parent1 = selection(population);
                List<int[]> parent2 = selection(population);

                // Perform crossover
                List<int[]> child1 = crossover(parent1, parent2);
                List<int[]> child2 = crossover(parent1, parent2);

                // Mutate children
                mutate(child1);
                mutate(child2);

                newPopulation.add(child1);
                newPopulation.add(child2);
            }

            // Sort by fitness and replace old population
            population = newPopulation;
            population.sort(Comparator.comparingInt(MaintenanceSchedulerGA::fitness).reversed());

            // Print best chromosome of this generation
            System.out.println("Generation " + (i + 1) + ": Best fitness = " + fitness(population.get(0)));
        }

        // Return the best chromosome
        return population.get(0);
    }

    public static void main(String[] args) {
        // Parameters: population size and number of generations
        List<int[]> bestSolution = geneticAlgorithm(20, 100);

        System.out.println("Best solution found:");
        for (int[] gene : bestSolution) {
            System.out.println(java.util.Arrays.toString(gene));
        }

        System.out.println("Best fitness: " + fitness(bestSolution));
    }
}
