from lxml import etree
import os
import sys
import traceback
from datetime import datetime
from pymongo import Connection

#import cPickle as pickle
#import glob

"""
HOW TO USE: nytimes_parser sourcedir 

"""
def parse_xml(file):
	
	doc = etree.parse(file)
	root = doc.getroot()

	#meta data
	try:
		pagenum = root.xpath('//head/meta[@name="print_page_number"]')[0].get("content")
	except:
		pagenum = ""
		print "missing page number: %s" % file
	try:
		month = root.xpath('//head/meta[@name="publication_month"]')[0].get("content")
		date = root.xpath('//head/meta[@name="publication_day_of_month"]')[0].get("content")
		year = root.xpath('//head/meta[@name="publication_year"]')[0].get("content")
		day = root.xpath('//head/meta[@name="publication_day_of_week"]')[0].get("content")
		date_string = month + '/' + date + '/' + year
		date_obj = datetime.strptime(date_string, '%m/%d/%Y')
	except:
		date_obj = ""
		print "malformed date: %s" % file
	
	#need to make datetime object from month date year

	
	#docdata

	#body data
	try:
		headline = root.xpath('//body/body.head/hedline/hl1')[0].text
	except:
		headline = ""
		print "missing headline: %s" % file

	try:
		body = root.xpath('//body/body.content/block[@class="full_text"]/p/text()')
		body = [unicode(element) for element in body]
		body = ' '.join(body)
	except UnicodeError:
		#print unicode(body)
		body = ""
		print "unicode error: %s" % file
	except:
		body = ""
		print "error parsing body text: %s" % file

	return {"newspaper": "New York Times", "date": date_obj, "headline": headline, "body": body, "pagenum": pagenum, "file": os.path.basename(file), "path": os.path.relpath(file, "/media/NEWSPAPER")}

def parse_dir(root_dir):
	c = Connection("localhost")
	db = c.test
	articles = db.articles
	for root, dirs, files in os.walk(root_dir):
		for file in files:
			f = os.path.join(root, file)
			if f.endswith(".xml"):
				try:
					#article = parse_xml(f)
					#print article
					#articles.insert(article)
					#print(parse_xml(f)["path"])
					articles.insert(parse_xml(f))
				except:
					print "exception parsing file: %s" % f
					print traceback.print_exc()
					os._exit(0)

def main():
	parse_dir(sys.argv[1])

if __name__ == '__main__':
	main()
	print "Done."
