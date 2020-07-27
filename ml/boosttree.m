function BDT=boosttree(X,y,nt,maxdepth)
% function BDT=boosttree(x,y,nt,maxdepth)
%
% Learns a boosted decision tree on data X with labels y.
% It performs at most nt boosting iterations. Each decision tree has maximum depth "maxdepth".
%
% INPUT:
% X  | input vectors dxn
% y  | input labels 1xn
% nt | number of trees (default = 100)
% maxdepth | depth of each tree (default = 3)
%
% OUTPUT:
% BDT | Boosted DTree
%

dimension = size(X, 1);
n = size(X, 2);

if ~exist('maxdepth', 'var')
    maxdepth = 3;
end
if ~exist('nt', 'var')
    nt = 100;
end

weights = ones(1, n)/n;
H = cell(2, nt);

for index = 1:nt
    
    Tree = id3tree(X, y, maxdepth, weights);
    
    pre = evaltree(Tree, X);
    err = sum(abs(sign(pre-y)))/n;
    
    if err > 0.5
        break
    end
    
    alpha = 0.5 * log((1-err)/err);
    
    H(1, index) = {Tree};
    H(2, index) = {alpha};
    
    z = 1-2*abs(pre-y);
    weights = ((exp(-alpha*z)))./(2*sqrt(err*(1-err)));

end
BDT = H;
    
end