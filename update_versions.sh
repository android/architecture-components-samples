#!/bin/bash
# Copies the master versions.gradle file to each sample, to make them 
# compatible with Android Studio's "Import sample" feature.
for line in `find . -name 'gradlew'`
do
  p=$(dirname "${line}");
  echo "Copying versions.gradle -> ${p}";
  cp versions.gradle "$p"
  sed -i 's/.*\[ADMIN\].*//' $p/versions.gradle
  cp gradle-wrapper.properties "$p/gradle/wrapper/."
done
