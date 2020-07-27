function preds=evalboost(BDT,xTe)
% function preds=evalboost(BDT,xTe);
%
% Evaluates a boosted decision tree on a test set xTe.
%
% input:
% BDT | Boosted Decision Trees
% xTe | matrix of m input vectors (matrix size dxm)
%
% output:
%
% preds | predictions of labels for xTe
%

dimension = size(xTe, 1);
n = size(xTe, 2);
BDTn = size(BDT, 2);

a = 0;
preds = zeros(1, n);
for index = 1:BDTn
    
    preds = preds + BDT{2, index}*evaltree(BDT{1, index}, xTe);
    a = a + BDT{2, index};

end

preds = preds/a;


