#!/bin/sh

# Gradle wrapper script
# This is a simplified wrapper that downloads Gradle if needed

# Determine the project root dir
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"

# Download Gradle wrapper if not present
if [ ! -f "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
    mkdir -p "$SCRIPT_DIR/gradle/wrapper"
    echo "Downloading Gradle wrapper..."
    curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.10.0/gradle/wrapper/gradle-wrapper.jar" -o "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null || {
        echo "Cannot download gradle-wrapper.jar" >&2
        echo "Please ensure gradle/wrapper/gradle-wrapper.jar exists" >&2
        exit 1
    }
fi

# Create wrapper properties if not exist
if [ ! -f "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.properties" ]; then
    mkdir -p "$SCRIPT_DIR/gradle/wrapper"
    cat > "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.properties" << 'WRAPPER_PROPS'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
WRAPPER_PROPS
fi

# Execute Gradle wrapper
exec java -Dorg.gradle.appname=gradlew -classpath "$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
