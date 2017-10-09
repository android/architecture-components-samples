#!/bin/bash
current_dir=$PWD;
for line in `find . -name 'gradlew'`
do
  cd $current_dir;
  echo "will run tests for ${line}";
  cd $(dirname "${line}");
  pwd
  adb shell input keyevent 82 # unlock device
  ./gradlew --no-daemon test cC;
done