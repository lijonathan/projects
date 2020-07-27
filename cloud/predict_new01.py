
from __future__ import print_function
import sys
from operator import add
from pyspark import SparkContext

# stripe representation
def key_mapping(line):
    (item1, item2), similarity = line
    return (item1, [(item2, similarity)])

def get_watched(line):
    pred_movie = line[0]
    user = line[1]
    trueR = float(line[2])
    sim = []

    #print pred_movie

    pred_movie = int(pred_movie)

    if pred_movie not in similarity_dict:
        return (pred_movie, sim)

    for (watched, r) in dict[user]:
        watched = int(watched)
        r = float(r)
        if watched not in similarity_dict[pred_movie]:
            sim.append((watched, r, 0))
        else:
            sim.append((watched, r, similarity_dict[pred_movie][watched]))
    
    sorted_by_similarity = sorted(sim, key=lambda tup: tup[2],reverse=True)

    top_list = sorted_by_similarity[:3]

    ret = 0.0
    
    for (movie, score, similarity_score) in top_list:
	
        ret = ret + score*similarity_score
   
    return (pred_movie, trueR, ret/len(top_list))
def mysplit(line):
    movie_id=line[0].split(",")
    return ((int(movie_id[0]),int(movie_id[1])),float(line[1]))

if __name__ == "__main__":
    
    
    sc = SparkContext(appName="predict")
    #read trainingfile
    myfile = "file:/home/training/FINAL/similarity.txt"

    itemsimilarities = sc.textFile(myfile).map(lambda line:line.split("\t")).map(lambda line:mysplit(line))
    similarity_stripe = itemsimilarities.map(lambda line: key_mapping(line)).reduceByKey(lambda a, b: a+b).collect()

    similarity_dict = {}
    for (key, pairs) in similarity_stripe:
        inside_pair = {}
        for (item1, item2) in pairs:
            inside_pair[item1] = item2
        similarity_dict[key] = inside_pair

    trainfile="file:/home/training/FINAL/TestingRatings.txt"
    train_list = sc.textFile(trainfile).map(lambda line: line.split(',')).map(lambda ids: (ids[1],[(ids[0],ids[2])])).reduceByKey(lambda a, b: a + b).collect()
    dict = {}
    for (user, pairs) in train_list:
        dict[user] = pairs


    testfile="file:/home/training/FINAL/mytest.txt"
    test_watched = sc.textFile(testfile).map(lambda line: line.split(',')).map(lambda ids: get_watched(ids))
    test_watched.take(10)
    test_watched.saveAsTextFile("file:/home/training/FINAL/Testout")
    sc.stop()



