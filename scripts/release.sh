#!/bin/bash  
direc="bouquet-auth";
dirprivate="";
alldirs="$direc $dirprivate";


if [ -z "$1" ]
  then
  	echo "No version name"
    echo usage: $0 version next-version workdir
    exit
fi
if [ -z "$2" ]
  then
  	echo "No next version name"
    echo usage: $0 version next-version workdir
    exit
fi

workdir="$3";
if [ -z $workdir ]
  then
  	workdir="/tmp";
fi

echo "workdir : $workdir";
cd $workdir;

# repositories setup
for i in $direc
do
	git clone git@github.com:openbouquet/$i
	cd $i;
	git pull
	git checkout develop
	cd ..;
done
for i in $dirprivate
do
	git clone git@admin.squidsolutions.com:$i
	cd $i;
	git pull
	git checkout develop
	cd ..;
done

#
# start releases
#
for i in $alldirs
do
		echo starting release $1 for $i
        cd $i;
        git checkout -b release/$1
        cd ..;
done

#
# set release branches version to $1
#
cd bouquet-auth
mvn versions:set -DnewVersion=$1 -DgenerateBackupPoms=false
cd ..;
for i in $alldirs
do
		echo committing $i as $1
        cd $i;
        git commit -am"set master branch pom version to $1"
        cd ..;
done

#
# finish gitflow releases
#
for i in $alldirs
do
		
        cd $i;
        echo finishing $i
        git checkout master
        git merge --no-edit release/$1
        git tag -am"$1" $1
        git branch -d release/$1
        git checkout develop
        # back merge to develop
        git merge --no-edit master
        cd ..;
done

#
# set develop branches version to to next version $2-SNAPSHOT
#
cd bouquet-auth
mvn versions:set -DnewVersion=$2-SNAPSHOT -DgenerateBackupPoms=false
cd ..;
for i in $alldirs
do
        cd $i;
        echo setting develop branch $i to $2-SNAPSHOT
        git commit -am"set develop branch pom version to $2-SNAPSHOT"
        cd ..;
done

cd ..

