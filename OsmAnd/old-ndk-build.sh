#!/bin/bash

SCRIPT_LOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
NAME=$(basename $(dirname "${BASH_SOURCE[0]}") )

if [ ! -d "$ANDROID_SDK" ]; then
    echo "ANDROID_SDK is not set"
    exit 1
fi
export ANDROID_SDK_ROOT=$ANDROID_SDK

if [ ! -d "$ANDROID_NDK" ]; then
	echo "ANDROID_NDK is not set"
	exit 1
fi

"$SCRIPT_LOC/../../core-legacy/externals/configure.sh"
(cd "$SCRIPT_LOC" && "$ANDROID_NDK/ndk-build" -j1 $*)
