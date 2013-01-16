killall rmiregistry
rmiregistry &
java -cp "/news/lib/*" -Djava.rmi.server.hostname=`hostname` -Djava.rmi.server.codebase=file:/news/lib/news.jar edu.stanford.pcl.news.task.TaskServer