# Maths Jigsaw Puzzle #
A jigsaw puzzle app for macOS implemented in Java, tried to use some newer JDK features

 * Add jpeg files to ~/.jigsaw/images directory
 * Select an arithmetic operation for solving the puzzle  (~/.jigsaw/conf/conf.properties)
 * Select a jpeg image using File -> Open menu. 
 * This image will be cut into pieces and jumbled on the left pane
 * Click on right side and you get some arithmetic operations to solve
 * If you solve it right, then correct picture will be filled on the  right side
 
# Some JDK Features used #
* JPackage for macOS dmg creation
* Panama CLinker
* Records

# Build Pre Requsites #
* Install OpenJDK 16 in macOS
* Install gcc
* Update JAVA_HOME in build.sh
* Execute build.sh and the app is ready


