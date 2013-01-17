
To build, see BUILD.txt

To run, you will need to:

*


EBS Volume Information

  * Raw Data
    * tgz                           # contains the master archives of all news documents
    * xml                           # contains the uncompressed contents of the news archives

  * MongoDB
    * db                            # contains the main application database

Instance Information

  * Amazon Linux TaskServer
    * filesystem
      * /news                       # root directory for news project
        * bin
        * data -> /mnt/sdf/xml
        * db -> /mnt/sdg/db
        * lib
        * vendor
      * /mnt/sdf                    # mount point for Raw Data EBS volume
      * /mnt/sdg                    # mount point for MongoDB EBS volume


Notes on setting up the task server:

* Start with an Amazon Linux instance.
  * M1 Medium instance type
  * TaskCluster security group
  * Setup:
    # sudo -i
    # yum update
    # mkdir /news
    # chown ec2-user:ec2-user /news
    # mkdir /mnt/sdf /mnt/sdg       # might have to adjust these names
    # echo "/dev/sdf    /mnt/sdf    ext4    defaults,ro     0   0" >> /etc/fstab
    # echo "/dev/sdg    /mnt/sdg    ext4    defaults        0   0" >> /etc/fstab
    # exit
    $ echo "export AWS_ACCESS_KEY=aws-access-key ABCDEFGHIJKLMNOPQRST" >> ~/.bashrc
    $ echo "export AWS_SECRET_KEY=aws-access-key ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" >> ~/.bashrc
    $ mkdir /news/data
    $ ln -s /mnt/sdf/xml/ /news/data
    $ ln -s /mnt/sdg/db/ /news/db
    $ mkdir /news/bin
    $ mkdir /news/log
    $ # copy project files to server
    $ sudo ln -s /news/bin/news-TaskServer /etc/init.d/
    $ sudo ln -s /news/bin/news-TaskWorker /etc/init.d/



* Create an EBS volume for the database.  Mount to /news/db/

* Download and install MongoDB on the task server:
  # cd /news
  # wget http://fastdl.mongodb.org/linux/mongodb-linux-x86_64-2.2.2.tgz
  # tar -xzvf mongodb-linux-x86_64-2.2.2.tgz
  # mv mongodb-linux-x86_64-2.2.2 vendor/mongodb
  # rm mongodb-linux-x86_64-2.2.2.tgz

* Run mongodb.
  # vendor/mongodb/bin/mongod --dbpath /news/db