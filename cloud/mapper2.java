package stubs;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.hadoop.mapred.*;
import org.apache.hadoop.io.*;

public class mapper2 extends MapReduceBase implements Mapper<Text, Text, Text, Text>{

	private Text moviePair = new Text();
	private Text ratingPair = new Text();
	
		
	@Override
	public void map(Text key, Text value, OutputCollector<Text, Text> output,
			Reporter reporter) throws IOException {
		
		HashMap<String,String> MovieMap = new HashMap<String,String>();
		
		//create the (movie,movie) pairs and (rate,reate) pair
		String[] item_pairs = value.toString().split(" ");
		String[] split = null;
		
		for(int i=0;i<item_pairs.length;i++){
			
			//movie and rate key value pairs
			split = item_pairs[i].split(",");
			MovieMap.put(split[0], split[1]);
		}
		
		//form the movie pairs by iterating through map
		Set<String> keys = MovieMap.keySet();
		for(String topkey: keys){

			for(String multikey: keys){
				
				if(Integer.parseInt(multikey) > Integer.parseInt(topkey)){
					
					moviePair.set(topkey+","+multikey);
					ratingPair.set(MovieMap.get(topkey)+","+MovieMap.get(multikey));

					output.collect(moviePair, ratingPair);
					
				}
			}
			
		}
		
		
	}


}
