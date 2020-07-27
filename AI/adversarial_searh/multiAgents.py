# multiAgents.py
# --------------
# Licensing Information: Please do not distribute or publish solutions to this
# project. You are free to use and extend these projects for educational
# purposes. The Pacman AI projects were developed at UC Berkeley, primarily by
# John DeNero (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# For more info, see http://inst.eecs.berkeley.edu/~cs188/sp09/pacman.html

from util import manhattanDistance
from game import Directions
import random, util
import sys
from game import Agent
from searchAgents import mazeDistance


debug_print = False
class ReflexAgent(Agent):
  """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
  """


  def getAction(self, gameState):
    """
    You do not need to change this method, but you're welcome to.

    getAction chooses among the best options according to the evaluation function.

    Just like in the previous project, getAction takes a GameState and returns
    some Directions.X for some X in the set {North, South, West, East, Stop}
    """
    # Collect legal moves and successor states
    legalMoves = gameState.getLegalActions()
    # Choose one of the best actions
    scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
    bestScore = max(scores)
    bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
    chosenIndex = random.choice(bestIndices) # Pick randomly among the best

    "Add more of your code here if you want to"
    return legalMoves[chosenIndex]

  def evaluationFunction(self, currentGameState, action):
    """
    Design a better evaluation function here.

    The evaluation function takes in the current and proposed successor
    GameStates (pacman.py) and returns a number, where higher numbers are better.

    The code below extracts some useful information from the state, like the
    remaining food (newFood) and Pacman position after moving (newPos).
    newScaredTimes holds the number of moves that each ghost will remain
    scared because of Pacman having eaten a power pellet.

    Print out these variables to see what you're getting, then combine them
    to create a masterful evaluation function.
    """
    # Useful information you can extract from a GameState (pacman.py)
    successorGameState = currentGameState.generatePacmanSuccessor(action)
    curr_food_list = currentGameState.getFood().asList()
    newPos = successorGameState.getPacmanPosition()
    newFood = successorGameState.getFood()
    score = successorGameState.getScore()
    "*** YOUR CODE HERE ***"
    food_list = newFood.asList()
    ghost_positions = successorGameState.getGhostPositions()
    newGhostStates = successorGameState.getGhostStates()
    newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]
    scared = False
    dist_to_ghosts = 0
    for i in range(0, len(newScaredTimes)):
        dist = util.manhattanDistance(newPos, ghost_positions[i])
        dist_to_ghosts += dist  
        if newScaredTimes[i] > 3:
                scared = True
                continue
        if dist < 3:
                return -1 * sys.maxint - 1
    
    if len(food_list) == 0:
        return sys.maxint
    for food in curr_food_list:
        dist = util.manhattanDistance(newPos, food)
        if dist == 0:
                return sys.maxint
        score += 10000/dist 
    score = score * 10000/len(newFood.asList())
    if not scared:
        score = score / (1000 / dist_to_ghosts)
    return score

def scoreEvaluationFunction(currentGameState):
  """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
  """
  return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
  """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
  """

  def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
    self.index = 0 # Pacman is always agent index 0
    self.evaluationFunction = util.lookup(evalFn, globals())
    self.depth = int(depth)


class MinimaxAgent(MultiAgentSearchAgent):
  """
    Your minimax agent (question 2)
  """
  def max_value(self, gameState, depth):
        if depth > self.depth:
                return [None, self.evaluationFunction(gameState)]
        v = -1 * sys.maxint - 1
        ghost_index = gameState.getNumAgents()
        pac_act = gameState.getLegalActions(0)
        if "Stop" in pac_act:
                pac_act.remove("Stop")
        best_action = None 
        for k in range(0, len(pac_act)):
                action = pac_act[k]
                successor_state = gameState.generateSuccessor(0, action)
                new_val = self.min_value(successor_state, depth, 1)
                if new_val > v:
                        v = new_val
                        best_action = action
        return tuple([best_action, self.evaluationFunction(gameState)])

  def min_value(self, gameState, depth, ghost_num):
        v = sys.maxint
        successor_state = gameState
        ghost_index = gameState.getNumAgents()
        for i in range(ghost_num, ghost_index):
                actions = gameState.getLegalActions(i)
                for act in range(0, len(actions)):
                        successor_state = gameState.generateSuccessor(i, actions[act])
                        new_score = self.min_value(successor_state, depth, i + 1)
                        if new_score < v:
                                v = new_score

        if depth >= self.depth:
                return v
        else:
                return self.max_value(successor_state, depth + 1)[1]


  def getAction(self, gameState):
    """
      Returns the minimax action from the current gameState using self.depth
      and self.evaluationFunction.

      Here are some method calls that might be useful when implementing minimax.

      gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

      Directions.STOP:
        The stop direction, which is always legal

      gameState.generateSuccessor(agentIndex, action):
        Returns the successor game state after an agent takes an action

      gameState.getNumAgents():
        Returns the total number of agents in the game
    """
    "*** YOUR CODE HERE ***"
    return self.max_value(gameState, 1)[0]
        



class AlphaBetaAgent(MultiAgentSearchAgent):
  """
    Your minimax agent with alpha-beta pruning (question 3)
  """

  def maxVal(self, gameState, depth, alpha, beta, agent):
    #----------------------#
    #      Debugging       #
    #----------------------#
    stop_me = False
    if (debug_print):
      print "  " * depth,"***********************"
      print "  " * depth,"   DEPTH: ", depth
      print "  " * depth,"***********************"
      if (stop_me):
        raw_input("  " * depth + "Start Turn: " + str(depth))

    #----------------------#
    #     Depth Check      #
    #----------------------#
    if depth > self.depth:
      return [None, self.evaluationFunction(gameState)]

    #----------------------#
    #      Variables       #
    #----------------------#
    pacman      = 0
    v           = [None, -1 * sys.maxint]
    moves       = gameState.getLegalActions(pacman)
    excludeStop = True

    #Speed increase option
    if excludeStop:
      if "Stop" in moves:
        moves.remove("Stop")

    if (debug_print):
      print "  " * depth, "*******************"
      print "  " * depth, "MAX: ", moves, " @ ", depth
      print "  " * depth, "*******************"  

    #----------------------#
    #      Main Body       #
    #----------------------#
    #Loop through all available moves
    for move in moves:
      if (debug_print):
        print "  " * depth,"MAX EVALUATING MOVE: ", move, " FOR AGENT: ", agent, " @ ", depth
      #Grab successor of pacman move
      successor = gameState.generateSuccessor(pacman, move)
      #Evaluate the tree for the current move @ depth with alpha, beta
      currMove = self.minVal(successor, depth, alpha, beta, agent + 1) 
      if (debug_print):
        print "  " * depth,"*******************"
        print "  " * depth,"Max Move Loop: ", move, " --> ", currMove
        print "  " * depth,"*******************"  
      #Does the current move evaluate to a better outcome?
      if (currMove[1] >= v[1]):
        if (debug_print):
          print "  " * depth,"!!!!!!!!!!!!!!!!!"
          print "  " * depth,"MAX UPDATE: ", move, currMove[1], " > ", v, " @ ", depth
          print "  " * depth,"!!!!!!!!!!!!!!!!!"
        #Update to better outcome
        v[0] = move
        v[1] = currMove[1]
      #----------------------#
      #      Pruning         #
      #----------------------#
      #Update alpha
      alpha = max(alpha, v[1])
      #Check for pruning condition
      if (alpha >= beta):
        if (debug_print):
          print "  " * depth,"--MAX PRUNING--"
        return v

    return v

  def minVal(self, gameState, depth, alpha, beta, agent):
    #----------------------#
    #      Variables       #
    #----------------------#
    v         = [None, sys.maxint]
    maxAgents = gameState.getNumAgents()
    bestState = gameState
    #----------------------#
    #      Main Body       #
    #----------------------#

    #Check if we've reached depth
    if depth > self.depth:
      return [None, self.evaluationFunction(gameState)]
    #Check if it's pacman's turn again
    if (agent > maxAgents -1):
      return self.maxVal(gameState, depth + 1, alpha, beta, 0)

    #Loop to evaluate all agent moves starting with current agent
    for i in range (agent, maxAgents):
      #Get legal moves for Inky, Pinky, Blinky, or Clyde
      moves = gameState.getLegalActions(i)
      if (debug_print):
        print "  " * depth,"*******************"
        print "  " * depth,"MIN: ", moves, " @ ", depth
        print "  " * depth,"*******************"
      #Loop through legal moves for current agent
      for move in moves:
        if (debug_print):
          print "  " * depth,"MIN EVALUATING MOVE: ", move, " FOR AGENT: ", agent, " @ ", depth
        #Determine successor state based off current possible move for agent {i}  
        successor = gameState.generateSuccessor(i, move)
        #Evaluate next agent's move for this successor state, depth, alpha, beta
        currMove = self.minVal(successor, depth, alpha, beta, i + 1)
        if (debug_print):
          print "  " * depth,"*******************"
          print "  " * depth,"Min Move Loop: ", move, " --> ", currMove
          print "  " * depth,"*******************"  
        #Check of Pacman's utility is reduced using this move
        if (currMove[1] <= v[1]):
          if (debug_print):
            print "  " * depth,"!!!!!!!!!!!!!!!!!"
            print "  " * depth,"MIN UPDATE: ", move, currMove[1], " < ", v, " @ ", depth
            print "  " * depth,"!!!!!!!!!!!!!!!!!"
          #Update agent's best move
          v[0]      = move
          v[1]      = currMove[1]
          bestState = successor
        #----------------------#
        #      Pruning         #
        #----------------------#
        #Update beta
        beta = min(beta, v[1])
        #Check for pruning condition
        if (alpha >= beta):
          if (debug_print):
            print "  " * depth,"--MIN PRUNING--"
          return v

    #Passed loop
    #Return bad move with score
    return [v[0], self.evaluationFunction(bestState)]

  def getAction(self, gameState):
    """
      Returns the minimax action using self.depth and self.evaluationFunction
    """
    #**********************#
    #*** YOUR CODE HERE ***#
    #**********************#
    #** AlphaBeta Pseudo **#
    #**********************#
    #** ---------------------------------------------------- **#
    #** def max-value(state, alpha, beta):                   **#
    #**  initialize v = -inf                                 **#
    #**  for each successor of state:                        **#
    #**    v = max(v, min-value(successor, alpha, beta))     **#
    #**    alpha = max (alpha, v)                            **#
    #**    if (alpha >= beta)                                **#
    #**      return v                                        **#
    #**  return v                                            **#
    #** ---------------------------------------------------- **#
    #** def min-value(state, alpha, beta):                   **#
    #**  initialize v = +inf                                 **#
    #**  for each successor of state:                        **#
    #**    v = min(v, max-value(successor, alpha, beta))     **#
    #**    beta = min (beta, v)                              **#
    #**    if (alpha >= beta)                                **#
    #**      return v                                        **#
    #**  return v                                            **#
    #** ---------------------------------------------------- **#
    alpha = -1 * sys.maxint
    beta  = +1 * sys.maxint

    action = self.maxVal(gameState, 1, alpha, beta, 0)
    if (debug_print):
      print "********************"
      print " AlphaBeta Complete "
      print "********************"
      print " Optimal Move: ", action
      print "********************"    
    return action[0]

class ExpectimaxAgent(MultiAgentSearchAgent):
  """
    Your expectimax agent (question 4)
  """

  def max_value(self, gameState, depth):
        better = "evalFn=better" in sys.argv
        if depth > self.depth:
                score = self.evaluationFunction(gameState)
                if better:
                        score = betterEvaluationFunction(gameState) 
                return [None, score]
        v = -1 * sys.maxint - 1
        ghost_index = gameState.getNumAgents()
        pac_act = gameState.getLegalActions(0)
        best_action = None 
        for k in range(0, len(pac_act)):
                action = pac_act[k]
                successor_state = gameState.generateSuccessor(0, action)
                new_val = self.min_value(successor_state, depth, 1)
                if new_val > v:
                        v = new_val
                        best_action = action
        score = self.evaluationFunction(gameState)
        if better:
                score = betterEvaluationFunction(gameState)     
        return tuple([best_action, score])

  def min_value(self, gameState, depth, ghost_num):
        v = sys.maxint
        successor_state = gameState
        ghost_index = gameState.getNumAgents()
        for i in range(ghost_num, ghost_index):
                actions = successor_state.getLegalActions(i)
                new_score = 0
                for action in actions:
                        new_score += self.min_value(successor_state.generateSuccessor(i, action), depth, i + 1) * 1/len(actions)
                if new_score < v:
                        v = new_score           
                        
        if depth >= self.depth:
                return v
        else:
                return self.max_value(successor_state, depth + 1)[1]


  def getAction(self, gameState):
    """
      Returns the expectimax action using self.depth and self.evaluationFunction

      All ghosts should be modeled as choosing uniformly at random from their
      legal moves.
    """
    "*** YOUR CODE HERE ***"
    action = self.max_value(gameState, 1)[0]
    return action



def betterEvaluationFunction(currentGameState):
  """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: <write something here so we know what you did>
  """
  "*** YOUR CODE HERE ***"
  pac_position = currentGameState.getPacmanPosition()
  food_list = currentGameState.getFood().asList()
  ghost_positions = currentGameState.getGhostPositions()

  scared_times = [ghost_state.scaredTimer for ghost_state in currentGameState.getGhostStates()]

  bonus = 0
  for i in range(0, len(ghost_positions)):
        gp_int = tuple([int(ghost_positions[i][0]), int(ghost_positions[i][1])])
        dist = mazeDistance(pac_position, gp_int, currentGameState)
        if scared_times[i] > 1:
                bonus += scared_times[i] * 10
                continue
        if dist < 2:
                return -1 * sys.maxint - 1

  if len(food_list) == 0:
        return sys.maxint

  dist = sys.maxint
  if len(food_list) > 0:
        dist = mazeDistance(pac_position, food_list[0], currentGameState)

 
  score = (scoreEvaluationFunction(currentGameState) +
                2000/(len(food_list) + 1) -
                dist) + bonus
                
 
  return score

# Abbreviation
better = betterEvaluationFunction

def contestEvaluationFunction(currentGameState):
    """
      Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
      evaluation function (question 5).

      DESCRIPTION: <write something here so we know what you did>
    """
    "*** YOUR CODE HERE ***"
    
    pac_position = currentGameState.getPacmanPosition()
    food_list = currentGameState.getFood().asList()
    ghost_positions = currentGameState.getGhostPositions()
    capsules = currentGameState.getCapsules()
    scared_times = [ghost_state.scaredTimer for ghost_state in currentGameState.getGhostStates()]
    if len(food_list) == 0:
        return sys.maxint 
    closest_dist = sys.maxint
    closest_food = None
    total_dist = 0
    for food in food_list:
        dist = manhattanDistance(food, pac_position)
        total_dist += dist
        if dist < closest_dist:
                closest_dist = dist
                closest_food = food

    closest_dist = mazeDistance(closest_food, pac_position, currentGameState)
    score = 0
    #score = 5000.0/float(total_dist) - 50.0 * float(closest_dist)
    hunt = False
    for scared in scared_times:
	if scared < 2:
		hunt = True
		break

    closest_c = None
    if hunt:	
	    cd = sys.maxint
	    for capsule in capsules:
        	dist_c = manhattanDistance(capsule, pac_position)
       		 #cd += dist
		if dist_c < cd:
			cd = dist_c
			closest_c = capsule
    	    if closest_c is not None:
			dist_c = mazeDistance(pac_position, closest_c, currentGameState)
			    #score += 10.0/(cd)

    #closest_ghost = 100
    #closest_gd = sys.maxint
    for i in range(0, len(ghost_positions)):
        gp_int = tuple([int(ghost_positions[i][0]), int(ghost_positions[i][1])])
        dist = manhattanDistance(gp_int, pac_position)
     #   	if dist < closest_dist:
                	#closest_gd = dist
      #          	closest_ghost = i
       	if dist < 3 and scared_times[i] < 1:
		#print gp_int, scared_times[i]
                maze_dist = mazeDistance(pac_position, gp_int, currentGameState)
               	if maze_dist < 2:
                       	return -1 * sys.maxint  

    if closest_c is not None:
	return 10.0/(dist_c + 1) + 100.0/(len(capsules) + 1)
	
    score =  6800.0/float(len(food_list) + 1)  + 10.0/float(closest_dist)
    return score


class ContestAgent(MultiAgentSearchAgent):
  """
    Your agent for the mini-contest
  """

  def getAction(self, gameState):
    """
      Returns an action.  You can use any method you want and search to any depth you want.
      Just remember that the mini-contest is timed, so you have to trade off speed and computation.

      Ghosts don't behave randomly anymore, but they aren't perfect either -- they'll usually
      just make a beeline straight towards Pacman (or away from him if they're scared!)
    """
    "*** YOUR CODE HERE ***"
    mni = MinimaxAgent()
    mni.depth = 2 
    mni.evaluationFunction = contestEvaluationFunction
    action = mni.getAction(gameState)

    return action 

