#!/bin/bash
current_dir=$PWD;
for line in `find . -name 'gradlew'`
do
  cd $current_dir;
  echo "will run tests for ${line}";
  cd $(dirname "${line}");
  pwd
  ./gradlew test cC;
done