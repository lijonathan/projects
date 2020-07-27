function output=analyze(kind,truth,preds)	
% function output=analyze(kind,truth,preds)		
%
% Analyses the accuracy of a prediction
% Input:
% kind='acc' classification error
% kind='abs' absolute loss
% (other values of 'kind' will follow later)
% 

switch kind
	case 'abs'
        output = mean(abs(truth - preds));
		% compute the absolute difference between truth and predictions
		%fill in the code here
		
	case 'acc' 
		diffs = truth-preds;
		zeroOutput = find(not(diffs));
        output = size(zeroOutput, 2)/size(truth, 2);
end;

