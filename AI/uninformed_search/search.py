# search.py
# ---------
# Licensing Information: Please do not distribute or publish solutions to this
# project. You are free to use and extend these projects for educational
# purposes. The Pacman AI projects were developed at UC Berkeley, primarily by
# John DeNero (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# For more info, see http://inst.eecs.berkeley.edu/~cs188/sp09/pacman.html

"""
In search.py, you will implement generic search algorithms which are called
by Pacman agents (in searchAgents.py).
"""

import util

class SearchProblem:
    """
    This class outlines the structure of a search problem, but doesn't implement
    any of the methods (in object-oriented terminology: an abstract class).

    You do not need to change anything in this class, ever.
    """

    def getStartState(self):
        """
        Returns the start state for the search problem
        """
        util.raiseNotDefined()

    def isGoalState(self, state):
        """
          state: Search state

        Returns True if and only if the state is a valid goal state
        """
        util.raiseNotDefined()

    def getSuccessors(self, state):
        """
          state: Search state

        For a given state, this should return a list of triples,
        (successor, action, stepCost), where 'successor' is a
        successor to the current state, 'action' is the action
        required to get there, and 'stepCost' is the incremental
        cost of expanding to that successor
        """
        util.raiseNotDefined()

    def getCostOfActions(self, actions):
        """
         actions: A list of actions to take

        This method returns the total cost of a particular sequence of actions.  The sequence must
        be composed of legal moves
        """
        util.raiseNotDefined()


def tinyMazeSearch(problem):
    """
    Returns a sequence of moves that solves tinyMaze.  For any other
    maze, the sequence of moves will be incorrect, so only use this for tinyMaze
    """
    from game import Directions
    s = Directions.SOUTH
    w = Directions.WEST
    return  [s,s,w,s,w,w,s,w]

def nullHeuristic(state, problem=None):
    """
    A heuristic function estimates the cost from the current state to the nearest
    goal in the provided SearchProblem.  This heuristic is trivial.
    """
    return 0    

def pacmanSearch(problem, searchType="DFS", heuristic=nullHeuristic):
    #searchType priorities:
    #DFS - Farthest Depth
    #BFS - Lowest Depth
    #UCS - Min(Cost)
    #A*  - Min(Cost + Heuristic)
    #-----------------------#
    #--  Local Variables  --#
    #-----------------------#
    l_moves   = []
    l_visited = []
    l_expnd   = []
    l_pqueue  = util.PriorityQueue()
    #------------#
    #Start with tree with only start state
    l_start  = problem.getStartState()
    l_node   = l_start
    l_pri    = 0
    #if searchType == "DFS":
    #    l_pri = 1000000
    l_parent = (l_node, [], l_pri)
    #------------#
    #-----------------------#
    #--Debugging variables--#
    #-----------------------#
    d_moveNum  = 0
    d_maxMoves = 5
    d_debug = False
    #------------#
    l_visited.append(l_start)
    l_pqueue.push(l_parent, l_pri)
    #While queue is non-empty
    while (not problem.isGoalState(l_node)):
        #Unpacking Data
        l_ppos   = l_parent[0] #parent position
        l_moves  = l_parent[1] #parent path
        l_pri    = l_parent[2] #parent priority

        #add to visited nodes
        if l_ppos not in l_visited:
            l_visited.append(l_ppos)

        #make sure we don't expand twice
        if l_node not in l_expnd:
            for nodes in problem.getSuccessors(l_node):
                #check if not visited, or new value is cheaper
                l_expnd.append(l_node)
                l_orig = l_moves[:]
                l_loc  = nodes[0]
                l_step = nodes[1]
                if not nodes[0] in l_visited:
                    if d_debug:
                        print "NODES[0] : ", nodes[0]
                        print "l_visited[0] : ",l_visited
                    l_orig.append(l_step)
                    if (searchType == "DFS"):
                        l_delta = -1
                    elif (searchType == "BFS"):
                        l_delta =  1
                    elif (searchType == "UCS"):
                        l_delta = 0
                        l_pri   = problem.getCostOfActions(l_orig)
                    elif (searchType == "ASTAR"):
			#print "TES"
                        l_delta = 0
                        l_pri   = problem.getCostOfActions(l_orig) + heuristic(l_loc, problem)
                    l_fringe = (nodes[0], l_orig, l_pri + l_delta)   
                    l_pqueue.push(l_fringe, l_pri + l_delta)   
                #found a cheaper path to the node
                else:
                    if d_debug:
                        print "FOUND NODE: ", l_node

        #Grab smallest cost action from queue
        l_parent = l_pqueue.pop()
        l_node   = l_parent[0]
        if d_debug:
            print "CURRENT PATH: ", l_parent[1]

        if d_debug:
            d_moveNum = d_moveNum + 1
            print "Succ: ", l_moves
            if d_moveNum == d_maxMoves:
                l_node = (1, 1)        

    return l_parent[1]        

def depthFirstSearch(problem):
    """
    Search the deepest nodes in the search tree first
    [2nd Edition: p 75, 3rd Edition: p 87]

    Your search algorithm needs to return a list of actions that reaches
    the goal.  Make sure to implement a graph search algorithm
    [2nd Edition: Fig. 3.18, 3rd Edition: Fig 3.7].

    To get started, you might want to try some of these simple commands to
    understand the search problem that is being passed in:

    print "Start:", problem.getStartState()
    print "Is the start a goal?", problem.isGoalState(problem.getStartState())
    print "Start's successors:", problem.getSuccessors(problem.getStartState())
    """
    "*** YOUR CODE HERE ***"
    #return pacmanSearch(problem, "DFS")
    #-----------------------#
    #--     Variables     --#
    #-----------------------#
    l_start = problem.getStartState()
    l_pos   = l_start #current position
    l_path  = []    #the path to be returned
    l_expnd = []    #list of expanded nodes
    l_succ  = []    #list of successors at current position
    l_moves = []    #list of all possible moves at each depth
    l_opts  = []    #list of possible moves at current depth
    l_ooo   = False #out of options
    l_findNextMove = True #self-explanitory...
    l_depth = -1
    #-----------------------#
    #-----------------------#
    #--Debugging variables--#
    #-----------------------#
    d_moveNum  = 0
    d_maxMoves = 15
    d_debug    = False
    #-----------------------#
    while (not problem.isGoalState(l_pos)):
        if d_debug:
            print "CURRENT POSITION: ", l_pos        
        l_succ = problem.getSuccessors(l_pos)
        l_expnd.append(l_pos)
        if (len(l_succ) > 0):
            l_depth = l_depth + 1
            l_moves.insert(l_depth, l_succ)
        else:
            if d_debug:
                print "NO SUCCESSORS"
            l_depth = l_depth - 1
            l_path.pop()

        while l_findNextMove:
            l_findNextMove = False
            #get current options
            l_opts = l_moves[l_depth]
            while len(l_opts) == 0:
                l_depth = l_depth - 1
                l_opts = l_moves[l_depth]
                l_path.pop()
            #pick a fringe node
            l_fring = l_opts.pop()
            #make sure we haven't already visited that node
            if (l_fring[0] in l_expnd):
                while (l_fring[0] in l_expnd and not l_ooo):
                    if len(l_opts) == 0:
                        #Ran out of options at current depth
                        l_ooo = True
                        break
                    l_fring = l_opts.pop()

            #no new move options
            if l_ooo :
                #need to move up a level, 
                l_depth = l_depth - 1
                #remove last path adddition,
                l_path.pop()
                #then search those move options
                l_ooo = False
                l_findNextMove = True

        #update current position
        l_pos  = l_fring[0]
        l_path.append(l_fring[1])
        l_ooo  = False
        l_findNextMove = True

        if d_debug:
            d_moveNum = d_moveNum + 1
            print "FRING: ", l_fring
            print "NEW POS: ", l_pos
            print "PATH: ", l_path
            if d_moveNum == d_maxMoves:
                print "FINAL: ", l_succ
                print "DEPTH: ", depth
                print "MOVES: ",l_moves
                l_pos = (1, 1)

    return l_path

def breadthFirstSearch(problem):
    """
    "*** YOUR CODE HERE ***"
    Search the shallowest nodes in the search tree first.
    [2nd Edition: p 73, 3rd Edition: p 82]
    """
    return pacmanSearch(problem, "BFS")

def uniformCostSearch(problem):
    "Search the node of least total cost first. "
    "*** YOUR CODE HERE ***"
    return pacmanSearch(problem, "UCS")

def aStarSearch(problem, heuristic=nullHeuristic):
    "Search the node that has the lowest combined cost and heuristic first."
    "*** YOUR CODE HERE ***"
    """
    start = problem.getStartState()
    pq = util.PriorityQueue()

    pq.push((start, [], [], 0), 0)
    visited = []
    while not pq.isEmpty():
	state = pq.pop()
	visited.append(state[0])
	if problem.isGoalState(state[0]):
		return state[1]
	successors = problem.getSuccessors(state[0])
	for i in range(0, len(successors)):
		ancestors = state[2]
		if successors[i][0] not in ancestors and successors[i][0] not in visited:
			s_loc = successors[i][0]
			s_action = successors[i][1]
			s_act_list = state[1][:]
			s_act_list.append(s_action)
			new_cost = problem.getCostOfActions(s_act_list) + heuristic(successors[i][0], problem)

			new_ancestors = ancestors[:]
			new_ancestors.append(state[0])
			triple = (s_loc, s_act_list, new_ancestors, new_cost)
			pq.push(triple, new_cost)

    return None
    """
    return pacmanSearch(problem, "ASTAR", heuristic)

# Abbreviations
bfs = breadthFirstSearch
dfs = depthFirstSearch
astar = aStarSearch
ucs = uniformCostSearch

