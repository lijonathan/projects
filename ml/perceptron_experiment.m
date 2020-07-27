function [ num_iters bounds] = perceptron_experiment ( N, d, num_samples )
%perceptron_experiment Code for running the perceptron experiment in HW1
%   Inputs: N is the number of training examples
%           d is the dimensionality of each example (before adding the 1)
%           num_samples is the number of times to repeat the experiment
%   Outputs: num_iters is the # of iterations PLA takes for each sample
%            bound_minus_ni is the difference between the theoretical bound
%               and the actual number of iterations
%      (both the outputs should be num_samples long)

    %%%NOTE: I could not get this part to work, but perceptron_learn works
    %%%in and of itself as a standalone perceptron learning algorithm
    count = 1;
    num_iters = zeros(1, num_samples);
    while count < num_samples
        trueWeightVector = zeros(1, d + 1);
        for i = 2: 11
            trueWeightVector(i) = rand();
        end
        trainingSet = (1-(-1)).*rand(N+1, d+1)+(-1);
        trainingSet
        trueWeightVector
        break;
        %size(trueWeightVector) %size of weightVector is 1x11
        %size(trainingSet) %size of trainingSet is 6x10??
      
        trueClassified = trueWeightVector * trainingSet;
        trueClassified = sign(trueClassified);
        for i = 1:trueClassified
            if trueClassified(i) == 0
                trueClassified(i) = 1;
            end
        end
        answer = perceptron_learn(trainingSet, trueClassified, d, N);
        num_iters(count) = answer(2);
    end
    num_iters;
    
    
end

