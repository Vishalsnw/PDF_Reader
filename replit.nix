
{ pkgs }: {
  deps = [
    pkgs.openjdk17_headless
    pkgs.gradle
    pkgs.androidenv.androidPkgs_9_0.platform-tools
  ];
  
  env = {
    JAVA_HOME = "${pkgs.openjdk17_headless}";
    GRADLE_OPTS = "-Dorg.gradle.java.home=${pkgs.openjdk17_headless}";
  };
}
