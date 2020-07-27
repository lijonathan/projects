function [ypredict]=evaltree(T,xTe)
% function [ypredict]=evaltree(T,xTe);
%
% input:
% T0  | tree structure
% xTe | Test data (dxn matrix)
%
% output:
%
% ypredict : predictions of labels for xTe
%

Treesize = size(T, 2);
n = size(xTe, 2);
dimension = size(xTe, 1);
ypredict = zeros(1,n);

for index = 1:n
    datapoint = xTe(:, index);
    Tindex = 1;
    
    while Tindex <= Treesize
        if T(4, Tindex) == 0 && T(5, Tindex) == 0
            break;
        end
        if datapoint(T(2,Tindex)) <= T(3,Tindex) %if datapoint(feature) > cutoff
            Tindex = T(4, Tindex);
        else
            Tindex = T(5, Tindex);
        end
%         datapoint = datapoint(1:end ~= T(2,Tindex)); %update the datapoint to not include last feature
    end
    
    if Tindex <= Treesize
        ypredict(index) = T(1, Tindex);
    else
        ypredict(index) = T(1, floor(Tindex/2));
    end
    
    
end

