import os
import multiprocessing
import subprocess

def work(file):
	return subprocess.call(["/usr/bin/python", "/home/rebecca/Desktop/fp/newspaper-project/nytimes_nlp_worker.py", file], shell=False)

def main():
	articles = []
	for root, dirs, files in os.walk('/home/rebecca/Desktop/fp/2000'):
		for file in files:
			f = os.path.join(root, file)
			articles.append(f)
	print "Traversed.  Working..."
	count = multiprocessing.cpu_count()
	pool = multiprocessing.Pool(processes=count)
	pool.map(work, articles)
	print "Done with everything."

if __name__ == '__main__':
	main()
