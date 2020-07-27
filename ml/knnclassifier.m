function preds=knnclassifier(xTr,yTr,xTe,k)
% function preds=knnclassifier(xTr,yTr,xTe,k);
%
% k-nn classifier 
%
% Input:
% xTr = dxn input matrix with n column-vectors of dimensionality d
% xTe = dxm input matrix with n column-vectors of dimensionality d
% k = number of nearest neighbors to be found
%
% Output:
%
% preds = predicted labels, ie preds(i) is the predicted label of xTe(:,i)
%

% % output random result as default (you can erase this code)
% [d,n]=size(xTe);
% [d,ntr]=size(xTr);
% if k>ntr,k=ntr;end;
% 
% %currently assigning random predictions
% un=unique(yTr);
% preds=un(ceil(rand(1,n)*length(un)));
% 
% %% fill in code here
function g=modeY(C, yTr)
    yTrainK = yTr(:, C);
    [g,f,cell] = mode(yTrainK);
    while size(cell) ~= size(ones(1,1))
        C = C(1:length(C)-1);
    end
end
[I, D] = findknn(xTr, xTe, k);
[d,n]=size(xTe);
yTrExpand = repmat(yTr, n, 1);
yTrExpand = num2cell(yTrExpand,2);
I = num2cell(I',2);
preds = cellfun(@modeY,I,yTrExpand);
preds = preds';
end


