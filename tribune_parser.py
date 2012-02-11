from lxml import etree
import os
import sys
import traceback
from datetime import datetime
from pymongo import Connection

"""
HOW TO USE: nytimes_parser sourcedir 

"""

def parse_xml(file):
	
	doc = etree.parse(file)
	root = doc.getroot()

	#meta data
	try: 
		newspaper = root.xpath('//pmtitle')[0].text
	except:
		newspaper = ""
		print "missing newspaper title: %s" % file
	try:
		pagenum = root.xpath('//startpg')[0].text
	except:
		pagenum = ""
		print "missing page number: %s" % file
	try:
		date_string = root.xpath('//pcdta')[0].text
		date_obj = datetime.strptime(date_string, '%b %d, %Y')
	except:
		date_obj = ""
		print "malformed date: %s" % file
	
	#body data
	try:
		headline = root.xpath('//doctitle')[0].text
	except:
		headline = ""
		print "missing headline: %s" % file

	try:
		body = root.xpath('//txtdt/text/*/text()')
		body = [str(element) for element in body]
		body = ' '.join(body)
	except:
		body = ""
		print "error parsing body text: %s" % file

	return {"newspaper": newspaper, "date": date_obj, "headline": headline, "body": body, "pagenum": pagenum, "file": os.path.basename(file)}

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
					#articles.insert(article)
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
