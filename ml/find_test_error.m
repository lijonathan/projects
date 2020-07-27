function [ test_error ] = find_test_error( w, X, y )
%FIND_TEST_ERROR Find the test error of a linear separator
%   This function takes as inputs the weight vector representing a linear
%   separator (w), the test examples in matrix form with each row
%   representing an example (X), and the labels for the test data as a
%   column vector (y). X does not have a column of 1s as input, so that 
%   should be added. The labels are assumed to be plus or minus one. 
%   The function returns the error on the test examples as a fraction. The
%   hypothesis is assumed to be of the form (sign ( [1 x(n,:)] * w )
    
    [rows, columns] = size(X);
    test_classification = zeros(1, rows);
    for i=1:rows
        answer = X(i, :) * w';
        if answer <= 0
            test_classification(i) = -1;
        end
        if answer > 0
            test_classification(i) = 1;
        end
    end
    incorrect = 0;
    for j=1:rows
        if test_classification(j) ~= y(j)
            incorrect = incorrect + 1;
        end
    end
    test_error = incorrect / rows;
    
    
end

