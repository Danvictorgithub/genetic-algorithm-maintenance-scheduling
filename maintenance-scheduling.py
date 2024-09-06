import random

# Power unit capacities
power_units = [20, 15, 35, 40, 15, 15, 10]

# System capacity and maximum loads per interval
total_capacity = 150
max_load_intervals = [80, 90, 65, 70]

# Gene pool: Represents different maintenance schedules (1: under maintenance, 0: active)
power_units_gene_pool = [
    [[1,1,0,0],[0,1,1,0],[0,0,1,1]],   # for power unit 1
    [[1,1,0,0],[0,1,1,0],[0,0,1,1]],   # for power unit 2
    [[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]],  # for power unit 3
    [[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]],  # for power unit 4
    [[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]],  # for power unit 5
    [[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]],  # for power unit 6
    [[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]]   # for power unit 7
]

# Generate a chromosome (random gene selection from the pool)
def generate_chromosome():
    return [random.choice(genes) for genes in power_units_gene_pool]

# Fitness function to evaluate a chromosome
def fitness(chromosome):
    net_reserves = []
    
    # Loop through each interval (4 intervals total)
    for i in range(4):
        # Calculate the total maintenance capacity for this interval
        maintenance_capacity = sum(power_units[j] * chromosome[j][i] for j in range(len(chromosome)))
        
        # Subtract from total capacity
        available_capacity = total_capacity - maintenance_capacity
        
        # Calculate net reserve by subtracting the max load for this interval
        net_reserve = available_capacity - max_load_intervals[i]
        net_reserves.append(net_reserve)
        
        # If any net reserve is negative, it's an illegal schedule
        if net_reserve < 0:
            return 0
    
    # The fitness is the minimum net reserve (higher is better)
    return min(net_reserves)

# Selection (tournament selection)
def selection(population):
    tournament = random.sample(population, 3)
    return max(tournament, key=lambda chromosome: chromosome['fitness'])

# Crossover operation (gene-constrained crossover)
def crossover(parent1, parent2):
    # Perform crossover by picking genes from parent1 or parent2 but ensure valid genes from the pool
    child1 = []
    child2 = []
    for i in range(len(parent1['chromosome'])):
        gene_pool = power_units_gene_pool[i]  # Use valid gene pool for power unit i
        if random.random() > 0.5:
            gene1 = parent1['chromosome'][i]
            gene2 = parent2['chromosome'][i]
        else:
            gene1 = parent2['chromosome'][i]
            gene2 = parent1['chromosome'][i]
        # Ensure that the genes are valid by checking against the gene pool
        child1.append(gene1 if gene1 in gene_pool else random.choice(gene_pool))
        child2.append(gene2 if gene2 in gene_pool else random.choice(gene_pool))
    return child1, child2

# Mutation operation (gene-constrained mutation)
def mutate(chromosome):
    # Mutate a single gene by replacing it with a valid gene from the gene pool
    unit = random.randint(0, len(chromosome) - 1)
    chromosome[unit] = random.choice(power_units_gene_pool[unit])

# Run genetic algorithm
def genetic_algorithm(pop_size, generations):
    # Initialize population with random chromosomes
    population = [{'chromosome': generate_chromosome(), 'fitness': 0} for _ in range(pop_size)]
    
    # Evaluate the initial population's fitness
    for individual in population:
        individual['fitness'] = fitness(individual['chromosome'])
    
    # Run the algorithm for the specified number of generations
    for gen in range(generations):
        new_population = []
        
        # Generate new population through selection, crossover, and mutation
        while len(new_population) < pop_size:
            parent1 = selection(population)
            parent2 = selection(population)
            
            # Perform crossover
            child1_chromosome, child2_chromosome = crossover(parent1, parent2)
            
            # Mutate children
            mutate(child1_chromosome)
            mutate(child2_chromosome)
            
            # Add new individuals to the population
            new_population.append({'chromosome': child1_chromosome, 'fitness': fitness(child1_chromosome)})
            new_population.append({'chromosome': child2_chromosome, 'fitness': fitness(child2_chromosome)})
        
        # Sort by fitness and replace old population
        population = sorted(new_population, key=lambda x: x['fitness'], reverse=True)[:pop_size]
        
        # Print best chromosome of this generation
        print(f"Generation {gen + 1}: Best fitness = {population[0]['fitness']}")
    
    # Return the best chromosome
    return max(population, key=lambda individual: individual['fitness'])

# Parameters: population size and number of generations
best_solution = genetic_algorithm(pop_size=20, generations=100)

print("Best solution found:", best_solution['chromosome'])
print("Best fitness:", best_solution['fitness'])
