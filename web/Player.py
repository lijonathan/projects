from decimal import Decimal
from fractions import Fraction
class Player:
    
    def __init__(self, name, hits, bats, runs):
        self.name = name
        self.hits = hits
        self.bats = bats
        self.runs = runs
       # self.battingavg = battingavg(self)
        
    def totalHits(self, hits):
        self.hits +=hits
        
    def totalbats(self, bats):
        self.bats += bats
    
    def totalruns(self, runs):
        self.runs +=runs
    
    def battingavg(self):
        print("Calculating avg")
        avg = self.hits/self.bats
        
        return avg
    
    def printplayer(self):
        print(Player.name + ": ")
        #printf(Player.battingavg)
        print(format(battingavg(),'.3f \n'))
        
        
    
    
            
