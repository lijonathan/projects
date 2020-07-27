function [ w iterations ] = perceptron_learn(data_in)
%perceptron_learn Run PLA on the input data
%   Inputs: data_in: Assumed to be a matrix with each row representing an
%                    (x,y) pair, with the x vector augmented with an
%                    initial 1, and the label (y) in the last column
%   Outputs: w: A weight vector (should linearly separate the data if it is
%               linearly separable)
%            iterations: The number of iterations the algorithm ran for

initial_weight_vector = rand(1,11);
initial_weight_vector(1) = 0;
trainingSet = 2*rand(11, 100) + -1;
correct_classification = initial_weight_vector * trainingSet;
correct_classification = sign(correct_classification);
for i = 1:100
    if correct_classification(i) == 0
        correct_classification(i) = 1;
    end
end

incorrectlyClassified = 1;
currentWeightVector = zeros(1,11);
counter = 0;
while incorrectlyClassified == 1
    current_classification = currentWeightVector * trainingSet;
    current_classification = sign(current_classification);
    for i = 1:100
        if current_classification(i) == 0
            current_classification(i) = 1;
        end
    end
    
    for index = 1:100
        if current_classification(index) ~= correct_classification(index)
            currentWeightVector = currentWeightVector + correct_classification(index) * trainingSet(:,index)';
            break
        end
        if index == 100
            incorrectlyClassified = 0;
        end
     end
     counter = counter + 1;
end

iterations = counter;
w = currentWeightVector;

end

