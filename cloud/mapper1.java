package stubs;
import java.io.IOException;


import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class mapper1 extends MapReduceBase implements Mapper<LongWritable,Text,IntWritable,Text>{

	private IntWritable cus_id = new IntWritable();
	private Text rate = new Text();
	@Override
	public void map(LongWritable key, Text value,
			OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException {

		//split the input by ","
		String line= value.toString();
		String[] a= line.split(",");
		
		//change the format of movieid,cust_id from string to int
		cus_id.set(Integer.parseInt(a[1]));
		//change the format of rate from string to float
		rate.set(Integer.parseInt(a[0])+ ","+ Float.parseFloat(a[2]));
		
		//create(custome_id,(movie,rating))pair
		output.collect(cus_id, rate);
			
		
		
	}

}
