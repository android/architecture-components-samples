#!/bin/bash
current_dir=$PWD;
test_out=$1
mkdir $test_out
for line in `find . -name 'gradlew'`
do
  cd $current_dir;
  echo "will collect test results from ${line}";
  folder=$(dirname "${line}")
  project_name=$(basename "${folder}")
  cd $project_name
  project_out="${test_out}/${project_name}"
  mkdir $project_out
  for out_folder in `find . -type d -regex ".*/test-results/*"`
  do
    cp -R $out_folder $project_out
  done
  for out_folder in `find . -type d -regex ".*/androidTest-results/connected/*"`
  do
    cp -R $out_folder $project_out
  done
  pwd
done