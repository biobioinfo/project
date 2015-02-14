#!bin/bash
# This script helps to install the javailp and minisatjni to enable integer linear programming (ILP)

JAVAILPPATH="$1"
MINISATJNIPATH="$2"
GROUPID="net.sf"
PACKAGING="jar"

mvn install:install-file -Dfile=$JAVAILPPATH -DgroupId=$GROUPID -DartifactId="javailp" -Dversion="1.2a" -Dpackaging=$PACKAGING
mvn install:install-file -Dfile=$MINISATJNIPATH -DgroupId=$GROUPID -DartifactId="minisatjni" -Dversion="1.0" -Dpackaging=$PACKAGING
