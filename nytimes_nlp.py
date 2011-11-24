import nltk
import re
import os
import cPickle as pickle

class Article(object):

	#might not need access to all these attributes, but for now might as well define them
	def __init__(self, article_dict):
		self.headline = article_dict['headline']
		self.lead = article_dict['lead']
		self.body = article_dict['body']
		self.pagenum = article_dict['pagenum']
		self.taxclass = article_dict['taxclass']
		self.descriptors = article_dict['descriptors']
		self.pagecol = article_dict['pagecol']
		self.month = article_dict['month']
		self.pagesect = article_dict['pagesect']
		self.year = article_dict['year']
		self.date = article_dict['date']
		self.general_desc = article_dict['general_desc']
		self.day = article_dict['day']

	

def main():
	main_path = '/home/rebecca/Desktop/final project/'
	os.chdir(main_path)

	nytimes = open('nytimes2000.pkl', 'r')
	nytimes = pickle.load(nytimes)

	articles = []
	for article in nytimes:
		articles.append(Article(article['headline'], article['lead'], article['body']))	

if __name__ == '__main__':
	main()
	print "Done."
