from lxml import etree
import os
import cPickle as pickle
#import glob

def parse_xml(file):
	doc = etree.parse(file)
	root = doc.getroot()

	#meta data
	pagenum = root.xpath('//head/meta[@name="print_page_number"]')[0].get("content")
	pagesect = root.xpath('//head/meta[@name="print_section"]')[0].get("content")
	pagecol = root.xpath('//head/meta[@name="print_column"]')[0].get("content")
	month = root.xpath('//head/meta[@name="publication_month"]')[0].get("content")
	date = root.xpath('//head/meta[@name="publication_day_of_month"]')[0].get("content")
	year = root.xpath('//head/meta[@name="publication_year"]')[0].get("content")
	day = root.xpath('//head/meta[@name="publication_day_of_week"]')[0].get("content")

	#docdata
	descriptors = root.xpath('//head/docdata/identified-content/classifier[@type="descriptor"]/text()')
	taxclass = root.xpath('//head/docdata/identified-content/classifier[@type="taxonomic_classifier"]/text()')
	general_desc = root.xpath('//head/docdata/identified-content/classifier[@type="general_descriptor"]/text()')

	#body data
	headline = root.xpath('//body/body.head/hedline/hl1')[0].text
	lead = root.xpath('//body/body.content/block[@class="lead_paragraph"]/p/text()')
	body = root.xpath('//body/body.content/block[@class="full_text"]/p/text()')

	#.xpath() returns some implicit methods which don't jive with pickle, so you want to make sure everything is a string
	descriptors = [str(element) for element in descriptors]
	taxclass = [str(element) for element in taxclass]
	general_desc = [str(element) for element in general_desc]
	lead = [str(element) for element in lead]
	lead = ' '.join(lead)
	body = [str(element) for element in body]
	body = ' '.join(body)

	article = {"year":year, "month":month, "date":date, "day":day, "pagenum": pagenum, "pagesect":pagesect, "pagecol": pagecol, "descriptors": descriptors, "taxclass": taxclass, "general_desc": general_desc, "headline": headline, "lead": lead, "body":body, 'file': file}

	return article

def parse_dir(root_dir):
	articles = []
	for root, dirs, files in os.walk(root_dir):
		for file in files:
			f = os.path.join(root, file)
			try:
				article = parse_xml(f)
				p = open(f + '.pkl', 'wb')
				pickle.dump(article, p)
				p.close()
			except:
				print "There is something wrong with %s" % file

#parsing NYTimes articles from 2000
def main():	
	main_path = "/home/rebecca/Desktop/fp/"
	parse_dir(main_path + '2000/')
	os.chdir(main_path)

if __name__ == '__main__':
	main()
	print "Done."
