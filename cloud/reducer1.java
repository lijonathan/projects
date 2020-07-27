package stubs;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapred.*;
import org.apache.hadoop.io.*;

public class reducer1 extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, Text>{

	@Override
	public void reduce(IntWritable key, Iterator<Text> values,
			OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException {
		
		StringBuilder rate = new StringBuilder();
		String newValue = "";
		
		while(values.hasNext()){
			newValue = values.next().toString();
			rate.append(newValue+" ");			
			
		}
	    
		//create (cust_id,(movieid,rating),(moveid,rating))stripe
		output.collect(key, new Text(rate.toString().trim()));
	}

	

}
