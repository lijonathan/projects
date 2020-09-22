import sys, os, re, operator
#from Player import Player
from decimal import Decimal
from fractions import Fraction

class Player:
    
    def __init__(self, name, hits, bats, runs):
        self.name = name
        self.hits = hits
        self.bats = bats
        self.runs = runs

        
    def totalHits(self, hits):
        self.hits +=hits
        
    def totalbats(self, bats):
        self.bats += bats
    
    def totalruns(self, runs):
        self.runs +=runs
    
    def battingavg(self):

        avg = float (self.hits)/ float (self.bats)
        avg = (format(avg, '.3f'))
        return avg
    def printplayer(self):
        avg = self.battingavg()
        print(self.name + ": " + avg + '\n')

            

def main():
	if len(sys.argv) < 2:
		sys.exit("Usage: %s filename" % sys.argv[0])
	
	filename = sys.argv[1]
	
	if not os.path.exists(filename):
		sys.exit("Error: File '%s' not found" % sys.argv[1])

	f = open(filename)
	baseballstat_regex = re.compile(r"\w+\s\w+\sbatted\s\d\stimes\swith\s\d\shits\sand\s\d\sruns")
	
	
	
	playerlist = []

	for line in f:
		
	
		linematch = baseballstat_regex.match(line)
		
		if linematch is not None:
			
			inputline = line.rstrip()
			matchedlinearray = []
			matchedlinearray = inputline.split(" ")
			name = matchedlinearray[0] + " " + matchedlinearray[1]
			hits = int (matchedlinearray[6])
		
		
			
			bats = int (matchedlinearray[3])

			runs = int (matchedlinearray[9])
		
			inputPlayer = Player(name, hits , bats , runs)
			
			inlist = False
		
			for p in playerlist:
				if p.name == inputPlayer.name:
					inlist = True
					p.bats += inputPlayer.bats
					p.hits += inputPlayer.hits
					p.runs += inputPlayer.runs
			if inlist == False:
				playerlist.insert( len(playerlist),inputPlayer)
			
		
	f.close()
	playerlist = sorted(playerlist, key = lambda Player: Player.battingavg(), reverse = True)
	for i in playerlist:
		i.printplayer()
		
	

if __name__ == "__main__":
	main()