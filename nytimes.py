from lxml import etree
import sys
import os
import yaml 

main_path =  "/home/rebecca/Desktop/final project/"
os.chdir(main_path)

def parseXML(file):
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

	article = {"year":year, "month":month, "date":date, "day":day, "pagenum": pagenum, "pagesect":pagesect, "pagecol": pagecol, "descriptors": descriptors, "taxclass": taxclass, "general_desc": general_desc, "headline": headline, "lead": lead, "body":body}
	return article


articles = []
year_path = main_path + "2000/"
year_listing = os.listdir(year_path)
for month_dir in year_listing:
	month_path = year_path + "/%s" % month_dir
	os.chdir(month_path)
	month_listing = os.listdir(month_path)
	for day_dir in month_listing:
		day_path = month_path + "/%s" % day_dir
		os.chdir(day_path)
		day_listing = os.listdir(day_path)
		for file in day_listing:
			try:
				articles.append(parseXML(file))
			except:
				print "There is a problem with %s" %file 

os.chdir(main_path)
f = open('nytimes2000.yaml', "w")
yaml.dump(articles[0], f)
f.close()
