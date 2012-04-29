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
	root_path = sys.argv[1]
	main_path = root_path + '/' + sys.argv[2]
	os.chdir(sys.argv[3])
	test = open('test.txt', 'wb')
	train = open('train.txt', 'wb')
	keys = ['headline', 'body', 'descriptors', 'taxclass']
	prev_dict = {}
	for root, dirs, files in os.walk(main_path):
		for file in files:
			if file.endswith('.nltk'):
				outfile = train
				if (uniform(0,1) > 0.66):
					outfile = test
				f = os.path.join(root, file)
				article = pickle.load(open(f, 'rb'))
#				if int(article['date']) > 1:
#					prev_date = '%d/%02d/%02d' % (int(article['year']), int(article['month']),  int(article['date']) - 1)
#				else:
#					prev_date = '%d/%02d/%02d' % (int(article['year']), int(article['month']),  int(article['date'])) #TODO: Change to previous month
#				#sub_article = dict([(key, article[key]) for key in keys if key in article]) 

#				try:
#					prev_headlines = prev_dict[prev_date]
#				except:
#					prev_headlines = []
#					for root2, dirs2, files2 in os.walk(root_path + '/' + prev_date):
#						for file2 in files2:
#							if file2.endswith('.nltk'):
#								f2 = os.path.join(root2, file2)
#								article2 = pickle.load(open(f2, 'rb'))
#								prev_headlines += article2['headline_words']
#					prev_dict[prev_date] = prev_headlines
#
#				article['prev_headline_words'] = prev_headlines
				
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
