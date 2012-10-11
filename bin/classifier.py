import csv
import numpy
import matplotlib
import pymongo
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.naive_bayes import MultinomialNB
from sklearn.svm.sparse import LinearSVC
from sklearn.pipeline import Pipeline

#connecting to mongodb on localhost

codes = []
with ('coderdata.csv', 'rb') as coderdata:
  for (datum in coderdata):
          codes.append((datum[0], datum[1], datum[2]))

conn = pymongo.Connection()
db = conn.news

#text = db.devset.distinct({})
