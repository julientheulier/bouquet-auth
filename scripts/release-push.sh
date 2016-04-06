#!/bin/bash  

workdir="$1";
if [ -z $workdir ]
  then
  	workdir="/tmp";
fi

echo "workdir : $workdir";
cd $workdir;

#
# push
#

cd bouquet-auth;
git push --tags
git push origin master
git push origin develop
cd ..;

