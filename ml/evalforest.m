function preds=evalforest(F,xTe)
% function preds=evalforest(F,xTe);
%
% Evaluates a random forest on a test set xTe.
%
% input:
% F   | Forest of decision trees
% xTe | matrix of m input vectors (matrix size dxm)
%
% output:
%
% preds | predictions of labels for xTe
%

Fsize = size(F, 2);
m = size(xTe, 2);
totalYPreds = zeros(Fsize, m); %aggregation of all y predictions (#trees by #datapoints) 

for index = 1:Fsize
    Tree = cell2mat(F(index));
    totalYPreds(index, :) = evaltree(Tree, xTe);
end

preds = mode(totalYPreds);
