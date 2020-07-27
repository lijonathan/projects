package stubs;


import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapred.*;
import org.apache.hadoop.io.*;

public class reducer2 extends MapReduceBase implements Reducer<Text, Text, Text, Text>{

	@Override
	public void reduce(Text key, Iterator<Text> value,
			OutputCollector<Text, Text> output, Reporter reporter)
			throws IOException {
		
		//calculate Cosine
		float sum=0;
		float sqt1 = 0,sqt2 = 0;
		String[] ratings =null;
		
		while(value.hasNext()){
						
		   ratings = value.next().toString().split(",");
			
			sum = sum + Float.parseFloat(ratings[0]) * Float.parseFloat(ratings[1]);
			
			sqt1 = sqt1 + Float.parseFloat(ratings[0])*Float.parseFloat(ratings[0]);
			sqt2 = sqt2 + Float.parseFloat(ratings[1])*Float.parseFloat(ratings[1]);

		}
		
		double similarity = 0.0;
	
		//calculate Cosine Similarity
		similarity = sum /(Math.sqrt(sqt1) * Math.sqrt(sqt2));

		
		//Storing the 1682 values into a matrix		
		output.collect(key, new Text(String.valueOf(similarity)));
		
	}


}
