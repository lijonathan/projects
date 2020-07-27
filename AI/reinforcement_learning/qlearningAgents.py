# qlearningAgents.py
# ------------------
# Licensing Information: Please do not distribute or publish solutions to this
# project. You are free to use and extend these projects for educational
# purposes. The Pacman AI projects were developed at UC Berkeley, primarily by
# John DeNero (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# For more info, see http://inst.eecs.berkeley.edu/~cs188/sp09/pacman.html

from game import *
from learningAgents import ReinforcementAgent
from featureExtractors import *

import random,util,math

class QLearningAgent(ReinforcementAgent):
  """
    Q-Learning Agent

    Functions you should fill in:
      - getQValue
      - getAction
      - getValue
      - getPolicy
      - update

    Instance variables you have access to
      - self.epsilon (exploration prob)
      - self.alpha (learning rate)
      - self.discount (discount rate)

    Functions you should use
      - self.getLegalActions(state)
        which returns legal actions
        for a state
  """
  def __init__(self, **args):
    "You can initialize Q-values here..."
    ReinforcementAgent.__init__(self, **args)

    "*** YOUR CODE HERE ***"
    #same concept as valueIterationAgent
    self.values = util.Counter()

  def getQValue(self, state, action):
    """
      Returns Q(state,action)
      Should return 0.0 if we never seen
      a state or (state,action) tuple
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    # No local variables
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    if (state, action) in self.values:
      return self.values[(state, action)]
    else:
      if (d_debug):
        print "!!! ACTION NOT FOUND !!!"
        print "STATE:  ", state
        print "ACTION: ", action
      return 0.0

  def getValue(self, state):
    """
      Returns max_action Q(state,action)
      where the max is over legal actions.  Note that if
      there are no legal actions, which is the case at the
      terminal state, you should return a value of 0.0.
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_actions   = None
    l_qVal      = float("-inf")
    l_max       = float("-inf")
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    l_actions = self.getLegalActions(state)
    if (d_debug):
      print "ACTIONS: ", l_actions
    #No possible actions
    if len(l_actions) == 0:
      return 0.0
    else:
      for action in l_actions:
        l_qVal = self.getQValue(state, action)
        if (l_qVal > l_max):
          l_max = l_qVal
    if (d_debug):
      print "MAX: ", l_max
    return l_max

  def getPolicy(self, state):
    """
      Compute the best action to take in a state.  Note that if there
      are no legal actions, which is the case at the terminal state,
      you should return None.
    """
    "*** YOUR CODE HERE ***"
    return self.getAction(state)

  def getAction(self, state):
    """
      Compute the action to take in the current state.  With
      probability self.epsilon, we should take a random action and
      take the best policy action otherwise.  Note that if there are
      no legal actions, which is the case at the terminal state, you
      should choose None as the action.

      HINT: You might want to use util.flipCoin(prob)
      HINT: To pick randomly from a list, use random.choice(list)
    """
    # Pick Action
    legalActions = self.getLegalActions(state)
    action = None
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_qVal      = float("-inf")
    l_max       = float("-inf")
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    #First need to check if we need to pick a random action
    if (d_debug):
      print "ACTIONS: ", legalActions
    if util.flipCoin(self.epsilon):
      if (d_debug):
        print "RANDOM CHOICE"
      return random.choice(legalActions)
    #Choosing the best action
    for act in legalActions:
      l_qVal = self.getQValue(state, act)
      if (l_qVal > l_max):
        l_max = l_qVal
        action = act
    if (d_debug):
      print "MAX/ACTION", (l_max, action)
    return action

  def update(self, state, action, nextState, reward):
    """
      The parent class calls this to observe a
      state = action => nextState and reward transition.
      You should do your Q-Value update here

      NOTE: You should never call this function,
      it will be called on your behalf
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_peek       = None
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!#
    # Updating Q Value -- Lecture 10/Slide 19            #
    # Q[s, a] = (1-a)*Q[s, a] +                          #
    #               a*(reward + discount*max(Q[s', a'])) #
    #!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!#
    l_peek = self.alpha * (reward + self.discount * self.getValue(nextState))
    if (d_debug):
      print "PEEK:   ", l_peek
      print "ALPHA:  ", self.alpha
      print "State:  ", state
      print "Action: ", action
    self.values[(state, action)] = (1-self.alpha)*self.getQValue(state, action) + l_peek

class PacmanQAgent(QLearningAgent):
  "Exactly the same as QLearningAgent, but with different default parameters"

  def __init__(self, epsilon=0.05,gamma=0.8,alpha=0.2, numTraining=0, **args):
    """
    These default parameters can be changed from the pacman.py command line.
    For example, to change the exploration rate, try:
        python pacman.py -p PacmanQLearningAgent -a epsilon=0.1

    alpha    - learning rate
    epsilon  - exploration rate
    gamma    - discount factor
    numTraining - number of training episodes, i.e. no learning after these many episodes
    """
    args['epsilon'] = epsilon
    args['gamma'] = gamma
    args['alpha'] = alpha
    args['numTraining'] = numTraining
    self.index = 0  # This is always Pacman
    QLearningAgent.__init__(self, **args)

  def getAction(self, state):
    """
    Simply calls the getAction method of QLearningAgent and then
    informs parent of action for Pacman.  Do not change or remove this
    method.
    """
    action = QLearningAgent.getAction(self,state)
    self.doAction(state,action)
    return action


class ApproximateQAgent(PacmanQAgent):
  """
     ApproximateQLearningAgent

     You should only have to overwrite getQValue
     and update.  All other QLearningAgent functions
     should work as is.
  """
  def __init__(self, extractor='IdentityExtractor', **args):
    self.featExtractor = util.lookup(extractor, globals())()
    PacmanQAgent.__init__(self, **args)

    # You might want to initialize weights here.
    "*** YOUR CODE HERE ***"
    self.weights = util.Counter()

  def getQValue(self, state, action):
    """
      Should return Q(state,action) = w * featureVector
      where * is the dotProduct operator
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_qVal       = 0.0
    l_feats      = None
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    l_feats = self.featExtractor.getFeatures(state, action)
    for i in l_feats.keys():
      if (d_debug):
        print "FEATURE: ", l_feats[i]
        print "KEYS: ", l_feats.keys()
      l_qVal += self.weights[i] * l_feats[i]

    return l_qVal    

  def update(self, state, action, nextState, reward):
    """
       Should update your weights based on transition
    """
    "*** YOUR CODE HERE ***"
    #---------------------#
    #   Local Variables   #
    #---------------------#
    l_correction = 0.0
    l_feats      = None
    #---------------------#
    # Debugging Variables #
    #---------------------#
    d_debug = False
    #---------------------#
    #correction = (Reward[s,a] + discount*V(s'))-Q[s,a]
    #weight     = weight + alpha[correction]*f[s,a]
    l_feats = self.featExtractor.getFeatures(state, action)
    l_correction = reward + self.discount*self.getValue(nextState) - self.getQValue(state, action)
    if (d_debug):
      print "FEATURES: ", l_feats    
      print "KEYS: ", l_feats.keys()
    for i in l_feats.keys():
      if (d_debug):
        print "UPDATING WEIGHTS"
      self.weights[i] += self.alpha*l_correction*l_feats[i]

  def final(self, state):
    "Called at the end of each game."
    # call the super-class final method
    PacmanQAgent.final(self, state)

    # did we finish training?
    if self.episodesSoFar == self.numTraining:
      # you might want to print your weights here for debugging
      "*** YOUR CODE HERE ***"
      pass

