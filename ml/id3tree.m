function T=id3tree(xTr,yTr,maxdepth,weights)
% function T=id3tree(xTr,yTr,maxdepth,weights)
%
% The maximum tree depth is defined by "maxdepth" (maxdepth=2 means one split).
% Each example can be weighted with "weights".
%
% Builds an id3 tree
%
% Input:
% xTr | dxn input matrix with n column-vectors of dimensionality d
% yTr | 1xn input matrix
% maxdepth = maximum tree depth
% weights = 1xn vector where weights(i) is the weight of example i
%
% Output:
% T = decision tree
%
dimension = size(xTr,1);
n = size(yTr,2);
if ~exist('maxdepth', 'var')
    maxdepth = Inf(1);
end
if ~exist('weights', 'var')
    weights = 1/n*ones(1,n);
end
if dimension + 1 > maxdepth
    q = 2^maxdepth - 1;
else
    q = 2^(dimension + 1) - 1; %q is the number of nodes in the tree
end
T = zeros(6,q);
% 1 prediction at node, 2 index of feature to cut, 3 cutoff value c
% 4 index of left subtree, 5 index of right subtree, 6 parent

%the second row of indexBucket stores the relevant xTr indices per node
%the first row of indexBucket stores the average sign (1, -1) of those indices
indexBucket = cell(2,q);
indexBucket(2,1) = {1:n}; %populate indexBucket(i=1) with all of xTr
indexBucket(1,1) = {mode(yTr)}; %mode returns the smallest value if there is a tie

stack = 1;
feature = 0;
inputweights = weights;
while ~isempty(stack)
    
    index = stack(end); %get last index in stack -> i
    stack = stack(1:length(stack)-1); %popping stack
    if 2*index <= q
        stack = [stack 2*index+1 2*index]; %populate stack with 2i and 2i+1 (if 2i <= q)
    end
    
%     %run entropysplit based on the indices found in indexBucket(i) -> reduce xTr
%     %into just those indices in indexBucket
%     reducedxTr = xTr(:, cell2mat(indexBucket(2,index)));
%     reducedyTr = yTr(cell2mat(indexBucket(2,index)));
    
    %The issue is that the reducedxTr is reading off the indices of the
    %past reducedxTr which means that the indices are being shifted
    
    %solution: utilize index bucket as a way to manipulate the weights
    currentIndices = cell2mat(indexBucket(2, index));
    weights = zeros(1, n);
    weights(currentIndices) = inputweights(currentIndices);
    
%     if ~isempty(setdiff(1:n, currentIndices))
%         weights(setdiff(1:n, currentIndices)) = 0;
%     end
    
    [feature, cut, Hbest] = entropysplit(xTr,yTr,weights);
    % feature | best feature to split
    % cut     | Value to split the feature on
    % Hbest   | Loss of best split (like an error term)

    if ~isempty(cell2mat(indexBucket(1, index)))
        T(1, index) = cell2mat(indexBucket(1, index));
        T(2, index) = feature;
        T(3, index) = cut;
        T(6, index) = floor(index/2);
    end

    %when we get the split from entropy split, update: populate indexBucket(2i) and
    %indexBucket(2i+1) with the indices of xTr based on the split

    if ~isempty(currentIndices) && range(yTr(currentIndices)) ~= 0 %not leaf
        T(4, index) = 2*index;
        T(5, index) = 2*index+1;
        
        %Let's use set intersect
        indexBucket(2, 2*index) = {intersect(find(xTr(feature,:) <= cut), cell2mat(indexBucket(2, index)))};
        indexBucket(1, 2*index) = {mode(yTr(cell2mat(indexBucket(2, 2*index))))};
        indexBucket(2, 2*index+1) = {intersect(find(xTr(feature,:) > cut), cell2mat(indexBucket(2, index)))};
        indexBucket(1, 2*index+1) = {mode(yTr(cell2mat(indexBucket(2, 2*index+1))))};
    end
    
end

