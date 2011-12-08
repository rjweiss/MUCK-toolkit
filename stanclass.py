import os
import sys
import cPickle as pickle
from random import uniform

def unique(inlist):
	uniques = []
	for item in inlist:
		if item not in uniques:
			uniques.append(item)
	return uniques

def main():
	main_path = sys.argv[1]
	os.chdir(sys.argv[2])
	test = open('test.txt', 'wb')
	train = open('train.txt', 'wb')
	#keys = ['headline_words', 'body_words', 'body_bigrams']
	keys = ['headline_words', 'body_words']
	for root, dirs, files in os.walk(main_path):
		for file in files:
			if file.endswith('.nltk'):
				outfile = train
				if (uniform(0,1) > 0.66):
					outfile = test
				f = os.path.join(root, file)
				article = pickle.load(open(f, 'rb'))
				#sub_article = dict([(key, article[key]) for key in keys if key in article]) 
				if (article['pagenum'] == "1"):
					outfile.write("1\t")
				else:
					outfile.write("2\t")
				for key in keys:
					value = article[key]
					#outfile.write(key)
					#outfile.write(str(type(value)))
					#outfile.write('\t')
					if (article[key]):
						if type(value) == type([]):
							#value = unique(value)
							#value.sort()
							#outfile.write("[")
							for v in value:
								if (type(v) == type(())):
									v = str(v)
								#v = v.replace("'", 'Q')
								v = v.replace("\t", '')
								v = v.replace("|", '_____')
								#outfile.write("'")
								outfile.write(v)
								#outfile.write("', ")
								outfile.write("|")
							#outfile.write("]")
						else:
							outfile.write(str(value))
						outfile.write('\t')
					else:
						outfile.write("[]\t")
				outfile.write("\n")
	


if __name__ == '__main__':
	main()
