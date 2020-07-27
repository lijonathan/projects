function [ w, e_in ] = logistic_reg(max_its)

%function [ w, e_in ] = logistic_reg( X, y, w_init, max_its, eta )

%LOGISTIC_REG Learn logistic regression model using gradient descent
%   Inputs:
%       X : data matrix (without an initial column of 1s)
%       y : data labels (plus or minus 1)
%       w_init: initial value of the w vector (d+1 dimensional)
%       max_its: maximum number of iterations to run for
%       eta: learning rate
    
    %y is a row vector 1xN
    %w_init is a row vector 1xd
    X = csvread('clevelandtrain.csv', 1, 0);
    X = X(1:152,1:13);
    y = csvread('clevelandtrain.csv', 1, 13);
    for i=1:size(y)
        if y(i) == 0
            y(i) = -1;
        end
    end
    [rows, columns] = size(X);
    
    zeros_first_col = zeros(1, rows);
    X = X';
    X = [zeros_first_col; X];
    X = X';
    w_init = zeros(1, columns + 1);
    
    eta = .00001;
    iterations = 0;
    condition_to_continue = 1;
    for i= 0:max_its
       g_t = 0;
       g_N = 0;
       for n= 1:rows
           numerator = X(n, :) * y(n); %vector 
           denominator = 1 + exp(y(n)* (X(n, :) * transpose(w_init))); %scalar
           g_N = g_N + numerator / denominator;
       end
       g_t = g_N * -1 / rows;
       v_t = -1 * g_t;
       w_init = w_init + eta* v_t;
       for j = 1: size(g_t) %checks if each term in the gradient is below threshold
            if abs(g_t(j)) > .001
                break;
            end
            if j == size(g_t)
                condition_to_continue = 0;
                break;
            end
       end
       iterations = i
       
       if condition_to_continue == 0
           break
       end
    end
    
    %%%%%%%%%%%%Finding Error
    sum = 0;
    for n=1:rows
        sum = sum + log(1 + exp(-y(n) * X(n, :) * transpose(w_init)));
    end


%   Outputs:
%       w : weight vector
%       e_in : in-sample error (as defined in LFD)
    w = w_init
    e_in = sum * 1 / rows
    
    
    testData = csvread('clevelandtest.csv', 1, 0);
    testData = testData(1:145,1:13);
    [testRows, testCols] = size(testData);
    zeros_first_cols = zeros(1, testRows);
    testData = testData';
    testData = [zeros_first_cols; testData];
    testData = testData';
    
    testY = csvread('clevelandtest.csv', 1, 13);
    for k=1:size(testY)
        if testY(k) == 0
            testY(k) = -1;
        end
    end
    test_error = find_test_error(w, testData, testY)
end

