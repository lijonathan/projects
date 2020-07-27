
/*Jonathan Li
 * jonathanli
 */
#include "cachelab.h"
#include <getopt.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>

struct lines{
	size_t tag, valid_bit, last_used_counter;
};

struct set{
	struct lines** the_lines; // wasted a lot of space
	unsigned no_lines;
	unsigned last_used_counter_generator;
};

struct cache{
	struct set** the_sets;
	unsigned no_sets, no_lines;
};

struct set* allocateSets(int associativity){
    struct set* return_sets = (struct set*)malloc(sizeof(struct set)); 
    return_sets -> the_lines = (struct lines**)malloc(sizeof(struct lines) * associativity);
    for (int i = 0; i < associativity; ++i){
        return_sets -> the_lines[i] = (struct lines*) malloc(sizeof(struct lines));
	return_sets -> the_lines[i] -> valid_bit = 0;
	return_sets -> the_lines[i] -> tag = 0;
	return_sets -> the_lines[i] -> last_used_counter = 0;   
    }
    return return_sets;
}

struct cache* allocateCache(int no_sets, int associativity){
	struct cache* Simulator = (struct cache*)malloc(sizeof(struct cache));
	Simulator -> the_sets = (struct set**)malloc(sizeof(struct set) * no_sets);
    	for (int j = 0; j < no_sets; ++j){
            Simulator -> the_sets[j] = allocateSets(associativity);
	    Simulator -> the_sets[j] -> last_used_counter_generator = 0; //start the count at 1 so the initial add will do something
   	 }
	return Simulator;
}

int cache_storage(struct cache* Simulator, unsigned long address, int* hits, int* misses, int* evictions, unsigned long tag, unsigned long set_index){
	int used_counter = Simulator -> the_sets[set_index] -> last_used_counter_generator;

	for(int j = 0; j < (Simulator -> no_lines); ++j){//check if cache contains the address
		if((Simulator -> the_sets[set_index] -> the_lines[j] -> tag == tag) && (Simulator -> the_sets[set_index] -> the_lines[j] -> valid_bit == 1)){
			*hits = *hits + 1;
			used_counter += 1;
			Simulator -> the_sets[set_index] -> the_lines[j] -> last_used_counter = used_counter;
			Simulator -> the_sets[set_index] -> last_used_counter_generator = used_counter;
			return 1;
		}
	}

	for(int k = 0; k < (Simulator -> no_lines); ++k){ //check if cache has empty spots to load
		if(Simulator -> the_sets[set_index] -> the_lines[k] -> valid_bit == 0){
			Simulator -> the_sets[set_index] -> the_lines[k] -> valid_bit = 1;
			Simulator -> the_sets[set_index] -> the_lines[k] -> tag = tag;
			used_counter += 1;
			Simulator -> the_sets[set_index] -> the_lines[k] -> last_used_counter = used_counter;
			Simulator -> the_sets[set_index] -> last_used_counter_generator = used_counter; 
			*misses = *misses + 1;
			return 2;
		}
	}

	int least_recently_used_line_index = 0; //if doesn't contain the address and no empty spots, evictions start
	int lowest_counter = 1 << 30;
	for (int i = 0; i < (Simulator -> no_lines); ++i){
		if (lowest_counter > Simulator -> the_sets[set_index] -> the_lines[i] -> last_used_counter){
			lowest_counter = Simulator -> the_sets[set_index] -> the_lines[i] -> last_used_counter;
			least_recently_used_line_index = i;
		}
	}
	Simulator -> the_sets[set_index] -> the_lines[least_recently_used_line_index] -> tag = tag;
	Simulator -> the_sets[set_index] -> the_lines[least_recently_used_line_index] -> valid_bit = 1;
	used_counter += 1;
	Simulator -> the_sets[set_index] -> the_lines[least_recently_used_line_index] -> last_used_counter = used_counter;
	Simulator -> the_sets[set_index] -> last_used_counter_generator = used_counter;
	
	*misses = *misses + 1;
	*evictions = *evictions + 1;
	return 0;

}


void address_calculation(struct cache* Simulator, char operation, char* char_address, int* hits, int* misses, int* evictions, int set_bits_size, int block_bits_size){
	unsigned long address, set_index, tag; //deleted valid_bit because it was not necesaary. Set in the struct
	unsigned long tag_bits_size = 64 - set_bits_size - block_bits_size;
        
        
	address = (unsigned long) strtol(char_address, NULL, 16);
	set_index = (address << (tag_bits_size)) >> (block_bits_size + tag_bits_size); 
	tag = address >> (block_bits_size + set_bits_size);
	if(operation == 'L' || operation == 'S'){
		cache_storage(Simulator, address, hits, misses, evictions, tag, set_index); 
	}
	if(operation == 'M'){ //Handle the case of modify - Store and load ( S, L)
		cache_storage(Simulator, address, hits, misses, evictions, tag, set_index);
		cache_storage(Simulator, address, hits, misses, evictions, tag, set_index);
	}
}

void parse_lines(char* line, struct cache* Simulator, int set_bits, int blocksize, int* hits, int* misses, int* evictions){ //Call everything from the parse_lines method
    char operation;
    char address[100];
    if(line[0] == ' '){
	operation = line[1];
	int counter = 2;
	int index = 0;
	while (line[counter] != ','){
	    address[index] = line[counter];
            counter++;
	    index++;
    	}
	address[index] = '\0';
	address_calculation(Simulator, operation, address, hits, misses, evictions, set_bits, blocksize);
    }
}

int main(int argc, char * argv[])
{
    int hits = 0, misses = 0, evictions = 0;
    int no_sets, set_bits, associativity, blockBits, c;
    char *fileName = (char*) malloc(100);
    while ((c = getopt(argc, argv, "hvs:E:b:t:")) != -1){
	switch(c)
	{
	case 's':
	    set_bits = atoi(optarg);
	    no_sets =  1 << atoi(optarg);
	    break;
	case 'E':
	    associativity = atoi(optarg);
	    break;
	case 'b':
	    blockBits = atoi(optarg);
	    break;
	case 't':
	    strncpy(fileName, optarg, 100); 
	    break;
	default:
	    break;
	}	
    }
    struct cache* Simulator = allocateCache(no_sets, associativity);
    Simulator -> no_sets = no_sets;
    Simulator -> no_lines = associativity;
    FILE *trace = fopen(fileName, "r");
    if (trace != NULL){
	char line[100];
	while (fgets(line, 100, trace)){
	    parse_lines(line, Simulator, set_bits, blockBits, &hits, &misses, &evictions); //everything is done here
	}
	fclose(trace);
    }
    else{
	printf("File could not be opened.");
    }
    free(fileName);
    for (int k = 0; k < no_sets; ++k){
	for (int j = 0; j < associativity; ++j){
		free(Simulator -> the_sets[k] -> the_lines[j]);
	}
	free(Simulator -> the_sets[k] -> the_lines);
	free (Simulator -> the_sets[k]);
    } 
    free(Simulator -> the_sets);
    free(Simulator);
    printSummary(hits, misses, evictions);
    return 0;
}
