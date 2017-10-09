#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status.

for line in $(find . -name 'gradlew'); do
   p=$(dirname "${line}");
   echo
   echo
   echo Running unit and Android tests in $p
   echo "====================================================================="

   pushd $p > /dev/null  # Silent pushd
   ./gradlew :app:assembleDebug -PdisablePreDex | sed "s@^@$p @"  # Prefix every line with directory
   ./gradlew :app:assembleAndroidTest -PdisablePreDex | sed "s@^@$p @"  # Prefix every line with directory
   ./gradlew test | sed "s@^@$p @"  # Prefix every line with directory
   echo "y" | sudo /opt/google-cloud-sdk/bin/gcloud firebase test android run --app app/build/outputs/apk/debug/app-debug.apk --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk -d Nexus5X -v 21,25 -l fr --results-bucket cloud-test-android-devrel-ci
   code=${PIPESTATUS[0]}
   if [ "$code" -ne "0" ]; then
       exit $code
   fi
   popd > /dev/null  # Silent popd
done

echo
echo "ALL TESTS PASS"
