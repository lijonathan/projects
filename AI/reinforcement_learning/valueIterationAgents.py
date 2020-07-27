# valueIterationAgents.py
# -----------------------
# Licensing Information: Please do not distribute or publish solutions to this
# project. You are free to use and extend these projects for educational
# purposes. The Pacman AI projects were developed at UC Berkeley, primarily by
# John DeNero (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# For more info, see http://inst.eecs.berkeley.edu/~cs188/sp09/pacman.html

import mdp, util

from learningAgents import ValueEstimationAgent

class ValueIterationAgent(ValueEstimationAgent):
  """
      * Please read learningAgents.py before reading this.*

      A ValueIterationAgent takes a Markov decision process
      (see mdp.py) on initialization and runs value iteration
      for a given number of iterations using the supplied
      discount factor.
  """
  def __init__(self, mdp, discount = 0.9, iterations = 100):
    """
      Your value iteration agent should take an mdp on
      construction, run the indicated number of iterations
      and then act according to the resulting policy.
    
      Some useful mdp methods you will use:
          mdp.getStates()
          mdp.getPossibleActions(state)
          mdp.getTransitionStatesAndProbs(state, action)
          mdp.getReward(state, action, nextState)
    """
    self.mdp = mdp
    self.discount = discount
    self.iterations = iterations
    self.values = util.Counter() # A Counter is a dict with default 0
     
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_states   = None
    l_action   = None
    l_tempVals = None
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    l_states = self.mdp.getStates()
    for iteration in range(0, self.iterations):
      l_tempVals = util.Counter()
      for state in l_states:
        l_action = self.getPolicy(state)
        if l_action == None:
          l_tempVals[state] = 0.0
        else:
          l_tempVals[state] = self.getQValue(state, l_action)
      self.values = l_tempVals

  def getValue(self, state):
    """
      Return the value of the state (computed in __init__).
    """
    return self.values[state]


  def getQValue(self, state, action):
    """
      The q-value of the state action pair
      (after the indicated number of value iteration
      passes).  Note that value iteration does not
      necessarily create this quantity and you may have
      to derive it on the fly.
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_qval   = 0.0
    l_reward = 0.0
    l_next   = None
    l_prob   = None
    l_sandp  = None #states and probabilities
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    #Q = sum( T * (reward + discount * value(next state)))
    l_sandp = self.mdp.getTransitionStatesAndProbs(state, action)
    for stateAndProb in l_sandp:
      l_next = stateAndProb[0]
      l_prob  = stateAndProb[1]
      l_reward = self.mdp.getReward(state, action, l_next)
      if (d_debug):
        print "PROB: ", l_prob
        print "DISC: ", self.discount
        print "RWRD: ", l_reward
        print "Next: ", self.values[l_next]
      l_qval += l_prob * (l_reward + self.discount * self.values[l_next])
    return l_qval

  def getPolicy(self, state):
    """
      The policy is the best action in the given state
      according to the values computed by value iteration.
      You may break ties any way you see fit.  Note that if
      there are no legal actions, which is the case at the
      terminal state, you should return None.
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_bestMove = None
    l_actions  = None
    l_qval     = float("-inf")
    l_qtemp    = 0.0
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    l_actions = self.mdp.getPossibleActions(state)
    for action in l_actions:
      l_qtemp = self.getQValue(state, action)
      if l_qtemp > l_qval:
        l_qval = l_qtemp
        l_bestMove = action

    return l_bestMove

  def getAction(self, state):
    "Returns the policy at the state (no exploration)."
    return self.getPolicy(state)
  

