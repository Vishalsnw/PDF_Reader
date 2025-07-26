
{ pkgs }: {
  deps = [
    pkgs.openjdk17
    pkgs.gradle
    pkgs.androidenv.androidPkgs_9_0.platform-tools
  ];
  
  env = {
    JAVA_HOME = "${pkgs.openjdk17}/lib/openjdk";
    GRADLE_OPTS = "-Dorg.gradle.java.home=${pkgs.openjdk17}/lib/openjdk";
  };
}
