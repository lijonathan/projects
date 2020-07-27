function T=prunetree(T,xTe,y)
% function T=prunetree(T,xTe,y)
%
% Prunes a tree to minimal size such that performance on data xTe,y does not
% suffer.
%
% Input:
% T = tree
% xTe = validation data x (dxn matrix)
% y = labels (1xn matrix)
%
% Output:
% T = pruned tree
%

originT = T;
yPred = evaltree(originT, xTe);
error = sum(abs(y - yPred));
compareError = -inf;

while compareError < error
    originT = T;
    currentIndex = size(T, 2);
    %find the last leaf
    %stop when the current node's left child is nothing 
    %but it has a prediction
    while T(4, currentIndex) == 0 && T(1, currentIndex) == 0
         currentIndex = currentIndex - 1;
    end

    %right now the current index should be the last leaf
    parentWithDeadChildrenIndex = floor(currentIndex/2);
    T(4, parentWithDeadChildrenIndex) = 0;
    T(5, parentWithDeadChildrenIndex) = 0;
    
    T = T(:, 1:parentWithDeadChildrenIndex);
    ypred = evaltree(T, xTe);
    compareError = sum(abs(y-ypred));
end
T = originT;
