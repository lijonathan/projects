package stubs;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.*;

public class RecSystemDriver extends Configured implements Tool{
	public static void main(String[] args) throws Exception {
		  int exitCode=ToolRunner.run(new Configuration(),newRecSystemDriver(),args);
		  System.exit(exitCode);
	  }
	public int run(String[] args) throws Exception{
		if (args.length != 2) {
		        System.out.printf("Usage: %s [generic options] <input dir><output dir>\n",getClass().getSimpleName());
		    	return -1;   
		    }
		//job1 specifications
		Job job1 = new Job(getConf());
		job1.setJarByClass(RecSystemDriver.class);
		job1.setJobName("RecSystemJob1");
		
		job1.setOutputKeyClass(IntWritable.class);
		job1.setOutputValueClass(Text.class);
		
	    FileInputFormat.setInputPaths(job1,new Path(args[0]));
	    FileOutputFormat.setOutputPath(job1, new Path(args[1]));		
		
		job1.setMapperClass(mapper1.class);
		job1.setReducerClass(reducer1.class);		       
		
		boolean success = job1.waitForCompletion(true);
	    //System.exit(success ? 0 : 1);
	    return success? 0:1;
	}
	
}
