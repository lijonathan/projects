function preds=competition(xTr,yTr,xTe);
% function preds=competition(xTr,yTr,xTe);
%
% A classifier that outputs predictions for the data set xTe based on 
% what it has learned from xTr,yTr
%
% Input:
% xTr = dxn input matrix with n column-vectors of dimensionality d
% xTe = dxm input matrix with n column-vectors of dimensionality d
%
% Output:
%
% preds = predicted labels, ie preds(i) is the predicted label of xTe(:,i)
%

% The random classifier is awesome!!! Could you possibly do better?

k = 1; %%play around with this
preds = knnclassifer(xTr, yTr, xTe, k);