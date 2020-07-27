function D=l2distance(X,Z)
% function D=l2distance(X,Z)
%	
% Computes the Euclidean distance matrix. 
% Syntax:
% D=l2distance(X,Z)
% Input:
% X: dxn data matrix with n vectors (columns) of dimensionality d
% Z: dxm data matrix with m vectors (columns) of dimensionality d
%
% Output:
% Matrix D of size nxm 
% D(i,j) is the Euclidean distance of X(:,i) and Z(:,j)
%
% call with only one input:
% l2distance(X)=l2distance(X,X)
%

if (nargin==1) % case when there is only one input (X)
    G = X' * X; 
    
    S = repmat(sum(X.^2)', 1, size(X,2));
    
    
    R = repmat(sum(X.^2), size(X,2), 1);
    

    D = sqrt(abs(S - 2.*G + R));

else  % case when there are two inputs (X,Z)
    G = X' * Z; 

    S = repmat(sum(X.^2)', 1, size(Z,2));
    
    R = repmat(sum(Z.^2), size(X,2), 1);
    

    D = sqrt(abs(S - 2.*G + R));
end;
%



