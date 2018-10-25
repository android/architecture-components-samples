#!/bin/bash
# Copies the master versions.gradle file to each sample, to make them 
# compatible with Android Studio's "Import sample" feature.
for line in `find . -name 'gradlew'`
do
  p=$(dirname "${line}");
  echo "Copying versions.gradle -> ${p}";
  cp versions.gradle "$p"

  # remove the "ADMIN" line from the samples themselves
  extraArg=""
  if [[ $OSTYPE == darwin* ]]; then
    # The macOS version of sed requires the backup file extension
    # to be specified. Linux requires it not to be.
    extraArg=".bak"
  fi
  sed -i $extraArg 's/.*\[ADMIN\].*//' $p/versions.gradle

  cp gradle-wrapper.properties "$p/gradle/wrapper/."
done
# Remove the generated backup files
echo "Removing backup files"

# ignore output saying backups not found
find . -name '*.bak' | xargs rm 2> /dev/null
