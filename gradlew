#!/bin/sh

#
# Copyright © 2015–2025 the original authors.
#
# Licensed under the Apache License, Version 2.0
# https://www.apache.org/licenses/LICENSE-2.0
#

##############################################################################
#
#   Gradle start up script for POSIX
#
##############################################################################

# Resolve links: $0 may be a symlink
app_path=$0

while
  APP_HOME=${app_path%"${app_path##*/}"}  # dirname
  [ -h "$app_path" ]
do
  link=$(ls -ld "$app_path")
  link=${link#* -> }
  case $link in
    /*)   app_path=$link ;;
    *)    app_path=$APP_HOME$link ;;
  esac
done

APP_BASE_NAME=${0##*/}
APP_HOME=$(cd "${APP_HOME:-./}" > /dev/null && pwd -P) || exit

MAX_FD=maximum

warn() {
  echo "$*" >&2
}

die() {
  echo
  echo "$*" >&2
  echo
  exit 1
}

cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN*)  cygwin=true ;;
  Darwin*)  darwin=true ;;
  MSYS*|MINGW*) msys=true ;;
  NONSTOP*) nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

if [ -n "$JAVA_HOME" ]; then
  if [ -x "$JAVA_HOME/jre/sh/java" ]; then
    JAVACMD=$JAVA_HOME/jre/sh/java
  else
    JAVACMD=$JAVA_HOME/bin/java
  fi
  if [ ! -x "$JAVACMD" ]; then
    die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable to the correct Java installation directory."
  fi
else
  JAVACMD=java
  if ! command -v java >/dev/null 2>&1; then
    die "ERROR: JAVA_HOME is not set and 'java' could not be found in PATH.

Please install Java or set JAVA_HOME accordingly."
  fi
fi

if ! "$cygwin" && ! "$darwin" && ! "$nonstop"; then
  case $MAX_FD in
    max*)
      MAX_FD=$(ulimit -H -n) || warn "Could not query max file descriptor limit" ;;
  esac
  case $MAX_FD in
    ''|soft) ;;
    *)
      ulimit -n "$MAX_FD" || warn "Could not set max file descriptor limit to $MAX_FD" ;;
  esac
fi

if "$cygwin" || "$msys"; then
  APP_HOME=$(cygpath --path --mixed "$APP_HOME")
  CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")
  JAVACMD=$(cygpath --unix "$JAVACMD")
  for arg; do
    case $arg in
      -*) ;;
      /?*) if [ -e "${arg%%/*}" ]; then arg=$(cygpath --path --ignore --mixed "$arg"); fi ;;
    esac
    shift
    set -- "$@" "$arg"
  done
fi

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

set -- \
  "-Dorg.gradle.appname=$APP_BASE_NAME" \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"

if ! command -v xargs >/dev/null 2>&1; then
  die "ERROR: xargs is required but not found"
fi

eval "set -- $(
  printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
  xargs -n1 |
  sed 's~[^-[:alnum:]+,./:=@_]~\\&~g' |
  tr '\n' ' '
) \"$@\""

exec "$JAVACMD" "$@"
