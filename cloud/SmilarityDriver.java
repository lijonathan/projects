package stubs;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class SmilarityDriver extends Configured implements Tool{

	public int run(String[] args) throws Exception{
		
		       
		//job specifications
		JobConf job = new JobConf(getConf(), RecSystemDriver.class);
		
		job.setJobName("Smilarityjob");
		
		job.setInputFormat(KeyValueTextInputFormat.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setJarByClass(SmilarityDriver.class);
		
		job.setMapperClass(mapper2.class);
		job.setReducerClass(reducer2.class);
		
		
		FileInputFormat.addInputPath(job, new Path(args[1]));//("file:///home/training/Desktop/temp_full"));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));//("file:///home/training/Desktop/finalresult"));
		JobClient.runJob(job);		
        
		return 0;
	}
	
	public static void main(String[] args) throws Exception{
	
		int res = ToolRunner.run(new Configuration(), new RecSystemDriver(), args);		
		System.exit(res);
	}

}