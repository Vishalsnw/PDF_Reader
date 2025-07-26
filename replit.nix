{ pkgs }: {
  deps = [
    pkgs.jdk17
    pkgs.gradle
    pkgs.android-tools
    pkgs.unzip
    pkgs.wget
  ];

  env = {
    JAVA_HOME = "${pkgs.jdk17}";
    ANDROID_HOME = "/tmp/android-sdk";
    ANDROID_SDK_ROOT = "/tmp/android-sdk";
  };
}