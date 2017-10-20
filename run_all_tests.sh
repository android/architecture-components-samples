#!/bin/bash
current_dir=$PWD;
for line in `find . -name 'gradlew'`
do
  cd $current_dir;
  echo "will run tests for ${line}";
  cd $(dirname "${line}");
  pwd
  #adb shell input keyevent 82 # unlock device
  ./gradlew clean && ./gradlew --no-daemon test cC;
  if [ $? -eq 0 ]
  then
    echo "tests for ${line} are successful"
  else
    echo "tests for ${line} FAILED"
    exit 1
  fi
done