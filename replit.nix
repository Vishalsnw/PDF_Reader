
{ pkgs }: {
  deps = [
    pkgs.jdk17
    pkgs.gradle_7
    pkgs.android-tools
  ];
  
  env = {
    JAVA_HOME = "${pkgs.jdk17}/lib/openjdk";
    ANDROID_HOME = "${pkgs.android-tools}";
    PATH = "${pkgs.jdk17}/bin:${pkgs.gradle_7}/bin:${pkgs.android-tools}/bin";
  };
}
