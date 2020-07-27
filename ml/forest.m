function F=forest(X,y,nt)
% function F=forest(x,y,nt)
%
% INPUT:
% X  | input vectors dxn
% y  | input labels 1xn
% nt | number of trees
%
% OUTPUT:
% F | Forest
%

F = cell(1, nt);

n = size(X, 2);

for index = 1:nt
    randIndices = randsample(1:n, n, true);
    tempX = X(:,randIndices);
    tempY = y(randIndices);
    F(index) = {id3tree(tempX, tempY)};
end